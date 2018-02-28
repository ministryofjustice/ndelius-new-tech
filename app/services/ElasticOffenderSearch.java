package services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.typesafe.config.Config;
import data.offendersearch.OffenderSearchResult;
import helpers.DateTimeHelper;
import helpers.Encryption;
import helpers.FutureListener;
import interfaces.OffenderApi;
import interfaces.OffenderSearch;
import lombok.val;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.suggest.SuggestBuilder;
import play.Logger;

import javax.inject.Inject;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.elasticsearch.index.query.MultiMatchQueryBuilder.Type.CROSS_FIELDS;
import static org.elasticsearch.index.query.MultiMatchQueryBuilder.Type.MOST_FIELDS;
import static org.elasticsearch.index.query.QueryBuilders.*;
import static org.elasticsearch.search.suggest.SuggestBuilders.termSuggestion;
import static play.libs.Json.parse;
import static services.helpers.CurrentDisposalAndNameComparator.currentDisposalAndNameComparator;
import static services.helpers.SearchResultAppenders.*;

public class ElasticOffenderSearch implements OffenderSearch {

    private final OffenderApi offenderApi;
    private final RestHighLevelClient elasticSearchClient;
    private final Function<String, String> encrypter;

    @Inject
    public ElasticOffenderSearch(Config configuration, RestHighLevelClient elasticSearchClient, OffenderApi offenderApi) {
        this.elasticSearchClient = elasticSearchClient;
        this.offenderApi = offenderApi;

        val paramsSecretKey = configuration.getString("params.secret.key");

        encrypter = plainText -> Encryption.encrypt(plainText, paramsSecretKey);
    }

    @Override
    public CompletionStage<OffenderSearchResult> search(String bearerToken, String searchTerm, int pageSize, int pageNumber) {
        val listener = new FutureListener<SearchResponse>();
        elasticSearchClient.searchAsync(new SearchRequest("offender")
            .source(searchSourceFor(searchTerm, pageSize, pageNumber)), listener);
        return listener.stage().thenComposeAsync(response -> processSearchResponse(bearerToken, searchTerm, response));
    }

    @Override
    public CompletionStage<Boolean> isHealthy() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return elasticSearchClient.ping();
            } catch (IOException e) {
                Logger.error("Got an error calling ElasticSearch health endpoint", e);
                return false;
            }
        });
    }

    private SearchSourceBuilder searchSourceFor(String searchTerm, int pageSize, int pageNumber) {
        val boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.should().add(multiMatchQuery(termsWithoutDates(searchTerm))
            .field("firstName", 10)
            .field("surname", 10)
            .field("middleNames", 8)
            .field("offenderAliases.firstName", 8)
            .field("offenderAliases.surname", 8)
            .field("contactDetails.addresses.town")
            // CROSS_FIELDS Analyzes the query string into individual terms, then looks
            // for each term in any of the fields, as though they were one big field
            .type(CROSS_FIELDS));

        boolQueryBuilder.should().add(multiMatchQuery(termsWithoutSingleLetters(termsWithoutDates(searchTerm)))
            .field("gender")
            .field("otherIds.crn", 10)
            .field("otherIds.nomsNumber", 10)
            .field("otherIds.niNumber", 10)
            .field("otherIds.pncNumber", 10)
            .field("otherIds.croNumber", 10)
            .field("contactDetails.addresses.streetName")
            .field("contactDetails.addresses.county")
            .field("contactDetails.addresses.postcode", 10)
            // MOST_FIELDS Finds documents which match any field and combines the _score from each field
            .type(MOST_FIELDS));

        termsThatLookLikeDates(searchTerm).forEach(dateTerm ->
            boolQueryBuilder.should().add(multiMatchQuery(dateTerm)
                .field("dateOfBirth", 11)
                .lenient(true)));

        Stream.of(termsWithoutDates(searchTerm).split(" "))
            .filter(term -> !term.isEmpty())
            .forEach(term -> boolQueryBuilder.should().add(prefixQuery("firstName", term.toLowerCase()).boost(11)));

        val highlight = new HighlightBuilder().
                highlighterType("unified").
                field("*").
                preTags("").
                postTags("");

        val searchSource = new SearchSourceBuilder()
            .query(boolQueryBuilder)
            .highlighter(highlight)
            .postFilter(termQuery("softDeleted", false))
            .explain(Logger.isDebugEnabled())
            .size(pageSize)
            .from(pageSize * aValidPageNumberFor(pageNumber))
            .suggest(suggestionsFor(searchTerm));

        Logger.debug(searchSource.toString());
        return searchSource;
    }

    private String termsWithoutSingleLetters(String searchTerm) {
        return Stream.of(searchTerm.split(" "))
            .filter(term -> term.length() > 1)
            .collect(joining(" "));
    }

    private List<String> termsThatLookLikeDates(String searchTerm) {
        return Stream.of(searchTerm.split(" "))
            .filter(DateTimeHelper::canBeConvertedToADate)
            .map(term -> DateTimeHelper.covertToCanonicalDate(term).get())
            .collect(toList());
    }

    private String termsWithoutDates(String searchTerm) {
        return Stream.of(searchTerm.split(" "))
            .filter(term -> !DateTimeHelper.covertToCanonicalDate(term).isPresent())
            .collect(joining(" "));
    }

    private SuggestBuilder suggestionsFor(String searchTerm) {
        return new SuggestBuilder()
            .addSuggestion("surname", termSuggestion("surname").text(searchTerm))
            .addSuggestion("firstName", termSuggestion("firstName").text(searchTerm));
    }

    private CompletionStage<OffenderSearchResult> processSearchResponse(String bearerToken, String searchTerm, SearchResponse response) {
        logResults(response);

        val offenderNodesCompletionStages = stream(response.getHits().getHits())
                .sorted(currentDisposalAndNameComparator)
                .map(searchHit -> {
                    JsonNode offender = parse(searchHit.getSourceAsString());
                    return restrictAndEmbellishNode(bearerToken, searchTerm, offender, searchHit.getHighlightFields());
                })
                .collect(toList());

        return CompletableFuture.allOf(
                toCompletableFutureArray(offenderNodesCompletionStages))
                .thenApply(ignoredVoid ->
                        OffenderSearchResult.builder()
                                .offenders(offendersFromCompletionStages(offenderNodesCompletionStages))
                                .total(response.getHits().getTotalHits())
                                .suggestions(suggestionsIn(response))
                                .build());
    }

    private void logResults(SearchResponse response) {
        Logger.debug(() -> {
            try {
                return new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT).writeValueAsString(parse(response.toString()));
            } catch (Exception e) {
                return response.toString();
            }
        });
    }

    private CompletableFuture[] toCompletableFutureArray(List<CompletionStage<ObjectNode>> offenderNodesCompletionStages) {
        return offenderNodesCompletionStages
                .stream()
                .map(CompletionStage::toCompletableFuture)
                .toArray(CompletableFuture[]::new);
    }

    private List<JsonNode> offendersFromCompletionStages(List<CompletionStage<ObjectNode>> offenderNodes) {
        return offenderNodes
                .stream()
                .map(objectNodeCompletionStage -> objectNodeCompletionStage.toCompletableFuture().join())
                .collect(toList());
    }


    private JsonNode suggestionsIn(SearchResponse response) {
        return Optional.ofNullable(response.getSuggest())
            .map(suggest -> parse(suggest.toString()))
            .orElse(parse("{}"));
    }

    private int aValidPageNumberFor(int pageNumber) {
        return pageNumber >= 1 ? pageNumber - 1 : 0;
    }

    private CompletionStage<ObjectNode> restrictAndEmbellishNode(String bearerToken, String searchTerm, JsonNode node, Map<String, HighlightField> highlightFields) {
        return restrictViewOfOffenderIfNecessary(
                bearerToken,
                appendHighlightFields(
                    appendOffendersAge(
                        appendOneTimeNomisRef(bearerToken, (ObjectNode)node, encrypter)),
                    searchTerm,
                    highlightFields));
    }

    private ObjectNode appendHighlightFields(ObjectNode rootNode, String searchTerm, Map<String, HighlightField> highlightFields) {
        val highlightNode = JsonNodeFactory.instance.objectNode();
        highlightFields.forEach((key, value) -> {
            val arrayNode = JsonNodeFactory.instance.arrayNode();
            Stream.of(value.fragments()).forEach(text -> arrayNode.add(text.string()));
            highlightNode.set(key, arrayNode);
        });

        if (shouldHighlightDateOfBirth(rootNode, searchTerm)) {
            val arrayNode = JsonNodeFactory.instance.arrayNode();
            arrayNode.add(dateOfBirth(rootNode));
            highlightNode.set("dateOfBirth", arrayNode);
        }

        rootNode.set("highlight", highlightNode);
        return rootNode;
    }

    private boolean shouldHighlightDateOfBirth(ObjectNode rootNode, String searchTerm) {
        val dateOfBirth = dateOfBirth(rootNode);

        return Optional.ofNullable(dateOfBirth).map(dob -> doAnySearchTermsMatchDateOfBirth(searchTerm, dob)).orElse(false);
    }

    private boolean doAnySearchTermsMatchDateOfBirth(String searchTerm, String dateOfBirth) {
        return termsThatLookLikeDates(searchTerm).stream().anyMatch(currentDate -> currentDate.equals(dateOfBirth));
    }

    private CompletionStage<ObjectNode> restrictViewOfOffenderIfNecessary(String bearerToken, ObjectNode rootNode) {
        if (toBoolean(rootNode, "currentExclusion") || toBoolean(rootNode, "currentRestriction")) {
            return offenderApi.canAccess(bearerToken, rootNode.get("offenderId").asLong())
                    .thenApply(canAccess -> canAccess ? rootNode : restrictView(rootNode));
        }
        return CompletableFuture.completedFuture(rootNode);
    }

    private Boolean toBoolean(ObjectNode rootNode, String nodeName) {
        return Optional.ofNullable(rootNode.get(nodeName))
                .map(JsonNode::asBoolean).orElse(false);
    }

    private ObjectNode restrictView(ObjectNode rootNode) {
        val restrictedAccessRootNode = JsonNodeFactory.instance.objectNode();
        restrictedAccessRootNode
            .put("accessDenied", true)
            .put("offenderId", rootNode.get("offenderId").asLong())
            .set("otherIds", JsonNodeFactory.instance.objectNode().put("crn", rootNode.get("otherIds").get("crn").asText()));
        return restrictedAccessRootNode;
    }
}

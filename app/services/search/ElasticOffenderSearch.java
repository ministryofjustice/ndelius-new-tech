package services.search;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import data.offendersearch.OffenderSearchResult;
import helpers.FutureListener;
import interfaces.OffenderSearch;
import lombok.val;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.suggest.SuggestBuilder;
import play.Logger;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static helpers.DateTimeHelper.calculateAge;
import static java.time.Clock.systemUTC;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static org.elasticsearch.index.query.QueryBuilders.multiMatchQuery;
import static org.elasticsearch.search.suggest.SuggestBuilders.termSuggestion;
import static play.libs.Json.parse;

public class ElasticOffenderSearch implements OffenderSearch {

    private final RestHighLevelClient elasticSearchClient;

    @Inject
    public ElasticOffenderSearch(RestHighLevelClient elasticSearchClient) {
        this.elasticSearchClient = elasticSearchClient;
    }

    @Override
    public CompletionStage<OffenderSearchResult> search(String searchTerm, int pageSize, int pageNumber) {
        val listener = new FutureListener<SearchResponse>();
        elasticSearchClient.searchAsync(new SearchRequest("offender")
            .source(searchSourceFor(searchTerm, pageSize, pageNumber)), listener);
        return listener.stage().thenApply(this::processSearchResponse);
    }

    @Override
    public CompletionStage<Boolean> isHealthy() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return elasticSearchClient.ping();
            } catch (IOException e) {
                Logger.error(e.getMessage(), e);
                return false;
            }
        });
    }

    private SearchSourceBuilder searchSourceFor(String searchTerm, int pageSize, int pageNumber) {
        val searchSource = new SearchSourceBuilder()
            .query(multiMatchQuery(searchTerm,
                "firstName",
                "surname",
                "dateOfBirth",
                "gender",
                "otherIds.crn",
                "otherIds.nomsNumber",
                "otherIds.niNumber",
                "otherIds.pncNumber",
                "otherIds.croNumber",
                "offenderAliases.firstName",
                "offenderAliases.surname",
                "contactDetails.addresses.streetName",
                "contactDetails.addresses.town",
                "contactDetails.addresses.county",
                "contactDetails.addresses.postcode")
                .lenient(true)
            )
            .size(pageSize)
            .from(pageSize * aValidPageNumberFor(pageNumber))
            .suggest(suggestionsFor(searchTerm));
        Logger.debug(searchSource.toString());
        return searchSource;
    }

    private SuggestBuilder suggestionsFor(String searchTerm) {
        return new SuggestBuilder()
            .addSuggestion("surname", termSuggestion("surname").text(searchTerm))
            .addSuggestion("firstName", termSuggestion("firstName").text(searchTerm));
    }

    private OffenderSearchResult processSearchResponse(SearchResponse response) {
        Logger.debug(response.toString());

        val offenders =
            stream(response.getHits().getHits())
                .map(searchHit -> {
                    JsonNode offender = parse(searchHit.getSourceAsString());
                    return embellishNode(offender);
                }).collect(toList());

        return OffenderSearchResult.builder()
            .offenders(offenders)
            .total(response.getHits().getTotalHits())
            .suggestions(suggestionsIn(response))
            .build();
    }

    private JsonNode suggestionsIn(SearchResponse response) {
        return Optional.ofNullable(response.getSuggest())
            .map(suggest -> parse(suggest.toString()))
            .orElse(parse("{}"));
    }

    private int aValidPageNumberFor(int pageNumber) {
        return pageNumber >= 1 ? pageNumber - 1 : 0;
    }

    private JsonNode embellishNode(JsonNode node) {
        ObjectNode rootNode = (ObjectNode) node;
        JsonNode dateOfBirth = rootNode.get("dateOfBirth");

        return Optional.ofNullable(dateOfBirth)
            .map(dob -> rootNode.put("age", calculateAge(dob.asText(), systemUTC())))
            .orElse(rootNode);
    }

}
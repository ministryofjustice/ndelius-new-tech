package data.offendersearch;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class OffenderSearchResult {

    private List<JsonNode> offenders;
    private List<String> suggestions;
    private long total;
}

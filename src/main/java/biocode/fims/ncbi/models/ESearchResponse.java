package biocode.fims.ncbi.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author rjewing
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ESearchResponse {

    private ESearchResult eSearchResult;

    private ESearchResponse() {}

    public ESearchResponse(ESearchResult eSearchResult) {
        this.eSearchResult = eSearchResult;
    }

    @JsonProperty("eSearchResult")
    public ESearchResult geteSearchResult() {
        return eSearchResult;
    }

    @JsonProperty("esearchresult")
    public void seteSearchResult(ESearchResult eSearchResult) {
        this.eSearchResult = eSearchResult;
    }


}

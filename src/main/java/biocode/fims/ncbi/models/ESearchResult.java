package biocode.fims.ncbi.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * @author rjewing
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ESearchResult {

    private int count;
    private int retMax;
    private int retStart;
    private List<String> idList;

    private ESearchResult(){}

    public ESearchResult(int count, int retMax, int retStart, List<String> idList) {
        this.count = count;
        this.retMax = retMax;
        this.retStart = retStart;
        this.idList = idList;
    }

    public int getCount() {
        return count;
    }

    @JsonProperty("retmax")
    public int getRetMax() {
        return retMax;
    }

    @JsonProperty("retstart")
    public int getRetStart() {
        return retStart;
    }

    @JsonProperty("idlist")
    public List<String> getIdList() {
        return idList;
    }
}

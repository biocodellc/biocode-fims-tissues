package biocode.fims.ncbi.entrez;

import biocode.fims.ncbi.models.ESearchResponse;
import biocode.fims.ncbi.NCBIEntrezRequest;
import org.springframework.util.Assert;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.MediaType;
import java.util.HashMap;
import java.util.Map;

/**
 * Make http requests to the NCBI Entrez eSearch endpoint
 *
 * @author rjewing
 */

public class ESearchRequest extends NCBIEntrezRequest<ESearchResponse> {
    private final static String SERVICE_PATH = "esearch.fcgi";
    private final static String RET_MODE = "json";
    private final static int RET_START = 0;
    private final static int RET_MAX = 100000;

    public ESearchRequest(String db, String term, Client client) {
        super(SERVICE_PATH, client, "GET", biocode.fims.ncbi.models.ESearchResponse.class);
        setQueryParams(getDefaultQueryParams(db, term));
        setAccepts(MediaType.APPLICATION_JSON_TYPE);
    }

    private Map<String, Object[]> getDefaultQueryParams(String db, String term) {
        Assert.hasText(db, "Required parameter db must not be empty");
        Assert.hasText(term, "Required parameter term must not be empty");

        Map<String, Object[]> queryParams = new HashMap<>();
        queryParams.put("db", new Object[]{db});
        queryParams.put("term", new Object[]{term});
        queryParams.put("retmode", new Object[]{RET_MODE});
        queryParams.put("retstart", new Object[]{RET_START});
        queryParams.put("retmax", new Object[]{RET_MAX});

        return queryParams;
    }
}
package biocode.fims.ncbi.entrez.requests;

import biocode.fims.ncbi.entrez.EntrezQueryParams;
import biocode.fims.ncbi.models.ESearchResponse;
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

public class ESearchRequestImpl extends AbstractEntrezRequest<ESearchResponse> implements ESearchRequest {
    private final static String SERVICE_PATH = "esearch.fcgi";
    private final static String RET_MODE = "json";
    public final static int RET_MAX = 100000;

    public ESearchRequestImpl(String db, String term, Client client) {
        super(SERVICE_PATH, client, "GET", biocode.fims.ncbi.models.ESearchResponse.class);
        setQueryParams(getDefaultQueryParams(db, term));
        setAccepts(MediaType.APPLICATION_JSON_TYPE);
    }

    private Map<String, Object[]> getDefaultQueryParams(String db, String term) {
        Assert.hasText(db, "Required parameter db must not be empty");
        Assert.hasText(term, "Required parameter term must not be empty");

        Map<String, Object[]> queryParams = new HashMap<>();
        queryParams.put(EntrezQueryParams.DB.getName(), new Object[]{db});
        queryParams.put(EntrezQueryParams.TERM.getName(), new Object[]{term});
        queryParams.put(EntrezQueryParams.RETRIEVAL_MODE.getName(), new Object[]{RET_MODE});
        queryParams.put(EntrezQueryParams.RETRIEVAL_START.getName(), new Object[]{currentPage});
        queryParams.put(EntrezQueryParams.RETRIEVAL_MAX.getName(), new Object[]{RET_MAX});

        return queryParams;
    }
}
package biocode.fims.ncbi.entrez;

import biocode.fims.ncbi.NCBIEntrezRequest;
import biocode.fims.ncbi.models.ESearchResponse;
import org.elasticsearch.common.recycler.Recycler;
import org.glassfish.jersey.moxy.xml.MoxyXmlFeature;
import org.springframework.util.Assert;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Make a http requests to the NCBI Entrez eFetch endpoint
 *
 * @author rjewing
 */

public class EFetchRequest<T> extends NCBIEntrezRequest<T> {
    private final static String SERVICE_PATH = "efetch.fcgi";
    private final static String RET_MODE = "xml";
    private final static int RET_START = 0;
    private final static int RET_MAX = 100000;

    public EFetchRequest(String db, List<String> ids, Client client, Class<T> responseClass) {
        super(SERVICE_PATH, client, "POST", responseClass);
        registerDefaultClientFeatures(client);

        setQueryParams(getDefaultQueryParams(db));
        setHttpEntity(getDefaultHttpEntity(ids));
        setAccepts(MediaType.APPLICATION_ATOM_XML_TYPE);
    }

    private Entity getDefaultHttpEntity(List<String> ids) {
        Assert.notEmpty(ids, "Required parameter ids must not be empty");

        MultivaluedMap<String, String> formParams = new MultivaluedHashMap<>();
        formParams.addAll("id", ids);

        Form form = new Form(formParams);

        return Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE);
    }

    private Map<String, Object[]> getDefaultQueryParams(String db) {
        Assert.hasText(db, "Required parameter db must not be empty");

        Map<String, Object[]> queryParams = new HashMap<>();
        queryParams.put("db", new Object[]{db});
        queryParams.put("retmode", new Object[]{RET_MODE});
        queryParams.put("retstart", new Object[]{RET_START});
        queryParams.put("retmax", new Object[]{RET_MAX});

        return queryParams;
    }

    private void registerDefaultClientFeatures(Client client) {
        Configuration config = client.getConfiguration();

        if (!config.isRegistered(MoxyXmlFeature.class)) {
            client.register(MoxyXmlFeature.class);
        }
    }
}
package biocode.fims.ncbi.entrez;

import biocode.fims.ncbi.NCBIDatabase;
import biocode.fims.ncbi.entrez.requests.*;

import javax.ws.rs.client.Client;
import java.util.List;

/**
 * @author rjewing
 */
public class EntrezApiFactoryImpl implements EntrezApiFactory {
    private final Client client;

    public EntrezApiFactoryImpl(Client client) {
        this.client = client;
    }

    @Override
    public ESearchRequest getBioSampleESearchRequest() {
        return new ESearchRequestImpl(NCBIDatabase.BIO_SAMPLE.getName(), "bcid[Attribute Name]", client);
    }

    @Override
    public BioSampleEFetchRequest getBioSamplesFromIds(List<String> bioSampleIds) {
        return new BioSampleEFetchRequestImpl(bioSampleIds, client);
    }

    @Override
    public ESearchRequest getSraESearchRequest(String bioProjectId) {
        return new ESearchRequestImpl(NCBIDatabase.SRA.getName(), bioProjectId + "[BioProject]", client);
    }

    @Override
    public SraEFetchRequest getSraExperimentsFromIds(List<String> experimentPackageIds) {
        return new SraEFetchRequestImpl(experimentPackageIds, client);
    }
}

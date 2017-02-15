package biocode.fims.ncbi.entrez;

import biocode.fims.ncbi.entrez.requests.BioSampleEFetchRequest;
import biocode.fims.ncbi.entrez.requests.ESearchRequest;
import biocode.fims.ncbi.entrez.requests.SraEFetchRequest;

import java.util.List;

/**
 * @author rjewing
 */
public interface EntrezApiFactory {
    ESearchRequest getBioSampleESearchRequest();

    BioSampleEFetchRequest getBioSamplesFromIds(List<String> bioSampleIds);

    ESearchRequest getSraESearchRequest(String bioProjectId);

    SraEFetchRequest getSraExperimentsFromIds(List<String> experimentPackageIds);
}

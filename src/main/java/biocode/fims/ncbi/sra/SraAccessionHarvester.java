package biocode.fims.ncbi.sra;

import biocode.fims.ncbi.entrez.requests.BioSampleEFetchRequestImpl;
import biocode.fims.ncbi.models.BioSample;
import org.springframework.util.Assert;

import javax.ws.rs.client.Client;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * class for harvesting accession's for fimMetadata resource sequences submitted the NCBI SRA database
 *
 * @author rjewing
 */
public class SraAccessionHarvester {


    private final Client client;
    private final List<String> bcids;

    public SraAccessionHarvester(Client client, List<String> bcids) {
        this.client = client;
        this.bcids = bcids;
        Assert.notEmpty(bcids);
        Assert.notNull(client);
    }

    public List<Map> harvest() {
        return new ArrayList<Map>();

//        ESearchRequestImpl bioSamplesSearchRequest = EntrezRequest.searchForBioSamplesWithBcidAttribute(client);
//        ESearchResult bioSampleSearchResult = bioSamplesSearchRequest
//                .execute()
//                .geteSearchResult();
//
//        List<BioSample> bioSamples = fetchBioSamples(client, bioSampleSearchResult.getIdList());
//
//        while (bioSampleSearchResult.getRetrievalStart() * bioSampleSearchResult.getRetrievalMax() > bioSampleSearchResult.getCount()) {
//
//            bioSamplesSearchRequest.addQueryParam(
//                    EntrezQueryParams.RETRIEVAL_START.getName(),
//                    bioSampleSearchResult.getRetrievalStart() + 1
//            );
//
//            bioSampleSearchResult = bioSamplesSearchRequest
//                    .execute()
//                    .geteSearchResult();
//
//            bioSamples.addAll(fetchBioSamples(client, bioSampleSearchResult.getIdList()));
//        }
//
//        List<BioSample> filteredBioSamples = filterBioSamples(bioSamples, bcids);
//        List<String> bioProjectIdsToFetchSraData = getBeoProjectIdsForBioSamples(filteredBioSamples);

//        List<>

    }

    private List<String> getBioProjectIdsForBioSamples(List<BioSample> bioSamples) {
        List<String> bioProjectIds = new ArrayList<>();

        for (BioSample bioSample : bioSamples) {
            if (!bioProjectIds.contains(bioSample.getBioProjectId())) {
                bioProjectIds.add(bioSample.getBioProjectId());
            }
        }

        return bioProjectIds;
    }

    private List<BioSample> fetchBioSamples(Client client, List<String> ids) {
        List<BioSample> bioSamples = new ArrayList<>();
        int chunkCounter = 0;
        int maxResultsSize = BioSampleEFetchRequestImpl.RET_MAX;

        do {
            List<String> idsToFetch = getIdsChunk(ids, maxResultsSize, chunkCounter * maxResultsSize);
//            BioSampleEFetchResult bioSampleFetchResult = EntrezRequest.fetchBioSamples(client, idsToFetch)
//                    .execute();

//            bioSamples.addAll(bioSampleFetchResult.getBioSamplesFromIds());

            chunkCounter++;
        }
        while (chunkCounter * maxResultsSize < ids.size());

        return bioSamples;

    }

    private List<String> getIdsChunk(List<String> ids, int chunkSize, int startIndex) {
        int lastIndex = (startIndex + chunkSize) < ids.size() ? startIndex + chunkSize : ids.size() - 1;
        return ids.subList(startIndex, lastIndex);
    }

    private List<BioSample> filterBioSamples(List<BioSample> bioSamples, List<String> bcids) {
        List<BioSample> filteredBioSamples = new ArrayList<>();

        for (BioSample bioSample : bioSamples) {
            if (bcids.contains(bioSample.getBcid())) {
                filteredBioSamples.add(bioSample);
            }
        }

        return filteredBioSamples;
    }

}

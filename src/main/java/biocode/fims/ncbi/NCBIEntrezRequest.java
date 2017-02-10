package biocode.fims.ncbi;

import biocode.fims.api.services.AbstractRequest;
import biocode.fims.ncbi.entrez.BioSampleEFetchRequest;
import biocode.fims.ncbi.entrez.ESearchRequest;
import biocode.fims.ncbi.entrez.SraEFetchRequest;
import org.springframework.util.Assert;

import javax.ws.rs.client.Client;
import java.util.List;


/**
 * provides Factory methods for calling the NCBI Entrez api
 *
 * @author rjewing
 */
public class NCBIEntrezRequest<T> extends AbstractRequest<T> {
    private final static String BASE_URL = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/";

    protected NCBIEntrezRequest(String path, Client client, String method, Class<T> responseClass) {
        super(method, responseClass, client, path, BASE_URL);
    }

    /**
     * TODO maybe query by date to limit results from ncbi
     * <p>
     * get a list of {@link biocode.fims.ncbi.models.BioSample#id}s that have a "bcid" attribute from the NCBI BioSample
     * database
     *
     * @param client
     * @return
     */
    public static ESearchRequest searchForBioSamplesWithBcidAttribute(Client client) {
        return new ESearchRequest(NCBIDatabase.BIO_SAMPLE.getName(), "bcid[Attribute Name]", client);
    }

    /**
     * get a list of all SRA Experiment id's for a {@link biocode.fims.ncbi.models.BioSample#bioProjectId}
     * from the NCBI SRA database
     *
     * @param client
     * @param bioProjectId
     * @return
     */
    public static ESearchRequest searchForSRAByBioProject(Client client, String bioProjectId) {
        Assert.hasText(bioProjectId, "Parameter bioProjectId must not be empty");
        return new ESearchRequest(NCBIDatabase.SRA.getName(), bioProjectId + "[BioProject]", client);
    }

    /**
     * get the {@link biocode.fims.ncbi.models.BioSample}s from the NCBI BioSample database
     *
     * @param client
     * @param ids    the list ids of {@link biocode.fims.ncbi.models.BioSample}s to fetch
     * @return
     */
    public static BioSampleEFetchRequest fetchBioSamples(Client client, List<String> ids) {
        return new BioSampleEFetchRequest(ids, client);
    }

    /**
     * get the {@link biocode.fims.ncbi.models.SraExperimentPackage}s from the NCBI SRA database
     *
     * @param client
     * @param ids    the list ids of Sra Experiments to fetch
     * @return
     */
    public static SraEFetchRequest fetchSraExperimentPackages(Client client, List<String> ids) {
        return new SraEFetchRequest(ids, client);
    }
}

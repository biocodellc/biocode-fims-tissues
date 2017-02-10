package biocode.fims.ncbi.entrez;

import biocode.fims.ncbi.NCBIDatabase;
import biocode.fims.ncbi.models.BioSampleEFetchResult;

import javax.ws.rs.client.Client;
import java.util.List;

/**
 * Class for fetching BioSample entries from the NCBI Entrez efetch endpoint
 *
 * @author rjewing
 */
public class BioSampleEFetchRequest extends EFetchRequest<BioSampleEFetchResult> {

    public BioSampleEFetchRequest(List<String> ids, Client client) {
        super(NCBIDatabase.BIO_SAMPLE.getName(), ids, client, BioSampleEFetchResult.class);
    }
}

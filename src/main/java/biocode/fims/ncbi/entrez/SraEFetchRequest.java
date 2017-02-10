package biocode.fims.ncbi.entrez;

import biocode.fims.ncbi.NCBIDatabase;
import biocode.fims.ncbi.models.SraEFetchResult;

import javax.ws.rs.client.Client;
import java.util.List;

/**
 * Class for fetching SRA entries from the NCBI Entrez efetch endpoint
 *
 * @author rjewing
 */
public class SraEFetchRequest extends EFetchRequest<SraEFetchResult> {

    public SraEFetchRequest(List<String> ids, Client client) {
        super(NCBIDatabase.SRA.getName(), ids, client, SraEFetchResult.class);
    }
}

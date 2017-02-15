package biocode.fims.ncbi.sra;

import org.glassfish.jersey.client.JerseyClient;
import org.junit.Test;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * @author rjewing
 */
public class SraAccessionHarvesterTest {

    @Test(expected = IllegalArgumentException.class)
    public void noBcidsFailsFast() {
        Client client = ClientBuilder.newClient();
        SraAccessionHarvester harvester = new SraAccessionHarvester(client, new ArrayList<>());
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullBcidsFailsFast() {
        Client client = ClientBuilder.newClient();
        SraAccessionHarvester harvester = new SraAccessionHarvester(client,null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void noClientFailsFast() {
        List<String> bcids = Collections.singletonList("ark:/99999/A2");
        SraAccessionHarvester harvester = new SraAccessionHarvester(null, bcids);
    }

    @Test
    public void emptyListWhenNoSraAccessionsForGivenBcids() {
        List<String> bcids = Collections.singletonList("ark:/99999/A2");
        Client client = ClientBuilder.newClient();
        SraAccessionHarvester harvester = new SraAccessionHarvester(client, bcids);
        List<Map> accessions = harvester.harvest();
        assertTrue(accessions.isEmpty());
    }

}
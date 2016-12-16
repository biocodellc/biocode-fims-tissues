package biocode.fims.fileManagers.fasta;

import biocode.fims.run.ProcessController;
import org.json.simple.JSONObject;

import java.util.List;
import java.util.Map;

/**
 * Interface for handling fasta persistence
 */
public interface FastaPersistenceManager {

    void upload(ProcessController processController, Map<String,List<JSONObject>> fastaSequences, boolean newDataset);

    Map<String, List<JSONObject>> getFastaSequences(ProcessController processController, String conceptAlias);
}

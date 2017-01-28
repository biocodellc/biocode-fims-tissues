package biocode.fims.fasta.fileManagers;

import biocode.fims.run.ProcessController;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.util.Map;

/**
 * Interface for handling fasta persistence
 */
public interface FastaPersistenceManager {

    void upload(ProcessController processController, Map<String, ArrayNode> fastaSequences, boolean newDataset);

    Map<String, ArrayNode> getFastaSequences(ProcessController processController, String conceptAlias);
}

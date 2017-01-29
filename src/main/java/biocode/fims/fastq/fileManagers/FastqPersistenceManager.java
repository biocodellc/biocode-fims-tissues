package biocode.fims.fastq.fileManagers;

import biocode.fims.fastq.FastqMetadata;
import biocode.fims.run.ProcessController;

import java.util.Map;

/**
 * Interface for handling fasta persistence
 */
public interface FastqPersistenceManager {

    void upload(ProcessController processController, Map<String, FastqMetadata> resourceFastqMetadataObjects, boolean newDataset);

    Map<String, FastqMetadata> getFastqMetadata(ProcessController processController, String conceptAlias);
}

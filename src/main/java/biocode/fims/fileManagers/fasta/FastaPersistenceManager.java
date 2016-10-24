package biocode.fims.fileManagers.fasta;

import biocode.fims.fasta.FastaSequence;
import biocode.fims.run.ProcessController;

import java.util.List;

/**
 * Interface for handling fasta persistence
 */
public interface FastaPersistenceManager {

    void upload(ProcessController processController, List<FastaSequence> fastaSequences, boolean newDataset);
}

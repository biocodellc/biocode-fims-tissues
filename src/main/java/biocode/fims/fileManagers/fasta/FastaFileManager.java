package biocode.fims.fileManagers.fasta;

import biocode.fims.digester.Mapping;
import fasta.FastaSequence;
import biocode.fims.fileManagers.AuxilaryFileManager;
import biocode.fims.fileManagers.dataset.Dataset;
import biocode.fims.fimsExceptions.ServerErrorException;
import biocode.fims.renderers.RowMessage;
import biocode.fims.run.ProcessController;
import org.apache.commons.lang.StringUtils;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * AuxilaryFileManger implementation to handle fasta sequences
 */
public class FastaFileManager implements AuxilaryFileManager {
    private static final String NAME = "fasta";

    private final FastaPersistenceManager persistenceManager;

    private List<FastaSequence> fastaSequences = null;
    private ProcessController processController;
    private String filename;

    @Autowired
    public FastaFileManager(FastaPersistenceManager persistenceManager) {
        this.persistenceManager = persistenceManager;
    }

    /**
     * verify that the identifiers in the fasta file are in a dataset.
     *
     */
    @Override
    public boolean validate(Dataset dataset) {
        Assert.notNull(processController);

        if (filename != null) {
            processController.appendStatus("\nRunning FASTA validation");
            List<String> sampleIds = getUniqueIds(dataset);


            if (sampleIds.isEmpty()) {
                processController.addMessage(
                        processController.getMapping().getDefaultSheetName(),
                        new RowMessage("No sample data found", "Spreadsheet check", RowMessage.ERROR)
                );
                return false;
            }

            // parse the FASTA file to get an array of sequence identifiers
            fastaSequences = parseFasta();

            if (fastaSequences.isEmpty()) {
                processController.addMessage(
                        "FASTA",
                        new RowMessage("No data found", "FASTA check", RowMessage.ERROR)
                );
                return false;
            }

            // verify that all fastaIds exist in the dataset
            ArrayList<String> invalidIds = new ArrayList<>();
            for (FastaSequence sequence : fastaSequences) {
                if (!sampleIds.contains(sequence.getLocalIdentifier())) {
                    invalidIds.add(sequence.getLocalIdentifier());
                }
            }
            if (!invalidIds.isEmpty()) {
                int level;
                // this is an error if no ids exist in the dataset
                if (invalidIds.size() == fastaSequences.size()) {
                    level = RowMessage.ERROR;
                } else {
                    level = RowMessage.WARNING;
                    processController.setHasWarnings(true);
                }
                processController.addMessage(
                        "FASTA",
                        new RowMessage(StringUtils.join(invalidIds, ", "),
                                "The following sequences exist in the FASTA file, but not the dataset.", level)
                );
                if (level == RowMessage.ERROR) {
                    return false;
                }
            }
        }
        return true;
    }


    @Override
    public void upload(boolean newDataset) {
        persistenceManager.upload(processController, fastaSequences, newDataset);
    }

    private List<String> getUniqueIds(Dataset dataset) {
        List<String> sampleIds = new ArrayList<>();
        Mapping mapping = processController.getMapping();

        String uniqueKey = mapping.getDefaultSheetUniqueKey();

        for (Object obj : dataset.getSamples()) {
            JSONObject sample = (JSONObject) obj;
            if (sample.containsKey(uniqueKey)) {
                sampleIds.add(String.valueOf(sample.get(uniqueKey)));
            }
        }

        return sampleIds;
    }

    /**
     * parse the fasta file identifier-sequence pairs
     *
     * @return list of {@link FastaSequence} objects
     */
    private List<FastaSequence> parseFasta() {
        List<FastaSequence> sequences = new ArrayList<>();
        try {
            FileReader input = new FileReader(filename);
            BufferedReader bufRead = new BufferedReader(input);
            String line;
            String identifier = null;
            String sequence = "";

            while ((line = bufRead.readLine()) != null) {
                // > deliminates the next identifier, sequence block in the fasta file
                if (line.startsWith(">")) {
                    if (!sequence.isEmpty() || identifier != null) {
                        sequences.add(new FastaSequence(identifier, sequence));
                        // after putting the sequence into the hashmap, reset the sequence
                        sequence = "";
                    }
                    // parse the identifier - minus the deliminator
                    identifier = line.substring(1, line.indexOf(" "));
                } else {
                    // if we are here, we are inbetween 2 identifiers. This means this is all sequence data
                    sequence += line;
                }
            }

            // need to put the last sequence data into the hashmap
            if (identifier != null) {
                sequences.add(new FastaSequence(identifier, sequence));
            }
        } catch (IOException e) {
            throw new ServerErrorException(e);
        }
        return sequences;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void setFilename(String value) {
        this.filename = value;
    }

    @Override
    public void setProcessController(ProcessController processController) {
        this.processController = processController;
    }

    @Override
    public void close() {
        if (filename != null) {
            new File(filename).delete();
        }
    }
}

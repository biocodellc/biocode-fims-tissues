package biocode.fims.fileManagers.fastq;

import biocode.fims.digester.Field;
import biocode.fims.digester.List;
import biocode.fims.digester.Mapping;
import biocode.fims.digester.Validation;
import biocode.fims.fileManagers.AuxilaryFileManager;
import biocode.fims.fileManagers.dataset.Dataset;
import biocode.fims.renderers.RowMessage;
import biocode.fims.run.ProcessController;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.json.simple.JSONObject;
import org.openjena.atlas.json.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import java.io.*;
import java.util.ArrayList;

/**
 * AuxilaryFileManger implementation to handle fastq sequences
 */
public class FastqFileManager implements AuxilaryFileManager {
    private static final String NAME = "fastq";

    private ProcessController processController;
    private String filename;
    // TODO refactor to FastqMetadata object
    private JSONObject fastqMetadata;
    // TODO remove when fastqMetadata is refactored to FastqMetadata class
    private java.util.List<String> fastqFilenames;

    @Autowired
    public FastqFileManager() {
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void setFilename(String value) {
        this.filename = value;
    }

    public void setMetadata(JSONObject metadata) {
        this.fastqMetadata = metadata;
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

    @Override
    public boolean validate(Dataset dataset) {
        Assert.notNull(processController);
        boolean valid = true;
        if (filename != null || fastqMetadata != null) {
            if (fastqMetadata == null) {
                processController.addMessage(
                        "FASTQ",
                        new RowMessage("No metadata found", "FASTQ Metadata Check", RowMessage.ERROR)
                );
                return false;
            }

            if (filename == null) {
                processController.addMessage(
                        "FASTQ",
                        new RowMessage("No FASTQ file", "FASTQ filenames check", RowMessage.ERROR)
                );
            }

            if (!validateMetadata() || !validateFilenames(dataset)) {
                valid = false;
            }
        }

        return valid;
    }

    @Override
    public void upload(boolean newDataset) {

    }

    /**
     * validate that every sample has the corresponding fastqFile(s).
     *
     *
     * If fastqMetadata.libraryLayout == "single" then we are looking for a corresponding fastq filename of the format
     *
     *      {sampleUniqueKeyValue}.(fq/fastq)
     *
     *
     * If fastqMetadata.libraryLayout == "paired" then we are looking for a corresponding fastq filenames of the format
     *
     *      {sampleUniqueKeyValue}.1.(fq/fastq) and {sampleUniqueKeyValue}.2.(fq/fastq)
     *      or
     *      {sampleUniqueKeyValue}.R1.(fq/fastq) and {sampleUniqueKeyValue}.R2.(fq/fastq)
     *
     * @param dataset
     * @return
     */
    private boolean validateFilenames(Dataset dataset) {
        boolean paired = StringUtils.equalsIgnoreCase(String.valueOf(fastqMetadata.get("libraryLayout")), "paired");
        java.util.List<String> samplesMissingFiles = new ArrayList<>();
        fastqFilenames = parseFastqFilenames();

        String uniqueKey = processController.getMapping().getDefaultSheetUniqueKey();

        for (Object obj : dataset.getSamples()) {
            JSONObject sample = (JSONObject) obj;
            boolean foundFastqFiles = false;
            boolean found1 = false;
            boolean found2 = false;

            String sampleId = (String) sample.get(uniqueKey);

            for (String filename : fastqFilenames) {
                if (paired) {
                    if (!found1 &&
                            StringUtils.startsWithAny(filename, new String[] { sampleId + ".1", sampleId + ".R1" })) {
                        found1 = true;
                    } else if (!found2 &&
                            StringUtils.startsWithAny(filename, new String[] { sampleId + ".2", sampleId + ".R2" })) {
                        found2 = true;
                    }

                    if (found1 && found2) {
                        foundFastqFiles = true;
                        break;
                    }
                } else {
                    if (StringUtils.startsWithIgnoreCase(filename, sampleId + ".")) {
                        foundFastqFiles = true;
                        break;
                    }
                }
            }

            if (!foundFastqFiles) {
                samplesMissingFiles.add(sampleId);
            }
        }

        if (samplesMissingFiles.size() > 0) {
            processController.addMessage(
                    "FASTQ",
                    new RowMessage(
                            "FASTQ filenames data missing FASTQ filenames for the following " + uniqueKey + "'s : \n " + ArrayUtils.toString(samplesMissingFiles),
                            "FASTQ filenames check",
                            RowMessage.ERROR
                    )
            );

            return false;
        }

        return true;
    }

    /**
     * Parses the associated file of filenames and extracts the filenames.
     * We are looking for 1 filename per line.
     *
     * @return
     */
    private java.util.List<String> parseFastqFilenames() {
        java.util.List<String> fastqFilenames = new ArrayList<>();

        try (FileReader file = new FileReader(filename)) {
            BufferedReader br = new BufferedReader(file);

            String line;

            while ((line = br.readLine()) != null) {
                fastqFilenames.add(line.trim());
            }

        } catch (IOException e) {
            processController.addMessage(
                    "FASTQ",
                    new RowMessage("Unable to open FASTQ file", "FASTQ filenames check", RowMessage.ERROR)
            );
        }

        return fastqFilenames;
    }

    /**
     * loops the keys in fastqMetadata and verifies that the value of each key is in the List,
     * specified in the project Configuration
     *
     * @return
     */
    private boolean validateMetadata() {
        boolean valid = true;

        for (Object k : fastqMetadata.keySet()) {
            String key = String.valueOf(k);

            if (!StringUtils.equalsIgnoreCase(key, "filenames")) {
                if (!inList(key, String.valueOf(fastqMetadata.get(key)))) {
                    processController.addMessage(
                            "FASTQ",
                            new RowMessage(
                                    "Invalid value \"" + fastqMetadata.get(key) + "\" FASTQ metadata " + key,
                                    "FASTQ Metadata Check",
                                    RowMessage.ERROR
                            )
                    );
                    valid = false;
                }
            }

        }

        return valid;
    }

    private boolean inList(String listName, String value) {
        Validation validation = processController.getValidation();
        List list = validation.findList(listName);

        boolean containsValue = false;

        for (Field field : list.getFields()) {
            if (StringUtils.equalsIgnoreCase(field.getValue(), value)) {
                containsValue = true;
                break;
            }
        }

        return containsValue;
    }

    public java.util.List<String> getFastqFilenames() {
        return fastqFilenames;
    }
}

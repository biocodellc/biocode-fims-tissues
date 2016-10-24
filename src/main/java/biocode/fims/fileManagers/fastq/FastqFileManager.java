package biocode.fims.fileManagers.fastq;

import biocode.fims.digester.Field;
import biocode.fims.digester.List;
import biocode.fims.digester.Validation;
import biocode.fims.fileManagers.AuxilaryFileManager;
import biocode.fims.fileManagers.dataset.Dataset;
import biocode.fims.renderers.RowMessage;
import biocode.fims.run.ProcessController;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.springframework.util.PatternMatchUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * AuxilaryFileManger implementation to handle fastq sequences
 */
public class FastqFileManager implements AuxilaryFileManager {
    private static final Logger logger = LoggerFactory.getLogger(FastqFileManager.class);
    private static final String NAME = "fastq";
    public static final String PAIRED_FILE_1_PATTERN = "^{sampleId}(\\.|_).*1.*\\.(fq|fastq)$";
    public static final String PAIRED_FILE_2_PATTERN = "^{sampleId}(\\.|_).*2.*\\.(fq|fastq)$";
    public static final String SINGLE_FILE_PATTERN = "^{sampleId}\\.(fq|fastq)$";

    private ProcessController processController;
    private String filename;
    // TODO refactor to FastqMetadata object
    private JSONObject fastqMetadata;
    // TODO remove when fastqMetadata is refactored to FastqMetadata class
    private java.util.List<String> fastqFilenames;

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
            processController.appendStatus("\nRunning FASTQ validation");

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
                return false;
            }

            if (!validateMetadata()) {
                valid = false;
            }

            if (!validateFilenames(dataset)) {
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
     * <p>
     * <p>
     * If fastqMetadata.libraryLayout == "single" then we are looking for a corresponding fastq filename of the format
     * <p>
     * {sampleUniqueKeyValue}.(fq/fastq)
     * <p>
     * <p>
     * If fastqMetadata.libraryLayout == "paired" then we are looking for a corresponding fastq filenames with the
     * REGEXP:
     * <p>
     * "^{sampleUniqueKeyValue}(\.|_).*1.*\.(fq|fastq)$"
     *
     * @param dataset
     * @return
     */
    private boolean validateFilenames(Dataset dataset) {
        boolean paired = StringUtils.equalsIgnoreCase(String.valueOf(fastqMetadata.get("libraryLayout")), "paired");
        java.util.List<String> samplesMissingFiles = new ArrayList<>();
        fastqFilenames = parseFastqFilenames();

        // hack until we get FastqFileManger.upload working
        fastqMetadata.put("filenames", fastqFilenames);
        processController.setFastqMetadata(fastqMetadata);

        String uniqueKey = processController.getMapping().getDefaultSheetUniqueKey();

        for (Object obj : dataset.getSamples()) {
            JSONObject sample = (JSONObject) obj;
            boolean foundFastqFiles = false;
            boolean found1 = false;
            boolean found2 = false;

            String sampleId = (String) sample.get(uniqueKey);
            Pattern pairedEnd1Pattern = Pattern.compile(PAIRED_FILE_1_PATTERN.replace("{sampleId}", sampleId));
            Pattern pairedEnd2Pattern = Pattern.compile(PAIRED_FILE_2_PATTERN.replace("{sampleId}", sampleId));
            Pattern singleEndPattern = Pattern.compile(SINGLE_FILE_PATTERN.replace("{sampleId}", sampleId));

            for (String filename : fastqFilenames) {
                if (paired) {
                    if (!found1 && pairedEnd1Pattern.matcher(filename).matches()) {
                        found1 = true;
                    } else if (!found2 && pairedEnd2Pattern.matcher(filename).matches()) {
                        found2 = true;
                    }

                    if (found1 && found2) {
                        foundFastqFiles = true;
                        break;
                    }
                } else {
                    if (singleEndPattern.matcher(filename).matches()) {
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
                            "Missing filenames for the following " + uniqueKey + "'s : \n " + ArrayUtils.toString(samplesMissingFiles) +
                                    ". If this is paired data, be sure to include 2 fastq files for each sampleId.",
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

            while (!StringUtils.isBlank(line = br.readLine())) {
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
                String value = (String) fastqMetadata.get(key);
                if (value == null) {
                    processController.addMessage(
                            "FASTQ",
                            new RowMessage(
                                    "Invalid value \"" + value + "\" for FASTQ metadata: " + key,
                                    "FASTQ Metadata Check",
                                    RowMessage.ERROR
                            )
                    );
                    valid = false;
                } else {

                    // instrumentModel is a special case where the list is dependent on FastqMetadata.platform
                    if (StringUtils.equalsIgnoreCase(key, "instrumentModel")) {
                        key = (String) fastqMetadata.get("platform");
                    }

                    if (!inList(key, value)) {
                        valid = false;
                    }
                }
            }

        }

        return valid;
    }

    private boolean inList(String key, String value) {
        Validation validation = processController.getValidation();
        List list = validation.findList(key);

        // don't throw an exception if the list isn't defined
        if (list == null) {
            logger.warn("Couldn't find List for FastqMetadata key: " + key);
            return true;
        }

        boolean containsValue = false;

        for (Field field : list.getFields()) {
            if (StringUtils.equalsIgnoreCase(field.getValue(), value)) {
                containsValue = true;
                break;
            }
        }

        if (!containsValue) {
            processController.addMessage(
                    "FASTQ",
                    new RowMessage(
                            "Invalid value \"" + fastqMetadata.get(key) + "\" for FASTQ metadata: " + key,
                            "FASTQ Metadata Check",
                            RowMessage.ERROR
                    )
            );

        }

        return containsValue;
    }

    public java.util.List<String> getFastqFilenames() {
        return fastqFilenames;
    }
}

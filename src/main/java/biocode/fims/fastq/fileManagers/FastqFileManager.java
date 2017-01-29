package biocode.fims.fastq.fileManagers;

import biocode.fims.digester.Entity;
import biocode.fims.digester.Field;
import biocode.fims.digester.Mapping;
import biocode.fims.digester.Validation;
import biocode.fims.fastq.FastqMetadata;
import biocode.fims.fileManagers.AuxilaryFileManager;
import biocode.fims.fimsExceptions.FimsRuntimeException;
import biocode.fims.renderers.RowMessage;
import biocode.fims.rest.SpringObjectMapper;
import biocode.fims.run.ProcessController;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

/**
 * AuxilaryFileManger implementation to handle fastq sequences
 */
public class FastqFileManager implements AuxilaryFileManager {
    private static final Logger logger = LoggerFactory.getLogger(FastqFileManager.class);
    public static final String CONCEPT_ALIAS = "fastqMetadata";

    public static final String NAME = "fastq";
    public static final String PAIRED_FILE_1_PATTERN = "^{resourceId}(\\.|_).*1.*\\.(fq|fastq)$";
    public static final String PAIRED_FILE_2_PATTERN = "^{resourceId}(\\.|_).*2.*\\.(fq|fastq)$";
    public static final String SINGLE_FILE_PATTERN = "^{resourceId}\\.(fq|fastq)$";

    private final FastqPersistenceManager persistenceManager;
    private ProcessController processController;
    private String filename;
    private Map<String, FastqMetadata> resourceFastqMetadataObjects = new HashMap<>();
    private FastqMetadata fastqMetadata;

    public FastqFileManager(FastqPersistenceManager persistenceManager) {
        this.persistenceManager = persistenceManager;
    }

    @Override
    public String getName() {
        return NAME;
    }

    public void setFilename(String value) {
        this.filename = value;
    }

    public void setMetadata(FastqMetadata metadata) {
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
    public boolean validate(ArrayNode dataset) {
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
        persistenceManager.upload(processController, resourceFastqMetadataObjects, newDataset);
    }

    /**
     * Adds the fastqMetadata object to each entry in the dataset
     */
    @Override
    @SuppressWarnings("unchecked")
    public void index(ArrayNode dataset) {
        Mapping mapping = processController.getMapping();
        String uniqueKey = mapping.lookupUriForColumn(mapping.getDefaultSheetUniqueKey(), mapping.getDefaultSheetAttributes());

        copyExistingFastqMetadata(dataset);

        SpringObjectMapper objectMapper = new SpringObjectMapper();
        if (!resourceFastqMetadataObjects.isEmpty()) {

            for (JsonNode node : dataset) {
                ObjectNode resource = (ObjectNode) node;

                String localIdentifier = resource.get(uniqueKey).asText();

                if (resourceFastqMetadataObjects.containsKey(localIdentifier)) {
                    resource.set(CONCEPT_ALIAS, objectMapper.valueToTree(resourceFastqMetadataObjects.get(localIdentifier)));
                }
            }
        }

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
    private boolean validateFilenames(ArrayNode dataset) {
        final String sheetName = "FASTQ";
        final String groupMessage = "FASTQ filenames check";
        boolean paired = StringUtils.equalsIgnoreCase(fastqMetadata.getLibraryLayout(), FastqMetadata.PAIRED_LAYOUT);
        List<String> samplesMissingFiles = new ArrayList<>();
        List<String> fastqFilenames = parseFastqFilenames();

        if (fastqFilenames.isEmpty()) {
            processController.addMessage(
                    sheetName,
                    new RowMessage("Didn't find any fastq filenames when parsing provided file", groupMessage, RowMessage.ERROR)
            );
        }

        String uniqueKey = processController.getMapping().getDefaultSheetUniqueKey();

        String file1;
        String file2;
        boolean foundFastqFiles;
        boolean found1;
        boolean found2;
        boolean valid = true;

        for (JsonNode node : dataset) {
            ObjectNode resource = (ObjectNode) node;
            foundFastqFiles = false;
            file1 = null;
            file2 = null;
            found1 = false;
            found2 = false;

            String resourceId = resource.get(uniqueKey).asText();
            Pattern pairedEnd1Pattern = Pattern.compile(PAIRED_FILE_1_PATTERN.replace("{resourceId}", resourceId));
            Pattern pairedEnd2Pattern = Pattern.compile(PAIRED_FILE_2_PATTERN.replace("{resourceId}", resourceId));
            Pattern singleEndPattern = Pattern.compile(SINGLE_FILE_PATTERN.replace("{resourceId}", resourceId));

            for (String filename : fastqFilenames) {
                if (paired) {
                    if (!found1 && pairedEnd1Pattern.matcher(filename).matches()) {
                        found1 = true;
                        file1 = filename;
                    } else if (!found2 && pairedEnd2Pattern.matcher(filename).matches()) {
                        found2 = true;
                        file2 = filename;
                    }

                    if (found1 && found2) {
                        foundFastqFiles = true;
                        break;
                    }
                } else {
                    if (singleEndPattern.matcher(filename).matches()) {
                        foundFastqFiles = true;
                        file1 = filename;
                        break;
                    }
                }
            }

            if (paired &&
                    ((found1 && !found2) || (found2 && !found1))) {
                valid = false;
            }

            if (!foundFastqFiles) {
                samplesMissingFiles.add(resourceId);
            } else {
                FastqMetadata resourceFastqMetadata = deepClone(this.fastqMetadata);

                if (resourceFastqMetadata == null) {
                    throw new FimsRuntimeException("Error validating fastqMetadata", 500);
                }

                ArrayList<String> filenames = new ArrayList<>();
                filenames.add(file1);

                if (paired) {
                    filenames.add(file2);
                }

                resourceFastqMetadata.setFilenames(filenames);
                resourceFastqMetadataObjects.put(resourceId, resourceFastqMetadata);
            }
        }

        if (resourceFastqMetadataObjects.isEmpty()) {
            valid = false;
        }

        if (samplesMissingFiles.size() > 0) {
            int level;
            if (!valid) {
                level = RowMessage.ERROR;
            } else {
                level = RowMessage.WARNING;
                processController.setHasWarnings(true);
            }
            String msg = "Missing filenames for the following " + uniqueKey + "'s : \n " +
                    ArrayUtils.toString(samplesMissingFiles) + ".";
            if (paired) {
                msg += " Be sure to include 2 fastq filenames for each resourceId.";
            }
            processController.addMessage(
                    sheetName,
                    new RowMessage(
                            msg,
                            groupMessage,
                            level
                    )
            );
        }

        return valid;
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
     * looks at each key in fastqMetadata and verifies that the value of each key is in the List,
     * specified in the project Configuration
     *
     * @return
     */
    private boolean validateMetadata() {
        boolean valid = true;

        String libraryStrategy = fastqMetadata.getLibraryStrategy();
        if (!inList("libraryStrategy", libraryStrategy)) {
            valid = false;
        }
        String librarySource = fastqMetadata.getLibrarySource();
        if (!inList("librarySource", librarySource)) {
            valid = false;
        }
        String librarySelection = fastqMetadata.getLibrarySelection();
        if (!inList("librarySelection", librarySelection)) {
            valid = false;
        }
        String libraryLayout = fastqMetadata.getLibraryLayout();
        if (!inList("libraryLayout", libraryLayout)) {
            valid = false;
        }
        String platform = fastqMetadata.getPlatform();
        if (!inList("platform", platform)) {
            valid = false;
        }
        String instrumentModel = fastqMetadata.getInstrumentModel();
        if (!inList(fastqMetadata.getPlatform(), instrumentModel)) {
            valid = false;
        }
        String designDescription = fastqMetadata.getDesignDescription();
        if (!inList("designDescription", designDescription)) {
            valid = false;
        }

        return valid;
    }

    private boolean inList(String key, String value) {
        if (value == null) {
            processController.addMessage(
                    "FASTQ",
                    new RowMessage(
                            "Invalid value \"" + value + "\" for FASTQ metadata: " + key,
                            "FASTQ Metadata Check",
                            RowMessage.ERROR
                    )
            );
            return false;
        }
        Validation validation = processController.getValidation();
        biocode.fims.digester.List list = validation.findList(key);

        // don't throw an exception if the list isn't defined
        if (list == null) {
            logger.debug("Couldn't find List for FastqMetadata key: " + key);
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
                            "Invalid value \"" + value + "\" for FASTQ metadata: " + key,
                            "FASTQ Metadata Check",
                            RowMessage.ERROR
                    )
            );

        }

        return containsValue;
    }

    private FastqMetadata deepClone(FastqMetadata fastqMetadata) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(fastqMetadata);
            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            ObjectInputStream ois = new ObjectInputStream(bais);
            return (FastqMetadata) ois.readObject();
        } catch (Exception e) {
            logger.warn("", e);
            return null;
        }
    }

    /**
     * if any existing resources contain fastqMetadata and the resource exists in the new dataset, we need to copy the fastqMetdata
     * to the new dataset.
     *
     * @param dataset
     */
    private void copyExistingFastqMetadata(ArrayNode dataset) {
        Map<String, FastqMetadata> existingFastqMetadata = persistenceManager.getFastqMetadata(processController, CONCEPT_ALIAS);

        for (String identifier : existingFastqMetadata.keySet()) {

            if (!resourceFastqMetadataObjects.containsKey(identifier) && datasetContainsResource(dataset, identifier)) {
                resourceFastqMetadataObjects.put(identifier, existingFastqMetadata.get(identifier));
            }
        }

    }

    /**
     * check if a resource with the same local identifier exists in the dataset
     *
     * @param dataset
     * @param localIdentifier the value to check against the {@link Entity#getUniqueKey()} value for each resource in
     *                        the dataset
     * @return
     */
    private boolean datasetContainsResource(ArrayNode dataset, String localIdentifier) {
        // check that the dataset still contains the resource, and add the existingSequences if it does
        Mapping mapping = processController.getMapping();
        String uniqueKeyUri = mapping.lookupUriForColumn(mapping.getDefaultSheetUniqueKey(), mapping.getDefaultSheetAttributes());

        for (JsonNode node : dataset) {
            ObjectNode resource = (ObjectNode) node;

            if (resource.get(uniqueKeyUri).asText().equals(localIdentifier)) {
                return true;
            }
        }

        return false;
    }
}

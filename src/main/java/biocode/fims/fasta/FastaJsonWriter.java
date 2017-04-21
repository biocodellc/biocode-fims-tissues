package biocode.fims.fasta;

import biocode.fims.fimsExceptions.errorCodes.FileCode;
import biocode.fims.fimsExceptions.FimsRuntimeException;
import biocode.fims.query.writers.JsonFieldTransform;
import biocode.fims.query.writers.JsonWriter;
import biocode.fims.settings.PathManager;
import biocode.fims.utils.FileUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ValueNode;
import org.apache.commons.lang.StringUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class to write Json fastaSequences to a fasta file
 *
 * @author RJ Ewing
 */
public class FastaJsonWriter implements JsonWriter {
    private final ArrayNode resources;
    private final FastaSequenceFields fastaSequenceFields;
    private final String outputDirectory;
    private List<FastaSequenceJsonFieldFilter> fastaSequenceFilters;

    /**
     * @param resources           {@link ArrayNode} of {@link ObjectNode}'s. Each ObjectNode should contain an ArrayNode
     *                            of FastaSequences. Each {@link ObjectNode#fields()} in the FastaSequences ArrayNode
     *                            may only contain a basic java data type (String, Integer, int, etc...)
     * @param fastaSequenceFields
     * @param outputDirectory
     */
    public FastaJsonWriter(ArrayNode resources, FastaSequenceFields fastaSequenceFields, String outputDirectory) {
        this(resources, fastaSequenceFields, outputDirectory, new ArrayList<>());
    }

    /**
     * @param resources            {@link ArrayNode} of {@link ObjectNode}'s. Each ObjectNode should contain an ArrayNode
     *                             of FastaSequences. Each {@link ObjectNode#fields()} in the FastaSequences ArrayNode
     *                             may only contain a basic java data type (String, Integer, int, etc...)
     * @param fastaSequenceFields
     * @param outputDirectory
     * @param fastaSequenceFilters fastaSequence must match each filter in order to be written to file
     */
    public FastaJsonWriter(ArrayNode resources, FastaSequenceFields fastaSequenceFields, String outputDirectory,
                           List<FastaSequenceJsonFieldFilter> fastaSequenceFilters) {
        this.resources = resources;
        this.fastaSequenceFields = fastaSequenceFields;
        this.outputDirectory = outputDirectory;
        this.fastaSequenceFilters = fastaSequenceFilters;
    }

    /**
     * writes the given resources to a file, using the {@link FastaSequenceFields} object. If {@link FastaSequenceFields#getUniqueKeyPath()}
     * isn't null, a zip file will be returned containing a fasta file containing the fastaSequences objects for each unique
     * fastaSequence.{uniqueKeyPath} in the given resources
     *
     * @return
     */
    @Override
    public File write() {
        Map<String, List<SequenceData>> fastaFileMap = new HashMap<>();

        for (JsonNode resource : resources) {

            JsonNode sequencesNode = resource.path(fastaSequenceFields.getSequencesPath());

            ValueNode identifierNode = (ValueNode) resource.path(fastaSequenceFields.getIdentifierPath());
            String identifier = identifierNode.asText();

            if (!sequencesNode.isMissingNode()) {

                ArrayNode sequencesArrayNode = (ArrayNode) sequencesNode;

                for (JsonNode sequenceNode : sequencesArrayNode) {

                    if (writeSequence((ObjectNode) sequenceNode)) {

                        // add the sequenceNode to a map based on the uniqueKeyPath so we can later write 1 file for
                        // each key in the map
                        String uniqueKey = sequenceNode.path(fastaSequenceFields.getUniqueKeyPath()).asText();

                        fastaFileMap.computeIfAbsent(uniqueKey, k -> new ArrayList<>());

                        fastaFileMap.get(uniqueKey).add(new SequenceData(identifier, (ObjectNode) sequenceNode, (ObjectNode) resource));

                    }

                }
            }
        }

        return writeFiles(fastaFileMap);
    }

    /**
     * determines if the given sequenceNode should be written. this is done by comparing the
     * sequenceNode values against the fastaSequenceFilters
     *
     * @param sequenceNode
     * @return
     */
    private boolean writeSequence(ObjectNode sequenceNode) {

        for (FastaSequenceJsonFieldFilter filter : fastaSequenceFilters) {

            JsonNode filterNode = sequenceNode.path(filter.getPath());

            if (filterNode.isMissingNode() || !filterNode.isValueNode() ||
                    !filterNode.asText().equals(filter.getValue())) {
                return false;
            }
        }

        return true;
    }

    private File writeFiles(Map<String, List<SequenceData>> fileMap) {
        List<File> sequenceFiles = new ArrayList<>();

        for (Map.Entry<String, List<SequenceData>> entry : fileMap.entrySet()) {
            sequenceFiles.add(writeSequenceFile(entry.getValue(), entry.getKey()));
        }

        if (sequenceFiles.size() < 1) {
            try {
                return File.createTempFile("output", "fasta");
            } catch (IOException e) {
                throw new FimsRuntimeException(FileCode.WRITE_ERROR, 500);
            }
        } else if (sequenceFiles.size() == 1) {
            return sequenceFiles.get(0);
        } else {
            Map<String, File> fastaFileMap = new HashMap<>();

            for (File fastaFile : sequenceFiles) {
                fastaFileMap.put(fastaFile.getName(), fastaFile);
            }

            return FileUtils.zip(fastaFileMap, outputDirectory);
        }
    }

    private File writeSequenceFile(List<SequenceData> sequences, String sequenceUniqueKey) {
        String filename = StringUtils.isBlank(sequenceUniqueKey) ? "output.fasta" : sequenceUniqueKey + ".fasta";
        File file = PathManager.createFile(filename, outputDirectory);


        try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(file)))) {

            for (SequenceData s : sequences) {
                ValueNode sequence = (ValueNode) s.getSequenceNode().path(fastaSequenceFields.getSequencePath());

                writer.write("> ");
                writer.write(s.getIdentifier());

                StringBuilder metadataBuilder = new StringBuilder();

                for (JsonFieldTransform field : fastaSequenceFields.getMetadata()) {
                    // metadata can be in the sequenceNode or the resourceNode
                    JsonNode fieldNode = s.getResourceNode().at(field.getPath());

                    if (fieldNode.isMissingNode()) {
                        fieldNode = s.getSequenceNode().at(field.getPath());
                    }

                    if (!fieldNode.isMissingNode()) {
                        metadataBuilder.append(" [");
                        metadataBuilder.append(field.getFieldName());
                        metadataBuilder.append(" = ");
                        metadataBuilder.append(fieldNode.asText());
                        metadataBuilder.append("]");
                    }
                }

                writer.write(metadataBuilder.toString());
                writer.write("\n");
                writer.write(sequence.asText());
                writer.write("\n");
            }

        } catch (IOException e) {
            throw new FimsRuntimeException(FileCode.WRITE_ERROR, 500);
        }

        return file;
    }

    private class SequenceData {
        private final String identifier;
        private final ObjectNode sequenceNode;
        private final ObjectNode resourceNode;

        private SequenceData(String identifier, ObjectNode sequenceNode, ObjectNode resourceNode) {
            this.identifier = identifier;
            this.sequenceNode = sequenceNode;
            this.resourceNode = resourceNode;
        }

        String getIdentifier() {
            return identifier;
        }

        ObjectNode getSequenceNode() {
            return sequenceNode;
        }

        ObjectNode getResourceNode() {
            return resourceNode;
        }
    }
}

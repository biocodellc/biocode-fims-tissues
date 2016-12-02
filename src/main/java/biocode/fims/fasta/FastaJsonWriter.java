package biocode.fims.fasta;

import biocode.fims.fimsExceptions.FileCode;
import biocode.fims.fimsExceptions.FimsRuntimeException;
import biocode.fims.query.JsonFieldTransform;
import biocode.fims.query.JsonWriter;
import biocode.fims.settings.PathManager;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ValueNode;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

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

    @Override
    public File write() {
        File file = PathManager.createUniqueFile("output.fasta", outputDirectory);

        try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(file)))) {

            for (JsonNode resource : resources) {

                JsonNode sequencesNode = resource.path(fastaSequenceFields.getSequencesPath());

                ValueNode identifierNode = (ValueNode) resource.path(fastaSequenceFields.getIdentifierPath());

                if (!sequencesNode.isMissingNode()) {

                    ArrayNode sequencesArrayNode = (ArrayNode) sequencesNode;

                    for (JsonNode sequenceNode : sequencesArrayNode) {

                        if (writeSequence((ObjectNode) sequenceNode)) {

                            ValueNode sequence = (ValueNode) sequenceNode.path(fastaSequenceFields.getSequencePath());

                            writer.write("> ");
                            writer.write(identifierNode.asText());

                            StringBuilder metadataBuilder = new StringBuilder();

                            for (JsonFieldTransform field : fastaSequenceFields.getMetadata()) {
                                JsonNode fieldNode = sequenceNode.at(field.getPath());

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

                    }
                }
            }

        } catch (IOException e) {
            throw new FimsRuntimeException(FileCode.WRITE_ERROR, 500);
        }

        return file;
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
}

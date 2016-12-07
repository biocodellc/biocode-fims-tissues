package biocode.fims.fasta;

import biocode.fims.query.JsonFieldTransform;

import java.util.List;

/**
 * Domain object encapsulating fastaSequence field data
 *
 * @author RJ Ewing
 */
public class FastaSequenceFields {

    private final String sequencesPath;
    private final String identifierPath;
    private final String sequencePath;
    private final List<JsonFieldTransform> metadata;

    private String uniqueKeyPath;

    /**
     * @param sequencesPath  the path to the fastaSequences array
     * @param identifierPath the path to the identifier for each fastaSequence
     * @param sequencePath   the path to the sequence field for each fastaSequence
     * @param metadata       the JsonFieldTransforms for each metadata field for each fastaSequence
     */
    public FastaSequenceFields(String sequencesPath, String identifierPath,
                               String sequencePath, List<JsonFieldTransform> metadata) {
        this.sequencesPath = sequencesPath;
        this.identifierPath = identifierPath;
        this.sequencePath = sequencePath;
        this.metadata = metadata;
    }

    public String getSequencesPath() {
        return sequencesPath;
    }

    public String getIdentifierPath() {
        return identifierPath;
    }

    public String getSequencePath() {
        return sequencePath;
    }

    /**
     * this is used to determine if we should write 1 or multiple fasta file
     * @param uniqueKeyPath the path to the field that denotes the unique fastaSequence entity
     */
    public void setUniqueKeyPath(String uniqueKeyPath) {
        this.uniqueKeyPath = uniqueKeyPath;
    }

    public String getUniqueKeyPath() {
        return uniqueKeyPath;
    }

    public List<JsonFieldTransform> getMetadata() {
        return metadata;
    }
}

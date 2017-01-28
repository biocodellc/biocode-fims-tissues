package biocode.fims.fasta;

import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * object to hold fasta data
 */
public class FastaData {
    private String filename;
    private ObjectNode metadata;

    /**
     * the filename containing the fasta data
     */
    public String getFilename() {
        return filename;
    }

    /**
     * {@link FastaData#getFilename}
     */
    public void setFilename(String filename) {
        this.filename = filename;
    }

    /**
     * any metadata that is associated with the fasta file
     */
    public ObjectNode getMetadata() {
        return metadata;
    }

    /**
     * {@link FastaData#getMetadata}
     */
    public void setMetadata(ObjectNode metadata) {
        this.metadata = metadata;
    }
}

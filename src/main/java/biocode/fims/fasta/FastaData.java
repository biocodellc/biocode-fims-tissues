package biocode.fims.fasta;

import org.json.simple.JSONObject;

/**
 * object to hold fasta data
 */
public class FastaData {
    private String filename;
    private JSONObject metadata;

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
    public JSONObject getMetadata() {
        return metadata;
    }

    /**
     * {@link FastaData#getMetadata}
     */
    public void setMetadata(JSONObject metadata) {
        this.metadata = metadata;
    }
}

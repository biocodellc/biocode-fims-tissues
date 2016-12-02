package biocode.fims.fasta;

/**
 * Domain object specifying a filter for fastaSequence data
 *
 * @author RJ Ewing
 */
public class FastaSequenceJsonFieldFilter {

    private final String path;
    private final String value;

    /**
     * @param path  path of the fastaSequence field to filter on
     * @param value value for the path to filter the fastaSequence object
     */
    public FastaSequenceJsonFieldFilter(String path, String value) {
        this.path = path;
        this.value = value;
    }

    public String getPath() {
        return path;
    }

    public String getValue() {
        return value;
    }
}

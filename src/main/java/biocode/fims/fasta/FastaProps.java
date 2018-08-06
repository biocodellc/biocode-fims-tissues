package biocode.fims.fasta;

import biocode.fims.projectConfig.models.EntityProps;

/**
 * @author rjewing
 */
public enum FastaProps implements EntityProps {
    SEQUENCE("sequence"),
    MARKER("marker"),
    IDENTIFIER("identifier");

    private final String val;

    FastaProps(String val) {
        this.val = val;
    }

    @Override
    public String value() {
        return val;
    }

    @Override
    public String toString() {
        return val;
    }
}

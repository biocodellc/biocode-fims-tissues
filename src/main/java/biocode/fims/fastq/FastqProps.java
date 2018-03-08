package biocode.fims.fastq;

/**
 * @author rjewing
 */
public enum FastqProps {
    LIBRARY_STRATEGY("libraryStrategy"),
    LIBRARY_SOURCE("librarySource"),
    LIBRARY_SELECTION("librarySelection"),
    LIBRARY_LAYOUT("libraryLayout"),
    PLATFORM("platform"),
    INSTRUMENT_MODEL("instrumentModel"),
    DESIGN_DESCRIPTION("designDescription"),
    FILENAMES("filenames");

    private final String val;

    FastqProps(String val) {
        this.val = val;
    }

    public String value() {
        return val;
    }

    @Override
    public String toString() {
        return val;
    }
}

package biocode.fims.fastq;

import biocode.fims.digester.EntityProps;

/**
 * @author rjewing
 */
public enum FastqProps implements EntityProps {
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

    @Override
    public String value() {
        return val;
    }

    @Override
    public String toString() {
        return val;
    }
}

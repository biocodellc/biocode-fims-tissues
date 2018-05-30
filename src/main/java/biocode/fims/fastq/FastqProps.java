package biocode.fims.fastq;

import biocode.fims.projectConfig.models.EntityProps;

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
    FILENAMES("filenames"),
    BIOSAMPLE("bioSample"); // TODO should we mark this attribute as hidden or none editable

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

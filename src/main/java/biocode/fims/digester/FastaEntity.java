package biocode.fims.digester;

import biocode.fims.fasta.FastaRecord;
import biocode.fims.fimsExceptions.FimsRuntimeException;
import biocode.fims.fimsExceptions.errorCodes.ConfigCode;
import biocode.fims.projectConfig.ProjectConfig;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.util.StdConverter;

import java.util.Collections;
import java.util.List;

/**
 * @author rjewing
 */
@JsonDeserialize(converter = FastaEntity.FastaEntitySanitizer.class)
public class FastaEntity extends Entity {
    public static final String SEQUENCE_URI = "urn:sequence";
    public static final String SEQUENCE_KEY = "sequence";
    public static final String MARKER_URI = "urn:marker";
    public static final String MARKER_KEY = "marker";

    private static final String CONCEPT_URI = "urn:fastaSequence";
    private static final String TYPE = "Fasta";


    private FastaEntity() { // needed for EntityTypeIdResolver
        super();
    }

    public FastaEntity(String conceptAlias) {
        super(conceptAlias, CONCEPT_URI);
        init();
    }

    private void init() {
        boolean hasSequence = false;
        boolean hasMarker = false;

        for (Attribute a: getAttributes()) {
            if (a.getUri().equals(SEQUENCE_URI)) {
                hasSequence = true;
                a.setColumn(SEQUENCE_KEY);
            } else if (a.getUri().equals(MARKER_URI)) {
                hasMarker = true;
                a.setColumn(MARKER_KEY);
            }
        }
        if (!hasSequence) {
            addAttribute(new Attribute(SEQUENCE_KEY, SEQUENCE_URI));
        }
        if (!hasMarker) {
            addAttribute(new Attribute(MARKER_KEY, MARKER_URI));
        }
        // This is actually a composite unique_key. The actual key is
        // parentEntityUniqueKey_fastaEntityUniqueKey
        setUniqueKey(MARKER_KEY);
        recordType = FastaRecord.class;

        // note: default rules are set in the FastaValidator
    }

    @Override
    public void configure(ProjectConfig config) {
        Entity parentEntity = config.entity(getParentEntity());

        if (parentEntity != null) {
            String uniqueKey = parentEntity.getUniqueKey();
            try {
                getAttribute(uniqueKey);
            } catch (FimsRuntimeException e) {
                if (e.getErrorCode() != ConfigCode.MISSING_ATTRIBUTE) {
                    throw e;
                }
                addAttribute(new Attribute(uniqueKey, parentEntity.getAttributeUri(uniqueKey)));
            }
        }
    }

    @Override
    public boolean isValid(ProjectConfig config) {
        // ProjectConfigValidator does further validation of the parentEntity.
        // we just need to make sure it is set.
        return isChildEntity();
    }

    @Override
    public List<String> validationErrorMessages() {
        return Collections.singletonList("Entity \"" + getConceptAlias() + "\" is missing a valid parentEntity");

    }

    @Override
    public String type() {
        return TYPE;
    }

    @Override
    public boolean canReload() {
        return false;
    }

    /**
     * class used to verify FastaEntity data integrity after deserialization. This is necessary
     * so we don't overwrite the default values during deserialization.
     */
    public static class FastaEntitySanitizer extends StdConverter<FastaEntity, FastaEntity> {
        public FastaEntitySanitizer() {
            super();
        }

        @Override
        public FastaEntity convert(FastaEntity value) {
            value.init();
            return value;
        }
    }
}


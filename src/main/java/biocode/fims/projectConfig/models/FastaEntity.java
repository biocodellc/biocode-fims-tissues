package biocode.fims.projectConfig.models;

import biocode.fims.fasta.FastaProps;
import biocode.fims.fasta.FastaRecord;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.util.StdConverter;

/**
 * @author rjewing
 */
@JsonDeserialize(converter = FastaEntity.FastaEntitySanitizer.class)
public class FastaEntity extends PropEntity<FastaProps> {
    private static final String CONCEPT_URI = "urn:fastaSequence";
    private static final String TYPE = "Fasta";


    private FastaEntity() { // needed for EntityTypeIdResolver
        super(FastaProps.class);
    }

    public FastaEntity(String conceptAlias) {
        super(FastaProps.class, conceptAlias, CONCEPT_URI);
        init();
    }

    @Override
    protected void init() {
        super.init();
        getAttribute(FastaProps.IDENTIFIER.value()).setInternal(true);
        setUniqueKey(FastaProps.IDENTIFIER.value());
        recordType = FastaRecord.class;

        // note: default rules are set in the FastaValidator
    }

    @Override
    public String type() {
        return TYPE;
    }

    /**
     * class used to verify FastaEntity data integrity after deserialization. This is necessary
     * so we don't overwrite the default values during deserialization.
     */
    static class FastaEntitySanitizer extends PropEntitySanitizer<FastaEntity> {}
}


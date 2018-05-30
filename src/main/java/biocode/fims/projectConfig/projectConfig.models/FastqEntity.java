package biocode.fims.projectConfig.models;

import biocode.fims.fastq.FastqProps;
import biocode.fims.fastq.FastqRecord;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * @author rjewing
 */
@JsonDeserialize(converter = FastqEntity.FastqEntitySanitizer.class)
public class FastqEntity extends PropEntity<FastqProps> {
    private static final String CONCEPT_URI = "urn:fastqMetadata";
    public static final String TYPE = "Fastq";

//    private static final List<String> library


    private FastqEntity() { // needed for EntityTypeIdResolver
        super(FastqProps.class);
    }

    public FastqEntity(String conceptAlias) {
        super(FastqProps.class, conceptAlias, CONCEPT_URI);
    }

    @Override
    protected void init() {
        super.init();
        // only a single fastq record is allowed / parent record
        setUniqueKey(null);
        recordType = FastqRecord.class;

        // note: default rules are set in the FastqValidator

        // TODO add lists?
    }

    @Override
    public String type() {
        return TYPE;
    }

    /**
     * class used to verify FastqEntity data integrity after deserialization. This is necessary
     * so we don't overwrite the default values during deserialization.
     */
    static class FastqEntitySanitizer extends PropEntitySanitizer<FastqEntity> {}
}


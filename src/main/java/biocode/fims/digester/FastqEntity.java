package biocode.fims.digester;

import biocode.fims.fastq.FastqProps;
import biocode.fims.fastq.FastqRecord;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.util.StdConverter;

/**
 * @author rjewing
 */
@JsonDeserialize(converter = FastqEntity.FastqEntitySanitizer.class)
public class FastqEntity extends ChildEntity {
    private static final String CONCEPT_URI = "urn:fastqMetadata";
    public static final String TYPE = "Fastq";

//    private static final List<String> library


    private FastqEntity() { // needed for EntityTypeIdResolver
        super();
    }

    public FastqEntity(String conceptAlias) {
        super(conceptAlias, CONCEPT_URI);
        init();
    }

    private void init() {
        for (FastqProps p :FastqProps.values()) {
            if (getAttribute(p.value()) == null) {
                addAttribute(new Attribute(p.value(), p.value()));
            }
        }
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
    public static class FastqEntitySanitizer extends StdConverter<FastqEntity, FastqEntity> {
        public FastqEntitySanitizer() {
            super();
        }

        @Override
        public FastqEntity convert(FastqEntity value) {
            value.init();
            return value;
        }
    }
}


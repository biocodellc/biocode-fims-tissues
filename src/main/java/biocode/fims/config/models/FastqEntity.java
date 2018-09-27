package biocode.fims.config.models;

import biocode.fims.config.Config;
import biocode.fims.config.network.NetworkConfig;
import biocode.fims.fastq.FastqProps;
import biocode.fims.fastq.FastqRecord;
import biocode.fims.models.dataTypes.JacksonUtil;
import biocode.fims.validation.rules.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.LinkedHashSet;

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
        getAttribute(FastqProps.IDENTIFIER.value()).setInternal(true);
        setUniqueKey(FastqProps.IDENTIFIER.value());
        getAttribute(FastqProps.FILENAMES.value()).setInternal(true);
        getAttribute(FastqProps.BIOSAMPLE.value()).setInternal(true);

        recordType = FastqRecord.class;

        // note: default rules are set in the FastqValidator

        // TODO add lists?
    }

    @Override
    public String type() {
        return TYPE;
    }

    @Override
    public boolean getUniqueAcrossProject() {
        return false;
    }

    @Override
    public void addDefaultRules(Config config) {
        RequiredValueRule requiredValueRule = getRule(RequiredValueRule.class, RuleLevel.ERROR);

        if (requiredValueRule == null) {
            requiredValueRule = new RequiredValueRule(new LinkedHashSet<>(), RuleLevel.ERROR);
            addRule(requiredValueRule);
        }

        if (!(config instanceof NetworkConfig)) {
            Entity parentEntity = config.entity(getParentEntity());
            requiredValueRule.addColumn(parentEntity.getUniqueKey());
        }

        for (FastqProps p : FastqProps.values()) {
            if (p != FastqProps.BIOSAMPLE) {
                requiredValueRule.addColumn(p.value());
            }
        }

        addRule(new UniqueValueRule(FastqProps.IDENTIFIER.value(), getUniqueAcrossProject(), RuleLevel.ERROR));
        addRule(new ValidParentIdentifiersRule());
        addRule(new FastqLibraryLayoutRule());
        addRule(new FastqFilenamesRule());
        addRule(new FastqMetadataRule());

        // validate all parent records have a FastqRecord???
    }

    @Override
    public Entity clone() {
        FastqEntity entity = new FastqEntity(getConceptAlias());

        getRules().forEach(r -> {
            // TODO create a Rule method clone()
            // hacky way to make a copy of the rule
            Rule newR = JacksonUtil.fromString(
                    JacksonUtil.toString(r),
                    r.getClass()
            );
            entity.addRule(newR);
        });
        getAttributes().forEach(a -> entity.addAttribute(a.clone()));

        entity.setParentEntity(getParentEntity());
        entity.recordType = recordType;

        entity.setWorksheet(getWorksheet());
        entity.setUniqueKey(getUniqueKey());
        entity.setUniqueAcrossProject(getUniqueAcrossProject());
        entity.setHashed(isHashed());

        return entity;
    }

    /**
     * class used to verify FastqEntity data integrity after deserialization. This is necessary
     * so we don't overwrite the default values during deserialization.
     */
    static class FastqEntitySanitizer extends PropEntitySanitizer<FastqEntity> {
    }
}


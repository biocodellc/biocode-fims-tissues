package biocode.fims.projectConfig.models;

import biocode.fims.fasta.FastaProps;
import biocode.fims.fasta.FastaRecord;
import biocode.fims.projectConfig.ProjectConfig;
import biocode.fims.validation.rules.RequiredValueRule;
import biocode.fims.validation.rules.RuleLevel;
import biocode.fims.validation.rules.UniqueValueRule;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.util.StdConverter;

/**
 * @author rjewing
 */
@JsonDeserialize(converter = FastaEntity.FastaEntitySanitizer.class)
public class FastaEntity extends PropEntity<FastaProps> {
    private static final String CONCEPT_URI = "urn:fastaSequence";
    public static final String TYPE = "Fasta";


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

    @Override
    public boolean getUniqueAcrossProject() {
        return false;
    }

    @Override
    public void addDefaultRules(ProjectConfig config) {
        super.addDefaultRules(config);
        RequiredValueRule requiredValueRule = getRule(RequiredValueRule.class, RuleLevel.ERROR);
        requiredValueRule.addColumn(FastaProps.SEQUENCE.value());
        requiredValueRule.addColumn(FastaProps.IDENTIFIER.value());

        UniqueValueRule uniqueValueRule = new UniqueValueRule(FastaProps.IDENTIFIER.value(), getUniqueAcrossProject(), RuleLevel.ERROR);
        addRule(uniqueValueRule);
    }

    /**
     * class used to verify FastaEntity data integrity after deserialization. This is necessary
     * so we don't overwrite the default values during deserialization.
     */
    static class FastaEntitySanitizer extends PropEntitySanitizer<FastaEntity> {
    }
}


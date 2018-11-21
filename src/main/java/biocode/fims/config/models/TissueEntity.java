package biocode.fims.config.models;

import biocode.fims.config.Config;
import biocode.fims.models.dataTypes.JacksonUtil;
import biocode.fims.records.GenericRecord;
import biocode.fims.tissues.TissueProps;
import biocode.fims.validation.rules.RequiredValueRule;
import biocode.fims.validation.rules.Rule;
import biocode.fims.validation.rules.RuleLevel;
import biocode.fims.validation.rules.UniqueValueRule;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

/**
 * @author rjewing
 */
@JsonDeserialize(converter = TissueEntity.TissueEntitySanitizer.class)
public class TissueEntity extends PropEntity<TissueProps> {
    private static final String CONCEPT_URI = "urn:Tissue";
    public static final String CONCEPT_ALIAS = "Tissue";
    public static final String TYPE = "Tissue";

    private static final String GENERATE_ID_KEY = "generateID";

    private boolean generateID = false;


    public TissueEntity() {
        super(TissueProps.class, CONCEPT_ALIAS, CONCEPT_URI);
        init();
    }

    @Override
    protected void init() {
        super.init();
        getAttribute(TissueProps.IDENTIFIER.column());
//        setUniqueKey(TissueProps.IDENTIFIER.column());
        recordType = GenericRecord.class;

        // note: default rules are set in the TissueValidator
    }

    public boolean isGenerateID() {
        return generateID;
    }

    public void setGenerateID(boolean generateID) {
        this.generateID = generateID;
    }

    @Override
    public String type() {
        return TYPE;
    }

    @Override
    public boolean isHashed() {
        return false;
    }

    @Override
    public Map<String, Object> additionalProps() {
        Map<String, Object> props = new HashMap<>();
        props.put(GENERATE_ID_KEY, generateID);
        return props;
    }

    @Override
    public void setAdditionalProps(Map<String, Object> props) {
        generateID = (boolean) props.getOrDefault(GENERATE_ID_KEY, false);
    }

    @Override
    public void addDefaultRules(Config config) {
        super.addDefaultRules(config);
//        RequiredValueRule requiredValueRule = getRule(RequiredValueRule.class, RuleLevel.ERROR);
//
//        if (requiredValueRule == null) {
//            requiredValueRule = new RequiredValueRule(new LinkedHashSet<>(), RuleLevel.ERROR);
//            addRule(requiredValueRule);
//        }
//
//        requiredValueRule.addColumn(TissueProps.SEQUENCE.value());
//        requiredValueRule.addColumn(TissueProps.IDENTIFIER.value());
//
//        UniqueValueRule uniqueValueRule = new UniqueValueRule(TissueProps.IDENTIFIER.value(), getUniqueAcrossProject(), RuleLevel.ERROR);
//        addRule(uniqueValueRule);
    }


    @Override
    public Entity clone() {
        TissueEntity entity = (TissueEntity) clone(new TissueEntity());

        entity.setGenerateID(isGenerateID());

        return entity;
    }

    /**
     * class used to verify TissueEntity data integrity after deserialization. This is necessary
     * so we don't overwrite the default values during deserialization.
     */
    static class TissueEntitySanitizer extends PropEntitySanitizer<TissueEntity> {
    }
}


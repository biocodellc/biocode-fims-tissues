package biocode.fims.fastq;

import biocode.fims.projectConfig.models.Entity;
import biocode.fims.models.records.RecordSet;
import biocode.fims.projectConfig.ProjectConfig;
import biocode.fims.validation.RecordValidator;
import biocode.fims.validation.ValidatorInstantiator;
import biocode.fims.validation.rules.*;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author rjewing
 */
public class FastqValidator extends RecordValidator {

    public FastqValidator(ProjectConfig config) {
        super(config);
    }

    @Override
    protected void addDefaultRules(Set<Rule> rules, RecordSet recordSet) {
        Entity entity = recordSet.entity();

        RequiredValueRule requiredValueRule = entity.getRule(RequiredValueRule.class, RuleLevel.ERROR);

        if (requiredValueRule == null) {
            requiredValueRule = new RequiredValueRule(new LinkedHashSet<>(), RuleLevel.ERROR);
            entity.addRule(requiredValueRule);
        }

        Entity parentEntity = config.entity(entity.getParentEntity());

        requiredValueRule.addColumn(parentEntity.getUniqueKey());

        for (FastqProps p : FastqProps.values()) {
            if (p != FastqProps.BIOSAMPLE) {
                requiredValueRule.addColumn(p.value());
            }
        }

        rules.add(new UniqueValueRule(FastqProps.IDENTIFIER.value(), RuleLevel.ERROR));
        rules.add(new ValidParentIdentifiersRule());
        rules.add(new FastqLibraryLayoutRule());
        rules.add(new FastqFilenamesRule());
        rules.add(new FastqMetadataRule());

        // validate all parent records have a FastqRecord???
    }

    public static class FastqValidatorInstantiator implements ValidatorInstantiator {
        @Override
        public RecordValidator newInstance(ProjectConfig config) {
            return new FastqValidator(config);
        }
    }
}

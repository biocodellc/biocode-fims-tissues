package biocode.fims.fasta;

import biocode.fims.projectConfig.models.Entity;
import biocode.fims.models.records.RecordSet;
import biocode.fims.projectConfig.ProjectConfig;
import biocode.fims.validation.RecordValidator;
import biocode.fims.validation.ValidatorInstantiator;
import biocode.fims.validation.rules.*;

import java.util.Set;


/**
 * @author rjewing
 */
public class FastaValidator extends RecordValidator {

    public FastaValidator(ProjectConfig config) {
        super(config);
    }

    @Override
    protected void addDefaultRules(Set<Rule> rules, RecordSet recordSet) {
        super.addDefaultRules(rules, recordSet);

        Entity entity = recordSet.entity();

        RequiredValueRule requiredValueRule = entity.getRule(RequiredValueRule.class, RuleLevel.ERROR);
        requiredValueRule.addColumn(FastaProps.SEQUENCE.value());
        requiredValueRule.addColumn(FastaProps.IDENTIFIER.value());

        UniqueValueRule uniqueValueRule = new UniqueValueRule(FastaProps.IDENTIFIER.value(), entity.getUniqueAcrossProject(), RuleLevel.ERROR);
        entity.addRule(uniqueValueRule);
    }

    public static class FastaValidatorInstantiator implements ValidatorInstantiator {
        @Override
        public RecordValidator newInstance(ProjectConfig config) {
            return new FastaValidator(config);
        }
    }
}

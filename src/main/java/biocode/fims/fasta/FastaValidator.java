package biocode.fims.fasta;

import biocode.fims.projectConfig.models.Entity;
import biocode.fims.records.RecordSet;
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


    public static class FastaValidatorInstantiator implements ValidatorInstantiator {
        @Override
        public RecordValidator newInstance(ProjectConfig config) {
            return new FastaValidator(config);
        }
    }
}

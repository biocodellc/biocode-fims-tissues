package biocode.fims.application.config;

import org.springframework.core.env.Environment;

/**
 * @author rjewing
 */
public class TissueProperties {
    private final Environment env;

    public TissueProperties(Environment env) {
        this.env = env;
    }

    public String sraSubmissionDir() {
        String dir = env.getRequiredProperty("sraSubmissionDir");
        if (!dir.endsWith("/")) {
            dir += "/";
        }
        return dir;
    }
}

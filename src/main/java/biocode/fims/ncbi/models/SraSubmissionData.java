package biocode.fims.ncbi.models;

import java.util.List;

/**
 * @author rjewing
 */
public class SraSubmissionData {
    public List<SubmittableBioSample> bioSamples;
    public List<SraMetadata> sraMetadata;

    public SraSubmissionData(List<SubmittableBioSample> bioSamples, List<SraMetadata> sraMetadata) {
        this.bioSamples = bioSamples;
        this.sraMetadata = sraMetadata;
    }
}

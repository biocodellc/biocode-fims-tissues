package biocode.fims.ncbi.models.submission;

import biocode.fims.models.User;
import biocode.fims.ncbi.models.SraMetadata;
import biocode.fims.ncbi.models.SraSubmissionData;
import biocode.fims.ncbi.models.SubmittableBioSample;
import biocode.fims.rest.models.SraUploadMetadata;
import org.eclipse.persistence.oxm.annotations.XmlPath;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Domain object representing the information necessary to submit to sra for a new submission
 *
 * @author rjewing
 */
@XmlRootElement(name = "Submission")
public class SraSubmission {

    @XmlPath("Description/Organization")
    public Organization organization;
    @XmlPath("Description/Submitter/@user_name")
    private String submitter;
    @XmlPath("Description/Hold/@release_date")
    private Date holdDate;
    @XmlElements({
            @XmlElement(name = "Action", type = BioProject.class),
            @XmlElement(name = "Action", type = SubmittableBioSample.class),
            @XmlElement(name = "Action", type = SraMetadata.class),
    })
    private List<Object> actions;

    // necessary for jackson/moxy
    public SraSubmission() {
    }

    public SraSubmission(SraSubmissionData data, SraUploadMetadata metadata, User user, String url) {
        organization = new Organization(user, url);
        this.holdDate = metadata.holdDate;
        this.submitter = user.getUsername();
        this.actions = new ArrayList<>();

        BioProject bioProject = null;
        if (metadata.bioProjectAccession == null) {
            bioProject = new BioProject(metadata);
            this.actions.add(bioProject);
        }

        BioProject finalBioProject = bioProject;
        data.bioSamples.forEach(b -> {
            if (finalBioProject == null) {
                b.setBioProjectAccession(metadata.bioProjectAccession);
            } else {
                b.setBioProjectId(finalBioProject.getIdentifier());
            }
            b.setType(metadata.bioSampleType);
            this.actions.add(b);
        });

        data.sraMetadata.forEach(m -> {
            if (finalBioProject == null) {
                m.setBioProjectAccession(metadata.bioProjectAccession);
            } else {
                m.setBioProjectId(finalBioProject.getIdentifier());
            }
            this.actions.add(m);
        });
    }
}

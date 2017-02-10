package biocode.fims.ncbi.models;

import org.eclipse.persistence.oxm.annotations.XmlPath;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import java.util.List;

/**
 * Domain object representing the information we are interested in from the NCBI Entrez eFetch Sra
 *
 * @author rjewing
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class SraExperimentPackage {

    @XmlPath("STUDY/@accession")
    private String studyAccession;

    @XmlPath("EXPERIMENT/@accession")
    private String experimentAccession;

    @XmlPath("SAMPLE/IDENTIFIERS/EXTERNAL_ID[@namespace='BioSample']/text()")
    private String bioSampleAccession;

    @XmlPath("RUN_SET/RUN/@accession")
    private List<String> runAccessions;

    public SraExperimentPackage() {}

    public SraExperimentPackage(String studyAccession, String experimentAccession, String bioSampleAccession, List<String> runAccessions) {
        this.studyAccession = studyAccession;
        this.experimentAccession = experimentAccession;
        this.bioSampleAccession = bioSampleAccession;
        this.runAccessions = runAccessions;
    }

    public String getStudyAccession() {
        return studyAccession;
    }

    public String getExperimentAccession() {
        return experimentAccession;
    }

    public String getBioSampleAccession() {
        return bioSampleAccession;
    }

    public List<String> getRunAccessions() {
        return runAccessions;
    }
}

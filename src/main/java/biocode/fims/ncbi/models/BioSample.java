package biocode.fims.ncbi.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.eclipse.persistence.oxm.annotations.XmlPath;

import javax.xml.bind.annotation.*;

/**
 * Domain object representing the information we are interested in from the NCBI Entrez eFetch BioSample
 *
 * @author rjewing
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class BioSample {

    @XmlAttribute
    private String id;

    @XmlAttribute
    private String accession;

    @JsonIgnore
    @XmlPath("Attributes/Attribute[@attribute_name='bcid']/text()")
    private String bcid;

    @XmlPath("Links/Link[@target='bioproject']/text()")
    private String bioProjectId;

    @XmlPath("Links/Link[@target='bioproject']/@label")
    private String bioProjectAccession;
    @JsonProperty("experiment")
    private SraExperimentPackage sraExperimentPackage;

    private BioSample() {
    }

    public BioSample(String id, String accession, String bcid, String bioProjectId, String bioProjectAccession) {
        this.id = id;
        this.accession = accession;
        this.bcid = bcid;
        this.bioProjectId = bioProjectId;
        this.bioProjectAccession = bioProjectAccession;
    }

    public String getId() {
        return id;
    }

    public String getAccession() {
        return accession;
    }

    public String getBcid() {
        return bcid;
    }

    public String getBioProjectId() {
        return bioProjectId;
    }

    public String getBioProjectAccession() {
        return bioProjectAccession;
    }

    public SraExperimentPackage getSraExperimentPackage() {
        return sraExperimentPackage;
    }

    public void setSraExperimentPackage(SraExperimentPackage sraExperimentPackage) {
        this.sraExperimentPackage = sraExperimentPackage;
    }
}

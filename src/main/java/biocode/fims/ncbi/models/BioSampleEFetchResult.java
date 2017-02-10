package biocode.fims.ncbi.models;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * @author rjewing
 */
@XmlRootElement(name="BioSampleSet")
@XmlAccessorType(XmlAccessType.FIELD)
public class BioSampleEFetchResult {

    @XmlElement(name="BioSample")
    private List<BioSample> bioSamples;

    private BioSampleEFetchResult() {}
    public BioSampleEFetchResult(List<BioSample> bioSamples) {
        this.bioSamples = bioSamples;
    }

    public List<BioSample> getBioSamples() {
        return bioSamples;
    }
}

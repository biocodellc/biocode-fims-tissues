package biocode.fims.ncbi.models;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * @author rjewing
 */
@XmlRootElement(name="EXPERIMENT_PACKAGE_SET")
@XmlAccessorType(XmlAccessType.FIELD)
public class SraEFetchResult {

    @XmlElement(name="EXPERIMENT_PACKAGE")
    private List<SraExperimentPackage> sraExperimentPackages;

    private SraEFetchResult() {}

    public SraEFetchResult(List<SraExperimentPackage> sraExperimentPackages) {
        this.sraExperimentPackages = sraExperimentPackages;
    }

    public List<SraExperimentPackage> getSraExperimentPackages() {
        return sraExperimentPackages;
    }
}

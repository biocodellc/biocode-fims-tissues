package biocode.fims.ncbi.models.submission;

import biocode.fims.rest.models.SraUploadMetadata;
import org.eclipse.persistence.oxm.annotations.XmlPath;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author rjewing
 */
public class BioProject {
//    @XmlAttribute(name = "target_db")
    @XmlPath("AddData/@target_db")
    private static String targetDb = "BioProject";
    @XmlPath("AddData/Data/@content_type")
    private static String contentType = "xml";


    @XmlPath("AddData/Data/XmlContent/Project/ProjectID/SPUID/text()")
    private String expeditionCode;
    @XmlPath("AddData/Data/XmlContent/Project/Descriptor/Title/text()")
    private String title;
    @XmlPath("AddData/Data/XmlContent/Project/Descriptor/Description/text()")
    private String description;

    BioProject() {}

    public BioProject(SraUploadMetadata metadata) {
        this.expeditionCode = metadata.expeditionCode;
        this.description = metadata.bioProjectDescription;
        this.title = metadata.bioProjectTitle;
    }

    @XmlPath("AddData/Identifier/SPUID/text()")
    public String getIdentifier() {
        return this.expeditionCode;
    }

}

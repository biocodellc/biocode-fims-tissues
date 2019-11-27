package biocode.fims.ncbi.models.submission;

import biocode.fims.models.User;
import org.eclipse.persistence.oxm.annotations.XmlPath;

import javax.xml.bind.annotation.*;

/**
 * @author rjewing
 */
@XmlRootElement
public class Organization {
    @XmlAttribute
    private static String role = "owner";
    @XmlAttribute
    private static String type = "consortium"; //"institute";
    @XmlElement
    private static String Name = "GEOME";
    @XmlAttribute
    private String url;// = "https://geome-db.org";

    private User user;

    Organization() {}

    public Organization(User user, String url) {
        this.user = user;
        this.url = url;
    }

//    @XmlElement
//    public String getName() {
//        return user.getInstitution();
//    }

    @XmlElement
    @XmlPath("Contact/Name/First/text()")
    public String getFirst() {
        return user.getFirstName();
    }

    @XmlElement
    @XmlPath("Contact/Name/Last/text()")
    public String getLast() {
        return user.getLastName();
    }

    @XmlPath("Contact/@email")
    public String getEmail() {
        return user.getEmail();
    }

}

package biocode.fims.ncbi.models;

import biocode.fims.ncbi.models.submission.BioSampleAttribute;
import biocode.fims.ncbi.models.submission.BioSampleTypeAdaptor;
import biocode.fims.rest.models.SraUploadMetadata;
import org.eclipse.persistence.oxm.annotations.XmlPath;

import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author rjewing
 */
@XmlType(propOrder = {"sampleName", "sampleTitle", "organism", "bioProjectAccession", "bioProjectId", "type", "attributes", "identifier"})
public class SubmittableBioSample {
    private static ArrayList<String> IGNOREABLE_ATTRIBUTES = new ArrayList<String>() {{
        add("sample_name");
        add("sample_title");
        add("organism");
    }};

    @XmlPath("AddData/@target_db")
    private static String targetDb = "BioSample";
    @XmlPath("AddData/Data/@content_type")
    private static String contentType = "xml";
    @XmlPath("AddData/Data/XmlContent/BioSample/@schema_version")
    private static String schemaVersion = "2.0";

    @XmlPath("AddData/Data/XmlContent/BioSample/SampleId/SPUID/@spuid_namespace")
    private static String spuidNamespace = SPUIDNamespace.value;
    @XmlPath("AddData/Data/XmlContent/BioSample/SampleId/SPUID/text()")
    public String sampleName;

    @XmlPath("AddData/Data/XmlContent/BioSample/Descriptor/Title/text()")
    public String sampleTitle;

    @XmlPath("AddData/Data/XmlContent/BioSample/Organism/OrganismName/text()")
    public String organism;

    @XmlPath("AddData/Data/XmlContent/BioSample/BioProject/PrimaryId/text()")
    public String bioProjectAccession;
    @XmlPath("AddData/Data/XmlContent/BioSample/BioProject/SPUID/@spuid_namespace")
    private static String spuidNamespace2 = SPUIDNamespace.value;
    @XmlPath("AddData/Data/XmlContent/BioSample/BioProject/SPUID/text()")
    public String bioProjectId;
    @XmlPath("AddData/Data/XmlContent/BioSample/Package/text()")
    @XmlJavaTypeAdapter(BioSampleTypeAdaptor.class)
    public SraUploadMetadata.BioSampleType type;

    @XmlPath("AddData/Data/XmlContent/BioSample/Attributes/Attribute")
    public List<BioSampleAttribute> attributes;

    @XmlPath("AddData/Identifier/SPUID/text()")
    public String identifier;

    private SubmittableBioSample() {
    }

    private SubmittableBioSample(String sampleName, String sampleTitle, String organism,
                                 List<BioSampleAttribute> attributes, SraUploadMetadata.BioSampleType bioSampleType,
                                 String bioProjectAccession, String bioProjectId) {
        this.sampleName = sampleName;
        this.identifier = sampleName;
        this.sampleTitle = sampleTitle;
        this.organism = organism;
        this.attributes = attributes;
        type = bioSampleType;
        this.bioProjectAccession = bioProjectAccession;
        this.bioProjectId = bioProjectId;
    }


    @XmlPath("AddData/Data/XmlContent/BioSample/BioProject/PrimaryId/@db")
    public String getBioProjectDb() {
        return this.bioProjectAccession == null ? null : "BioProject";
    }

    @XmlPath("AddData/Identifier/SPUID/@spuid_namespace")
    private static String spuidNamespace3 = SPUIDNamespace.value;

    public static SubmittableBioSample fromBioSample(GeomeBioSample bioSample, String bioProjectAccession, String bioProjectId, SraUploadMetadata.BioSampleType bioSampleType) {
        List<BioSampleAttribute> attributes = bioSample.entrySet().stream()
                .filter(e -> !IGNOREABLE_ATTRIBUTES.contains(e.getKey()))
                .map(e -> new BioSampleAttribute(e.getKey(), e.getValue()))
                .collect(Collectors.toList());

        return new SubmittableBioSample(
                bioSample.get("sample_name"),
                bioSample.get("sample_title"),
                bioSample.get("organism"),
                attributes,
                bioSampleType,
                bioProjectAccession,
                bioProjectId
        );
    }
}


package biocode.fims.ncbi.models;

import biocode.fims.ncbi.models.submission.Attribute;
import biocode.fims.rest.models.SraUploadMetadata;
import biocode.fims.serializers.SubmittableBioSampleSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.eclipse.persistence.oxm.annotations.XmlPath;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author rjewing
 */
@JsonSerialize(using = SubmittableBioSampleSerializer.class)
public class SubmittableBioSample {
    private static ArrayList<String> IGNOREABLE_ATTRIBUTES = new ArrayList<String>() {{
        add("sample_name");
        add("sample_title");
        add("organism");
    }};

    private LinkedHashMap<String, String> attributes = new LinkedHashMap<>();

    @XmlPath("AddData/@target_db")
    private static String targetDb = "BioSample";
    @XmlPath("AddData/Data/@content_type")
    private static String contentType = "xml";

    @XmlPath("AddData/Data/XmlContent/BioSample/BioProject/PrimaryId/text()")
    private String bioProjectAccession;
    @XmlPath("AddData/Data/XmlContent/BioSample/BioProject/SPUID/text()")
    private String bioProjectId;
    @XmlPath("AddData/Data/XmlContent/BioSample/Package/text()")
    private SraUploadMetadata.BioSampleType type;

    // we can't just extend a Map b/c marshelling doesn't work correctly
    public String get(String key) {
        return this.attributes.get(key);
    }

    public String put(String key, String val) {
        return this.attributes.put(key, val);
    }

    public Collection<String> values() {
        return this.attributes.values();
    }

    @XmlPath("AddData/Data/XmlContent/BioSample/SampleID/SPUID/text()")
    public String getSampleName() {
        return get("sample_name");
    }

    @XmlPath("AddData/Data/XmlContent/BioSample/Descriptor/Title/text()")
    public String getSampleTitle() {
        return get("sample_title");
    }

    @XmlPath("AddData/Data/XmlContent/BioSample/Organism/OrganismName/text()")
    public String getOrganism() {
        return get("organism");
    }

    @XmlPath("AddData/Data/XmlContent/BioSample/BioProject/PrimaryId/@db")
    public String getBioProjectDb() {
        return this.bioProjectAccession == null ? null : "BioProject";
    }

    @XmlPath("AddData/Data/XmlContent/BioSample/Attributes/Attribute")
    public List<Attribute> getAttributes() {
        return this.attributes.entrySet().stream()
                .filter(e -> !IGNOREABLE_ATTRIBUTES.contains(e.getKey()))
                .map(e -> new Attribute(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
    }

    @XmlPath("AddData/Identifier/SPUID/text()")
    public String getIdentifier() {
        return this.getSampleName();
    }

    public void setBioProjectAccession(String accession) {
        this.bioProjectAccession = accession;
    }

    public void setType(SraUploadMetadata.BioSampleType type) {
        this.type = type;
    }

    public void setBioProjectId(String bioProjectId) {
        this.bioProjectId = bioProjectId;
    }
}


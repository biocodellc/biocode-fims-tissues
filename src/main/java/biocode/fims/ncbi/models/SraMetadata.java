package biocode.fims.ncbi.models;

import biocode.fims.ncbi.models.submission.Attribute;
import biocode.fims.serializers.SraMetadataSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.eclipse.persistence.oxm.annotations.XmlPath;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author rjewing
 */
@JsonSerialize(using = SraMetadataSerializer.class)
public class SraMetadata {
    private static ArrayList<String> IGNOREABLE_ATTRIBUTES = new ArrayList<String>() {{
        add("sample_name");
        add("library_ID");
        add("filename");
        add("filename2");
    }};

    private LinkedHashMap<String, String> attributes = new LinkedHashMap<>();

    @XmlPath("AddFiles/@target_db")
    private static String targetDb = "SRA";

    private String bioProjectAccession;
    private String bioProjectId;

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


    @XmlElements({
            @XmlElement(name = "AddFiles/File", type = File.class),
    })
    public List<File> getFiles() {
        List<File> files = new ArrayList<>();
        files.add(new File(get("filename")));
        if (attributes.containsKey("filename2")) files.add(new File(get("filename2")));
        return files;
    }

    @XmlPath("AddFiles/Attribute")
    public List<Attribute> getAttributes() {
        return this.attributes.entrySet().stream()
                .filter(e -> !IGNOREABLE_ATTRIBUTES.contains(e.getKey()))
                .map(e -> new Attribute(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
    }

    @XmlElements({
            @XmlElement(name = "AddFiles/AttributeRefId", type = AttributeRef.class),
    })
    public List<AttributeRef> getRefs() {
        List<AttributeRef> refs = new ArrayList<>();
        refs.add(new AttributeRef(this.bioProjectAccession, this.bioProjectId, "BioProject"));
        refs.add(new AttributeRef(get("sample_name"), null, "BioSample"));
        return refs;
    }

    @XmlPath("AddFiles/Identifier/SPUID/text()")
    public String getIdentifier() {
        return get("library_ID");
    }

    public void setBioProjectAccession(String bioProjectAccession) {
        this.bioProjectAccession = bioProjectAccession;
    }

    public void setBioProjectId(String bioProjectId) {
        this.bioProjectId = bioProjectId;
    }

    private static class AttributeRef {
        @XmlAttribute
        private String name;
        @XmlPath("RefId/PrimaryId/@db")
        private String db;
        @XmlPath("RefId/SPUID/text()")
        private String spuid;
        @XmlPath("RefId/PrimaryId/text()")
        private String id;

        AttributeRef() {
        }

        private AttributeRef(String spuid, String id, String name) {
            this.id = id;
            this.name = name;
            if (id != null) {
                this.db = name;
            } else {
                this.spuid = spuid;
            }
        }
    }

    private static class File {
        @XmlElement(name = "DataType")
        private static String dataType = "generic-data";
        @XmlAttribute(name = "file_path")
        private String path;

        File() {
        }

        private File(String path) {
            this.path = path;
        }
    }
}


package biocode.fims.fastq;

import biocode.fims.exceptions.FastqReaderCode;
import biocode.fims.fimsExceptions.FimsRuntimeException;
import biocode.fims.models.dataTypes.JacksonUtil;
import biocode.fims.records.GenericRecord;
import biocode.fims.records.RecordMetadata;
import biocode.fims.ncbi.models.BioSample;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static biocode.fims.fastq.FastqProps.*;

/**
 * @author rjewing
 */
public class FastqRecord extends GenericRecord {

    private List<String> filenames;
    private BioSample bioSample;

    public FastqRecord(String parentUniqueKeyUri, String identifier, List<String> filenames, RecordMetadata recordMetadata) {
        super();
        this.filenames = filenames;
        properties.put(parentUniqueKeyUri, identifier);
        // only a single fastq record is allowed / parent record
        properties.put(FastqProps.IDENTIFIER.value(), identifier);

        for (Map.Entry e : recordMetadata.metadata().entrySet()) {
            properties.put((String) e.getKey(), (String) e.getValue());
        }
//        for (Map.Entry e : recordMetadata.metadata().entrySet()) {
//            FastqProps p;
//            try {
//                p = FastqProps.valueOf(key.trim());
//            } catch (IllegalArgumentException) {}
//
//            properties.put((String) e.getKey(), (String) e.getValue());
//        }
    }

    public FastqRecord(Map<String, String> properties, List<String> filenames, String rootIdentifier, int projectId, String expeditionCode, boolean shouldPersist) {
        super(properties, rootIdentifier, projectId, expeditionCode, shouldPersist);
        this.filenames = filenames;
    }

    public String libraryStrategy() {
        return properties.get(LIBRARY_STRATEGY.value());
    }

    public String librarySource() {
        return properties.get(LIBRARY_SOURCE.value());
    }

    public String librarySelection() {
        return properties.get(LIBRARY_SELECTION.value());
    }

    public String libraryLayout() {
        return properties.get(LIBRARY_LAYOUT.value());
    }

    public String platform() {
        return properties.get(PLATFORM.value());
    }

    public String instrumentModel() {
        return properties.get(INSTRUMENT_MODEL.value());
    }

    public String designDescription() {
        return properties.get(DESIGN_DESCRIPTION.value());
    }

    public List<String> filenames() {
        return filenames;
    }

    public BioSample bioSample() {
        return bioSample;
    }

    public void setBioSample(BioSample bioSample) {
        this.bioSample = bioSample;
        persist = true;
    }

    @Override
    public String get(String property) {
        if (Objects.equals(property, FILENAMES.value())) {
            return filenamesAsString();
        }
        return super.get(property);
    }

    @Override
    public Map<String, String> properties() {
        Map<String, String> properties = new HashMap<>(super.properties());
        properties.put(FILENAMES.value(), filenamesAsString());
        if (bioSample != null) {
            properties.put(FastqProps.BIOSAMPLE.value(), JacksonUtil.toString(bioSample));
        }
        return properties;
    }

    private String filenamesAsString() {
        if (filenames.size() == 0) return "";

        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(filenames);
        } catch (JsonProcessingException e) {
            throw new FimsRuntimeException(FastqReaderCode.INVALID_FILENAMES, "Could not serialize filenames", 500);
        }

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FastqRecord)) return false;
        if (!super.equals(o)) return false;
        FastqRecord that = (FastqRecord) o;
        return Objects.equals(filenames, that.filenames) &&
                Objects.equals(bioSample, that.bioSample);
    }

    @Override
    public int hashCode() {

        return Objects.hash(super.hashCode(), filenames, bioSample);
    }
}


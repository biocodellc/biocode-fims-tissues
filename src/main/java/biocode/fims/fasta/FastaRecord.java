package biocode.fims.fasta;

import biocode.fims.models.records.GenericRecord;
import biocode.fims.models.records.RecordMetadata;

import java.util.Map;


/**
 * @author rjewing
 */
public class FastaRecord extends GenericRecord {

    public FastaRecord(String parentUniqueKeyUri, String identifier, String sequence, RecordMetadata recordMetadata) {
        super();
        properties.put(FastaProps.SEQUENCE.value(), sequence);
        properties.put(parentUniqueKeyUri, identifier);

        properties.put(FastaProps.IDENTIFIER.value(), identifier + "_" + recordMetadata.get(FastaProps.MARKER.value()));
        for (Map.Entry e : recordMetadata.metadata().entrySet()) {
            properties.put((String) e.getKey(), (String) e.getValue());
        }
    }

}


package biocode.fims.fasta;

import biocode.fims.records.GenericRecord;
import biocode.fims.records.RecordMetadata;

import java.util.Map;


/**
 * @author rjewing
 */
public class FastaRecord extends GenericRecord {

    public FastaRecord(String parentUniqueKeyUri, String identifier, String sequence, RecordMetadata recordMetadata) {
        super();
        properties.put(FastaProps.SEQUENCE.uri(), sequence);
        properties.put(parentUniqueKeyUri, identifier);

        properties.put(FastaProps.IDENTIFIER.uri(), identifier + "_" + recordMetadata.get(FastaProps.MARKER.uri()));
        for (Map.Entry e : recordMetadata.metadata().entrySet()) {
            properties.put((String) e.getKey(), (String) e.getValue());
        }
    }

}


package biocode.fims.fasta;

import biocode.fims.models.records.GenericRecord;
import biocode.fims.models.records.RecordMetadata;

import java.util.Map;

import static biocode.fims.digester.FastaEntity.SEQUENCE_URI;

/**
 * @author rjewing
 */
public class FastaRecord extends GenericRecord {

    public FastaRecord(String parentUniqueKeyUri, String identifier, String sequence, RecordMetadata recordMetadata) {
        super();
        properties.put(SEQUENCE_URI, sequence);
        properties.put(parentUniqueKeyUri, identifier);

        for (Map.Entry e : recordMetadata.metadata().entrySet()) {
            properties.put((String) e.getKey(), (String) e.getValue());
        }
    }

}


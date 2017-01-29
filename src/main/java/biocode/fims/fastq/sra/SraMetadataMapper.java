package biocode.fims.fastq.sra;

import java.util.List;

/**
 * Interface to handle the mapping of Biocode FIMS project attributes to Sra BioSample attributes
 */
public interface SraMetadataMapper {

    boolean hasNextResource();

    List<String> getHeaderValues();

    List<String> getResourceMetadata();
}

package biocode.fims.ncbi.sra.submission;

import java.util.List;

/**
 * Interface to handle the mapping of Biocode FIMS project attributes to Sra BioSample attributes
 */
public interface BioSampleMapper {

    boolean hasNextSample();

    List<String> getHeaderValues();

    List<String> getBioSampleAttributes();
}

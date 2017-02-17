package biocode.fims.fastq;

import biocode.fims.entities.Resource;
import biocode.fims.repositories.ResourceRepository;

import java.util.List;

/**
 * @author rjewing
 */
public interface FastqMetadataRepository extends ResourceRepository {

    List<Resource> getResourcesWithFastqMetadataMissingBioSamples(int projectId);
}

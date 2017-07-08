package biocode.fims.ncbi.sra;

import biocode.fims.application.config.FimsProperties;
import biocode.fims.entities.Project;
import biocode.fims.entities.Resource;
import biocode.fims.fastq.FastqMetadata;
import biocode.fims.fastq.FastqMetadataRepository;
import biocode.fims.fastq.fileManagers.FastqFileManager;
import biocode.fims.fimsExceptions.FimsRuntimeException;
import biocode.fims.ncbi.entrez.BioSampleRepository;
import biocode.fims.ncbi.models.BioSample;
import biocode.fims.service.ProjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * class for harvesting accession's for fimMetadata resource sequences submitted the NCBI SRA database
 *
 * @author rjewing
 */
public class SraAccessionHarvester {
    private static final Logger logger = LoggerFactory.getLogger(SraAccessionHarvester.class);

    private final BioSampleRepository bioSampleRepository;
    private final FastqMetadataRepository fastqMetadataRepository;
    private final ProjectService projectService;
    private final FimsProperties props;

    private List<Resource> resources;

    @Autowired
    public SraAccessionHarvester(FastqMetadataRepository fastqMetadataRepository, BioSampleRepository bioSampleRepository,
                                 ProjectService projectService, FimsProperties props) {
        Assert.notNull(bioSampleRepository);
        Assert.notNull(fastqMetadataRepository);
        this.projectService = projectService;
        this.props = props;
        this.fastqMetadataRepository = fastqMetadataRepository;
        this.bioSampleRepository = bioSampleRepository;
    }

    @Scheduled(cron = "0 0 1 * * *")
    public void harvestForAllProjects() {
        List<Project> projects = projectService.getProjects(props.appRoot());

        for (Project project: projects) {
            harvest(project.getProjectId());
        }
    }

    private void harvest(int projectId) {
        resources = fastqMetadataRepository.getResourcesWithFastqMetadataMissingBioSamples(projectId);

        List<String> bcidsToQuery = datasetBcids();
        List<BioSample> bioSamples = bioSampleRepository.getBioSamples(bcidsToQuery);

        if (bioSamples.isEmpty()) {
            return;
        }

        addBioSamplesToDatasets(bioSamples);
        fastqMetadataRepository.save(resources, projectId);
    }

    private List<String> datasetBcids() {
        List<String> datasetBcids = new ArrayList<>();

        for (Resource resource : resources) {
            datasetBcids.add(resource.getBcid());
        }

        return datasetBcids;
    }

    private void addBioSamplesToDatasets(List<BioSample> bioSamples) {

        for (BioSample bioSample: bioSamples) {
            Resource matchingResource = getDatasetByBcid(bioSample.getBcid());

            if (matchingResource != null) {
                try {
                    FastqMetadata fastqMetadata = matchingResource.getProperty(FastqFileManager.CONCEPT_ALIAS, FastqMetadata.class);

                    fastqMetadata.setBioSample(bioSample);

                    matchingResource.setProperty(FastqFileManager.CONCEPT_ALIAS, fastqMetadata);
                } catch (FimsRuntimeException e) {
                    logger.warn(String.format("Error converting fastqMetadata object for dataset with bcid {}", matchingResource.getBcid()), e);
                }
            }
        }

    }

    private Resource getDatasetByBcid(String bcid) {
        for (Resource resource : resources) {
            if (resource.getBcid().equals(bcid)) {
                return resource;
            }
        }

        return null;
    }


}

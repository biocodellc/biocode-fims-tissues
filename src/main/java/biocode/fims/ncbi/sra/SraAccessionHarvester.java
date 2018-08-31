package biocode.fims.ncbi.sra;

import biocode.fims.application.config.FimsProperties;
import biocode.fims.bcid.Identifier;
import biocode.fims.config.models.Entity;
import biocode.fims.config.models.FastqEntity;
import biocode.fims.fastq.FastqRecord;
import biocode.fims.models.Project;
import biocode.fims.records.Record;
import biocode.fims.records.RecordSet;
import biocode.fims.ncbi.entrez.BioSampleRepository;
import biocode.fims.ncbi.models.BioSample;
import biocode.fims.query.QueryResult;
import biocode.fims.query.QueryResults;
import biocode.fims.query.dsl.Query;
import biocode.fims.repositories.RecordRepository;
import biocode.fims.run.Dataset;
import biocode.fims.service.ProjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.Assert;

import java.util.*;
import java.util.stream.Collectors;

/**
 * class for harvesting accession's for fimMetadata resource sequences submitted the NCBI SRA database
 *
 * @author rjewing
 */
public class SraAccessionHarvester {
    private static final Logger logger = LoggerFactory.getLogger(SraAccessionHarvester.class);

    private final BioSampleRepository bioSampleRepository;
    private final ProjectService projectService;
    private final RecordRepository recordRepository;
    private final FimsProperties props;

    @Autowired
    public SraAccessionHarvester(RecordRepository recordRepository, BioSampleRepository bioSampleRepository,
                                 ProjectService projectService, FimsProperties props) {
        this.props = props;
        Assert.notNull(bioSampleRepository);
        Assert.notNull(recordRepository);
        this.projectService = projectService;
        this.recordRepository = recordRepository;
        this.bioSampleRepository = bioSampleRepository;
    }

//        @Scheduled(cron = "0 0 1 * * *")
    @Scheduled(fixedDelay = 1000 * 60 * 10)
    public void harvestForAllProjects() {
        for (Project project : projectService.getProjects()) {
            harvest(project);
        }
    }

    private void harvest(Project project) {

        project.getProjectConfig().entities().stream()
                .filter(e -> e.type().equals(FastqEntity.TYPE))
                .forEach(e -> {
                    String q = "not _exists_:bioSample and _projects_:" + project.getProjectId();

                    Query query = Query.factory(project, e.getConceptAlias(), q);
                    QueryResults queryResults = recordRepository.query(query);

                    if (queryResults.isEmpty()) {
                        return;
                    }

                    Entity parentEntity = project.getProjectConfig().entity(e.getParentEntity());

                    QueryResult result = queryResults.getResult(e.getConceptAlias());

                    List<String> bcidsToQuery = result.records().stream()
                            .map(r -> r.rootIdentifier() + r.get(parentEntity.getUniqueKeyURI()))
                            .collect(Collectors.toList());
                    List<BioSample> bioSamples = bioSampleRepository.getBioSamples(bcidsToQuery);

                    if (bioSamples.isEmpty()) {
                        return;
                    }

                    Dataset d = generateUpdateDataset(result, parentEntity.getUniqueKeyURI(), bioSamples);
                    recordRepository.saveDataset(d, project.getProjectId());
                });
    }

    private Dataset generateUpdateDataset(QueryResult fastqResults, String parentUniqueKeyURI, List<BioSample> bioSamples) {
        Map<String, RecordSet> recordSets = new HashMap<>();

        Map<String, Record> records = new HashMap<>();
        fastqResults.records().forEach(r -> records.put(r.get(parentUniqueKeyURI), r));

        for (BioSample bioSample : bioSamples) {
            String parentIdentifier = new Identifier(bioSample.getBcid(), props.divider()).getSuffix();

            FastqRecord record = (FastqRecord) records.get(parentIdentifier);
            record.setBioSample(bioSample);

            recordSets.computeIfAbsent(
                    record.expeditionCode(),
                    k -> new RecordSet(fastqResults.entity(), false)
            ).add(record);
        }

        return new Dataset(new ArrayList<>(recordSets.values()));
    }
}

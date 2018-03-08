package biocode.fims.ncbi.sra;

import biocode.fims.digester.FastqEntity;
import biocode.fims.fastq.FastqRecord;
import biocode.fims.models.Project;
import biocode.fims.models.records.Record;
import biocode.fims.models.records.RecordSet;
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

import java.util.Collections;
import java.util.List;
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

    @Autowired
    public SraAccessionHarvester(RecordRepository recordRepository, BioSampleRepository bioSampleRepository,
                                 ProjectService projectService) {
        Assert.notNull(bioSampleRepository);
        Assert.notNull(recordRepository);
        this.projectService = projectService;
        this.recordRepository = recordRepository;
        this.bioSampleRepository = bioSampleRepository;
    }

    @Scheduled(cron = "0 0 1 * * *")
    public void harvestForAllProjects() {
        for (Project project : projectService.getProjects()) {
            harvest(project);
        }
    }

    private void harvest(Project project) {

        project.getExpeditions().forEach(expedition -> {
            project.getProjectConfig().entities().stream()
                    .filter(e -> e.type().equals(FastqEntity.TYPE))
                    .forEach(e -> {
                        String q = "_select_:" +
                                e.getParentEntity() +
                                " not _exists_:bioSample and _expedition_:" +
                                expedition.getExpeditionCode();

                        Query query = Query.factory(project, e.getConceptAlias(), q);
                        QueryResults queryResults = recordRepository.query(query);

                        QueryResult parentResults = queryResults.getResult(e.getParentEntity());

                        String rootIdentifier = parentResults.rootIdentifier();
                        List<String> bcidsToQuery = parentResults.get(false).stream()
                                .map(r -> rootIdentifier + r.get(parentResults.entity().getUniqueKey()))
                                .collect(Collectors.toList());
                        List<BioSample> bioSamples = bioSampleRepository.getBioSamples(bcidsToQuery);

                        if (bioSamples.isEmpty()) {
                            return;
                        }

                        Dataset d = generateUpdateDataset(parentResults, queryResults.getResult(e.getConceptAlias()), bioSamples);
                        recordRepository.save(d, project.getProjectId(), expedition.getExpeditionId());
                    });
        });
    }

    private Dataset generateUpdateDataset(QueryResult parentResults, QueryResult fastqResults, List<BioSample> bioSamples) {
        String parentUniqueKey = parentResults.entity().getUniqueKey();
        String rootIdentifier = parentResults.rootIdentifier();

        RecordSet recordSet = new RecordSet(parentResults.entity(), false);

        for (BioSample bioSample : bioSamples) {
            String parentIdentifier = bioSample.getBcid().replace(rootIdentifier, "");

            for (Record record : fastqResults.records()) {
                if (parentIdentifier.equals(record.get(parentUniqueKey))) {
                    FastqRecord r = (FastqRecord) record;
                    r.setBioSample(bioSample);
                    recordSet.add(r);
                    break;
                }
            }
        }

        return new Dataset(Collections.singletonList(recordSet));
    }
}

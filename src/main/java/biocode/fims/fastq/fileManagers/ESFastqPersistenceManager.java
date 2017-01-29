package biocode.fims.fastq.fileManagers;

import biocode.fims.digester.Mapping;
import biocode.fims.elasticSearch.ElasticSearchIndexer;
import biocode.fims.fastq.FastqMetadata;
import biocode.fims.rest.SpringObjectMapper;
import biocode.fims.run.ProcessController;
import biocode.fims.utils.EmailUtils;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * {@link FastqPersistenceManager to handle uploading to Elastic Search
 */
public class ESFastqPersistenceManager implements FastqPersistenceManager {
    private final Logger logger = LoggerFactory.getLogger(ESFastqPersistenceManager.class);
    private final Client esClient;

    public ESFastqPersistenceManager(Client esClient) {
        this.esClient = esClient;
    }

    @Override
    public void upload(ProcessController processController, Map<String, FastqMetadata> resourceFastqMetadataObjects, boolean newDataset) {
        // do nothing, as elasticsearch "uploading" is handled in FastqFileManager.index
    }

    @Override
    public Map<String, FastqMetadata> getFastqMetadata(ProcessController processController, String conceptAlias) {
        Map<String, FastqMetadata> resourceFastaqMetadataObjects = new HashMap<>();
        Mapping mapping = processController.getMapping();
        String uniqueKey = mapping.lookupUriForColumn(mapping.getDefaultSheetUniqueKey(), mapping.getDefaultSheetAttributes());

        SearchRequestBuilder builder = esClient.prepareSearch(String.valueOf(processController.getProjectId()))
                .setTypes(ElasticSearchIndexer.TYPE)
                .setSize(1000)
                .setScroll(new TimeValue(1, TimeUnit.MINUTES))
                .setQuery(QueryBuilders.boolQuery()
                        .must(
                                QueryBuilders.boolQuery()
                                        .filter(
                                                QueryBuilders.existsQuery(conceptAlias)
                                        )
                        ).must(
                                QueryBuilders.matchQuery("expedition.expeditionCode.keyword", processController.getExpeditionCode())
                        )
                )
                .setFetchSource(new String[]{uniqueKey, conceptAlias}, null);
        SearchResponse response = builder.get();


        if (response.status() != RestStatus.OK) {
            logger.warn("FAILED to fetch fastq metadata for ExpeditionCode: {}.\n {}", processController.getExpeditionCode(), response);
            EmailUtils.sendAdminEmail(
                    "FAILED to fetch fastq metadata",
                    "ElasticSearch index may be out of sync with uploaded data for expedition: " + processController.getExpeditionCode() +
                            " and projectId: " + processController.getProjectId()
            );
        } else {

            SpringObjectMapper objectMapper = new SpringObjectMapper();
            do {

                for (SearchHit hit : response.getHits().getHits()) {
                    Map<String, Object> source = hit.getSource();
                    String localIdentifier = String.valueOf(source.get(uniqueKey));

                    FastqMetadata fastqMetadata = objectMapper.convertValue(source.get(conceptAlias), FastqMetadata.class);

                    resourceFastaqMetadataObjects.put(localIdentifier, fastqMetadata);
                }

                response = esClient.prepareSearchScroll(response.getScrollId()).setScroll(new TimeValue(1, TimeUnit.MINUTES)).get();

            } while (response.getHits().getHits().length != 0);

        }

        return resourceFastaqMetadataObjects;
    }
}

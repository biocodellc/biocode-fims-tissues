package biocode.fims.fastq;

import biocode.fims.elasticSearch.ElasticSearchCode;
import biocode.fims.elasticSearch.ElasticSearchIndexer;
import biocode.fims.elasticSearch.EsResourceRepository;
import biocode.fims.elasticSearch.query.ElasticSearchQuerier;
import biocode.fims.elasticSearch.query.ElasticSearchQuery;
import biocode.fims.entities.Resource;
import biocode.fims.fastq.fileManagers.FastqFileManager;
import biocode.fims.fimsExceptions.FimsRuntimeException;
import biocode.fims.repositories.ResourceRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author rjewing
 */
public class EsFastqMetadataRepository extends EsResourceRepository implements FastqMetadataRepository, ResourceRepository {
    private static final Logger logger = LoggerFactory.getLogger(EsFastqMetadataRepository.class);

    private final Client esClient;

    public EsFastqMetadataRepository(Client esClient) {
        super(esClient);
        this.esClient = esClient;
    }

    @Override
    public List<Resource> getResourcesWithFastqMetadataMissingBioSamples(int projectId) {
        BoolQueryBuilder query = QueryBuilders.boolQuery();

        query.must(
                QueryBuilders.existsQuery(FastqFileManager.CONCEPT_ALIAS)
        ).mustNot(
                QueryBuilders.existsQuery(FastqFileManager.CONCEPT_ALIAS + ".bioSample")
        );

        ElasticSearchQuery esQuery = new ElasticSearchQuery(query, new String[] {String.valueOf(projectId)}, new String[] {ElasticSearchIndexer.TYPE});

        ElasticSearchQuerier querier = new ElasticSearchQuerier(esClient, esQuery);

        ArrayNode querierResults = querier.getAllResults();

        return transformQueryierResults(querierResults);
    }

    private List<Resource> transformQueryierResults(ArrayNode querierResults) {
        List<Resource> resources = new ArrayList<>();

        for (JsonNode node : querierResults) {
            if (node.isObject()) {
                Resource resource = new Resource(node.get("bcid").asText(), (ObjectNode) node);
                resources.add(resource);
            } else {
                throw new FimsRuntimeException(ElasticSearchCode.UNEXPECTED_JSON_NODE, "was expecting objectNode, but got something else", 500);
            }
        }

        return resources;
    }
}

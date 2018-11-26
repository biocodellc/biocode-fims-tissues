package biocode.fims.tissues.reader;

import biocode.fims.config.models.TissueEntity;
import biocode.fims.config.project.ProjectConfig;
import biocode.fims.fimsExceptions.FimsRuntimeException;
import biocode.fims.fimsExceptions.errorCodes.DataReaderCode;
import biocode.fims.reader.DataConverter;
import biocode.fims.records.GenericRecord;
import biocode.fims.records.Record;
import biocode.fims.records.RecordSet;
import biocode.fims.tissues.TissueProps;
import biocode.fims.tissues.TissueRepository;
import biocode.fims.utils.RecordHasher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author rjewing
 */
public class TissueConverter implements DataConverter {
    private final static Logger logger = LoggerFactory.getLogger(TissueConverter.class);

    private final TissueRepository tissueRepository;
    protected ProjectConfig config;

    private String parentKey;
    private Map<String, Integer> existingTissuesByParentId;
    private Map<String, Record> existingTissuesByHash;

    public TissueConverter(TissueRepository tissueRepository) {
        this.tissueRepository = tissueRepository;
    }

    private TissueConverter(TissueRepository tissueRepository, ProjectConfig projectConfig) {
        this.tissueRepository = tissueRepository;
        this.config = projectConfig;
    }

    @Override
    public RecordSet convertRecordSet(RecordSet recordSet, int networkId) {
        if (!(recordSet.entity() instanceof TissueEntity)) {
            throw new FimsRuntimeException(DataReaderCode.READ_ERROR, 500);
        }

        TissueEntity tissueEntity = (TissueEntity) recordSet.entity();
        if (!tissueEntity.isGenerateID()) return recordSet;


        String parent = tissueEntity.getParentEntity();
        parentKey = config.entity(parent).getUniqueKeyURI();

        existingTissuesByParentId = new HashMap<>();
        existingTissuesByHash = new HashMap<>();
        Map<String, Integer> existingTissuesByParentIdCount = new HashMap<>();
        
        getExistingRecords(recordSet, networkId, parentKey).stream()
                .filter(r -> !r.get(TissueProps.IDENTIFIER.uri()).equals(""))
                .forEach(r -> {
                    String parentID = r.get(parentKey);

                    // we get the max here so we don't create duplicates if a tissue has been deleted
                    // if id is of form parentIdentifier.[0-9] we parse the digit and update max if
                    // necessary
                    int count = existingTissuesByParentIdCount.getOrDefault(parentID, 0);
                    int max = existingTissuesByParentId.getOrDefault(parentID, count);

                    if (count > max) max = count;

                    Pattern p = Pattern.compile(parentID + "\\.(\\d+)");
                    Matcher matcher = p.matcher(r.get(TissueProps.IDENTIFIER.uri()));
                    if (matcher.matches()) {
                        Integer i = Integer.parseInt(matcher.group(1));
                        if (i > max) max = i;
                    }
                    existingTissuesByParentIdCount.put(parentID, ++count);
                    existingTissuesByParentId.put(parentID, max);

                    Map<String, String> props = new HashMap<>(r.properties());
                    props.remove(TissueProps.IDENTIFIER.uri());

                    Record record = new GenericRecord(props);
                    // store record hashes w/o tissueID so we can compare values before generating the tissue
                    existingTissuesByHash.put(RecordHasher.hash(record), r);
                });

        updateRecords(recordSet);
        return recordSet;
    }

    /**
     * Generate unique tissueIDs for each record
     *
     * @param recordSet
     */
    private void updateRecords(RecordSet recordSet) {

        for (Record r : recordSet.recordsToPersist()) {

            if (r.get(TissueProps.IDENTIFIER.uri()).equals("")) {
                // check the hash of the Tissue w/o the identifier included
                // if we have an existing tissue w/ a matching hash, then this
                // is treated as an update to the existing tissue, otherwise
                // we create a identifier for the new tissue
                String hash = RecordHasher.hash(r);
                Record existingRecord = existingTissuesByHash.get(hash);
                if (existingRecord == null) {
                    String parentID = r.get(parentKey);
                    int count = existingTissuesByParentId.getOrDefault(parentID, 0);
                    count += 1;

                    r.set(TissueProps.IDENTIFIER.uri(), parentID + "." + count);
                    existingTissuesByParentId.put(parentID, count);
                } else {
                    r.set(TissueProps.IDENTIFIER.uri(), existingRecord.get(TissueProps.IDENTIFIER.uri()));
                }

            }
        }
    }

    /**
     * fetch any existing records for that are in the given RecordSet
     *
     * @param recordSet
     * @param networkId
     * @param parentKey
     * @return
     */
    private List<Record> getExistingRecords(RecordSet recordSet, int networkId, String parentKey) {
        if (networkId == 0 || recordSet.expeditionCode() == null) {
            throw new FimsRuntimeException(DataReaderCode.READ_ERROR, 500);
        }

        List<String> parentIdentifiers = recordSet.recordsToPersist().stream()
                .map(r -> r.get(parentKey))
                .distinct()
                .collect(Collectors.toList());

        return tissueRepository.getTissues(networkId, recordSet.projectId(), recordSet.conceptAlias(), parentIdentifiers);
    }

    @Override
    public DataConverter newInstance(ProjectConfig projectConfig) {
        return new TissueConverter(tissueRepository, projectConfig);
    }
}

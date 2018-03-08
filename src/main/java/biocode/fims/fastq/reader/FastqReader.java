package biocode.fims.fastq.reader;

import biocode.fims.digester.Entity;
import biocode.fims.digester.FastaEntity;
import biocode.fims.exceptions.FastaReaderCode;
import biocode.fims.exceptions.FastqReaderCode;
import biocode.fims.fasta.FastaRecord;
import biocode.fims.fastq.FastqRecord;
import biocode.fims.fimsExceptions.FimsRuntimeException;
import biocode.fims.fimsExceptions.ServerErrorException;
import biocode.fims.fimsExceptions.errorCodes.DataReaderCode;
import biocode.fims.models.records.Record;
import biocode.fims.models.records.RecordMetadata;
import biocode.fims.models.records.RecordSet;
import biocode.fims.projectConfig.ProjectConfig;
import biocode.fims.reader.DataReader;
import org.apache.commons.lang.StringUtils;
import org.springframework.util.Assert;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static biocode.fims.digester.FastaEntity.MARKER_KEY;
import static biocode.fims.digester.FastaEntity.MARKER_URI;
import static biocode.fims.fastq.FastqProps.LIBRARY_LAYOUT;

/**
 * DataReader implementation for Fastq metadata files.
 *
 *
 * This Reader expects the following RecordMetadata:
 *
 *     - {@link FastqReader.CONCEPT_ALIAS_KEY}
 *
 */
public class FastqReader implements DataReader {
    public static final String CONCEPT_ALIAS_KEY = "conceptAlias";
    public static final List<String> EXTS = Arrays.asList("txt");

//    public static final Pattern PAIRED_FILE_PATTERN = Pattern.compile("^(parentId)(\\.|_).*(1|2).*\\.(fq|fastq)(\\.gz|\\.gzip|\\.bz2)?$");
//    public static final Pattern PAIRED_FILE_2_PATTERN = Pattern.compile("^(parentId)(\\.|_).*2.*\\.(fq|fastq)(\\.gz|\\.gzip|\\.bz2)?$");
//    public static final Pattern SINGLE_FILE_PATTERN = Pattern.compile("^(parentId)\\.(fq|fastq)$");
    public static final Pattern ID_PATTERN = Pattern.compile("^(parentId)(\\.|_).*$");

    protected File file;
    protected ProjectConfig config;
    private RecordMetadata recordMetadata;
    private List<RecordSet> recordSets;
    private Map<String, List<String>> filenames;
    private String parentUniqueKeyUri;
//    private String libraryLayout;

    /**
     * This is only to be used for passing the class into the DataReaderFactory
     */
    public FastqReader() {
    }

    public FastqReader(File file, ProjectConfig projectConfig, RecordMetadata recordMetadata) {
        Assert.notNull(file);
        Assert.notNull(projectConfig);
        Assert.notNull(recordMetadata);
        this.file = file;
        this.config = projectConfig;
        this.recordMetadata = recordMetadata;

        // so we know which one we are dealing with
        if (!recordMetadata.has(CONCEPT_ALIAS_KEY)) {
//                !recordMetadata.has(LIBRARY_LAYOUT.value())) {
            throw new FimsRuntimeException(DataReaderCode.MISSING_METADATA, 500);
        }

//        this.libraryLayout = ((String) recordMetadata.get(LIBRARY_LAYOUT.value())).toLowerCase();
    }

    @Override
    public boolean handlesExtension(String ext) {
        return EXTS.contains(ext.toLowerCase());
    }

    @Override
    public DataReader newInstance(File file, ProjectConfig projectConfig, RecordMetadata recordMetadata) {
        return new FastqReader(file, projectConfig, recordMetadata);
    }

    @Override
    public List<RecordSet> getRecordSets() {
        if (recordSets == null) {
            Entity entity = this.config.entity((String) recordMetadata.remove(CONCEPT_ALIAS_KEY));
            Entity parentEntity = this.config.entity(entity.getParentEntity());
            this.parentUniqueKeyUri = parentEntity.getUniqueKeyURI();

            List<Record> records = generateRecords();

            if (records.isEmpty()) {
                throw new FimsRuntimeException(FastqReaderCode.NO_DATA, 400);
            }

            recordSets = Collections.singletonList(
                    new RecordSet(entity, records, recordMetadata.reload())
            );
        }

        return recordSets;
    }

    private List<Record> generateRecords() {
        if (filenames == null) {
            filenames = parseFastqFilenames();
        }
        List<Record> fastaRecords = new ArrayList<>();

        for (Map.Entry<String, List<String>> entry: filenames.entrySet()) {
            // TODO what is entry.key() for failed matches
            fastaRecords.add(
                    new FastqRecord(parentUniqueKeyUri, entry.getKey(), entry.getValue(), recordMetadata)
            );
        }

        return fastaRecords;
    }

    private Map<String, List<String>> parseFastqFilenames() {
        Map<String, List<String>> recordFilenames = new HashMap<>();

        try {
            FileReader input = new FileReader(this.file);
            BufferedReader br = new BufferedReader(input);
            String line;

            while (!StringUtils.isBlank(line = br.readLine())) {
                Matcher matcher = ID_PATTERN.matcher(line);

                matcher.matches();
                String id = matcher.group(0);

                // TODO what happens if matcher.matches() fails?
                recordFilenames.computeIfAbsent(id, k -> new ArrayList<>()).add(line);
            }
        } catch (IOException e) {
            throw new ServerErrorException(e);
        }

        return recordFilenames;
    }

    @Override
    public DataReaderType readerType() {
        return FastqDataReaderType.READER_TYPE;
    }
}

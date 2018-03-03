package biocode.fims.fasta;

import biocode.fims.digester.Entity;
import biocode.fims.digester.FastaEntity;
import biocode.fims.exceptions.FastaWriteCode;
import biocode.fims.fimsExceptions.FimsRuntimeException;
import biocode.fims.fimsExceptions.errorCodes.FileCode;
import biocode.fims.fimsExceptions.errorCodes.QueryCode;
import biocode.fims.projectConfig.ProjectConfig;
import biocode.fims.query.QueryResult;
import biocode.fims.query.writers.QueryWriter;
import biocode.fims.settings.PathManager;
import biocode.fims.utils.FileUtils;
import org.apache.commons.lang.StringUtils;

import java.io.*;
import java.util.*;

/**
 * @author RJ Ewing
 */
public class FastaQueryWriter implements QueryWriter {
    private final QueryResult queryResult;
    private final ProjectConfig config;
    private final Entity parentEntity;
    private final String uniqueKey;
    private final String parentUniqueKey;

    public FastaQueryWriter(QueryResult queryResult, ProjectConfig config) {
        // TODO pass in parentRecords & list of columns to write out as metadata
        this.config = config;
        if (!(queryResult.entity() instanceof FastaEntity)) {
            throw new FimsRuntimeException(FastaWriteCode.INVALID_ENTITY, 500, queryResult.entity().type());
        }
        this.queryResult = queryResult;

        if (queryResult.get(false).size() == 0) {
            throw new FimsRuntimeException(QueryCode.NO_RESOURCES, 400);
        }

        parentEntity = config.entity(queryResult.entity().getParentEntity());
        uniqueKey = queryResult.entity().getUniqueKey();
        parentUniqueKey = parentEntity.getUniqueKey();
    }

    /**
     * writes the records in QueryResults to a file. If more the 1 marker type is present,
     * a zip file will be returned with a fasta file with records for each unique marker
     *
     * @return
     */
    @Override
    public File write() {
        Map<String, List<Map<String, String>>> recordsMap = sortByMarker();
        List<File> sequenceFiles = new ArrayList<>();

        for (Map.Entry<String, List<Map<String, String>>> entry : recordsMap.entrySet()) {
            sequenceFiles.add(
                    writeMarkerFile(entry.getValue(), entry.getKey())
            );
        }

        if (sequenceFiles.size() == 1) {
            return sequenceFiles.get(0);
        } else {
            Map<String, File> fastaFileMap = new HashMap<>();

            for (File fastaFile : sequenceFiles) {
                fastaFileMap.put(fastaFile.getName(), fastaFile);
            }

            return FileUtils.zip(fastaFileMap, System.getProperty("java.io.tmpdir"));
        }
    }

    private Map<String, List<Map<String, String>>> sortByMarker() {
        Map<String, List<Map<String, String>>> fastaFileMap = new HashMap<>();

        for (Map<String, String> record : queryResult.get(false)) {
            String marker = record.get(FastaEntity.MARKER_KEY);

            fastaFileMap
                    .computeIfAbsent(marker, k -> new ArrayList<>())
                    .add(record);
        }

        return fastaFileMap;
    }

    private File writeMarkerFile(List<Map<String, String>> records, String marker) {
        String filename = StringUtils.isBlank(marker) ? "output.fasta" : marker + ".fasta";
        File file = PathManager.createFile(filename, System.getProperty("java.io.tmpdir"));

        try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(file)))) {


            for (Map<String, String> record : records) {
                writer.write(">");

                String identifier = queryResult.rootIdentifier();
                if (!identifier.endsWith("/")) identifier += "/";
                identifier += queryResult.entity().buildChildIdentifier(
                        record.get(parentUniqueKey),
                        record.get(uniqueKey)
                );
                writer.write(identifier);

                writer.write(" [marker = ");
                writer.write(record.get(FastaEntity.MARKER_KEY));
                writer.write("] [");
                writer.write(parentUniqueKey);
                writer.write(" = ");
                writer.write(record.get(parentUniqueKey));
                writer.write("]\n");

                // TODO add more metadata (locality, genus, species) once networks are implemented

                writer.write(record.get(FastaEntity.SEQUENCE_KEY));
                writer.write("\n");
            }

        } catch (IOException e) {
            throw new FimsRuntimeException(FileCode.WRITE_ERROR, 500);
        }

        return file;
    }
}

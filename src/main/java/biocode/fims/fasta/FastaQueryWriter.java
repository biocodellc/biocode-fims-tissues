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
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author RJ Ewing
 */
public class FastaQueryWriter implements QueryWriter {
    private final QueryResult queryResult;
    private final ProjectConfig config;

    public FastaQueryWriter(QueryResult queryResult, ProjectConfig config) {
        // TODO pass in parentRecords & list of columns to write out as metadata
        this.config = config;
        if (!(queryResult.entity() instanceof FastaEntity)) {
            throw new FimsRuntimeException(FastaWriteCode.INVALID_ENTITY, 500, queryResult.entity().type());
        }
        this.queryResult = queryResult;
    }

    @Override
    public File write() {
        File file = PathManager.createUniqueFile("output.fasta", System.getProperty("java.io.tmpdir"));

        try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(file)))) {

            List<Map<String, String>> records = queryResult.get(true);

            if (records.size() == 0) {
                throw new FimsRuntimeException(QueryCode.NO_RESOURCES, 400);
            }

            Entity parentEntity = config.entity(queryResult.entity().getParentEntity());

            String uniqueKey = queryResult.entity().getUniqueKey();
            String parentUniqueKey = parentEntity.getUniqueKey();
            for (Map<String, String> record: records) {
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

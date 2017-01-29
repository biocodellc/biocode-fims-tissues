package biocode.fims.fastq.sra;

import biocode.fims.exceptions.SraCode;
import biocode.fims.fimsExceptions.FimsRuntimeException;
import biocode.fims.settings.PathManager;

import java.io.*;
import java.util.List;

/**
 * class to generate a NCBI sra metadata file (https://www.ncbi.nlm.nih.gov/sra/docs/submitmeta/)
 * This takes the FastqMetadata and resource metadata stored in the FIMS system and writes it to a tsv file.
 */
public class SraMetadataGenerator {
    private static final String DELIMITER = "\t";

    /**
     * generates a sra metadata file from the provided {@link SraMetadataMapper}
     * @param mapper
     * @param outputDirectory
     * @return
     */
    public static File generateFile(SraMetadataMapper mapper, String outputDirectory) {
        File metadataFile = PathManager.createUniqueFile("sra-metadata.tsv", outputDirectory);

        try (FileWriter fw = new FileWriter(metadataFile)) {
            List<String> headers = mapper.getHeaderValues();

            for (String header : headers) {
                fw.write(header + DELIMITER);
            }
            fw.write("\n");

            while (mapper.hasNextResource()) {
                List<String> resourceMetadata = mapper.getResourceMetadata();

                if (!resourceMetadata.isEmpty()) {
                    for (String mValue : resourceMetadata) {
                        fw.write(mValue + DELIMITER);
                    }
                    fw.write("\n");
                }
            }

        } catch (IOException e) {
            throw new FimsRuntimeException(SraCode.METADATA_FILE_CREATION_FAILED, 500);
        }

//        try (LineNumberReader lineNumberReader = new LineNumberReader(new FileReader(metadataFile))) {
//            lineNumberReader.skip(Long.MAX_VALUE);
//
//            if (lineNumberReader.getLineNumber() == 0) {
//                throw new FimsRuntimeException(SraCode.MISSING_FASTQ_METADATA, 400);
//            }
//
//        } catch (IOException e) {
//            throw new FimsRuntimeException(SraCode.METADATA_FILE_CREATION_FAILED, 500);
//        }

        return metadataFile;
    }
}

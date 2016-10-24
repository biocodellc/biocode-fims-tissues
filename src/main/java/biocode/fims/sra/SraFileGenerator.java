package biocode.fims.sra;

import biocode.fims.exceptions.SraCode;
import biocode.fims.fimsExceptions.FimsRuntimeException;
import biocode.fims.settings.PathManager;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * class to generate a zip of Genbank Sra Metadata and BioSample files. See {@link SraMetadataGenerator} and {@link BioSampleAttributesGenerator}
 */
public class SraFileGenerator {

    public static File generateFiles(BioSampleMapper sampleMapper, SraMetadataMapper metadataMapper, String outputDirectory) {
        Map<String, File> zipFilesMap = new HashMap<>();
        zipFilesMap.put("bioSample-attributes.tsv", BioSampleAttributesGenerator.generateFile(sampleMapper, outputDirectory));
        zipFilesMap.put("sra-metadata.tsv", SraMetadataGenerator.generateFile(metadataMapper, outputDirectory));
        File sraFile = PathManager.createUniqueFile("sra-files.zip", outputDirectory);

        try {
            ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(sraFile));

            // Create a buffer for reading the files
            byte[] buf = new byte[1024];

            for (Map.Entry<String, File> fileEntry: zipFilesMap.entrySet()) {
                FileInputStream in = new FileInputStream(fileEntry.getValue().getCanonicalPath());
                zout.putNextEntry(new ZipEntry(fileEntry.getKey()));

                int len;
                while ((len = in.read(buf)) > 0) {
                    zout.write(buf, 0, len);
                }
                zout.closeEntry();
                in.close();
            }

            zout.close();
            return sraFile;
        } catch (IOException e) {
            throw new FimsRuntimeException(SraCode.SRA_FILES_FAILED, 500);
        }
    }

}

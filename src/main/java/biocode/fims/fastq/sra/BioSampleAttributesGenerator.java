package biocode.fims.fastq.sra;

import biocode.fims.exceptions.SraCode;
import biocode.fims.fimsExceptions.FimsRuntimeException;
import biocode.fims.settings.PathManager;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * class to generate a NCBI sra BioSample attributes file (https://www.ncbi.nlm.nih.gov/biosample/docs/submission/faq/)
 * This takes the sample metadata stored in the FIMS system and writes it to a tsv file. The attributes file is compatible
 * with both invertebrate and "model organism or animal sample" BioSample submissions.
 */
public class BioSampleAttributesGenerator {
    private static final String DELIMITER = "\t";

    /**
     * generates a bioSample attributes file from the provided {@link BioSampleMapper}
     * @param mapper {@link BioSampleMapper} implementation
     * @param outputDirectory
     * @return
     */
    public static File generateFile(BioSampleMapper mapper, String outputDirectory) {
        File attributesFile = PathManager.createUniqueFile("bioSample-attributes.tsv", outputDirectory);

        try (FileWriter fw = new FileWriter(attributesFile)) {

            List<String> headers = mapper.getHeaderValues();

            for (String header: headers) {
                fw.write(header + DELIMITER);
            }
            fw.write("\n");

            while (mapper.hasNextSample()) {
                List<String> bioSampleAttributes = mapper.getBioSampleAttributes();

                if (!bioSampleAttributes.isEmpty()) {
                    for (String mValue : bioSampleAttributes) {
                        fw.write(mValue + DELIMITER);
                    }
                    fw.write("\n");
                }
            }
        } catch (IOException e) {
            throw new FimsRuntimeException(SraCode.METADATA_FILE_CREATION_FAILED, 500);
        }

        return attributesFile;


    }
}

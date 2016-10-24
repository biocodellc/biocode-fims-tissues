package biocode.fims.sra;

import biocode.fims.fileManagers.fastq.FastqFileManager;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Abstract class that contains general SraMetadataMapper funcitonality
 */
public abstract class AbstractSraMetadataMapper implements SraMetadataMapper{

    public static final List<String> SRA_HEADERS = new ArrayList<String>() {{
        add("library_ID");
        add("title");
        add("library_strategy");
        add("library_source");
        add("library_selection");
        add("library_layout");
        add("platform");
        add("instrument_model");
        add("design_description");
        add("filetype");
        add("filename");
        add("filename2");
    }};

    @Override
    public List<String> getHeaderValues() {
        return SRA_HEADERS;
    }

    /**
     * Search the fastqFilenames list for the fastq filename for the given sampleId
     * @param fastqFilenames list of fastq filenames
     * @param sampleId the identifier of the fastq file to find
     * @param getPairedFile get the 2nd of the paired fastq files
     * @return
     */
    protected String getFilename(List<String> fastqFilenames, String sampleId, boolean getPairedFile) {
        Pattern pairedEnd1Pattern = Pattern.compile(FastqFileManager.PAIRED_FILE_1_PATTERN.replace("{sampleId}", sampleId));
        Pattern pairedEnd2Pattern = Pattern.compile(FastqFileManager.PAIRED_FILE_2_PATTERN.replace("{sampleId}", sampleId));
        Pattern singleEndPattern = Pattern.compile(FastqFileManager.SINGLE_FILE_PATTERN.replace("{sampleId}", sampleId));

        String singleFile = null;

        for (String filename : fastqFilenames) {
            if (getPairedFile) {
                if (pairedEnd2Pattern.matcher(filename).matches()) {
                    return filename;
                }
            } else {
                // try to find the pairedEnd1 file before the singleEnd file as pairedEnd1Pattern is more specific
                if (pairedEnd1Pattern.matcher(filename).matches()) {
                    return filename;
                } else if (singleEndPattern.matcher(filename).matches()) {
                    // don't return file directly here in case a pairedEnd1 file occurs later in the list. It is
                    // possible that this is a pairedEnd2 file
                    singleFile = filename;
                }
            }
        }

        return singleFile;
    }
}

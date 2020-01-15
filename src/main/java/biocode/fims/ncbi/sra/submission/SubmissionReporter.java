package biocode.fims.ncbi.sra.submission;

import biocode.fims.application.config.TissueProperties;
import biocode.fims.models.SraSubmission;
import biocode.fims.ncbi.models.submission.SraSubmissionReport;
import biocode.fims.repositories.SraSubmissionRepository;
import biocode.fims.utils.EmailUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.*;

/**
 * @author rjewing
 */
public class SubmissionReporter {
    private static final long FOUR_HOURS = 1000 * 60 * 60 * 4;
    private static final Logger logger = LoggerFactory.getLogger(SubmissionReporter.class);

    private final SraSubmissionRepository submissionRepository;
    private final TissueProperties tissueProperties;

    public SubmissionReporter(SraSubmissionRepository submissionRepository, TissueProperties tissueProperties) {
        this.submissionRepository = submissionRepository;
        this.tissueProperties = tissueProperties;
    }

    @Scheduled(initialDelay = 60 * 1000 * 2, fixedDelay = FOUR_HOURS)
    public void checkSubmissions() {
        for (SraSubmission submission : submissionRepository.getByStatus(SraSubmission.Status.SUBMITTED)) {
            checkReport(submission);
        }
    }

    private void checkReport(SraSubmission submission) {
        FTPClient client = new FTPClient();
        PipedOutputStream os = new PipedOutputStream();
        PipedInputStream is = null;

        try {
            client.connect(tissueProperties.sraSubmissionUrl());
            client.login(tissueProperties.sraSubmissionUser(), tissueProperties.sraSubmissionPassword());

            String dirName = tissueProperties.sraSubmissionRootDir() + "/" + submission.getSubmissionDir().getFileName().toString();

            client.changeWorkingDirectory(dirName);

            // hasn't started processing yet
            if (client.listFiles(dirName + "/" + "report.xml").length == 0) {
                return;
            }

            is = new PipedInputStream(os);
            client.retrieveFile("report.xml", os);

            System.out.println(is);

//            JAXBContext jaxbContext = JAXBContext.newInstance(SraSubmissionReport.class);
//            Unmarshaller marshaller = jaxbContext.createUnmarshaller();
//            SraSubmissionReport report = marshaller.unmarshal(is);

//            submission.setStatus(SraSubmission.Status.COMPLETED);

            client.logout();
//        } catch (JAXBException e) {
        } catch (IOException e) {
            logger.error("Failed to read SRA submission report.", e);
        } finally {
            try {
                os.close();
                if (is != null) {
                    is.close();
                }
                client.disconnect();
            } catch (IOException e) {
                logger.error("Error closing input stream", e);
            }
        }

//        this.submissionRepository.save(submission);

        // notify user
//        EmailUtils.sendEmail(
//                submission.getUser().getEmail(),
//                "SRA Submission Successful",
//                "Your submission for \"" + submission.getExpedition().getExpeditionTitle() + "\" has been successfully uploaded to the SRA via GEOME.\n\n" +
//                        "You will receive another email with a status report once the SRA has processed your submission."
//        );
    }
}

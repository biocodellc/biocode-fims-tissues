package biocode.fims.fasta.fileManagers;

import biocode.fims.digester.Entity;
import biocode.fims.entities.Bcid;
import biocode.fims.fasta.FastaSequence;
import biocode.fims.fasta.FastaUtils;
import biocode.fims.fimsExceptions.ServerErrorException;
import biocode.fims.fuseki.Uploader;
import biocode.fims.run.ProcessController;
import biocode.fims.service.BcidService;
import biocode.fims.service.ExpeditionService;
import biocode.fims.settings.PathManager;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.hp.hpl.jena.sparql.modify.UpdateProcessRemote;
import com.hp.hpl.jena.update.UpdateExecutionFactory;
import com.hp.hpl.jena.update.UpdateFactory;
import com.hp.hpl.jena.update.UpdateRequest;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * {@link FastaPersistenceManager to handle uploading to fuseki
 * //TODO rewrite this to support multiple fastaSequence per resource, and use the fastaSequence entity attributes
 */
public class FusekiFastaPersistenceManager implements FastaPersistenceManager {
    private final BcidService bcidService;
    private final ExpeditionService expeditionService;

    @Autowired
    public FusekiFastaPersistenceManager(BcidService bcidService, ExpeditionService expeditionService) {
        this.bcidService = bcidService;
        this.expeditionService = expeditionService;
    }

    @Override
    public void upload(ProcessController processController, Map<String, ArrayNode> fastaSequences, boolean newDataset) {
        String fusekiService = processController.getMapping().getMetadata().getQueryTarget();

        if (!fastaSequences.isEmpty()) {

            // save fasta data as a triple file
            File tripleFile = PathManager.createUniqueFile(
                    processController.getExpeditionCode() + "_output",
                    processController.getOutputFolder()
            );

            try (PrintWriter out = new PrintWriter(tripleFile)) {

                for (String localIdentifier: fastaSequences.keySet()) {
                    ArrayNode sequence = (ArrayNode) fastaSequences.get(0);

                    if (sequence != null) {
                        out.write("<");
                        out.write(getEntityRootIdentifier(processController) + localIdentifier);
                        out.write("> <" + FastaSequence.SEQUENCE_URI + "> \"");
                        out.write(String.valueOf(sequence.get("sequence")));
                        out.write("\" .\n");
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            Bcid currentBcid = getCurrentBcid(processController);
            Uploader u = new Uploader(fusekiService + "/data", tripleFile, currentBcid.getGraph());
            u.execute();

            if (!newDataset) {
                bcidService.updateTs(currentBcid);
            }

            // delete the tripleFile now that it has been uploaded
            tripleFile.delete();
            processController.appendSuccessMessage(
                    "<br>\t" + "FASTA data added to dataset belonging to Expedition Code: " + processController.getExpeditionCode()
            );
        } else {
            // if fastaSequences is null, then we need to copy the sequences from the previous graph to the current graph
            copySequences(processController, fusekiService, newDataset);
        }

    }

    @Override
    public Map<String, ArrayNode> getFastaSequences(ProcessController processController, String conceptAlias) {
        // TODO fetch the fastaSequences from fuseki
        return new HashMap<>();
    }

    /**
     * copy over the fasta sequences <urn:sequence> from the previousGraph to the newGraph. Only copy the sequences if
     * there is a previous graph and where the ark: exists in the new graph
     */
    private void copySequences(ProcessController processController, String fusekiService, boolean newDataset) {
        if (newDataset) {
            String currentGraph = getCurrentGraph(processController);
            String previousGraph = getPreviousGraph(processController);
            Entity entityRoot = FastaUtils.getEntityRoot(processController.getMapping(), FastaSequence.SEQUENCE_URI);

            // if rootEntity is null, then there is no SEQUENCE_URI for this project
            if (entityRoot != null) {
                String insert = "INSERT { GRAPH <" + currentGraph + "> { ?s <" + FastaSequence.SEQUENCE_URI + "> ?o }} WHERE " +
                        "{ GRAPH <" + currentGraph + "> { ?s a <" + entityRoot.getConceptURI() + "> } . " +
                        "GRAPH <" + previousGraph + "> { ?s <" + FastaSequence.SEQUENCE_URI + "> ?o }}";

                UpdateRequest update = UpdateFactory.create(insert);

                UpdateProcessRemote riStore = (UpdateProcessRemote)
                        UpdateExecutionFactory.createRemote(update, fusekiService + "/update");

                riStore.execute();
            }
        }
    }

    private String getEntityRootIdentifier(ProcessController processController) {
        Entity rootEntity = FastaUtils.getEntityRoot(processController.getMapping(), FastaSequence.SEQUENCE_URI);

        if (rootEntity == null) {
            throw new ServerErrorException("Server Error", "No entity was found containing a urn:sequence attribute");
        }

        // get the bcidRoot so we can parse the identifier from the fuseki db
        Bcid bcid = expeditionService.getEntityBcid(
                processController.getExpeditionCode(),
                processController.getProjectId(),
                rootEntity.getConceptAlias()
        );

        return String.valueOf(bcid.getIdentifier());
    }

    private Bcid getCurrentBcid(ProcessController processController) {
        List<Bcid> datasetBcids = bcidService.getFimsMetadataDatasets(
                processController.getProjectId(),
                processController.getExpeditionCode()
        );

        if (!datasetBcids.isEmpty()) {
            return datasetBcids.get(0);
        } else {
            return null;
        }
    }

    private String getCurrentGraph(ProcessController processController) {
        List<Bcid> datasetBcids = bcidService.getFimsMetadataDatasets(
                processController.getProjectId(),
                processController.getExpeditionCode()
        );

        if (!datasetBcids.isEmpty()) {
            return datasetBcids.get(0).getGraph();
        } else {
            return null;
        }
    }

    private String getPreviousGraph(ProcessController processController) {
        List<Bcid> datasetBcids = bcidService.getFimsMetadataDatasets(
                processController.getProjectId(),
                processController.getExpeditionCode()
        );

        if (datasetBcids.size() > 1) {
            return datasetBcids.get(1).getGraph();
        } else {
            return null;
        }
    }
}

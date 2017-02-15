package biocode.fims.ncbi.entrez;

import biocode.fims.ncbi.models.BioSample;
import biocode.fims.ncbi.models.SraExperimentPackage;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author rjewing
 */
public class BioSampleRepository {
    private final EntrezApiService entrez;

    public BioSampleRepository(EntrezApiService entrez) {
        Assert.notNull(entrez);
        this.entrez = entrez;
    }

    public List<BioSample> getBioSamples(List<String> bcids) {
        if (bcids.isEmpty()) {
            return new ArrayList<>();
        }

        return getBioSamplesFromEntrezApi(bcids);
    }

    private List<BioSample> getBioSamplesFromEntrezApi(List<String> bcids) {
        List<BioSample> bioSamples = getBioSamplesForBcids(bcids);

        if (bioSamples.isEmpty()) {
            return bioSamples;
        }

        List<SraExperimentPackage> experiments = getExperimentsFromEntrezApi(bioSamples);

        return filterBioSamplesWithExperiment(bioSamples, experiments);
    }

    private List<BioSample> getBioSamplesForBcids(List<String> bcids) {
        List<String> bioSampleIds = entrez.getBioSampleIdsWithBcidAttribute();
        if (bioSampleIds.isEmpty()) {
            return new ArrayList<>();
        }

        List<BioSample> bioSamples = entrez.getBioSamplesFromIds(bioSampleIds);

        return filterBioSamplesMatchingBcids(bioSamples, bcids);

    }

    private List<BioSample> filterBioSamplesMatchingBcids(List<BioSample> bioSamples, List<String> bcids) {
        List<BioSample> filteredBioSamples = new ArrayList<>();

        for (BioSample bioSample : bioSamples) {
            if (bcids.contains(bioSample.getBcid())) {
                filteredBioSamples.add(bioSample);
            }
        }

        return filteredBioSamples;
    }

    private List<SraExperimentPackage> getExperimentsFromEntrezApi(List<BioSample> bioSamples) {
        Set<String> bioProjectIds = getBioProjectIds(bioSamples);

        return getExperimentsForBioProjects(bioProjectIds);
    }

    private Set<String> getBioProjectIds(List<BioSample> bioSamples) {
        Set<String> bioProjectIds = new HashSet<>();

        for (BioSample bioSample : bioSamples) {
            bioProjectIds.add(bioSample.getBioProjectId());
        }
        return bioProjectIds;
    }

    private List<SraExperimentPackage> getExperimentsForBioProjects(Set<String> bioProjectIds) {
        List<SraExperimentPackage> experimentPackages = new ArrayList<>();

        for (String id : bioProjectIds) {
            experimentPackages.addAll(getExperimentForBioProject(id));
        }

        return experimentPackages;
    }

    private List<SraExperimentPackage> getExperimentForBioProject(String bioProjectId) {
        List<String> sraExperimentPackageIds = entrez.getSraExperimentPackageIds(bioProjectId);
        return entrez.getSraExperimentPackagesFromIds(sraExperimentPackageIds);
    }

    private List<BioSample> filterBioSamplesWithExperiment(List<BioSample> bioSamples, List<SraExperimentPackage> experiments) {
        List<BioSample> bioSamplesWithExperiments = new ArrayList<>();

        for (SraExperimentPackage experiment : experiments) {
            for (BioSample bioSample : bioSamples) {
                if (experiment.hasBioSampleAccession(bioSample.getAccession())) {
                    bioSample.setSraExperimentPackage(experiment);
                    bioSamplesWithExperiments.add(bioSample);
                    break;
                }
            }
        }

        return bioSamplesWithExperiments;
    }
}

package biocode.fims.ncbi.entrez;

import biocode.fims.ncbi.models.BioSample;
import biocode.fims.ncbi.models.SraExperimentPackage;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author rjewing
 */
public class BioSampleRepositoryTest {
    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void constructor_fails_fast_when_given_null_service_argument() {
        exception.expect(IllegalArgumentException.class);
        new BioSampleRepository(null);
    }

    @Test
    public void getBioSamples_returns_empty_list_when_given_empty_bcids_list() {
        BioSampleRepository repository = getRepository(new MockApiServiceBuilder().build());

        List<BioSample> bioSamples = repository.getBioSamples(Collections.emptyList());
        assertTrue(bioSamples.isEmpty());
    }

    @Test
    public void getBioSamples_returns_empty_list_when_no_bioSample_found() {
        EntrezApiService apiService = new MockApiServiceBuilder().build();
        BioSampleRepository repository = getRepository(apiService);

        List<BioSample> bioSamples = repository.getBioSamples(Arrays.asList("ark:/99999/r2"));
        assertTrue("empty list returned if no bioSamples found", bioSamples.isEmpty());
    }

    @Test
    public void getBioSamples_returns_empty_list_when_no_bioSample_found_with_matching_bcid() {
        BioSample bioSample1 = getBioSample1();
        BioSample bioSample2 = getBioSample2();

        EntrezApiService apiService = new MockApiServiceBuilder(bioSample2).build();
        BioSampleRepository repository = getRepository(apiService);

        List<BioSample> bioSamples = repository.getBioSamples(Arrays.asList(bioSample1.getBcid()));
        assertTrue("no bioSamples returned if bcid not in bcids list", bioSamples.isEmpty());
    }

    @Test
    public void getBioSamples_returns_bioSamples_in_bcids_list_when_bioSamples_in_same_bioProject_and_bioSamples_have_experiment_package() {
        BioSample bioSample1 = getBioSample1();
        BioSample bioSample2 = getBioSample2();

        EntrezApiService apiService = new MockApiServiceBuilder(bioSample1, bioSample2)
                .experimentPackages(bioSample1.getBioProjectId(), getBioSample1ExperimentPackage(), getBioSample2ExperimentPackage())
                .build();

        BioSampleRepository repository = getRepository(apiService);

        List<String> bcids = Arrays.asList(bioSample1.getBcid(), bioSample2.getBcid());
        List<BioSample> bioSamples = repository.getBioSamples(bcids);

        assertTrue("bioSample not found in bcids list", bcids.contains(bioSamples.get(0).getBcid()));
        assertTrue("bioSample not found in bcids list", bcids.contains(bioSamples.get(1).getBcid()));
        assertTrue("bioSample missing experiment package", bioSamples.get(0).getSraExperimentPackage() != null);
        assertTrue("bioSample missing experiment package", bioSamples.get(1).getSraExperimentPackage() != null);
    }

    @Test
    public void getBioSamples_returns_bioSamples_in_bcids_list_when_bioSamples_in_different_bioProject_and_bioSamples_have_experiment_package() {
        BioSample bioSample1 = getBioSample1();
        BioSample bioSample3 = getBioSample3();

        EntrezApiService apiService = new MockApiServiceBuilder(bioSample1, bioSample3)
                .experimentPackages(bioSample1.getBioProjectId(), getBioSample1ExperimentPackage())
                .experimentPackages(bioSample3.getBioProjectId(), getBioSample3ExperimentPackage())
                .build();

        BioSampleRepository repository = getRepository(apiService);

        List<String> bcids = Arrays.asList(bioSample1.getBcid(), bioSample3.getBcid());
        List<BioSample> bioSamples = repository.getBioSamples(bcids);

        assertTrue("bioSample not found in bcids list", bcids.contains(bioSamples.get(0).getBcid()));
        assertTrue("bioSample not found in bcids list", bcids.contains(bioSamples.get(1).getBcid()));
        assertTrue("bioSample missing experiment package", bioSamples.get(0).getSraExperimentPackage() != null);
        assertTrue("bioSample missing experiment package", bioSamples.get(1).getSraExperimentPackage() != null);
    }

    @Test
    public void getBioSamples_returns_empty_list_when_bioSample_does_not_have_experiment_package() {
        BioSample bioSample1 = getBioSample1();

        EntrezApiService apiService = new MockApiServiceBuilder(bioSample1)
                .experimentPackages(bioSample1.getBioProjectId())
                .build();

        BioSampleRepository repository = getRepository(apiService);

        List<String> bcids = Arrays.asList("ark:/99999/r2");
        List<BioSample> bioSamples = repository.getBioSamples(bcids);

        assertTrue("no bioSamples returned if bioSample missing experiment package", bioSamples.isEmpty());
    }

    @Test
    public void getBioSamples_returns_bioSamples_with_the_correct_experiment_package() {
        BioSample bioSample1 = getBioSample1();
        BioSample bioSample2 = getBioSample2();
        SraExperimentPackage experimentPackage1 = getBioSample1ExperimentPackage();
        SraExperimentPackage experimentPackage2 = getBioSample2ExperimentPackage();

        EntrezApiService apiService = new MockApiServiceBuilder(bioSample1, bioSample2)
                .experimentPackages(bioSample1.getBioProjectId(), experimentPackage1, experimentPackage2)
                .build();

        BioSampleRepository repository = getRepository(apiService);

        List<String> bcids = Arrays.asList(bioSample1.getBcid(), bioSample2.getBcid());
        List<BioSample> bioSamples = repository.getBioSamples(bcids);

        for (BioSample bioSample: bioSamples) {
            if (bioSample.equals(bioSample1)) {
                assertTrue("wrong sra experiment package for bioSample", bioSample.getSraExperimentPackage().equals( experimentPackage1));
            } else {
                assertTrue("wrong sra experiment package for bioSample", bioSample.getSraExperimentPackage().equals( experimentPackage2));
            }
        }
    }

    private static BioSample getBioSample1() {
        return new BioSample(
                "bioSample1",
                "SAMNB1",
                "ark:/99999/r2",
                "bioProject1",
                "PRJN1"
        );
    }

    private static BioSample getBioSample2() {
        return new BioSample(
                "bioSample2",
                "SAMNB2",
                "ark:/99999/s2",
                "bioProject1",
                "PRJN1"
        );
    }

    private static BioSample getBioSample3() {
        return new BioSample(
                "bioSample3",
                "SAMNB3",
                "ark:/99999/t2",
                "bioProject2",
                "PRJN2"
        );
    }

    private SraExperimentPackage getBioSample1ExperimentPackage() {
        return new SraExperimentPackage(
                "SRP1",
                "SRX1",
                "SAMNB1",
                Arrays.asList("SRR1")
        );
    }

    private SraExperimentPackage getBioSample2ExperimentPackage() {
        return new SraExperimentPackage(
                "SRP2",
                "SRX2",
                "SAMNB2",
                Arrays.asList("SRR2")
        );
    }

    private SraExperimentPackage getBioSample3ExperimentPackage() {
        return new SraExperimentPackage(
                "SRP3",
                "SRX3",
                "SAMNB3",
                Arrays.asList("SRR3")
        );
    }

    private static BioSampleRepository getRepository(EntrezApiService apiService) {
        return new BioSampleRepository(apiService);
    }


    private class MockApiServiceBuilder {
        private final List<String> bioSampleIds;
        private final List<BioSample> bioSamples;
        private Map<String, List<SraExperimentPackage>> experimentPackages;

        MockApiServiceBuilder(BioSample... bioSamples) {
            ArrayList<String> bioSampleIds = new ArrayList<>();
            ArrayList<BioSample> bioSamplesList = new ArrayList<>();

            for (BioSample bs : bioSamples) {
                bioSampleIds.add(bs.getId());
                bioSamplesList.add(bs);
            }

            this.bioSampleIds = bioSampleIds;
            this.bioSamples = bioSamplesList;
            this.experimentPackages = new HashMap<>();
        }

        MockApiServiceBuilder experimentPackages(String bioProjectId, SraExperimentPackage... experimentPackages) {
            this.experimentPackages.put(bioProjectId, Arrays.asList(experimentPackages));

            return this;
        }

        EntrezApiService build() {
            EntrezApiService apiService = mock(EntrezApiService.class);

            when(apiService.getBioSampleIdsWithBcidAttribute()).thenReturn(bioSampleIds);
            when(apiService.getBioSamplesFromIds(bioSampleIds)).thenReturn(bioSamples);

            if (_addExperiemntPackages()) {
                for (Map.Entry<String, List<SraExperimentPackage>> entry: experimentPackages.entrySet()) {

                    _setupExperimentPackageMock(apiService, entry.getKey(), entry.getValue());

                }
            }
            return apiService;
        }

        private void _setupExperimentPackageMock(EntrezApiService apiService, String bioProjectId, List<SraExperimentPackage> experiments) {
            ArrayList<String> experimentIds = new ArrayList<>();

            for (int i=0; i < experiments.size(); i++) {
                experimentIds.add(bioProjectId + "_" + i);
            }

            when(apiService.getSraExperimentPackageIds(bioProjectId)).thenReturn(experimentIds);
            when(apiService.getSraExperimentPackagesFromIds(experimentIds)).thenReturn(experiments);
        }

        private boolean _addExperiemntPackages() {
            return !experimentPackages.isEmpty();
        }
    }
}
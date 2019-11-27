package biocode.fims.rest.models;

import biocode.fims.models.Expedition;
import biocode.fims.models.Project;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;

import java.util.Date;
import java.util.List;

/**
 * @author rjewing
 */
public class SraUploadMetadata {

    public enum BioSampleType {
        // https://www.ncbi.nlm.nih.gov/biosample/docs/packages/
        ANIMAL("Model.organism.animal.1.0"),
        INVERTEBRATE("Invertebrate.1.0"),
        PLANT("Plant.1.0"),
        ENVIRONMENTAL("Metagenome.environmental.1.0"),
        //        GENOME,
        VIRUS("Virus.1.0"),
        //        BETALACTAMASE("Beta-lactamase.1.0"),
//        PATHOGEN("Pathogen.cl.1.0"),
        MICROBE("Microbe.1.0"),
        HUMAN("Human.1.0");

        private final String name;

        BioSampleType(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    @JsonSetter(nulls = Nulls.FAIL)
    public String expeditionCode;
    @JsonSetter(nulls = Nulls.FAIL)
    public Integer projectId;
    @JsonIgnore
    public Project project;
    @JsonIgnore
    public Expedition expedition;

    public String bioProjectAccession;
    public String bioProjectTitle;
    public String bioProjectDescription;

    public Date holdDate;

    @JsonSetter(nulls = Nulls.FAIL)
    public BioSampleType bioSampleType;
    @JsonSetter(nulls = Nulls.FAIL, contentNulls = Nulls.FAIL)
    public List<String> bioSamples;
}

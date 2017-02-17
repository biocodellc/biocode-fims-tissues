package biocode.fims.fastq;

import biocode.fims.fimsExceptions.FimsRuntimeException;
import biocode.fims.ncbi.models.BioSample;
import org.apache.commons.lang.ArrayUtils;

import java.io.Serializable;
import java.util.List;

/**
 * FastqMetadata domain object.
 */
public class FastqMetadata implements Serializable {

    public static String PAIRED_LAYOUT = "paired";
    public static String SINGLE_LAYOUT = "single";

    private String libraryStrategy;
    private String librarySource;
    private String librarySelection;
    private String libraryLayout;
    private String platform;
    private String instrumentModel;
    private String designDescription;
    private List<String> filenames;
    private BioSample bioSample;


    public static class FASTQMetadataBuilder {
        private String libraryStrategy;
        private String librarySource;
        private String librarySelection;
        private String libraryLayout;
        private String platform;
        private String instrumentModel;
        private String designDescription;
        private List<String> filenames;

        public FASTQMetadataBuilder() {
        }
        
        public FASTQMetadataBuilder libraryStrategy(String val) {
            this.libraryStrategy = val;
            return this;
        }

        public FASTQMetadataBuilder librarySource(String val) {
            this.librarySource = val;
            return this;
        }

        public FASTQMetadataBuilder librarySelection(String val) {
            this.librarySelection = val;
            return this;
        }

        public FASTQMetadataBuilder libraryLayout(String val) {
            this.libraryLayout = val;
            return this;
        }

        public FASTQMetadataBuilder platform(String val) {
            this.platform = val;
            return this;
        }

        public FASTQMetadataBuilder instrumentModel(String val) {
            this.instrumentModel = val;
            return this;
        }

        public FASTQMetadataBuilder designDescription(String val) {
            this.designDescription = val;
            return this;
        }

        public FASTQMetadataBuilder filenames(List<String> filenames) {
            this.filenames = filenames;
            return this;
        }

        private boolean validFASTQMetadata() {
            return (libraryStrategy != null && librarySource != null && librarySelection != null && filenames != null
                    && libraryLayout != null && platform != null && instrumentModel != null && designDescription != null);
        }

        public FastqMetadata build() {
            if (!validFASTQMetadata())
                throw new FimsRuntimeException("", "Trying to create an invalid expedition. " +
                        "libraryStrategy, librarySource, librarySelection, libraryLayout, platform, instrumentModel, " +
                        "and designDescription must not be null", 500);

            return new FastqMetadata(this);
        }
    }

    // needed for jackson
    private FastqMetadata() {}

    private FastqMetadata(FASTQMetadataBuilder builder) {
        this.libraryStrategy = builder.libraryStrategy;
        this.librarySource = builder.librarySource;
        this.librarySelection = builder.librarySelection;
        this.libraryLayout = builder.libraryLayout;
        this.platform = builder.platform;
        this.instrumentModel = builder.instrumentModel;
        this.designDescription = builder.designDescription;
        this.filenames = builder.filenames;
    }

    public String getLibraryStrategy() {
        return libraryStrategy;
    }

    public void setLibraryStrategy(String libraryStrategy) {
        this.libraryStrategy = libraryStrategy;
    }

    public String getLibrarySource() {
        return librarySource;
    }

    public void setLibrarySource(String librarySource) {
        this.librarySource = librarySource;
    }

    public String getLibrarySelection() {
        return librarySelection;
    }

    public void setLibrarySelection(String librarySelection) {
        this.librarySelection = librarySelection;
    }

    public String getLibraryLayout() {
        return libraryLayout;
    }

    public void setLibraryLayout(String libraryLayout) {
        this.libraryLayout = libraryLayout;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getInstrumentModel() {
        return instrumentModel;
    }

    public void setInstrumentModel(String instrumentModel) {
        this.instrumentModel = instrumentModel;
    }

    public String getDesignDescription() {
        return designDescription;
    }

    public void setDesignDescription(String designDescription) {
        this.designDescription = designDescription;
    }

    public List<String> getFilenames() {
        return filenames;
    }

    public void setFilenames(List<String> filenames) {
        this.filenames = filenames;
    }

    public BioSample getBioSample() {
        return bioSample;
    }

    public void setBioSample(BioSample bioSample) {
        this.bioSample = bioSample;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FastqMetadata)) return false;

        FastqMetadata that = (FastqMetadata) o;

        if (!getLibraryStrategy().equals(that.getLibraryStrategy())) return false;
        if (!getLibrarySource().equals(that.getLibrarySource())) return false;
        if (!getLibrarySelection().equals(that.getLibrarySelection())) return false;
        if (!getLibraryLayout().equals(that.getLibraryLayout())) return false;
        if (!getPlatform().equals(that.getPlatform())) return false;
        if (!getInstrumentModel().equals(that.getInstrumentModel())) return false;
        if (!getDesignDescription().equals(that.getDesignDescription())) return false;
        if (getFilenames() != null ? !getFilenames().equals(that.getFilenames()) : that.getFilenames() != null)
            return false;
        return getBioSample() != null ? getBioSample().equals(that.getBioSample()) : that.getBioSample() == null;
    }

    @Override
    public int hashCode() {
        int result = getLibraryStrategy().hashCode();
        result = 31 * result + getLibrarySource().hashCode();
        result = 31 * result + getLibrarySelection().hashCode();
        result = 31 * result + getLibraryLayout().hashCode();
        result = 31 * result + getPlatform().hashCode();
        result = 31 * result + getInstrumentModel().hashCode();
        result = 31 * result + getDesignDescription().hashCode();
        result = 31 * result + (getFilenames() != null ? getFilenames().hashCode() : 0);
        result = 31 * result + (getBioSample() != null ? getBioSample().hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "FastqMetadata{" +
                ", libraryStrategy='" + libraryStrategy + '\'' +
                ", librarySource='" + librarySource + '\'' +
                ", librarySelection='" + librarySelection + '\'' +
                ", libraryLayout='" + libraryLayout + '\'' +
                ", platform='" + platform + '\'' +
                ", instrumentModel='" + instrumentModel + '\'' +
                ", designDescription='" + designDescription + '\'' +
                ", filenames='" + ArrayUtils.toString(filenames.toArray()) + '\'' +
                '}';
    }
}

package biocode.fims.serializers;

import biocode.fims.ncbi.models.SubmittableBioSample;
import biocode.fims.ncbi.models.submission.Attribute;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

/**
 * Custom JSON serializer for {@link biocode.fims.ncbi.models.SubmittableBioSample}.
 */
public class SubmittableBioSampleSerializer extends JsonSerializer<SubmittableBioSample> {
    @Override
    public void serialize(SubmittableBioSample bioSample, JsonGenerator jgen, SerializerProvider provider)
            throws IOException {
        jgen.writeStartObject();
        jgen.writeStringField("sample_name", bioSample.getSampleName());
        jgen.writeStringField("sample_title", bioSample.getSampleTitle());
        jgen.writeStringField("organism", bioSample.getOrganism());
        for (Attribute attribute: bioSample.getAttributes()) {
            jgen.writeStringField(attribute.name, attribute.value);
        }
        jgen.writeEndObject();
    }
}

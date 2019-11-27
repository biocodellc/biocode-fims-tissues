package biocode.fims.serializers;

import biocode.fims.ncbi.models.SraMetadata;
import biocode.fims.ncbi.models.submission.Attribute;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

/**
 * Custom JSON serializer for {@link SraMetadata}.
 */
public class SraMetadataSerializer extends JsonSerializer<SraMetadata> {
    @Override
    public void serialize(SraMetadata metadata, JsonGenerator jgen, SerializerProvider provider)
            throws IOException {
        jgen.writeStartObject();
        jgen.writeStringField("sample_name", metadata.get("sample_name"));
        jgen.writeStringField("library_id", metadata.get("library_id"));
        jgen.writeStringField("filename", metadata.get("filename"));
        jgen.writeStringField("filename2", metadata.get("filename2"));
        for (Attribute attribute : metadata.getAttributes()) {
            jgen.writeStringField(attribute.name, attribute.value);
        }
        jgen.writeEndObject();
    }
}

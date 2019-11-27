package biocode.fims.serializers;

import biocode.fims.ncbi.models.submission.Attribute;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

/**
 * Custom JSON serializer for {@link biocode.fims.ncbi.models.submission.Attribute}.
 */
public class AttributeSerializer extends JsonSerializer<Attribute> {
    @Override
    public void serialize(Attribute attribute, JsonGenerator jgen, SerializerProvider provider)
            throws IOException {
//        jgen.writeStartObject();
        jgen.writeStringField(attribute.name, attribute.value);
//        jgen.writeEndObject();
    }
}

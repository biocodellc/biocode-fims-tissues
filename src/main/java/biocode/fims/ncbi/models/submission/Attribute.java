package biocode.fims.ncbi.models.submission;

import biocode.fims.serializers.AttributeSerializer;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

/**
 * @author rjewing
 */
@JsonSerialize(using = AttributeSerializer.class)
public class Attribute {
    @XmlAttribute(name = "attribute_name")
    public String name;
    @XmlValue
    public String value;

    Attribute() {
    }

    public Attribute(String name, String value) {
        this.name = name;
        this.value = value;
    }
}

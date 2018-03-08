package biocode.fims.fastq;

import biocode.fims.models.dataTypes.JacksonUtil;
import biocode.fims.ncbi.models.BioSample;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author rjewing
 */
public class FastqRecordRowMapper implements RowMapper<FastqRecord> {
    private final static JavaType TYPE;

    static {
        TYPE = TypeFactory.defaultInstance().constructMapType(HashMap.class, String.class, String.class);
    }

    @SuppressWarnings("unchecked")
    @Override
    public FastqRecord mapRow(ResultSet rs, int rowNum) throws SQLException {
        String data = rs.getString("data");

        try {
            Map<String, String> properties = (Map<String, String>) JacksonUtil.fromString(data, TYPE);
            BioSample bioSample = JacksonUtil.fromString(properties.remove("bioSample"), BioSample.class);
            List<String> filenames = JacksonUtil.fromString(properties.remove("filenames"), List.class);

            FastqRecord r = new FastqRecord(properties, filenames, false);
            r.setBioSample(bioSample);

            return r;
        } catch (Exception e) {
            throw new SQLException(e);
        }

    }
}

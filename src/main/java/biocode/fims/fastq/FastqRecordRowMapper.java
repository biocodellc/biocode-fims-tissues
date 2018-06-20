package biocode.fims.fastq;

import biocode.fims.models.dataTypes.JacksonUtil;
import biocode.fims.models.records.FimsRowMapper;
import biocode.fims.ncbi.models.BioSample;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author rjewing
 */
public class FastqRecordRowMapper implements FimsRowMapper<FastqRecord> {
    private final static JavaType TYPE;

    static {
        TYPE = TypeFactory.defaultInstance().constructMapType(HashMap.class, String.class, String.class);
    }

    @SuppressWarnings("unchecked")
    @Override
    public FastqRecord mapRow(ResultSet rs, int rowNum, String dataLabel) throws SQLException {
        String data = rs.getString(dataLabel);
        if (data == null) return null;

        try {
            Map<String, String> properties = (Map<String, String>) JacksonUtil.fromString(data, TYPE);
            BioSample bioSample = null;
            if (properties.get(FastqProps.BIOSAMPLE.value()) != null) {
                bioSample = JacksonUtil.fromString(properties.remove(FastqProps.BIOSAMPLE.value()), BioSample.class);
            }
            List<String> filenames = JacksonUtil.fromString(properties.remove("filenames"), List.class);

            FastqRecord r = new FastqRecord(properties, filenames, false);
            if (bioSample != null)
                r.setBioSample(bioSample);

            return r;
        } catch (Exception e) {
            throw new SQLException(e);
        }
    }

    @Override
    public FastqRecord mapRow(ResultSet rs, int rowNum) throws SQLException {
        return mapRow(rs, rowNum, "data");
    }
}

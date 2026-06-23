package com.citrus.knowledge.handler;

import com.pgvector.PGvector;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.sql.*;

/**
 * MyBatis-Plus TypeHandler for pgvector vector(512) type.
 * <p>
 * Maps between Java {@code float[]} and PostgreSQL {@code vector} type
 * using the pgvector JDBC driver's {@link PGvector} class.
 */
@MappedJdbcTypes(JdbcType.OTHER)
@MappedTypes(float[].class)
public class VectorTypeHandler extends BaseTypeHandler<float[]> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, float[] parameter, JdbcType jdbcType)
            throws SQLException {
        ps.setObject(i, new PGvector(parameter));
    }

    @Override
    public float[] getNullableResult(ResultSet rs, String columnName) throws SQLException {
        PGvector vec = (PGvector) rs.getObject(columnName);
        return vec != null ? vec.toArray() : null;
    }

    @Override
    public float[] getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        PGvector vec = (PGvector) rs.getObject(columnIndex);
        return vec != null ? vec.toArray() : null;
    }

    @Override
    public float[] getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        PGvector vec = (PGvector) cs.getObject(columnIndex);
        return vec != null ? vec.toArray() : null;
    }
}

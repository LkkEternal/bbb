package com.citrus.orchard.handler;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.sql.*;

/**
 * PostGIS GEOGRAPHY(POINT) 类型处理器
 * 将 Java String ("经度,纬度") 与 PostgreSQL GEOGRAPHY(POINT) 类型互相转换
 */
@MappedTypes(String.class)
@MappedJdbcTypes(JdbcType.OTHER)
public class GeographyTypeHandler extends BaseTypeHandler<String> {

    /**
     * 插入时：将 "经度,纬度" 字符串转换为 GEOGRAPHY(POINT)
     * 使用 PostGIS 函数 ST_SetSRID(ST_MakePoint(longitude, latitude), 4326)
     */
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, String parameter, JdbcType jdbcType) throws SQLException {
        if (parameter == null || parameter.isEmpty()) {
            ps.setNull(i, Types.OTHER);
            return;
        }
        String[] parts = parameter.split(",");
        if (parts.length != 2) {
            ps.setNull(i, Types.OTHER);
            return;
        }
        try {
            double longitude = Double.parseDouble(parts[0].trim());
            double latitude = Double.parseDouble(parts[1].trim());
            // 使用 PGobject 设置 GEOGRAPHY 类型
            Object geometry = createGeometryObject(longitude, latitude);
            ps.setObject(i, geometry);
        } catch (NumberFormatException e) {
            ps.setNull(i, Types.OTHER);
        }
    }

    /**
     * 读取时：将 GEOGRAPHY(POINT) 转换为 "经度,纬度" 字符串
     */
    @Override
    public String getNullableResult(ResultSet rs, String columnName) throws SQLException {
        Object obj = rs.getObject(columnName);
        return extractLocation(obj);
    }

    @Override
    public String getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        Object obj = rs.getObject(columnIndex);
        return extractLocation(obj);
    }

    @Override
    public String getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        Object obj = cs.getObject(columnIndex);
        return extractLocation(obj);
    }

    /**
     * 从数据库对象中提取经纬度字符串
     */
    private String extractLocation(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            // 如果是 PGgeometry 对象，转换为 WKT 格式
            String wkt = obj.toString();
            // WKT 格式: "POINT(114.93 25.38)" 或 "SRID=4326;POINT(114.93 25.38)"
            return parseWktToLocation(wkt);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 从 WKT 格式解析经纬度
     * 输入: "POINT(114.93 25.38)" 或 "SRID=4326;POINT(114.93 25.38)"
     * 输出: "114.93,25.38"
     */
    private String parseWktToLocation(String wkt) {
        if (wkt == null || !wkt.contains("POINT")) {
            return null;
        }
        try {
            // 提取 POINT(...) 中的内容
            int start = wkt.indexOf('(');
            int end = wkt.indexOf(')');
            if (start < 0 || end < 0) {
                return null;
            }
            String coords = wkt.substring(start + 1, end).trim();
            String[] parts = coords.split("\\s+");
            if (parts.length == 2) {
                return parts[0] + "," + parts[1];
            }
        } catch (Exception e) {
            // 解析失败
        }
        return null;
    }

    /**
     * 创建 PostGIS 几何对象
     * 使用 PostgreSQL 的 ST_SetSRID(ST_MakePoint(), 4326) 函数
     */
    private Object createGeometryObject(double longitude, double latitude) {
        // 使用 PGobject 来传递 GEOGRAPHY 类型
        try {
            Class<?> pgObjClass = Class.forName("org.postgresql.util.PGobject");
            Object pgObject = pgObjClass.newInstance();
            // 设置 type 为 geography
            java.lang.reflect.Method setType = pgObjClass.getMethod("setType", String.class);
            setType.invoke(pgObject, "geography");
            // 设置 value 为 WKT 格式
            java.lang.reflect.Method setValue = pgObjClass.getMethod("setValue", String.class);
            String wkt = String.format("SRID=4326;POINT(%f %f)", longitude, latitude);
            setValue.invoke(pgObject, wkt);
            return pgObject;
        } catch (Exception e) {
            // 如果 PGobject 不可用，返回 null
            return null;
        }
    }
}

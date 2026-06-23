package com.citrus.orchard.handler;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
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
     * PostgreSQL JDBC 驱动对 geography 类型返回 PGobject（toString() = WKB十六进制字符串）
     */
    @Override
    public String getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return extractLocation(rs.getObject(columnName));
    }

    @Override
    public String getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return extractLocation(rs.getObject(columnIndex));
    }

    @Override
    public String getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return extractLocation(cs.getObject(columnIndex));
    }

    /**
     * 从数据库对象中提取经纬度字符串
     * PGobject.toString() 返回 WKB 十六进制字符串
     */
    private String extractLocation(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            String hex = obj.toString();
            byte[] bytes = hexToBytes(hex);
            return parseWkbToLocation(bytes);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 十六进制字符串转字节数组
     */
    private byte[] hexToBytes(String hex) {
        if (hex == null || hex.length() % 2 != 0) {
            return null;
        }
        byte[] bytes = new byte[hex.length() / 2];
        for (int i = 0; i < hex.length(); i += 2) {
            bytes[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return bytes;
    }

    /**
     * 从 WKB (Well-Known Binary) 格式解析经纬度
     * PostGIS 返回的是 EWKB 格式，包含 SRID
     * @param bytes WKB 字节数组
     * @return "经度,纬度" 格式字符串
     */
    private String parseWkbToLocation(byte[] bytes) {
        if (bytes == null || bytes.length < 21) {
            return null;
        }
        try {
            ByteBuffer buffer = ByteBuffer.wrap(bytes);
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            
            // 读取字节序（1字节）：0=大端，1=小端
            byte byteOrder = buffer.get();
            if (byteOrder == 0) {
                buffer.order(ByteOrder.BIG_ENDIAN);
            }
            
            // 读取几何类型（4字节）
            int geometryType = buffer.getInt();
            
            // 检查是否为 POINT 类型 (TYPE_POINT = 1)
            // 如果有 SRID 标志位 (0x20000000)，需要跳过 SRID
            boolean hasSrid = (geometryType & 0x20000000) != 0;
            int pointType = geometryType & 0xFF; // 去掉标志位
            
            if (pointType != 1) {
                return null; // 不是 POINT 类型
            }
            
            // 如果有 SRID，跳过它（4字节）
            if (hasSrid) {
                buffer.getInt();
            }
            
            // 读取经度（X坐标，8字节）
            double longitude = buffer.getDouble();
            
            // 读取纬度（Y坐标，8字节）
            double latitude = buffer.getDouble();
            
            // 验证经纬度范围
            if (longitude < -180 || longitude > 180 || latitude < -90 || latitude > 90) {
                return null;
            }
            
            return longitude + "," + latitude;
        } catch (Exception e) {
            // 解析失败
            return null;
        }
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

package com.citrus.common.amap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest(classes = AmapClientTest.TestConfig.class)
public class AmapClientTest {

    @SpringBootApplication
    static class TestConfig {
    }

    @Autowired
    private AmapClient amapClient;

    private final ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    @Test
    public void testRegeo() throws JsonProcessingException {
        log.info("===== 测试逆地理编码（经纬度转地址）=====");
        
        // 测试坐标：江西省赣州市信丰县
        double longitude = 114.93;
        double latitude = 25.38;
        
        log.info("输入坐标: 经度={}, 纬度={}", longitude, latitude);
        
        AmapClient.GeocodeResult result = amapClient.regeo(longitude, latitude);
        
        if (result.getRegeocode() != null) {
            AmapClient.RegeoCode regeoCode = result.getRegeocode();
            log.info("格式化地址: {}", regeoCode.getFormattedAddress());
            
            if (regeoCode.getAddressComponent() != null) {
                AmapClient.AddressComponent comp = regeoCode.getAddressComponent();
                log.info("省份: {}", comp.getProvince());
                log.info("城市: {}", comp.getCity());
                log.info("区县: {}", comp.getDistrict());
                log.info("乡镇: {}", comp.getTownship());
                log.info("行政区划代码: {}", comp.getAdcode());
            }
            
            if (regeoCode.getPois() != null && !regeoCode.getPois().isEmpty()) {
                log.info("附近POI:");
                for (int i = 0; i < Math.min(3, regeoCode.getPois().size()); i++) {
                    AmapClient.Poi poi = regeoCode.getPois().get(i);
                    log.info("  {} - {} ({}米)", poi.getName(), poi.getType(), poi.getDistance());
                }
            }
        }
        
        log.info("完整响应:\n{}", objectMapper.writeValueAsString(result));
    }

    @Test
    public void testGeo() throws JsonProcessingException {
        log.info("===== 测试地理编码（地址转经纬度）=====");
        
        String address = "江西省赣州市信丰县嘉定镇马路坑村";
        log.info("输入地址: {}", address);
        
        AmapClient.GeoResult result = amapClient.geo(address);
        
        if (result.getGeocodes() != null && !result.getGeocodes().isEmpty()) {
            AmapClient.Geocode geocode = result.getGeocodes().get(0);
            log.info("格式化地址: {}", geocode.getFormattedAddress());
            log.info("经纬度: {}", geocode.getLocation());
            log.info("省份: {}", geocode.getProvince());
            log.info("城市: {}", geocode.getCity());
            log.info("区县: {}", geocode.getDistrict());
            log.info("行政区划代码: {}", geocode.getAdcode());
        }
        
        log.info("完整响应:\n{}", objectMapper.writeValueAsString(result));
    }

    @Test
    public void testIpLocation() throws JsonProcessingException {
        log.info("===== 测试IP定位 =====");
        
        String ip = "114.252.88.178";
        log.info("输入IP: {}", ip);
        
        AmapClient.IpResult result = amapClient.ip(ip);
        
        log.info("省份: {}", result.getProvince());
        log.info("城市: {}", result.getCity());
        log.info("行政区划代码: {}", result.getAdcode());
        log.info("矩形范围: {}", result.getRectangle());
        
        log.info("完整响应:\n{}", objectMapper.writeValueAsString(result));
    }
}

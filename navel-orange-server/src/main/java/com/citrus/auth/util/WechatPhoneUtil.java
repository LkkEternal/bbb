package com.citrus.auth.util;

import com.citrus.common.exception.BizException;
import com.citrus.common.result.ResultCode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Slf4j
public class WechatPhoneUtil {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 解密微信手机号
     * @param sessionKey  jscode2session返回的session_key（base64）
     * @param encryptedData wx.getPhoneNumber返回的encryptedData（base64）
     * @param iv            wx.getPhoneNumber返回的iv（base64）
     * @return 手机号
     */
    public static String decryptPhoneNumber(String sessionKey, String encryptedData, String iv) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(sessionKey);
            byte[] dataBytes = Base64.getDecoder().decode(encryptedData);
            byte[] ivBytes = Base64.getDecoder().decode(iv);

            SecretKeySpec key = new SecretKeySpec(keyBytes, "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, key, ivSpec);
            byte[] decrypted = cipher.doFinal(dataBytes);
            String json = new String(decrypted, StandardCharsets.UTF_8);
            JsonNode jsonNode = objectMapper.readTree(json);

            if (!jsonNode.has("phoneNumber")) {
                log.error("微信手机号解密结果缺少phoneNumber: {}", json);
                throw new BizException(ResultCode.WECHAT_LOGIN_FAILED);
            }

            return jsonNode.get("phoneNumber").asText();
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            log.error("微信手机号解密失败: {}", e.getMessage(), e);
            throw new BizException(ResultCode.WECHAT_LOGIN_FAILED);
        }
    }
}

package com.wl.core.encrypt.props;

import com.wl.core.encrypt.enums.EncryptType;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 *
 * @author 南顾北衫
 * @email 17674641491@163.com
 */
@ConfigurationProperties(prefix = "encrypt")
public class EncryptProperties {
    /**
     * 加密类型 AES BASE64 CUSTOM RSA
     */
    private EncryptType type;

    /**
     * 签名秘钥 使用@SignEncrypt需要 前端请求来的时候加入
     * secret=xxxxx和请求参数一起进行md5
     */
    private String signSecret;

    /**
     * AES 加密需要，128bit（长度16字节）
     */
    private String secret;

    /**
     * 公钥
     */
    private String publicKey;

    /**
     * 私钥
     */
    private String privateKey;

    /**
     * debug为true模式下@SeparateEncrypt不会生效
     */
    private Boolean debug;

    /**
     * 加密过滤器 order属性
     */
    private Integer order=0;

    public EncryptType getType() {
        return type;
    }

    public void setType(EncryptType type) {
        this.type = type;
    }

    public String getSignSecret() {
        return signSecret;
    }

    public void setSignSecret(String signSecret) {
        this.signSecret = signSecret;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public Boolean getDebug() {
        return debug;
    }

    public void setDebug(Boolean debug) {
        this.debug = debug;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }
}

package com.caoim.pushserver.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "push")
public class PushProperties {

    /** 内部调用令牌，请求头 X-Internal-Token */
    private String internalToken = "";

    /** log、webhook 或 umeng */
    private String delivery = "log";

    /** delivery=webhook 时的接收地址 */
    private String webhookUrl = "";

    private Umeng umeng = new Umeng();

    @Data
    public static class Umeng {
        private String androidAppKey = "";
        private String androidMasterSecret = "";
        private String iosAppKey = "";
        private String iosMasterSecret = "";
        /** iOS 开发证书用 false，上架包的生产证书用 true。单播不受测试设备名单限制。 */
        private boolean productionMode = false;
        private String channelActivity = "com.umeng.message.UmengOfflineMessageActivity";
        private String mainActivity = "com.clb.caoim.MainActivity";
        private String huaweiCategory = "IM";
        private String vivoCategory = "IM";
    }
}

package com.github.loadup.gateway.facade.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.HashMap;

/**
 * Gateway配置属性
 */
@Data
@Component
@ConfigurationProperties(prefix = "loadup.gateway")
public class GatewayProperties {
    private boolean enabled = true;
    private int routeRefreshInterval = 30;
    private int templateCacheSize = 1000;
    private long defaultTimeout = 30000;
    private int defaultRetryCount = 3;
    private Storage storage = new Storage();
    private Map<String, PluginProperties> plugins = new HashMap<>();
    private ResponseProperties response = new ResponseProperties();

    @Data
    public static class Storage {
        private String type = "FILE";
        private String filePath = "./gateway-config";
        private Database database = new Database();
    }
    @Data
    public static class Database {
        private String url;
        private String username;
        private String password;
        private String driverClassName = "com.mysql.cj.jdbc.Driver";
    }
    @Data
    public static class PluginProperties {
        private boolean enabled = true;
        private int priority = 100;
        private Map<String, Object> config = new HashMap<>();
    }
    @Data
    public static class ResponseProperties {
        private boolean wrap = true;
    }
}


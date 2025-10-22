package com.github.loadup.gateway.facade.config;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Gateway 配置属性（与 application.yml 中的 loadup.gateway 对应）
 */
@Data
@Component
@ConfigurationProperties(prefix = "loadup.gateway")
public class GatewayProperties {

    /**
     * 是否启用 Gateway
     */
    private boolean enabled = true;

    /**
     * 路由刷新间隔（秒）
     */
    private int routeRefreshInterval = 5;

    /**
     * 模板缓存大小
     */
    private int templateCacheSize = 100;

    /**
     * 默认超时时间（毫秒）
     */
    private long defaultTimeout = 10000L;

    /**
     * 默认重试次数
     */
    private int defaultRetryCount = 1;

    /**
     * 存储相关配置
     */

    // Replace the generic map with a strongly-typed Plugins holder so IDEs can provide YAML autocompletion
    @NestedConfigurationProperty
    private ProxyPlugins proxyPlugins = new ProxyPlugins();

    // New storage-plugin holder: contains type and type-specific property groups
    @NestedConfigurationProperty
    private Storage storage = new Storage();

    private ResponseProperties response = new ResponseProperties();

    @Data
    public static class PluginProperties {
        private boolean enabled = true;
        private int priority = 100;
        private Map<String, Object> properties = new HashMap<>();
    }


    // New strongly-typed holder for known plugins. Field names use camelCase and will map to kebab-case in YAML
    @Data
    public static class ProxyPlugins {
        @NestedConfigurationProperty
        private Bean bean = new Bean();

        @NestedConfigurationProperty
        private Http http = new Http();

        @NestedConfigurationProperty
        private Rpc rpc = new Rpc();

    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class Bean extends PluginProperties {
        // add plugin-specific properties here if needed in future
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class Http extends PluginProperties {
        /**
         * Maximum number of connections for the HTTP proxy plugin
         */
        private int maxConnections = 100;
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class Rpc extends PluginProperties {
        // RPC-specific configuration
    }

    @Data
    public static class StorageFile {
        // File storage specific properties can be added here
        /**
         * Base path for file storage
         */
        private String basePath;
    }

    @Data
    public static class StorageDatabase {

    }

    // Holder that selects storage type and provides type-specific config groups
    @Data
    public static class Storage {
        /**
         * Storage type to use for repository. Allowed values: FILE, DATABASE
         */
        private StorageType type = StorageType.FILE;

        @NestedConfigurationProperty
        private StorageFile file = new StorageFile();

        @NestedConfigurationProperty
        private StorageDatabase database = new StorageDatabase();

        public enum StorageType {
            FILE,
            DATABASE
        }
    }


    @Data
    public static class ResponseProperties {
        private boolean wrap = true;
    }

}

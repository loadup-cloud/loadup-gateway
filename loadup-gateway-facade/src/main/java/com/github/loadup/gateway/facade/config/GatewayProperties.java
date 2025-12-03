package com.github.loadup.gateway.facade.config;

/*-
 * #%L
 * LoadUp Gateway Facade
 * %%
 * Copyright (C) 2026 LoadUp Cloud
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Gateway configuration properties (corresponds to loadup.gateway in application.yml)
 */
@Data
@Component
@ConfigurationProperties(prefix = "loadup.gateway")
public class GatewayProperties {

    /**
     * Whether to enable Gateway
     */
    private boolean enabled = true;

    /**
     * Route refresh interval (seconds)
     */
    private int routeRefreshInterval = 5;

    /**
     * Template cache size
     */
    private int templateCacheSize = 100;

    /**
     * Default timeout (milliseconds)
     */
    private long defaultTimeout = 10000L;

    /**
     * Default retry count
     */
    private int defaultRetryCount = 1;

    /**
     * Storage related configuration
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

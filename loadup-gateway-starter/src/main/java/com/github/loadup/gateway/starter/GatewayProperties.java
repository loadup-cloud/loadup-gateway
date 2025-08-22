package com.github.loadup.gateway.starter;

/*-
 * #%L
 * LoadUp Gateway Starter
 * %%
 * Copyright (C) 2025 LoadUp Gateway Authors
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

    /**
     * 是否启用Gateway
     */
    private boolean enabled = true;

    /**
     * 路由配置刷新间隔(秒)
     */
    private int routeRefreshInterval = 30;

    /**
     * 模板脚本缓存大小
     */
    private int templateCacheSize = 1000;

    /**
     * 默认超时时间(毫秒)
     */
    private long defaultTimeout = 30000;

    /**
     * 默认重试次数
     */
    private int defaultRetryCount = 3;

    /**
     * 存储配置
     */
    private Storage storage = new Storage();

    /**
     * 插件配置
     */
    private Map<String, PluginProperties> plugins = new HashMap<>();

    @Data
    public static class Storage {
        /**
         * 存储类型 (FILE/DATABASE)
         */
        private String type = "FILE";

        /**
         * 文件存储路径
         */
        private String filePath = "./gateway-config";

        /**
         * 数据库配置
         */
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
}

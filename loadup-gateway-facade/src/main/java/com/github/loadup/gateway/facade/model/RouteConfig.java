package com.github.loadup.gateway.facade.model;

/*-
 * #%L
 * LoadUp Gateway Facade
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

import com.github.loadup.gateway.facade.constants.GatewayConstants;
import lombok.Builder;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 路由配置模型
 */
@Data
@Builder
public class RouteConfig {

    /**
     * 路由ID（自动生成，基于 path + method）
     */
    private String routeId;

    /**
     * 路由名称（自动生成，基于 path）
     */
    private String routeName;

    /**
     * 匹配路径
     */
    private String path;

    /**
     * 设置匹配路径，并自动生成ID
     */
    public void setPath(String path) {
        this.path = path;
        // 自动生成ID
        generateIdsInternal();
    }

    /**
     * HTTP方法
     */
    private String method = "POST";

    /**
     * 设置HTTP方法，并自动生成ID
     */
    public void setMethod(String method) {
        this.method = method;
        // 自动生成ID
        generateIdsInternal();
    }

    /**
     * 协议类型 (HTTP/RPC/BEAN)
     */
    private String protocol;

    /**
     * 统一目标配置 (支持前缀格式: http://..., bean://service:method, rpc://class:method:version)
     */
    private String target;

    /**
     * 设置目标配置，并自动解析到相应字段
     */
    public void setTarget(String target) {
        this.target = target;
        // 自动解析目标配置
        parseTargetInternal();
    }

    /**
     * 获取目标URL（用于HTTP和RPC协议）
     */
    public String getTargetUrl() {
        // 如果targetUrl为空但target不为空，尝试解析
        if (targetUrl == null && target != null && !target.trim().isEmpty()) {
            parseTargetInternal();
        }
        return targetUrl;
    }

    /**
     * 获取目标Bean名称（用于BEAN协议）
     */
    public String getTargetBean() {
        // 如果targetBean为空但target不为空，尝试解析
        if (targetBean == null && target != null && !target.trim().isEmpty()) {
            parseTargetInternal();
        }
        return targetBean;
    }

    /**
     * 获取目标方法名称（用于BEAN协议）
     */
    public String getTargetMethod() {
        // 如果targetMethod为空但target不为空，尝试解析
        if (targetMethod == null && target != null && !target.trim().isEmpty()) {
            parseTargetInternal();
        }
        return targetMethod;
    }

    /**
     * 请求模板脚本
     */
    private String requestTemplate;

    /**
     * 响应模板脚本
     */
    private String responseTemplate;

    /**
     * 是否启用
     */
    private boolean enabled;

    /**
     * 扩展配置 (包含 timeout、retryCount 等所有可扩展的配置项)
     */
    private Map<String, Object> properties;

    // 临时兼容字段，用于解析后存储
    private transient String targetUrl;
    private transient String targetBean;
    private transient String targetMethod;

    private RouteConfig() {

    }

    /**
     * 获取超时时间(毫秒)
     */
    public long getTimeout() {
        if (properties != null && properties.containsKey(GatewayConstants.PropertyKeys.TIMEOUT)) {
            Object timeout = properties.get(GatewayConstants.PropertyKeys.TIMEOUT);
            if (timeout instanceof Number) {
                return ((Number) timeout).longValue();
            } else if (timeout instanceof String) {
                try {
                    return Long.parseLong((String) timeout);
                } catch (NumberFormatException e) {
                    return 30000L; // 默认值
                }
            }
        }
        return 30000L; // 默认值
    }

    /**
     * 设置超时时间(毫秒)
     */
    public void setTimeout(long timeout) {
        if (properties == null) {
            properties = new HashMap<>();
        }
        properties.put(GatewayConstants.PropertyKeys.TIMEOUT, timeout);
    }

    /**
     * 获取重试次数
     */
    public int getRetryCount() {
        if (properties != null && properties.containsKey(GatewayConstants.PropertyKeys.RETRY_COUNT)) {
            Object retryCount = properties.get(GatewayConstants.PropertyKeys.RETRY_COUNT);
            if (retryCount instanceof Number) {
                return ((Number) retryCount).intValue();
            } else if (retryCount instanceof String) {
                try {
                    return Integer.parseInt((String) retryCount);
                } catch (NumberFormatException e) {
                    return 3; // 默认值
                }
            }
        }
        return 3; // 默认值
    }

    /**
     * 设置重试次数
     */
    public void setRetryCount(int retryCount) {
        if (properties == null) {
            properties = new HashMap<>();
        }
        properties.put(GatewayConstants.PropertyKeys.RETRY_COUNT, retryCount);
    }

    /**
     * 获取指定的属性值
     */
    public Object getProperty(String key) {
        return properties != null ? properties.get(key) : null;
    }

    /**
     * 设置属性值
     */
    public void setProperty(String key, Object value) {
        if (properties == null) {
            properties = new HashMap<>();
        }
        properties.put(key, value);
    }

    /**
     * 获取是否统一包装响应（优先级高于全局配置）
     */
    public Boolean getWrapResponse() {
        if (properties != null && properties.containsKey(GatewayConstants.PropertyKeys.WRAP_RESPONSE)) {
            Object wrapResponse = properties.get(GatewayConstants.PropertyKeys.WRAP_RESPONSE);
            if (wrapResponse instanceof Boolean) {
                return (Boolean) wrapResponse;
            } else if (wrapResponse instanceof String) {
                return Boolean.parseBoolean((String) wrapResponse);
            }
        }
        return null; // 返回null表示使用全局配置
    }

    /**
     * 设置是否统一包装响应
     */
    public void setWrapResponse(Boolean wrapResponse) {
        if (properties == null) {
            properties = new HashMap<>();
        }
        properties.put(GatewayConstants.PropertyKeys.WRAP_RESPONSE, wrapResponse);
    }

    /**
     * 内部ID生成方法
     */
    private void generateIdsInternal() {
        if (path != null && method != null) {
            // 生成 routeId: path + method 的哈希值，确保唯一性
            this.routeId = generateRouteId(path, method);
            // 生成 routeName: 基于 path 的友好名称
            this.routeName = generateRouteName(path, method);
        }
    }

    /**
     *
     * @return property value of routeId
     */
    public String getRouteId() {
        if (path != null && method != null) {
            // 生成 routeId: path + method 的哈希值，确保唯一性
            return generateRouteId(path, method);
        }
        return null;
    }

    /**
     *
     * @return property value of routeName
     */
    public String getRouteName() {
        if (path != null && method != null) {
            // 生成 routeName: 基于 path 的友好名称
            return generateRouteName(path, method);
        }
        return null;
    }

    /**
     * 生成路由ID
     */
    private String generateRouteId(String path, String method) {
        String combined = path + ":" + method;
        // 使用简单的哈希算法生成ID，也可以直接使用组合字符串
        return "route-" + Math.abs(combined.hashCode());
    }

    /**
     * 生成路由名称
     */
    private String generateRouteName(String path, String method) {
        // 将路径转换为友好的名称
        String name = path.replaceAll("^/", "")  // 移除开头的 /
                .replaceAll("/", " ")   // 将 / 替换为空格
                .replaceAll("-", " ")   // 将 - 替换为空格
                .trim();

        if (name.isEmpty()) {
            name = "root";
        }

        // 首字母大写
        name = Character.toUpperCase(name.charAt(0)) + name.substring(1);

        return name + " (" + method + ")";
    }


    /**
     * 解析目标配置（内部使用）
     */
    private void parseTargetInternal() {
        if (target == null || target.trim().isEmpty()) {
            return;
        }
        if (StringUtils.startsWithIgnoreCase(target, GatewayConstants.Protocol.HTTP + "://") ||
                StringUtils.startsWithIgnoreCase(target, GatewayConstants.Protocol.HTTP + "s://")) {
            this.protocol = GatewayConstants.Protocol.HTTP;
            this.targetUrl = target;
            this.targetBean = null;
            this.targetMethod = null;
        } else if (StringUtils.startsWithIgnoreCase(target, GatewayConstants.Protocol.BEAN + "://")) {
            this.protocol = GatewayConstants.Protocol.BEAN;
            String beanTarget = target.substring(7); // 移除 "bean://" 前缀
            String[] parts = beanTarget.split(":");
            if (parts.length >= 2) {
                this.targetBean = parts[0];
                this.targetMethod = parts[1];
            }
            this.targetUrl = null;
        } else if (StringUtils.startsWithIgnoreCase(target, GatewayConstants.Protocol.RPC + "://")) {
            this.protocol = GatewayConstants.Protocol.RPC;
            this.targetUrl = target.substring(6); // 移除 "rpc://" 前缀
            this.targetBean = null;
            this.targetMethod = null;
        }
    }

    /**
     * 自定义 Builder 类，支持自动解析和生成ID
     * 注意：routeId 和 routeName 不能通过 builder 设置，只能内部生成
     */
    public static class RouteConfigBuilder {
        private String path;
        private String method;
        private String protocol;
        private String target;
        private String requestTemplate;
        private String responseTemplate;
        private boolean enabled;
        private Map<String, Object> properties;

        public RouteConfigBuilder path(String path) {
            this.path = path;
            return this;
        }

        public RouteConfigBuilder method(String method) {
            this.method = method;
            return this;
        }

        public RouteConfigBuilder protocol(String protocol) {
            this.protocol = protocol;
            return this;
        }

        public RouteConfigBuilder target(String target) {
            this.target = target;
            return this;
        }

        public RouteConfigBuilder requestTemplate(String requestTemplate) {
            this.requestTemplate = requestTemplate;
            return this;
        }

        public RouteConfigBuilder responseTemplate(String responseTemplate) {
            this.responseTemplate = responseTemplate;
            return this;
        }

        public RouteConfigBuilder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public RouteConfigBuilder properties(Map<String, Object> properties) {
            this.properties = properties;
            return this;
        }


        public RouteConfig build() {
            // 验证必填字段
            if (StringUtils.isBlank(this.path)) {
                throw new IllegalArgumentException("path is required and cannot be empty");
            }
            if (StringUtils.isBlank(this.target)) {
                throw new IllegalArgumentException("target is required and cannot be empty");
            }

            RouteConfig config = new RouteConfig();
            config.path = this.path;
            config.method = this.method != null ? this.method : "POST"; // 使用默认值
            config.protocol = this.protocol;
            config.target = this.target;
            config.requestTemplate = this.requestTemplate;
            config.responseTemplate = this.responseTemplate;
            config.enabled = this.enabled; //默认 true
            config.properties = this.properties;

            // 构建完成后自动解析目标配置
            if (config.target != null && !config.target.trim().isEmpty()) {
                config.parseTargetInternal();
            }

            // 构建完成后自动生成ID（routeId 和 routeName 只能通过这里生成）
            if (config.path != null && config.method != null) {
                config.generateIdsInternal();
            }

            return config;
        }
    }
}

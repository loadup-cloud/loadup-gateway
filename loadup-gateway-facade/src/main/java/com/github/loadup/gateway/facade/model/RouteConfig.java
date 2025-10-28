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
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 路由配置模型（不可变）
 */
@Getter
public class RouteConfig {

    /**
     * 路由ID（自动生成，基于 path + method）
     */
    private final String routeId;

    /**
     * 路由名称（自动生成，基于 path）
     */
    private final String routeName;

    /**
     * 匹配路径
     */
    private final String path;

    /**
     * HTTP方法
     */
    private final String method;

    /**
     * 协议类型 (HTTP/RPC/BEAN)
     */
    private final String protocol;

    /**
     * 统一目标配置 (原始字符串)
     */
    private final String target;

    /**
     * 目标 URL（HTTP/RPC 时使用）
     */
    private final String targetUrl;

    /**
     * 目标 Bean 名称（BEAN 协议时使用）
     */
    private final String targetBean;

    /**
     * 目标方法名（BEAN 协议时使用）
     */
    private final String targetMethod;

    /**
     * 请求模板脚本
     */
    private final String requestTemplate;

    /**
     * 响应模板脚本
     */
    private final String responseTemplate;

    /**
     * 是否启用
     */
    private final boolean enabled;

    /**
     * 扩展配置（不可变拷贝）
     */
    private final Map<String, Object> properties;

    /**
     * 解析后的超时时间（毫秒）
     */
    private final long parsedTimeout;

    /**
     * 解析后的重试次数
     */
    private final int parsedRetryCount;

    /**
     * 解析后的 wrapResponse（null 表示使用全局配置）
     */
    private final Boolean parsedWrapResponse;

    // 私有构造，Builder 调用
    private RouteConfig(RouteConfigBuilder b) {
        this.path = Objects.requireNonNull(b.path, "path is required");
        this.method = b.method != null ? b.method : "POST";
        this.target = Objects.requireNonNull(b.target, "target is required");
        this.requestTemplate = b.requestTemplate;
        this.responseTemplate = b.responseTemplate;
        this.enabled = b.enabled;

        // properties 拷贝并不可变化
        if (b.properties == null) {
            this.properties = Collections.emptyMap();
        } else {
            this.properties = Collections.unmodifiableMap(new HashMap<>(b.properties));
        }

        // 解析 target
        TargetParseResult tpr = parseTarget(this.target);
        this.protocol = tpr.protocol;
        this.targetUrl = tpr.targetUrl;
        this.targetBean = tpr.targetBean;
        this.targetMethod = tpr.targetMethod;

        // 解析 properties
        PropertiesParseResult ppr = parseProperties(this.properties);
        this.parsedTimeout = ppr.timeout;
        this.parsedRetryCount = ppr.retryCount;
        this.parsedWrapResponse = ppr.wrapResponse;

        // 生成 id/name
        this.routeId = generateRouteId(this.path, this.method);
        this.routeName = generateRouteName(this.path, this.method);
    }


    // 公开的读取方法返回解析后缓存值（@Getter 已生成常规字段的 getter）
    public long getTimeout() {
        return this.parsedTimeout;
    }

    public int getRetryCount() {
        return this.parsedRetryCount;
    }

    public Boolean getWrapResponse() {
        return this.parsedWrapResponse;
    }

    // 内部静态帮助类和方法
    private static class TargetParseResult {
        String protocol;
        String targetUrl;
        String targetBean;
        String targetMethod;
    }

    private static TargetParseResult parseTarget(String target) {
        TargetParseResult r = new TargetParseResult();
        if (target == null || target.trim().isEmpty()) {
            return r;
        }

        if (StringUtils.startsWithIgnoreCase(target, GatewayConstants.Protocol.HTTP + "://") ||
                StringUtils.startsWithIgnoreCase(target, GatewayConstants.Protocol.HTTP + "s://")) {
            r.protocol = GatewayConstants.Protocol.HTTP;
            r.targetUrl = target;
            return r;
        }

        if (StringUtils.startsWithIgnoreCase(target, GatewayConstants.Protocol.BEAN + "://")) {
            r.protocol = GatewayConstants.Protocol.BEAN;
            String beanTarget = target.substring(7); // remove "bean://"
            String[] parts = beanTarget.split(":");
            if (parts.length >= 1) r.targetBean = parts[0];
            if (parts.length >= 2) r.targetMethod = parts[1];
            return r;
        }

        if (StringUtils.startsWithIgnoreCase(target, GatewayConstants.Protocol.RPC + "://")) {
            r.protocol = GatewayConstants.Protocol.RPC;
            r.targetUrl = target.substring(6);
            return r;
        }

        return r;
    }

    private static class PropertiesParseResult {
        long timeout = 30000L;
        int retryCount = 3;
        Boolean wrapResponse = null;
    }

    private static PropertiesParseResult parseProperties(Map<String, Object> properties) {
        PropertiesParseResult r = new PropertiesParseResult();
        if (properties == null || properties.isEmpty()) return r;

        Object timeout = properties.get(GatewayConstants.PropertyKeys.TIMEOUT);
        if (timeout instanceof Number) {
            r.timeout = ((Number) timeout).longValue();
        } else if (timeout instanceof String) {
            try {
                r.timeout = Long.parseLong((String) timeout);
            } catch (NumberFormatException ignored) {
            }
        }

        Object retry = properties.get(GatewayConstants.PropertyKeys.RETRY_COUNT);
        if (retry instanceof Number) {
            r.retryCount = ((Number) retry).intValue();
        } else if (retry instanceof String) {
            try {
                r.retryCount = Integer.parseInt((String) retry);
            } catch (NumberFormatException ignored) {
            }
        }

        Object wrap = properties.get(GatewayConstants.PropertyKeys.WRAP_RESPONSE);
        if (wrap instanceof Boolean) {
            r.wrapResponse = (Boolean) wrap;
        } else if (wrap instanceof String) {
            r.wrapResponse = Boolean.parseBoolean((String) wrap);
        }

        return r;
    }

    private static String generateRouteId(String path, String method) {
        String combined = path + ":" + method;
        return "route-" + Math.abs(combined.hashCode());
    }

    private static String generateRouteName(String path, String method) {
        String name = path.replaceAll("^/", "")
                .replaceAll("/", " ")
                .replaceAll("-", " ")
                .trim();
        if (name.isEmpty()) name = "root";
        name = Character.toUpperCase(name.charAt(0)) + name.substring(1);
        return name + " (" + method + ")";
    }

    /**
     * 手工 Builder（替代 Lombok 的自动 Builder），在 build 时完成所有解析和拷贝，返回不可变对象
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
            if (StringUtils.isBlank(this.path)) {
                throw new IllegalArgumentException("path is required and cannot be empty");
            }
            if (StringUtils.isBlank(this.target)) {
                throw new IllegalArgumentException("target is required and cannot be empty");
            }

            return new RouteConfig(this);
        }
    }

    /**
     * Compatibility helper to obtain a new builder (replaces Lombok's builder())
     */
    public static RouteConfigBuilder builder() {
        return new RouteConfigBuilder();
    }

    /**
     * Compatibility helper to create a builder pre-populated from an existing instance.
     */
    public static RouteConfigBuilder builderFrom(RouteConfig rc) {
        RouteConfigBuilder b = new RouteConfigBuilder();
        if (rc == null) return b;
        b.path(rc.getPath());
        b.method(rc.getMethod());
        b.protocol(rc.getProtocol());
        b.target(rc.getTarget());
        b.requestTemplate(rc.getRequestTemplate());
        b.responseTemplate(rc.getResponseTemplate());
        b.enabled(rc.isEnabled());
        if (rc.getProperties() != null) {
            b.properties(new HashMap<>(rc.getProperties()));
        }
        return b;
    }
}

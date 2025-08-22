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

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.github.loadup.gateway.facade.constants.GatewayConstants;

import java.util.Map;
import java.util.HashMap;

/**
 * 路由配置模型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
     * HTTP方法
     */
    private String method;

    /**
     * 协议类型 (HTTP/RPC/BEAN)
     */
    private String protocol;

    /**
     * 统一目标配置 (支持前缀格式: http://..., bean://service:method, rpc://class:method:version)
     */
    private String target;

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

    /**
     * 获取超时时间(毫秒)
     */
    public long getTimeout() {
        if (properties != null && properties.containsKey("timeout")) {
            Object timeout = properties.get("timeout");
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
        properties.put("timeout", timeout);
    }

    /**
     * 获取重试次数
     */
    public int getRetryCount() {
        if (properties != null && properties.containsKey("retryCount")) {
            Object retryCount = properties.get("retryCount");
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
        properties.put("retryCount", retryCount);
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
     * 自动生成 routeId 和 routeName
     */
    public void generateIds() {
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
     * 根据target字段自动设置protocol和相关字段
     */
    public void parseTarget() {
        if (target == null || target.trim().isEmpty()) {
            return;
        }

        if (target.startsWith("http://") || target.startsWith("https://")) {
            this.protocol = GatewayConstants.Protocol.HTTP;
            this.targetUrl = target;
            this.targetBean = null;
            this.targetMethod = null;
        } else if (target.startsWith("bean://")) {
            this.protocol = GatewayConstants.Protocol.BEAN;
            String beanTarget = target.substring(7); // 移除 "bean://" 前缀
            String[] parts = beanTarget.split(":");
            if (parts.length >= 2) {
                this.targetBean = parts[0];
                this.targetMethod = parts[1];
            }
            this.targetUrl = null;
        } else if (target.startsWith("rpc://")) {
            this.protocol = GatewayConstants.Protocol.RPC;
            this.targetUrl = target.substring(6); // 移除 "rpc://" 前缀
            this.targetBean = null;
            this.targetMethod = null;
        }
    }

    /**
     * 根据现有字段生成target字段 (向后兼容)
     */
    public void generateTarget() {
        if (target != null && !target.trim().isEmpty()) {
            return; // target 已存在，不覆盖
        }

        if (GatewayConstants.Protocol.HTTP.equals(protocol) && targetUrl != null) {
            this.target = targetUrl;
        } else if (GatewayConstants.Protocol.BEAN.equals(protocol) && targetBean != null && targetMethod != null) {
            this.target = "bean://" + targetBean + ":" + targetMethod;
        } else if (GatewayConstants.Protocol.RPC.equals(protocol) && targetUrl != null) {
            this.target = "rpc://" + targetUrl;
        }
    }
}

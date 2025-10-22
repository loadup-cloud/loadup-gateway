package com.github.loadup.gateway.core.plugin;

/*-
 * #%L
 * LoadUp Gateway Core
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
import com.github.loadup.gateway.facade.model.GatewayRequest;
import com.github.loadup.gateway.facade.model.GatewayResponse;
import com.github.loadup.gateway.facade.model.RouteConfig;
import com.github.loadup.gateway.facade.spi.ProxyPlugin;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * 插件管理器
 */
@Slf4j
@Component
public class PluginManager {

    @Resource
    private List<ProxyPlugin> proxyPlugins;

    /**
     * 执行代理转发
     */
    public GatewayResponse executeProxy(GatewayRequest request, RouteConfig route) throws Exception {
        if (StringUtils.isBlank(route.getProtocol())) {
            throw new RuntimeException("No protocol found!");
        }
        Optional<ProxyPlugin> pluginOpt = findProxyPlugin(route.getProtocol());
        if (!pluginOpt.isPresent()) {
            throw new RuntimeException("No proxy plugin found for protocol: " + route.getProtocol());
        }
        ProxyPlugin plugin = pluginOpt.get();
        String target = determineTarget(route);
        log.debug("Executing proxy with plugin: {} for target: {}", plugin.getName(), target);
        return plugin.proxy(request, target);
    }

    /**
     * 查找对应协议的代理插件
     */
    private Optional<ProxyPlugin> findProxyPlugin(String protocol) {
        return proxyPlugins.stream()
                .filter(plugin -> protocol.equals(plugin.getSupportedProtocol()))
                .findFirst();
    }

    /**
     * 确定代理目标
     */
    private String determineTarget(RouteConfig route) {
        switch (route.getProtocol()) {
            case GatewayConstants.Protocol.HTTP:
                return route.getTargetUrl();
            case GatewayConstants.Protocol.RPC:
                return route.getTargetUrl();
            case GatewayConstants.Protocol.BEAN:
                return route.getTargetBean() + ":" + route.getTargetMethod();
            default:
                throw new IllegalArgumentException("Unsupported protocol: " + route.getProtocol());
        }
    }
}

package com.github.loadup.gateway.core.router;

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

import com.github.loadup.gateway.facade.config.GatewayProperties;
import com.github.loadup.gateway.facade.model.GatewayRequest;
import com.github.loadup.gateway.facade.model.PluginConfig;
import com.github.loadup.gateway.facade.model.RouteConfig;
import com.github.loadup.gateway.facade.spi.RepositoryPlugin;
import com.github.loadup.gateway.facade.utils.JsonUtils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * 路由解析器
 */
@Slf4j
@Component
public class RouteResolver {

    @Resource
    private RepositoryPlugin repositoryPlugin;
    @Resource
    private GatewayProperties gatewayProperties;

    private final ConcurrentHashMap<String, RouteConfig> routeCache = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    @PostConstruct
    public void refresh() {
        //initialize route
        GatewayProperties.Storage storage = gatewayProperties.getStorage();
        GatewayProperties.Storage.StorageType storageType = storage.getType();
        GatewayProperties.StorageFile file = storage.getFile();
        Map<String, Object> map = JsonUtils.toMap(file);
        PluginConfig pluginConfig = PluginConfig.builder()
                .pluginName("StoragePlugin")
                .enabled(true)
                .pluginType("Storage")
                .properties(map)
                .build();
        repositoryPlugin.initialize(pluginConfig);
        this.refreshRoutes();
        // 定时刷新路由缓存（热更新支持）
//        scheduler.scheduleAtFixedRate(this::refreshRoutes, 30, 30, TimeUnit.SECONDS);
    }

    /**
     * 解析请求对应的路由配置
     */
    public Optional<RouteConfig> resolve(GatewayRequest request) {
        String routeKey = buildRouteKey(request.getPath(), request.getMethod());

        // 先从缓存查找
        RouteConfig cachedRoute = routeCache.get(routeKey);
        if (cachedRoute != null && cachedRoute.isEnabled()) {
            return Optional.of(cachedRoute);
        }

        // 从存储中查找
        try {
            Optional<RouteConfig> routeOpt = repositoryPlugin.getRouteByPath(request.getPath(), request.getMethod());
            if (routeOpt.isPresent() && routeOpt.get().isEnabled()) {
                // 更新缓存
                routeCache.put(routeKey, routeOpt.get());
                return routeOpt;
            }
        } catch (Exception e) {
            log.error("Failed to resolve route from repository", e);
        }

        return Optional.empty();
    }

    /**
     * 刷新路由缓存
     */
    public void refreshRoutes() {
        try {
            List<RouteConfig> allRoutes = repositoryPlugin.getAllRoutes();

            // 清空旧缓存
            routeCache.clear();

            // 重新加载路由
            for (RouteConfig route : allRoutes) {
                if (route.isEnabled()) {
                    String routeKey = buildRouteKey(route.getPath(), route.getMethod());
                    routeCache.put(routeKey, route);
                }
            }

            log.info("Route cache refreshed, loaded {} routes", routeCache.size());

        } catch (Exception e) {
            log.error("Failed to refresh route cache", e);
        }
    }

    /**
     * 构建路由缓存键
     */
    private String buildRouteKey(String path, String method) {
        return method + ":" + path;
    }

    /**
     * 获取缓存的路由数量
     */
    public int getCachedRouteCount() {
        return routeCache.size();
    }
}

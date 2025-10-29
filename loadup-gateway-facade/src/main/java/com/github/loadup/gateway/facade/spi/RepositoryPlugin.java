package com.github.loadup.gateway.facade.spi;

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

import com.github.loadup.gateway.facade.dto.RouteStructure;
import com.github.loadup.gateway.facade.model.RouteConfig;

import java.util.List;
import java.util.Optional;

/**
 * 存储插件SPI接口
 */
public interface RepositoryPlugin extends GatewayPlugin {

    /**
     * 根据路由ID获取路由配置
     */
    Optional<RouteConfig> getRoute(String routeId) throws Exception;

    /**
     * 根据路径和方法获取路由配置
     */
    Optional<RouteConfig> getRouteByPath(String path, String method) throws Exception;

    /**
     * 获取所有路由配置
     */
    List<RouteConfig> getAllRoutes() throws Exception;

    /**
     * 获取模板
     */
    Optional<String> getTemplate(String templateId, String templateType) throws Exception;

    /**
     * 获取支持的存储类型
     */
    String getSupportedStorageType();

    /**
     *
     * @return
     */
    RouteConfig convertToRouteConfig(RouteStructure structure) throws Exception;
}

package com.github.loadup.gateway.plugins;

/*-
 * #%L
 * Repository Database Plugin
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
import com.github.loadup.gateway.facade.dto.RouteStructure;
import com.github.loadup.gateway.facade.model.GatewayRequest;
import com.github.loadup.gateway.facade.model.GatewayResponse;
import com.github.loadup.gateway.facade.model.PluginConfig;
import com.github.loadup.gateway.facade.model.RouteConfig;
import com.github.loadup.gateway.facade.spi.RepositoryPlugin;
import com.github.loadup.gateway.facade.utils.JsonUtils;
import com.github.loadup.gateway.plugins.entity.RouteEntity;
import com.github.loadup.gateway.plugins.entity.TemplateEntity;
import com.github.loadup.gateway.plugins.manager.RouteManager;
import com.github.loadup.gateway.plugins.manager.TemplateManager;
import com.github.loadup.gateway.plugins.mapper.RouteMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Database storage plugin
 */
@Slf4j
@Component
public class DatabaseRepositoryPlugin implements RepositoryPlugin {

    @Resource
    private RouteManager routeManager;

    @Resource
    private TemplateManager templateManager;

    @Resource
    private RouteMapper routeMapper;

    @Override
    public String getName() {
        return "DatabaseRepositoryPlugin";
    }

    @Override
    public String getType() {
        return "REPOSITORY";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public int getPriority() {
        return 200;
    }

    @Override
    public void initialize(PluginConfig config) {
        log.info("DatabaseRepositoryPlugin initialized with config: {}", config);
    }

    @Override
    public GatewayResponse execute(GatewayRequest request) throws Exception {
        throw new UnsupportedOperationException("Repository plugin does not handle requests directly");
    }

    @Override
    public void destroy() {
        log.info("DatabaseRepositoryPlugin destroyed");
    }

    @Override
    public boolean supports(GatewayRequest request) {
        return false;
    }


    @Override
    public Optional<RouteConfig> getRoute(String routeId) throws Exception {
        Optional<RouteEntity> entity = routeManager.findByRouteId(routeId);
        return entity.map(this::convertToRouteConfig);
    }

    @Override
    public Optional<RouteConfig> getRouteByPath(String path, String method) throws Exception {
        Optional<RouteEntity> entity = routeManager.findByPathAndMethod(path, method);
        return entity.map(this::convertToRouteConfig);
    }

    @Override
    public List<RouteConfig> getAllRoutes() throws Exception {
        Iterable<RouteEntity> entities = routeManager.findAll();
        return StreamSupport.stream(entities.spliterator(), false).map(this::convertToRouteConfig).collect(Collectors.toList());
    }


    @Override
    public Optional<String> getTemplate(String templateId, String templateType) throws Exception {
        Optional<TemplateEntity> entity = templateManager.findByTemplateIdAndTemplateType(templateId, templateType);
        return entity.map(TemplateEntity::getContent);
    }

   
    @Override
    public String getSupportedStorageType() {
        return GatewayConstants.Storage.DATABASE;
    }

    @Override
    public RouteConfig convertToRouteConfig(RouteStructure structure) {
        if (!(structure instanceof RouteEntity entity)) {
            throw new IllegalArgumentException("Invalid RouteStructure type");
        }
        Map<String, Object> properties = new HashMap<>();
        String propertiesStr = entity.getProperties();
        if (StringUtils.isNotBlank(propertiesStr)) {
            String trimmed = propertiesStr.trim();

            // 检查是否是 JSON 格式
            if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
                properties = JsonUtils.toMap(trimmed);
            }
            // 解析键值对格式：timeout=30000;retryCount=3 (使用分号分隔)
            String[] pairs = trimmed.split(";");

            for (String pair : pairs) {
                String[] keyValue = pair.trim().split("=");
                if (keyValue.length == 2) {
                    String key = keyValue[0].trim();
                    String value = keyValue[1].trim();

                    // 尝试转换为合适的数据类型
                    try {
                        if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
                            properties.put(key, Boolean.parseBoolean(value));
                        } else if (value.contains(".")) {
                            properties.put(key, Double.parseDouble(value));
                        } else {
                            properties.put(key, Long.parseLong(value));
                        }
                    } catch (NumberFormatException e) {
                        // 保持为字符串
                        properties.put(key, value);
                    }
                }
            }
        }


        // 确保 properties 中包含 timeout 和 retryCount
        if (!properties.containsKey("timeout")) {
            properties.put("timeout", 30000L);
        }
        if (!properties.containsKey("retryCount")) {
            properties.put("retryCount", 3);
        }

        boolean enabled = Boolean.TRUE.equals(entity.getEnabled());

        RouteConfig config = RouteConfig.builder().path(entity.getPath()).method(entity.getMethod()).target(entity.getTarget()).requestTemplate(entity.getRequestTemplate()).responseTemplate(entity.getResponseTemplate()).enabled(enabled).properties(properties).build();

        return config;
    }


}

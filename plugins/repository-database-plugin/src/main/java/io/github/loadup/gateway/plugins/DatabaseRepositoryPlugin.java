package io.github.loadup.gateway.plugins;

/*-
 * #%L
 * Repository Database Plugin
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
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

import io.github.loadup.gateway.facade.config.GatewayProperties;
import io.github.loadup.gateway.facade.constants.GatewayConstants;
import io.github.loadup.gateway.facade.dto.RouteStructure;
import io.github.loadup.gateway.facade.model.RouteConfig;
import io.github.loadup.gateway.facade.spi.RepositoryPlugin;
import io.github.loadup.gateway.facade.utils.JsonUtils;
import io.github.loadup.gateway.plugins.entity.RouteEntity;
import io.github.loadup.gateway.plugins.entity.TemplateEntity;
import io.github.loadup.gateway.plugins.manager.RouteManager;
import io.github.loadup.gateway.plugins.manager.TemplateManager;
import io.github.loadup.gateway.plugins.mapper.RouteMapper;
import jakarta.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/** Database storage plugin */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "loadup.gateway.storage", name = "type", havingValue = "DATABASE")
public class DatabaseRepositoryPlugin implements RepositoryPlugin {

  @Resource private RouteManager routeManager;

  @Resource private TemplateManager templateManager;

  @Resource private RouteMapper routeMapper;

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

  @Resource private GatewayProperties gatewayProperties;

  @Override
  public void initialize() {
    log.info("DatabaseRepositoryPlugin initialized");
    // Configuration can be accessed from gatewayProperties if needed
  }

  @Override
  public void destroy() {
    log.info("DatabaseRepositoryPlugin destroyed");
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
    return StreamSupport.stream(entities.spliterator(), false)
        .map(this::convertToRouteConfig)
        .collect(Collectors.toList());
  }

  @Override
  public Optional<String> getTemplate(String templateId, String templateType) throws Exception {
    Optional<TemplateEntity> entity =
        templateManager.findByTemplateIdAndTemplateType(templateId, templateType);
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

      // Check if it is JSON Format
      if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
        properties = JsonUtils.toMap(trimmed);
      }
      // Parse key-value format：timeout=30000;retryCount=3 (Use semicolon separator)
      String[] pairs = trimmed.split(";");

      for (String pair : pairs) {
        String[] keyValue = pair.trim().split("=");
        if (keyValue.length == 2) {
          String key = keyValue[0].trim();
          String value = keyValue[1].trim();

          // Try to convert to appropriate data type
          try {
            if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
              properties.put(key, Boolean.parseBoolean(value));
            } else if (value.contains(".")) {
              properties.put(key, Double.parseDouble(value));
            } else {
              properties.put(key, Long.parseLong(value));
            }
          } catch (NumberFormatException e) {
            // Keep as string
            properties.put(key, value);
          }
        }
      }
    }

    // Ensure properties Contains in timeout And retryCount
    if (!properties.containsKey("timeout")) {
      properties.put("timeout", 30000L);
    }
    if (!properties.containsKey("retryCount")) {
      properties.put("retryCount", 3);
    }

    boolean enabled = Boolean.TRUE.equals(entity.getEnabled());

    RouteConfig config =
        RouteConfig.builder()
            .path(entity.getPath())
            .method(entity.getMethod())
            .target(entity.getTarget())
            .requestTemplate(entity.getRequestTemplate())
            .responseTemplate(entity.getResponseTemplate())
            .enabled(enabled)
            .properties(properties)
            .build();

    return config;
  }
}

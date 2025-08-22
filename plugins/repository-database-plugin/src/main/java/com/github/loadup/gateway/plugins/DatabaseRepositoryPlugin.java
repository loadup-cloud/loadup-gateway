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

import com.github.loadup.gateway.facade.model.GatewayRequest;
import com.github.loadup.gateway.facade.model.GatewayResponse;
import com.github.loadup.gateway.facade.model.PluginConfig;
import com.github.loadup.gateway.facade.model.RouteConfig;
import com.github.loadup.gateway.facade.spi.RepositoryPlugin;
import com.github.loadup.gateway.facade.constants.GatewayConstants;
import com.github.loadup.gateway.facade.utils.JsonUtils;
import com.github.loadup.gateway.plugins.repository.RouteRepository;
import com.github.loadup.gateway.plugins.repository.TemplateRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 数据库存储插件
 */
@Slf4j
@Component
public class DatabaseRepositoryPlugin implements RepositoryPlugin {

    @Autowired
    private RouteRepository routeRepository;

    @Autowired
    private TemplateRepository templateRepository;

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
    public void saveRoute(RouteConfig routeConfig) throws Exception {
        RouteEntity entity = convertToEntity(routeConfig);
        routeRepository.save(entity);
        log.info("Route saved to database: {}", routeConfig.getRouteId());
    }

    @Override
    public Optional<RouteConfig> getRoute(String routeId) throws Exception {
        Optional<RouteEntity> entity = routeRepository.findByRouteId(routeId);
        return entity.map(this::convertToModel);
    }

    @Override
    public Optional<RouteConfig> getRouteByPath(String path, String method) throws Exception {
        Optional<RouteEntity> entity = routeRepository.findByPathAndMethod(path, method);
        return entity.map(this::convertToModel);
    }

    @Override
    public List<RouteConfig> getAllRoutes() throws Exception {
        return routeRepository.findAll().stream()
                .map(this::convertToModel)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteRoute(String routeId) throws Exception {
        routeRepository.deleteByRouteId(routeId);
        log.info("Route deleted from database: {}", routeId);
    }

    @Override
    public void saveTemplate(String templateId, String templateType, String content) throws Exception {
        TemplateEntity entity = new TemplateEntity();
        entity.setTemplateId(templateId);
        entity.setTemplateType(templateType);
        entity.setContent(content);
        entity.setUpdatedAt(new Date());

        templateRepository.save(entity);
        log.info("Template saved to database: {} ({})", templateId, templateType);
    }

    @Override
    public Optional<String> getTemplate(String templateId, String templateType) throws Exception {
        Optional<TemplateEntity> entity = templateRepository.findByTemplateIdAndTemplateType(templateId, templateType);
        return entity.map(TemplateEntity::getContent);
    }

    @Override
    public void deleteTemplate(String templateId, String templateType) throws Exception {
        templateRepository.deleteByTemplateIdAndTemplateType(templateId, templateType);
        log.info("Template deleted from database: {} ({})", templateId, templateType);
    }

    @Override
    public String getSupportedStorageType() {
        return GatewayConstants.Storage.DATABASE;
    }

    /**
     * 转换为实体
     */
    private RouteEntity convertToEntity(RouteConfig config) {
        // 确保 ID 和名称已生成
        if (config.getRouteId() == null || config.getRouteId().trim().isEmpty()) {
            config.generateIds();
        }

        // 确保 target 字段已生成
        if (config.getTarget() == null || config.getTarget().trim().isEmpty()) {
            config.generateTarget();
        }

        RouteEntity entity = new RouteEntity();
        entity.setRouteId(config.getRouteId());
        entity.setRouteName(config.getRouteName());
        entity.setPath(config.getPath());
        entity.setMethod(config.getMethod());
        entity.setProtocol(config.getProtocol());
        entity.setTarget(config.getTarget());

        // 设置临时字段用于兼容性
        entity.setTargetUrl(config.getTargetUrl());
        entity.setTargetBean(config.getTargetBean());
        entity.setTargetMethod(config.getTargetMethod());

        entity.setRequestTemplate(config.getRequestTemplate());
        entity.setResponseTemplate(config.getResponseTemplate());
        entity.setEnabled(config.isEnabled());
        entity.setTimeout(config.getTimeout()); // 从 properties 中获取
        entity.setRetryCount(config.getRetryCount()); // 从 properties 中获取
        entity.setProperties(JsonUtils.toJson(config.getProperties()));
        entity.setUpdatedAt(new Date());
        return entity;
    }

    /**
     * 转换为模型
     */
    private RouteConfig convertToModel(RouteEntity entity) {
        Map<String, Object> properties = new HashMap<>();
        if (entity.getProperties() != null) {
            properties = JsonUtils.toMap(entity.getProperties());
        }

        // 确保 properties 中包含 timeout 和 retryCount
        if (!properties.containsKey("timeout")) {
            properties.put("timeout", entity.getTimeout());
        }
        if (!properties.containsKey("retryCount")) {
            properties.put("retryCount", entity.getRetryCount());
        }

        RouteConfig config = RouteConfig.builder()
                .routeId(entity.getRouteId())
                .routeName(entity.getRouteName())
                .path(entity.getPath())
                .method(entity.getMethod())
                .protocol(entity.getProtocol())
                .target(entity.getTarget())
                .requestTemplate(entity.getRequestTemplate())
                .responseTemplate(entity.getResponseTemplate())
                .enabled(entity.isEnabled())
                .properties(properties)
                .build();

        // 如果缺少 ID 或名称，则自动生成
        if ((config.getRouteId() == null || config.getRouteId().trim().isEmpty()) ||
            (config.getRouteName() == null || config.getRouteName().trim().isEmpty())) {
            config.generateIds();
        }

        // 如果没有 target 字段但有旧字段，则生成 target
        if ((config.getTarget() == null || config.getTarget().trim().isEmpty()) &&
            (entity.getTargetUrl() != null || entity.getTargetBean() != null)) {

            // 设置临时字段用于生成 target
            config.setTargetUrl(entity.getTargetUrl());
            config.setTargetBean(entity.getTargetBean());
            config.setTargetMethod(entity.getTargetMethod());
            config.generateTarget();
        }

        // 解析 target 字段以确保所有字段都正确设置
        if (config.getTarget() != null && !config.getTarget().trim().isEmpty()) {
            config.parseTarget();
        }

        return config;
    }
}

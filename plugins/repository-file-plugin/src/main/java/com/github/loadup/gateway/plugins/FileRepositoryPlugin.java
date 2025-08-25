package com.github.loadup.gateway.plugins;

/*-
 * #%L
 * Repository File Plugin
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
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * 文件存储插件 - 使用CSV格式
 */
@Slf4j
@Component
public class FileRepositoryPlugin implements RepositoryPlugin {

    private String basePath = "./gateway-config";
    private final String ROUTES_FILE = "routes.csv";
    private final String TEMPLATES_DIR = "templates";

    @Override
    public String getName() {
        return "FileRepositoryPlugin";
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
        return 100;
    }

    @Override
    public void initialize(PluginConfig config) {
        log.info("FileRepositoryPlugin initialized with config: {}", config);

        if (config.getProperties() != null) {
            String configPath = (String) config.getProperties().get("basePath");
            if (configPath != null) {
                this.basePath = configPath;
            }
        }

        // 创建必要的目录
        try {
            Files.createDirectories(Paths.get(basePath));
            Files.createDirectories(Paths.get(basePath, TEMPLATES_DIR));

            // 创建路由CSV文件头部（如果不存在）
            Path routesFile = Paths.get(basePath, ROUTES_FILE);
            if (!Files.exists(routesFile)) {
                createRoutesFile(routesFile);
            }
        } catch (Exception e) {
            log.error("Failed to initialize file repository", e);
        }
    }

    @Override
    public GatewayResponse execute(GatewayRequest request) throws Exception {
        throw new UnsupportedOperationException("Repository plugin does not handle requests directly");
    }

    @Override
    public void destroy() {
        log.info("FileRepositoryPlugin destroyed");
    }

    @Override
    public boolean supports(GatewayRequest request) {
        return false; // Repository plugin不直接处理请求
    }

    @Override
    public void saveRoute(RouteConfig routeConfig) throws Exception {
        Path routesFile = Paths.get(basePath, ROUTES_FILE);

        // 读取现有路由
        List<RouteConfig> routes = getAllRoutes();

        // 更新或添加路由
        boolean updated = false;
        for (int i = 0; i < routes.size(); i++) {
            if (routes.get(i).getRouteId().equals(routeConfig.getRouteId())) {
                routes.set(i, routeConfig);
                updated = true;
                break;
            }
        }

        if (!updated) {
            routes.add(routeConfig);
        }

        // 写回文件
        writeRoutesToFile(routesFile, routes);
        log.info("Route saved: {}", routeConfig.getRouteId());
    }

    @Override
    public Optional<RouteConfig> getRoute(String routeId) throws Exception {
        return getAllRoutes().stream()
                .filter(route -> route.getRouteId().equals(routeId))
                .findFirst();
    }

    @Override
    public Optional<RouteConfig> getRouteByPath(String path, String method) throws Exception {
        return getAllRoutes().stream()
                .filter(route -> route.getPath().equals(path) && route.getMethod().equals(method))
                .findFirst();
    }

    @Override
    public List<RouteConfig> getAllRoutes() throws Exception {
        Path routesFile = Paths.get(basePath, ROUTES_FILE);
        if (!Files.exists(routesFile)) {
            return new ArrayList<>();
        }

        List<RouteConfig> routes = new ArrayList<>();

        try (CSVReader reader = new CSVReader(new FileReader(routesFile.toFile()))) {
            String[] headers = reader.readNext(); // 跳过头部
            String[] line;

            while ((line = reader.readNext()) != null) {
                RouteConfig route = parseRouteFromCsvLine(line, headers);
                if (route != null) {
                    routes.add(route);
                }
            }
        }

        return routes;
    }

    /**
     * 从 CSV 行解析路由配置，支持新旧格式
     */
    private RouteConfig parseRouteFromCsvLine(String[] line, String[] headers) {
        if (line.length < 2) {
            return null;
        }

        // 最新格式：path,method,target,requestTemplate,responseTemplate,enabled,properties
        if (line.length >= 3) {
            Map<String, Object> properties = new HashMap<>();

            // 解析 properties 字段（支持键值对和JSON格式）
            if (line.length > 6 && line[6] != null && !line[6].trim().isEmpty()) {
                try {
                    properties = parseProperties(line[6]);
                } catch (Exception e) {
                    log.warn("Failed to parse properties: {}", line[6], e);
                }
            }

            RouteConfig route = RouteConfig.builder()
                    .path(line[0])
                    .method(line[1])
                    .target(line[2])
                    .requestTemplate(line.length > 3 ? line[3] : "")
                    .responseTemplate(line.length > 4 ? line[4] : "")
                    .enabled(line.length > 5 ? Boolean.parseBoolean(line[5]) : true)
                    .properties(properties)
                    .build();


            return route;
        }


        return null;
    }

    /**
     * 简单的 JSON 解析器（解析简单的键值对）
     */
    private Map<String, Object> parseSimpleJson(String json) {
        Map<String, Object> result = new HashMap<>();

        // 移除大括号
        String content = json.substring(1, json.length() - 1);

        // 按逗号分割
        String[] pairs = content.split(",");

        for (String pair : pairs) {
            String[] keyValue = pair.split(":");
            if (keyValue.length == 2) {
                String key = keyValue[0].trim().replaceAll("\"", "");
                String value = keyValue[1].trim().replaceAll("\"", "");

                // 尝试转换为数字
                try {
                    if (value.contains(".")) {
                        result.put(key, Double.parseDouble(value));
                    } else {
                        result.put(key, Long.parseLong(value));
                    }
                } catch (NumberFormatException e) {
                    // 保持为字符串
                    result.put(key, value);
                }
            }
        }

        return result;
    }

    /**
     * 简单的属性解析器（支持键值对格式）
     */
    private Map<String, Object> parseProperties(String propertiesStr) {
        Map<String, Object> result = new HashMap<>();

        if (propertiesStr == null || propertiesStr.trim().isEmpty()) {
            return result;
        }

        String trimmed = propertiesStr.trim();

        // 检查是否是 JSON 格式
        if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
            return parseSimpleJson(trimmed);
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
                        result.put(key, Boolean.parseBoolean(value));
                    } else if (value.contains(".")) {
                        result.put(key, Double.parseDouble(value));
                    } else {
                        result.put(key, Long.parseLong(value));
                    }
                } catch (NumberFormatException e) {
                    // 保持为字符串
                    result.put(key, value);
                }
            }
        }

        return result;
    }

    /**
     * 生成 properties 字符串（使用分号分隔的键值对格式）
     */
    private String generatePropertiesString(RouteConfig route) {
        Map<String, Object> properties = route.getProperties();
        if (properties == null || properties.isEmpty()) {
            // 生成默认的 properties
            return "timeout=30000;retryCount=3";
        }

        StringBuilder result = new StringBuilder();
        boolean first = true;

        for (Map.Entry<String, Object> entry : properties.entrySet()) {
            if (!first) {
                result.append(";");
            }
            result.append(entry.getKey()).append("=").append(entry.getValue());
            first = false;
        }

        return result.toString();
    }

    @Override
    public void deleteRoute(String routeId) throws Exception {
        List<RouteConfig> routes = getAllRoutes();
        routes.removeIf(route -> route.getRouteId().equals(routeId));

        Path routesFile = Paths.get(basePath, ROUTES_FILE);
        writeRoutesToFile(routesFile, routes);
        log.info("Route deleted: {}", routeId);
    }

    @Override
    public void saveTemplate(String templateId, String templateType, String content) throws Exception {
        String fileName = templateId + "_" + templateType.toLowerCase() + ".groovy";
        Path templateFile = Paths.get(basePath, TEMPLATES_DIR, fileName);

        FileUtils.writeStringToFile(templateFile.toFile(), content, "UTF-8");
        log.info("Template saved: {} ({})", templateId, templateType);
    }

    @Override
    public Optional<String> getTemplate(String templateId, String templateType) throws Exception {
        String fileName = templateId + "_" + templateType.toLowerCase() + ".groovy";
        Path templateFile = Paths.get(basePath, TEMPLATES_DIR, fileName);

        if (Files.exists(templateFile)) {
            String content = FileUtils.readFileToString(templateFile.toFile(), "UTF-8");
            return Optional.of(content);
        }

        return Optional.empty();
    }

    @Override
    public void deleteTemplate(String templateId, String templateType) throws Exception {
        String fileName = templateId + "_" + templateType.toLowerCase() + ".groovy";
        Path templateFile = Paths.get(basePath, TEMPLATES_DIR, fileName);

        if (Files.exists(templateFile)) {
            Files.delete(templateFile);
            log.info("Template deleted: {} ({})", templateId, templateType);
        }
    }

    @Override
    public String getSupportedStorageType() {
        return GatewayConstants.Storage.FILE;
    }

    /**
     * 创建路由CSV文件
     */
    private void createRoutesFile(Path routesFile) throws IOException {
        try (CSVWriter writer = new CSVWriter(new FileWriter(routesFile.toFile()))) {
            String[] headers = {
                "path", "method", "target", "requestTemplate",
                "responseTemplate", "enabled", "properties"
            };
            writer.writeNext(headers);
        }
    }

    /**
     * 写入路由到文件
     */
    private void writeRoutesToFile(Path routesFile, List<RouteConfig> routes) throws IOException {
        try (CSVWriter writer = new CSVWriter(new FileWriter(routesFile.toFile()))) {
            // 写入头部 - 使用最新的 properties 格式
            String[] headers = {
                "path", "method", "target", "requestTemplate",
                "responseTemplate", "enabled", "properties"
            };
            writer.writeNext(headers);

            // 写入数据
            for (RouteConfig route : routes) {
                // 生成 properties 字符串（使用分号分隔格式）
                String propertiesStr = generatePropertiesString(route);

                String[] data = {
                    route.getPath(),
                    route.getMethod(),
                    route.getTarget() != null ? route.getTarget() : "",
                    route.getRequestTemplate() != null ? route.getRequestTemplate() : "",
                    route.getResponseTemplate() != null ? route.getResponseTemplate() : "",
                    String.valueOf(route.isEnabled()),
                    propertiesStr
                };
                writer.writeNext(data);
            }
        }
    }
}

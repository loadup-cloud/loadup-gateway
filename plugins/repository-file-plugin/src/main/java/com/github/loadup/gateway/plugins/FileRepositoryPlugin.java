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

import com.github.loadup.gateway.facade.config.GatewayProperties;
import com.github.loadup.gateway.facade.constants.GatewayConstants;
import com.github.loadup.gateway.facade.model.GatewayRequest;
import com.github.loadup.gateway.facade.model.GatewayResponse;
import com.github.loadup.gateway.facade.model.PluginConfig;
import com.github.loadup.gateway.facade.model.RouteConfig;
import com.github.loadup.gateway.facade.spi.RepositoryPlugin;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Component;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;

/**
 * 文件存储插件 - 使用CSV格式
 */
@Slf4j
@Component
public class FileRepositoryPlugin implements RepositoryPlugin {

    // basePath will be resolved during initialize. Default source is classpath:/gateway-config
    private String basePath = null; // resolved filesystem directory
    private final String ROUTES_FILE = "routes.csv";
    private final String TEMPLATES_DIR = "templates";
    @Resource
    private GatewayProperties gatewayProperties;

    public FileRepositoryPlugin() {
    }

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

        // 1. Check PluginConfig properties first (backwards compatibility)
        String configured = null;
        if (config != null && config.getProperties() != null) {
            Object cfg = config.getProperties().get("basePath");
            if (cfg instanceof String) {
                configured = ((String) cfg).trim();
            }
        }

        // 2. If not provided via PluginConfig, read from GatewayProperties.storage.file.basePath
        if (configured == null || configured.trim().isEmpty()) {
            try {
                if (gatewayProperties != null) {
                    GatewayProperties.StorageFile file = gatewayProperties.getStorage().getFile();
                    if (file != null) {
                        configured = file.getBasePath().trim();
                    }
                }
            } catch (Exception e) {
                log.warn("Failed to read file repository base path from GatewayProperties (reflective)", e);
                configured = null;
            }
        }

        // 3. Default to classpath:/gateway-config when still not provided
        if (configured == null || configured.isEmpty()) {
            configured = "classpath:/gateway-config";
        }

        // Resolve the configured location to a writable filesystem directory
        try {
            if (configured.startsWith("classpath:")) {
                String cpPath = configured.substring("classpath:".length());
                // ensure no leading slash
                if (cpPath.startsWith("/")) {
                    cpPath = cpPath.substring(1);
                }
                Path tempDir = copyClasspathDirToTemp(cpPath);
                this.basePath = tempDir.toAbsolutePath().toString();
            } else {
                // treat as filesystem path (relative or absolute). Create directories if necessary.
                Path p = Paths.get(configured);
                Files.createDirectories(p);
                this.basePath = p.toAbsolutePath().toString();
            }

            // Ensure templates directory exists
            Files.createDirectories(Paths.get(basePath, TEMPLATES_DIR));

            // Create routes CSV file if missing
            Path routesFile = Paths.get(basePath, ROUTES_FILE);
            if (!Files.exists(routesFile)) {
                createRoutesFile(routesFile);
            }

            log.info("FileRepositoryPlugin basePath resolved to {} (source={})", this.basePath, configured);
        } catch (Exception e) {
            log.error("Failed to initialize file repository with configured path: {}", configured, e);
        }
    }

    /**
     * Copy resources under given classpath directory to a temporary directory and return its Path.
     * This allows reading/writing files that are packaged in the classpath.
     */
    private Path copyClasspathDirToTemp(String classpathDir) throws IOException {
        // Create a temp directory unique to this application run
        String tmpRoot = System.getProperty("java.io.tmpdir");
        Path targetDir = Paths.get(tmpRoot, "loadup-gateway-config", String.valueOf(Math.abs(new Random().nextInt())));
        Files.createDirectories(targetDir);

        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Enumeration<URL> resources = cl.getResources(classpathDir);

        boolean found = false;
        while (resources.hasMoreElements()) {
            URL resourceUrl = resources.nextElement();
            found = true;
            String protocol = resourceUrl.getProtocol();
            if ("file".equals(protocol)) {
                // Resource is on filesystem (e.g., during development)
                try {
                    Path src = Paths.get(resourceUrl.toURI());
                    // copy directory recursively
                    try (Stream<Path> walker = Files.walk(src)) {
                        walker.forEach(srcPath -> {
                            try {
                                Path rel = src.relativize(srcPath);
                                Path destPath = targetDir.resolve(rel.toString());
                                if (Files.isDirectory(srcPath)) {
                                    Files.createDirectories(destPath);
                                } else {
                                    Files.createDirectories(destPath.getParent());
                                    Files.copy(srcPath, destPath);
                                }
                            } catch (IOException e) {
                                log.warn("Failed to copy resource file {}", srcPath, e);
                            }
                        });
                    }
                } catch (Exception e) {
                    log.warn("Failed to copy classpath (file) resource {}", resourceUrl, e);
                }
            } else if ("jar".equals(protocol)) {
                // Resource is inside a JAR — iterate JAR entries
                try {
                    JarURLConnection jarCon = (JarURLConnection) resourceUrl.openConnection();
                    try (JarFile jar = jarCon.getJarFile()) {
                        Enumeration<JarEntry> entries = jar.entries();
                        String prefix = classpathDir.endsWith("/") ? classpathDir : (classpathDir + "/");
                        while (entries.hasMoreElements()) {
                            JarEntry entry = entries.nextElement();
                            String name = entry.getName();
                            if (name.startsWith(prefix)) {
                                String relative = name.substring(prefix.length());
                                if (entry.isDirectory()) {
                                    Files.createDirectories(targetDir.resolve(relative));
                                } else {
                                    Path outFile = targetDir.resolve(relative);
                                    Files.createDirectories(outFile.getParent());
                                    try (InputStream is = cl.getResourceAsStream(name)) {
                                        if (is != null) {
                                            Files.copy(is, outFile);
                                        }
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    log.warn("Failed to copy classpath (jar) resources from {}", resourceUrl, e);
                }
            } else {
                log.warn("Unsupported classpath resource protocol: {} for URL {}", protocol, resourceUrl);
            }
        }

        if (!found) {
            // No resources found — still ensure directory exists so plugin can create files
            Files.createDirectories(targetDir);
        }

        return targetDir;
    }

    @Override
    public GatewayResponse execute(GatewayRequest request) {
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
        return getAllRoutes().stream().filter(route -> route.getRouteId().equals(routeId)).findFirst();
    }

    @Override
    public Optional<RouteConfig> getRouteByPath(String path, String method) throws Exception {
        return getAllRoutes().stream().filter(route -> route.getPath().equals(path) && route.getMethod().equals(method)).findFirst();
    }


    @Override
    public List<RouteConfig> getAllRoutes() throws Exception {
        Path routesFile = Paths.get(basePath, ROUTES_FILE);
        if (!Files.exists(routesFile)) {
            return new ArrayList<>();
        }

        List<RouteConfig> routes = new ArrayList<>();

        try (CSVReader reader = new CSVReader(new FileReader(routesFile.toFile()))) {
            reader.readNext(); // skip header
            String[] line;

            while ((line = reader.readNext()) != null) {
                RouteConfig route = parseRouteFromCsvLine(line);
                if (route != null) {
                    routes.add(route);
                }
            }
        }

        routes.forEach(rc -> applyTemplates(rc));

        return routes;
    }

    /**
     * Replace requestTemplate/responseTemplate values that are template file names with their file contents.
     * If a template value does not correspond to an existing file, it will be left as-is.
     */
    private void applyTemplates(RouteConfig rc) {
        if (rc == null) return;
        try {
            String req = rc.getRequestTemplate();
            if (req != null && !req.trim().isEmpty()) {
                String loaded = loadTemplateContent(req.trim());
                if (loaded != null) rc.setRequestTemplate(loaded);
            }

            String resp = rc.getResponseTemplate();
            if (resp != null && !resp.trim().isEmpty()) {
                String loaded = loadTemplateContent(resp.trim());
                if (loaded != null) rc.setResponseTemplate(loaded);
            }
        } catch (Exception e) {
            log.warn("Error applying templates for route {}: {}", rc.getRouteId(), e.getMessage());
        }
    }

    /**
     * Try to load a template by name from the resolved filesystem `basePath/templates/<name>`.
     * If not found, try classpath `templates/<name>` (or the plain name) as fallback. Returns null if not found.
     */
    private String loadTemplateContent(String templateName) {
        if (templateName == null || templateName.trim().isEmpty()) return null;
        try {
            // Try a list of candidate filenames to be more flexible with CSV template naming
            List<String> candidates = new ArrayList<>();
            candidates.add(templateName);
            candidates.add(templateName + ".groovy");
            candidates.add(templateName + "_request.groovy");
            candidates.add(templateName + "_response.groovy");
            candidates.add(templateName + "_request_template.groovy");
            candidates.add(templateName + "_response_template.groovy");

            for (String candidate : candidates) {
                // Prefer filesystem templates under basePath/templates
                if (basePath != null) {
                    Path p = Paths.get(basePath, TEMPLATES_DIR, candidate);
                    if (Files.exists(p) && Files.isRegularFile(p)) {
                        return Files.readString(p);
                    }
                }

                // Fallback: try classpath resource under templates/<candidate>
                String resourcePath = TEMPLATES_DIR + "/" + candidate;
                ClassLoader cl = Thread.currentThread().getContextClassLoader();
                try (InputStream is = cl.getResourceAsStream(resourcePath)) {
                    if (is != null) {
                        return new String(is.readAllBytes());
                    }
                }

                // Fallback: try direct classpath resource by candidate name
                try (InputStream is = cl.getResourceAsStream(candidate)) {
                    if (is != null) {
                        return new String(is.readAllBytes());
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Failed to load template '{}' : {}", templateName, e.getMessage());
        }
        // not found -> return original filename so callers still see something
        return templateName;
    }

    /**
     * 从 CSV 行解析路由配置，支持新旧格式
     */
    private RouteConfig parseRouteFromCsvLine(String[] line) {
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

            return RouteConfig.builder().path(line[0]).method(line[1]).target(line[2]).requestTemplate(line.length > 3 ? line[3] : "").responseTemplate(line.length > 4 ? line[4] : "").enabled(line.length <= 5 || Boolean.parseBoolean(line[5])).properties(properties).build();
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
            String[] headers = {"path", "method", "target", "requestTemplate", "responseTemplate", "enabled", "properties"};
            writer.writeNext(headers);
        }
    }

    /**
     * 写入路由到文件
     */
    private void writeRoutesToFile(Path routesFile, List<RouteConfig> routes) throws IOException {
        try (CSVWriter writer = new CSVWriter(new FileWriter(routesFile.toFile()))) {
            // 写入头部 - 使用最新的 properties 格式
            String[] headers = {"path", "method", "target", "requestTemplate", "responseTemplate", "enabled", "properties"};
            writer.writeNext(headers);

            // 写入数据
            for (RouteConfig route : routes) {
                // 生成 properties 字符串（使用分号分隔格式）
                String propertiesStr = generatePropertiesString(route);

                // Ensure we don't persist raw template bodies into CSV: if template appears to be content, save it as a template file and reference its id instead
                String reqField = route.getRequestTemplate() != null ? route.getRequestTemplate() : "";
                String respField = route.getResponseTemplate() != null ? route.getResponseTemplate() : "";

                try {
                    if (looksLikeTemplateContent(reqField)) {
                        String tplId = route.getRouteId() + "_req";
                        // saveTemplate may throw; catch and fallback to writing raw
                        try {
                            saveTemplate(tplId, "request", reqField);
                            reqField = tplId; // store id in CSV
                        } catch (Exception e) {
                            log.warn("Failed to save request template for route {}: {}", route.getRouteId(), e.getMessage());
                        }
                    }
                } catch (Exception ignored) {
                }

                try {
                    if (looksLikeTemplateContent(respField)) {
                        String tplId = route.getRouteId() + "_resp";
                        try {
                            saveTemplate(tplId, "response", respField);
                            respField = tplId;
                        } catch (Exception e) {
                            log.warn("Failed to save response template for route {}: {}", route.getRouteId(), e.getMessage());
                        }
                    }
                } catch (Exception ignored) {
                }

                String[] data = {route.getPath(), route.getMethod(), route.getTarget() != null ? route.getTarget() : "", reqField, respField, String.valueOf(route.isEnabled()), propertiesStr};
                writer.writeNext(data);
            }
        }
    }

    /**
     * Heuristic to decide whether a template field is full content rather than a short id/filename.
     */
    private boolean looksLikeTemplateContent(String s) {
        if (s == null) return false;
        String t = s.trim();
        if (t.isEmpty()) return false;
        // If it contains newline or Groovy/JS keywords, looks like content
        if (t.contains("\n") || t.contains("\r")) return true;
        if (t.length() > 200) return true; // long text -> content
        String lower = t.toLowerCase();
        if (lower.contains("def ") || lower.contains("return ") || lower.contains("class ") || lower.contains("package ") || lower.contains("function "))
            return true;
        // If it contains symbols typical for templates, treat as content
        if (t.contains("{") || t.contains("}") || t.contains("$")) return true;
        return false;
    }
}


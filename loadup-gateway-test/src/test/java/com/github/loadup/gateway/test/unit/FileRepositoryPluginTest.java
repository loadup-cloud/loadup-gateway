package com.github.loadup.gateway.test.unit;

/*-
 * #%L
 * LoadUp Gateway Test
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

import com.github.loadup.gateway.facade.model.PluginConfig;
import com.github.loadup.gateway.facade.model.RouteConfig;
import com.github.loadup.gateway.facade.constants.GatewayConstants;
import com.github.loadup.gateway.plugins.FileRepositoryPlugin;
import com.github.loadup.gateway.test.BaseGatewayTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 文件存储插件单元测试
 */
@DisplayName("文件存储插件测试")
public class FileRepositoryPluginTest extends BaseGatewayTest {

    @TempDir
    Path tempDir;

    private FileRepositoryPlugin fileRepositoryPlugin;

    @BeforeEach
    public void setUp() {
        super.setUp();
        fileRepositoryPlugin = new FileRepositoryPlugin();

        // 配置插件使用临时目录
        Map<String, Object> properties = new HashMap<>();
        properties.put("basePath", tempDir.toString());

        PluginConfig config = PluginConfig.builder()
                .pluginName("FileRepositoryPlugin")
                .pluginType("REPOSITORY")
                .enabled(true)
                .properties(properties)
                .build();

        fileRepositoryPlugin.initialize(config);
    }

    @Test
    @DisplayName("应该能够保存和获取路由配置")
    public void shouldSaveAndGetRouteConfig() throws Exception {
        // Given
        RouteConfig route = createTestRoute("/api/test",
                                          GatewayConstants.Protocol.HTTP, "http://localhost:8080");
        route.generateIds();
        // When
        fileRepositoryPlugin.saveRoute(route);
        Optional<RouteConfig> result = fileRepositoryPlugin.getRoute(route.getRouteId());

        // Then
        assertTrue(result.isPresent());
        assertEquals("/api/test", result.get().getPath());
        assertEquals(GatewayConstants.Protocol.HTTP, result.get().getProtocol());
    }

    @Test
    @DisplayName("应该能够通过路径和方法获取路由")
    public void shouldGetRouteByPathAndMethod() throws Exception {
        // Given
        RouteConfig route = createTestRoute("/api/user",
                                          GatewayConstants.Protocol.BEAN, "userService:getUser");

        // When
        fileRepositoryPlugin.saveRoute(route);
        Optional<RouteConfig> result = fileRepositoryPlugin.getRouteByPath("/api/user", "GET");

        // Then
        assertTrue(result.isPresent());
        assertEquals("route-858608907", result.get().getRouteId());
        assertEquals("/api/user", result.get().getPath());
        assertEquals("GET", result.get().getMethod());
    }

    @Test
    @DisplayName("应该能够获取所有路由")
    public void shouldGetAllRoutes() throws Exception {
        // Given
        RouteConfig route1 = createTestRoute("/api/test1",
                                           GatewayConstants.Protocol.HTTP, "http://localhost:8080/test1");
        RouteConfig route2 = createTestRoute("/api/test2",
                                           GatewayConstants.Protocol.BEAN, "testService:getData");

        // When
        fileRepositoryPlugin.saveRoute(route1);
        fileRepositoryPlugin.saveRoute(route2);
        List<RouteConfig> routes = fileRepositoryPlugin.getAllRoutes();

        // Then
        assertEquals(2, routes.size());
        assertTrue(routes.stream().anyMatch(r -> "/api/test1".equals(r.getPath())));
        assertTrue(routes.stream().anyMatch(r -> "/api/test2".equals(r.getPath())));
    }

    @Test
    @DisplayName("应该能够删除路由")
    public void shouldDeleteRoute() throws Exception {
        // Given
        RouteConfig route = createTestRoute("/api/delete",
                                          GatewayConstants.Protocol.HTTP, "http://localhost:8080");

        // When
        fileRepositoryPlugin.saveRoute(route);
        assertTrue(fileRepositoryPlugin.getRoute(route.getRouteId()).isPresent());

        fileRepositoryPlugin.deleteRoute(route.getRouteId());
        Optional<RouteConfig> result = fileRepositoryPlugin.getRoute(route.getRouteId());

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("应该能够保存和获取模板")
    public void shouldSaveAndGetTemplate() throws Exception {
        // Given
        String templateId = "test-template";
        String templateType = "REQUEST";
        String templateContent = """
            // 测试模板
            request.headers.put("X-Test", "true")
            return request
            """;

        // When
        fileRepositoryPlugin.saveTemplate(templateId, templateType, templateContent);
        Optional<String> result = fileRepositoryPlugin.getTemplate(templateId, templateType);

        // Then
        assertTrue(result.isPresent());
        assertTrue(result.get().contains("X-Test"));
        assertTrue(result.get().contains("return request"));
    }

    @Test
    @DisplayName("应该能够删除模板")
    public void shouldDeleteTemplate() throws Exception {
        // Given
        String templateId = "delete-template";
        String templateType = "RESPONSE";
        String templateContent = "return response";

        // When
        fileRepositoryPlugin.saveTemplate(templateId, templateType, templateContent);
        assertTrue(fileRepositoryPlugin.getTemplate(templateId, templateType).isPresent());

        fileRepositoryPlugin.deleteTemplate(templateId, templateType);
        Optional<String> result = fileRepositoryPlugin.getTemplate(templateId, templateType);

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("应该返回正确的支持的存储类型")
    public void shouldReturnCorrectSupportedStorageType() {
        // When
        String storageType = fileRepositoryPlugin.getSupportedStorageType();

        // Then
        assertEquals(GatewayConstants.Storage.FILE, storageType);
    }
}

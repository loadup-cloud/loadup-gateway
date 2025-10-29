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

import com.github.loadup.gateway.facade.constants.GatewayConstants;
import com.github.loadup.gateway.facade.model.PluginConfig;
import com.github.loadup.gateway.facade.model.RouteConfig;
import com.github.loadup.gateway.plugins.FileRepositoryPlugin;
import com.github.loadup.gateway.test.BaseGatewayTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * File Storage Plugin Unit Test
 */
@DisplayName("File Storage Plugin Test")
public class FileRepositoryPluginTest extends BaseGatewayTest {

    @TempDir
    Path tempDir;

    private FileRepositoryPlugin fileRepositoryPlugin;

    @BeforeEach
    public void setUp() {
        super.setUp();
        fileRepositoryPlugin = new FileRepositoryPlugin();

        // Configure plugin to use temporary directory
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
    @DisplayName("Should be able to get route by path and method")
    public void shouldGetRouteByPathAndMethod() throws Exception {
        // Given
        RouteConfig route = createTestRoute("/api/user", "GET",
                "userService:getUser");

        // When
        Optional<RouteConfig> result = fileRepositoryPlugin.getRouteByPath("/api/user", "GET");

        // Then
        assertTrue(result.isPresent());
        assertEquals("route-858608907", result.get().getRouteId());
        assertEquals("/api/user", result.get().getPath());
        assertEquals("GET", result.get().getMethod());
    }

    @Test
    @DisplayName("Should be able to get all routes")
    public void shouldGetAllRoutes() throws Exception {
        // Given
        RouteConfig route1 = createTestRoute("/api/test1", "GET",
                "http://localhost:8080/test1");
        RouteConfig route2 = createTestRoute("/api/test2", "GET",
                "testService:getData");

        // When
        List<RouteConfig> routes = fileRepositoryPlugin.getAllRoutes();

        // Then
        assertEquals(2, routes.size());
        assertTrue(routes.stream().anyMatch(r -> "/api/test1".equals(r.getPath())));
        assertTrue(routes.stream().anyMatch(r -> "/api/test2".equals(r.getPath())));
    }

    @Test
    @DisplayName("Should be able to delete route")
    public void shouldDeleteRoute() throws Exception {
        // Given
        RouteConfig route = createTestRoute("/api/test", "GET",
                "http://localhost:8080");

        // When
        assertTrue(fileRepositoryPlugin.getRoute(route.getRouteId()).isPresent());

        Optional<RouteConfig> result = fileRepositoryPlugin.getRoute(route.getRouteId());

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Should be able to save andGetTemplate")
    public void shouldSaveAndGetTemplate() throws Exception {
        // Given
        String templateId = "test-template";
        String templateType = "REQUEST";
        String templateContent = """
                // Test template
                request.headers.put("X-Test", "true")
                return request
                """;

        // When
        Optional<String> result = fileRepositoryPlugin.getTemplate(templateId, templateType);

        // Then
        assertTrue(result.isPresent());
        assertTrue(result.get().contains("X-Test"));
        assertTrue(result.get().contains("return request"));
    }

    @Test
    @DisplayName("Should be able to deleteTemplate")
    public void shouldDeleteTemplate() throws Exception {
        // Given
        String templateId = "delete-template";
        String templateType = "RESPONSE";
        String templateContent = "return response";

        // When
        assertTrue(fileRepositoryPlugin.getTemplate(templateId, templateType).isPresent());

        Optional<String> result = fileRepositoryPlugin.getTemplate(templateId, templateType);

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Should return correct supported storage type")
    public void shouldReturnCorrectSupportedStorageType() {
        // When
        String storageType = fileRepositoryPlugin.getSupportedStorageType();

        // Then
        assertEquals(GatewayConstants.Storage.FILE, storageType);
    }
}

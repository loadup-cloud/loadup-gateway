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

import com.github.loadup.gateway.facade.model.RouteConfig;
import com.github.loadup.gateway.test.BaseGatewayTest;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RouteConfig 自动生成 ID 功能测试
 */
public class RouteConfigIdGenerationTest extends BaseGatewayTest {

    @Test
    public void testGenerateRouteId() {
        RouteConfig route = RouteConfig.builder()
                .path("/api/test/user")
                .method("GET")
                .build();

        route.generateIds();

        assertNotNull(route.getRouteId());
        assertTrue(route.getRouteId().startsWith("route-"));
        assertNotNull(route.getRouteName());
    }

    @Test
    public void testGenerateRouteName() {
        RouteConfig route1 = RouteConfig.builder()
                .path("/api/test/user")
                .method("GET")
                .build();

        route1.generateIds();
        assertEquals("Api test user (GET)", route1.getRouteName());

        RouteConfig route2 = RouteConfig.builder()
                .path("/api/user-management")
                .method("POST")
                .build();

        route2.generateIds();
        assertEquals("Api user management (POST)", route2.getRouteName());

        RouteConfig route3 = RouteConfig.builder()
                .path("/")
                .method("GET")
                .build();

        route3.generateIds();
        assertEquals("Root (GET)", route3.getRouteName());
    }

    @Test
    public void testUniqueRouteIds() {
        RouteConfig route1 = RouteConfig.builder()
                .path("/api/test")
                .method("GET")
                .build();

        RouteConfig route2 = RouteConfig.builder()
                .path("/api/test")
                .method("POST")
                .build();

        route1.generateIds();
        route2.generateIds();

        assertNotEquals(route1.getRouteId(), route2.getRouteId());
    }

    @Test
    public void testConsistentRouteIds() {
        RouteConfig route1 = RouteConfig.builder()
                .path("/api/test")
                .method("GET")
                .build();

        RouteConfig route2 = RouteConfig.builder()
                .path("/api/test")
                .method("GET")
                .build();

        route1.generateIds();
        route2.generateIds();

        // 相同的路径和方法应该生成相同的ID
        assertEquals(route1.getRouteId(), route2.getRouteId());
        assertEquals(route1.getRouteName(), route2.getRouteName());
    }

    @Test
    public void testSimplifiedCsvFormat() {
        // 创建 properties 包含扩展配置
        Map<String, Object> properties = new HashMap<>();
        properties.put("timeout", 5000L);
        properties.put("retryCount", 2);
        properties.put("maxConnections", 50);
        properties.put("compression", true);

        // 测试简化的配置创建
        RouteConfig route = RouteConfig.builder()
                .path("/api/user")
                .method("GET")
                .target("http://localhost:8080/users")
                .enabled(true)
                .properties(properties)
                .build();

        // 生成ID和解析target
        route.generateIds();
        route.parseTarget();

        // 验证所有字段都正确设置
        assertNotNull(route.getRouteId());
        assertNotNull(route.getRouteName());
        assertEquals("/api/user", route.getPath());
        assertEquals("GET", route.getMethod());
        assertEquals("http://localhost:8080/users", route.getTarget());
        assertEquals("HTTP", route.getProtocol());
        assertTrue(route.isEnabled());
        assertEquals(5000L, route.getTimeout());
        assertEquals(2, route.getRetryCount());
        assertEquals(50, route.getProperty("maxConnections"));
        assertEquals(true, route.getProperty("compression"));
    }
}

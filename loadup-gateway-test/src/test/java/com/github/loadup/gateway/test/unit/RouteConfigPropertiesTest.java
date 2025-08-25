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
 * RouteConfig properties 字段功能测试
 */
public class RouteConfigPropertiesTest extends BaseGatewayTest {

    @Test
    public void testPropertiesGettersAndSetters() {
        RouteConfig route = RouteConfig.builder()
                .path("/api/test")
                .method("GET")
                .target("http://localhost:8080/users")
                .build();

        // 测试设置和获取 timeout
        route.setTimeout(60000);
        assertEquals(60000, route.getTimeout());

        // 测试设置和获取 retryCount
        route.setRetryCount(5);
        assertEquals(5, route.getRetryCount());

        // 验证 properties 已正确设置
        assertNotNull(route.getProperties());
        assertEquals(60000L, route.getProperties().get("timeout"));
        assertEquals(5, route.getProperties().get("retryCount"));
    }

    @Test
    public void testDefaultValues() {
        RouteConfig route = RouteConfig.builder()
                .path("/api/test")
                .method("GET")
                .target("http://localhost:8080/users")
                .build();

        // 测试默认值
        assertEquals(30000L, route.getTimeout());
        assertEquals(3, route.getRetryCount());
    }

    @Test
    public void testCustomProperties() {
        RouteConfig route = RouteConfig.builder()
                .path("/api/test")
                .method("GET")
                .target("http://localhost:8080/users")
                .build();

        // 设置自定义属性
        route.setProperty("maxConnections", 100);
        route.setProperty("keepAlive", true);
        route.setProperty("customHeader", "MyApp/1.0");

        // 验证属性
        assertEquals(100, route.getProperty("maxConnections"));
        assertEquals(true, route.getProperty("keepAlive"));
        assertEquals("MyApp/1.0", route.getProperty("customHeader"));
    }

    @Test
    public void testPropertiesFromMap() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("timeout", 45000L);
        properties.put("retryCount", 2);
        properties.put("enableCache", true);

        RouteConfig route = RouteConfig.builder()
                .path("/api/test")
                .method("GET")
                .target("http://localhost:8080/users")
                .properties(properties)
                .build();

        assertEquals(45000L, route.getTimeout());
        assertEquals(2, route.getRetryCount());
        assertEquals(true, route.getProperty("enableCache"));
    }

    @Test
    public void testPropertiesStringParsing() {
        // 测试不同数据类型的解析
        Map<String, Object> properties = new HashMap<>();
        properties.put("timeout", "60000");  // 字符串数字
        properties.put("retryCount", "5");   // 字符串数字
        properties.put("enabled", "true");   // 字符串布尔值
        properties.put("rate", "1.5");       // 字符串小数

        RouteConfig route = RouteConfig.builder()
                .path("/api/test")
                .method("GET")
                .target("http://localhost:8080/users")
                .properties(properties)
                .build();

        // 验证数字字符串被正确解析
        assertEquals(60000L, route.getTimeout());
        assertEquals(5, route.getRetryCount());
    }

    @Test
    public void testUltraSimplifiedConfig() {
        // 测试最简化的配置创建
        RouteConfig route = RouteConfig.builder()
                .path("/api/users")
                .method("GET")
                .target("http://localhost:8080/users")
                .enabled(true)
                .build();

        // 设置扩展属性
        route.setTimeout(60000);
        route.setRetryCount(5);
        route.setProperty("maxConnections", 50);
        route.setProperty("compression", true);

        // 生成ID和解析target

        // 验证所有字段都正确设置
        assertNotNull(route.getRouteId());
        assertNotNull(route.getRouteName());
        assertEquals("/api/users", route.getPath());
        assertEquals("GET", route.getMethod());
        assertEquals("http://localhost:8080/users", route.getTarget());
        assertEquals("HTTP", route.getProtocol());
        assertTrue(route.isEnabled());
        assertEquals(60000L, route.getTimeout());
        assertEquals(5, route.getRetryCount());
        assertEquals(50, route.getProperty("maxConnections"));
        assertEquals(true, route.getProperty("compression"));
    }
}

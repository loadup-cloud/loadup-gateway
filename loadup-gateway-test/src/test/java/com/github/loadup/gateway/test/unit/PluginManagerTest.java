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

import com.github.loadup.gateway.core.plugin.PluginManager;
import com.github.loadup.gateway.facade.model.GatewayRequest;
import com.github.loadup.gateway.facade.model.GatewayResponse;
import com.github.loadup.gateway.facade.model.RouteConfig;
import com.github.loadup.gateway.facade.spi.ProxyPlugin;
import com.github.loadup.gateway.facade.constants.GatewayConstants;
import com.github.loadup.gateway.test.BaseGatewayTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 插件管理器单元测试
 */
@DisplayName("插件管理器测试")
public class PluginManagerTest extends BaseGatewayTest {

    @Mock
    private ProxyPlugin httpPlugin;

    @Mock
    private ProxyPlugin beanPlugin;

    @Mock
    private ProxyPlugin rpcPlugin;

    private PluginManager pluginManager;

    @BeforeEach
    public void setUp() {
        super.setUp();
        MockitoAnnotations.openMocks(this);

        // 配置mock插件
        when(httpPlugin.getSupportedProtocol()).thenReturn(GatewayConstants.Protocol.HTTP);
        when(beanPlugin.getSupportedProtocol()).thenReturn(GatewayConstants.Protocol.BEAN);
        when(rpcPlugin.getSupportedProtocol()).thenReturn(GatewayConstants.Protocol.RPC);

        pluginManager = new PluginManager();

        // 使用反射注入mock插件列表
        try {
            var field = PluginManager.class.getDeclaredField("proxyPlugins");
            field.setAccessible(true);
            field.set(pluginManager, Arrays.asList(httpPlugin, beanPlugin, rpcPlugin));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("应该能够执行HTTP代理")
    public void shouldExecuteHttpProxy() throws Exception {
        // Given
        GatewayRequest request = createHttpRequest("/api/test", "GET", null);
        RouteConfig route = createTestRoute("/api/test", "GET",
                "http://localhost:8080/api/test");

        GatewayResponse expectedResponse = GatewayResponse.builder()
                .requestId(testRequestId)
                .statusCode(200)
                .body("{\"result\":\"success\"}")
                .build();

        when(httpPlugin.proxy(request, "http://localhost:8080/api/test"))
                .thenReturn(expectedResponse);

        // When
        GatewayResponse result = pluginManager.executeProxy(request, route);

        // Then
        assertNotNull(result);
        assertEquals(200, result.getStatusCode());
        assertEquals("{\"result\":\"success\"}", result.getBody());
        verify(httpPlugin).proxy(request, "http://localhost:8080/api/test");
    }

    @Test
    @DisplayName("应该能够执行Bean代理")
    public void shouldExecuteBeanProxy() throws Exception {
        // Given
        GatewayRequest request = createHttpRequest("/api/test", "POST", "{\"data\":\"test\"}");
        RouteConfig route = createTestRoute("/api/test", "POST",
                "bean://testService:getData");

        // 目标配置现在会自动解析，无需手动调用 parseTarget()

        GatewayResponse expectedResponse = GatewayResponse.builder()
                .requestId(testRequestId)
                .statusCode(200)
                .body("{\"data\":\"from bean\"}")
                .build();

        when(beanPlugin.proxy(request, "testService:getData"))
                .thenReturn(expectedResponse);

        // When
        GatewayResponse result = pluginManager.executeProxy(request, route);

        // Then
        assertNotNull(result);
        assertEquals(200, result.getStatusCode());
        assertEquals("{\"data\":\"from bean\"}", result.getBody());
        verify(beanPlugin).proxy(request, "testService:getData");
    }

    @Test
    @DisplayName("应该抛出异常当找不到对应协议的插件时")
    public void shouldThrowExceptionWhenNoPluginFound() {
        // Given
        GatewayRequest request = createHttpRequest("/api/test", "GET", null);
        RouteConfig route = createTestRoute("/api/test", "GET", "unknown");

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            pluginManager.executeProxy(request, route);
        });

        assertTrue(exception.getMessage().contains("No protocol found!"));
    }

    @Test
    @DisplayName("应该处理插件执行异常")
    public void shouldHandlePluginExecutionException() throws Exception {
        // Given
        GatewayRequest request = createHttpRequest("/api/test", "GET", null);
        RouteConfig route = createTestRoute("/api/test", "GET",
                "http://localhost:8080/api/test");

        // 目标配置现在会自动解析，无需手动调用 parseTarget()

        when(httpPlugin.proxy(request, "http://localhost:8080/api/test"))
                .thenThrow(new RuntimeException("Network error"));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            pluginManager.executeProxy(request, route);
        });

        assertTrue(exception.getMessage().contains("Network error"));
    }
}

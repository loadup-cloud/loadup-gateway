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
import com.github.loadup.gateway.facade.constants.GatewayConstants;
import com.github.loadup.gateway.facade.model.GatewayRequest;
import com.github.loadup.gateway.facade.model.GatewayResponse;
import com.github.loadup.gateway.facade.model.RouteConfig;
import com.github.loadup.gateway.facade.spi.ProxyPlugin;
import com.github.loadup.gateway.test.BaseGatewayTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * PluginManager unit tests
 */
@DisplayName("PluginManager tests")
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

        // Configure mock plugins
        when(httpPlugin.getSupportedProtocol()).thenReturn(GatewayConstants.Protocol.HTTP);
        when(beanPlugin.getSupportedProtocol()).thenReturn(GatewayConstants.Protocol.BEAN);
        when(rpcPlugin.getSupportedProtocol()).thenReturn(GatewayConstants.Protocol.RPC);

        pluginManager = new PluginManager();

        // Inject mock plugin list via reflection
        try {
            var field = PluginManager.class.getDeclaredField("proxyPlugins");
            field.setAccessible(true);
            field.set(pluginManager, Arrays.asList(httpPlugin, beanPlugin, rpcPlugin));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("Should execute HTTP proxy")
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
    @DisplayName("Should execute Bean proxy")
    public void shouldExecuteBeanProxy() throws Exception {
        // Given
        GatewayRequest request = createHttpRequest("/api/test", "POST", "{\"data\":\"test\"}");
        RouteConfig route = createTestRoute("/api/test", "POST",
                "bean://testService:getData");

        // Target parsing is now automatic; no need to manually call parseTarget()

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
    @DisplayName("Should throw when no plugin found for protocol")
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
    @DisplayName("Should handle plugin execution exception")
    public void shouldHandlePluginExecutionException() throws Exception {
        // Given
        GatewayRequest request = createHttpRequest("/api/test", "GET", null);
        RouteConfig route = createTestRoute("/api/test", "GET",
                "http://localhost:8080/api/test");

        // Target parsing is now automatic; no need to manually call parseTarget()

        when(httpPlugin.proxy(request, "http://localhost:8080/api/test"))
                .thenThrow(new RuntimeException("Network error"));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            pluginManager.executeProxy(request, route);
        });

        assertTrue(exception.getMessage().contains("Network error"));
    }
}

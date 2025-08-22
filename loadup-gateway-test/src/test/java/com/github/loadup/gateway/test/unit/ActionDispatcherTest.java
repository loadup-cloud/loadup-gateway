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

import com.github.loadup.gateway.core.action.ActionDispatcher;
import com.github.loadup.gateway.core.router.RouteResolver;
import com.github.loadup.gateway.core.template.TemplateEngine;
import com.github.loadup.gateway.core.plugin.PluginManager;
import com.github.loadup.gateway.facade.model.GatewayRequest;
import com.github.loadup.gateway.facade.model.GatewayResponse;
import com.github.loadup.gateway.facade.model.RouteConfig;
import com.github.loadup.gateway.facade.constants.GatewayConstants;
import com.github.loadup.gateway.test.BaseGatewayTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Action分发器单元测试
 */
@DisplayName("Action分发器测试")
public class ActionDispatcherTest extends BaseGatewayTest {

    @Mock
    private RouteResolver routeResolver;

    @Mock
    private TemplateEngine templateEngine;

    @Mock
    private PluginManager pluginManager;

    private ActionDispatcher actionDispatcher;

    @BeforeEach
    public void setUp() {
        super.setUp();
        MockitoAnnotations.openMocks(this);

        actionDispatcher = new ActionDispatcher();

        // 使用反射注入mock对象
        try {
            var routeResolverField = ActionDispatcher.class.getDeclaredField("routeResolver");
            routeResolverField.setAccessible(true);
            routeResolverField.set(actionDispatcher, routeResolver);

            var templateEngineField = ActionDispatcher.class.getDeclaredField("templateEngine");
            templateEngineField.setAccessible(true);
            templateEngineField.set(actionDispatcher, templateEngine);

            var pluginManagerField = ActionDispatcher.class.getDeclaredField("pluginManager");
            pluginManagerField.setAccessible(true);
            pluginManagerField.set(actionDispatcher, pluginManager);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("应该成功分发并处理请求")
    public void shouldSuccessfullyDispatchRequest() throws Exception {
        // Given
        GatewayRequest request = createHttpRequest("/api/test", "GET", null);
        RouteConfig route = createTestRoute("/api/test",
                                          GatewayConstants.Protocol.HTTP, "http://localhost:8080");
        GatewayResponse mockResponse = GatewayResponse.builder()
                .requestId(testRequestId)
                .statusCode(200)
                .body("{\"data\":\"success\"}")
                .build();

        when(routeResolver.resolve(request)).thenReturn(Optional.of(route));
        when(templateEngine.processRequestTemplate(request, null)).thenReturn(request);
        when(pluginManager.executeProxy(request, route)).thenReturn(mockResponse);
        when(templateEngine.processResponseTemplate(mockResponse, null)).thenReturn(mockResponse);

        // When
        GatewayResponse result = actionDispatcher.dispatch(request);

        // Then
        assertSuccessResponse(result);
        assertEquals(testRequestId, result.getRequestId());
        assertEquals("{\"data\":\"success\"}", result.getBody());
        assertTrue(result.getProcessingTime() >= 0);

        verify(routeResolver).resolve(request);
        verify(pluginManager).executeProxy(request, route);
    }

    @Test
    @DisplayName("应该返回404当路由不存在时")
    public void shouldReturn404WhenRouteNotFound() {
        // Given
        GatewayRequest request = createHttpRequest("/api/nonexistent", "GET", null);
        when(routeResolver.resolve(request)).thenReturn(Optional.empty());

        // When
        GatewayResponse result = actionDispatcher.dispatch(request);

        // Then
        assertErrorResponse(result, GatewayConstants.Status.NOT_FOUND);
        assertTrue(result.getBody().contains("Route not found"));
        assertEquals(testRequestId, result.getRequestId());

        verify(routeResolver).resolve(request);
        verifyNoInteractions(pluginManager);
    }

    @Test
    @DisplayName("应该处理请求模板")
    public void shouldProcessRequestTemplate() throws Exception {
        // Given
        GatewayRequest request = createHttpRequest("/api/test", "POST", "{\"name\":\"test\"}");
        RouteConfig route = createTestRoute("/api/test",
                                          GatewayConstants.Protocol.HTTP, "http://localhost:8080");
        route.setRequestTemplate("test_request_template");

        GatewayRequest processedRequest = createHttpRequest("/api/test", "POST", "{\"name\":\"test\",\"processed\":true}");
        GatewayResponse mockResponse = GatewayResponse.builder()
                .requestId(testRequestId)
                .statusCode(200)
                .body("{\"result\":\"success\"}")
                .build();

        when(routeResolver.resolve(request)).thenReturn(Optional.of(route));
        when(templateEngine.processRequestTemplate(request, "test_request_template")).thenReturn(processedRequest);
        when(pluginManager.executeProxy(processedRequest, route)).thenReturn(mockResponse);
        when(templateEngine.processResponseTemplate(mockResponse, null)).thenReturn(mockResponse);

        // When
        GatewayResponse result = actionDispatcher.dispatch(request);

        // Then
        assertSuccessResponse(result);
        verify(templateEngine).processRequestTemplate(request, "test_request_template");
        verify(pluginManager).executeProxy(processedRequest, route);
    }

    @Test
    @DisplayName("应该处理响应模板")
    public void shouldProcessResponseTemplate() throws Exception {
        // Given
        GatewayRequest request = createHttpRequest("/api/test", "GET", null);
        RouteConfig route = createTestRoute("/api/test",
                                          GatewayConstants.Protocol.HTTP, "http://localhost:8080");
        route.setResponseTemplate("test_response_template");

        GatewayResponse originalResponse = GatewayResponse.builder()
                .requestId(testRequestId)
                .statusCode(200)
                .body("{\"data\":\"original\"}")
                .build();

        GatewayResponse processedResponse = GatewayResponse.builder()
                .requestId(testRequestId)
                .statusCode(200)
                .body("{\"data\":\"processed\",\"wrapped\":true}")
                .build();

        when(routeResolver.resolve(request)).thenReturn(Optional.of(route));
        when(templateEngine.processRequestTemplate(request, null)).thenReturn(request);
        when(pluginManager.executeProxy(request, route)).thenReturn(originalResponse);
        when(templateEngine.processResponseTemplate(originalResponse, "test_response_template")).thenReturn(processedResponse);

        // When
        GatewayResponse result = actionDispatcher.dispatch(request);

        // Then
        assertSuccessResponse(result);
        assertTrue(result.getBody().contains("processed"));
        verify(templateEngine).processResponseTemplate(originalResponse, "test_response_template");
    }

    @Test
    @DisplayName("应该处理代理执行异常")
    public void shouldHandleProxyExecutionException() throws Exception {
        // Given
        GatewayRequest request = createHttpRequest("/api/test", "GET", null);
        RouteConfig route = createTestRoute("/api/test",
                                          GatewayConstants.Protocol.HTTP, "http://localhost:8080");

        when(routeResolver.resolve(request)).thenReturn(Optional.of(route));
        when(templateEngine.processRequestTemplate(request, null)).thenReturn(request);
        when(pluginManager.executeProxy(request, route)).thenThrow(new RuntimeException("Network error"));

        // When
        GatewayResponse result = actionDispatcher.dispatch(request);

        // Then
        assertErrorResponse(result, GatewayConstants.Status.INTERNAL_ERROR);
        assertTrue(result.getBody().contains("Internal Server Error"));
        assertNotNull(result.getErrorMessage());
        assertTrue(result.getProcessingTime() > 0);
    }
}

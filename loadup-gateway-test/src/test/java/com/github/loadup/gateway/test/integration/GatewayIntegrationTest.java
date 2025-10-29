package com.github.loadup.gateway.test.integration;

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
import com.github.loadup.gateway.facade.constants.GatewayConstants;
import com.github.loadup.gateway.facade.exception.ErrorCode;
import com.github.loadup.gateway.facade.model.GatewayRequest;
import com.github.loadup.gateway.facade.model.GatewayResponse;
import com.github.loadup.gateway.facade.model.RouteConfig;
import com.github.loadup.gateway.facade.spi.RepositoryPlugin;
import com.github.loadup.gateway.test.BaseGatewayTest;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Gateway integration tests
 */
@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.yml")
@DisplayName("Gateway integration tests")
public class GatewayIntegrationTest extends BaseGatewayTest {

    @Resource
    private ActionDispatcher actionDispatcher;

    @MockBean
    private RepositoryPlugin repositoryPlugin;

    @BeforeEach
    public void setUp() {
        super.setUp();
    }

    @Test
    @DisplayName("Complete HTTP proxy flow test")
    public void shouldCompleteHttpProxyFlow() throws Exception {
        // Given
        GatewayRequest request = createHttpRequest("/api/test/http", "GET", null);

        // Create properties including timeout and retryCount
        Map<String, Object> properties = new HashMap<>();
        properties.put(GatewayConstants.PropertyKeys.TIMEOUT, 30000L);
        properties.put(GatewayConstants.PropertyKeys.RETRY_COUNT, 3);

        RouteConfig route = RouteConfig.builder()
                .path("/api/test/http")
                .method("GET")
                .protocol(GatewayConstants.Protocol.HTTP)
                .target("http://httpbin.org/get")
                .enabled(true)
                .properties(properties)
                .build();

        when(repositoryPlugin.getRouteByPath("/api/test/http", "GET"))
                .thenReturn(Optional.of(route));

        // When
        GatewayResponse response = actionDispatcher.dispatch(request);

        // Then
        assertSuccessResponse(response);
        assertEquals(testRequestId, response.getRequestId());
        assertNotNull(response.getBody());
        System.out.println("Response Body: " + response.getProcessingTime());
        assertTrue(response.getProcessingTime() >= 0);
    }

    @Test
    @DisplayName("Complete Bean proxy flow test")
    public void shouldCompleteBeanProxyFlow() throws Exception {
        // Given
        GatewayRequest request = createHttpRequest("/api/test/bean", "POST", "{\"name\":\"test\"}");

        // Create properties including timeout and retryCount
        Map<String, Object> properties = new HashMap<>();
        properties.put(GatewayConstants.PropertyKeys.TIMEOUT, 30000L);
        properties.put(GatewayConstants.PropertyKeys.RETRY_COUNT, 3);

        RouteConfig route = RouteConfig.builder()
                .path("/api/test/bean")
                .method("POST")
                .protocol(GatewayConstants.Protocol.BEAN)
                .target("bean://testService:processData")
                .enabled(true)
                .properties(properties)
                .build();

        when(repositoryPlugin.getRouteByPath("/api/test/bean", "POST"))
                .thenReturn(Optional.of(route));

        // When
        GatewayResponse response = actionDispatcher.dispatch(request);

        // Then
        // Bean may not exist, but there should be a clear error response
        assertNotNull(response);
        assertEquals(testRequestId, response.getRequestId());
        assertTrue(response.getProcessingTime() >= 0);
    }

    @Test
    @DisplayName("Complete flow with templates test")
    public void shouldCompleteFlowWithTemplates() throws Exception {
        // Given
        GatewayRequest request = createHttpRequest("/api/test/template", "POST", "{\"data\":\"test\"}");

        // Create properties including timeout and retryCount
        Map<String, Object> properties = new HashMap<>();
        properties.put(GatewayConstants.PropertyKeys.TIMEOUT, 30000L);
        properties.put(GatewayConstants.PropertyKeys.RETRY_COUNT, 3);

        RouteConfig route = RouteConfig.builder()
                .path("/api/test/template")
                .method("POST")
                .protocol(GatewayConstants.Protocol.HTTP)
                .target("http://httpbin.org/post")
                .requestTemplate("""
                        request.headers.put("X-Custom-Header", "test-value")
                        return request
                        """)
                .responseTemplate("""
                        response.headers.put("X-Response-Processed", "true")
                        return response
                        """)
                .enabled(true)
                .properties(properties)
                .build();

        when(repositoryPlugin.getRouteByPath("/api/test/template", "POST"))
                .thenReturn(Optional.of(route));

        // When
        GatewayResponse response = actionDispatcher.dispatch(request);

        // Then
        assertSuccessResponse(response);
        assertEquals(testRequestId, response.getRequestId());
        assertNotNull(response.getHeaders());
        assertTrue(response.getProcessingTime() > 0);
    }

    @Test
    @DisplayName("Error handling flow test")
    public void shouldHandleErrorFlow() throws Exception {
        // Given
        GatewayRequest request = createHttpRequest("/api/nonexistent", "GET", null);

        when(repositoryPlugin.getRouteByPath("/api/nonexistent", "GET"))
                .thenReturn(Optional.empty());

        // When
        GatewayResponse response = actionDispatcher.dispatch(request);

        // Then
        assertErrorResponse(response, GatewayConstants.Status.NOT_FOUND);
        assertTrue(response.getBody().contains(ErrorCode.ROUTE_NOT_FOUND.getMessage()));
        assertEquals(testRequestId, response.getRequestId());
    }

    @Test
    @DisplayName("Timeout handling test")
    public void shouldHandleTimeout() throws Exception {
        // Given
        GatewayRequest request = createHttpRequest("/api/test/timeout", "GET", null);

        // Create properties including timeout and retryCount
        Map<String, Object> properties = new HashMap<>();
        properties.put(GatewayConstants.PropertyKeys.TIMEOUT, 1000L); // 1 second timeout
        properties.put(GatewayConstants.PropertyKeys.RETRY_COUNT, 1);

        RouteConfig route = RouteConfig.builder()
                .path("/api/test/timeout")
                .method("GET")
                .protocol(GatewayConstants.Protocol.HTTP)
                .target("http://httpbin.org/delay/5") // 5 seconds delay
                .enabled(true)
                .properties(properties)
                .build();

        when(repositoryPlugin.getRouteByPath("/api/test/timeout", "GET"))
                .thenReturn(Optional.of(route));

        // When
        long startTime = System.currentTimeMillis();
        GatewayResponse response = actionDispatcher.dispatch(request);
        long duration = System.currentTimeMillis() - startTime;

        // Then
        assertNotNull(response);
        assertEquals(testRequestId, response.getRequestId());
        // Request should complete within a reasonable time (should not wait 5 seconds)
        assertTrue(duration < 10000, "Request should not take more than 10 seconds");
    }
}

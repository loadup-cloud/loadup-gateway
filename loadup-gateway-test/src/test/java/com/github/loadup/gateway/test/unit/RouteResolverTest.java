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

import com.github.loadup.gateway.core.router.RouteResolver;
import com.github.loadup.gateway.facade.model.GatewayRequest;
import com.github.loadup.gateway.facade.model.RouteConfig;
import com.github.loadup.gateway.facade.spi.RepositoryPlugin;
import com.github.loadup.gateway.test.BaseGatewayTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Route Resolver Unit Test
 */
@DisplayName("Route Resolver Test")
public class RouteResolverTest extends BaseGatewayTest {

    @Mock
    private RepositoryPlugin repositoryPlugin;

    private RouteResolver routeResolver;

    @BeforeEach
    public void setUp() {
        super.setUp();
        MockitoAnnotations.openMocks(this);
        routeResolver = new RouteResolver();
        // Use reflection to injectmockObject
        try {
            var field = RouteResolver.class.getDeclaredField("repositoryPlugin");
            field.setAccessible(true);
            field.set(routeResolver, repositoryPlugin);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("Should be able to resolve existing route")
    public void shouldResolveExistingRoute() throws Exception {
        // Given
        String path = "/api/test";
        String method = "GET";
        GatewayRequest request = createHttpRequest(path, method, null);
        RouteConfig expectedRoute = createTestRoute("/api/test", "GET", "http://localhost:8080/api/test");

        when(repositoryPlugin.getRouteByPath(path, method))
                .thenReturn(Optional.of(expectedRoute));

        // When
        Optional<RouteConfig> result = routeResolver.resolve(request);

        // Then
        assertTrue(result.isPresent());
        assertEquals(expectedRoute.getRouteId(), result.get().getRouteId());
        assertEquals(path, result.get().getPath());
        verify(repositoryPlugin).getRouteByPath(path, method);
    }

    @Test
    @DisplayName("Should return empty when route does not exist")
    public void shouldReturnEmptyWhenRouteNotExists() throws Exception {
        // Given
        String path = "/api/nonexistent";
        String method = "GET";
        GatewayRequest request = createHttpRequest(path, method, null);

        when(repositoryPlugin.getRouteByPath(path, method))
                .thenReturn(Optional.empty());

        // When
        Optional<RouteConfig> result = routeResolver.resolve(request);

        // Then
        assertFalse(result.isPresent());
        verify(repositoryPlugin).getRouteByPath(path, method);
    }

    @Test
    @DisplayName("Should refresh route cache")
    public void shouldRefreshRouteCache() throws Exception {
        // Given
        RouteConfig route1 = createTestRoute("/api/test1", "GET", "http://localhost:8080");
        RouteConfig route2 = createTestRoute("/api/test2", "GET", "testService:getData");

        when(repositoryPlugin.getAllRoutes())
                .thenReturn(Arrays.asList(route1, route2));

        // When
        routeResolver.refreshRoutes();

        // Then
        assertEquals(2, routeResolver.getCachedRouteCount());
        verify(repositoryPlugin).getAllRoutes();
    }

    @Test
    @DisplayName("应该处理仓储异常")
    public void shouldHandleRepositoryException() throws Exception {
        // Given
        String path = "/api/test";
        String method = "GET";
        GatewayRequest request = createHttpRequest(path, method, null);

        when(repositoryPlugin.getRouteByPath(path, method))
                .thenThrow(new RuntimeException("Database error"));

        // When
        Optional<RouteConfig> result = routeResolver.resolve(request);

        // Then
        assertFalse(result.isPresent());
        verify(repositoryPlugin).getRouteByPath(path, method);
    }
}

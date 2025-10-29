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
 * RouteConfig Auto generate ID FunctionalityTest
 */
public class RouteConfigIdGenerationTest extends BaseGatewayTest {

    @Test
    public void testGenerateRouteId() {
        RouteConfig route = RouteConfig.builder()
                .path("/api/test")
                .target("http://localhost:8080/api/user")
                .method("GET")
                .build();


        assertNotNull(route.getRouteId());
        assertTrue(route.getRouteId().startsWith("route-"));
        assertNotNull(route.getRouteName());
    }

    @Test
    public void testGenerateRouteName() {
        RouteConfig route1 = RouteConfig.builder()
                .path("/api/test/user")
                .target("http://localhost:8080/api/user")
                .method("GET")
                .build();

        assertEquals("Api test user (GET)", route1.getRouteName());

        RouteConfig route2 = RouteConfig.builder()
                .path("/api/user/management")
                .target("http://localhost:8080/api/user-management")
                .method("POST")
                .build();

        assertEquals("Api user management (POST)", route2.getRouteName());

        RouteConfig route3 = RouteConfig.builder()
                .path("/")
                .target("http://localhost:8080/")
                .method("GET")
                .build();

        assertEquals("Root (GET)", route3.getRouteName());
    }

    @Test
    public void testUniqueRouteIds() {
        RouteConfig route1 = RouteConfig.builder()
                .path("/api/test")
                .target("http://localhost:8080/api/user")
                .method("GET")
                .build();

        RouteConfig route2 = RouteConfig.builder()
                .path("/api/test")
                .target("http://localhost:8080/api/user")
                .method("POST")
                .build();


        assertNotEquals(route1.getRouteId(), route2.getRouteId());
    }

    @Test
    public void testConsistentRouteIds() {
        RouteConfig route1 = RouteConfig.builder()
                .path("/api/test")
                .target("http://localhost:8080/api/test")
                .method("GET")
                .build();

        RouteConfig route2 = RouteConfig.builder()
                .path("/api/test")
                .target("http://localhost:8080/api/test")
                .method("GET")
                .build();


        // Same path and method should generate sameID
        assertEquals(route1.getRouteId(), route2.getRouteId());
        assertEquals(route1.getRouteName(), route2.getRouteName());
    }

    @Test
    public void testSimplifiedCsvFormat() {
        // Create properties Including extended config
        Map<String, Object> properties = new HashMap<>();
        properties.put("timeout", 5000L);
        properties.put("retryCount", 2);
        properties.put("maxConnections", 50);
        properties.put("compression", true);

        // Test simplified config creation
        RouteConfig route = RouteConfig.builder()
                .path("/api/user")
                .method("GET")
                .target("http://localhost:8080/users")
                .enabled(true)
                .properties(properties)
                .build();

        // GenerateIDand parsingtarget

        // VerifyAll fields are correctly set
        assertNotNull(route.getRouteId());
        assertNotNull(route.getRouteName());
        assertEquals("/api/user", route.getPath());
        assertEquals("GET", route.getMethod());
        assertEquals("http://localhost:8080/users", route.getTarget());
        assertEquals("HTTP", route.getProtocol());
        assertTrue(route.isEnabled());
        assertEquals(5000L, route.getTimeout());
        assertEquals(2, route.getRetryCount());
    }
}

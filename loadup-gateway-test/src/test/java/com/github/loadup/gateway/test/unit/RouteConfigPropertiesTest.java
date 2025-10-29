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
 * RouteConfig properties Field functionalityTest
 */
public class RouteConfigPropertiesTest extends BaseGatewayTest {

    @Test
    public void testPropertiesGettersAndSetters() {
        RouteConfig route = RouteConfig.builder()
                .path("/api/test")
                .method("GET")
                .target("http://localhost:8080/users")
                .build();

        // TestSet andGet timeout
        assertEquals(60000, route.getTimeout());

        // TestSet andGet retryCount
        assertEquals(5, route.getRetryCount());

        // Verify properties Has been correctly set
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

        // TestDefault value
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

        // Set customProperties

        // VerifyProperties
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
    }

    @Test
    public void testPropertiesStringParsing() {
        // TestDifferentDataType ofParse
        Map<String, Object> properties = new HashMap<>();
        properties.put("timeout", "60000");  // String number
        properties.put("retryCount", "5");   // String number
        properties.put("enabled", "true");   // String boolean value
        properties.put("rate", "1.5");       // String decimal

        RouteConfig route = RouteConfig.builder()
                .path("/api/test")
                .method("GET")
                .target("http://localhost:8080/users")
                .properties(properties)
                .build();

        // VerifyNumber string is correctlyParse
        assertEquals(60000L, route.getTimeout());
        assertEquals(5, route.getRetryCount());
    }

    @Test
    public void testUltraSimplifiedConfig() {
        // Test minimal config creation
        RouteConfig route = RouteConfig.builder()
                .path("/api/users")
                .method("GET")
                .target("http://localhost:8080/users")
                .enabled(true)
                .build();

        // Set extended properties

        // GenerateIDand parsingtarget

        // VerifyAll fields are correctly set
        assertNotNull(route.getRouteId());
        assertNotNull(route.getRouteName());
        assertEquals("/api/users", route.getPath());
        assertEquals("GET", route.getMethod());
        assertEquals("http://localhost:8080/users", route.getTarget());
        assertEquals("HTTP", route.getProtocol());
        assertTrue(route.isEnabled());
        assertEquals(60000L, route.getTimeout());
        assertEquals(5, route.getRetryCount());
    }
}

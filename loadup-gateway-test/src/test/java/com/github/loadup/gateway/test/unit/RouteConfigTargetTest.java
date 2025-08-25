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
import com.github.loadup.gateway.facade.model.RouteConfig;
import com.github.loadup.gateway.test.BaseGatewayTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RouteConfig target 字段功能测试
 */
public class RouteConfigTargetTest extends BaseGatewayTest {

    @Test
    public void testParseHttpTarget() {
        RouteConfig route = RouteConfig.builder()

                .path("/api/test").target("http://localhost:8080/api/test")
                .build();


        assertEquals(GatewayConstants.Protocol.HTTP, route.getProtocol());
        assertEquals("http://localhost:8080/api/test", route.getTargetUrl());
        assertNull(route.getTargetBean());
        assertNull(route.getTargetMethod());
    }

    @Test
    public void testParseHttpsTarget() {
        RouteConfig route = RouteConfig.builder()
                .path("/api/test")
                .target("https://api.example.com/v1/users")
                .build();


        assertEquals(GatewayConstants.Protocol.HTTP, route.getProtocol());
        assertEquals("https://api.example.com/v1/users", route.getTargetUrl());
        assertNull(route.getTargetBean());
        assertNull(route.getTargetMethod());
    }

    @Test
    public void testParseBeanTarget() {
        RouteConfig route = RouteConfig.builder()
                .path("/api/test")
                .target("bean://userService:getUser")
                .build();


        assertEquals(GatewayConstants.Protocol.BEAN, route.getProtocol());
        assertEquals("userService", route.getTargetBean());
        assertEquals("getUser", route.getTargetMethod());
        assertNull(route.getTargetUrl());
    }

    @Test
    public void testParseRpcTarget() {
        RouteConfig route = RouteConfig.builder()
                .path("/api/test")
                .target("rpc://com.example.UserService:getUser:1.0.0")
                .build();


        assertEquals(GatewayConstants.Protocol.RPC, route.getProtocol());
        assertEquals("com.example.UserService:getUser:1.0.0", route.getTargetUrl());
        assertNull(route.getTargetBean());
        assertNull(route.getTargetMethod());
    }

    @Test
    public void testGenerateHttpTarget() {
        RouteConfig route = RouteConfig.builder()
                .path("/api/test")
                .target("http://localhost:8080/api/test")
                .build();

        assertEquals("http://localhost:8080/api/test", route.getTarget());
    }

    @Test
    public void testGenerateBeanTarget() {
        RouteConfig route = RouteConfig.builder()
                .path("/api/test")
                .target("bean://userService:getUser")
                .build();

        assertEquals("bean://userService:getUser", route.getTarget());
    }

    @Test
    public void testGenerateRpcTarget() {
        RouteConfig route = RouteConfig.builder()
                .path("/api/test")
                .target("rpc://com.example.UserService:getUser:1.0.0")
                .build();

        assertEquals("rpc://com.example.UserService:getUser:1.0.0", route.getTarget());
    }

    @Test
    public void testParseEmptyTarget() {
        assertThrows(IllegalArgumentException.class
                , () -> {
                    RouteConfig route = RouteConfig.builder()
                            .target("")
                            .path("/api/test")
                            .build();
                }, "target is required and cannot be empty");

    }

    @Test
    public void testGenerateTargetDoesNotOverwriteExisting() {
        RouteConfig route = RouteConfig.builder()
                .target("http://existing.com")
                .path("/api/test")
                .protocol(GatewayConstants.Protocol.HTTP)
                .build();

        route.setTargetUrl("http://localhost:8080/api/test");

        assertEquals("http://existing.com", route.getTarget());
    }

    @Test
    public void testCreateTestRouteWithNewTargetFormat() {
        // 测试 HTTP 协议
        RouteConfig httpRoute = createTestRoute("/api/test", "GET",
                "http://localhost:8080/api/test");
        assertEquals("http://localhost:8080/api/test", httpRoute.getTarget());
        assertEquals(GatewayConstants.Protocol.HTTP, httpRoute.getProtocol());
        assertEquals("http://localhost:8080/api/test", httpRoute.getTargetUrl());

        // 测试 BEAN 协议
        RouteConfig beanRoute = createTestRoute("/api/test", "GET",
                "bean://userService:getUser");
        assertEquals("bean://userService:getUser", beanRoute.getTarget());
        assertEquals(GatewayConstants.Protocol.BEAN, beanRoute.getProtocol());
        assertEquals("userService", beanRoute.getTargetBean());
        assertEquals("getUser", beanRoute.getTargetMethod());

        // 测试 RPC 协议
        RouteConfig rpcRoute = createTestRoute("/api/test", "GET",
                "rpc://com.example.UserService:getUser:1.0.0");
        assertEquals("rpc://com.example.UserService:getUser:1.0.0", rpcRoute.getTarget());
        assertEquals(GatewayConstants.Protocol.RPC, rpcRoute.getProtocol());
        assertEquals("com.example.UserService:getUser:1.0.0", rpcRoute.getTargetUrl());
    }
}

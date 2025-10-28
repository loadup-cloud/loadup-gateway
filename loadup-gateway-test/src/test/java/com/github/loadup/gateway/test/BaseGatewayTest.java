package com.github.loadup.gateway.test;

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

import com.github.loadup.gateway.facade.model.GatewayRequest;
import com.github.loadup.gateway.facade.model.GatewayResponse;
import com.github.loadup.gateway.facade.model.RouteConfig;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 测试基类，提供通用的测试设置和工具方法
 */
@ActiveProfiles("test")
public abstract class BaseGatewayTest {

    protected String testRequestId;

    @BeforeEach
    public void setUp() {
        testRequestId = "test-" + System.currentTimeMillis();
    }

    /**
     * 创建测试HTTP请求
     */
    protected GatewayRequest createHttpRequest(String path, String method, String body) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Accept", "application/json");
        headers.put("User-Agent", "Test-Agent/1.0");

        return GatewayRequest.builder()
                .requestId(testRequestId)
                .path(path)
                .method(method)
                .headers(headers)
                .queryParameters(new HashMap<>())
                .pathParameters(new HashMap<>())
                .body(body)
                .contentType("application/json")
                .clientIp("127.0.0.1")
                .userAgent("Test-Agent/1.0")
                .requestTime(LocalDateTime.now())
                .attributes(new HashMap<>())
                .build();
    }

    /**
     * 创建测试路由配置
     */
    protected RouteConfig createTestRoute(String path, String method, String target) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("timeout", 30000L);
        properties.put("retryCount", 3);

        // 创建 RouteConfig 实例
        RouteConfig route = RouteConfig.builder().path(path)
                .method(method).enabled(true).target(target)
                .requestTemplate("test_request_template")
                .responseTemplate("test_response_template")
                .properties(properties).build();
        return route;
    }

    /**
     * 验证响应基本格式
     */
    protected void assertValidResponse(GatewayResponse response) {
        assertNotNull(response, "Response should not be null");
        assertNotNull(response.getRequestId(), "Request ID should not be null");
        assertTrue(response.getStatusCode() > 0, "Status code should be valid");
        assertNotNull(response.getResponseTime(), "Response time should not be null");
    }

    /**
     * 验证成功响应
     */
    protected void assertSuccessResponse(GatewayResponse response) {
        assertValidResponse(response);
        assertTrue(response.getStatusCode() >= 200 && response.getStatusCode() < 300,
                "Should be success status code, but was: " + response.getStatusCode());
    }

    /**
     * 验证错误响应
     */
    protected void assertErrorResponse(GatewayResponse response, int expectedStatus) {
        assertValidResponse(response);
        assertEquals(expectedStatus, response.getStatusCode(),
                "Expected status " + expectedStatus + ", but was: " + response.getStatusCode());
    }
}

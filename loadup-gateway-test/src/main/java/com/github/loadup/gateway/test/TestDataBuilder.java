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

import com.github.loadup.gateway.facade.constants.GatewayConstants;
import com.github.loadup.gateway.facade.model.GatewayRequest;
import com.github.loadup.gateway.facade.model.GatewayResponse;
import com.github.loadup.gateway.facade.model.RouteConfig;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 测试工具类，提供测试数据构建方法
 */
public class TestDataBuilder {

    /**
     * 创建测试用的GatewayRequest
     */
    public static GatewayRequest createTestRequest(String path, String method) {
        return GatewayRequest.builder()
                .requestId("test-" + System.currentTimeMillis())
                .path(path)
                .method(method)
                .headers(createTestHeaders())
                .queryParameters(new HashMap<>())
                .pathParameters(new HashMap<>())
                .body("{\"test\":\"data\"}")
                .contentType(GatewayConstants.ContentType.JSON)
                .clientIp("127.0.0.1")
                .userAgent("Test-Agent")
                .requestTime(LocalDateTime.now())
                .attributes(new HashMap<>())
                .build();
    }

    /**
     * 创建测试用的GatewayResponse
     */
    public static GatewayResponse createTestResponse(String requestId) {
        return GatewayResponse.builder()
                .requestId(requestId)
                .statusCode(200)
                .headers(createTestHeaders())
                .body("{\"result\":\"success\"}")
                .contentType(GatewayConstants.ContentType.JSON)
                .responseTime(LocalDateTime.now())
                .processingTime(100L)
                .build();
    }

    /**
     * 创建测试用的RouteConfig
     */
    public static RouteConfig createTestRoute(String path, String protocol) {
        return RouteConfig.builder()
                .path(path)
                .method("GET")
                .protocol(protocol)
                .target("http://localhost:8080/api/test")
                .requestTemplate(createTestRequestTemplate())
                .responseTemplate(createTestResponseTemplate())
                .enabled(true)
                .properties(new HashMap<>())
                .build();
    }

    /**
     * 创建测试请求头
     */
    private static Map<String, String> createTestHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Accept", "application/json");
        headers.put("X-Test-Header", "test-value");
        return headers;
    }

    /**
     * 创建测试请求模板
     */
    private static String createTestRequestTemplate() {
        return """
                // 处理请求的Groovy脚本示例
                import com.github.loadup.gateway.facade.model.GatewayRequest
                
                // 修改请求头
                request.headers.put("X-Processed", "true")
                
                // 返回处理后的请求
                return request
                """;
    }

    /**
     * 创建测试响应模板
     */
    private static String createTestResponseTemplate() {
        return """
                // 处理响应的Groovy脚本示例
                import com.github.loadup.gateway.facade.model.GatewayResponse
                
                // 修改响应头
                response.headers.put("X-Gateway-Processed", "true")
                
                // 返回处理后的响应
                return response
                """;
    }
}

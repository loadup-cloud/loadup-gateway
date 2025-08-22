package com.github.loadup.gateway.test.template;

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

import com.github.loadup.gateway.core.template.TemplateEngine;
import com.github.loadup.gateway.facade.model.GatewayRequest;
import com.github.loadup.gateway.facade.model.GatewayResponse;
import com.github.loadup.gateway.facade.utils.JsonUtils;
import com.github.loadup.gateway.test.BaseGatewayTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 模板功能专项测试
 */
@DisplayName("模板功能测试")
public class TemplateTest extends BaseGatewayTest {

    private TemplateEngine templateEngine;

    @BeforeEach
    public void init() {
        templateEngine = new TemplateEngine();
    }

    @Test
    @DisplayName("测试复杂请求模板处理")
    public void shouldProcessComplexRequestTemplate() throws Exception {
        // Given
        GatewayRequest request = createHttpRequest("/api/user", "POST",
            "{\"name\":\"张三\",\"age\":25,\"email\":\"zhangsan@example.com\"}");

        String templateContent = Files.readString(
            Paths.get("src/test/resources/templates/test_request_template.groovy"));

        // When
        GatewayRequest result = templateEngine.processRequestTemplate(request, templateContent);

        // Then
        assertNotNull(result);
        assertEquals("true", result.getHeaders().get("X-Gateway-Processed"));
        assertEquals(testRequestId, result.getHeaders().get("X-Request-Id"));

        // 验证请求体被正确处理
        if (result.getBody() != null) {
            var bodyMap = JsonUtils.toMap(result.getBody());
            assertTrue(bodyMap.containsKey("_system"));
            var systemInfo = (Map<String, Object>) bodyMap.get("_system");
            assertEquals(testRequestId, systemInfo.get("requestId"));
            assertEquals("127.0.0.1", systemInfo.get("clientIp"));
        }
    }

    @Test
    @DisplayName("测试复杂响应模板处理")
    public void shouldProcessComplexResponseTemplate() throws Exception {
        // Given
        GatewayResponse response = GatewayResponse.builder()
                .requestId(testRequestId)
                .statusCode(200)
                .body("{\"userId\":123,\"name\":\"张三\",\"status\":\"active\"}")
                .processingTime(150L)
                .build();

        String templateContent = Files.readString(
            Paths.get("src/test/resources/templates/test_response_template.groovy"));

        // When
        GatewayResponse result = templateEngine.processResponseTemplate(response, templateContent);

        // Then
        assertNotNull(result);
        assertEquals("true", result.getHeaders().get("X-Gateway-Response-Processed"));

        // 验证响应体被统一格式化
        var responseBody = JsonUtils.toMap(result.getBody());
        assertEquals(200, responseBody.get("code"));
        assertEquals("success", responseBody.get("message"));
        assertTrue(responseBody.containsKey("data"));
        assertTrue(responseBody.containsKey("meta"));

        var meta = (Map<String, Object>) responseBody.get("meta");
        assertEquals(testRequestId, meta.get("requestId"));
        assertEquals(150L, ((Number) meta.get("processingTime")).longValue());
    }

    @Test
    @DisplayName("测试数据验证模板")
    public void shouldValidateDataInTemplate() {
        // Given
        GatewayRequest request = createHttpRequest("/api/user", "POST",
            "{\"name\":\"这是一个非常长的名字，超过了50个字符的限制，应该被标记为验证失败\",\"age\":25}");

        String validationTemplate = """
            import com.github.loadup.gateway.facade.utils.JsonUtils
            
            if (request.body != null) {
                def bodyMap = JsonUtils.toMap(request.body)
                
                // 名字长度验证
                if (bodyMap.containsKey("name") && bodyMap.name.length() > 20) {
                    bodyMap.put("_validation", ["nameLength": "too_long"])
                }
                
                // 年龄范围验证
                if (bodyMap.containsKey("age")) {
                    def age = bodyMap.age as Integer
                    if (age < 0 || age > 150) {
                        def validation = bodyMap.get("_validation", [:])
                        validation.put("ageRange", "invalid")
                        bodyMap.put("_validation", validation)
                    }
                }
                
                request.body = JsonUtils.toJson(bodyMap)
            }
            
            return request
            """;

        // When
        GatewayRequest result = templateEngine.processRequestTemplate(request, validationTemplate);

        // Then
        assertNotNull(result);
        var bodyMap = JsonUtils.toMap(result.getBody());
        assertTrue(bodyMap.containsKey("_validation"));
        var validation = (Map<String, String>) bodyMap.get("_validation");
        assertEquals("too_long", validation.get("nameLength"));
    }

    @Test
    @DisplayName("测试错误响应模板")
    public void shouldProcessErrorResponseTemplate() throws Exception {
        // Given
        GatewayResponse errorResponse = GatewayResponse.builder()
                .requestId(testRequestId)
                .statusCode(400)
                .errorMessage("Invalid request parameters")
                .processingTime(50L)
                .build();

        String templateContent = Files.readString(
            Paths.get("src/test/resources/templates/test_response_template.groovy"));

        // When
        GatewayResponse result = templateEngine.processResponseTemplate(errorResponse, templateContent);

        // Then
        assertNotNull(result);
        assertEquals(400, result.getStatusCode());

        var responseBody = JsonUtils.toMap(result.getBody());
        assertEquals(400, responseBody.get("code"));
        assertEquals("Invalid request parameters", responseBody.get("message"));
        assertNull(responseBody.get("data"));

        var meta = (Map<String, Object>) responseBody.get("meta");
        assertEquals(testRequestId, meta.get("requestId"));
    }

    @Test
    @DisplayName("测试模板性能和缓存")
    public void shouldCacheTemplateForPerformance() {
        // Given
        GatewayRequest request = createHttpRequest("/api/test", "GET", null);
        String simpleTemplate = "request.headers.put('X-Cached', 'true'); return request";

        // When - 多次执行相同模板
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < 100; i++) {
            templateEngine.processRequestTemplate(request, simpleTemplate);
        }
        long duration = System.currentTimeMillis() - startTime;

        // Then - 应该很快完成（缓存生效）
        assertTrue(duration < 1000, "Template processing should be fast with caching");

        // 验证结果正确
        GatewayRequest result = templateEngine.processRequestTemplate(request, simpleTemplate);
        assertEquals("true", result.getHeaders().get("X-Cached"));
    }
}

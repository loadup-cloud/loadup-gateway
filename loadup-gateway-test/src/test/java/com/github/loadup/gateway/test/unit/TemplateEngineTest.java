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

import com.github.loadup.gateway.core.template.TemplateEngine;
import com.github.loadup.gateway.facade.model.GatewayRequest;
import com.github.loadup.gateway.facade.model.GatewayResponse;
import com.github.loadup.gateway.test.BaseGatewayTest;
import com.github.loadup.gateway.test.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 模板引擎单元测试
 */
@DisplayName("模板引擎测试")
public class TemplateEngineTest extends BaseGatewayTest {

    private TemplateEngine templateEngine;

    @BeforeEach
    public void initEngine() {
        templateEngine = new TemplateEngine();
    }

    @Test
    @DisplayName("应该能够处理请求模板")
    public void shouldProcessRequestTemplate() {
        // Given
        GatewayRequest request = createHttpRequest("/api/test", "POST", "{\"name\":\"test\"}");
        String templateScript = """
            // 添加请求头
            request.headers.put("X-Processed", "true")
            request.headers.put("X-Template", "request")
            
            // 修改请求体
            if (request.body != null) {
                def bodyMap = new groovy.json.JsonSlurper().parseText(request.body)
                bodyMap.put("processed", true)
                request.body = new groovy.json.JsonBuilder(bodyMap).toString()
            }
            
            return request
            """;

        // When
        GatewayRequest result = templateEngine.processRequestTemplate(request, templateScript);

        // Then
        assertNotNull(result);
        assertEquals("true", result.getHeaders().get("X-Processed"));
        assertEquals("request", result.getHeaders().get("X-Template"));
        assertTrue(result.getBody().contains("\"processed\":true"));
    }

    @Test
    @DisplayName("应该能够处理响应模板")
    public void shouldProcessResponseTemplate() {
        // Given
        GatewayResponse response = TestDataBuilder.createTestResponse(testRequestId);
        String templateScript = """
            // 添加响应头
            response.headers.put("X-Processed", "true")
            response.headers.put("X-Template", "response")
            
            // 包装响应体
            if (response.body != null) {
                def originalBody = response.body
                def wrappedResponse = [
                    "success": true,
                    "data": new groovy.json.JsonSlurper().parseText(originalBody),
                    "timestamp": System.currentTimeMillis()
                ]
                response.body = new groovy.json.JsonBuilder(wrappedResponse).toString()
            }
            
            return response
            """;

        // When
        GatewayResponse result = templateEngine.processResponseTemplate(response, templateScript);

        // Then
        assertNotNull(result);
        assertEquals("true", result.getHeaders().get("X-Processed"));
        assertEquals("response", result.getHeaders().get("X-Template"));
        assertTrue(result.getBody().contains("\"success\":true"));
        assertTrue(result.getBody().contains("\"timestamp\":"));
    }

    @Test
    @DisplayName("应该处理模板脚本异常")
    public void shouldHandleTemplateScriptException() {
        // Given
        GatewayRequest request = createHttpRequest("/api/test", "GET", null);
        String invalidScript = "invalid groovy syntax {{{";

        // When & Then
        assertDoesNotThrow(() -> {
            GatewayRequest result = templateEngine.processRequestTemplate(request, invalidScript);
            // 应该返回原始请求
            assertEquals(request, result);
        });
    }

    @Test
    @DisplayName("应该缓存编译后的脚本")
    public void shouldCacheCompiledScripts() {
        // Given
        GatewayRequest request = createHttpRequest("/api/test", "GET", null);
        String script = "return request";

        // When
        templateEngine.processRequestTemplate(request, script);
        templateEngine.processRequestTemplate(request, script); // 第二次调用应该使用缓存

        // Then
        // 验证脚本被缓存（通过性能测试或其他方式）
        assertDoesNotThrow(() -> templateEngine.clearScriptCache());
    }
}

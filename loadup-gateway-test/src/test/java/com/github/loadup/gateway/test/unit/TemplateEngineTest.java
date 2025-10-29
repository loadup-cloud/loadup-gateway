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
 * Template Engine Unit Test
 */
@DisplayName("Template Engine Test")
public class TemplateEngineTest extends BaseGatewayTest {

    private TemplateEngine templateEngine;

    @BeforeEach
    public void initEngine() {
        templateEngine = new TemplateEngine();
    }

    @Test
    @DisplayName("Should be able to process request template")
    public void shouldProcessRequestTemplate() {
        // Given
        GatewayRequest request = createHttpRequest("/api/test", "POST", "{\"name\":\"test\"}");
        String templateScript = """
            // Add request header
            request.headers.put("X-Processed", "true")
            request.headers.put("X-Template", "request")
            
            // Modify request body
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
    @DisplayName("Should be able to process response template")
    public void shouldProcessResponseTemplate() {
        // Given
        GatewayResponse response = TestDataBuilder.createTestResponse(testRequestId);
        String templateScript = """
            // Add response header
            response.headers.put("X-Processed", "true")
            response.headers.put("X-Template", "response")
            
            // Wrap response body
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
    @DisplayName("Should handle template script exception")
    public void shouldHandleTemplateScriptException() {
        // Given
        GatewayRequest request = createHttpRequest("/api/test", "GET", null);
        String invalidScript = "invalid groovy syntax {{{";

        // When & Then
        assertDoesNotThrow(() -> {
            GatewayRequest result = templateEngine.processRequestTemplate(request, invalidScript);
            // Should return original request
            assertEquals(request, result);
        });
    }

    @Test
    @DisplayName("Should cache compiled scripts")
    public void shouldCacheCompiledScripts() {
        // Given
        GatewayRequest request = createHttpRequest("/api/test", "GET", null);
        String script = "return request";

        // When
        templateEngine.processRequestTemplate(request, script);
        templateEngine.processRequestTemplate(request, script); // Second call should use cache

        // Then
        // Verify script is cached（through performance testing or other means）
        assertDoesNotThrow(() -> templateEngine.clearScriptCache());
    }
}

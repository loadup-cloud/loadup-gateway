// package io.github.loadup.gateway.test.template;
//
/// *-
// * #%L
// * LoadUp Gateway Test
// * %%
// * Copyright (C) 2025 LoadUp Gateway Authors
// * %%
// * This program is free software: you can redistribute it and/or modify
// * it under the terms of the GNU General Public License as
// * published by the Free Software Foundation, either version 3 of the
// * License, or (at your option) any later version.
// *
// * This program is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// * GNU General Public License for more details.
// *
// * You should have received a copy of the GNU General Public
// * License along with this program.  If not, see
// * <http://www.gnu.org/licenses/gpl-3.0.html>.
// * #L%
// */
//
// import io.github.loadup.gateway.core.template.TemplateEngine;
// import io.github.loadup.gateway.test.GatewayTestApplication;
// import com.github.loadup.testify.core.annotation.TestBean;
// import com.github.loadup.testify.core.base.TestifyTestBase;
// import com.github.loadup.testify.core.model.PrepareData;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.test.context.ActiveProfiles;
// import org.testng.annotations.Test;
//
/// **
// * Template functionality specialTest
// */
// @SpringBootTest(classes = GatewayTestApplication.class)
// @ActiveProfiles("test")
// public class TemplateTest extends TestifyTestBase {
//
//    @TestBean
//    @Autowired
//    private TemplateEngine templateEngine;
//
//    @Test(dataProvider = "TestifyProvider")
//    public void processRequestTemplate(String caseId, PrepareData prepareData) {
//        runTest(caseId, prepareData);
//    }
/// *
//    @Test
//    @DisplayName("TestComplex request template processing")
//    public void shouldProcessComplexRequestTemplate() throws Exception {
//        // Given
//        GatewayRequest request = createHttpRequest("/api/user", "POST",
//                "{\"name\":\"Zhang San\",\"age\":25,\"email\":\"zhangsan@example.com\"}");
//
//        String templateContent = Files.readString(
//                Paths.get("src/test/resources/templates/test_request_template.groovy"));
//
//        // When
//        GatewayRequest result = templateEngine.processRequestTemplate(request, templateContent);
//
//        // Then
//        assertNotNull(result);
//        assertEquals("true", result.getHeaders().get("X-Gateway-Processed"));
//        assertEquals(testRequestId, result.getHeaders().get("X-Request-Id"));
//
//        // VerifyRequest body is correctly processed
//        if (result.getBody() != null) {
//            var bodyMap = JsonUtils.toMap(result.getBody());
//            assertTrue(bodyMap.containsKey("_system"));
//            var systemInfo = (Map<String, Object>) bodyMap.get("_system");
//            assertEquals(testRequestId, systemInfo.get("requestId"));
//            assertEquals("127.0.0.1", systemInfo.get("clientIp"));
//        }
//    }
//
//    @Test
//    @DisplayName("Test complex response template processing")
//    public void shouldProcessComplexResponseTemplate() throws Exception {
//        // Given
//        GatewayResponse response = GatewayResponse.builder()
//                .requestId(testRequestId)
//                .statusCode(200)
//                .body("{\"userId\":123,\"name\":\"Zhang San\",\"status\":\"active\"}")
//                .processingTime(150L)
//                .build();
//
//        String templateContent = Files.readString(
//                Paths.get("src/test/resources/templates/test_response_template.groovy"));
//
//        // When
//        GatewayResponse result = templateEngine.processResponseTemplate(response,
// templateContent);
//
//        // Then
//        assertNotNull(result);
//        assertEquals("true", result.getHeaders().get("X-Gateway-Response-Processed"));
//
//        // VerifyResponseBody is unifiedFormatization
//        var responseBody = JsonUtils.toMap(result.getBody());
//        assertEquals(200, responseBody.get("code"));
//        assertEquals("success", responseBody.get("message"));
//        assertTrue(responseBody.containsKey("data"));
//        assertTrue(responseBody.containsKey("meta"));
//
//        var meta = (Map<String, Object>) responseBody.get("meta");
//        assertEquals(testRequestId, meta.get("requestId"));
//        assertEquals(150L, ((Number) meta.get("processingTime")).longValue());
//    }
//
//    @Test
//    @DisplayName("Test dataVerifyTemplate")
//    public void shouldValidateDataInTemplate() {
//        // Given
//        GatewayRequest request = createHttpRequest("/api/user", "POST",
//                "{\"name\":\"This is a very long name，Exceeded50character limit，Should be marked
// asVerifyFailed\",\"age\":25}");
//
//        String validationTemplate = """
//                import io.github.loadup.gateway.facade.utils.JsonUtils
//
//                if (request.body != null) {
//                    def bodyMap = JsonUtils.toMap(request.body)
//
//                    // Name lengthVerify
//                    if (bodyMap.containsKey("name") && bodyMap.name.length() > 20) {
//                        bodyMap.put("_validation", ["nameLength": "too_long"])
//                    }
//
//                    // Age rangeVerify
//                    if (bodyMap.containsKey("age")) {
//                        def age = bodyMap.age as Integer
//                        if (age < 0 || age > 150) {
//                            def validation = bodyMap.get("_validation", [:])
//                            validation.put("ageRange", "invalid")
//                            bodyMap.put("_validation", validation)
//                        }
//                    }
//
//                    request.body = JsonUtils.toJson(bodyMap)
//                }
//
//                return request
//                """;
//
//        // When
//        GatewayRequest result = templateEngine.processRequestTemplate(request,
// validationTemplate);
//
//        // Then
//        assertNotNull(result);
//        var bodyMap = JsonUtils.toMap(result.getBody());
//        assertTrue(bodyMap.containsKey("_validation"));
//        var validation = (Map<String, String>) bodyMap.get("_validation");
//        assertEquals("too_long", validation.get("nameLength"));
//    }
//
//    @Test
//    @DisplayName("TestErrorResponseTemplate")
//    public void shouldProcessErrorResponseTemplate() throws Exception {
//        // Given
//        GatewayResponse errorResponse = GatewayResponse.builder()
//                .requestId(testRequestId)
//                .statusCode(400)
//                .errorMessage("Invalid request parameters")
//                .processingTime(50L)
//                .build();
//
//        String templateContent = Files.readString(
//                Paths.get("src/test/resources/templates/test_response_template.groovy"));
//
//        // When
//        GatewayResponse result = templateEngine.processResponseTemplate(errorResponse,
// templateContent);
//
//        // Then
//        assertNotNull(result);
//        assertEquals(400, result.getStatusCode());
//
//        var responseBody = JsonUtils.toMap(result.getBody());
//        assertEquals(400, responseBody.get("code"));
//        assertEquals("Invalid request parameters", responseBody.get("message"));
//        assertNull(responseBody.get("data"));
//
//        var meta = (Map<String, Object>) responseBody.get("meta");
//        assertEquals(testRequestId, meta.get("requestId"));
//    }
//
//    @Test
//    @DisplayName("Test templatePerformance and caching")
//    public void shouldCacheTemplateForPerformance() {
//        // Given
//        GatewayRequest request = createHttpRequest("/api/test", "GET", null);
//        String simpleTemplate = "request.headers.put('X-Cached', 'true'); return request";
//
//        // When - Execute same template multiple times
//        long startTime = System.currentTimeMillis();
//        for (int i = 0; i < 100; i++) {
//            templateEngine.processRequestTemplate(request, simpleTemplate);
//        }
//        long duration = System.currentTimeMillis() - startTime;
//
//        // Then - Should complete quickly（Cache takes effect）
//        assertTrue(duration < 1000, "Template processing should be fast with caching");
//
//        // VerifyResult correct
//        GatewayRequest result = templateEngine.processRequestTemplate(request, simpleTemplate);
//        assertEquals("true", result.getHeaders().get("X-Cached"));
//    }*/
// }

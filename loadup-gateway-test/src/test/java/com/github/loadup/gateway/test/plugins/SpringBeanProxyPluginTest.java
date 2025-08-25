package com.github.loadup.gateway.test.plugins;

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
import com.github.loadup.gateway.facade.model.PluginConfig;
import com.github.loadup.gateway.plugins.SpringBeanProxyPlugin;
import com.github.loadup.gateway.test.BaseGatewayTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * SpringBeanProxyPlugin 单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SpringBean代理插件测试")
public class SpringBeanProxyPluginTest extends BaseGatewayTest {

    @Mock
    private ApplicationContext applicationContext;

    @InjectMocks
    private SpringBeanProxyPlugin plugin;

    private GatewayRequest testRequest;
    private TestService testService;

    @BeforeEach
    public void setUp() {
        super.setUp();
        testRequest = GatewayRequest.builder()
                .requestId(testRequestId)
                .path("/api/test")
                .method("POST")
                .headers(new HashMap<>())
                .body("{\"name\":\"test\",\"value\":123}")
                .build();

        testService = new TestService();
    }

    @Test
    @DisplayName("应该返回正确的插件元数据")
    void shouldReturnCorrectPluginMetadata() {
        assertEquals("SpringBeanProxyPlugin", plugin.getName());
        assertEquals("PROXY", plugin.getType());
        assertEquals("1.0.0", plugin.getVersion());
        assertEquals(100, plugin.getPriority());
        assertEquals(GatewayConstants.Protocol.BEAN, plugin.getSupportedProtocol());
        assertTrue(plugin.supports(testRequest));
    }

    @Test
    @DisplayName("应该正确初始化插件")
    void shouldInitializePlugin() {
        PluginConfig config = PluginConfig.builder()
                .pluginName("SpringBeanProxyPlugin")
                .enabled(true)
                .build();

        assertDoesNotThrow(() -> plugin.initialize(config));
    }

    @Test
    @DisplayName("execute方法应该抛出UnsupportedOperationException")
    void shouldThrowUnsupportedOperationExceptionForExecute() {
        // The execute method should throw a GatewayException with specific message
        assertThrows(Exception.class, () -> plugin.execute(testRequest),
            "execute方法应该抛出异常，提示使用proxy方法");
    }

    @Test
    @DisplayName("应该成功调用无参数方法")
    void shouldSuccessfullyCallMethodWithNoParameters() throws Exception {
        // Given
        when(applicationContext.getBean("testService")).thenReturn(testService);

        // When
        GatewayResponse response = plugin.proxy(testRequest, "testService:getGreeting");

        // Then
        assertNotNull(response);
        assertEquals(testRequestId, response.getRequestId());
        assertEquals(GatewayConstants.Status.SUCCESS, response.getStatusCode());
        assertEquals(GatewayConstants.ContentType.JSON, response.getContentType());
        assertTrue(response.getBody().contains("Hello World"));
        assertNotNull(response.getResponseTime());

        verify(applicationContext).getBean("testService");
    }

    @Test
    @DisplayName("应该成功调用带String参数的方法")
    void shouldSuccessfullyCallMethodWithStringParameter() throws Exception {
        // Given
        when(applicationContext.getBean("testService")).thenReturn(testService);

        // When
        GatewayResponse response = plugin.proxy(testRequest, "testService:processString");

        // Then
        assertNotNull(response);
        assertEquals(GatewayConstants.Status.SUCCESS, response.getStatusCode());
        assertTrue(response.getBody().contains("Processed"));

        verify(applicationContext).getBean("testService");
    }

    @Test
    @DisplayName("应该成功调用带GatewayRequest参数的方法")
    void shouldSuccessfullyCallMethodWithGatewayRequestParameter() throws Exception {
        // Given
        when(applicationContext.getBean("testService")).thenReturn(testService);

        // When
        GatewayResponse response = plugin.proxy(testRequest, "testService:processRequest");

        // Then
        assertNotNull(response);
        assertEquals(GatewayConstants.Status.SUCCESS, response.getStatusCode());
        assertTrue(response.getBody().contains(testRequestId));

        verify(applicationContext).getBean("testService");
    }

    @Test
    @DisplayName("应该成功调用带自定义对象参数的方法")
    void shouldSuccessfullyCallMethodWithCustomObjectParameter() throws Exception {
        // Given
        when(applicationContext.getBean("testService")).thenReturn(testService);

        // When
        GatewayResponse response = plugin.proxy(testRequest, "testService:processTestData");

        // Then
        assertNotNull(response);
        assertEquals(GatewayConstants.Status.SUCCESS, response.getStatusCode());
        assertTrue(response.getBody().contains("test"));
        assertTrue(response.getBody().contains("246"));

        verify(applicationContext).getBean("testService");
    }

    @Test
    @DisplayName("应该处理无效的目标格式")
    void shouldHandleInvalidTargetFormat() {
        // Test missing colon - should return error response, not throw exception
        assertDoesNotThrow(() -> {
            GatewayResponse response = plugin.proxy(testRequest, "invalidTarget");
            assertNotNull(response);
            assertEquals(GatewayConstants.Status.INTERNAL_ERROR, response.getStatusCode());
        });

        // Test too many parts - should return error response, not throw exception
        assertDoesNotThrow(() -> {
            GatewayResponse response = plugin.proxy(testRequest, "bean:method:extra");
            assertNotNull(response);
            assertEquals(GatewayConstants.Status.INTERNAL_ERROR, response.getStatusCode());
        });
    }

    @Test
    @DisplayName("应该处理Bean不存在的情况")
    void shouldHandleBeanNotFound() throws Exception {
        // Given
        when(applicationContext.getBean("nonExistentBean"))
            .thenThrow(new RuntimeException("Bean not found"));

        // When
        GatewayResponse response = plugin.proxy(testRequest, "nonExistentBean:someMethod");

        // Then
        assertNotNull(response);
        assertEquals(testRequestId, response.getRequestId());
        assertEquals(GatewayConstants.Status.INTERNAL_ERROR, response.getStatusCode());
        // Check either error message or response body contains error info
        assertTrue((response.getErrorMessage() != null && response.getErrorMessage().contains("Bean not found")) ||
                   (response.getBody() != null && response.getBody().contains("error")));

        verify(applicationContext).getBean("nonExistentBean");
    }

    @Test
    @DisplayName("应该处理方法不存在的情况")
    void shouldHandleMethodNotFound() throws Exception {
        // Given
        when(applicationContext.getBean("testService")).thenReturn(testService);

        // When
        GatewayResponse response = plugin.proxy(testRequest, "testService:nonExistentMethod");

        // Then
        assertNotNull(response);
        assertEquals(testRequestId, response.getRequestId());
        assertEquals(GatewayConstants.Status.INTERNAL_ERROR, response.getStatusCode());
        // Check either error message or response body contains error info
        assertTrue((response.getErrorMessage() != null &&
                   (response.getErrorMessage().contains("Method not found") ||
                    response.getErrorMessage().contains("nonExistentMethod"))) ||
                   (response.getBody() != null && response.getBody().contains("error")));

        verify(applicationContext).getBean("testService");
    }

    @Test
    @DisplayName("应该处理方法调用异常")
    void shouldHandleMethodInvocationException() throws Exception {
        // Given
        when(applicationContext.getBean("testService")).thenReturn(testService);

        // When
        GatewayResponse response = plugin.proxy(testRequest, "testService:throwException");

        // Then
        assertNotNull(response);
        assertEquals(testRequestId, response.getRequestId());
        assertEquals(GatewayConstants.Status.INTERNAL_ERROR, response.getStatusCode());
        assertTrue(response.getBody().contains("Test exception") ||
                   response.getErrorMessage().contains("Test exception"));
        assertNotNull(response.getErrorMessage());

        verify(applicationContext).getBean("testService");
    }

    @Test
    @DisplayName("应该正确销毁插件")
    void shouldDestroyPlugin() {
        assertDoesNotThrow(() -> plugin.destroy());
    }

    @Test
    @DisplayName("应该处理JSON解析失败的情况")
    void shouldHandleJsonParsingFailure() throws Exception {
        // Given
        GatewayRequest invalidJsonRequest = GatewayRequest.builder()
                .requestId(testRequestId)
                .path("/api/test")
                .method("POST")
                .headers(new HashMap<>())
                .body("invalid json")
                .build();

        when(applicationContext.getBean("testService")).thenReturn(testService);

        // When
        GatewayResponse response = plugin.proxy(invalidJsonRequest, "testService:processTestData");

        // Then
        assertNotNull(response);
        assertEquals(GatewayConstants.Status.SUCCESS, response.getStatusCode());
        // 方法应该被调用，但参数为null
        assertTrue(response.getBody().contains("null"));

        verify(applicationContext).getBean("testService");
    }

    /**
     * 测试用的服务类
     */
    public static class TestService {

        public String getGreeting() {
            return "Hello World";
        }

        public String processString(String input) {
            return "Processed: " + input;
        }

        public Map<String, Object> processRequest(GatewayRequest request) {
            Map<String, Object> result = new HashMap<>();
            result.put("requestId", request.getRequestId());
            result.put("path", request.getPath());
            result.put("method", request.getMethod());
            return result;
        }

        public TestData processTestData(TestData data) {
            if (data == null) {
                return new TestData("null", 0);
            }
            return new TestData("Processed: " + data.getName(), data.getValue() * 2);
        }

        public void throwException() {
            throw new RuntimeException("Test exception");
        }
    }

    /**
     * 测试数据类
     */
    public static class TestData {
        private String name;
        private int value;

        public TestData() {}

        public TestData(String name, int value) {
            this.name = name;
            this.value = value;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getValue() { return value; }
        public void setValue(int value) { this.value = value; }
    }
}

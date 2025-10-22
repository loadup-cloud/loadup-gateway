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
import com.github.loadup.gateway.plugins.SpringBeanProxyPlugin;
import com.github.loadup.gateway.test.BaseGatewayTest;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SpringBeanProxyPlugin 集成测试
 */
@SpringBootTest(classes = {SpringBeanProxyPluginIntegrationTest.TestConfiguration.class})
@ActiveProfiles("test")
@DisplayName("SpringBean代理插件集成测试")
public class SpringBeanProxyPluginIntegrationTest extends BaseGatewayTest {

    @Resource
    private SpringBeanProxyPlugin plugin;

    @Resource
    private TestBusinessService testBusinessService;

    private GatewayRequest testRequest;

    @BeforeEach
    public void setUp() {
        super.setUp();
        testRequest = GatewayRequest.builder()
                .requestId(testRequestId)
                .path("/api/business")
                .method("POST")
                .headers(new HashMap<>())
                .body("{\"userId\":\"user123\",\"amount\":100.50,\"currency\":\"USD\"}")
                .build();
    }

    @Test
    @DisplayName("应该成功调用实际的Spring Bean方法")
    void shouldSuccessfullyCallRealSpringBeanMethod() throws Exception {
        // When
        GatewayResponse response = plugin.proxy(testRequest, "testBusinessService:processPayment");

        // Then
        assertNotNull(response);
        assertEquals(testRequestId, response.getRequestId());
        assertEquals(GatewayConstants.Status.SUCCESS, response.getStatusCode());
        assertEquals(GatewayConstants.ContentType.JSON, response.getContentType());

        // 验证响应内容
        assertTrue(response.getBody().contains("PROCESSED"));
        assertTrue(response.getBody().contains("user123"));
        assertTrue(response.getBody().contains("100.5"));
        assertTrue(response.getBody().contains("USD"));
    }

    @Test
    @DisplayName("应该成功调用返回集合类型的方法")
    void shouldSuccessfullyCallMethodReturningCollection() throws Exception {
        // When
        GatewayResponse response = plugin.proxy(testRequest, "testBusinessService:getUserTransactions");

        // Then
        assertNotNull(response);
        assertEquals(GatewayConstants.Status.SUCCESS, response.getStatusCode());

        // 验证返回的是数组格式
        assertTrue(response.getBody().startsWith("["));
        assertTrue(response.getBody().endsWith("]"));
        assertTrue(response.getBody().contains("user123"));
    }

    @Test
    @DisplayName("应该成功调用带复杂参数的方法")
    void shouldSuccessfullyCallMethodWithComplexParameters() throws Exception {
        // Given
        GatewayRequest complexRequest = GatewayRequest.builder()
                .requestId(testRequestId)
                .path("/api/order")
                .method("POST")
                .headers(new HashMap<>())
                .body("{\"productId\":\"prod-123\",\"quantity\":5,\"customerInfo\":{\"name\":\"John Doe\",\"email\":\"john@example.com\"}}")
                .build();

        // When
        GatewayResponse response = plugin.proxy(complexRequest, "testBusinessService:processOrder");

        // Then
        assertNotNull(response);
        assertEquals(GatewayConstants.Status.SUCCESS, response.getStatusCode());
        assertTrue(response.getBody().contains("ORDER_CREATED"));
        assertTrue(response.getBody().contains("prod-123"));
        assertTrue(response.getBody().contains("5"));
    }

    @Test
    @DisplayName("应该正确处理业务异常")
    void shouldHandleBusinessException() throws Exception {
        // Given
        GatewayRequest invalidRequest = GatewayRequest.builder()
                .requestId(testRequestId)
                .path("/api/validate")
                .method("POST")
                .headers(new HashMap<>())
                .body("{\"amount\":-100}")
                .build();

        // When
        GatewayResponse response = plugin.proxy(invalidRequest, "testBusinessService:validatePayment");

        // Then
        assertNotNull(response);
        assertEquals(GatewayConstants.Status.INTERNAL_ERROR, response.getStatusCode());
        assertTrue(response.getBody().contains("error"));
        assertTrue(response.getBody().contains("Invalid amount"));
    }

    @Test
    @DisplayName("应该支持方法重载")
    void shouldSupportMethodOverloading() throws Exception {
        // When - 调用无参数版本
        GatewayResponse response1 = plugin.proxy(testRequest, "testBusinessService:getStatus");

        // Then
        assertEquals(GatewayConstants.Status.SUCCESS, response1.getStatusCode());
        assertTrue(response1.getBody().contains("DEFAULT"));

        // When - 调用带参数版本（由于方法查找机制，会找到第一个匹配的方法）
        GatewayResponse response2 = plugin.proxy(testRequest, "testBusinessService:getStatus");

        // Then
        assertEquals(GatewayConstants.Status.SUCCESS, response2.getStatusCode());
    }

    /**
     * 测试配置类
     */
    @Configuration
    static class TestConfiguration {

        @Bean
        public SpringBeanProxyPlugin springBeanProxyPlugin() {
            return new SpringBeanProxyPlugin();
        }

        @Bean
        public TestBusinessService testBusinessService() {
            return new TestBusinessService();
        }
    }

    /**
     * 测试业务服务类
     */
    public static class TestBusinessService {

        public PaymentResult processPayment(PaymentRequest request) {
            return PaymentResult.builder()
                    .status("PROCESSED")
                    .transactionId("txn-" + System.currentTimeMillis())
                    .userId(request.getUserId())
                    .amount(request.getAmount())
                    .currency(request.getCurrency())
                    .build();
        }

        public java.util.List<Transaction> getUserTransactions(PaymentRequest request) {
            return java.util.Arrays.asList(
                    new Transaction("txn-1", request.getUserId(), 50.0),
                    new Transaction("txn-2", request.getUserId(), 75.0)
            );
        }

        public OrderResult processOrder(OrderRequest request) {
            return OrderResult.builder()
                    .status("ORDER_CREATED")
                    .orderId("order-" + System.currentTimeMillis())
                    .productId(request.getProductId())
                    .quantity(request.getQuantity())
                    .build();
        }

        public void validatePayment(PaymentRequest request) {
            if (request.getAmount() <= 0) {
                throw new IllegalArgumentException("Invalid amount: " + request.getAmount());
            }
        }

        public String getStatus() {
            return "DEFAULT_STATUS";
        }

        public String getStatus(String type) {
            return type + "_STATUS";
        }
    }

    /**
     * 支付请求类
     */
    public static class PaymentRequest {
        private String userId;
        private double amount;
        private String currency;

        public PaymentRequest() {
        }

        // Getters and Setters
        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public double getAmount() {
            return amount;
        }

        public void setAmount(double amount) {
            this.amount = amount;
        }

        public String getCurrency() {
            return currency;
        }

        public void setCurrency(String currency) {
            this.currency = currency;
        }
    }

    /**
     * 支付结果类
     */
    public static class PaymentResult {
        private String status;
        private String transactionId;
        private String userId;
        private double amount;
        private String currency;

        public PaymentResult() {
        }

        public static PaymentResultBuilder builder() {
            return new PaymentResultBuilder();
        }

        // Getters and Setters
        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getTransactionId() {
            return transactionId;
        }

        public void setTransactionId(String transactionId) {
            this.transactionId = transactionId;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public double getAmount() {
            return amount;
        }

        public void setAmount(double amount) {
            this.amount = amount;
        }

        public String getCurrency() {
            return currency;
        }

        public void setCurrency(String currency) {
            this.currency = currency;
        }

        public static class PaymentResultBuilder {
            private PaymentResult result = new PaymentResult();

            public PaymentResultBuilder status(String status) {
                result.setStatus(status);
                return this;
            }

            public PaymentResultBuilder transactionId(String transactionId) {
                result.setTransactionId(transactionId);
                return this;
            }

            public PaymentResultBuilder userId(String userId) {
                result.setUserId(userId);
                return this;
            }

            public PaymentResultBuilder amount(double amount) {
                result.setAmount(amount);
                return this;
            }

            public PaymentResultBuilder currency(String currency) {
                result.setCurrency(currency);
                return this;
            }

            public PaymentResult build() {
                return result;
            }
        }
    }

    /**
     * 交易记录类
     */
    public static class Transaction {
        private String transactionId;
        private String userId;
        private double amount;

        public Transaction() {
        }

        public Transaction(String transactionId, String userId, double amount) {
            this.transactionId = transactionId;
            this.userId = userId;
            this.amount = amount;
        }

        // Getters and Setters
        public String getTransactionId() {
            return transactionId;
        }

        public void setTransactionId(String transactionId) {
            this.transactionId = transactionId;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public double getAmount() {
            return amount;
        }

        public void setAmount(double amount) {
            this.amount = amount;
        }
    }

    /**
     * 订单请求类
     */
    public static class OrderRequest {
        private String productId;
        private int quantity;
        private CustomerInfo customerInfo;

        public OrderRequest() {
        }

        // Getters and Setters
        public String getProductId() {
            return productId;
        }

        public void setProductId(String productId) {
            this.productId = productId;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }

        public CustomerInfo getCustomerInfo() {
            return customerInfo;
        }

        public void setCustomerInfo(CustomerInfo customerInfo) {
            this.customerInfo = customerInfo;
        }

        public static class CustomerInfo {
            private String name;
            private String email;

            public CustomerInfo() {
            }

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public String getEmail() {
                return email;
            }

            public void setEmail(String email) {
                this.email = email;
            }
        }
    }

    /**
     * 订单结果类
     */
    public static class OrderResult {
        private String status;
        private String orderId;
        private String productId;
        private int quantity;

        public OrderResult() {
        }

        public static OrderResultBuilder builder() {
            return new OrderResultBuilder();
        }

        // Getters and Setters
        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getOrderId() {
            return orderId;
        }

        public void setOrderId(String orderId) {
            this.orderId = orderId;
        }

        public String getProductId() {
            return productId;
        }

        public void setProductId(String productId) {
            this.productId = productId;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }

        public static class OrderResultBuilder {
            private OrderResult result = new OrderResult();

            public OrderResultBuilder status(String status) {
                result.setStatus(status);
                return this;
            }

            public OrderResultBuilder orderId(String orderId) {
                result.setOrderId(orderId);
                return this;
            }

            public OrderResultBuilder productId(String productId) {
                result.setProductId(productId);
                return this;
            }

            public OrderResultBuilder quantity(int quantity) {
                result.setQuantity(quantity);
                return this;
            }

            public OrderResult build() {
                return result;
            }
        }
    }
}

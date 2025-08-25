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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SpringBeanProxyPlugin 性能测试
 */
@SpringBootTest(classes = {SpringBeanProxyPluginPerformanceTest.TestConfiguration.class})
@ActiveProfiles("test")
@DisplayName("SpringBean代理插件性能测试")
public class SpringBeanProxyPluginPerformanceTest extends BaseGatewayTest {

    @Autowired
    private SpringBeanProxyPlugin plugin;

    @Autowired
    private PerformanceTestService performanceTestService;

    private ExecutorService executorService;

    @BeforeEach
    public void setUp() {
        super.setUp();
        executorService = Executors.newFixedThreadPool(10);
    }

    @Test
    @DisplayName("应该在高并发场景下正常工作")
    void shouldWorkUnderHighConcurrency() throws Exception {
        // Given
        int threadCount = 50;
        int requestsPerThread = 20;
        int totalRequests = threadCount * requestsPerThread;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);
        List<Future<List<GatewayResponse>>> futures = new ArrayList<>();

        // When
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            Future<List<GatewayResponse>> future = executorService.submit(() -> {
                List<GatewayResponse> responses = new ArrayList<>();
                try {
                    startLatch.await(); // 等待所有线程就绪

                    for (int j = 0; j < requestsPerThread; j++) {
                        GatewayRequest request = GatewayRequest.builder()
                                .requestId("req-" + threadId + "-" + j)
                                .path("/api/performance")
                                .method("POST")
                                .headers(new HashMap<>())
                                .body("{\"threadId\":" + threadId + ",\"requestId\":" + j + "}")
                                .build();

                        GatewayResponse response = plugin.proxy(request, "performanceTestService:processData");
                        responses.add(response);
                    }
                } catch (Exception e) {
                    fail("Thread " + threadId + " failed: " + e.getMessage());
                } finally {
                    endLatch.countDown();
                }
                return responses;
            });
            futures.add(future);
        }

        long startTime = System.currentTimeMillis();
        startLatch.countDown(); // 启动所有线程

        boolean completed = endLatch.await(30, TimeUnit.SECONDS);
        long endTime = System.currentTimeMillis();

        // Then
        assertTrue(completed, "所有线程应该在30秒内完成");

        int successCount = 0;
        for (Future<List<GatewayResponse>> future : futures) {
            List<GatewayResponse> responses = future.get();
            for (GatewayResponse response : responses) {
                if (response.getStatusCode() == GatewayConstants.Status.SUCCESS) {
                    successCount++;
                }
            }
        }

        assertEquals(totalRequests, successCount, "所有请求都应该成功");

        long totalTime = endTime - startTime;
        double throughput = (double) totalRequests / totalTime * 1000; // requests per second

        System.out.println("并发性能测试结果:");
        System.out.println("线程数: " + threadCount);
        System.out.println("每线程请求数: " + requestsPerThread);
        System.out.println("总请求数: " + totalRequests);
        System.out.println("总耗时: " + totalTime + " ms");
        System.out.println("吞吐量: " + String.format("%.2f", throughput) + " req/s");

        // 验证性能指标
        assertTrue(throughput > 100, "吞吐量应该大于100 req/s，实际: " + throughput);
        assertTrue(totalTime < 20000, "总耗时应该小于20秒，实际: " + totalTime + "ms");
    }

    @Test
    @DisplayName("应该正确处理内存使用")
    void shouldHandleMemoryUsageProperly() throws Exception {
        // Given
        Runtime runtime = Runtime.getRuntime();
        long initialMemory = runtime.totalMemory() - runtime.freeMemory();

        // When - 执行大量请求
        for (int i = 0; i < 1000; i++) {
            GatewayRequest request = GatewayRequest.builder()
                    .requestId("memory-test-" + i)
                    .path("/api/memory")
                    .method("POST")
                    .headers(new HashMap<>())
                    .body("{\"data\":\"large data payload for memory test " + "x".repeat(100) + "\"}")
                    .build();

            GatewayResponse response = plugin.proxy(request, "performanceTestService:processLargeData");
            assertEquals(GatewayConstants.Status.SUCCESS, response.getStatusCode());

            // 每100次请求强制垃圾回收
            if (i % 100 == 0) {
                System.gc();
                Thread.sleep(10);
            }
        }

        // Then
        System.gc();
        Thread.sleep(100);
        long finalMemory = runtime.totalMemory() - runtime.freeMemory();
        long memoryIncrease = finalMemory - initialMemory;

        System.out.println("内存使用测试结果:");
        System.out.println("初始内存: " + (initialMemory / 1024) + " KB");
        System.out.println("最终内存: " + (finalMemory / 1024) + " KB");
        System.out.println("内存增长: " + (memoryIncrease / 1024) + " KB");

        // 验证内存增长在合理范围内（小于50MB）
        assertTrue(memoryIncrease < 50 * 1024 * 1024, "内存增长应该小于50MB，实际: " + (memoryIncrease / 1024 / 1024) + "MB");
    }

    @Test
    @DisplayName("应该在单线程场景下有良好性能")
    void shouldHaveGoodPerformanceInSingleThread() throws Exception {
        // Given
        int requestCount = 1000;
        List<Long> responseTimes = new ArrayList<>();

        // When
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < requestCount; i++) {
            GatewayRequest request = GatewayRequest.builder()
                    .requestId("single-thread-" + i)
                    .path("/api/single")
                    .method("GET")
                    .headers(new HashMap<>())
                    .body("{\"index\":" + i + "}")
                    .build();

            long requestStart = System.nanoTime();
            GatewayResponse response = plugin.proxy(request, "performanceTestService:quickProcess");
            long requestEnd = System.nanoTime();

            assertEquals(GatewayConstants.Status.SUCCESS, response.getStatusCode());
            responseTimes.add((requestEnd - requestStart) / 1_000_000); // Convert to milliseconds
        }
        long endTime = System.currentTimeMillis();

        // Then
        long totalTime = endTime - startTime;
        double averageResponseTime = responseTimes.stream().mapToLong(Long::longValue).average().orElse(0.0);
        double throughput = (double) requestCount / totalTime * 1000;

        System.out.println("单线程性能测试结果:");
        System.out.println("总请求数: " + requestCount);
        System.out.println("总耗时: " + totalTime + " ms");
        System.out.println("平均响应时间: " + String.format("%.2f", averageResponseTime) + " ms");
        System.out.println("吞吐量: " + String.format("%.2f", throughput) + " req/s");

        // 验证性能指标
        assertTrue(averageResponseTime < 10, "平均响应时间应该小于10ms，实际: " + averageResponseTime);
        assertTrue(throughput > 200, "吞吐量应该大于200 req/s，实际: " + throughput);
    }

    @Test
    @DisplayName("应该正确处理异常场景的性能")
    void shouldHandleExceptionScenariosPerformance() throws Exception {
        // Given
        int requestCount = 100;
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);

        // When
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < requestCount; i++) {
            GatewayRequest request = GatewayRequest.builder()
                    .requestId("error-test-" + i)
                    .path("/api/error")
                    .method("POST")
                    .headers(new HashMap<>())
                    .body("{\"shouldFail\":" + (i % 2 == 0) + "}")
                    .build();

            try {
                GatewayResponse response = plugin.proxy(request, "performanceTestService:processWithPotentialError");
                if (response.getStatusCode() == GatewayConstants.Status.SUCCESS) {
                    successCount.incrementAndGet();
                } else {
                    errorCount.incrementAndGet();
                }
            } catch (Exception e) {
                errorCount.incrementAndGet();
            }
        }
        long endTime = System.currentTimeMillis();

        // Then
        long totalTime = endTime - startTime;

        System.out.println("异常场景性能测试结果:");
        System.out.println("总请求数: " + requestCount);
        System.out.println("成功请求数: " + successCount.get());
        System.out.println("错误请求数: " + errorCount.get());
        System.out.println("总耗时: " + totalTime + " ms");

        assertEquals(requestCount, successCount.get() + errorCount.get(), "所有请求都应该被处理");
        assertTrue(totalTime < 5000, "即使有异常，处理时间也应该在合理范围内");
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
        public PerformanceTestService performanceTestService() {
            return new PerformanceTestService();
        }
    }

    /**
     * 性能测试服务类
     */
    public static class PerformanceTestService {

        private final AtomicInteger counter = new AtomicInteger(0);

        public ProcessResult processData(ProcessRequest request) {
            // 模拟一些处理时间
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            return new ProcessResult(
                    "processed-" + counter.incrementAndGet(),
                    request.getThreadId(),
                    request.getRequestId(),
                    System.currentTimeMillis()
            );
        }

        public String processLargeData(LargeDataRequest request) {
            // 模拟处理大数据
            String data = request.getData();
            return "Processed large data of length: " + data.length();
        }

        public String quickProcess(QuickRequest request) {
            // 快速处理，不添加延迟
            return "Quick result for index: " + request.getIndex();
        }

        public String processWithPotentialError(ErrorTestRequest request) {
            if (request.isShouldFail()) {
                throw new RuntimeException("Intentional test error");
            }
            return "Success result";
        }
    }

    /**
     * 处理请求类
     */
    public static class ProcessRequest {
        private int threadId;
        private int requestId;

        public ProcessRequest() {}

        public int getThreadId() { return threadId; }
        public void setThreadId(int threadId) { this.threadId = threadId; }
        public int getRequestId() { return requestId; }
        public void setRequestId(int requestId) { this.requestId = requestId; }
    }

    /**
     * 处理结果类
     */
    public static class ProcessResult {
        private String id;
        private int threadId;
        private int requestId;
        private long timestamp;

        public ProcessResult() {}

        public ProcessResult(String id, int threadId, int requestId, long timestamp) {
            this.id = id;
            this.threadId = threadId;
            this.requestId = requestId;
            this.timestamp = timestamp;
        }

        // Getters and Setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public int getThreadId() { return threadId; }
        public void setThreadId(int threadId) { this.threadId = threadId; }
        public int getRequestId() { return requestId; }
        public void setRequestId(int requestId) { this.requestId = requestId; }
        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    }

    /**
     * 大数据请求类
     */
    public static class LargeDataRequest {
        private String data;

        public LargeDataRequest() {}

        public String getData() { return data; }
        public void setData(String data) { this.data = data; }
    }

    /**
     * 快速请求类
     */
    public static class QuickRequest {
        private int index;

        public QuickRequest() {}

        public int getIndex() { return index; }
        public void setIndex(int index) { this.index = index; }
    }

    /**
     * 错误测试请求类
     */
    public static class ErrorTestRequest {
        private boolean shouldFail;

        public ErrorTestRequest() {}

        public boolean isShouldFail() { return shouldFail; }
        public void setShouldFail(boolean shouldFail) { this.shouldFail = shouldFail; }
    }
}

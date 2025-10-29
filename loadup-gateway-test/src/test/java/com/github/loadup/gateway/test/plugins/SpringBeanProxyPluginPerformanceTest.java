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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SpringBeanProxyPlugin PerformanceTest
 */
@SpringBootTest(classes = {SpringBeanProxyPluginPerformanceTest.TestConfiguration.class})
@ActiveProfiles("test")
@DisplayName("SpringBean Proxy Plugin Performance Test")
public class SpringBeanProxyPluginPerformanceTest extends BaseGatewayTest {

    @Resource
    private SpringBeanProxyPlugin plugin;

    @Resource
    private PerformanceTestService performanceTestService;

    private ExecutorService executorService;

    @BeforeEach
    public void setUp() {
        super.setUp();
        executorService = Executors.newFixedThreadPool(10);
    }

    @Test
    @DisplayName("Should work normally in high concurrency scenario")
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
                    startLatch.await(); // Wait for all threads to be ready

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
        startLatch.countDown(); // Start all threads

        boolean completed = endLatch.await(30, TimeUnit.SECONDS);
        long endTime = System.currentTimeMillis();

        // Then
        assertTrue(completed, "All threads should within30completed in seconds");

        int successCount = 0;
        for (Future<List<GatewayResponse>> future : futures) {
            List<GatewayResponse> responses = future.get();
            for (GatewayResponse response : responses) {
                if (response.getStatusCode() == GatewayConstants.Status.SUCCESS) {
                    successCount++;
                }
            }
        }

        assertEquals(totalRequests, successCount, "All requests should succeed");

        long totalTime = endTime - startTime;
        double throughput = (double) totalRequests / totalTime * 1000; // requests per second

        System.out.println("Concurrent performanceTest results:");
        System.out.println("Thread count: " + threadCount);
        System.out.println("Requests per thread: " + requestsPerThread);
        System.out.println("Total requests: " + totalRequests);
        System.out.println("Total time: " + totalTime + " ms");
        System.out.println("Throughput: " + String.format("%.2f", throughput) + " req/s");

        // VerifyPerformance metrics
        assertTrue(throughput > 100, "Throughput should be greater than100 req/s，Actual: " + throughput);
        assertTrue(totalTime < 20000, "Total timeShould be less than20seconds，Actual: " + totalTime + "ms");
    }

    @Test
    @DisplayName("Should correctly handleMemory usage")
    void shouldHandleMemoryUsageProperly() throws Exception {
        // Given
        Runtime runtime = Runtime.getRuntime();
        long initialMemory = runtime.totalMemory() - runtime.freeMemory();

        // When - Execute large number of requests
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

            // Per100RequestsForce garbage collection
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

        System.out.println("Memory usageTest results:");
        System.out.println("Initial memory: " + (initialMemory / 1024) + " KB");
        System.out.println("Final memory: " + (finalMemory / 1024) + " KB");
        System.out.println("Memory growth: " + (memoryIncrease / 1024) + " KB");

        // VerifyMemory growthWithin reasonable range（Less than50MB）
        assertTrue(memoryIncrease < 50 * 1024 * 1024, "Memory growthShould be less than50MB，Actual: " + (memoryIncrease / 1024 / 1024) + "MB");
    }

    @Test
    @DisplayName("Should have good performance in single-thread scenario")
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

        System.out.println("Single thread performanceTest results:");
        System.out.println("Total requests: " + requestCount);
        System.out.println("Total time: " + totalTime + " ms");
        System.out.println("AverageResponseTime: " + String.format("%.2f", averageResponseTime) + " ms");
        System.out.println("Throughput: " + String.format("%.2f", throughput) + " req/s");

        // VerifyPerformance metrics
        assertTrue(averageResponseTime < 10, "AverageResponseTime should be less than10ms，Actual: " + averageResponseTime);
        assertTrue(throughput > 200, "Throughput should be greater than200 req/s，Actual: " + throughput);
    }

    @Test
    @DisplayName("Should correctly handle performance in exception scenario")
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

        System.out.println("Exception scenario performanceTest results:");
        System.out.println("Total requests: " + requestCount);
        System.out.println("Successful requests: " + successCount.get());
        System.out.println("ErrorRequest count: " + errorCount.get());
        System.out.println("Total time: " + totalTime + " ms");

        assertEquals(requestCount, successCount.get() + errorCount.get(), "All requests should be processed");
        assertTrue(totalTime < 5000, "Even with exceptions，ProcessTimeAlsoShould be within reasonable range");
    }

    /**
     * Test Configuration Class
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
     * PerformanceTest serviceClass
     */
    public static class PerformanceTestService {

        private final AtomicInteger counter = new AtomicInteger(0);

        public ProcessResult processData(ProcessRequest request) {
            // Simulate some processingTime
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
            // Simulate processing largeData
            String data = request.getData();
            return "Processed large data of length: " + data.length();
        }

        public String quickProcess(QuickRequest request) {
            // Quick processing，No delay added
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
     * Process request class
     */
    public static class ProcessRequest {
        private int threadId;
        private int requestId;

        public ProcessRequest() {
        }

        public int getThreadId() {
            return threadId;
        }

        public void setThreadId(int threadId) {
            this.threadId = threadId;
        }

        public int getRequestId() {
            return requestId;
        }

        public void setRequestId(int requestId) {
            this.requestId = requestId;
        }
    }

    /**
     * Processing result class
     */
    public static class ProcessResult {
        private String id;
        private int threadId;
        private int requestId;
        private long timestamp;

        public ProcessResult() {
        }

        public ProcessResult(String id, int threadId, int requestId, long timestamp) {
            this.id = id;
            this.threadId = threadId;
            this.requestId = requestId;
            this.timestamp = timestamp;
        }

        // Getters and Setters
        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public int getThreadId() {
            return threadId;
        }

        public void setThreadId(int threadId) {
            this.threadId = threadId;
        }

        public int getRequestId() {
            return requestId;
        }

        public void setRequestId(int requestId) {
            this.requestId = requestId;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }
    }

    /**
     * LargeDataRequest class
     */
    public static class LargeDataRequest {
        private String data;

        public LargeDataRequest() {
        }

        public String getData() {
            return data;
        }

        public void setData(String data) {
            this.data = data;
        }
    }

    /**
     * Quick request class
     */
    public static class QuickRequest {
        private int index;

        public QuickRequest() {
        }

        public int getIndex() {
            return index;
        }

        public void setIndex(int index) {
            this.index = index;
        }
    }

    /**
     * ErrorTestRequest class
     */
    public static class ErrorTestRequest {
        private boolean shouldFail;

        public ErrorTestRequest() {
        }

        public boolean isShouldFail() {
            return shouldFail;
        }

        public void setShouldFail(boolean shouldFail) {
            this.shouldFail = shouldFail;
        }
    }
}

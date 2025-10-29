package com.github.loadup.gateway.test.integration;

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

import com.github.loadup.gateway.core.action.ActionDispatcher;
import com.github.loadup.gateway.facade.model.GatewayRequest;
import com.github.loadup.gateway.facade.model.GatewayResponse;
import com.github.loadup.gateway.test.BaseGatewayTest;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Gateway Performance Test
 */
@SpringBootTest
@DisplayName("Gateway Performance Test")
public class GatewayPerformanceTest extends BaseGatewayTest {

    @Resource
    private ActionDispatcher actionDispatcher;

    @Test
    @DisplayName("Single thread performanceTest")
    public void shouldPerformWellInSingleThread() {
        // Given
        int requestCount = 1000;
        List<Long> responseTimes = new ArrayList<>();

        // When
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < requestCount; i++) {
            long requestStart = System.currentTimeMillis();

            GatewayRequest request = createHttpRequest("/api/nonexistent", "GET", null);
            GatewayResponse response = actionDispatcher.dispatch(request);

            long requestEnd = System.currentTimeMillis();
            responseTimes.add(requestEnd - requestStart);

            // VerifyResponseCorrectness
            assertNotNull(response);
            assertEquals(404, response.getStatusCode());
        }

        long totalTime = System.currentTimeMillis() - startTime;

        // Then
        double averageResponseTime = responseTimes.stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0.0);

        double throughput = (requestCount * 1000.0) / totalTime; // requests per second

        System.out.printf("Single thread performanceTest results:%n");
        System.out.printf("Total requests: %d%n", requestCount);
        System.out.printf("Total time: %d ms%n", totalTime);
        System.out.printf("AverageResponseTime: %.2f ms%n", averageResponseTime);
        System.out.printf("Throughput: %.2f req/s%n", throughput);

        // AssertPerformance metrics
        assertTrue(averageResponseTime < 100, "AverageResponseTime should be less than100ms");
        assertTrue(throughput > 10, "Throughput should be greater than10 req/s");
    }

    @Test
    @DisplayName("Concurrent performanceTest")
    public void shouldPerformWellUnderConcurrency() throws Exception {
        // Given
        int threadCount = 10;
        int requestsPerThread = 100;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        List<CompletableFuture<List<Long>>> futures = new ArrayList<>();

        // When
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < threadCount; i++) {
            CompletableFuture<List<Long>> future = CompletableFuture.supplyAsync(() -> {
                List<Long> threadResponseTimes = new ArrayList<>();

                for (int j = 0; j < requestsPerThread; j++) {
                    long requestStart = System.currentTimeMillis();

                    GatewayRequest request = createHttpRequest("/api/test" + j, "GET", null);
                    GatewayResponse response = actionDispatcher.dispatch(request);

                    long requestEnd = System.currentTimeMillis();
                    threadResponseTimes.add(requestEnd - requestStart);

                    assertNotNull(response);
                }

                return threadResponseTimes;
            }, executor);

            futures.add(future);
        }

        // Wait for all requests to complete
        List<Long> allResponseTimes = new ArrayList<>();
        for (CompletableFuture<List<Long>> future : futures) {
            allResponseTimes.addAll(future.get());
        }

        long totalTime = System.currentTimeMillis() - startTime;
        executor.shutdown();

        // Then
        int totalRequests = threadCount * requestsPerThread;
        double averageResponseTime = allResponseTimes.stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0.0);

        double throughput = (totalRequests * 1000.0) / totalTime;

        System.out.printf("Concurrent performanceTest results:%n");
        System.out.printf("Thread count: %d%n", threadCount);
        System.out.printf("Requests per thread: %d%n", requestsPerThread);
        System.out.printf("Total requests: %d%n", totalRequests);
        System.out.printf("Total time: %d ms%n", totalTime);
        System.out.printf("AverageResponseTime: %.2f ms%n", averageResponseTime);
        System.out.printf("Throughput: %.2f req/s%n", throughput);

        // AssertPerformance metrics
        assertTrue(averageResponseTime < 200, "Average in concurrent scenarioResponseTime should be less than200ms");
        assertTrue(throughput > 50, "Concurrent throughput should be greater than50 req/s");
    }

    @RepeatedTest(5)
    @DisplayName("Repeated performanceTest")
    public void shouldMaintainConsistentPerformance() {
        // Given
        int warmupRequests = 50;
        int testRequests = 200;

        // Warmup
        for (int i = 0; i < warmupRequests; i++) {
            GatewayRequest request = createHttpRequest("/api/warmup", "GET", null);
            actionDispatcher.dispatch(request);
        }

        // When - TestPhase
        List<Long> responseTimes = new ArrayList<>();
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < testRequests; i++) {
            long requestStart = System.currentTimeMillis();

            GatewayRequest request = createHttpRequest("/api/test", "GET", null);
            GatewayResponse response = actionDispatcher.dispatch(request);

            long requestEnd = System.currentTimeMillis();
            responseTimes.add(requestEnd - requestStart);

            assertNotNull(response);
        }

        long totalTime = System.currentTimeMillis() - startTime;

        // Then
        double averageResponseTime = responseTimes.stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0.0);

        double throughput = (testRequests * 1000.0) / totalTime;

        // Performance should remain stable
        assertTrue(averageResponseTime < 50, "After warmupResponseTime should be fast");
        assertTrue(throughput > 100, "Throughput after warmup should be high");

        // CheckResponseTime stability（99%requests should complete within reasonable time）
        responseTimes.sort(Long::compareTo);
        long p99ResponseTime = responseTimes.get((int) (responseTimes.size() * 0.99));
        assertTrue(p99ResponseTime < 100, "99%requests should be within100msComplete within");
    }

    @Test
    @DisplayName("Memory usageTest")
    public void shouldNotLeakMemory() {
        // Given
        Runtime runtime = Runtime.getRuntime();
        int requestCount = 1000;

        // Force garbage collection，Get baseline memory
        System.gc();
        long initialMemory = runtime.totalMemory() - runtime.freeMemory();

        // When - Execute large number of requests
        for (int i = 0; i < requestCount; i++) {
            GatewayRequest request = createHttpRequest("/api/memory-test-" + i, "POST",
                    "{\"data\":\"" + "x".repeat(1000) + "\"}"); // 1KBData
            GatewayResponse response = actionDispatcher.dispatch(request);

            assertNotNull(response);

            // Per100requestsCheckOne memory
            if (i % 100 == 0) {
                System.gc(); // Suggest garbage collection
            }
        }

        // Final garbage collection and memoryCheck
        System.gc();
        Thread.yield(); // LetGChave chance to run

        long finalMemory = runtime.totalMemory() - runtime.freeMemory();
        long memoryIncrease = finalMemory - initialMemory;

        // Then
        System.out.printf("Memory usageTest results:%n");
        System.out.printf("Initial memory: %d KB%n", initialMemory / 1024);
        System.out.printf("Final memory: %d KB%n", finalMemory / 1024);
        System.out.printf("Memory growth: %d KB%n", memoryIncrease / 1024);

        // Memory growthShould be within reasonable range（Less than10MB）
        assertTrue(memoryIncrease < 10 * 1024 * 1024,
                "Memory growthShould be less than10MB，Actual growth: " + (memoryIncrease / 1024 / 1024) + "MB");
    }
}

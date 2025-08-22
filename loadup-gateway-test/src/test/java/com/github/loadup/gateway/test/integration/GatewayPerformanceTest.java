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
import com.github.loadup.gateway.facade.model.RouteConfig;
import com.github.loadup.gateway.facade.constants.GatewayConstants;
import com.github.loadup.gateway.test.BaseGatewayTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 网关性能测试
 */
@SpringBootTest
@DisplayName("网关性能测试")
public class GatewayPerformanceTest extends BaseGatewayTest {

    @Autowired
    private ActionDispatcher actionDispatcher;

    @Test
    @DisplayName("单线程性能测试")
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

            // 验证响应正确性
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

        System.out.printf("单线程性能测试结果:%n");
        System.out.printf("总请求数: %d%n", requestCount);
        System.out.printf("总耗时: %d ms%n", totalTime);
        System.out.printf("平均响应时间: %.2f ms%n", averageResponseTime);
        System.out.printf("吞吐量: %.2f req/s%n", throughput);

        // 断言性能指标
        assertTrue(averageResponseTime < 100, "平均响应时间应该小于100ms");
        assertTrue(throughput > 10, "吞吐量应该大于10 req/s");
    }

    @Test
    @DisplayName("并发性能测试")
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

        // 等待所有请求完成
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

        System.out.printf("并发性能测试结果:%n");
        System.out.printf("线程数: %d%n", threadCount);
        System.out.printf("每线程请求数: %d%n", requestsPerThread);
        System.out.printf("总请求数: %d%n", totalRequests);
        System.out.printf("总耗时: %d ms%n", totalTime);
        System.out.printf("平均响应时间: %.2f ms%n", averageResponseTime);
        System.out.printf("吞吐量: %.2f req/s%n", throughput);

        // 断言性能指标
        assertTrue(averageResponseTime < 200, "并发场景下平均响应时间应该小于200ms");
        assertTrue(throughput > 50, "并发吞吐量应该大于50 req/s");
    }

    @RepeatedTest(5)
    @DisplayName("重复性能测试")
    public void shouldMaintainConsistentPerformance() {
        // Given
        int warmupRequests = 50;
        int testRequests = 200;

        // Warmup
        for (int i = 0; i < warmupRequests; i++) {
            GatewayRequest request = createHttpRequest("/api/warmup", "GET", null);
            actionDispatcher.dispatch(request);
        }

        // When - 测试阶段
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

        // 性能应该保持稳定
        assertTrue(averageResponseTime < 50, "预热后响应时间应该很快");
        assertTrue(throughput > 100, "预热后吞吐量应该很高");

        // 检查响应时间的稳定性（99%的请求应该在合理时间内完成）
        responseTimes.sort(Long::compareTo);
        long p99ResponseTime = responseTimes.get((int) (responseTimes.size() * 0.99));
        assertTrue(p99ResponseTime < 100, "99%的请求应该在100ms内完成");
    }

    @Test
    @DisplayName("内存使用测试")
    public void shouldNotLeakMemory() {
        // Given
        Runtime runtime = Runtime.getRuntime();
        int requestCount = 1000;

        // 强制垃圾回收，获取基准内存
        System.gc();
        long initialMemory = runtime.totalMemory() - runtime.freeMemory();

        // When - 执行大量请求
        for (int i = 0; i < requestCount; i++) {
            GatewayRequest request = createHttpRequest("/api/memory-test-" + i, "POST",
                    "{\"data\":\"" + "x".repeat(1000) + "\"}"); // 1KB数据
            GatewayResponse response = actionDispatcher.dispatch(request);

            assertNotNull(response);

            // 每100个请求检查一次内存
            if (i % 100 == 0) {
                System.gc(); // 建议垃圾回收
            }
        }

        // 最终垃圾回收和内存检查
        System.gc();
        Thread.yield(); // 让GC有机会运行

        long finalMemory = runtime.totalMemory() - runtime.freeMemory();
        long memoryIncrease = finalMemory - initialMemory;

        // Then
        System.out.printf("内存使用测试结果:%n");
        System.out.printf("初始内存: %d KB%n", initialMemory / 1024);
        System.out.printf("最终内存: %d KB%n", finalMemory / 1024);
        System.out.printf("内存增长: %d KB%n", memoryIncrease / 1024);

        // 内存增长应该在合理范围内（小于10MB）
        assertTrue(memoryIncrease < 10 * 1024 * 1024,
                "内存增长应该小于10MB，实际增长: " + (memoryIncrease / 1024 / 1024) + "MB");
    }
}

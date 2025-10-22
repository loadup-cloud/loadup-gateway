package com.github.loadup.gateway.core.filter;

/*-
 * #%L
 * LoadUp Gateway Core
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
import com.github.loadup.gateway.facade.utils.CommonUtils;
import com.github.loadup.gateway.facade.utils.JsonUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 网关核心过滤器
 */
@Slf4j
@Component
public class GatewayFilter implements Filter {

    @Resource
    private ActionDispatcher actionDispatcher;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // 生成请求ID
        String requestId = CommonUtils.generateRequestId();

        try {
            // 构建网关请求对象
            GatewayRequest gatewayRequest = buildGatewayRequest(httpRequest, requestId);

            log.info("Gateway processing request: {} {} with ID: {}",
                    gatewayRequest.getMethod(), gatewayRequest.getPath(), requestId);

            // 分发到Action处理器
            GatewayResponse gatewayResponse = actionDispatcher.dispatch(gatewayRequest);

            // 写入响应
            writeResponse(httpResponse, gatewayResponse);

        } catch (Exception e) {
            log.error("Gateway processing failed for request ID: {}", requestId, e);
            handleError(httpResponse, requestId, e);
        }
    }

    /**
     * 构建网关请求对象
     */
    private GatewayRequest buildGatewayRequest(HttpServletRequest request, String requestId)
            throws IOException {

        // 获取请求头
        Map<String, String> headers = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            headers.put(headerName, request.getHeader(headerName));
        }

        // 获取查询参数
        Map<String, List<String>> queryParams = new HashMap<>();
        if (request.getQueryString() != null) {
            Arrays.stream(request.getQueryString().split("&"))
                    .forEach(param -> {
                        String[] kv = param.split("=", 2);
                        if (kv.length == 2) {
                            queryParams.computeIfAbsent(kv[0], k -> new ArrayList<>()).add(kv[1]);
                        }
                    });
        }

        // 读取请求体
        String body = request.getReader().lines().collect(Collectors.joining("\n"));

        return GatewayRequest.builder()
                .requestId(requestId)
                .path(request.getRequestURI())
                .method(request.getMethod())
                .headers(headers)
                .queryParameters(queryParams)
                .body(body)
                .contentType(request.getContentType())
                .clientIp(getClientIp(request))
                .userAgent(request.getHeader("User-Agent"))
                .requestTime(LocalDateTime.now())
                .attributes(new HashMap<>())
                .build();
    }

    /**
     * 写入响应
     */
    private void writeResponse(HttpServletResponse response, GatewayResponse gatewayResponse)
            throws IOException {

        response.setStatus(gatewayResponse.getStatusCode());

        // 设置响应头
        if (gatewayResponse.getHeaders() != null) {
            gatewayResponse.getHeaders().forEach(response::setHeader);
        }

        // 设置内容类型
        if (gatewayResponse.getContentType() != null) {
            response.setContentType(gatewayResponse.getContentType());
        }

        // 写入响应体
        if (gatewayResponse.getBody() != null) {
            response.getWriter().write(gatewayResponse.getBody());
        }

        response.getWriter().flush();
    }

    /**
     * 处理错误
     */
    private void handleError(HttpServletResponse response, String requestId, Exception e)
            throws IOException {

        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        response.setContentType("application/json");

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("requestId", requestId);
        errorResponse.put("error", "Internal Server Error");
        errorResponse.put("message", e.getMessage());
        errorResponse.put("timestamp", LocalDateTime.now());

        response.getWriter().write(JsonUtils.toJson(errorResponse));
        response.getWriter().flush();
    }

    /**
     * 获取客户端IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}

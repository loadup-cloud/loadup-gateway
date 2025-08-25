package com.github.loadup.gateway.core.action;

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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.loadup.gateway.facade.config.GatewayProperties;
import com.github.loadup.gateway.facade.constants.GatewayConstants;
import com.github.loadup.gateway.facade.model.GatewayRequest;
import com.github.loadup.gateway.facade.model.GatewayResponse;
import com.github.loadup.gateway.facade.model.Result;
import com.github.loadup.gateway.facade.model.RouteConfig;
import com.github.loadup.gateway.facade.exception.*;
import com.github.loadup.gateway.core.plugin.PluginManager;
import com.github.loadup.gateway.core.router.RouteResolver;
import com.github.loadup.gateway.core.template.TemplateEngine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Action分发器
 */
@Slf4j
@Component
public class ActionDispatcher {

    @Autowired
    private RouteResolver routeResolver;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private PluginManager pluginManager;

    @Autowired
    private GatewayProperties gatewayProperties;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 分发请求到相应的处理器
     */
    public GatewayResponse dispatch(GatewayRequest request) {
        long startTime = System.currentTimeMillis();

        try {
            // 解析路由
            Optional<RouteConfig> routeOpt = routeResolver.resolve(request);
            if (!routeOpt.isPresent()) {
                // 使用统一异常处理构建404响应
                RouteException routeException = GatewayExceptionFactory.routeNotFound(request.getPath());
                return ExceptionHandler.handleException(request.getRequestId(), routeException,
                    System.currentTimeMillis() - startTime);
            }

            RouteConfig route = routeOpt.get();
            log.debug("Route resolved: {} -> {}", request.getPath(), route.getRouteId());

            // 请求预处理 - 应用请求模板
            GatewayRequest processedRequest = preprocessRequest(request, route);

            // 执行代理转发
            GatewayResponse response = executeProxy(processedRequest, route);

            // 响应后处理 - 应用响应模板
            GatewayResponse processedResponse = postprocessResponse(response, route);

            if (processedResponse.getResponseTime() == null) {
                processedResponse.setResponseTime(LocalDateTime.now());
            }

            // 设置处理时间
            long processingTime = System.currentTimeMillis() - startTime;
            processedResponse.setProcessingTime(processingTime);

            // 统一响应格式处理
            boolean wrap = gatewayProperties.getResponse() != null && gatewayProperties.getResponse().isWrap();
            if (route.getWrapResponse() != null) {
                wrap = route.getWrapResponse();
            }
            if (wrap) {
                processedResponse = wrapGatewayResponse(processedResponse);
            }

            return processedResponse;

        } catch (GatewayException e) {
            // 网关异常直接处理
            return ExceptionHandler.handleException(request.getRequestId(), e,
                System.currentTimeMillis() - startTime);
        } catch (Exception e) {
            // 其他异常包装后处理
            log.error("Request dispatch failed", e);
            GatewayException wrappedException = GatewayExceptionFactory.wrap(e, "DISPATCHER");
            return ExceptionHandler.handleException(request.getRequestId(), wrappedException,
                System.currentTimeMillis() - startTime);
        }
    }

    /**
     * 请求预处理
     */
    private GatewayRequest preprocessRequest(GatewayRequest request, RouteConfig route) {
        if (route.getRequestTemplate() != null) {
            try {
                return templateEngine.processRequestTemplate(request, route.getRequestTemplate());
            } catch (Exception e) {
                log.warn("Request template processing failed", e);
                throw GatewayExceptionFactory.templateExecutionError(route.getRequestTemplate(), e);
            }
        }
        return request;
    }

    /**
     * 执行代理转发
     */
    private GatewayResponse executeProxy(GatewayRequest request, RouteConfig route) {
        try {
            return pluginManager.executeProxy(request, route);
        } catch (GatewayException e) {
            // 网关异常直接抛出
            throw e;
        } catch (Exception e) {
            log.error("Proxy execution failed", e);
            throw GatewayExceptionFactory.wrap(e, "PROXY_EXECUTION", "代理执行失败: " + route.getTarget());
        }
    }

    /**
     * 响应后处理
     */
    private GatewayResponse postprocessResponse(GatewayResponse response, RouteConfig route) {
        if (route.getResponseTemplate() != null) {
            try {
                return templateEngine.processResponseTemplate(response, route.getResponseTemplate());
            } catch (Exception e) {
                log.warn("Response template processing failed", e);
                throw GatewayExceptionFactory.templateExecutionError(route.getResponseTemplate(), e);
            }
        }
        return response;
    }

    /**
     * 包装响应为统一格式
     */
    private GatewayResponse wrapGatewayResponse(GatewayResponse response) {
        try {
            ObjectNode root = objectMapper.createObjectNode();
            // 构建result对象
            Result result = Result.builder()
                    .code(String.valueOf(response.getStatusCode()))
                    .status(response.getStatusCode() == 200 ? "success" : "error")
                    .message(response.getHeaders() != null && response.getHeaders().containsKey("X-Message")
                            ? response.getHeaders().get("X-Message") : "")
                    .build();
            root.set("result", objectMapper.valueToTree(result));
            // 业务数据合并
            if (response.getBody() != null && response.getContentType() != null && response.getContentType().contains("json")) {
                try {
                    ObjectNode dataNode = (ObjectNode) objectMapper.readTree(response.getBody());
                    dataNode.fieldNames().forEachRemaining(field -> {
                        if (!"result".equals(field)) {
                            root.set(field, dataNode.get(field));
                        }
                    });
                } catch (Exception e) {
                    // body不是标准json，作为data字段返回
                    root.put("data", response.getBody());
                }
            } else if (response.getBody() != null) {
                root.put("data", response.getBody());
            }
            response.setBody(objectMapper.writeValueAsString(root));
            response.setContentType("application/json;charset=UTF-8");
        } catch (Exception e) {
            // 包装失败，降级为原始body
        }
        return response;
    }

    /**
     * 构建404响应
     */
    private GatewayResponse buildNotFoundResponse(GatewayRequest request) {
        return GatewayResponse.builder()
                .requestId(request.getRequestId())
                .statusCode(GatewayConstants.Status.NOT_FOUND)
                .body("{\"error\":\"Route not found\",\"path\":\"" + request.getPath() + "\"}")
                .contentType(GatewayConstants.ContentType.JSON)
                .responseTime(LocalDateTime.now())
                .build();
    }

    /**
     * 构建错误响应
     */
    private GatewayResponse buildErrorResponse(GatewayRequest request, Exception e, long processingTime) {
        return GatewayResponse.builder()
                .requestId(request.getRequestId())
                .statusCode(GatewayConstants.Status.INTERNAL_ERROR)
                .body("{\"error\":\"Internal Server Error\",\"message\":\"" + e.getMessage() + "\"}")
                .contentType(GatewayConstants.ContentType.JSON)
                .responseTime(LocalDateTime.now())
                .processingTime(processingTime)
                .errorMessage(e.getMessage())
                .build();
    }
}

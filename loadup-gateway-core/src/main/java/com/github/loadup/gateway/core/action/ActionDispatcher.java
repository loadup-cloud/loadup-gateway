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
import com.github.loadup.gateway.core.plugin.PluginManager;
import com.github.loadup.gateway.core.router.RouteResolver;
import com.github.loadup.gateway.core.template.TemplateEngine;
import com.github.loadup.gateway.facade.config.GatewayProperties;
import com.github.loadup.gateway.facade.constants.GatewayConstants;
import com.github.loadup.gateway.facade.exception.ExceptionHandler;
import com.github.loadup.gateway.facade.exception.GatewayException;
import com.github.loadup.gateway.facade.exception.GatewayExceptionFactory;
import com.github.loadup.gateway.facade.exception.RouteException;
import com.github.loadup.gateway.facade.model.GatewayRequest;
import com.github.loadup.gateway.facade.model.GatewayResponse;
import com.github.loadup.gateway.facade.model.Result;
import com.github.loadup.gateway.facade.model.RouteConfig;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Action dispatcher
 */
@Slf4j
@Component
public class ActionDispatcher {

    @Resource
    private RouteResolver routeResolver;

    @Resource
    private TemplateEngine templateEngine;

    @Resource
    private PluginManager pluginManager;

    @Resource
    private GatewayProperties gatewayProperties;

    @Resource
    private ObjectMapper objectMapper;

    /**
     * Dispatch request to appropriate handler
     */
    public GatewayResponse dispatch(GatewayRequest request) {
        StopWatch stopWatch = StopWatch.createStarted();

        try {
            // Resolve route
            Optional<RouteConfig> routeOpt = routeResolver.resolve(request);
            if (!routeOpt.isPresent()) {
                // Use unified exception handling to build 404 response
                RouteException routeException = GatewayExceptionFactory.routeNotFound(request.getPath());
                return ExceptionHandler.handleException(request.getRequestId(), routeException,
                        stopWatch.getTime());
            }

            RouteConfig route = routeOpt.get();
            log.debug("Route resolved: {} -> {}", request.getPath(), route.getRouteId());

            // Request preprocessing - Apply request template
            GatewayRequest processedRequest = preprocessRequest(request, route);

            // Execute proxy forwarding
            GatewayResponse response = executeProxy(processedRequest, route);

            // Response postprocessing - Apply response template
            GatewayResponse processedResponse = postprocessResponse(response, route);

            if (processedResponse.getResponseTime() == null) {
                processedResponse.setResponseTime(LocalDateTime.now());
            }

            // Set processing time
            stopWatch.stop();
            long processingTime = stopWatch.getTime();
            processedResponse.setProcessingTime(processingTime);

            // Unified response format handling
            boolean wrap = gatewayProperties.getResponse() != null && gatewayProperties.getResponse().isWrap();
            if (route.getWrapResponse() != null) {
                wrap = route.getWrapResponse();
            }
            if (wrap) {
                processedResponse = wrapGatewayResponse(processedResponse);
            }

            return processedResponse;

        } catch (GatewayException e) {
            // Handle gateway exception directly
            return ExceptionHandler.handleException(request.getRequestId(), e,
                    stopWatch.getTime());
        } catch (Exception e) {
            // Wrap and handle other exceptions
            log.error("Request dispatch failed", e);
            GatewayException wrappedException = GatewayExceptionFactory.wrap(e, "DISPATCHER");
            return ExceptionHandler.handleException(request.getRequestId(), wrappedException,
                    stopWatch.getTime());
        }
    }

    /**
     * Request preprocessing
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
     * Execute proxy forwarding
     */
    private GatewayResponse executeProxy(GatewayRequest request, RouteConfig route) {
        try {
            return pluginManager.executeProxy(request, route);
        } catch (GatewayException e) {
            // Throw gateway exception directly
            throw e;
        } catch (Exception e) {
            log.error("Proxy execution failed", e);
            throw GatewayExceptionFactory.wrap(e, "PROXY_EXECUTION", "Proxy execution failed: " + route.getTarget());
        }
    }

    /**
     * Response postprocessing
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
     * Wrap response in unified format
     */
    private GatewayResponse wrapGatewayResponse(GatewayResponse response) {
        try {
            ObjectNode root = objectMapper.createObjectNode();
            // Build result object
            Result result = Result.builder()
                    .code(String.valueOf(response.getStatusCode()))
                    .status(response.getStatusCode() == 200 ? "success" : "error")
                    .message(response.getHeaders() != null && response.getHeaders().containsKey("X-Message")
                            ? response.getHeaders().get("X-Message") : "")
                    .build();
            root.set("result", objectMapper.valueToTree(result));
            // Merge business data
            if (response.getBody() != null && response.getContentType() != null && response.getContentType().contains("json")) {
                try {
                    ObjectNode dataNode = (ObjectNode) objectMapper.readTree(response.getBody());
                    dataNode.fieldNames().forEachRemaining(field -> {
                        if (!"result".equals(field)) {
                            root.set(field, dataNode.get(field));
                        }
                    });
                } catch (Exception e) {
                    // body is not standard json, return as data field
                    root.put("data", response.getBody());
                }
            } else if (response.getBody() != null) {
                root.put("data", response.getBody());
            }
            response.setBody(objectMapper.writeValueAsString(root));
            response.setContentType("application/json;charset=UTF-8");
        } catch (Exception e) {
            // Wrap failed，Fallback to originalbody
        }
        return response;
    }

    /**
     * Build404Response
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
     * Build error response
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

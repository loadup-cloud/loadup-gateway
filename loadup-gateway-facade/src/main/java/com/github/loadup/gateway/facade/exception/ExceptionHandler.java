package com.github.loadup.gateway.facade.exception;

/*-
 * #%L
 * LoadUp Gateway Facade
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

import com.github.loadup.gateway.facade.model.GatewayResponse;
import com.github.loadup.gateway.facade.constants.GatewayConstants;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.HashMap;

/**
 * 异常处理工具类
 * 提供统一的异常处理和响应构建方法
 */
@Slf4j
public final class ExceptionHandler {

    private ExceptionHandler() {
        // 工具类，不允许实例化
    }

    /**
     * 处理异常并构建统一的错误响应
     *
     * @param requestId 请求ID
     * @param exception 异常对象
     * @return 统一格式的错误响应
     */
    public static GatewayResponse handleException(String requestId, Throwable exception) {
        // 记录异常日志
        logException(exception);

        // 将异常包装为网关异常
        GatewayException gatewayException = ensureGatewayException(exception);

        // 构建错误响应
        return buildErrorResponse(requestId, gatewayException);
    }

    /**
     * 处理异常并构建统一的错误响应（带处理时间）
     *
     * @param requestId 请求ID
     * @param exception 异常对象
     * @param processingTime 处理时间
     * @return 统一格式的错误响应
     */
    public static GatewayResponse handleException(String requestId, Throwable exception, long processingTime) {
        GatewayResponse response = handleException(requestId, exception);
        response.setProcessingTime(processingTime);
        return response;
    }

    /**
     * 确保异常是网关异常类型
     */
    private static GatewayException ensureGatewayException(Throwable exception) {
        if (exception instanceof GatewayException) {
            return (GatewayException) exception;
        }

        // 包装为网关异常
        return GatewayExceptionFactory.wrap(exception, "UNKNOWN");
    }

    /**
     * 构建错误响应
     */
    private static GatewayResponse buildErrorResponse(String requestId, GatewayException exception) {
        // 根据错误类型确定HTTP状态码
        int statusCode = mapToHttpStatus(exception.getErrorType());

        // 构建错误响应体
        String errorBody = buildErrorBody(exception);

        return GatewayResponse.builder()
                .requestId(requestId)
                .statusCode(statusCode)
                .body(errorBody)
                .contentType(GatewayConstants.ContentType.JSON)
                .headers(new HashMap<>())
                .responseTime(LocalDateTime.now())
                .errorMessage(exception.getMessage())
                .build();
    }

    /**
     * 构建错误响应体
     */
    private static String buildErrorBody(GatewayException exception) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"error\": {");
        sb.append("\"code\":\"").append(exception.getErrorCode()).append("\",");
        sb.append("\"type\":\"").append(exception.getErrorType().name()).append("\",");
        sb.append("\"module\":\"").append(exception.getModule()).append("\",");
        sb.append("\"message\":\"").append(escapeJsonString(exception.getMessage())).append("\"");

        // 如果有原因异常，添加原因信息
        if (exception.getCause() != null) {
            sb.append(",\"cause\":\"").append(escapeJsonString(exception.getCause().getMessage())).append("\"");
        }

        sb.append("},");
        sb.append("\"timestamp\":\"").append(LocalDateTime.now()).append("\"");
        sb.append("}");

        return sb.toString();
    }

    /**
     * 根据错误类型映射到HTTP状态码
     */
    private static int mapToHttpStatus(ErrorType errorType) {
        switch (errorType) {
            case ROUTING:
                return GatewayConstants.Status.NOT_FOUND;
            case VALIDATION:
                return GatewayConstants.Status.BAD_REQUEST;
            case AUTHORIZATION:
                return GatewayConstants.Status.UNAUTHORIZED;
            case RATE_LIMIT:
                return 429; // Too Many Requests
            case TIMEOUT:
                return 408; // Request Timeout
            case NETWORK:
                return 502; // Bad Gateway
            case PROXY:
            case PLUGIN:
            case SYSTEM:
            case SERIALIZATION:
            case TEMPLATE:
            case STORAGE:
            case BUSINESS:
            case CONFIGURATION:
            case UNKNOWN:
            default:
                return GatewayConstants.Status.INTERNAL_ERROR;
        }
    }

    /**
     * 记录异常日志
     */
    private static void logException(Throwable exception) {
        if (exception instanceof GatewayException) {
            GatewayException ge = (GatewayException) exception;
            log.error("[{}] {} - {}: {}",
                    ge.getModule(),
                    ge.getErrorType(),
                    ge.getErrorCode(),
                    ge.getMessage(),
                    exception);
        } else {
            log.error("Unhandled exception: {}", exception.getMessage(), exception);
        }
    }

    /**
     * 转义JSON字符串中的特殊字符
     */
    private static String escapeJsonString(String str) {
        if (str == null) {
            return "";
        }
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }

    /**
     * 检查异常是否需要重试
     */
    public static boolean isRetryable(Throwable exception) {
        if (exception instanceof GatewayException) {
            GatewayException ge = (GatewayException) exception;
            // 网络错误和超时错误通常可以重试
            return ge.getErrorType() == ErrorType.NETWORK ||
                   ge.getErrorType() == ErrorType.TIMEOUT;
        }

        // 对于标准异常，判断是否为网络相关异常
        return exception instanceof java.net.SocketTimeoutException ||
               exception instanceof java.net.ConnectException ||
               exception instanceof java.io.IOException;
    }

    /**
     * 获取异常的错误级别
     */
    public static String getLogLevel(Throwable exception) {
        if (exception instanceof GatewayException) {
            GatewayException ge = (GatewayException) exception;
            switch (ge.getErrorType()) {
                case VALIDATION:
                case AUTHORIZATION:
                case RATE_LIMIT:
                    return "WARN"; // 客户端错误，警告级别
                case SYSTEM:
                case CONFIGURATION:
                case STORAGE:
                    return "ERROR"; // 系统错误，错误级别
                default:
                    return "INFO"; // 其他情况，信息级别
            }
        }
        return "ERROR"; // 未知异常，错误级别
    }
}

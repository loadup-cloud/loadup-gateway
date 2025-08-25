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

/**
 * 异常工厂类
 * 提供统一的异常创建方法
 */
public final class GatewayExceptionFactory {

    private GatewayExceptionFactory() {
        // 工具类，不允许实例化
    }

    /**
     * 将标准异常包装为网关异常
     *
     * @param cause 原始异常
     * @param module 模块名称
     * @return 包装后的网关异常
     */
    public static GatewayException wrap(Throwable cause, String module) {
        if (cause instanceof GatewayException) {
            return (GatewayException) cause;
        }

        // 根据异常类型选择合适的错误码和类型
        ErrorCode errorCode = mapToErrorCode(cause);
        ErrorType errorType = mapToErrorType(cause);

        return new GatewayException(errorCode.getCode(), errorType, module, cause.getMessage(), cause);
    }

    /**
     * 将标准异常包装为网关异常（带自定义消息）
     *
     * @param cause 原始异常
     * @param module 模块名称
     * @param message 自定义消息
     * @return 包装后的网关异常
     */
    public static GatewayException wrap(Throwable cause, String module, String message) {
        if (cause instanceof GatewayException) {
            return (GatewayException) cause;
        }

        ErrorCode errorCode = mapToErrorCode(cause);
        ErrorType errorType = mapToErrorType(cause);

        return new GatewayException(errorCode.getCode(), errorType, module, message, cause);
    }

    /**
     * 创建路由异常
     */
    public static RouteException routeNotFound(String path) {
        return RouteException.notFound(path);
    }

    public static RouteException routeConfigError(String message) {
        return RouteException.configError(message);
    }

    /**
     * 创建插件异常
     */
    public static PluginException pluginNotFound(String pluginName) {
        return PluginException.notFound(pluginName);
    }

    public static PluginException pluginExecutionFailed(String pluginName, Throwable cause) {
        return PluginException.executionFailed(pluginName, cause);
    }

    /**
     * 创建代理异常
     */
    public static ProxyException beanNotFound(String beanName) {
        return ProxyException.beanNotFound(beanName);
    }

    public static ProxyException methodNotFound(String beanName, String methodName) {
        return ProxyException.methodNotFound(beanName, methodName);
    }

    public static ProxyException methodInvokeFailed(String beanName, String methodName, Throwable cause) {
        return ProxyException.methodInvokeFailed(beanName, methodName, cause);
    }

    public static ProxyException invalidBeanTarget(String target) {
        return ProxyException.invalidTarget(target);
    }

    public static ProxyException httpRequestFailed(String url, Throwable cause) {
        return ProxyException.httpRequestFailed(url, cause);
    }

    public static ProxyException rpcCallFailed(String service, String method, Throwable cause) {
        return ProxyException.rpcCallFailed(service, method, cause);
    }

    /**
     * 创建参数验证异常
     */
    public static ValidationException paramRequired(String paramName) {
        return ValidationException.required(paramName);
    }

    public static ValidationException paramInvalidFormat(String paramName, String expectedFormat) {
        return ValidationException.invalidFormat(paramName, expectedFormat);
    }

    /**
     * 创建系统异常
     */
    public static SystemException configurationError(String message) {
        return SystemException.configurationError(message);
    }

    public static SystemException operationNotSupported(String operation) {
        return SystemException.operationNotSupported(operation);
    }

    public static SystemException internalError(String message, Throwable cause) {
        return SystemException.internalError(message, cause);
    }

    /**
     * 创建序列化异常
     */
    public static SerializationException jsonParseError(String json, Throwable cause) {
        return SerializationException.jsonParseError(json, cause);
    }

    public static SerializationException jsonSerializeError(Object object, Throwable cause) {
        return SerializationException.jsonSerializeError(object, cause);
    }

    /**
     * 创建模板异常
     */
    public static TemplateException templateNotFound(String templateName) {
        return TemplateException.notFound(templateName);
    }

    public static TemplateException templateExecutionError(String templateName, Throwable cause) {
        return TemplateException.executionError(templateName, cause);
    }

    // 私有方法：根据异常类型映射到错误码
    private static ErrorCode mapToErrorCode(Throwable cause) {
        if (cause instanceof IllegalArgumentException) {
            return ErrorCode.PARAM_INVALID_FORMAT;
        }
        if (cause instanceof NullPointerException) {
            return ErrorCode.PARAM_REQUIRED;
        }
        if (cause instanceof NoSuchMethodException) {
            return ErrorCode.BEAN_METHOD_NOT_FOUND;
        }
        if (cause instanceof UnsupportedOperationException) {
            return ErrorCode.OPERATION_NOT_SUPPORTED;
        }
        if (cause instanceof java.net.SocketTimeoutException) {
            return ErrorCode.NETWORK_TIMEOUT;
        }
        if (cause instanceof java.net.ConnectException) {
            return ErrorCode.NETWORK_CONNECTION_REFUSED;
        }
        if (cause instanceof java.io.IOException) {
            return ErrorCode.NETWORK_UNREACHABLE;
        }
        if (cause instanceof com.fasterxml.jackson.core.JsonProcessingException) {
            return ErrorCode.JSON_PARSE_ERROR;
        }
        // 默认返回内部错误
        return ErrorCode.INTERNAL_ERROR;
    }

    // 私有方法：根据异常类型映射到错误类型
    private static ErrorType mapToErrorType(Throwable cause) {
        if (cause instanceof IllegalArgumentException || cause instanceof NullPointerException) {
            return ErrorType.VALIDATION;
        }
        if (cause instanceof java.net.SocketTimeoutException ||
            cause instanceof java.net.ConnectException ||
            cause instanceof java.io.IOException) {
            return ErrorType.NETWORK;
        }
        if (cause instanceof com.fasterxml.jackson.core.JsonProcessingException) {
            return ErrorType.SERIALIZATION;
        }
        if (cause instanceof UnsupportedOperationException) {
            return ErrorType.SYSTEM;
        }
        // 默认返回系统错误
        return ErrorType.SYSTEM;
    }
}

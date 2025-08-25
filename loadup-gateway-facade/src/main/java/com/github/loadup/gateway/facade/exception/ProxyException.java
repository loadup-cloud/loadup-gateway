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
 * 代理相关异常
 */
public class ProxyException extends GatewayException {

    private static final String MODULE = "PROXY";

    public ProxyException(ErrorCode errorCode, String message) {
        super(errorCode.getCode(), ErrorType.PROXY, MODULE, message);
    }

    public ProxyException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode.getCode(), ErrorType.PROXY, MODULE, message, cause);
    }

    // 便捷方法 - SpringBean代理异常
    public static ProxyException beanNotFound(String beanName) {
        return new ProxyException(ErrorCode.BEAN_NOT_FOUND, "Spring Bean未找到: " + beanName);
    }

    public static ProxyException methodNotFound(String beanName, String methodName) {
        return new ProxyException(ErrorCode.BEAN_METHOD_NOT_FOUND,
            "Bean方法未找到: " + beanName + "." + methodName);
    }

    public static ProxyException methodInvokeFailed(String beanName, String methodName, Throwable cause) {
        return new ProxyException(ErrorCode.BEAN_METHOD_INVOKE_FAILED,
            "Bean方法调用失败: " + beanName + "." + methodName, cause);
    }

    public static ProxyException invalidTarget(String target) {
        return new ProxyException(ErrorCode.BEAN_TARGET_FORMAT_INVALID,
            "Bean目标格式无效: " + target + "，期望格式: beanName:methodName");
    }

    // 便捷方法 - HTTP代理异常
    public static ProxyException httpRequestFailed(String url, Throwable cause) {
        return new ProxyException(ErrorCode.HTTP_REQUEST_FAILED, "HTTP请求失败: " + url, cause);
    }

    public static ProxyException httpTimeout(String url) {
        return new ProxyException(ErrorCode.HTTP_CONNECTION_TIMEOUT, "HTTP连接超时: " + url);
    }

    // 便捷方法 - RPC代理异常
    public static ProxyException rpcServiceNotFound(String serviceName) {
        return new ProxyException(ErrorCode.RPC_SERVICE_NOT_FOUND, "RPC服务未找到: " + serviceName);
    }

    public static ProxyException rpcCallFailed(String serviceName, String methodName, Throwable cause) {
        return new ProxyException(ErrorCode.RPC_CALL_FAILED,
            "RPC调用失败: " + serviceName + "." + methodName, cause);
    }

    // 通用代理异常
    public static ProxyException executionFailed(String target, Throwable cause) {
        return new ProxyException(ErrorCode.PROXY_EXECUTION_FAILED, "代理执行失败: " + target, cause);
    }

    public static ProxyException timeout(String target) {
        return new ProxyException(ErrorCode.PROXY_TIMEOUT, "代理超时: " + target);
    }
}

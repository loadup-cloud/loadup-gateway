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
 * 系统异常
 */
public class SystemException extends GatewayException {

    private static final String MODULE = "SYSTEM";

    public SystemException(ErrorCode errorCode, String message) {
        super(errorCode.getCode(), ErrorType.SYSTEM, MODULE, message);
    }

    public SystemException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode.getCode(), ErrorType.SYSTEM, MODULE, message, cause);
    }

    // 便捷方法
    public static SystemException configurationError(String message) {
        return new SystemException(ErrorCode.CONFIGURATION_ERROR, "配置错误: " + message);
    }

    public static SystemException initializationError(String component, Throwable cause) {
        return new SystemException(ErrorCode.INITIALIZATION_ERROR, "组件初始化失败: " + component, cause);
    }

    public static SystemException internalError(String message) {
        return new SystemException(ErrorCode.SYSTEM_ERROR, "系统内部错误: " + message);
    }

    public static SystemException internalError(String message, Throwable cause) {
        return new SystemException(ErrorCode.SYSTEM_ERROR, "系统内部错误: " + message, cause);
    }

    public static SystemException operationNotSupported(String operation) {
        return new SystemException(ErrorCode.OPERATION_NOT_SUPPORTED, "操作不支持: " + operation);
    }
}

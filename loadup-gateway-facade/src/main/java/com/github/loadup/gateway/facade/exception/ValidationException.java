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
 * 参数验证异常
 */
public class ValidationException extends GatewayException {

    private static final String MODULE = "VALIDATION";

    public ValidationException(ErrorCode errorCode, String message) {
        super(errorCode.getCode(), ErrorType.VALIDATION, MODULE, message);
    }

    public ValidationException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode.getCode(), ErrorType.VALIDATION, MODULE, message, cause);
    }

    // 便捷方法
    public static ValidationException required(String paramName) {
        return new ValidationException(ErrorCode.PARAM_REQUIRED, "必需参数缺失: " + paramName);
    }

    public static ValidationException invalidFormat(String paramName, String expectedFormat) {
        return new ValidationException(ErrorCode.PARAM_INVALID_FORMAT,
            "参数格式无效: " + paramName + "，期望格式: " + expectedFormat);
    }

    public static ValidationException outOfRange(String paramName, String range) {
        return new ValidationException(ErrorCode.PARAM_OUT_OF_RANGE,
            "参数超出范围: " + paramName + "，有效范围: " + range);
    }
}

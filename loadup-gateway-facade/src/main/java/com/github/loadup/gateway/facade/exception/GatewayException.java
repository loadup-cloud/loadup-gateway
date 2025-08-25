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
 * 网关异常基类
 * 所有网关相关异常都应该继承此类
 */
public class GatewayException extends RuntimeException {

    /**
     * 错误码
     */
    private final String errorCode;

    /**
     * 错误类型
     */
    private final ErrorType errorType;

    /**
     * 模块名称
     */
    private final String module;

    /**
     * 构造函数
     *
     * @param errorCode 错误码
     * @param errorType 错误类型
     * @param module 模块名称
     * @param message 错误消息
     */
    public GatewayException(String errorCode, ErrorType errorType, String module, String message) {
        super(message);
        this.errorCode = errorCode;
        this.errorType = errorType;
        this.module = module;
    }

    /**
     * 构造函数（带原因异常）
     *
     * @param errorCode 错误码
     * @param errorType 错误类型
     * @param module 模块名称
     * @param message 错误消息
     * @param cause 原因异常
     */
    public GatewayException(String errorCode, ErrorType errorType, String module, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.errorType = errorType;
        this.module = module;
    }

    /**
     * 获取错误码
     */
    public String getErrorCode() {
        return errorCode;
    }

    /**
     * 获取错误类型
     */
    public ErrorType getErrorType() {
        return errorType;
    }

    /**
     * 获取模块名称
     */
    public String getModule() {
        return module;
    }

    /**
     * 获取完整的错误信息
     */
    public String getFullErrorMessage() {
        return String.format("[%s] %s:%s - %s", module, errorType, errorCode, getMessage());
    }

    @Override
    public String toString() {
        return String.format("GatewayException{errorCode='%s', errorType=%s, module='%s', message='%s'}",
                errorCode, errorType, module, getMessage());
    }
}

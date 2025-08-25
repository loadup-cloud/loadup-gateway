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
 * 模板处理异常
 */
public class TemplateException extends GatewayException {

    private static final String MODULE = "TEMPLATE";

    public TemplateException(ErrorCode errorCode, String message) {
        super(errorCode.getCode(), ErrorType.TEMPLATE, MODULE, message);
    }

    public TemplateException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode.getCode(), ErrorType.TEMPLATE, MODULE, message, cause);
    }

    // 便捷方法
    public static TemplateException notFound(String templateName) {
        return new TemplateException(ErrorCode.TEMPLATE_NOT_FOUND, "模板未找到: " + templateName);
    }

    public static TemplateException parseError(String templateName, Throwable cause) {
        return new TemplateException(ErrorCode.TEMPLATE_PARSE_ERROR, "模板解析失败: " + templateName, cause);
    }

    public static TemplateException executionError(String templateName, Throwable cause) {
        return new TemplateException(ErrorCode.TEMPLATE_EXECUTION_ERROR, "模板执行失败: " + templateName, cause);
    }
}

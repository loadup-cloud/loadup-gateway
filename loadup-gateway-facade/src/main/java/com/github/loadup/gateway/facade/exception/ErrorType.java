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
 * 错误类型枚举
 */
public enum ErrorType {

    /**
     * 配置错误
     */
    CONFIGURATION("配置错误"),

    /**
     * 路由错误
     */
    ROUTING("路由错误"),

    /**
     * 插件错误
     */
    PLUGIN("插件错误"),

    /**
     * 代理错误
     */
    PROXY("代理错误"),

    /**
     * 参数验证错误
     */
    VALIDATION("参数验证错误"),

    /**
     * 业务逻辑错误
     */
    BUSINESS("业务逻辑错误"),

    /**
     * 系统错误
     */
    SYSTEM("系统错误"),

    /**
     * 网络错误
     */
    NETWORK("网络错误"),

    /**
     * 序列化/反序列化错误
     */
    SERIALIZATION("序列化错误"),

    /**
     * 模板处理错误
     */
    TEMPLATE("模板处理错误"),

    /**
     * 存储错误
     */
    STORAGE("存储错误"),

    /**
     * 权限错误
     */
    AUTHORIZATION("权限错误"),

    /**
     * 限流错误
     */
    RATE_LIMIT("限流错误"),

    /**
     * 超时错误
     */
    TIMEOUT("超时错误"),

    /**
     * 未知错误
     */
    UNKNOWN("未知错误");

    private final String description;

    ErrorType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return name() + "(" + description + ")";
    }
}

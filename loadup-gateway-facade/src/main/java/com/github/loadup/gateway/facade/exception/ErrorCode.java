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
 * 网关错误码枚举
 * 统一管理所有错误码
 */
public enum ErrorCode {

    // 路由相关错误 (1000-1999)
    ROUTE_NOT_FOUND("1001", "路由未找到"),
    ROUTE_INVALID_PATH("1002", "无效的路由路径"),
    ROUTE_INVALID_METHOD("1003", "无效的HTTP方法"),
    ROUTE_CONFIG_ERROR("1004", "路由配置错误"),

    // 插件相关错误 (2000-2999)
    PLUGIN_NOT_FOUND("2001", "插件未找到"),
    PLUGIN_INIT_FAILED("2002", "插件初始化失败"),
    PLUGIN_EXECUTION_FAILED("2003", "插件执行失败"),
    PLUGIN_CONFIG_INVALID("2004", "插件配置无效"),

    // 代理相关错误 (3000-3999)
    PROXY_TARGET_INVALID("3001", "代理目标无效"),
    PROXY_CONNECTION_FAILED("3002", "代理连接失败"),
    PROXY_EXECUTION_FAILED("3003", "代理执行失败"),
    PROXY_TIMEOUT("3004", "代理超时"),

    // SpringBean代理错误 (3100-3199)
    BEAN_NOT_FOUND("3101", "Spring Bean未找到"),
    BEAN_METHOD_NOT_FOUND("3102", "Bean方法未找到"),
    BEAN_METHOD_INVOKE_FAILED("3103", "Bean方法调用失败"),
    BEAN_TARGET_FORMAT_INVALID("3104", "Bean目标格式无效"),

    // HTTP代理错误 (3200-3299)
    HTTP_REQUEST_FAILED("3201", "HTTP请求失败"),
    HTTP_RESPONSE_INVALID("3202", "HTTP响应无效"),
    HTTP_CONNECTION_TIMEOUT("3203", "HTTP连接超时"),

    // RPC代理错误 (3300-3399)
    RPC_SERVICE_NOT_FOUND("3301", "RPC服务未找到"),
    RPC_METHOD_NOT_FOUND("3302", "RPC方法未找到"),
    RPC_CALL_FAILED("3303", "RPC调用失败"),

    // 参数验证错误 (4000-4999)
    PARAM_REQUIRED("4001", "必需参数缺失"),
    PARAM_INVALID_FORMAT("4002", "参数格式无效"),
    PARAM_OUT_OF_RANGE("4003", "参数超出范围"),

    // 序列化错误 (5000-5999)
    JSON_PARSE_ERROR("5001", "JSON解析错误"),
    JSON_SERIALIZE_ERROR("5002", "JSON序列化错误"),

    // 模板处理错误 (6000-6999)
    TEMPLATE_NOT_FOUND("6001", "模板未找到"),
    TEMPLATE_PARSE_ERROR("6002", "模板解析错误"),
    TEMPLATE_EXECUTION_ERROR("6003", "模板执行错误"),

    // 存储错误 (7000-7999)
    STORAGE_READ_ERROR("7001", "存储读取错误"),
    STORAGE_WRITE_ERROR("7002", "存储写入错误"),
    STORAGE_CONNECTION_ERROR("7003", "存储连接错误"),

    // 系统错误 (8000-8999)
    SYSTEM_ERROR("8001", "系统内部错误"),
    CONFIGURATION_ERROR("8002", "配置错误"),
    INITIALIZATION_ERROR("8003", "初始化错误"),

    // 网络错误 (9000-9999)
    NETWORK_TIMEOUT("9001", "网络超时"),
    NETWORK_CONNECTION_REFUSED("9002", "网络连接被拒绝"),
    NETWORK_UNREACHABLE("9003", "网络不可达"),

    // 通用错误 (0000-0999)
    UNKNOWN_ERROR("0001", "未知错误"),
    OPERATION_NOT_SUPPORTED("0002", "操作不支持"),
    INTERNAL_ERROR("0003", "内部错误");

    private final String code;
    private final String message;

    ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return code + ": " + message;
    }
}

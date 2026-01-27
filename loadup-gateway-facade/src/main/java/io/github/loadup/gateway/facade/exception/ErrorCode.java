package io.github.loadup.gateway.facade.exception;

/*-
 * #%L
 * LoadUp Gateway Facade
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
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

/** Gateway error code enumeration Centralized management of all error codes */
public enum ErrorCode {

  // Route related errors (1000-1999)
  ROUTE_NOT_FOUND("1001", "Route not found"),
  ROUTE_INVALID_PATH("1002", "Invalid route path"),
  ROUTE_INVALID_METHOD("1003", "Invalid HTTP method"),
  ROUTE_CONFIG_ERROR("1004", "Route configuration error"),

  // Plugin related errors (2000-2999)
  PLUGIN_NOT_FOUND("2001", "Plugin not found"),
  PLUGIN_INIT_FAILED("2002", "Plugin initialization failed"),
  PLUGIN_EXECUTION_FAILED("2003", "Plugin execution failed"),
  PLUGIN_CONFIG_INVALID("2004", "Invalid plugin configuration"),

  // Proxy related errors (3000-3999)
  PROXY_TARGET_INVALID("3001", "Invalid proxy target"),
  PROXY_CONNECTION_FAILED("3002", "Proxy connection failed"),
  PROXY_EXECUTION_FAILED("3003", "Proxy execution failed"),
  PROXY_TIMEOUT("3004", "Proxy timeout"),

  // SpringBean proxy errors (3100-3199)
  BEAN_NOT_FOUND("3101", "Spring Bean not found"),
  BEAN_METHOD_NOT_FOUND("3102", "Bean method not found"),
  BEAN_METHOD_INVOKE_FAILED("3103", "Bean method invocation failed"),
  BEAN_TARGET_FORMAT_INVALID("3104", "Invalid Bean target format"),

  // HTTP proxy errors (3200-3299)
  HTTP_REQUEST_FAILED("3201", "HTTP request failed"),
  HTTP_RESPONSE_INVALID("3202", "Invalid HTTP response"),
  HTTP_CONNECTION_TIMEOUT("3203", "HTTP connection timeout"),

  // RPC proxy errors (3300-3399)
  RPC_SERVICE_NOT_FOUND("3301", "RPC service not found"),
  RPC_METHOD_NOT_FOUND("3302", "RPC method not found"),
  RPC_CALL_FAILED("3303", "RPC call failed"),

  // Parameter validation errors (4000-4999)
  PARAM_REQUIRED("4001", "Required parameter missing"),
  PARAM_INVALID_FORMAT("4002", "Invalid parameter format"),
  PARAM_OUT_OF_RANGE("4003", "Parameter out of range"),

  // Serialization errors (5000-5999)
  JSON_PARSE_ERROR("5001", "JSON parse error"),
  JSON_SERIALIZE_ERROR("5002", "JSON serialize error"),

  // Template processing errors (6000-6999)
  TEMPLATE_NOT_FOUND("6001", "Template not found"),
  TEMPLATE_PARSE_ERROR("6002", "Template parse error"),
  TEMPLATE_EXECUTION_ERROR("6003", "Template execution error"),

  // Storage errors (7000-7999)
  STORAGE_READ_ERROR("7001", "Storage read error"),
  STORAGE_WRITE_ERROR("7002", "Storage write error"),
  STORAGE_CONNECTION_ERROR("7003", "Storage connection error"),

  // System errors (8000-8999)
  SYSTEM_ERROR("8001", "System internal error"),
  CONFIGURATION_ERROR("8002", "Configuration error"),
  INITIALIZATION_ERROR("8003", "Initialization error"),

  // Network errors (9000-9999)
  NETWORK_TIMEOUT("9001", "Network timeout"),
  NETWORK_CONNECTION_REFUSED("9002", "Network connection refused"),
  NETWORK_UNREACHABLE("9003", "Network unreachable"),

  // Common errors (0000-0999)
  UNKNOWN_ERROR("0001", "Unknown error"),
  OPERATION_NOT_SUPPORTED("0002", "Operation not supported"),
  INTERNAL_ERROR("0003", "Internal error");

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

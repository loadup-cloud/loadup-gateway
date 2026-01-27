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

/** Proxy-related exceptions */
public class ProxyException extends GatewayException {

  private static final String MODULE = "PROXY";

  public ProxyException(ErrorCode errorCode, String message) {
    super(errorCode.getCode(), ErrorType.PROXY, MODULE, errorCode.getMessage() + ":" + message);
  }

  public ProxyException(ErrorCode errorCode, String message, Throwable cause) {
    super(
        errorCode.getCode(),
        ErrorType.PROXY,
        MODULE,
        errorCode.getMessage() + ":" + message,
        cause);
  }

  // Convenience methods - SpringBean proxy exceptions
  public static ProxyException beanNotFound(String beanName) {
    return new ProxyException(ErrorCode.BEAN_NOT_FOUND, beanName);
  }

  public static ProxyException methodNotFound(String beanName, String methodName) {
    return new ProxyException(ErrorCode.BEAN_METHOD_NOT_FOUND, beanName + "." + methodName);
  }

  public static ProxyException methodInvokeFailed(
      String beanName, String methodName, Throwable cause) {
    return new ProxyException(
        ErrorCode.BEAN_METHOD_INVOKE_FAILED, beanName + "." + methodName, cause);
  }

  public static ProxyException invalidTarget(String target) {
    return new ProxyException(
        ErrorCode.BEAN_TARGET_FORMAT_INVALID, target + ", Expected format is beanName:methodName");
  }

  // Convenience methods - HTTP proxy exceptions
  public static ProxyException httpRequestFailed(String url, Throwable cause) {
    return new ProxyException(ErrorCode.HTTP_REQUEST_FAILED, url, cause);
  }

  public static ProxyException httpTimeout(String url) {
    return new ProxyException(ErrorCode.HTTP_CONNECTION_TIMEOUT, url);
  }

  // Convenience methods - RPC proxy exceptions
  public static ProxyException rpcServiceNotFound(String serviceName) {
    return new ProxyException(ErrorCode.RPC_SERVICE_NOT_FOUND, serviceName);
  }

  public static ProxyException rpcCallFailed(
      String serviceName, String methodName, Throwable cause) {
    return new ProxyException(
        ErrorCode.RPC_CALL_FAILED, "RPC call failed: " + serviceName + "." + methodName, cause);
  }

  // General proxy exceptions
  public static ProxyException executionFailed(String target, Throwable cause) {
    return new ProxyException(
        ErrorCode.PROXY_EXECUTION_FAILED, "Proxy execution failed: " + target, cause);
  }

  public static ProxyException timeout(String target) {
    return new ProxyException(ErrorCode.PROXY_TIMEOUT, "Proxy timeout: " + target);
  }
}

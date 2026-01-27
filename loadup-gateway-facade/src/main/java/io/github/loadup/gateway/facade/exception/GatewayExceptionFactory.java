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

/** Exception factory class Provides unified exception creation methods */
public final class GatewayExceptionFactory {

  private GatewayExceptionFactory() {
    // Utility class, instantiation not allowed
  }

  /**
   * Wrap a standard exception as a gateway exception
   *
   * @param cause Original exception
   * @param module Module name
   * @return Wrapped gateway exception
   */
  public static GatewayException wrap(Throwable cause, String module) {
    if (cause instanceof GatewayException) {
      return (GatewayException) cause;
    }

    // Select appropriate error code and type based on exception type
    ErrorCode errorCode = mapToErrorCode(cause);
    ErrorType errorType = mapToErrorType(cause);

    return new GatewayException(errorCode.getCode(), errorType, module, cause.getMessage(), cause);
  }

  /**
   * Wrap a standard exception as a gateway exception (with custom message)
   *
   * @param cause Original exception
   * @param module Module name
   * @param message Custom message
   * @return Wrapped gateway exception
   */
  public static GatewayException wrap(Throwable cause, String module, String message) {
    if (cause instanceof GatewayException) {
      return (GatewayException) cause;
    }

    ErrorCode errorCode = mapToErrorCode(cause);
    ErrorType errorType = mapToErrorType(cause);

    return new GatewayException(errorCode.getCode(), errorType, module, message, cause);
  }

  /** Create route exception */
  public static RouteException routeNotFound(String path) {
    return RouteException.notFound(path);
  }

  public static RouteException routeConfigError(String message) {
    return RouteException.configError(message);
  }

  /** Create plugin exception */
  public static PluginException pluginNotFound(String pluginName) {
    return PluginException.notFound(pluginName);
  }

  public static PluginException pluginExecutionFailed(String pluginName, Throwable cause) {
    return PluginException.executionFailed(pluginName, cause);
  }

  /** Create proxy exception */
  public static ProxyException beanNotFound(String beanName) {
    return ProxyException.beanNotFound(beanName);
  }

  public static ProxyException methodNotFound(String beanName, String methodName) {
    return ProxyException.methodNotFound(beanName, methodName);
  }

  public static ProxyException methodInvokeFailed(
      String beanName, String methodName, Throwable cause) {
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

  /** Create parameter validation exception */
  public static ValidationException paramRequired(String paramName) {
    return ValidationException.required(paramName);
  }

  public static ValidationException paramInvalidFormat(String paramName, String expectedFormat) {
    return ValidationException.invalidFormat(paramName, expectedFormat);
  }

  /** Create system exception */
  public static SystemException configurationError(String message) {
    return SystemException.configurationError(message);
  }

  public static SystemException operationNotSupported(String operation) {
    return SystemException.operationNotSupported(operation);
  }

  public static SystemException internalError(String message, Throwable cause) {
    return SystemException.internalError(message, cause);
  }

  /** Create serialization exception */
  public static SerializationException jsonParseError(String json, Throwable cause) {
    return SerializationException.jsonParseError(json, cause);
  }

  public static SerializationException jsonSerializeError(Object object, Throwable cause) {
    return SerializationException.jsonSerializeError(object, cause);
  }

  /** Create template exception */
  public static TemplateException templateNotFound(String templateName) {
    return TemplateException.notFound(templateName);
  }

  public static TemplateException templateExecutionError(String templateName, Throwable cause) {
    return TemplateException.executionError(templateName, cause);
  }

  // Private method: Map exception type to error code
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
    // Default return internal error
    return ErrorCode.INTERNAL_ERROR;
  }

  // Private method: Map exception type to error type
  private static ErrorType mapToErrorType(Throwable cause) {
    if (cause instanceof IllegalArgumentException || cause instanceof NullPointerException) {
      return ErrorType.VALIDATION;
    }
    if (cause instanceof java.net.SocketTimeoutException
        || cause instanceof java.net.ConnectException
        || cause instanceof java.io.IOException) {
      return ErrorType.NETWORK;
    }
    if (cause instanceof com.fasterxml.jackson.core.JsonProcessingException) {
      return ErrorType.SERIALIZATION;
    }
    if (cause instanceof UnsupportedOperationException) {
      return ErrorType.SYSTEM;
    }
    // Default return system error
    return ErrorType.SYSTEM;
  }
}

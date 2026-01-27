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

import io.github.loadup.gateway.facade.constants.GatewayConstants;
import io.github.loadup.gateway.facade.model.GatewayResponse;
import java.time.LocalDateTime;
import java.util.HashMap;
import lombok.extern.slf4j.Slf4j;

/**
 * Exception handling utility class Provides unified exception handling and response building
 * methods
 */
@Slf4j
public final class ExceptionHandler {

  private ExceptionHandler() {
    // Utility class, instantiation not allowed
  }

  /**
   * Handle exception and build unified error response
   *
   * @param requestId Request ID
   * @param exception Exception object
   * @return Unified format error response
   */
  public static GatewayResponse handleException(String requestId, Throwable exception) {
    // Log exception
    logException(exception);

    // Wrap exception as gateway exception
    GatewayException gatewayException = ensureGatewayException(exception);

    // Build error response
    return buildErrorResponse(requestId, gatewayException);
  }

  /**
   * Handle exception and build unified error response (with processing time)
   *
   * @param requestId Request ID
   * @param exception Exception object
   * @param processingTime Processing time
   * @return Unified format error response
   */
  public static GatewayResponse handleException(
      String requestId, Throwable exception, long processingTime) {
    GatewayResponse response = handleException(requestId, exception);
    response.setProcessingTime(processingTime);
    return response;
  }

  /** Ensure exception is of gateway exception type */
  private static GatewayException ensureGatewayException(Throwable exception) {
    if (exception instanceof GatewayException) {
      return (GatewayException) exception;
    }

    // Wrap as gateway exception
    return GatewayExceptionFactory.wrap(exception, "UNKNOWN");
  }

  /** Build error response */
  private static GatewayResponse buildErrorResponse(String requestId, GatewayException exception) {
    // Determine HTTP status code based on error type
    int statusCode = mapToHttpStatus(exception.getErrorType());

    // Build error response body
    String errorBody = buildErrorBody(exception);

    return GatewayResponse.builder()
        .requestId(requestId)
        .statusCode(statusCode)
        .body(errorBody)
        .contentType(GatewayConstants.ContentType.JSON)
        .headers(new HashMap<>())
        .responseTime(LocalDateTime.now())
        .errorMessage(exception.getMessage())
        .build();
  }

  /** Build error response body */
  private static String buildErrorBody(GatewayException exception) {
    StringBuilder sb = new StringBuilder();
    sb.append("{");
    sb.append("\"error\": {");
    sb.append("\"code\":\"").append(exception.getErrorCode()).append("\",");
    sb.append("\"type\":\"").append(exception.getErrorType().name()).append("\",");
    sb.append("\"module\":\"").append(exception.getModule()).append("\",");
    sb.append("\"message\":\"").append(escapeJsonString(exception.getMessage())).append("\"");

    // Add cause information if there is a cause exception
    if (exception.getCause() != null) {
      sb.append(",\"cause\":\"")
          .append(escapeJsonString(exception.getCause().getMessage()))
          .append("\"");
    }

    sb.append("},");
    sb.append("\"timestamp\":\"").append(LocalDateTime.now()).append("\"");
    sb.append("}");

    return sb.toString();
  }

  /** Map error type to HTTP status code */
  private static int mapToHttpStatus(ErrorType errorType) {
    switch (errorType) {
      case ROUTING:
        return GatewayConstants.Status.NOT_FOUND;
      case VALIDATION:
        return GatewayConstants.Status.BAD_REQUEST;
      case AUTHORIZATION:
        return GatewayConstants.Status.UNAUTHORIZED;
      case RATE_LIMIT:
        return 429; // Too Many Requests
      case TIMEOUT:
        return 408; // Request Timeout
      case NETWORK:
        return 502; // Bad Gateway
      case PROXY:
      case PLUGIN:
      case SYSTEM:
      case SERIALIZATION:
      case TEMPLATE:
      case STORAGE:
      case BUSINESS:
      case CONFIGURATION:
      case UNKNOWN:
      default:
        return GatewayConstants.Status.INTERNAL_ERROR;
    }
  }

  /** Log exception */
  private static void logException(Throwable exception) {
    if (exception instanceof GatewayException) {
      GatewayException ge = (GatewayException) exception;
      log.error(
          "[{}] {} - {}: {}",
          ge.getModule(),
          ge.getErrorType(),
          ge.getErrorCode(),
          ge.getMessage(),
          exception);
    } else {
      log.error("Unhandled exception: {}", exception.getMessage(), exception);
    }
  }

  /** Escape special characters in JSON string */
  private static String escapeJsonString(String str) {
    if (str == null) {
      return "";
    }
    return str.replace("\\", "\\\\")
        .replace("\"", "\\\"")
        .replace("\n", "\\n")
        .replace("\r", "\\r")
        .replace("\t", "\\t");
  }

  /** Check if exception is retryable */
  public static boolean isRetryable(Throwable exception) {
    if (exception instanceof GatewayException) {
      GatewayException ge = (GatewayException) exception;
      // Network errors and timeout errors are usually retryable
      return ge.getErrorType() == ErrorType.NETWORK || ge.getErrorType() == ErrorType.TIMEOUT;
    }

    // For standard exceptions, check if it is a network-related exception
    return exception instanceof java.net.SocketTimeoutException
        || exception instanceof java.net.ConnectException
        || exception instanceof java.io.IOException;
  }

  /** Get log level of exception */
  public static String getLogLevel(Throwable exception) {
    if (exception instanceof GatewayException) {
      GatewayException ge = (GatewayException) exception;
      switch (ge.getErrorType()) {
        case VALIDATION:
        case AUTHORIZATION:
        case RATE_LIMIT:
          return "WARN"; // Client error, warning level
        case SYSTEM:
        case CONFIGURATION:
        case STORAGE:
          return "ERROR"; // System error, error level
        default:
          return "INFO"; // Other cases, info level
      }
    }
    return "ERROR"; // Unknown exception, error level
  }
}

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

/** Base gateway exception All gateway-related exceptions should extend this class */
public class GatewayException extends RuntimeException {

  /** Error code */
  private final String errorCode;

  /** Error type */
  private final ErrorType errorType;

  /** Module name */
  private final String module;

  /**
   * Constructor
   *
   * @param errorCode error code
   * @param errorType error type
   * @param module module name
   * @param message error message
   */
  public GatewayException(String errorCode, ErrorType errorType, String module, String message) {
    super(message);
    this.errorCode = errorCode;
    this.errorType = errorType;
    this.module = module;
  }

  /**
   * Constructor with cause
   *
   * @param errorCode error code
   * @param errorType error type
   * @param module module name
   * @param message error message
   * @param cause cause throwable
   */
  public GatewayException(
      String errorCode, ErrorType errorType, String module, String message, Throwable cause) {
    super(message, cause);
    this.errorCode = errorCode;
    this.errorType = errorType;
    this.module = module;
  }

  /** Get error code */
  public String getErrorCode() {
    return errorCode;
  }

  /** Get error type */
  public ErrorType getErrorType() {
    return errorType;
  }

  /** Get module name */
  public String getModule() {
    return module;
  }

  /** Get the full error message */
  public String getFullErrorMessage() {
    return String.format("[%s] %s:%s - %s", module, errorType, errorCode, getMessage());
  }

  @Override
  public String toString() {
    return String.format(
        "GatewayException{errorCode='%s', errorType=%s, module='%s', message='%s'}",
        errorCode, errorType, module, getMessage());
  }
}

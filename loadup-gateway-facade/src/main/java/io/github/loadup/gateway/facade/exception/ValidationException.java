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

/** Parameter validation exception */
public class ValidationException extends GatewayException {

  private static final String MODULE = "VALIDATION";

  public ValidationException(ErrorCode errorCode, String message) {
    super(
        errorCode.getCode(), ErrorType.VALIDATION, MODULE, errorCode.getMessage() + ":" + message);
  }

  public ValidationException(ErrorCode errorCode, String message, Throwable cause) {
    super(
        errorCode.getCode(),
        ErrorType.VALIDATION,
        MODULE,
        errorCode.getMessage() + ":" + message,
        cause);
  }

  // Convenience methods
  public static ValidationException required(String paramName) {
    return new ValidationException(ErrorCode.PARAM_REQUIRED, paramName);
  }

  public static ValidationException invalidFormat(String paramName, String expectedFormat) {
    return new ValidationException(
        ErrorCode.PARAM_INVALID_FORMAT, paramName + ", Expected format: " + expectedFormat);
  }

  public static ValidationException outOfRange(String paramName, String range) {
    return new ValidationException(
        ErrorCode.PARAM_OUT_OF_RANGE, paramName + ", Valid range: " + range);
  }
}

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

/** Route related exception */
public class RouteException extends GatewayException {

  private static final String MODULE = "ROUTE";

  public RouteException(ErrorCode errorCode, String message) {
    super(errorCode.getCode(), ErrorType.ROUTING, MODULE, errorCode.getMessage() + ":" + message);
  }

  public RouteException(ErrorCode errorCode, String message, Throwable cause) {
    super(
        errorCode.getCode(),
        ErrorType.ROUTING,
        MODULE,
        errorCode.getMessage() + ":" + message,
        cause);
  }

  // Convenience methods
  public static RouteException notFound(String path) {
    return new RouteException(ErrorCode.ROUTE_NOT_FOUND, path);
  }

  public static RouteException invalidPath(String path) {
    return new RouteException(ErrorCode.ROUTE_INVALID_PATH, path);
  }

  public static RouteException configError(String message) {
    return new RouteException(ErrorCode.ROUTE_CONFIG_ERROR, message);
  }
}

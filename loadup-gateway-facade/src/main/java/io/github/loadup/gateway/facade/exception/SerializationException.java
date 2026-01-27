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

/** Serialization exception */
public class SerializationException extends GatewayException {

  private static final String MODULE = "SERIALIZATION";

  public SerializationException(ErrorCode errorCode, String message) {
    super(
        errorCode.getCode(),
        ErrorType.SERIALIZATION,
        MODULE,
        errorCode.getMessage() + ":" + message);
  }

  public SerializationException(ErrorCode errorCode, String message, Throwable cause) {
    super(
        errorCode.getCode(),
        ErrorType.SERIALIZATION,
        MODULE,
        errorCode.getMessage() + ":" + message,
        cause);
  }

  // Convenience methods
  public static SerializationException jsonParseError(String json, Throwable cause) {
    return new SerializationException(ErrorCode.JSON_PARSE_ERROR, json, cause);
  }

  public static SerializationException jsonSerializeError(Object object, Throwable cause) {
    return new SerializationException(
        ErrorCode.JSON_SERIALIZE_ERROR, object.getClass().getSimpleName(), cause);
  }
}

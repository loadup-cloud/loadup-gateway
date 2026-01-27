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

/** Error type enumeration */
public enum ErrorType {

  /** Configuration error */
  CONFIGURATION("Configuration error"),

  /** Routing error */
  ROUTING("Routing error"),

  /** Plugin error */
  PLUGIN("Plugin error"),

  /** Proxy error */
  PROXY("Proxy error"),

  /** Parameter validation error */
  VALIDATION("Validation error"),

  /** Business logic error */
  BUSINESS("Business logic error"),

  /** System error */
  SYSTEM("System error"),

  /** Network error */
  NETWORK("Network error"),

  /** Serialization/Deserialization error */
  SERIALIZATION("Serialization error"),

  /** Template processing error */
  TEMPLATE("Template processing error"),

  /** Storage error */
  STORAGE("Storage error"),

  /** Authorization error */
  AUTHORIZATION("Authorization error"),

  /** Rate limiting error */
  RATE_LIMIT("Rate limit error"),

  /** Timeout error */
  TIMEOUT("Timeout error"),

  /** Unknown error */
  UNKNOWN("Unknown error");

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

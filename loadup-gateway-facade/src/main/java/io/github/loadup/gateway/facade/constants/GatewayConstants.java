package io.github.loadup.gateway.facade.constants;

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

/** Gateway constant definitions */
public final class GatewayConstants {

  private GatewayConstants() {}

  /** Protocol types */
  public static final class Protocol {
    public static final String HTTP = "HTTP";
    public static final String RPC = "RPC";
    public static final String BEAN = "BEAN";
  }

  /** Storage types */
  public static final class Storage {
    public static final String FILE = "FILE";
    public static final String DATABASE = "DATABASE";
  }

  /** Template types */
  public static final class Template {
    public static final String REQUEST = "REQUEST";
    public static final String RESPONSE = "RESPONSE";
  }

  /** Content types */
  public static final class ContentType {
    public static final String JSON = "application/json";
    public static final String FORM = "application/x-www-form-urlencoded";
    public static final String XML = "application/xml";
  }

  /** HTTP methods */
  public static final class HttpMethod {
    public static final String GET = "GET";
    public static final String POST = "POST";
    public static final String PUT = "PUT";
    public static final String DELETE = "DELETE";
    public static final String PATCH = "PATCH";
  }

  /** Status codes */
  public static final class Status {
    public static final int SUCCESS = 200;
    public static final int BAD_REQUEST = 400;
    public static final int UNAUTHORIZED = 401;
    public static final int FORBIDDEN = 403;
    public static final int NOT_FOUND = 404;
    public static final int INTERNAL_ERROR = 500;
    public static final int SERVICE_UNAVAILABLE = 503;
  }

  /** Configuration keys */
  public static final class Config {
    public static final String GATEWAY_PREFIX = "loadup.gateway";
    public static final String PLUGIN_ENABLED = "enabled";
    public static final String PLUGIN_CONFIG = "config";
    public static final String TEMPLATE_PATH = "template.path";
    public static final String STORAGE_TYPE = "storage.type";
  }

  /** Route configuration property keys */
  public static final class PropertyKeys {
    public static final String TIMEOUT = "timeout";
    public static final String RETRY_COUNT = "retryCount";
    public static final String WRAP_RESPONSE = "wrapResponse";
  }
}

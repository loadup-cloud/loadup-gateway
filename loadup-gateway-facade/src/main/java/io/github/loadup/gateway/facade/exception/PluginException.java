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

/** Plugin related exception */
public class PluginException extends GatewayException {

  private static final String MODULE = "PLUGIN";

  public PluginException(ErrorCode errorCode, String message) {
    super(errorCode.getCode(), ErrorType.PLUGIN, MODULE, errorCode.getMessage() + ":" + message);
  }

  public PluginException(ErrorCode errorCode, String message, Throwable cause) {
    super(
        errorCode.getCode(),
        ErrorType.PLUGIN,
        MODULE,
        errorCode.getMessage() + ":" + message,
        cause);
  }

  // Convenience methods
  public static PluginException notFound(String pluginName) {
    return new PluginException(ErrorCode.PLUGIN_NOT_FOUND, pluginName);
  }

  public static PluginException initFailed(String pluginName, Throwable cause) {
    return new PluginException(ErrorCode.PLUGIN_INIT_FAILED, pluginName, cause);
  }

  public static PluginException executionFailed(String pluginName, Throwable cause) {
    return new PluginException(ErrorCode.PLUGIN_EXECUTION_FAILED, pluginName, cause);
  }

  public static PluginException configInvalid(String pluginName, String reason) {
    return new PluginException(ErrorCode.PLUGIN_CONFIG_INVALID, pluginName + " - " + reason);
  }
}

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

/** Template processing exception */
public class TemplateException extends GatewayException {

  private static final String MODULE = "TEMPLATE";

  public TemplateException(ErrorCode errorCode, String message) {
    super(errorCode.getCode(), ErrorType.TEMPLATE, MODULE, errorCode.getMessage() + ":" + message);
  }

  public TemplateException(ErrorCode errorCode, String message, Throwable cause) {
    super(
        errorCode.getCode(),
        ErrorType.TEMPLATE,
        MODULE,
        errorCode.getMessage() + ":" + message,
        cause);
  }

  // Convenience methods
  public static TemplateException notFound(String templateName) {
    return new TemplateException(ErrorCode.TEMPLATE_NOT_FOUND, templateName);
  }

  public static TemplateException parseError(String templateName, Throwable cause) {
    return new TemplateException(ErrorCode.TEMPLATE_PARSE_ERROR, templateName, cause);
  }

  public static TemplateException executionError(String templateName, Throwable cause) {
    return new TemplateException(ErrorCode.TEMPLATE_EXECUTION_ERROR, templateName, cause);
  }
}

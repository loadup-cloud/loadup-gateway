package io.github.loadup.gateway.facade.model;

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

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Unified response result object */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {
  /** Business code */
  private String code;

  /** Status */
  private String status;

  /** Message */
  private String message;

  /** Data */
  private T data;

  public static <T> Result<T> success(T data) {
    return Result.<T>builder()
        .code("20000")
        .status("success")
        .message("Success")
        .data(data)
        .build();
  }

  public static <T> Result<T> error(String code, String message) {
    return Result.<T>builder().code(code).status("error").message(message).build();
  }
}

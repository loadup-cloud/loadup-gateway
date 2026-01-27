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

import java.time.LocalDateTime;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Gateway response model */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GatewayResponse {

  /** Request ID */
  private String requestId;

  /** Status code */
  private int statusCode;

  /** Response headers */
  private Map<String, String> headers;

  /** Response body */
  private String body;

  /** Content type */
  private String contentType;

  /** Response time */
  private LocalDateTime responseTime;

  /** Processing time in milliseconds */
  private long processingTime;

  /** Error message */
  private String errorMessage;

  /** Extension attributes */
  private Map<String, Object> attributes;
}

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
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Gateway request model */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GatewayRequest {

  /** Request ID */
  private String requestId;

  /** Request path */
  private String path;

  /** HTTP method */
  private String method;

  /** Request headers */
  private Map<String, String> headers;

  /** Query parameters */
  private Map<String, List<String>> queryParameters;

  /** Path parameters */
  private Map<String, String> pathParameters;

  /** Request body */
  private String body;

  /** Content type */
  private String contentType;

  /** Client IP */
  private String clientIp;

  /** User agent */
  private String userAgent;

  /** Request time */
  private LocalDateTime requestTime;

  /** Extension attributes */
  private Map<String, Object> attributes;
}

package io.github.loadup.gateway.plugins.entity;

/*-
 * #%L
 * Repository Database Plugin
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

import io.github.loadup.gateway.facade.dto.RouteStructure;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

/** Route entity */
@Getter
@Setter
@Table("gateway_routes")
public class RouteEntity extends RouteStructure {
  // path,method,target,requestTemplate,responseTemplate,enabled,properties
  /** id */
  @Id private String routeId;

  /** name */
  private String routeName;

  /** request path */
  private String path;

  /** request method GET, POST, PUT, DELETE, etc. */
  private String method;

  /** http://..., bean://service:method, rpc://class:method:version */
  private String target;

  /** request template */
  private String requestTemplate;

  /** response template */
  private String responseTemplate;

  /** enabled status */
  private Boolean enabled;

  /** additional properties */
  private String properties;

  /** updated at */
  private LocalDateTime updatedAt;

  /** created at */
  private LocalDateTime createdAt;
}

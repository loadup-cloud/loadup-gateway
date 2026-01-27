package io.github.loadup.gateway.plugins.entity;

/*-
 * #%L
 * Repository File Plugin
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
import lombok.Getter;
import lombok.Setter;

/** Simple DTO representing a CSV route row in the file repository. */
@Getter
@Setter
public class FileRouteEntity extends RouteStructure {
  private String routeId;
  private String path;
  private String method;
  private String target;
  private String requestTemplate;
  private String responseTemplate;
  private Boolean enabled;

  /** Raw properties string as persisted in CSV (either JSON or key=value;...) */
  private String properties;
}

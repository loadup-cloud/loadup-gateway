package io.github.loadup.gateway.facade.spi;

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

import io.github.loadup.gateway.facade.dto.RouteStructure;
import io.github.loadup.gateway.facade.model.RouteConfig;
import java.util.List;
import java.util.Optional;

/** Storage plugin SPI interface */
public interface RepositoryPlugin extends GatewayPlugin {

  /** Get route configuration by route ID */
  Optional<RouteConfig> getRoute(String routeId) throws Exception;

  /** Get route configuration by path and method */
  Optional<RouteConfig> getRouteByPath(String path, String method) throws Exception;

  /** Get all route configurations */
  List<RouteConfig> getAllRoutes() throws Exception;

  /** Get template */
  Optional<String> getTemplate(String templateId, String templateType) throws Exception;

  /** Get supported storage type */
  String getSupportedStorageType();

  /**
   * @return
   */
  RouteConfig convertToRouteConfig(RouteStructure structure) throws Exception;
}

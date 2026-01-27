package io.github.loadup.gateway.plugins.manager;

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

import io.github.loadup.gateway.plugins.entity.RouteEntity;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/** Route database Repository */
@Repository
public interface RouteManager extends CrudRepository<RouteEntity, Long> {

  Optional<RouteEntity> findByRouteId(String routeId);

  Optional<RouteEntity> findByPathAndMethod(String path, String method);

  void deleteByRouteId(String routeId);
}

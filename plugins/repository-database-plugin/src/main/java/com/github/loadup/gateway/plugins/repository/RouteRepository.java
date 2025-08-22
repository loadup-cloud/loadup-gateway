package com.github.loadup.gateway.plugins.repository;

/*-
 * #%L
 * Repository Database Plugin
 * %%
 * Copyright (C) 2025 LoadUp Gateway Authors
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

import com.github.loadup.gateway.plugins.RouteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 路由数据库Repository
 */
@Repository
public interface RouteRepository extends JpaRepository<RouteEntity, Long> {

    Optional<RouteEntity> findByRouteId(String routeId);

    Optional<RouteEntity> findByPathAndMethod(String path, String method);

    void deleteByRouteId(String routeId);
}

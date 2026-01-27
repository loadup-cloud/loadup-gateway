package io.github.loadup.gateway.plugins.mapper;

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

import io.github.loadup.gateway.facade.model.RouteConfig;
import io.github.loadup.gateway.facade.utils.JsonUtils;
import io.github.loadup.gateway.plugins.entity.RouteEntity;
import java.util.Date;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(
    componentModel = "spring",
    imports = {JsonUtils.class, Date.class})
public interface RouteMapper {

  @Mapping(target = "routeId", expression = "java(config.getRouteId())")
  @Mapping(target = "routeName", expression = "java(config.getRouteName())")
  @Mapping(target = "properties", expression = "java(JsonUtils.toJson(config.getProperties()))")
  RouteEntity toEntity(RouteConfig config);
}

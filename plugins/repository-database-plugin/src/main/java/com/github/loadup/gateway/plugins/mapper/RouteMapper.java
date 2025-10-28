package com.github.loadup.gateway.plugins.mapper;

import com.github.loadup.gateway.facade.model.RouteConfig;
import com.github.loadup.gateway.facade.utils.JsonUtils;
import com.github.loadup.gateway.plugins.entity.RouteEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Date;

@Mapper(componentModel = "spring", imports = {JsonUtils.class, Date.class})
public interface RouteMapper {

    @Mapping(target = "routeId", expression = "java(config.getRouteId())")
    @Mapping(target = "routeName", expression = "java(config.getRouteName())")
    @Mapping(target = "properties", expression = "java(JsonUtils.toJson(config.getProperties()))")
    RouteEntity toEntity(RouteConfig config);
}

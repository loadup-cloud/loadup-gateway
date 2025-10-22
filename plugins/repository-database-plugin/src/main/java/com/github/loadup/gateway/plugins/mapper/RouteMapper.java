package com.github.loadup.gateway.plugins.mapper;

import com.github.loadup.gateway.facade.model.RouteConfig;
import com.github.loadup.gateway.facade.utils.JsonUtils;
import com.github.loadup.gateway.plugins.RouteEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Date;

@Mapper(componentModel = "spring", imports = {JsonUtils.class, Date.class})
public interface RouteMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "routeId", expression = "java(config.getRouteId())")
    @Mapping(target = "routeName", expression = "java(config.getRouteName())")
    @Mapping(target = "timeout", expression = "java(config.getTimeout())")
    @Mapping(target = "retryCount", expression = "java(config.getRetryCount())")
    @Mapping(target = "properties", expression = "java(JsonUtils.toJson(config.getProperties()))")
    @Mapping(target = "updatedAt", expression = "java(new Date())")
    RouteEntity toEntity(RouteConfig config);
}


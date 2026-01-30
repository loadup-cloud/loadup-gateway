package io.github.loadup.gateway.core.router;

/*-
 * #%L
 * LoadUp Gateway Core
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

import io.github.loadup.gateway.facade.config.GatewayProperties;
import io.github.loadup.gateway.facade.model.GatewayRequest;
import io.github.loadup.gateway.facade.model.RouteConfig;
import io.github.loadup.gateway.facade.spi.RepositoryPlugin;
import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;
import lombok.extern.slf4j.Slf4j;

/** Route resolver */
@Slf4j
public class RouteResolver {

  private final RepositoryPlugin repositoryPlugin;
  private final GatewayProperties gatewayProperties;

  public RouteResolver(RepositoryPlugin repositoryPlugin, GatewayProperties gatewayProperties) {
    this.repositoryPlugin = repositoryPlugin;
    this.gatewayProperties = gatewayProperties;
  }

  private final ConcurrentHashMap<String, RouteConfig> routeCache = new ConcurrentHashMap<>();
  private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

  @PostConstruct
  public void refresh() {
    repositoryPlugin.initialize();
    //    this.refreshRoutes();
    // Periodically refresh route cache (hot reload support)
    //        scheduler.scheduleAtFixedRate(this::refreshRoutes, 30, 30, TimeUnit.SECONDS);
  }

  /** Resolve the route configuration for the given request */
  public Optional<RouteConfig> resolve(GatewayRequest request) {
    String routeKey = buildRouteKey(request.getPath(), request.getMethod());

    // First check cache
    RouteConfig cachedRoute = routeCache.get(routeKey);
    if (cachedRoute != null && cachedRoute.isEnabled()) {
      return Optional.of(cachedRoute);
    }

    // Look up from storage
    try {
      Optional<RouteConfig> routeOpt =
          repositoryPlugin.getRouteByPath(request.getPath(), request.getMethod());
      if (routeOpt.isPresent() && routeOpt.get().isEnabled()) {
        // Update cache
        routeCache.put(routeKey, routeOpt.get());
        return routeOpt;
      }
    } catch (Exception e) {
      log.error("Failed to resolve route from repository", e);
    }

    return Optional.empty();
  }

  /** Refresh route cache */
  public void refreshRoutes() {
    try {
      List<RouteConfig> allRoutes = repositoryPlugin.getAllRoutes();

      // Clear old cache
      routeCache.clear();

      // Reload routes
      for (RouteConfig route : allRoutes) {
        if (route.isEnabled()) {
          String routeKey = buildRouteKey(route.getPath(), route.getMethod());
          routeCache.put(routeKey, route);
        }
      }

      log.info("Route cache refreshed, loaded {} routes", routeCache.size());

    } catch (Exception e) {
      log.error("Failed to refresh route cache", e);
    }
  }

  /** Build route cache key */
  private String buildRouteKey(String path, String method) {
    return method + ":" + path;
  }

  /** Get the number of cached routes */
  public int getCachedRouteCount() {
    return routeCache.size();
  }
}

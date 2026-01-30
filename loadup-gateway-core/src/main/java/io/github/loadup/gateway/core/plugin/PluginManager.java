package io.github.loadup.gateway.core.plugin;

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

import io.github.loadup.gateway.facade.model.GatewayRequest;
import io.github.loadup.gateway.facade.model.GatewayResponse;
import io.github.loadup.gateway.facade.model.RouteConfig;
import io.github.loadup.gateway.facade.spi.ProxyProcessor;
import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/** Plugin manager */
@Slf4j
public class PluginManager {

  private final List<ProxyProcessor> proxyProcessors;

  public PluginManager(List<ProxyProcessor> proxyProcessors) {
    this.proxyProcessors = proxyProcessors;
  }

  private final Map<String, ProxyProcessor> processorMap = new ConcurrentHashMap<>();

  @PostConstruct
  public void init() {
    if (proxyProcessors != null) {
      for (ProxyProcessor processor : proxyProcessors) {
        String protocol = processor.getSupportedProtocol();
        if (processorMap.containsKey(protocol)) {
          log.warn(
              "Duplicate protocol processor found for: {}, overwriting with {}",
              protocol,
              processor.getClass().getName());
        }
        processorMap.put(protocol, processor);
      }
    }
    log.info(
        "Initialized PluginManager with {} processors: {}",
        processorMap.size(),
        processorMap.keySet());
  }

  /** Execute proxy forwarding */
  public GatewayResponse executeProxy(GatewayRequest request, RouteConfig route) throws Exception {
    if (StringUtils.isBlank(route.getProtocol())) {
      throw new RuntimeException("No protocol found!");
    }
    ProxyProcessor plugin = processorMap.get(route.getProtocol());
    if (plugin == null) {
      throw new RuntimeException("No proxy plugin found for protocol: " + route.getProtocol());
    }

    log.debug(
        "Executing proxy with plugin: {} for route: {}", plugin.getName(), route.getRouteId());
    return plugin.proxy(request, route);
  }

  /** Find the proxy plugin for the given protocol */
  @Deprecated
  private Optional<ProxyProcessor> findProxyPlugin(String protocol) {
    return Optional.ofNullable(processorMap.get(protocol));
  }
}

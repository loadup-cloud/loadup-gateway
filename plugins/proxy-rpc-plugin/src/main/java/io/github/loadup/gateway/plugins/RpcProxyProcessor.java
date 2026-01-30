package io.github.loadup.gateway.plugins;

/*-
 * #%L
 * Proxy RPC Plugin
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
import io.github.loadup.gateway.facade.constants.GatewayConstants;
import io.github.loadup.gateway.facade.model.GatewayRequest;
import io.github.loadup.gateway.facade.model.GatewayResponse;
import io.github.loadup.gateway.facade.model.RouteConfig;
import io.github.loadup.gateway.facade.spi.ProxyProcessor;
import io.github.loadup.gateway.facade.utils.JsonUtils;
import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.*;
import org.apache.dubbo.rpc.service.GenericService;
import org.springframework.stereotype.Component;

/** Dubbo RPC proxy plugin */
@Slf4j
@Component
public class RpcProxyProcessor implements ProxyProcessor {

  private ApplicationConfig applicationConfig;
  private RegistryConfig registryConfig;
  private Map<String, GenericService> serviceCache = new HashMap<>();

  @Override
  public String getName() {
    return "RpcProxyPlugin";
  }

  @Override
  public String getType() {
    return "PROXY";
  }

  @Override
  public String getVersion() {
    return "1.0.0";
  }

  @Override
  public int getPriority() {
    return 300;
  }

  @Resource private GatewayProperties gatewayProperties;

  @Override
  public void initialize() {
    log.info("RpcProxyPlugin initialized");

    // Initialize Dubbo configuration
    applicationConfig = new ApplicationConfig();
    applicationConfig.setName("loadup-gateway");

    registryConfig = new RegistryConfig();
    String registryAddress = gatewayProperties.getProxyPlugins().getRpc().getRegistryAddress();
    registryConfig.setAddress(registryAddress);
  }

  @Override
  public GatewayResponse proxy(GatewayRequest request, RouteConfig route) throws Exception {
    try {
      String target = route.getTargetUrl();
      // Parse target format: interfaceName:methodName:version
      String[] parts = target.split(":");
      if (parts.length < 2) {
        throw new IllegalArgumentException(
            "Invalid RPC target format. Expected: interfaceName:methodName[:version]");
      }

      String interfaceName = parts[0];
      String methodName = parts[1];
      String version = parts.length > 2 ? parts[2] : null;

      // Get generic service
      GenericService genericService = getGenericService(interfaceName, version);

      // Prepare arguments
      Object[] args = prepareRpcArgs(request);
      String[] parameterTypes = getParameterTypes(request);

      // Invoke RPC service
      Object result = genericService.$invoke(methodName, parameterTypes, args);

      // Build response
      return GatewayResponse.builder()
          .requestId(request.getRequestId())
          .statusCode(GatewayConstants.Status.SUCCESS)
          .headers(new HashMap<>())
          .body(JsonUtils.toJson(result))
          .contentType(GatewayConstants.ContentType.JSON)
          .responseTime(LocalDateTime.now())
          .build();

    } catch (Exception e) {
      log.error("RPC proxy failed", e);
      return GatewayResponse.builder()
          .requestId(request.getRequestId())
          .statusCode(GatewayConstants.Status.INTERNAL_ERROR)
          .body("{\"error\":\"RPC proxy failed\",\"message\":\"" + e.getMessage() + "\"}")
          .contentType(GatewayConstants.ContentType.JSON)
          .responseTime(LocalDateTime.now())
          .errorMessage(e.getMessage())
          .build();
    }
  }

  @Override
  public void destroy() {
    log.info("RpcProxyPlugin destroyed");
    serviceCache.clear();
  }

  @Override
  public String getSupportedProtocol() {
    return GatewayConstants.Protocol.RPC;
  }

  /** Get generic service */
  private GenericService getGenericService(String interfaceName, String version) {
    String cacheKey = interfaceName + ":" + (version != null ? version : "");

    return serviceCache.computeIfAbsent(
        cacheKey,
        k -> {
          ReferenceConfig<GenericService> reference = new ReferenceConfig<>();
          reference.setApplication(applicationConfig);
          reference.setRegistry(registryConfig);
          reference.setInterface(interfaceName);
          reference.setGeneric(true);
          if (version != null) {
            reference.setVersion(version);
          }

          return reference.get();
        });
  }

  /** Prepare RPC call arguments */
  private Object[] prepareRpcArgs(GatewayRequest request) {
    if (request.getBody() == null || request.getBody().trim().isEmpty()) {
      return new Object[0];
    }

    try {
      // Try parsing as JSON array
      if (request.getBody().trim().startsWith("[")) {
        return JsonUtils.fromJson(request.getBody(), Object[].class);
      } else {
        // Single argument
        return new Object[] {JsonUtils.toMap(request.getBody())};
      }
    } catch (Exception e) {
      log.warn("Failed to parse RPC args from request body", e);
      return new Object[] {request.getBody()};
    }
  }

  /** Get parameter types */
  private String[] getParameterTypes(GatewayRequest request) {
    // Simplified handling; real-world usage may require more complex type inference
    Object[] args = prepareRpcArgs(request);
    String[] types = new String[args.length];

    for (int i = 0; i < args.length; i++) {
      if (args[i] instanceof Map) {
        types[i] = "java.util.Map";
      } else if (args[i] instanceof String) {
        types[i] = "java.lang.String";
      } else {
        types[i] = "java.lang.Object";
      }
    }

    return types;
  }
}

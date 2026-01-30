package io.github.loadup.gateway.core.filter;

import io.github.loadup.gateway.facade.model.GatewayRequest;
import io.github.loadup.gateway.facade.model.GatewayResponse;
import io.github.loadup.gateway.facade.model.RouteConfig;

/**
 * Gateway filter interface (Chain pattern)
 */
public interface GatewayFilter {

  /**
   * Filter method
   * @param request Gateway request
   * @param route Route configuration
   * @param chain Filter chain
   * @return Gateway response
   */
  GatewayResponse filter(GatewayRequest request, RouteConfig route, GatewayFilterChain chain);

  /**
   * Get filter order
   * @return order (lower value has higher priority)
   */
  default int getOrder() {
    return 0;
  }
}

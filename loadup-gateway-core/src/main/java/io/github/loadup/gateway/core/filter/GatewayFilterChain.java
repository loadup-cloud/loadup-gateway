package io.github.loadup.gateway.core.filter;

import io.github.loadup.gateway.facade.model.GatewayRequest;
import io.github.loadup.gateway.facade.model.GatewayResponse;
import io.github.loadup.gateway.facade.model.RouteConfig;

/** Filter chain interface */
public interface GatewayFilterChain {

  /**
   * Continue filter chain
   *
   * @param request Gateway request
   * @param route Route configuration
   * @return Gateway response
   */
  GatewayResponse filter(GatewayRequest request, RouteConfig route);
}

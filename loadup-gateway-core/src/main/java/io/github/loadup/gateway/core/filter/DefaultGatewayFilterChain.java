package io.github.loadup.gateway.core.filter;

import io.github.loadup.gateway.facade.model.GatewayRequest;
import io.github.loadup.gateway.facade.model.GatewayResponse;
import io.github.loadup.gateway.facade.model.RouteConfig;
import java.util.List;

/** Default implementation of GatewayFilterChain */
public class DefaultGatewayFilterChain implements GatewayFilterChain {

  private final List<GatewayFilter> filters;
  private final int index;

  public DefaultGatewayFilterChain(List<GatewayFilter> filters) {
    this(filters, 0);
  }

  private DefaultGatewayFilterChain(List<GatewayFilter> filters, int index) {
    this.filters = filters;
    this.index = index;
  }

  @Override
  public GatewayResponse filter(GatewayRequest request, RouteConfig route) {
    if (this.index < filters.size()) {
      GatewayFilter filter = filters.get(this.index);
      GatewayFilterChain nextChain = new DefaultGatewayFilterChain(filters, this.index + 1);
      return filter.filter(request, route, nextChain);
    }
    return null; // End of chain
  }
}

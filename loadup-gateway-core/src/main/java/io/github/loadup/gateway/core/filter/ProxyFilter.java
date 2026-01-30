package io.github.loadup.gateway.core.filter;

import io.github.loadup.gateway.core.plugin.PluginManager;
import io.github.loadup.gateway.facade.exception.GatewayException;
import io.github.loadup.gateway.facade.exception.GatewayExceptionFactory;
import io.github.loadup.gateway.facade.model.GatewayRequest;
import io.github.loadup.gateway.facade.model.GatewayResponse;
import io.github.loadup.gateway.facade.model.RouteConfig;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

/** Filter for executing proxy plugins */
@Slf4j
@Component
public class ProxyFilter implements GatewayFilter, Ordered {

  @Resource private PluginManager pluginManager;

  @Override
  public GatewayResponse filter(
      GatewayRequest request, RouteConfig route, GatewayFilterChain chain) {
    try {
      GatewayResponse response = pluginManager.executeProxy(request, route);
      // If the chain continues (e.g. for post-processing filters that don't just transform response
      // but do other things),
      // we generally return the response here because Proxy is usually terminal for obtaining the
      // response.
      // However, the chain pattern in this project seems to be recursive: filter returns Response.
      // If we are the "execute" step, we get the response.

      // But we still need to allow subsequent filters (like Response Template) to run.
      // So we should NOT call chain.filter() *after* getting response?
      // Wait, in a standard chain (like Servlet Filter), you call chain.doFilter() to pass request
      // down, and then process result on way up.
      // But here `executeProxy` produces the response.
      // So if I call `chain.filter()`, what does it do?
      // If there are filters *after* me, they expect to run.

      // Actually, typically "Proxy" is the implementation that *returns* the response.
      // Filters *wrap* the proxy.
      // So ProxyFilter should probably be the LAST filter in the chain that produces the initial
      // response.
      // But if there's a chain, "nextChain.filter()" needs to be called to proceed.
      // If `ProxyFilter` produces the response, it shouldn't call `nextChain.filter` unless
      // `nextChain` implementation is designed to handle it?
      // In this design: `filter(req, route, chain)` returns `GatewayResponse`.

      // If ProxyFilter is the one fetching data, it shouldn't call chain.filter() unless it wants
      // to delegate.
      // So ProxyFilter should be at the end of the chain logic?
      // Or, subsequent filters (Response Template) should wrap this?

      // If I look at `DefaultGatewayFilterChain`: one calls `next.filter()`.
      // If ProxyFilter returns a response without calling `next.filter()`, then any filters *after*
      // it in the list will NOT be executed?

      // Correct. In a chain where you iterate a list:
      // Filter 1 calls next.filter() -> Filter 2
      // Filter 2 calls next.filter() -> Filter 3 (Proxy)
      // Filter 3 does work, returns Response.
      // Filter 2 receives response, modifies it, returns.
      // Filter 1 receive response, returns.

      // So ProxyFilter should strictly be the "last" filter effectively, or at least the one that
      // stops the chain propagation downwards and starts the return journey.

      return response;

    } catch (GatewayException e) {
      throw e;
    } catch (Exception e) {
      log.error("Proxy execution failed", e);
      throw GatewayExceptionFactory.wrap(
          e, "PROXY_EXECUTION", "Proxy execution failed: " + route.getTarget());
    }
  }

  @Override
  public int getOrder() {
    return Ordered.LOWEST_PRECEDENCE - 100; // Run late, acts as terminal
  }
}

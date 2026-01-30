package io.github.loadup.gateway.core.action;

import io.github.loadup.gateway.core.router.RouteResolver;
import io.github.loadup.gateway.facade.context.GatewayContext;
import io.github.loadup.gateway.facade.exception.GatewayExceptionFactory;
import io.github.loadup.gateway.facade.model.RouteConfig;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;

/** Action to resolve the route for the request. */
@Slf4j
public class RouteAction implements GatewayAction {

  private final RouteResolver routeResolver;

  public RouteAction(RouteResolver routeResolver) {
    this.routeResolver = routeResolver;
  }

  @Override
  public void execute(GatewayContext context, GatewayActionChain chain) {
    // Resolve route
    Optional<RouteConfig> routeOpt = routeResolver.resolve(context.getRequest());

    if (!routeOpt.isPresent()) {
      // Throw exception to be handled by the adapter
      throw GatewayExceptionFactory.routeNotFound(context.getRequest().getPath());
    }

    RouteConfig route = routeOpt.get();
    log.debug("Route resolved: {} -> {}", context.getRequest().getPath(), route.getRouteId());

    // Store route in context
    context.setRoute(route);

    // Proceed
    chain.proceed(context);
  }

  @Override
  public int getOrder() {
    return Ordered.HIGHEST_PRECEDENCE + 1000;
  }
}

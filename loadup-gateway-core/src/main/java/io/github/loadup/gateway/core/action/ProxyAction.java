package io.github.loadup.gateway.core.action;

import io.github.loadup.gateway.core.plugin.PluginManager;
import io.github.loadup.gateway.facade.context.GatewayContext;
import io.github.loadup.gateway.facade.model.GatewayResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;

/** Action to execute the proxy request. */
@Slf4j
public class ProxyAction implements GatewayAction {

  private final PluginManager pluginManager;

  public ProxyAction(PluginManager pluginManager) {
    this.pluginManager = pluginManager;
  }

  @Override
  public void execute(GatewayContext context, GatewayActionChain chain) {
    try {
      // Execute proxy through plugin manager
      GatewayResponse response =
          pluginManager.executeProxy(context.getRequest(), context.getRoute());

      // Set response in context
      context.setResponse(response);

      // Proceed (though typically this is the end)
      chain.proceed(context);
    } catch (Exception e) {
      // Re-throw to be handled by adapter (or wrap if needed, but adapter handles generic
      // Exception)
      throw new RuntimeException("Proxy execution failed", e);
    }
  }

  public int getOrder() {
    return Ordered.LOWEST_PRECEDENCE - 1000;
  }
}

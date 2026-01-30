package io.github.loadup.gateway.core.action;

import io.github.loadup.gateway.facade.context.GatewayContext;
import org.springframework.core.Ordered;

/** Interface for gateway actions in the processing chain. */
public interface GatewayAction extends Ordered {

  /**
   * Execute the action.
   *
   * @param context the gateway context
   * @param chain the action chain
   */
  void execute(GatewayContext context, GatewayActionChain chain);
}

package io.github.loadup.gateway.core.action;

import io.github.loadup.gateway.facade.context.GatewayContext;

/** Interface for gateway actions in the processing chain. */
public interface GatewayAction {

  /**
   * Execute the action.
   *
   * @param context the gateway context
   * @param chain the action chain
   */
  void execute(GatewayContext context, GatewayActionChain chain);
}

package io.github.loadup.gateway.core.action;

import io.github.loadup.gateway.facade.context.GatewayContext;

/** Interface for the chain of gateway actions. */
public interface GatewayActionChain {

  /**
   * Proceed to the next action in the chain.
   *
   * @param context the gateway context
   */
  void proceed(GatewayContext context);
}

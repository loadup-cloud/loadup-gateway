package io.github.loadup.gateway.core.action;

import io.github.loadup.gateway.facade.context.GatewayContext;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/** Default implementation of GatewayActionChain. */
public class DefaultGatewayActionChain implements GatewayActionChain {

  private final List<GatewayAction> actions;
  private final AtomicInteger index = new AtomicInteger(0);

  public DefaultGatewayActionChain(List<GatewayAction> actions) {
    this.actions = actions;
  }

  @Override
  public void proceed(GatewayContext context) {
    if (index.get() < actions.size()) {
      GatewayAction action = actions.get(index.getAndIncrement());
      action.execute(context, this);
    }
  }
}

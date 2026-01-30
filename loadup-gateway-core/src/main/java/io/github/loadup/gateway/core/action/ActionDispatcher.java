package io.github.loadup.gateway.core.action;

import io.github.loadup.gateway.facade.context.GatewayContext;
import java.util.Comparator;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/** Action dispatcher */
@Slf4j
public class ActionDispatcher {

  private final List<GatewayAction> actions;

  public ActionDispatcher(List<GatewayAction> actions) {
    this.actions = actions;
  }

  /** Dispatch request to appropriate handler */
  public void dispatch(GatewayContext context) {
    if (actions == null || actions.isEmpty()) {
      log.warn("No actions configured for dispatcher");
      return;
    }

    // Sort actions
    actions.sort(Comparator.comparingInt(GatewayAction::getOrder));

    // Execute Chain
    new DefaultGatewayActionChain(actions).proceed(context);
  }
}

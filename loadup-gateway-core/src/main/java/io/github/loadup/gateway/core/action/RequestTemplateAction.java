package io.github.loadup.gateway.core.action;

import io.github.loadup.gateway.core.template.TemplateEngine;
import io.github.loadup.gateway.facade.context.GatewayContext;
import io.github.loadup.gateway.facade.exception.GatewayExceptionFactory;
import io.github.loadup.gateway.facade.model.GatewayRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;

/** Action for request template processing */
@Slf4j
public class RequestTemplateAction implements GatewayAction {

  private final TemplateEngine templateEngine;

  public RequestTemplateAction(TemplateEngine templateEngine) {
    this.templateEngine = templateEngine;
  }

  @Override
  public void execute(GatewayContext context, GatewayActionChain chain) {
    if (context.getRoute().getRequestTemplate() != null) {
      try {
        GatewayRequest processedRequest =
            templateEngine.processRequestTemplate(
                context.getRequest(), context.getRoute().getRequestTemplate());
        context.setRequest(processedRequest);
      } catch (Exception e) {
        log.warn("Request template processing failed", e);
        throw GatewayExceptionFactory.templateExecutionError(
            context.getRoute().getRequestTemplate(), e);
      }
    }
    chain.proceed(context);
  }

  public int getOrder() {
    return Ordered.HIGHEST_PRECEDENCE + 2000; // Early in the chain
  }
}

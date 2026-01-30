package io.github.loadup.gateway.core.action;

import io.github.loadup.gateway.core.template.TemplateEngine;
import io.github.loadup.gateway.facade.context.GatewayContext;
import io.github.loadup.gateway.facade.exception.GatewayExceptionFactory;
import io.github.loadup.gateway.facade.model.GatewayResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;

/** Action for response template processing */
@Slf4j
public class ResponseTemplateAction implements GatewayAction {

  private final TemplateEngine templateEngine;

  public ResponseTemplateAction(TemplateEngine templateEngine) {
    this.templateEngine = templateEngine;
  }

  @Override
  public void execute(GatewayContext context, GatewayActionChain chain) {
    // Proceed chain first to get response
    chain.proceed(context);

    // Post-process response
    if (context.getResponse() != null && context.getRoute().getResponseTemplate() != null) {
      try {
        GatewayResponse processedResponse =
            templateEngine.processResponseTemplate(
                context.getResponse(), context.getRoute().getResponseTemplate());
        context.setResponse(processedResponse);
      } catch (Exception e) {
        log.warn("Response template processing failed", e);
        throw GatewayExceptionFactory.templateExecutionError(
            context.getRoute().getResponseTemplate(), e);
      }
    }
  }

  public int getOrder() {
    return Ordered.LOWEST_PRECEDENCE - 2000;
  }
}

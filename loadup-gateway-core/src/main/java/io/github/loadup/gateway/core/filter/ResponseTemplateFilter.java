package io.github.loadup.gateway.core.filter;

import io.github.loadup.gateway.core.template.TemplateEngine;
import io.github.loadup.gateway.facade.exception.GatewayExceptionFactory;
import io.github.loadup.gateway.facade.model.GatewayRequest;
import io.github.loadup.gateway.facade.model.GatewayResponse;
import io.github.loadup.gateway.facade.model.RouteConfig;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

/** Filter for response template processing */
@Slf4j
@Component
public class ResponseTemplateFilter implements GatewayFilter, Ordered {

  @Resource private TemplateEngine templateEngine;

  @Override
  public GatewayResponse filter(
      GatewayRequest request, RouteConfig route, GatewayFilterChain chain) {
    // Execute chain to get response first
    GatewayResponse response = chain.filter(request, route);

    // Post-process response
    if (route.getResponseTemplate() != null) {
      try {
        return templateEngine.processResponseTemplate(response, route.getResponseTemplate());
      } catch (Exception e) {
        log.warn("Response template processing failed", e);
        throw GatewayExceptionFactory.templateExecutionError(route.getResponseTemplate(), e);
      }
    }
    return response;
  }

  @Override
  public int getOrder() {
    return 300;
  }
}

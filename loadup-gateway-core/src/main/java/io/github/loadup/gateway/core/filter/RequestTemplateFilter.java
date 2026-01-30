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

/**
 * Filter for request template processing
 */
@Slf4j
@Component
public class RequestTemplateFilter implements GatewayFilter, Ordered {

  @Resource private TemplateEngine templateEngine;

  @Override
  public GatewayResponse filter(GatewayRequest request, RouteConfig route, GatewayFilterChain chain) {
    GatewayRequest processedRequest = request;
    if (route.getRequestTemplate() != null) {
      try {
        processedRequest = templateEngine.processRequestTemplate(request, route.getRequestTemplate());
      } catch (Exception e) {
        log.warn("Request template processing failed", e);
        throw GatewayExceptionFactory.templateExecutionError(route.getRequestTemplate(), e);
      }
    }
    return chain.filter(processedRequest, route);
  }

  @Override
  public int getOrder() {
    return 100; // Early in the chain
  }
}

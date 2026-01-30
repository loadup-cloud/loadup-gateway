package io.github.loadup.gateway.core.action;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.loadup.gateway.facade.config.GatewayProperties;
import io.github.loadup.gateway.facade.context.GatewayContext;
import io.github.loadup.gateway.facade.model.GatewayResponse;
import io.github.loadup.gateway.facade.model.Result;
import io.github.loadup.gateway.facade.model.RouteConfig;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;

/** Action to wrap response in standard format */
@Slf4j
public class ResponseWrapperAction implements GatewayAction {

  private final GatewayProperties gatewayProperties;
  private final ObjectMapper objectMapper;

  public ResponseWrapperAction(GatewayProperties gatewayProperties, ObjectMapper objectMapper) {
    this.gatewayProperties = gatewayProperties;
    this.objectMapper = objectMapper;
  }

  @Override
  public void execute(GatewayContext context, GatewayActionChain chain) {
    chain.proceed(context);

    GatewayResponse response = context.getResponse();
    RouteConfig route = context.getRoute();
    if (response != null
        && route != null
        && route.getWrapResponse() != null
        && route.getWrapResponse()) {
      try {
        // Assume response body is JSON, wrap it in Result
        Object data = null;
        if (response.getBody() != null) {
          try {
            data = objectMapper.readValue(response.getBody(), Object.class);
          } catch (Exception e) {
            data = response.getBody();
          }
        }

        Result<Object> result = Result.success(data);
        String newBody = objectMapper.writeValueAsString(result);

        response.setBody(newBody);
        if (response.getHeaders() == null) {
          response.setHeaders(new HashMap<>());
        }
        response.getHeaders().put("Content-Type", "application/json");
        response
            .getHeaders()
            .put("Content-Length", String.valueOf(newBody.getBytes(StandardCharsets.UTF_8).length));

      } catch (Exception e) {
        log.error("Failed to wrap response", e);
        // On error, we might leave the response as is or set error response
      }
    }
  }

  @Override
  public int getOrder() {
    // Execute before ResponseTemplateAction (-2000) and ProxyAction (-1000)
    // So that on the return path (post-processing), it corresponds to:
    // Proxy (returns raw) -> Template (transforms) -> Wrapper (wraps)
    return Ordered.LOWEST_PRECEDENCE - 3000;
  }
}

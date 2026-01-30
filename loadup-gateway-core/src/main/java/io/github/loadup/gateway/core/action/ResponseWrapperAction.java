package io.github.loadup.gateway.core.action;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.loadup.gateway.facade.config.GatewayProperties;
import io.github.loadup.gateway.facade.context.GatewayContext;
import io.github.loadup.gateway.facade.model.GatewayResponse;
import io.github.loadup.gateway.facade.model.RouteConfig;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j; // ...existing code...
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

    boolean shouldWrap = false;
    if (response != null && route != null) {
      if (route.getWrapResponse() != null) {
        shouldWrap = route.getWrapResponse();
      } else if (gatewayProperties.getResponse() != null) {
        shouldWrap = gatewayProperties.getResponse().isWrap();
      }
    }

    if (shouldWrap) {
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

        Map<String, Object> wrapper = new LinkedHashMap<>();

        // 1. Result block
        if (gatewayProperties.getResponse().isResult()) {
          Map<String, Object> result = new LinkedHashMap<>();
          // Use response status code or default to 200/success logic
          int statusCode = response.getStatusCode();
          result.put("code", statusCode == 0 ? 200 : statusCode);
          result.put("status", (statusCode >= 200 && statusCode < 300) ? "success" : "error");
          result.put(
              "message",
              (statusCode >= 200 && statusCode < 300)
                  ? "Request processed successfully"
                  : "Request processed with error");
          wrapper.put("result", result);
        }

        // 2. Data block
        wrapper.put("data", data);

        // 3. Meta block
        if (gatewayProperties.getResponse().isMeta()) {
          Map<String, Object> meta = new LinkedHashMap<>();
          if (context.getRequest() != null) {
            meta.put("requestId", context.getRequest().getRequestId());
            if (context.getRequest().getRequestTime() != null) {
              meta.put("timestamp", context.getRequest().getRequestTime().toString());
            } else {
              meta.put("timestamp", java.time.LocalDateTime.now().toString());
            }
          }
          wrapper.put("meta", meta);
        }

        String newBody = objectMapper.writeValueAsString(wrapper);

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

  public int getOrder() {
    return Ordered.LOWEST_PRECEDENCE - 3000;
  }
}

package io.github.loadup.gateway.core.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.loadup.gateway.facade.config.GatewayProperties;
import io.github.loadup.gateway.facade.model.GatewayRequest;
import io.github.loadup.gateway.facade.model.GatewayResponse;
import io.github.loadup.gateway.facade.model.Result;
import io.github.loadup.gateway.facade.model.RouteConfig;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

/** Filter for wrapping response */
@Slf4j
@Component
public class ResponseWrapperFilter implements GatewayFilter, Ordered {

  @Resource private GatewayProperties gatewayProperties;
  @Resource private ObjectMapper objectMapper;

  @Override
  public GatewayResponse filter(
      GatewayRequest request, RouteConfig route, GatewayFilterChain chain) {
    // Execute chain
    GatewayResponse response = chain.filter(request, route);

    // Wrap response
    boolean wrap =
        gatewayProperties.getResponse() != null && gatewayProperties.getResponse().isWrap();
    if (route.getWrapResponse() != null) {
      wrap = route.getWrapResponse();
    }
    if (wrap) {
      return wrapGatewayResponse(response);
    }
    return response;
  }

  private GatewayResponse wrapGatewayResponse(GatewayResponse response) {
    try {
      ObjectNode root = objectMapper.createObjectNode();
      // Build result object
      Result result =
          Result.builder()
              .code(String.valueOf(response.getStatusCode()))
              .status(response.getStatusCode() == 200 ? "success" : "error")
              .message(
                  response.getHeaders() != null && response.getHeaders().containsKey("X-Message")
                      ? response.getHeaders().get("X-Message")
                      : "")
              .build();
      root.set("result", objectMapper.valueToTree(result));
      // Merge business data
      if (response.getBody() != null
          && response.getContentType() != null
          && response.getContentType().contains("json")) {
        try {
          ObjectNode dataNode = (ObjectNode) objectMapper.readTree(response.getBody());
          dataNode
              .fieldNames()
              .forEachRemaining(
                  field -> {
                    if (!"result".equals(field)) {
                      root.set(field, dataNode.get(field));
                    }
                  });
        } catch (Exception e) {
          // body is not standard json, return as data field
          root.put("data", response.getBody());
        }
      } else if (response.getBody() != null) {
        root.put("data", response.getBody());
      }
      response.setBody(objectMapper.writeValueAsString(root));
      response.setContentType("application/json;charset=UTF-8");
    } catch (Exception e) {
      // Wrap failed，Fallback to original body
      log.warn("Failed to wrap response", e);
    }
    return response;
  }

  @Override
  public int getOrder() {
    return 200; // Runs before Template Filter (meaning it wraps invocation of template filter? No.)
    // If Order is 200. List: [Wrapper(200), Template(300), Proxy(MAX)].
    // Wrapper calls next -> Template.
    // Template calls next -> Proxy.
    // Proxy returns.
    // Template runs on return.
    // Wrapper runs on return of Template.
    // Result: The processed response from Template is then Wrapped.
    // This seems correct: Transform then Wrap.
  }
}

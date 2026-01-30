package io.github.loadup.gateway.core.handler;

import io.github.loadup.gateway.core.action.ActionDispatcher;
import io.github.loadup.gateway.facade.context.GatewayContext;
import io.github.loadup.gateway.facade.exception.ExceptionHandler;
import io.github.loadup.gateway.facade.exception.GatewayException;
import io.github.loadup.gateway.facade.exception.GatewayExceptionFactory;
import io.github.loadup.gateway.facade.model.GatewayRequest;
import io.github.loadup.gateway.facade.model.GatewayResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.HandlerAdapter;
import org.springframework.web.servlet.ModelAndView;

public class GatewayHandlerAdapter implements HandlerAdapter, Ordered {

  private final ActionDispatcher actionDispatcher;

  public GatewayHandlerAdapter(ActionDispatcher actionDispatcher) {
    this.actionDispatcher = actionDispatcher;
  }

  @Override
  public boolean supports(Object handler) {
    return handler instanceof GatewayHandler;
  }

  @Override
  public ModelAndView handle(
      HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
    GatewayHandler gatewayHandler = (GatewayHandler) handler;

    // 1. Build GatewayContext
    GatewayContext context = buildGatewayContext(request, response, gatewayHandler);

    try {
      // 2. Call core dispatcher
      actionDispatcher.dispatch(context);
    } catch (Exception e) {
      // Log and handle global exception if not handled in chain
      GatewayException wrapped = GatewayExceptionFactory.wrap(e, "HANDLER_ADAPTER");
      GatewayResponse errorResponse =
          ExceptionHandler.handleException(context.getRequest().getRequestId(), wrapped, 0);
      context.setResponse(errorResponse);
      context.setException(e);
    }

    // 3. Write response
    if (context.getResponse() != null) {
      writeResponse(response, context.getResponse());
    } else {
      // Fallback for null response
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "No response generated");
    }

    // Return null to indicate request handled
    return null;
  }

  @Override
  public long getLastModified(HttpServletRequest request, Object handler) {
    return -1;
  }

  private GatewayContext buildGatewayContext(
      HttpServletRequest request, HttpServletResponse response, GatewayHandler handler)
      throws IOException {
    GatewayRequest gatewayRequest = buildGatewayRequest(request);
    return GatewayContext.builder()
        .request(gatewayRequest)
        .originalRequest(request)
        .originalResponse(response)
        .build();
  }

  private GatewayRequest buildGatewayRequest(HttpServletRequest request) throws IOException {

    // Get request headers
    Map<String, String> headers = new HashMap<>();
    Enumeration<String> headerNames = request.getHeaderNames();
    while (headerNames.hasMoreElements()) {
      String headerName = headerNames.nextElement();
      headers.put(headerName, request.getHeader(headerName));
    }

    // Get query parameters
    Map<String, List<String>> queryParams = new HashMap<>();
    if (request.getQueryString() != null) {
      Arrays.stream(request.getQueryString().split("&"))
          .forEach(
              param -> {
                String[] kv = param.split("=", 2);
                if (kv.length == 2) {
                  queryParams.computeIfAbsent(kv[0], k -> new ArrayList<>()).add(kv[1]);
                }
              });
    }

    // Read body
    String body = request.getReader().lines().collect(Collectors.joining("\n"));

    return GatewayRequest.builder()
        .requestId(UUID.randomUUID().toString())
        .path(request.getRequestURI())
        .method(request.getMethod())
        .headers(headers)
        .queryParameters(queryParams)
        .body(body)
        .contentType(request.getContentType())
        .clientIp("*")
        .userAgent(request.getHeader("User-Agent"))
        .requestTime(LocalDateTime.now())
        .attributes(new HashMap<>())
        .build();
  }

  /** Write the HTTP response */
  private void writeResponse(HttpServletResponse response, GatewayResponse gatewayResponse)
      throws IOException {

    response.setStatus(gatewayResponse.getStatusCode());

    // Set response headers
    if (gatewayResponse.getHeaders() != null) {
      gatewayResponse
          .getHeaders()
          .forEach(
              (k, v) -> {
                if (k == null || v == null) return;
                String lower = k.toLowerCase(Locale.ROOT);
                if ("content-length".equals(lower) || "transfer-encoding".equals(lower)) {
                  // skip these; container will set proper values
                  return;
                }
                response.setHeader(k, v);
              });
    }

    // Set content type and ensure charset where appropriate (default to UTF-8 for JSON/text)
    if (gatewayResponse.getContentType() != null) {
      response.setContentType(gatewayResponse.getContentType());
      String currentEncoding = response.getCharacterEncoding();
      if ((currentEncoding == null || currentEncoding.isEmpty())) {
        String ct = gatewayResponse.getContentType().toLowerCase(Locale.ROOT);
        if (ct.startsWith("application/json")
            || ct.startsWith("text/")
            || ct.contains("json")
            || ct.contains("text")) {
          response.setCharacterEncoding("UTF-8");
        }
      }
    } else {
      // If no content type provided but body looks like JSON, default to
      // application/json;charset=UTF-8
      if (gatewayResponse.getBody() != null && gatewayResponse.getBody().trim().startsWith("{")) {
        response.setContentType("application/json;charset=UTF-8");
      }
    }

    // Write body
    if (gatewayResponse.getBody() != null) {
      response.getWriter().write(gatewayResponse.getBody());
      response.getWriter().flush();
    } else {
      // ensure writer is flushed even if no body
      response.getWriter().flush();
    }
  }

  @Override
  public int getOrder() {
    return Ordered.HIGHEST_PRECEDENCE;
  }
}

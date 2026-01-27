package io.github.loadup.gateway.core.filter;

/*-
 * #%L
 * LoadUp Gateway Core
 * %%
 * Copyright (C) 2025 LoadUp Gateway Authors
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import io.github.loadup.gateway.core.action.ActionDispatcher;
import io.github.loadup.gateway.facade.model.GatewayRequest;
import io.github.loadup.gateway.facade.model.GatewayResponse;
import io.github.loadup.gateway.facade.utils.CommonUtils;
import io.github.loadup.gateway.facade.utils.JsonUtils;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.TextMapGetter;
import io.opentelemetry.context.propagation.TextMapPropagator;
import jakarta.annotation.Resource;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Core gateway filter */
@Slf4j
@Component
public class GatewayFilter implements Filter {

  @Resource private ActionDispatcher actionDispatcher;

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {

    HttpServletRequest httpRequest = (HttpServletRequest) request;
    HttpServletResponse httpResponse = (HttpServletResponse) response;

    // Prepare OpenTelemetry propagator and tracer
    TextMapPropagator propagator = GlobalOpenTelemetry.getPropagators().getTextMapPropagator();
    TextMapGetter<HttpServletRequest> getter =
        new TextMapGetter<HttpServletRequest>() {
          @Override
          public Iterable<String> keys(HttpServletRequest carrier) {
            Enumeration<String> headerNames = carrier.getHeaderNames();
            if (headerNames == null) return Collections.emptyList();
            List<String> keys = new ArrayList<>();
            while (headerNames.hasMoreElements()) {
              keys.add(headerNames.nextElement());
            }
            return keys;
          }

          @Override
          public String get(HttpServletRequest carrier, String key) {
            if (carrier == null) return null;
            return carrier.getHeader(key);
          }
        };

    // Extract incoming context and start a server span. The span will inherit an upstream trace if
    // present.
    Context extractedContext = propagator.extract(Context.current(), httpRequest, getter);
    Tracer tracer = GlobalOpenTelemetry.getTracer("io.github.loadup.gateway.core");
    Span span =
        tracer
            .spanBuilder(httpRequest.getMethod() + " " + httpRequest.getRequestURI())
            .setSpanKind(SpanKind.SERVER)
            .setParent(extractedContext)
            .startSpan();

    // Use the span's trace id as requestId (will be upstream trace id if present, otherwise newly
    // generated)
    String requestId =
        span.getSpanContext() != null && span.getSpanContext().isValid()
            ? span.getSpanContext().getTraceId()
            : CommonUtils.generateRequestId();

    try (Scope scope = span.makeCurrent()) {

      try {
        // Build gateway request object
        GatewayRequest gatewayRequest = buildGatewayRequest(httpRequest, requestId);

        log.info(
            "Gateway processing request: {} {} with ID: {}",
            gatewayRequest.getMethod(),
            gatewayRequest.getPath(),
            requestId);

        // Dispatch to Action handler
        GatewayResponse gatewayResponse = actionDispatcher.dispatch(gatewayRequest);

        // Write response
        writeResponse(httpResponse, gatewayResponse);

      } catch (Exception e) {
        log.error("Gateway processing failed for request ID: {}", requestId, e);
        handleError(httpResponse, requestId, e);
      }

    } finally {
      // End the span when request processing is complete
      span.end();
    }
  }

  /** Build gateway request object */
  private GatewayRequest buildGatewayRequest(HttpServletRequest request, String requestId)
      throws IOException {

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
        .requestId(requestId)
        .path(request.getRequestURI())
        .method(request.getMethod())
        .headers(headers)
        .queryParameters(queryParams)
        .body(body)
        .contentType(request.getContentType())
        .clientIp(getClientIp(request))
        .userAgent(request.getHeader("User-Agent"))
        .requestTime(LocalDateTime.now())
        .attributes(new HashMap<>())
        .build();
  }

  /** Write the HTTP response */
  private void writeResponse(HttpServletResponse response, GatewayResponse gatewayResponse)
      throws IOException {

    response.setStatus(gatewayResponse.getStatusCode());

    // Set response headers, but don't forward hop-by-hop or conflicting headers that the servlet
    // container
    // should manage (for example Content-Length or Transfer-Encoding) — forwarding an incorrect
    // Content-Length from upstream can cause the client to receive truncated body.
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

    // Write body using the writer (body is modeled as String). Flushing after write ensures data is
    // sent.
    if (gatewayResponse.getBody() != null) {
      response.getWriter().write(gatewayResponse.getBody());
      response.getWriter().flush();
    } else {
      // ensure writer is flushed even if no body
      response.getWriter().flush();
    }
  }

  /** Handle errors */
  private void handleError(HttpServletResponse response, String requestId, Exception e)
      throws IOException {

    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    response.setContentType("application/json");

    Map<String, Object> errorResponse = new HashMap<>();
    errorResponse.put("requestId", requestId);
    errorResponse.put("error", "Internal Server Error");
    errorResponse.put("message", e.getMessage());
    errorResponse.put("timestamp", LocalDateTime.now());

    response.getWriter().write(JsonUtils.toJson(errorResponse));
    response.getWriter().flush();
  }

  /** Get client IP address */
  private String getClientIp(HttpServletRequest request) {
    String ip = request.getHeader("X-Forwarded-For");
    if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
      ip = request.getHeader("X-Real-IP");
    }
    if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
      ip = request.getRemoteAddr();
    }
    return ip;
  }
}

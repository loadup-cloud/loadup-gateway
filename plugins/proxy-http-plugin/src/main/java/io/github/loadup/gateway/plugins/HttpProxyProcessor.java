package io.github.loadup.gateway.plugins;

/*-
 * #%L
 * Proxy HTTP Plugin
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
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

import io.github.loadup.gateway.facade.constants.GatewayConstants;
import io.github.loadup.gateway.facade.model.GatewayRequest;
import io.github.loadup.gateway.facade.model.GatewayResponse;
import io.github.loadup.gateway.facade.model.RouteConfig;
import io.github.loadup.gateway.facade.spi.ProxyProcessor;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/** HTTP proxy plugin */
@Slf4j
@Component
public class HttpProxyProcessor implements ProxyProcessor {

  private final RestClient restClient = RestClient.create();

  @Override
  public String getName() {
    return "HttpProxyPlugin";
  }

  @Override
  public String getType() {
    return "PROXY";
  }

  @Override
  public String getVersion() {
    return "1.0.0";
  }

  @Override
  public int getPriority() {
    return 200;
  }

  @Override
  public void initialize() {
    log.info("HttpProxyPlugin initialized");
    // RestClient can be configured centrally via its @Bean
  }

  @Override
  public GatewayResponse proxy(GatewayRequest request, RouteConfig route) {
    try {
      String target = route.getTargetUrl();
      // Build request headers
      HttpHeaders headers = new HttpHeaders();
      if (request.getHeaders() != null) {
        request.getHeaders().forEach(headers::set);
      }

      // Determine HTTP method
      HttpMethod httpMethod = HttpMethod.valueOf(request.getMethod().toUpperCase());

      // Build full URL using UriComponentsBuilder to ensure encoding
      String fullUrl = buildFullUrl(target, request);

      log.debug("Proxying {} request to: {}", httpMethod, fullUrl);

      // Execute HTTP request using RestClient fluent API per method
      ResponseEntity<String> response;
      URI uri = URI.create(fullUrl);

      if (httpMethod == HttpMethod.GET) {
        response =
            restClient
                .get()
                .uri(uri)
                .headers(h -> h.putAll(headers))
                .retrieve()
                .toEntity(String.class);
      } else if (httpMethod == HttpMethod.POST) {
        response =
            restClient
                .post()
                .uri(uri)
                .headers(h -> h.putAll(headers))
                .body(request.getBody())
                .retrieve()
                .toEntity(String.class);
      } else if (httpMethod == HttpMethod.PUT) {
        response =
            restClient
                .put()
                .uri(uri)
                .headers(h -> h.putAll(headers))
                .body(request.getBody())
                .retrieve()
                .toEntity(String.class);
      } else if (httpMethod == HttpMethod.DELETE) {
        response =
            restClient
                .delete()
                .uri(uri)
                .headers(h -> h.putAll(headers))
                .retrieve()
                .toEntity(String.class);
      } else if (httpMethod == HttpMethod.PATCH) {
        response =
            restClient
                .patch()
                .uri(uri)
                .headers(h -> h.putAll(headers))
                .body(request.getBody())
                .retrieve()
                .toEntity(String.class);
      } else {
        // Fallback to POST if method is unknown
        response =
            restClient
                .post()
                .uri(uri)
                .headers(h -> h.putAll(headers))
                .body(request.getBody())
                .retrieve()
                .toEntity(String.class);
      }

      // Build gateway response
      Map<String, String> responseHeaders = new HashMap<>();
      response
          .getHeaders()
          .forEach(
              (key, values) -> {
                if (!values.isEmpty()) {
                  responseHeaders.put(key, values.get(0));
                }
              });

      return GatewayResponse.builder()
          .requestId(request.getRequestId())
          .statusCode(response.getStatusCode().value())
          .headers(responseHeaders)
          .body(response.getBody())
          .contentType(responseHeaders.get("Content-Type"))
          .responseTime(LocalDateTime.now())
          .build();

    } catch (Exception e) {
      log.error("HTTP proxy failed", e);
      return GatewayResponse.builder()
          .requestId(request.getRequestId())
          .statusCode(GatewayConstants.Status.INTERNAL_ERROR)
          .body("{\"error\":\"HTTP proxy failed\",\"message\":\"" + e.getMessage() + "\"}")
          .contentType(GatewayConstants.ContentType.JSON)
          .responseTime(LocalDateTime.now())
          .errorMessage(e.getMessage())
          .build();
    }
  }

  @Override
  public void destroy() {
    log.info("HttpProxyPlugin destroyed");
  }

  @Override
  public String getSupportedProtocol() {
    return GatewayConstants.Protocol.HTTP;
  }

  /** Build full URL */
  private String buildFullUrl(String target, GatewayRequest request) {
    StringBuilder url = new StringBuilder(target);

    if (request.getQueryParameters() != null && !request.getQueryParameters().isEmpty()) {
      request
          .getQueryParameters()
          .forEach(
              (key, values) -> {
                for (String value : values) {
                  url.append(key).append("=").append(value).append("&");
                }
              });
      // Remove trailing &
      if (url.charAt(url.length() - 1) == '&') {
        url.deleteCharAt(url.length() - 1);
      }
    }

    return url.toString();
  }
}

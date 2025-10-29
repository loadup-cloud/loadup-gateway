package com.github.loadup.gateway.plugins;

/*-
 * #%L
 * Proxy HTTP Plugin
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

import com.github.loadup.gateway.facade.constants.GatewayConstants;
import com.github.loadup.gateway.facade.model.GatewayRequest;
import com.github.loadup.gateway.facade.model.GatewayResponse;
import com.github.loadup.gateway.facade.model.PluginConfig;
import com.github.loadup.gateway.facade.spi.ProxyPlugin;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * HTTP proxy plugin
 */
@Slf4j
@Component
public class HttpProxyPlugin implements ProxyPlugin {

    private RestTemplate restTemplate = new RestTemplate();

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
    public void initialize(PluginConfig config) {
        log.info("HttpProxyPlugin initialized with config: {}", config);
        // You can configure RestTemplate timeouts, connection pool, etc. here
    }

    @Override
    public GatewayResponse execute(GatewayRequest request) throws Exception {
        throw new UnsupportedOperationException("Use proxy method instead");
    }

    @Override
    public GatewayResponse proxy(GatewayRequest request, String target) throws Exception {
        try {
            // Build request headers
            HttpHeaders headers = new HttpHeaders();
            if (request.getHeaders() != null) {
                request.getHeaders().forEach(headers::set);
            }

            // Build request body
            HttpEntity<String> entity = new HttpEntity<>(request.getBody(), headers);

            // Determine HTTP method
            HttpMethod httpMethod = HttpMethod.valueOf(request.getMethod().toUpperCase());

            // Build full URL
            String fullUrl = buildFullUrl(target, request);

            log.debug("Proxying {} request to: {}", httpMethod, fullUrl);

            // Execute HTTP request
            ResponseEntity<String> response = restTemplate.exchange(
                    fullUrl, httpMethod, entity, String.class);

            // Build gateway response
            Map<String, String> responseHeaders = new HashMap<>();
            response.getHeaders().forEach((key, values) -> {
                if (!values.isEmpty()) {
                    responseHeaders.put(key, values.get(0));
                }
            });

            return GatewayResponse.builder()
                    .requestId(request.getRequestId())
                    .statusCode(response.getStatusCodeValue())
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
    public boolean supports(GatewayRequest request) {
        return true;
    }

    @Override
    public String getSupportedProtocol() {
        return GatewayConstants.Protocol.HTTP;
    }

    /**
     * Build full URL
     */
    private String buildFullUrl(String target, GatewayRequest request) {
        StringBuilder url = new StringBuilder(target);

        // Add query parameters
        if (request.getQueryParameters() != null && !request.getQueryParameters().isEmpty()) {
            url.append("?");
            request.getQueryParameters().forEach((key, values) -> {
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

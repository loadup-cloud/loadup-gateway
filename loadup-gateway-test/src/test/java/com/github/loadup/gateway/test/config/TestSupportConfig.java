package com.github.loadup.gateway.test.config;

/*-
 * #%L
 * LoadUp Gateway Test
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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.time.LocalDateTime;
import java.util.HashMap;

@Configuration
@Profile("test")
public class TestSupportConfig {

    @Bean
    public ProxyPlugin httpStubProxyPlugin() {
        return new ProxyPlugin() {
            @Override
            public GatewayResponse proxy(GatewayRequest request, String target) {
                return GatewayResponse.builder()
                        .requestId(request.getRequestId())
                        .statusCode(GatewayConstants.Status.SUCCESS)
                        .headers(new HashMap<>())
                        .body("{\"stub\":true,\"target\":\"" + target + "\"}")
                        .contentType(GatewayConstants.ContentType.JSON)
                        .responseTime(LocalDateTime.now())
                        .build();
            }

            @Override
            public String getSupportedProtocol() {
                return GatewayConstants.Protocol.HTTP;
            }

            @Override
            public String getName() { return "HttpStubProxyPlugin"; }
            @Override
            public String getType() { return "PROXY"; }
            @Override
            public String getVersion() { return "test"; }
            @Override
            public int getPriority() { return 1; }
            @Override
            public void initialize(PluginConfig config) { }
            @Override
            public GatewayResponse execute(GatewayRequest request) { throw new UnsupportedOperationException(); }
            @Override
            public void destroy() { }
            @Override
            public boolean supports(GatewayRequest request) { return true; }
        };
    }

    @Bean
    public ProxyPlugin beanStubProxyPlugin() {
        return new ProxyPlugin() {
            @Override
            public GatewayResponse proxy(GatewayRequest request, String target) {
                return GatewayResponse.builder()
                        .requestId(request.getRequestId())
                        .statusCode(GatewayConstants.Status.SUCCESS)
                        .headers(new HashMap<>())
                        .body("{\"beanStub\":true,\"target\":\"" + target + "\"}")
                        .contentType(GatewayConstants.ContentType.JSON)
                        .responseTime(LocalDateTime.now())
                        .build();
            }
            @Override
            public String getSupportedProtocol() { return GatewayConstants.Protocol.BEAN; }
            @Override
            public String getName() { return "BeanStubProxyPlugin"; }
            @Override
            public String getType() { return "PROXY"; }
            @Override
            public String getVersion() { return "test"; }
            @Override
            public int getPriority() { return 2; }
            @Override
            public void initialize(PluginConfig config) { }
            @Override
            public GatewayResponse execute(GatewayRequest request) { throw new UnsupportedOperationException(); }
            @Override
            public void destroy() { }
            @Override
            public boolean supports(GatewayRequest request) { return true; }
        };
    }
}

package com.github.loadup.gateway.plugins;

/*-
 * #%L
 * Proxy RPC Plugin
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

import com.github.loadup.gateway.facade.model.GatewayRequest;
import com.github.loadup.gateway.facade.model.GatewayResponse;
import com.github.loadup.gateway.facade.model.PluginConfig;
import com.github.loadup.gateway.facade.spi.ProxyPlugin;
import com.github.loadup.gateway.facade.constants.GatewayConstants;
import com.github.loadup.gateway.facade.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.config.ReferenceConfig;
import org.apache.dubbo.config.RegistryConfig;
import org.apache.dubbo.rpc.service.GenericService;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Dubbo RPC代理插件
 */
@Slf4j
@Component
public class RpcProxyPlugin implements ProxyPlugin {

    private ApplicationConfig applicationConfig;
    private RegistryConfig registryConfig;
    private Map<String, GenericService> serviceCache = new HashMap<>();

    @Override
    public String getName() {
        return "RpcProxyPlugin";
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
        return 300;
    }

    @Override
    public void initialize(PluginConfig config) {
        log.info("RpcProxyPlugin initialized with config: {}", config);

        // 初始化Dubbo配置
        applicationConfig = new ApplicationConfig();
        applicationConfig.setName("loadup-gateway");

        registryConfig = new RegistryConfig();
        registryConfig.setAddress("zookeeper://127.0.0.1:2181");

        // 可以从config中读取注册中心地址等配置
        if (config.getProperties() != null) {
            String registryAddress = (String) config.getProperties().get("registry.address");
            if (registryAddress != null) {
                registryConfig.setAddress(registryAddress);
            }
        }
    }

    @Override
    public GatewayResponse execute(GatewayRequest request) throws Exception {
        throw new UnsupportedOperationException("Use proxy method instead");
    }

    @Override
    public GatewayResponse proxy(GatewayRequest request, String target) throws Exception {
        try {
            // 解析target格式: interfaceName:methodName:version
            String[] parts = target.split(":");
            if (parts.length < 2) {
                throw new IllegalArgumentException("Invalid RPC target format. Expected: interfaceName:methodName[:version]");
            }

            String interfaceName = parts[0];
            String methodName = parts[1];
            String version = parts.length > 2 ? parts[2] : null;

            // 获取泛化服务
            GenericService genericService = getGenericService(interfaceName, version);

            // 准备参数
            Object[] args = prepareRpcArgs(request);
            String[] parameterTypes = getParameterTypes(request);

            // 调用RPC服务
            Object result = genericService.$invoke(methodName, parameterTypes, args);

            // 构建响应
            return GatewayResponse.builder()
                    .requestId(request.getRequestId())
                    .statusCode(GatewayConstants.Status.SUCCESS)
                    .headers(new HashMap<>())
                    .body(JsonUtils.toJson(result))
                    .contentType(GatewayConstants.ContentType.JSON)
                    .responseTime(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            log.error("RPC proxy failed", e);
            return GatewayResponse.builder()
                    .requestId(request.getRequestId())
                    .statusCode(GatewayConstants.Status.INTERNAL_ERROR)
                    .body("{\"error\":\"RPC proxy failed\",\"message\":\"" + e.getMessage() + "\"}")
                    .contentType(GatewayConstants.ContentType.JSON)
                    .responseTime(LocalDateTime.now())
                    .errorMessage(e.getMessage())
                    .build();
        }
    }

    @Override
    public void destroy() {
        log.info("RpcProxyPlugin destroyed");
        serviceCache.clear();
    }

    @Override
    public boolean supports(GatewayRequest request) {
        return true;
    }

    @Override
    public String getSupportedProtocol() {
        return GatewayConstants.Protocol.RPC;
    }

    /**
     * 获取泛化服务
     */
    private GenericService getGenericService(String interfaceName, String version) {
        String cacheKey = interfaceName + ":" + (version != null ? version : "");

        return serviceCache.computeIfAbsent(cacheKey, k -> {
            ReferenceConfig<GenericService> reference = new ReferenceConfig<>();
            reference.setApplication(applicationConfig);
            reference.setRegistry(registryConfig);
            reference.setInterface(interfaceName);
            reference.setGeneric(true);
            if (version != null) {
                reference.setVersion(version);
            }

            return reference.get();
        });
    }

    /**
     * 准备RPC调用参数
     */
    private Object[] prepareRpcArgs(GatewayRequest request) {
        if (request.getBody() == null || request.getBody().trim().isEmpty()) {
            return new Object[0];
        }

        try {
            // 尝试解析为JSON数组
            if (request.getBody().trim().startsWith("[")) {
                return JsonUtils.fromJson(request.getBody(), Object[].class);
            } else {
                // 单个参数
                return new Object[]{JsonUtils.toMap(request.getBody())};
            }
        } catch (Exception e) {
            log.warn("Failed to parse RPC args from request body", e);
            return new Object[]{request.getBody()};
        }
    }

    /**
     * 获取参数类型
     */
    private String[] getParameterTypes(GatewayRequest request) {
        // 简化处理，实际应用中可能需要更复杂的类型推断
        Object[] args = prepareRpcArgs(request);
        String[] types = new String[args.length];

        for (int i = 0; i < args.length; i++) {
            if (args[i] instanceof Map) {
                types[i] = "java.util.Map";
            } else if (args[i] instanceof String) {
                types[i] = "java.lang.String";
            } else {
                types[i] = "java.lang.Object";
            }
        }

        return types;
    }
}

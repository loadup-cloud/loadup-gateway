package com.github.loadup.gateway.plugins;

/*-
 * #%L
 * Proxy SpringBean Plugin
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
import com.github.loadup.gateway.facade.exception.ExceptionHandler;
import com.github.loadup.gateway.facade.exception.GatewayException;
import com.github.loadup.gateway.facade.exception.GatewayExceptionFactory;
import com.github.loadup.gateway.facade.model.GatewayRequest;
import com.github.loadup.gateway.facade.model.GatewayResponse;
import com.github.loadup.gateway.facade.model.PluginConfig;
import com.github.loadup.gateway.facade.spi.ProxyPlugin;
import com.github.loadup.gateway.facade.utils.JsonUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.HashMap;

/**
 * Spring Bean代理插件
 */
@Slf4j
@Component
public class SpringBeanProxyPlugin implements ProxyPlugin {

    @Resource
    private ApplicationContext applicationContext;

    @Override
    public String getName() {
        return "SpringBeanProxyPlugin";
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
        return 100;
    }

    @Override
    public void initialize(PluginConfig config) {
        log.info("SpringBeanProxyPlugin initialized with config: {}", config);
    }

    @Override
    public GatewayResponse execute(GatewayRequest request) throws Exception {
        // 这个方法在ProxyPlugin中不直接使用，而是通过proxy方法
        throw GatewayExceptionFactory.operationNotSupported("Use proxy method instead");
    }

    @Override
    public GatewayResponse proxy(GatewayRequest request, String target) throws Exception {

        try {
            String[] parts = target.split(":");
            if (parts.length != 2) {
                throw GatewayExceptionFactory.invalidBeanTarget(target);
            }

            String beanName = parts[0];
            String methodName = parts[1];

            // 获取Spring Bean
            Object bean;
            try {
                bean = applicationContext.getBean(beanName);
            } catch (Exception e) {
                throw GatewayExceptionFactory.beanNotFound(beanName);
            }

            // 获取方法
            Method method = findMethod(bean.getClass(), methodName);
            if (method == null) {
                throw GatewayExceptionFactory.methodNotFound(beanName, methodName);
            }

            // 准备参数
            Object[] args = prepareMethodArgs(request, method);

            // 调用方法
            Object result;
            try {
                result = method.invoke(bean, args);
            } catch (java.lang.reflect.InvocationTargetException e) {
                // 提取原始异常并包装
                Throwable cause = e.getCause() != null ? e.getCause() : e;
                throw GatewayExceptionFactory.methodInvokeFailed(beanName, methodName, cause);
            }

            // 构建响应
            return GatewayResponse.builder()
                    .requestId(request.getRequestId())
                    .statusCode(GatewayConstants.Status.SUCCESS)
                    .headers(new HashMap<>())
                    .body(JsonUtils.toJson(result))
                    .contentType(GatewayConstants.ContentType.JSON)
                    .responseTime(LocalDateTime.now())
                    .build();

        } catch (GatewayException e) {
            // 网关异常直接使用异常处理器处理
            return ExceptionHandler.handleException(request.getRequestId(), e);
        } catch (Exception e) {
            // 其他异常包装后处理
            GatewayException wrappedException = GatewayExceptionFactory.wrap(e, "SPRINGBEAN_PROXY");
            return ExceptionHandler.handleException(request.getRequestId(), wrappedException);
        }
    }

    @Override
    public void destroy() {
        log.info("SpringBeanProxyPlugin destroyed");
    }

    @Override
    public boolean supports(GatewayRequest request) {
        return true; // 支持所有请求
    }

    @Override
    public String getSupportedProtocol() {
        return GatewayConstants.Protocol.BEAN;
    }

    /**
     * 查找匹配的方法
     */
    private Method findMethod(Class<?> clazz, String methodName) {
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.getName().equals(methodName)) {
                return method;
            }
        }
        return null;
    }

    /**
     * 准备方法参数
     */
    private Object[] prepareMethodArgs(GatewayRequest request, Method method) {
        Class<?>[] paramTypes = method.getParameterTypes();
        Object[] args = new Object[paramTypes.length];

        for (int i = 0; i < paramTypes.length; i++) {
            if (paramTypes[i] == GatewayRequest.class) {
                args[i] = request;
            } else if (paramTypes[i] == String.class) {
                args[i] = request.getBody();
            } else {
                // 尝试从请求体JSON解析
                try {
                    args[i] = JsonUtils.fromJson(request.getBody(), paramTypes[i]);
                } catch (Exception e) {
                    args[i] = null;
                }
            }
        }

        return args;
    }
}

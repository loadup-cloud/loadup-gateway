package com.github.loadup.gateway.core.template;

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

import com.github.loadup.gateway.facade.model.GatewayRequest;
import com.github.loadup.gateway.facade.model.GatewayResponse;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 模板引擎 - 支持Groovy脚本
 */
@Slf4j
@Component
public class TemplateEngine {

    private final ConcurrentHashMap<String, groovy.lang.Script> scriptCache = new ConcurrentHashMap<>();
    private final GroovyShell groovyShell = new GroovyShell();

    /**
     * 处理请求模板
     */
    public GatewayRequest processRequestTemplate(GatewayRequest request, String templateScript) {
        try {
            if (request.getHeaders() == null) {
                request.setHeaders(new HashMap<>());
            }
            if (request.getAttributes() == null) {
                request.setAttributes(new HashMap<>());
            }
            Binding binding = new Binding();
            binding.setVariable("request", request);
            binding.setVariable("log", log);

            groovy.lang.Script script = getCompiledScript(templateScript);
            script.setBinding(binding);

            Object result = script.run();
            if (result instanceof GatewayRequest) {
                return (GatewayRequest) result;
            }

            log.warn("Request template script did not return GatewayRequest, using original request");
            return request;

        } catch (Exception e) {
            log.error("Failed to process request template", e);
            return request;
        }
    }

    /**
     * 处理响应模板
     */
    public GatewayResponse processResponseTemplate(GatewayResponse response, String templateScript) {
        try {
            if (response.getHeaders() == null) {
                response.setHeaders(new HashMap<>());
            }
            Binding binding = new Binding();
            binding.setVariable("response", response);
            binding.setVariable("log", log);

            groovy.lang.Script script = getCompiledScript(templateScript);
            script.setBinding(binding);

            Object result = script.run();
            if (result instanceof GatewayResponse) {
                return (GatewayResponse) result;
            }

            log.warn("Response template script did not return GatewayResponse, using original response");
            return response;

        } catch (Exception e) {
            log.error("Failed to process response template", e);
            return response;
        }
    }

    /**
     * 获取编译后的脚本（带缓存）
     */
    private groovy.lang.Script getCompiledScript(String scriptText) {
        String scriptHash = String.valueOf(scriptText.hashCode());
        return scriptCache.computeIfAbsent(scriptHash, k -> groovyShell.parse(scriptText));
    }

    /**
     * 清空脚本缓存
     */
    public void clearScriptCache() {
        scriptCache.clear();
        log.info("Template script cache cleared");
    }
}

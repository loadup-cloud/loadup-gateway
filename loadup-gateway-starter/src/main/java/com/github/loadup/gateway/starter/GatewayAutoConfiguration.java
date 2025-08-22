package com.github.loadup.gateway.starter;

/*-
 * #%L
 * LoadUp Gateway Starter
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

import com.github.loadup.gateway.core.action.ActionDispatcher;
import com.github.loadup.gateway.core.filter.GatewayFilter;
import com.github.loadup.gateway.core.plugin.PluginManager;
import com.github.loadup.gateway.core.router.RouteResolver;
import com.github.loadup.gateway.core.template.TemplateEngine;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ComponentScan;

/**
 * Gateway自动配置类
 */
@Configuration
@EnableConfigurationProperties(GatewayProperties.class)
@ConditionalOnClass(GatewayFilter.class)
@ComponentScan(basePackages = "com.github.loadup.gateway.core")
public class GatewayAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ActionDispatcher actionDispatcher() {
        return new ActionDispatcher();
    }

    @Bean
    @ConditionalOnMissingBean
    public RouteResolver routeResolver() {
        return new RouteResolver();
    }

    @Bean
    @ConditionalOnMissingBean
    public TemplateEngine templateEngine() {
        return new TemplateEngine();
    }

    @Bean
    @ConditionalOnMissingBean
    public PluginManager pluginManager() {
        return new PluginManager();
    }

    @Bean
    @ConditionalOnMissingBean
    public GatewayFilter gatewayFilter() {
        return new GatewayFilter();
    }
}

package com.github.loadup.gateway.facade.spi;

/*-
 * #%L
 * LoadUp Gateway Facade
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

/**
 * 网关插件SPI接口
 */
public interface GatewayPlugin {

    /**
     * 插件名称
     */
    String getName();

    /**
     * 插件类型
     */
    String getType();

    /**
     * 插件版本
     */
    String getVersion();

    /**
     * 插件优先级
     */
    int getPriority();

    /**
     * 初始化插件
     */
    void initialize(PluginConfig config);

    /**
     * 执行插件逻辑
     */
    GatewayResponse execute(GatewayRequest request) throws Exception;

    /**
     * 销毁插件
     */
    void destroy();

    /**
     * 检查插件是否支持该请求
     */
    boolean supports(GatewayRequest request);
}

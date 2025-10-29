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
 * Gateway plugin SPI interface
 */
public interface GatewayPlugin {

    /**
     * Plugin name
     */
    String getName();

    /**
     * Plugin type
     */
    String getType();

    /**
     * Plugin version
     */
    String getVersion();

    /**
     * Plugin priority
     */
    int getPriority();

    /**
     * Initialize the plugin
     */
    void initialize(PluginConfig config);

    /**
     * Execute plugin logic
     */
    GatewayResponse execute(GatewayRequest request) throws Exception;

    /**
     * Destroy the plugin
     */
    void destroy();

    /**
     * Check whether the plugin supports the given request
     */
    boolean supports(GatewayRequest request);
}

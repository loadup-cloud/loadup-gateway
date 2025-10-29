package com.github.loadup.gateway.facade.model;

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

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Plugin configuration model
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PluginConfig {

    /**
     * Plugin name
     */
    private String pluginName;

    /**
     * Plugin type
     */
    private String pluginType;

    /**
     * Plugin version
     */
    private String version;

    /**
     * Enabled flag
     */
    private boolean enabled;

    /**
     * Priority (lower value means higher priority)
     */
    private int priority;

    /**
     * Plugin configuration parameters
     */
    private Map<String, Object> properties;

    /**
     * Plugin description
     */
    private String description;
}

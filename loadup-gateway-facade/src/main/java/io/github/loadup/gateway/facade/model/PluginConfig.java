package io.github.loadup.gateway.facade.model;

import java.util.Map;
import lombok.*;

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

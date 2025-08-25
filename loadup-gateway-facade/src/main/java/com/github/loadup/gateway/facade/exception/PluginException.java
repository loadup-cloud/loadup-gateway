package com.github.loadup.gateway.facade.exception;

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

/**
 * 插件相关异常
 */
public class PluginException extends GatewayException {

    private static final String MODULE = "PLUGIN";

    public PluginException(ErrorCode errorCode, String message) {
        super(errorCode.getCode(), ErrorType.PLUGIN, MODULE, message);
    }

    public PluginException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode.getCode(), ErrorType.PLUGIN, MODULE, message, cause);
    }

    // 便捷方法
    public static PluginException notFound(String pluginName) {
        return new PluginException(ErrorCode.PLUGIN_NOT_FOUND, "插件未找到: " + pluginName);
    }

    public static PluginException initFailed(String pluginName, Throwable cause) {
        return new PluginException(ErrorCode.PLUGIN_INIT_FAILED, "插件初始化失败: " + pluginName, cause);
    }

    public static PluginException executionFailed(String pluginName, Throwable cause) {
        return new PluginException(ErrorCode.PLUGIN_EXECUTION_FAILED, "插件执行失败: " + pluginName, cause);
    }

    public static PluginException configInvalid(String pluginName, String reason) {
        return new PluginException(ErrorCode.PLUGIN_CONFIG_INVALID, "插件配置无效: " + pluginName + " - " + reason);
    }
}

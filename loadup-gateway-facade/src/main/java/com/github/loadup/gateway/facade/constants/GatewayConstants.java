package com.github.loadup.gateway.facade.constants;

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
 * Gateway常量定义
 */
public final class GatewayConstants {

    private GatewayConstants() {}

    /**
     * 协议类型
     */
    public static final class Protocol {
        public static final String HTTP = "HTTP";
        public static final String RPC = "RPC";
        public static final String BEAN = "BEAN";
    }

    /**
     * 存储类型
     */
    public static final class Storage {
        public static final String FILE = "FILE";
        public static final String DATABASE = "DATABASE";
    }

    /**
     * 模板类型
     */
    public static final class Template {
        public static final String REQUEST = "REQUEST";
        public static final String RESPONSE = "RESPONSE";
    }

    /**
     * 内容类型
     */
    public static final class ContentType {
        public static final String JSON = "application/json";
        public static final String FORM = "application/x-www-form-urlencoded";
        public static final String XML = "application/xml";
    }

    /**
     * HTTP方法
     */
    public static final class HttpMethod {
        public static final String GET = "GET";
        public static final String POST = "POST";
        public static final String PUT = "PUT";
        public static final String DELETE = "DELETE";
        public static final String PATCH = "PATCH";
    }

    /**
     * 状态码
     */
    public static final class Status {
        public static final int SUCCESS = 200;
        public static final int BAD_REQUEST = 400;
        public static final int UNAUTHORIZED = 401;
        public static final int FORBIDDEN = 403;
        public static final int NOT_FOUND = 404;
        public static final int INTERNAL_ERROR = 500;
        public static final int SERVICE_UNAVAILABLE = 503;
    }

    /**
     * 配置键名
     */
    public static final class Config {
        public static final String GATEWAY_PREFIX = "loadup.gateway";
        public static final String PLUGIN_ENABLED = "enabled";
        public static final String PLUGIN_CONFIG = "config";
        public static final String TEMPLATE_PATH = "template.path";
        public static final String STORAGE_TYPE = "storage.type";
    }
}

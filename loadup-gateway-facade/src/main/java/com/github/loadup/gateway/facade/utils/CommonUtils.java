package com.github.loadup.gateway.facade.utils;

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

import org.apache.commons.lang3.StringUtils;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * 通用工具类
 */
public final class CommonUtils {

    private static final Pattern PATH_PATTERN = Pattern.compile("^/[a-zA-Z0-9/_-]*$");

    private CommonUtils() {}

    /**
     * 生成请求ID
     */
    public static String generateRequestId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 验证路径格式
     */
    public static boolean isValidPath(String path) {
        return StringUtils.isNotBlank(path) && PATH_PATTERN.matcher(path).matches();
    }

    /**
     * 规范化路径
     */
    public static String normalizePath(String path) {
        if (StringUtils.isBlank(path)) {
            return "/";
        }
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        if (path.endsWith("/") && path.length() > 1) {
            path = path.substring(0, path.length() - 1);
        }
        return path;
    }

    /**
     * 安全地获取字符串
     */
    public static String safeString(Object obj) {
        return obj == null ? "" : obj.toString();
    }

    /**
     * 检查字符串是否为空或null
     */
    public static boolean isEmpty(String str) {
        return StringUtils.isEmpty(str);
    }

    /**
     * 检查字符串是否不为空
     */
    public static boolean isNotEmpty(String str) {
        return StringUtils.isNotEmpty(str);
    }
}

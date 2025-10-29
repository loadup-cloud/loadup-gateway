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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

/**
 * JSON utility class
 */
public final class JsonUtils {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private JsonUtils() {
    }

    /**
     * Convert an object to a JSON string
     */
    public static String toJson(Object obj) {
        try {
            if (obj instanceof String str) {
                return str;
            }
            return OBJECT_MAPPER.writeValueAsString(obj);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Convert JSON string to an object
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            return OBJECT_MAPPER.readValue(json, clazz);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Convert JSON string to a Map
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> toMap(String json) {
        try {
            return OBJECT_MAPPER.readValue(json, Map.class);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Convert an object to a Map via JSON
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> toMap(Object obj) {
        try {
            return toMap(toJson(obj));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Parse JSON node
     */
    public static JsonNode parseJson(String json) {
        try {
            return OBJECT_MAPPER.readTree(json);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Check whether string is valid JSON
     */
    public static boolean isValidJson(String json) {
        if (StringUtils.isBlank(json)) {
            return false;
        }
        try {
            OBJECT_MAPPER.readTree(json);
            return true;
        } catch (Exception e) {
            return false;
        }
    }


}

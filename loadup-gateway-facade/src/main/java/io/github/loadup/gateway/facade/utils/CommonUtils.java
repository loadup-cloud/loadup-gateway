package io.github.loadup.gateway.facade.utils;

/*-
 * #%L
 * LoadUp Gateway Facade
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
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

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;

/** Common utility class */
public final class CommonUtils {

  private static final Pattern PATH_PATTERN = Pattern.compile("^/[a-zA-Z0-9/_-]*$");
  // SecureRandom for generating OpenTelemetry-compatible trace IDs
  private static final SecureRandom RANDOM = new SecureRandom();

  private CommonUtils() {}

  /**
   * Generate request ID (OpenTelemetry trace-id format: 32 lowercase hex characters representing 16
   * bytes)
   */
  public static String generateRequestId() {
    byte[] bytes = new byte[16];
    String traceId;
    do {
      RANDOM.nextBytes(bytes);
      traceId = bytesToLowerHex(bytes);
      // repeat if all-zero (OpenTelemetry disallows all-zero trace id)
    } while ("00000000000000000000000000000000".equals(traceId));
    return traceId;
  }

  private static String bytesToLowerHex(byte[] bytes) {
    char[] hexArray = "0123456789abcdef".toCharArray();
    char[] hexChars = new char[bytes.length * 2];
    for (int j = 0; j < bytes.length; j++) {
      int v = bytes[j] & 0xFF;
      hexChars[j * 2] = hexArray[v >>> 4];
      hexChars[j * 2 + 1] = hexArray[v & 0x0F];
    }
    return new String(hexChars);
  }

  /** Validate path format */
  public static boolean isValidPath(String path) {
    return StringUtils.isNotBlank(path) && PATH_PATTERN.matcher(path).matches();
  }

  /** Normalize path */
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

  /** Safely get string */
  public static String safeString(Object obj) {
    return obj == null ? "" : obj.toString();
  }

  /** Check if string is empty or null */
  public static boolean isEmpty(String str) {
    return StringUtils.isEmpty(str);
  }

  /** Check if string is not empty */
  public static boolean isNotEmpty(String str) {
    return StringUtils.isNotEmpty(str);
  }

  public static Map<String, Object> propertiesToMap(String propertiesStr) {
    Map<String, Object> properties = new HashMap<>();
    try {
      if (propertiesStr != null && !propertiesStr.trim().isEmpty()) {
        String trimmed = propertiesStr.trim();

        // JSON format
        if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
          properties = JsonUtils.toMap(trimmed);
        } else {
          // key=value;... format
          String[] pairs = trimmed.split(";");
          for (String pair : pairs) {
            String[] keyValue = pair.trim().split("=");
            if (keyValue.length == 2) {
              String key = keyValue[0].trim();
              String value = keyValue[1].trim();
              try {
                if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
                  properties.put(key, Boolean.parseBoolean(value));
                } else if (value.contains(".")) {
                  properties.put(key, Double.parseDouble(value));
                } else {
                  properties.put(key, Long.parseLong(value));
                }
              } catch (NumberFormatException e) {
                properties.put(key, value);
              }
            }
          }
        }
      }
    } catch (Exception e) {
    }
    return properties;
  }
}

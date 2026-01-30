package io.github.loadup.gateway.plugins;

/*-
 * #%L
 * Repository File Plugin
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

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import io.github.loadup.gateway.facade.config.GatewayProperties;
import io.github.loadup.gateway.facade.constants.GatewayConstants;
import io.github.loadup.gateway.facade.dto.RouteStructure;
import io.github.loadup.gateway.facade.model.RouteConfig;
import io.github.loadup.gateway.facade.spi.RepositoryPlugin;
import io.github.loadup.gateway.facade.utils.CommonUtils;
import io.github.loadup.gateway.plugins.entity.FileRouteEntity;
import jakarta.annotation.Resource;
import java.io.*;
import java.net.JarURLConnection;
import java.net.URL;
import java.nio.file.*;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/** File storage plugin - Using CSV format */
@Slf4j
@Component
@ConditionalOnProperty(
    prefix = "loadup.gateway.storage",
    name = "type",
    havingValue = "FILE",
    matchIfMissing = true)
public class FileRepositoryPlugin implements RepositoryPlugin {

  // basePath will be resolved during initialize. Default source is classpath:/gateway-config
  private String basePath = null; // resolved filesystem directory
  private final String ROUTES_FILE = "routes.csv";
  private final String TEMPLATES_DIR = "templates";
  @Resource private GatewayProperties gatewayProperties;

  public FileRepositoryPlugin() {}

  @Override
  public String getName() {
    return "FileRepositoryPlugin";
  }

  @Override
  public String getType() {
    return "REPOSITORY";
  }

  @Override
  public String getVersion() {
    return "1.0.0";
  }

  @Override
  public int getPriority() {
    return 100;
  }

  @Override
  public void initialize() {
    log.info("FileRepositoryPlugin initialized");

    String configured = null;
    if (gatewayProperties != null
        && gatewayProperties.getStorage() != null
        && gatewayProperties.getStorage().getFile() != null) {
      configured = gatewayProperties.getStorage().getFile().getBasePath();
    }

    // If not provided via GatewayProperties, default to classpath:/gateway-config
    if (configured == null || configured.trim().isEmpty()) {
      configured = "classpath:/gateway-config";
    }

    try {
      if (configured.startsWith("classpath:")) {
        String cpPath = configured.substring("classpath:".length());
        // ensure no leading slash
        if (cpPath.startsWith("/")) {
          cpPath = cpPath.substring(1);
        }
        Path tempDir = copyClasspathDirToTemp(cpPath);
        this.basePath = tempDir.toAbsolutePath().toString();
      } else {
        // treat as filesystem path (relative or absolute). Create directories if necessary.
        Path p = Paths.get(configured);
        Files.createDirectories(p);
        this.basePath = p.toAbsolutePath().toString();
      }

      // Ensure templates directory exists
      Files.createDirectories(Paths.get(basePath, TEMPLATES_DIR));

      // Create routes CSV file if missing
      Path routesFile = Paths.get(basePath, ROUTES_FILE);
      if (!Files.exists(routesFile)) {
        createRoutesFile(routesFile);
      }

      log.info(
          "FileRepositoryPlugin basePath resolved to {} (source={})", this.basePath, configured);
    } catch (Exception e) {
      log.error("Failed to initialize file repository with configured path: {}", configured, e);
    }
  }

  /**
   * Copy resources under given classpath directory to a temporary directory and return its Path.
   * This allows reading/writing files that are packaged in the classpath.
   */
  private Path copyClasspathDirToTemp(String classpathDir) throws IOException {
    // Create a temp directory unique to this application run
    String tmpRoot = System.getProperty("java.io.tmpdir");
    Path targetDir =
        Paths.get(
            tmpRoot, "loadup-gateway-config", String.valueOf(Math.abs(new Random().nextInt())));
    Files.createDirectories(targetDir);

    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    Enumeration<URL> resources = cl.getResources(classpathDir);

    boolean found = false;
    while (resources.hasMoreElements()) {
      URL resourceUrl = resources.nextElement();
      found = true;
      String protocol = resourceUrl.getProtocol();
      if ("file".equals(protocol)) {
        // Resource is on filesystem (e.g., during development)
        try {
          Path src = Paths.get(resourceUrl.toURI());
          // copy directory recursively
          try (Stream<Path> walker = Files.walk(src)) {
            walker.forEach(
                srcPath -> {
                  try {
                    Path rel = src.relativize(srcPath);
                    Path destPath = targetDir.resolve(rel.toString());
                    if (Files.isDirectory(srcPath)) {
                      Files.createDirectories(destPath);
                    } else {
                      Files.createDirectories(destPath.getParent());
                      Files.copy(srcPath, destPath);
                    }
                  } catch (IOException e) {
                    log.warn("Failed to copy resource file {}", srcPath, e);
                  }
                });
          }
        } catch (Exception e) {
          log.warn("Failed to copy classpath (file) resource {}", resourceUrl, e);
        }
      } else if ("jar".equals(protocol)) {
        // Resource is inside a JAR — iterate JAR entries
        try {
          JarURLConnection jarCon = (JarURLConnection) resourceUrl.openConnection();
          try (JarFile jar = jarCon.getJarFile()) {
            Enumeration<JarEntry> entries = jar.entries();
            String prefix = classpathDir.endsWith("/") ? classpathDir : (classpathDir + "/");
            while (entries.hasMoreElements()) {
              JarEntry entry = entries.nextElement();
              String name = entry.getName();
              if (name.startsWith(prefix)) {
                String relative = name.substring(prefix.length());
                if (entry.isDirectory()) {
                  Files.createDirectories(targetDir.resolve(relative));
                } else {
                  Path outFile = targetDir.resolve(relative);
                  Files.createDirectories(outFile.getParent());
                  try (InputStream is = cl.getResourceAsStream(name)) {
                    if (is != null) {
                      Files.copy(is, outFile);
                    }
                  }
                }
              }
            }
          }
        } catch (Exception e) {
          log.warn("Failed to copy classpath (jar) resources from {}", resourceUrl, e);
        }
      } else {
        log.warn("Unsupported classpath resource protocol: {} for URL {}", protocol, resourceUrl);
      }
    }

    if (!found) {
      // No resources found — still ensure directory exists so plugin can create files
      Files.createDirectories(targetDir);
    }

    return targetDir;
  }

  @Override
  public void destroy() {
    log.info("FileRepositoryPlugin destroyed");
  }

  @Override
  public Optional<RouteConfig> getRoute(String routeId) throws Exception {
    return getAllRoutes().stream().filter(route -> route.getRouteId().equals(routeId)).findFirst();
  }

  @Override
  public Optional<RouteConfig> getRouteByPath(String path, String method) throws Exception {
    return getAllRoutes().stream()
        .filter(route -> route.getPath().equals(path) && route.getMethod().equals(method))
        .findFirst();
  }

  @Override
  public List<RouteConfig> getAllRoutes() throws Exception {
    Path routesFile = Paths.get(basePath, ROUTES_FILE);
    if (!Files.exists(routesFile)) {
      return new ArrayList<>();
    }

    List<RouteConfig> routes = new ArrayList<>();

    try (CSVReader reader = new CSVReader(new FileReader(routesFile.toFile()))) {
      reader.readNext(); // skip header
      String[] line;

      while ((line = reader.readNext()) != null) {
        RouteConfig route = parseRouteFromCsvLine(line);
        if (route != null) {
          routes.add(route);
        }
      }
    }

    // apply templates immutably: replace each route with possibly modified instance
    List<RouteConfig> processed = new ArrayList<>();
    for (RouteConfig rc : routes) {
      processed.add(applyTemplates(rc));
    }

    return processed;
  }

  /**
   * Replace requestTemplate/responseTemplate values that are template file names with their file
   * contents. Returns the same instance if no changes are needed, otherwise returns a new
   * RouteConfig instance with templates replaced (using RouteConfig.builderFrom(rc)).
   */
  private RouteConfig applyTemplates(RouteConfig rc) {
    if (rc == null) {
      return null;
    }
    try {
      String req = rc.getRequestTemplate();
      String newReq = req;
      if (req != null && !req.trim().isEmpty()) {
        String loaded = loadTemplateContent(req.trim());
        if (loaded != null) {
          newReq = loaded;
        }
      }

      String resp = rc.getResponseTemplate();
      String newResp = resp;
      if (resp != null && !resp.trim().isEmpty()) {
        String loaded = loadTemplateContent(resp.trim());
        if (loaded != null) {
          newResp = loaded;
        }
      }

      if (!Objects.equals(req, newReq) || !Objects.equals(resp, newResp)) {
        return RouteConfig.builderFrom(rc)
            .requestTemplate(newReq)
            .responseTemplate(newResp)
            .build();
      }
    } catch (Exception e) {
      log.warn("Error applying templates for route {}: {}", rc.getRouteId(), e.getMessage());
    }
    return rc;
  }

  /**
   * Try to load a template by name from the resolved filesystem `basePath/templates/<name>`. If not
   * found, try classpath `templates/<name>` (or the plain name) as fallback. Returns null if not
   * found.
   */
  private String loadTemplateContent(String templateName) {
    if (templateName == null || templateName.trim().isEmpty()) {
      return null;
    }
    try {
      // Try a list of candidate filenames to be more flexible with CSV template naming
      List<String> candidates = new ArrayList<>();
      candidates.add(templateName);
      candidates.add(templateName + ".groovy");
      candidates.add(templateName + "_request.groovy");
      candidates.add(templateName + "_response.groovy");
      candidates.add(templateName + "_request_template.groovy");
      candidates.add(templateName + "_response_template.groovy");

      for (String candidate : candidates) {
        // Prefer filesystem templates under basePath/templates
        if (basePath != null) {
          Path p = Paths.get(basePath, TEMPLATES_DIR, candidate);
          if (Files.exists(p) && Files.isRegularFile(p)) {
            return Files.readString(p);
          }
        }

        // Fallback: try classpath resource under templates/<candidate>
        String resourcePath = TEMPLATES_DIR + "/" + candidate;
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        try (InputStream is = cl.getResourceAsStream(resourcePath)) {
          if (is != null) {
            return new String(is.readAllBytes());
          }
        }

        // Fallback: try direct classpath resource by candidate name
        try (InputStream is = cl.getResourceAsStream(candidate)) {
          if (is != null) {
            return new String(is.readAllBytes());
          }
        }
      }
    } catch (Exception e) {
      log.warn("Failed to load template '{}' : {}", templateName, e.getMessage());
    }
    // not found -> return original filename so callers still see something
    return templateName;
  }

  /** From CSV Row parse route config，Support new and old formats */
  private RouteConfig parseRouteFromCsvLine(String[] line) {
    if (line.length < 2) {
      return null;
    }

    // LatestFormat：path,method,target,requestTemplate,responseTemplate,enabled,properties
    if (line.length >= 3) {
      // Build a FileRouteEntity DTO and delegate conversion to convertToRouteConfig
      FileRouteEntity entity = new FileRouteEntity();
      entity.setPath(line[0]);
      entity.setMethod(line[1]);
      entity.setTarget(line[2]);
      entity.setRequestTemplate(line.length > 3 ? line[3] : "");
      entity.setResponseTemplate(line.length > 4 ? line[4] : "");
      if (line.length > 5 && line[5] != null && !line[5].isEmpty()) {
        entity.setEnabled(Boolean.parseBoolean(line[5]));
      } else {
        entity.setEnabled(null); // unspecified
      }
      if (line.length > 6) {
        entity.setProperties(line[6]);
      } else {
        entity.setProperties(null);
      }

      try {
        return convertToRouteConfig(entity);
      } catch (Exception e) {
        log.warn("Failed to convert CSV row to RouteConfig: {}", e.getMessage());
        return null;
      }
    }

    return null;
  }

  @Override
  public Optional<String> getTemplate(String templateId, String templateType) throws Exception {
    String fileName = templateId + "_" + templateType.toLowerCase() + ".groovy";
    Path templateFile = Paths.get(basePath, TEMPLATES_DIR, fileName);

    if (Files.exists(templateFile)) {
      String content = FileUtils.readFileToString(templateFile.toFile(), "UTF-8");
      return Optional.of(content);
    }

    return Optional.empty();
  }

  @Override
  public String getSupportedStorageType() {
    return GatewayConstants.Storage.FILE;
  }

  @Override
  public RouteConfig convertToRouteConfig(RouteStructure structure) throws Exception {
    if (!(structure instanceof FileRouteEntity entity)) {
      throw new IllegalArgumentException("Invalid RouteStructure type");
    }

    Map<String, Object> properties = CommonUtils.propertiesToMap(entity.getProperties());

    boolean enabled = Boolean.TRUE.equals(entity.getEnabled()) || entity.getEnabled() == null;

    return RouteConfig.builder()
        .path(entity.getPath())
        .method(entity.getMethod() != null ? entity.getMethod() : "POST")
        .target(entity.getTarget())
        .requestTemplate(entity.getRequestTemplate())
        .responseTemplate(entity.getResponseTemplate())
        .enabled(enabled)
        .properties(properties)
        .build();
  }

  /** Create routeCSVFile */
  private void createRoutesFile(Path routesFile) throws IOException {
    try (CSVWriter writer = new CSVWriter(new FileWriter(routesFile.toFile()))) {
      String[] headers = {
        "path", "method", "target", "requestTemplate", "responseTemplate", "enabled", "properties"
      };
      writer.writeNext(headers);
    }
  }
}

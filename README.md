# LoadUp Gateway

A flexible gateway framework with plugin architecture for building scalable and extensible API gateways.

## Overview

LoadUp Gateway is a modular gateway framework built with Spring Boot that provides a plugin-based architecture for
handling various types of proxying and data repository operations. It supports HTTP, RPC, and Spring Bean proxying,
along with file-based and database-based configuration repositories.

## Features

- **Plugin Architecture**: Extensible plugin system for custom functionality
- **Multiple Proxy Types**:
    - HTTP proxy plugin
    - RPC proxy plugin (Dubbo integration)
    - Spring Bean proxy plugin
- **Flexible Repository Support**:
    - File-based repository plugin
    - Database repository plugin
- **Spring Boot Integration**: Native Spring Boot starter for easy integration
- **Configuration Management**: Dynamic configuration with multiple sources

## Project Structure

```
loadup-gateway/
├── loadup-gateway-facade/      # API interfaces and contracts
├── loadup-gateway-core/        # Core framework implementation
├── loadup-gateway-starter/     # Spring Boot starter
├── loadup-gateway-test/        # Test modules and examples
└── plugins/                    # Plugin implementations
    ├── proxy-http-plugin/      # HTTP proxy functionality
    ├── proxy-rpc-plugin/       # RPC proxy functionality
    ├── proxy-springbean-plugin/ # Spring Bean proxy
    ├── repository-file-plugin/  # File-based configuration
    └── repository-database-plugin/ # Database configuration
```

## Requirements

- Java 17 or higher
- Maven 3.6 or higher
- Spring Boot 3.2.0

## Quick Start

### 1. Add Dependency

Add the LoadUp Gateway starter to your project:

```xml

<dependency>
    <groupId>io.github.loadup-cloud</groupId>
    <artifactId>loadup-gateway-starter</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

### 2. Configuration

Add basic configuration to your `application.yml`:

```yaml
loadup:
  gateway:
    enabled: true
    plugins:
      - proxy-http-plugin
      - repository-file-plugin
```

### 3. Usage Example

```java
@RestController
public class GatewayController {
    
    @Resource
    private GatewayService gatewayService;
    
    @RequestMapping("/**")
    public ResponseEntity<?> proxy(HttpServletRequest request) {
        return gatewayService.process(request);
    }
}
```

## Building from Source

```bash
# Clone the repository
git clone https://github.com/loadup/loadup-gateway.git
cd loadup-gateway

# Build the project
mvn clean install

# Run tests
mvn test
```

## Plugin Development

### Creating a Custom Plugin

1. Extend the base plugin interface:

```java
public class MyCustomPlugin implements GatewayPlugin {
    @Override
    public String getName() {
        return "my-custom-plugin";
    }

    @Override
    public void initialize(PluginContext context) {
        // Plugin initialization logic
    }
}
```

2. Register your plugin in `META-INF/spring.factories`:

```properties
io.github.loadup.gateway.plugin.GatewayPlugin=\
com.example.MyCustomPlugin
```

## Available Plugins

### Proxy Plugins

- **HTTP Proxy Plugin**: Routes HTTP requests to backend services
- **RPC Proxy Plugin**: Handles RPC calls using Dubbo framework
- **Spring Bean Proxy Plugin**: Proxies calls to local Spring beans

### Repository Plugins

- **File Repository Plugin**: Loads configuration from CSV/JSON files
- **Database Repository Plugin**: Manages configuration in database

## Configuration

### Plugin Configuration

```yaml
loadup:
  gateway:
    plugins:
      proxy-http:
        timeout: 5000
        retries: 3
      repository-file:
        location: classpath:config/routes.csv
```

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the GNU General Public License v3.0 - see the [LICENSE](LICENSE) file for details.

## Support

- Documentation: [Wiki](https://github.com/loadup/loadup-gateway/wiki)
- Issues: [GitHub Issues](https://github.com/loadup/loadup-gateway/issues)
- Discussions: [GitHub Discussions](https://github.com/loadup/loadup-gateway/discussions)

## Changelog

### Version 1.0.0-SNAPSHOT

- Initial release
- Core gateway framework
- Plugin architecture implementation
- HTTP, RPC, and Spring Bean proxy plugins
- File and database repository plugins

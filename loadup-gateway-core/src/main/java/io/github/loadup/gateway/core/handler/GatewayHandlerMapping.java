package io.github.loadup.gateway.core.handler;

import io.github.loadup.gateway.facade.model.RouteConfig;
import io.github.loadup.gateway.facade.spi.RepositoryPlugin;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.handler.AbstractHandlerMapping;

public class GatewayHandlerMapping extends AbstractHandlerMapping {

  private final RepositoryPlugin routeRepository;

  public GatewayHandlerMapping(RepositoryPlugin routeRepository) {
    this.routeRepository = routeRepository;
    setOrder(Ordered.HIGHEST_PRECEDENCE); // 优先级高于普通 Controller
    System.out.println("GatewayHandlerMapping initialized");
  }


  @Override
  protected Object getHandlerInternal(HttpServletRequest request) throws Exception {
    String path = request.getRequestURI();

    // 从你的 repository-plugin 中查找是否存在该配置
    Optional<RouteConfig> route = routeRepository.getRouteByPath(path, "POST");
    //    RouteConfig routeConfig = RouteConfig.builder().path(path).target("xxx").build();
    // 返回处理对象，Spring 会拿着它去找对应的 Adapter
    return route.map(routeConfig -> new GatewayHandler(routeConfig.getRouteId())).orElse(null);

    // 返回 null，Spring 会继续找下一个 Mapping (比如标准的 RequestMappingHandlerMapping)
  }
}

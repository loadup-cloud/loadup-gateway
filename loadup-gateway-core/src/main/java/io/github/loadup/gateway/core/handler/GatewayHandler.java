package io.github.loadup.gateway.core.handler;

public class GatewayHandler {
  private final String routeId;

  // 可以存放匹配到的路由元数据，比如目标是 RPC 还是 Bean
  public GatewayHandler(String routeId) {
    this.routeId = routeId;
  }
}

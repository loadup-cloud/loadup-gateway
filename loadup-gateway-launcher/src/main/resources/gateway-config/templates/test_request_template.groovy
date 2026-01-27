package templates
// 测试请求模板 - 处理入参并添加通用字段

import io.github.loadup.gateway.facade.utils.JsonUtils

// 添加通用请求头
request.headers.put("X-Gateway-Processed", "true")
request.headers.put("X-Request-Time", request.requestTime.toString())
request.headers.put("X-Request-Id", request.requestId)

// 如果是POST请求，处理请求体
if (request.method == "POST" && request.body != null) {
    def bodyMap = JsonUtils.toMap(request.body)

    // 添加系统字段
    bodyMap.put("_system", [
            "requestId": request.requestId,
            "timestamp": System.currentTimeMillis(),
            "clientIp" : request.clientIp,
            "userAgent": request.userAgent
    ])

    // 数据验证
    if (bodyMap.containsKey("name") && bodyMap.name.length() > 50) {
        bodyMap.put("_validation", ["nameLength": "too_long"])
    }

    request.body = JsonUtils.toJson(bodyMap)
}

// 添加查询参数处理
if (request.queryParameters.containsKey("transform")) {
    request.attributes.put("needTransform", true)
}

log.info("Request template processed for: {}", request.requestId)

return request

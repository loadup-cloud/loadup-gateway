package templates
// 高级请求模板 - 支持数据转换和路由决策

import io.github.loadup.gateway.facade.utils.JsonUtils

// 用户认证和权限检查
def token = request.headers.get("Authorization")
if (token != null && token.startsWith("Bearer ")) {
    request.attributes.put("authenticated", true)
    request.attributes.put("token", token.substring(7))
} else {
    request.attributes.put("authenticated", false)
}

// API版本处理
def apiVersion = request.headers.get("API-Version") ?: "v1"
request.attributes.put("apiVersion", apiVersion)

// 请求体数据转换
if (request.body != null && !request.body.trim().isEmpty()) {
    try {
        def bodyMap = JsonUtils.toMap(request.body)

        // 数据清洗和标准化
        if (bodyMap.containsKey("phone")) {
            // 标准化手机号格式
            def phone = bodyMap.phone.toString().replaceAll("[^0-9]", "")
            bodyMap.put("phone", phone)
        }

        if (bodyMap.containsKey("email")) {
            // 邮箱转小写
            bodyMap.put("email", bodyMap.email.toString().toLowerCase())
        }

        // 添加请求元数据
        bodyMap.put("_meta", [
                "requestId": request.requestId,
                "timestamp": System.currentTimeMillis(),
                "source"   : "gateway",
                "version"  : apiVersion
        ])

        request.body = JsonUtils.toJson(bodyMap)

    } catch (Exception e) {
        log.warn("Failed to process request body: {}", e.message)
        // 添加错误标记但不阻止请求
        request.attributes.put("bodyProcessError", e.message)
    }
}

// 请求速率限制标记
def clientIp = request.clientIp
request.attributes.put("rateLimitKey", "ip:" + clientIp)

// 日志记录
log.info("Advanced request template processed - Method: {}, Path: {}, Auth: {}",
        request.method, request.path, request.attributes.get("authenticated"))

return request

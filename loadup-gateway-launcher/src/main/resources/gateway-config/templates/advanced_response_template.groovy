package templates
// 高级响应模板 - 支持数据包装、缓存和监控

import io.github.loadup.gateway.facade.utils.JsonUtils

// 添加通用响应头
response.headers.put("X-Gateway-Version", "1.0.0")
response.headers.put("X-Response-Time", response.responseTime.toString())
response.headers.put("X-Processing-Time", response.processingTime.toString())

// 添加CORS支持
response.headers.put("Access-Control-Allow-Origin", "*")
response.headers.put("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
response.headers.put("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Requested-With")

// 处理成功响应
if (response.statusCode >= 200 && response.statusCode < 300) {

    if (response.body != null) {
        try {
            def responseData = JsonUtils.toMap(response.body)

            // 统一响应格式
            def wrappedResponse = [
                    "success"   : true,
                    "code"      : response.statusCode,
                    "message"   : "操作成功",
                    "data"      : responseData,
                    "pagination": null,
                    "meta"      : [
                            "requestId"     : response.requestId,
                            "timestamp"     : System.currentTimeMillis(),
                            "processingTime": response.processingTime,
                            "version"       : "v1"
                    ]
            ]

            // 处理分页数据
            if (responseData.containsKey("total") || responseData.containsKey("pageSize")) {
                wrappedResponse.pagination = [
                        "total"     : responseData.get("total", 0),
                        "pageSize"  : responseData.get("pageSize", 20),
                        "pageNumber": responseData.get("pageNumber", 1),
                        "hasMore"   : responseData.get("hasMore", false)
                ]
            }

            response.body = JsonUtils.toJson(wrappedResponse)

        } catch (Exception e) {
            log.warn("Failed to wrap response: {}", e.message)
            // 如果处理失败，返回简单格式
            def simpleResponse = [
                    "success": true,
                    "code"   : response.statusCode,
                    "message": "操作成功",
                    "data"   : response.body,
                    "meta"   : [
                            "requestId": response.requestId,
                            "timestamp": System.currentTimeMillis()
                    ]
            ]
            response.body = JsonUtils.toJson(simpleResponse)
        }
    }

} else {
    // 处理错误响应
    def errorResponse = [
            "success": false,
            "code"   : response.statusCode,
            "message": getErrorMessage(response.statusCode),
            "error"  : [
                    "details"  : response.errorMessage ?: "未知错误",
                    "timestamp": System.currentTimeMillis(),
                    "requestId": response.requestId
            ],
            "data"   : null,
            "meta"   : [
                    "requestId"     : response.requestId,
                    "timestamp"     : System.currentTimeMillis(),
                    "processingTime": response.processingTime
            ]
    ]

    response.body = JsonUtils.toJson(errorResponse)
}

// 设置内容类型
response.contentType = "application/json;charset=UTF-8"

// 添加缓存控制
if (response.statusCode == 200) {
    response.headers.put("Cache-Control", "public, max-age=300") // 5分钟缓存
} else {
    response.headers.put("Cache-Control", "no-cache, no-store, must-revalidate")
}

log.info("Advanced response template processed - Status: {}, RequestId: {}",
        response.statusCode, response.requestId)

return response

// 辅助方法：获取错误消息
def getErrorMessage(statusCode) {
    switch (statusCode) {
        case 400: return "请求参数错误"
        case 401: return "未授权访问"
        case 403: return "禁止访问"
        case 404: return "资源不存在"
        case 429: return "请求过于频繁"
        case 500: return "服务器内部错误"
        case 502: return "网关错误"
        case 503: return "服务不可用"
        case 504: return "网关超时"
        default: return "未知错误"
    }
}

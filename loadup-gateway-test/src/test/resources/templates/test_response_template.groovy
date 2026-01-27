// 测试响应模板 - 处理响应并添加统一格式
import io.github.loadup.gateway.facade.model.GatewayResponse
import io.github.loadup.gateway.facade.utils.JsonUtils

// 添加通用响应头
response.headers.put("X-Gateway-Response-Processed", "true")
response.headers.put("X-Response-Time", response.responseTime.toString())
response.headers.put("X-Processing-Time", response.processingTime.toString())

// 处理响应体，统一格式
if (response.body != null && response.statusCode == 200) {
    def responseData

    try {
        responseData = JsonUtils.toMap(response.body)
    } catch (Exception e) {
        // 如果不是JSON，包装成JSON格式
        responseData = ["data": response.body]
    }

    // 构建统一响应格式
    def unifiedResponse = [
        "code": 200,
        "message": "success",
        "data": responseData,
        "meta": [
            "requestId": response.requestId,
            "timestamp": System.currentTimeMillis(),
            "processingTime": response.processingTime
        ]
    ]

    response.body = JsonUtils.toJson(unifiedResponse)
    response.contentType = "application/json"

} else if (response.statusCode >= 400) {
    // 处理错误响应
    def errorResponse = [
        "code": response.statusCode,
        "message": response.errorMessage ?: "Unknown error",
        "data": null,
        "meta": [
            "requestId": response.requestId,
            "timestamp": System.currentTimeMillis(),
            "processingTime": response.processingTime
        ]
    ]

    response.body = JsonUtils.toJson(errorResponse)
    response.contentType = "application/json"
}

log.info("Response template processed for: {}", response.requestId)

return response

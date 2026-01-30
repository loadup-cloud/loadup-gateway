package templates
// 测试响应模板 - 处理响应并添加统一格式

import io.github.loadup.gateway.facade.utils.JsonUtils

// 添加通用响应头
response.headers.put("X-Gateway-Response-Processed", "true")
response.headers.put("X-Response-Time", response.responseTime.toString())
response.headers.put("X-Processing-Time", response.processingTime.toString())

// 处理响应体，统一格式
if (response.body != null && response.statusCode == 200) {
    def responseData


    responseData = JsonUtils.toMap(response.body)
    if (responseData == null) {
        // 如果不是JSON，包装成JSON格式
        responseData = response.body
    }


    // 构建统一响应格式
    def unifiedResponse = ["data": responseData,
                           "meta": ["requestId"     : response.requestId,
                                    "timestamp"     : System.currentTimeMillis(),
                                    "processingTime": response.processingTime]]

    response.body = JsonUtils.toJson(responseData)
    response.contentType = "application/json"

}

log.info("Response template processed for: {}", response.requestId)

return response

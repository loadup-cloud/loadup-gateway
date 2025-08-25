package com.github.loadup.gateway.facade.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一响应结果对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Result {
    /**
     * 业务码
     */
    private String code;
    /**
     * 状态
     */
    private String status;
    /**
     * 提示信息
     */
    private String message;
}


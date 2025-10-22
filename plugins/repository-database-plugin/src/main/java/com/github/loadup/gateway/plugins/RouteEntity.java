package com.github.loadup.gateway.plugins;

/*-
 * #%L
 * Repository Database Plugin
 * %%
 * Copyright (C) 2025 LoadUp Gateway Authors
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

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.Date;

/**
 * 路由实体
 */
@Data
@Table("gateway_routes")
public class RouteEntity {
    @Id
    private Long id;

    // unique/nullable constraints should be enforced at DB schema level
    private String routeId;

    private String routeName;

    @Column("path")
    private String path;

    @Column("method")
    private String method;

    @Column("protocol")
    private String protocol;

    /**
     * 统一目标配置 (支持前缀格式: http://..., bean://service:method, rpc://class:method:version)
     */
    private String target;

    // 兼容字段，用于迁移期间
    private String targetUrl;
    private String targetBean;
    private String targetMethod;

    private String requestTemplate;

    private String responseTemplate;

    private boolean enabled = true;

    private long timeout = 30000;
    private int retryCount = 3;

    // large text field; no @Lob in Spring Data JDBC
    private String properties;

    private Date updatedAt;
}

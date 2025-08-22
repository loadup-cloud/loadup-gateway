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

import jakarta.persistence.*;
import java.util.Date;

/**
 * 路由实体
 */
@Data
@Entity
@Table(name = "gateway_routes", indexes = {
    @Index(name = "idx_path_method", columnList = "path,method"),
    @Index(name = "idx_route_id", columnList = "routeId")
})
public class RouteEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String routeId;

    private String routeName;

    @Column(nullable = false)
    private String path;

    @Column(nullable = false)
    private String method;

    @Column(nullable = false)
    private String protocol;

    /**
     * 统一目标配置 (支持前缀格式: http://..., bean://service:method, rpc://class:method:version)
     */
    private String target;

    // 兼容字段，用于迁移期间
    private String targetUrl;
    private String targetBean;
    private String targetMethod;

    @Lob
    private String requestTemplate;

    @Lob
    private String responseTemplate;

    @Column(nullable = false)
    private boolean enabled = true;

    private long timeout = 30000;
    private int retryCount = 3;

    @Lob
    private String properties;

    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;
}

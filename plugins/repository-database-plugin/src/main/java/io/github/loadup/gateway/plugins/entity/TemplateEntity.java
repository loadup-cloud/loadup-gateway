package io.github.loadup.gateway.plugins.entity;

/*-
 * #%L
 * Repository Database Plugin
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
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

import java.util.Date;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

/** Template entity */
@Data
@Table("gateway_templates")
public class TemplateEntity {
  @Id private Long id;

  // nullable constraint enforced by DB schema
  private String templateId;

  // nullable constraint enforced by DB schema
  private String templateType;

  private String content;

  // temporal information stored as timestamp in DB
  private Date updatedAt;
}

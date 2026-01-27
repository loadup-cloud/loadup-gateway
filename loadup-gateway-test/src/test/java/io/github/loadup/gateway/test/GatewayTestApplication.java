package io.github.loadup.gateway.test;

/*-
 * #%L
 * LoadUp Gateway Test
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

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/** Test application entry point for tests */
@SpringBootApplication
@ComponentScan(
    basePackages = {
      "io.github.loadup.gateway.core",
      "io.github.loadup.gateway.facade",
      "io.github.loadup.gateway.plugins",
      "io.github.loadup.gateway.test"
    })
public class GatewayTestApplication {

  public static void main(String[] args) {
    SpringApplication.run(GatewayTestApplication.class, args);
  }
}

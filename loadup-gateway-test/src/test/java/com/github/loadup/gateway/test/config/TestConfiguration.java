package com.github.loadup.gateway.test.config;

/*-
 * #%L
 * LoadUp Gateway Test
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

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.HashMap;
import java.util.Map;

/**
 * Test Configuration Class，Provide testBeanAnd service
 */
@Configuration
@Profile("test")
public class TestConfiguration {

    /**
     * Test serviceBean，Used forTestBeanProxy functionality
     */
    @Bean
    public TestService testService() {
        return new TestService();
    }

    /**
     * Test dataStorageBean
     */
    @Bean
    public TestDataStore testDataStore() {
        return new TestDataStore();
    }

    /**
     * Test service implementation
     */
    public static class TestService {

        private final TestDataStore dataStore;

        public TestService() {
            this.dataStore = new TestDataStore();
        }

        public Map<String, Object> getData() {
            Map<String, Object> result = new HashMap<>();
            result.put("message", "Hello from TestService");
            result.put("timestamp", System.currentTimeMillis());
            result.put("service", "testService");
            return result;
        }

        public Map<String, Object> processData(Map<String, Object> data) {
            Map<String, Object> result = new HashMap<>();
            result.put("original", data);
            result.put("processed", true);
            result.put("timestamp", System.currentTimeMillis());
            result.put("service", "testService");
            return result;
        }

        public Map<String, Object> saveData(String name, Object data) {
            String id = dataStore.save(name, data);
            Map<String, Object> result = new HashMap<>();
            result.put("id", id);
            result.put("saved", true);
            result.put("timestamp", System.currentTimeMillis());
            return result;
        }

        public Map<String, Object> findData(String id) {
            Object data = dataStore.findById(id);
            Map<String, Object> result = new HashMap<>();
            result.put("id", id);
            result.put("found", data != null);
            result.put("data", data);
            result.put("timestamp", System.currentTimeMillis());
            return result;
        }
    }

    /**
     * Test dataStorage
     */
    public static class TestDataStore {

        private final Map<String, Object> storage = new HashMap<>();
        private int counter = 1;

        public String save(String name, Object data) {
            String id = name + "_" + (counter++);
            storage.put(id, data);
            return id;
        }

        public Object findById(String id) {
            return storage.get(id);
        }

        public Map<String, Object> findAll() {
            return new HashMap<>(storage);
        }

        public boolean delete(String id) {
            return storage.remove(id) != null;
        }

        public void clear() {
            storage.clear();
        }
    }
}

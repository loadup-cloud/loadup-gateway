package com.github.loadup.gateway.test;

import com.github.loadup.gateway.facade.utils.CommonUtils;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

public class CommonUtilsTest {

    private static final Pattern TRACE_ID_PATTERN = Pattern.compile("^[0-9a-f]{32}$");

    @Test
    public void generateRequestId_formatAndNotAllZero() {
        String id = CommonUtils.generateRequestId();
        assertNotNull(id, "requestId should not be null");
        assertEquals(32, id.length(), "requestId should be 32 characters long");
        assertTrue(TRACE_ID_PATTERN.matcher(id).matches(), "requestId should be lowercase hex of length 32");
        assertNotEquals("00000000000000000000000000000000", id, "requestId must not be all zeros");
    }

    @Test
    public void generateRequestId_uniqueness() {
        Set<String> seen = new HashSet<>();
        final int iterations = 1000;
        for (int i = 0; i < iterations; i++) {
            String id = CommonUtils.generateRequestId();
            assertTrue(seen.add(id), "duplicate requestId generated at iteration: " + i);
        }
    }
}


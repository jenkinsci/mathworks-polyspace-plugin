package com.mathworks.polyspace.jenkins.test;

import com.mathworks.polyspace.jenkins.PolyspaceConstants;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class PolyspaceConstantsTest {

    @Test
    void testPolyspaceBinConstant() {
        assertEquals("POLYSPACE_BIN", PolyspaceConstants.POLYSPACE_BIN);
    }

    @Test
    void testPolyspaceAccessConstant() {
        assertEquals("ps_helper_access", PolyspaceConstants.POLYSPACE_ACCESS);
    }

    @Test
    void testPolyspaceAccessProtocolConstant() {
        assertEquals("POLYSPACE_ACCESS_PROTOCOL", PolyspaceConstants.POLYSPACE_ACCESS_PROTOCOL);
    }

    @Test
    void testPolyspaceAccessHostConstant() {
        assertEquals("POLYSPACE_ACCESS_HOST", PolyspaceConstants.POLYSPACE_ACCESS_HOST);
    }

    @Test
    void testPolyspaceAccessPortConstant() {
        assertEquals("POLYSPACE_ACCESS_PORT", PolyspaceConstants.POLYSPACE_ACCESS_PORT);
    }

    @Test
    void testPolyspaceAccessUrlConstant() {
        assertEquals("POLYSPACE_ACCESS_URL", PolyspaceConstants.POLYSPACE_ACCESS_URL);
    }
}

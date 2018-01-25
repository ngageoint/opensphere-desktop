package io.opensphere.xyztile.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Unit test for {@link XYZServerInfo}.
 */
public class XYZServerInfoTest
{
    /**
     * Tests the {@link XYZServerInfo}.
     */
    @Test
    public void test()
    {
        XYZServerInfo serverInfo = new XYZServerInfo("daServer", "http://somehost");

        assertEquals("daServer", serverInfo.getServerName());
        assertEquals("http://somehost", serverInfo.getServerUrl());
    }
}

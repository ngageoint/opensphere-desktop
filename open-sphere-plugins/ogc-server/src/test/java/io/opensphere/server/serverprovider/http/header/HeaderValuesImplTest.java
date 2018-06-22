package io.opensphere.server.serverprovider.http.header;

import static org.junit.Assert.assertEquals;

import net.jcip.annotations.NotThreadSafe;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests the {@link HeaderValues} class.
 */
@NotThreadSafe
public class HeaderValuesImplTest
{
    /**
     * The identifier of the renderer string used in the test.
     */
    private static final String RENDER_IDENTIFIER = "Quadro NVS 450/PCIe/SSE2 ";

    /**
     * Prepares the test environment for execution.
     */
    @BeforeClass
    public static void setupBeforeClasses()
    {
        // this is a very bad way to test this, as it is dependent on execution order. HttpServerFactoryTest also relies
        // on HeaderValuesImpl, and in certain circumstances, that test can execute before HeaderValuesImplTest, which will
        // cause the HeaderValuesImpl.ourUserAgentPrefix field to be initialized with null values. To preserve functionality
        // of this test in a standalone manner, I've left this initialization here, but I've also made the initialization occur
        // within the pom.xml file, as Maven will reliably execute this test after HttpServerFactoryTest (causing a failure).
        //

        final String version = "5.1";
        final String userName = "user";
        final String osName = "Linux";
        final String osVersion = "2.6.32-504.8.1.el6.x86_64";
        final String osArch = "amd64";
        final String javaVersion = "1.7.0_17";

        System.setProperty("opensphere.useragent", "OpenSphere Desktop");
        System.setProperty("opensphere.version", version);
        System.setProperty("opensphere.deployment.name", "Deployment");
        System.setProperty("user.name", userName);
        System.setProperty("os.name", osName);
        System.setProperty("os.version", osVersion);
        System.setProperty("os.arch", osArch);
        System.setProperty("java.version", javaVersion);
    }

    /**
     * Tests constructing the user agent.
     */
    @Test
    public void testGetUserAgent()
    {
        final String expected = "OpenSphere Desktop (ver: 5.1 Deployment)(user/Linux/2.6.32-504.8.1.el6.x86_64/amd64/1.7.0_17/Quadro NVS 450/PCIe/SSE2)";
        final HeaderValuesImpl headerValues = new HeaderValuesImpl(RENDER_IDENTIFIER);
        final String actual = headerValues.getUserAgent();
        assertEquals(expected, actual);
    }
}

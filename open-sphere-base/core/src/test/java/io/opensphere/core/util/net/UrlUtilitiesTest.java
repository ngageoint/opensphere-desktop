package io.opensphere.core.util.net;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Test;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.opensphere.core.util.lang.ThreeTuple;

/**
 * Tests the {@link UrlUtilities}.
 */
public class UrlUtilitiesTest
{
    /**
     * Tests the is file function.
     *
     * @throws MalformedURLException Bad URL.
     */
    @Test
    public void testIsFile() throws MalformedURLException
    {
        URL fileUrl = new URL("file:/C:/test.txt");
        URL javaFileUrl = new URL("jar:file:/C:/Program%20Files/OpenSphere/OpenSphere-KML-plugin.jar!"
                + "/images/maps.google.com/mapfiles/kml/pushpin/ylw-pushpin.png");
        URL httpUrl = new URL("http://somehost");

        assertTrue(UrlUtilities.isFile(fileUrl));
        assertTrue(UrlUtilities.isFile(javaFileUrl));
        assertFalse(UrlUtilities.isFile(httpUrl));
    }

    /**
     * Tests different url's for getting the protocol, host and port.
     */
    @Test
    public void testGetProtocolHostPort()
    {
        ThreeTuple<String, String, Integer> protoHostPort = UrlUtilities.getProtocolHostPort("http://somehost", 80);
        assertEquals("http", protoHostPort.getFirstObject());
        assertEquals("somehost", protoHostPort.getSecondObject());
        assertEquals(80, protoHostPort.getThirdObject().intValue());

        protoHostPort = UrlUtilities.getProtocolHostPort("rtmpt://host:80/app/stream", 1935);
        assertEquals("rtmpt", protoHostPort.getFirstObject());
        assertEquals("host", protoHostPort.getSecondObject());
        assertEquals(80, protoHostPort.getThirdObject().intValue());

        protoHostPort = UrlUtilities.getProtocolHostPort("rtmp://host/app/stream", 1935);
        assertEquals("rtmp", protoHostPort.getFirstObject());
        assertEquals("host", protoHostPort.getSecondObject());
        assertEquals(1935, protoHostPort.getThirdObject().intValue());

        protoHostPort = UrlUtilities.getProtocolHostPort("https://somehost:8080", 80);
        assertEquals("https", protoHostPort.getFirstObject());
        assertEquals("somehost", protoHostPort.getSecondObject());
        assertEquals(8080, protoHostPort.getThirdObject().intValue());
    }

    /**
     * Tests the {@link UrlUtilities#toURLNew(String)} function.
     *
     * @throws MalformedURLException Bad URL.
     */
    @SuppressFBWarnings("DMI_HARDCODED_ABSOLUTE_FILENAME")
    @Test
    public void testToURLNew() throws MalformedURLException
    {
        assertEquals(null, UrlUtilities.toURLNew(null));
        assertEquals(null, UrlUtilities.toURLNew(""));
        assertEquals(new URL("http://www.google.com"), UrlUtilities.toURLNew("http://www.google.com"));
        assertEquals(new URL("file:///data"), UrlUtilities.toURLNew("file:///data"));
        assertEquals(new URL("file:///data"), UrlUtilities.toURLNew("/data"));
        assertEquals(new URL("file:///data/test.txt"), UrlUtilities.toURLNew("/data/test.txt"));
        assertEquals(new URL("file:///C:/test.txt"), UrlUtilities.toURLNew("C:/test.txt"));
        assertEquals(new URL("file:///C:/test.txt"), UrlUtilities.toURLNew("file://C:/test.txt"));
        assertEquals(new URL("file:///C:/test.txt"), UrlUtilities.toURLNew("file:///C:/test.txt"));
    }
}

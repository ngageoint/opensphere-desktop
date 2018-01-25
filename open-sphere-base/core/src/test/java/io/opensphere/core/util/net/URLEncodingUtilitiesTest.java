package io.opensphere.core.util.net;

import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Test;

import org.junit.Assert;

/** Test for {@link URLEncodingUtilities}. */
public class URLEncodingUtilitiesTest
{
    /**
     * Test for {@link URLEncodingUtilities#encodeURL(URL)}.
     *
     * @throws MalformedURLException If the test URL cannot be encoded.
     */
    @Test
    public void testEncodeURL() throws MalformedURLException
    {
        URL input = new URL("http://www.blah.com:8080/something/getKml.pl?a=b>1&c=d#ref");
        URL expected = new URL("http://www.blah.com:8080/something/getKml.pl?a=b%3E1&c=d#ref");
        URL actual = URLEncodingUtilities.encodeURL(input);
        Assert.assertEquals(expected.toString(), actual.toString());
    }

    /**
     * Test for {@link URLEncodingUtilities#encodeURL(URL)}.
     *
     * @throws MalformedURLException If the test URL cannot be encoded.
     */
    @Test
    public void testEncodeURLNoQuery() throws MalformedURLException
    {
        URL input = new URL("http://www.blah.com:8080/something/getKml.pl#ref");
        URL actual = URLEncodingUtilities.encodeURL(input);
        Assert.assertEquals(input.toString(), actual.toString());
    }

    /** Test for {@link URLEncodingUtilities#encodeURL(URL)}. */
    @Test
    public void testEncodeURLNull()
    {
        Assert.assertNull(URLEncodingUtilities.encodeURL(null));
    }
}

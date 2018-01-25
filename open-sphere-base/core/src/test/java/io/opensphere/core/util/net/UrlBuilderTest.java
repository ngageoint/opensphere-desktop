package io.opensphere.core.util.net;

import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Test;

import org.junit.Assert;

/** Test for {@link UrlBuilder}. */
public class UrlBuilderTest
{
    /**
     * Creates a URL.
     *
     * @param urlText the URL text
     * @return The URL
     */
    private static URL createUrl(String urlText)
    {
        URL url;
        try
        {
            url = new URL(urlText);
        }
        catch (MalformedURLException e)
        {
            url = null;
            Assert.fail("Failed to create URL");
        }
        return url;
    }

    /** Test for {@link UrlBuilder#toURL()}. */
    @Test
    public void testAddPath()
    {
        URL input = createUrl("http://www.blah.com:8080/something/getKml.pl");
        URL expected = createUrl("http://www.blah.com:8080/something/getKml.pl/more");

        UrlBuilder builder = new UrlBuilder(input);
        builder.addPath("more");
        try
        {
            Assert.assertEquals(expected.toString(), builder.toURL().toString());
        }
        catch (MalformedURLException e)
        {
            Assert.fail(e.getMessage());
        }
    }

    /** Test for {@link UrlBuilder#toURL()}. */
    @Test
    public void testAddQuery()
    {
        URL input = createUrl("http://www.blah.com:8080/something/getKml.pl?a=b&c=d#ref");
        URL expected = createUrl("http://www.blah.com:8080/something/getKml.pl?a=b&c=d&e=f#ref");

        UrlBuilder builder = new UrlBuilder(input);
        builder.addQuery("e=f");
        try
        {
            Assert.assertEquals(expected.toString(), builder.toURL().toString());
        }
        catch (MalformedURLException e)
        {
            Assert.fail(e.getMessage());
        }
    }

    /** Test for {@link UrlBuilder#toURL()}. */
    @Test
    public void testAddQueryHaveQuestion()
    {
        URL input = createUrl("http://www.blah.com:8080/something/getKml.pl?");
        URL expected = createUrl("http://www.blah.com:8080/something/getKml.pl?e=f");

        UrlBuilder builder = new UrlBuilder(input);
        builder.addQuery("e=f");
        try
        {
            Assert.assertEquals(expected.toString(), builder.toURL().toString());
        }
        catch (MalformedURLException e)
        {
            Assert.fail(e.getMessage());
        }
    }

//    /** Test for {@link UrlBuilder#toURL()}. */
//    @Test
//    public void testAddQueryHaveAmpersand()
//    {
//        URL input = createUrl("http://www.blah.com:8080/something/getKml.pl?a=b&");
//        URL expected = createUrl("http://www.blah.com:8080/something/getKml.pl?a=b&e=f");
//
//        UrlBuilder builder = new UrlBuilder(input);
//        builder.addQuery("e=f");
//        try
//        {
//            Assert.assertEquals(expected.toString(), builder.toURL().toString());
//        }
//        catch (MalformedURLException e)
//        {
//            Assert.fail(e.getMessage());
//        }
//    }

    /** Test for {@link UrlBuilder#toURL()}. */
    @Test
    public void testAddQueryNoQuery()
    {
        URL input = createUrl("http://www.blah.com:8080/something/getKml.pl#ref");
        URL expected = createUrl("http://www.blah.com:8080/something/getKml.pl?e=f#ref");

        UrlBuilder builder = new UrlBuilder(input);
        builder.addQuery("e=f");
        try
        {
            Assert.assertEquals(expected.toString(), builder.toURL().toString());
        }
        catch (MalformedURLException e)
        {
            Assert.fail(e.getMessage());
        }
    }

    /** Test for {@link UrlBuilder#toURL()}. */
    @Test
    public void testSetPath()
    {
        URL input = createUrl("http://www.blah.com:8080");
        URL expected = createUrl("http://www.blah.com:8080/something/getKml.pl");

        UrlBuilder builder = new UrlBuilder(input);
        builder.setPath("something/getKml.pl");
        try
        {
            Assert.assertEquals(expected.toString(), builder.toURL().toString());
        }
        catch (MalformedURLException e)
        {
            Assert.fail(e.getMessage());
        }

        builder.setPath("/something/getKml.pl");
        try
        {
            Assert.assertEquals(expected.toString(), builder.toURL().toString());
        }
        catch (MalformedURLException e)
        {
            Assert.fail(e.getMessage());
        }
    }

    /** Test for {@link UrlBuilder#toURL()}. */
    @Test
    public void testToURL()
    {
        URL input = createUrl("http://www.blah.com:8080/something/getKml.pl?a=b&c=d#ref");

        UrlBuilder builder = new UrlBuilder(input);
        Assert.assertEquals("http", builder.getProtocol());
        Assert.assertEquals("www.blah.com", builder.getHost());
        Assert.assertEquals(8080, builder.getPort());
        Assert.assertEquals("/something/getKml.pl", builder.getPath());
        Assert.assertEquals("a=b&c=d", builder.getQuery());
        Assert.assertEquals("ref", builder.getRef());
        Assert.assertEquals("b", builder.getQueryParameters().get("a"));
        try
        {
            Assert.assertEquals(input.toString(), builder.toURL().toString());
        }
        catch (MalformedURLException e)
        {
            Assert.fail(e.getMessage());
        }
    }
}

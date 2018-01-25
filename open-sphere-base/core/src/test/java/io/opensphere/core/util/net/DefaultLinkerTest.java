package io.opensphere.core.util.net;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Map;

import org.junit.Test;

import io.opensphere.core.util.collections.New;
import org.junit.Assert;

/** Test {@link DefaultLinker}. */
public class DefaultLinkerTest
{
    /**
     * Test {@link DefaultLinker}.
     *
     * @throws MalformedURLException If the test fails.
     */
    @Test
    public void test() throws MalformedURLException
    {
        Collection<LinkPattern> patterns = New.collection();
        patterns.add(new LinkPattern("Alpha Description", "alph.", null, "http://some.url?path=%s&something=else"));
        patterns.add(new LinkPattern("Beta Description", "beta", null, "http://another.url?path=%s&something=else"));
        patterns.add(new LinkPattern("Beta2 Description", "be.a", null, "http://another.url?path=%s&something=another"));
        patterns.add(new LinkPattern("Value match Description", "be.a", "f.*", "http://another.url?path=%s&something=alternate"));
        patterns.add(new LinkPattern("Bad Description", "badurl", null, "://another.url?path=%s&something=else"));

        DefaultLinker linker = new DefaultLinker();
        linker.addPatterns(patterns);

        Map<String, URL> urls;
        urls = linker.getURLs("alpha", "ga m&ma");
        Assert.assertEquals(1, urls.size());
        Assert.assertEquals(new URL("http://some.url?path=ga+m%26ma&something=else").toString(),
                urls.get("Alpha Description").toString());

        urls = linker.getURLs("alpha2", "gamma");
        Assert.assertTrue(urls.isEmpty());

        urls = linker.getURLs("beta", "ga m&ma");
        Assert.assertEquals(2, urls.size());
        Assert.assertEquals(new URL("http://another.url?path=ga+m%26ma&something=else").toString(),
                urls.get("Beta Description").toString());
        Assert.assertEquals(new URL("http://another.url?path=ga+m%26ma&something=another").toString(),
                urls.get("Beta2 Description").toString());

        urls = linker.getURLs("beta", "fa m&ma");
        Assert.assertEquals(3, urls.size());
        Assert.assertEquals(new URL("http://another.url?path=fa+m%26ma&something=else").toString(),
                urls.get("Beta Description").toString());
        Assert.assertEquals(new URL("http://another.url?path=fa+m%26ma&something=another").toString(),
                urls.get("Beta2 Description").toString());
        Assert.assertEquals(new URL("http://another.url?path=fa+m%26ma&something=alternate").toString(),
                urls.get("Value match Description").toString());

        urls = linker.getURLs("badurl", "gamma");
        Assert.assertTrue(urls.isEmpty());
    }
}

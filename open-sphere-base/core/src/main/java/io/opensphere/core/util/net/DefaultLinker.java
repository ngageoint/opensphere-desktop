package io.opensphere.core.util.net;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Map;

import net.jcip.annotations.NotThreadSafe;

import org.apache.log4j.Logger;

import io.opensphere.core.util.collections.New;

/**
 * Implementation that uses a collection of key patterns and URL patterns to
 * generate URLs.
 */
@NotThreadSafe
public class DefaultLinker implements Linker
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(DefaultLinker.class);

    /** The available patterns. */
    private final Collection<LinkPattern> myPatterns = New.collection();

    /**
     * Add a map of patterns.
     *
     * @param linkPatterns Collection of possible link configurations.
     */
    public void addPatterns(Collection<? extends LinkPattern> linkPatterns)
    {
        myPatterns.addAll(linkPatterns);
    }

    @Override
    public Map<String, URL> getURLs(String key, String value)
    {
        Map<String, URL> result = New.map();
        for (LinkPattern pattern : myPatterns)
        {
            if (pattern.matchesKey(key) && pattern.matchesValue(value))
            {
                try
                {
                    result.put(pattern.getDescription(), pattern.getURL(value));
                }
                catch (MalformedURLException e)
                {
                    LOGGER.error("Failed to generate URL with pattern " + pattern + " for value " + value + ": " + e, e);
                }
            }
        }
        return result;
    }

    @Override
    public boolean hasURLFor(String key, String value)
    {
        if (key != null)
        {
            for (LinkPattern pattern : myPatterns)
            {
                if (pattern.matchesKey(key) && pattern.matchesValue(value))
                {
                    return true;
                }
            }
        }
        return false;
    }
}

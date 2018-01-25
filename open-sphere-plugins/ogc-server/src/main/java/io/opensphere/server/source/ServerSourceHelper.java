package io.opensphere.server.source;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;

import io.opensphere.core.util.lang.StringUtilities;

/**
 * Helper class for the ServerSourceEditorPanel.
 */
public final class ServerSourceHelper
{
    /**
     * Gets a unique server name.
     *
     * @param name the intended name
     * @param otherNames the list of names to check for uniqueness against
     * @return a unique name
     */
    public static String getUniqueName(String name, Collection<? extends String> otherNames)
    {
        int count = 1;
        String uniqueName = name;
        while (otherNames.contains(uniqueName))
        {
            // Add count to the name, then increment if for the next pass.
            uniqueName = name + "_" + Integer.toString(count++);
        }
        return uniqueName;
    }

    /**
     * Checks if URL is valid and unique.
     *
     * @param service the OGC Service associated with this URL
     * @param url the url to check
     * @param otherUrls the other configured server sources' URLs to check for
     *            duplication against
     * @param result the object that holds the result and any errors that occur
     * @return true, if the url is valid and unique
     */
    @SuppressWarnings("unused")
    public static boolean isUrlValidAndUnique(String service, String url, Collection<? extends String> otherUrls,
            EditorValidResult result)
    {
        if (url == null || url.isEmpty())
        {
            result.setError("Empty URL", StringUtilities.concat(service, " URL is blank"));
            return false;
        }

        if (!url.matches("http(s?)://(.*?)"))
        {
            result.setError("URL format Error", StringUtilities.concat(service, " URL does not start with \"http(s)://\""));
            return false;
        }

        try
        {
            new URL(url);
        }
        catch (MalformedURLException ex)
        {
            result.setError("URL format Error", StringUtilities.concat(service, " URL is not a properly formatted URL"));
            return false;
        }

        if (otherUrls.contains(url))
        {
            result.setError("URL Duplication Error", StringUtilities.concat(service, " URL is already in use"));
            return false;
        }
        result.setSuccess();
        return true;
    }

    /** Disallow instantiation of static helper class. */
    private ServerSourceHelper()
    {
    }
}

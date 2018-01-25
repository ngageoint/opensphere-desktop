package io.opensphere.core.util.net;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.function.Function;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.lang.ImpossibleException;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.core.util.lang.ToStringHelper;

/**
 * Defines a key regex pattern for matching relevant keys with a URL format used
 * to generate URLs from input values.
 */
public class LinkPattern
{
    /** Description for the URL. */
    private final String myDescription;

    /** Pattern for matching the key. */
    private final Pattern myKeyPattern;

    /** Pattern for generating the URL. */
    private final String myUrlPattern;

    /** Pattern for matching the value. */
    @Nullable
    private final Pattern myValuePattern;

    /** The optional transformations to be performed on the values. */
    private Collection<Function<String, String>> myTransforms;

    /**
     * Constructor.
     *
     * @param description Description for the URL.
     * @param keyPattern Pattern for matching the key.
     * @param valuePattern Optional pattern for matching the value.
     * @param urlPattern Pattern for generating the URL.
     */
    public LinkPattern(String description, String keyPattern, @Nullable String valuePattern, String urlPattern)
    {
        myKeyPattern = Pattern.compile(Utilities.checkNull(keyPattern, "keyPattern"));
        myValuePattern = valuePattern == null ? null : Pattern.compile(valuePattern);
        myUrlPattern = Utilities.checkNull(urlPattern, "urlPattern");
        myDescription = Utilities.checkNull(description, "description");
    }

    /**
     * Sets the transforms.
     *
     * @param transforms the transforms
     */
    public void setTransforms(Collection<Function<String, String>> transforms)
    {
        myTransforms = transforms;
    }

    /**
     * Get the description for the URL.
     *
     * @return The description.
     */
    public String getDescription()
    {
        return myDescription;
    }

    /**
     * Generate a URL for the given value.
     *
     * @param value The value.
     * @return The URL, or {@code null} if one could not be generated.
     * @throws MalformedURLException If the URL with the given value injected is
     *             malformed.
     */
    public URL getURL(String value) throws MalformedURLException
    {
        String modValue = value;
        if (CollectionUtilities.hasContent(myTransforms))
        {
            for (Function<String, String> transform : myTransforms)
            {
                modValue = transform.apply(modValue);
            }
        }

        try
        {
            if ("%s".equals(myUrlPattern))
            {
                return new URL(modValue);
            }
            else
            {
                return new URL(myUrlPattern.replace("%s", URLEncoder.encode(modValue, StringUtilities.DEFAULT_CHARSET.name())));
            }
        }
        catch (UnsupportedEncodingException e)
        {
            throw new ImpossibleException(e);
        }
    }

    /**
     * Get if the key pattern matches a key.
     *
     * @param key The key.
     * @return {@code true} if the key pattern matches.
     */
    public boolean matchesKey(String key)
    {
        return myKeyPattern.matcher(key).matches();
    }

    /**
     * Get if the value pattern matches a value.
     *
     * @param value The value.
     * @return {@code true} if the value pattern matches.
     */
    public boolean matchesValue(String value)
    {
        return myValuePattern == null || myValuePattern.matcher(value).matches();
    }

    @Override
    public String toString()
    {
        ToStringHelper helper = new ToStringHelper(this, 64);
        helper.add("desc", myDescription);
        helper.add("key", myKeyPattern);
        helper.add("url", myUrlPattern);
        helper.add("value", myValuePattern);
        helper.add("transforms", myTransforms);
        return helper.toStringMultiLine();
    }
}

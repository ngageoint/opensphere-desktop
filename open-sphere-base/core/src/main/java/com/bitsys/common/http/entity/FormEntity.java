package com.bitsys.common.http.entity;

import java.util.Map.Entry;

import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.protocol.HTTP;

import com.bitsys.common.http.header.ContentType;
import com.bitsys.common.http.util.ConversionUtils;

/**
 * This class is an entity representing a list of URL-encoded pairs. This is
 * typically used in HTTP POST requests.
 */
public class FormEntity extends StringEntity
{
    /**
     * Constructs a <code>FormEntity</code>.
     *
     * @param parameters the form parameter key-value pairs.
     */
    public FormEntity(final Iterable<? extends Entry<String, String>> parameters)
    {
        super(URLEncodedUtils.format(ConversionUtils.toNameValuePairs(parameters), HTTP.DEF_CONTENT_CHARSET),
                ContentType.APPLICATION_FORM_URLENCODED);
    }
}

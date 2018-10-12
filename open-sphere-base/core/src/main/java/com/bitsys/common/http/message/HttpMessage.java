package com.bitsys.common.http.message;

import com.bitsys.common.http.entity.HttpEntity;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;

/**
 * This interface defines the basic methods for an HTTP message.
 */
public interface HttpMessage
{
    /**
     * Returns the HTTP headers.
     *
     * @return the HTTP headers.
     */
    ListMultimap<String, String> getHeaders();

    /**
     * Returns the mapping of headers that match the given name in a
     * case-insensitive manner.
     *
     * @param name the header name.
     * @return the mapping of matching header names to values.
     */
    Multimap<String, String> getHeader(String name);

    /**
     * Returns the header value for the given case-insensitive header name.
     *
     * @param name the header name.
     * @return the header value for the given header name or <code>null</code>
     *         if not present.
     */
    String getHeaderValue(String name);

    /**
     * Returns the message entity.
     *
     * @return the message entity or <code>null</code> if it is not provided.
     */
    HttpEntity getEntity();
}

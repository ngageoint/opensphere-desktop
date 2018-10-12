package com.bitsys.common.http.entity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.bitsys.common.http.header.ContentType;

/**
 * This interface represents an HTTP entity that can be sent or received with an
 * HTTP message.
 */
public interface HttpEntity
{
    /**
     * Indicates if this entity is capable of producing its content more than
     * once. Multiple invocations of {@link #getContent()},
     * {@link #writeTo(OutputStream)} and {@link #asString()} can be safely used
     * if the entity is repeatable.
     *
     * @return <code>true</code> if this entity is repeatable.
     */
    boolean isRepeatable();

    /**
     * Returns the content stream of this entity. If this entity is
     * {@link #isRepeatable() repeatable}, the return values from multiple
     * invocations of this method can each be safely consumed. Entities that are
     * not repeatable will return an input stream that cannot be consumed more
     * than once.
     * <p>
     * NOTE: Callers of this method must call {@link InputStream#close()} on the
     * returned stream in order to release any allocated resources.
     *
     * @return the content stream of this entity.
     */
    InputStream getContent();

    /**
     * Returns the value from the <code>Content-Encoding</code> header.
     *
     * @return the value from the <code>Content-Encoding</code> header or
     *         <code>null</code> if unknown.
     */
    String getContentEncoding();

    /**
     * Returns the value from the <code>Content-Length</code> header.
     *
     * @return the value from the <code>Content-Length</code> header or a
     *         negative number if unknown or the length exceeds
     *         {@link Long#MAX_VALUE}.
     */
    long getContentLength();

    /**
     * Returns the value from the <code>Content-Type</code> header.
     *
     * @return the value from the <code>Content-Type</code> header or
     *         <code>null</code> if unknown.
     */
    ContentType getContentType();

    /**
     * Writes the entity content to the given output stream. If this entity is
     * {@link #isRepeatable() repeatable}, this method can be safely invoked
     * multiple times.
     *
     * @param outputStream the destination for the entity content.
     * @throws IOException if an error occurs while writing the content to the
     *             output.
     */
    void writeTo(OutputStream outputStream) throws IOException;

    /**
     * Writes the entity content to a {@link String} and returns that string. If
     * this entity is {@link #isRepeatable() repeatable}, this method can be
     * safely invoked multiple times.
     *
     * @return the string representation of the entity content.
     * @throws IOException if an error occurs while converting the content to a
     *             string.
     */
    String asString() throws IOException;
}

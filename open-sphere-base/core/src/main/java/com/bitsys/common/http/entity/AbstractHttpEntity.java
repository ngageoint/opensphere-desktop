package com.bitsys.common.http.entity;

import com.bitsys.common.http.header.ContentType;

/**
 * This abstract class provides the basic implementation for some methods in
 * {@link HttpEntity}.
 */
public abstract class AbstractHttpEntity implements HttpEntity
{
    /**
     * Indicates if this entity is repeatable.
     */
    private final boolean repeatable;

    /**
     * The value for the <code>Content-Encoding</code> header.
     */
    private String contentEncoding;

    /**
     * The value for the <code>Content-Type</code> header.
     */
    private ContentType contentType;

    /**
     * Constructs a <code>AbstractHttpEntity</code>.
     *
     * @param repeatable indicates if the entity is
     *            {@link HttpEntity#isRepeatable() repeatable}.
     */
    public AbstractHttpEntity(final boolean repeatable)
    {
        this.repeatable = repeatable;
    }

    @Override
    public boolean isRepeatable()
    {
        return repeatable;
    }

    /**
     * Specifies the value for the <code>Content-Encoding</code> header.
     *
     * @param contentEncoding the value for the <code>Content-Encoding</code>
     *            header
     */
    public void setContentEncoding(final String contentEncoding)
    {
        this.contentEncoding = contentEncoding;
    }

    /**
     * @see HttpEntity#getContentEncoding()
     */
    @Override
    public String getContentEncoding()
    {
        return contentEncoding;
    }

    /**
     * Specifies the value for the <code>Content-Type</code> header.
     *
     * @param contentType the value for the <code>Content-Type</code> header
     */
    public void setContentType(final ContentType contentType)
    {
        this.contentType = contentType;
    }

    /**
     * @see HttpEntity#getContentType()
     */
    @Override
    public ContentType getContentType()
    {
        return contentType;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append(getClass().getSimpleName());
        builder.append(" [repeatable=");
        builder.append(isRepeatable());
        builder.append(", contentLength=");
        builder.append(getContentLength());
        builder.append(", contentEncoding=");
        builder.append(getContentEncoding());
        builder.append(", contentType=");
        builder.append(getContentType());
        builder.append("]");
        return builder.toString();
    }
}

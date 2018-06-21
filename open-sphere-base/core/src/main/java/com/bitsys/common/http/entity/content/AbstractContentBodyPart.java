package com.bitsys.common.http.entity.content;

import java.nio.charset.Charset;

import com.bitsys.common.http.header.ContentType;
import com.google.common.base.Preconditions;

/**
 * This class provides a common implementation of {@link ContentBodyPart}s.
 * 
 * @param <T> the content type
 */
public abstract class AbstractContentBodyPart<T> implements ContentBodyPart<T>
{
    /** The content. */
    private final T content;

    /** The optional file name. */
    private final String fileName;

    /** The content type. */
    private final ContentType contentType;

    /**
     * Constructs a new content body part with the given parameters. Derived
     * classes must decide which parameters are appropriate for their purpose.
     *
     * @param content the content.
     * @param fileName the optional file name.
     * @param contentType the content type.
     */
    public AbstractContentBodyPart(final T content, final String fileName, final ContentType contentType)
    {
        Preconditions.checkArgument(content != null, "The content cannot be null");
        Preconditions.checkArgument(contentType != null, "The content type cannot be null");
        this.content = content;
        this.fileName = fileName;
        this.contentType = contentType;
    }

    /**
     * @see ContentBodyPart#getContent()
     */
    @Override
    public T getContent()
    {
        return this.content;
    }

    /**
     * @see ContentBodyPart#getFileName()
     */
    @Override
    public String getFileName()
    {
        return this.fileName;
    }

    /**
     * @see ContentBodyPart#getContentType()
     */
    @Override
    public ContentType getContentType()
    {
        return this.contentType;
    }

    /**
     * @see ContentBodyPart#getMimeType()
     */
    @Override
    public String getMimeType()
    {
        return this.contentType.getMimeType();
    }

    /**
     * @see ContentBodyPart#getCharset()
     */
    @Override
    public Charset getCharset()
    {
        return this.contentType.getCharset();
    }

    /**
     * @see Object#toString()
     */
    @Override
    public String toString()
    {
        final StringBuilder builder = new StringBuilder();
        builder.append(getClass().getSimpleName()).append(" [");
        builder.append("content=");
        builder.append(getContent());
        if (getFileName() != null)
        {
            builder.append(", ");
            builder.append("fileName=");
            builder.append(getFileName());
        }
        builder.append(", ");
        builder.append("contentType=");
        builder.append(getContentType());
        builder.append("]");
        return builder.toString();
    }
}

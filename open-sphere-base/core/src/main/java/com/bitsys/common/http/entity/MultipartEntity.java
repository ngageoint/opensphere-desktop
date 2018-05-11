package com.bitsys.common.http.entity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.http.Header;
import org.apache.http.entity.mime.HttpMultipartMode;

import com.bitsys.common.http.entity.content.ContentBodyPart;
import com.bitsys.common.http.entity.content.ContentUtils;
import com.bitsys.common.http.header.ContentType;

/**
 * This class is a multipart/form coded HTTP entity consisting of multiple body
 * parts. It is a this wrapper around Apache's
 * {@link org.apache.http.entity.mime.MultipartEntity}.
 */
@SuppressWarnings("deprecation")
public class MultipartEntity implements HttpEntity
{
    /** Apache's MultipartEntity instance. */
    private final org.apache.http.entity.mime.MultipartEntity entity;

    /** The content type */
    private final ContentType contentType;

    /**
     * Constructs a new {@linkplain MultipartEntity}.
     */
    public MultipartEntity()
    {
        entity = new org.apache.http.entity.mime.MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
        final Header contentTypeHeader = entity.getContentType();
        if (contentTypeHeader != null)
        {
            contentType = ContentType.parse(contentTypeHeader.getValue());
        }
        else
        {
            contentType = null;
        }
    }

    /**
     * Adds a file part to the entity.
     *
     * @param name the name of the part.
     * @param bodyPart the body part to add.
     */
    public void addPart(final String name, final ContentBodyPart<?> bodyPart)
    {
        entity.addPart(name, ContentUtils.toContentBody(bodyPart));
    }

    /**
     * This operation is not supported.
     *
     * @throws UnsupportedOperationException when invoked.
     */
    @Override
    public InputStream getContent()
    {
        throw new UnsupportedOperationException("getContent() is not supported for multi-part entities");
    }

    @Override
    public long getContentLength()
    {
        return entity.getContentLength();
    }

    @Override
    public boolean isRepeatable()
    {
        return entity.isRepeatable();
    }

    @Override
    public void writeTo(final OutputStream outputStream) throws IOException
    {
        entity.writeTo(outputStream);
    }

    /**
     * This operation is not supported.
     *
     * @throws UnsupportedOperationException when invoked.
     */
    @Override
    public String asString() throws IOException
    {
        throw new UnsupportedOperationException("asString() is not supported for multi-part entities");
    }

    @Override
    public String getContentEncoding()
    {
        final Header contentEncoding = entity.getContentEncoding();
        return contentEncoding == null ? null : contentEncoding.getValue();
    }

    @Override
    public ContentType getContentType()
    {
        return contentType;
    }

    /**
     * <span style="color:red">INTERNAL API - SUBJECT TO CHANGE AT ANY TIME -
     * USE AT YOUR OWN RISK.</span>
     * <p>
     * Returns Apache's {@link org.apache.http.entity.mime.MultipartEntity
     * MultipartEntity}.
     *
     * @return Apache's entity.
     */
    org.apache.http.entity.mime.MultipartEntity getEntity()
    {
        return entity;
    }
}

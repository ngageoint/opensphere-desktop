package com.bitsys.common.http.entity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;

import com.bitsys.common.http.header.ContentType;

/**
 * This class is an input stream wrapper HTTP entity. Its functionality is
 * similar to that of {@link org.apache.http.entity.InputStreamEntity}.
 */
public class InputStreamEntity extends AbstractHttpEntity
{
   /**
    * The entity's input stream content.
    */
   private final InputStream content;

   /**
    * The content length.
    */
   private long contentLength;

   /**
    * Constructs a <code>InputStreamEntity</code>.
    *
    * @param inputStream
    *           the entity's input stream content.
    */
   public InputStreamEntity(final InputStream inputStream)
   {
      this(inputStream, -1);
   }

   /**
    *
    * Constructs a <code>InputStreamEntity</code>.
    *
    * @param inputStream
    *           the entity's input stream content.
    * @param contentLength
    *           the content length or <code>-1</code> if unknown.
    */
   public InputStreamEntity(final InputStream inputStream, final long contentLength)
   {
      this(inputStream, contentLength, null);
   }

   /**
    * Constructs a <code>InputStreamEntity</code>.
    *
    * @param inputStream
    *           the entity's input stream content.
    * @param contentLength
    *           the content length or <code>-1</code> if unknown.
    * @param contentType
    *           the value for the <code>Content-Type</code> header.
    */
   public InputStreamEntity(final InputStream inputStream, final long contentLength,
                            final ContentType contentType)
   {
      super(false);
      content = inputStream;
      this.contentLength = contentLength;
      setContentType(contentType);
   }

   @Override
   public InputStream getContent()
   {
      return content;
   }

   @Override
   public long getContentLength()
   {
      return contentLength;
   }

   @Override
   public void writeTo(final OutputStream outputStream) throws IOException
   {
      contentLength = IOUtils.copyLarge(getContent(), outputStream);
   }

   @Override
   public String asString() throws IOException
   {
      return IOUtils.toString(getContent(), getContentType() == null ? null
         : getContentType().toString());
   }
}

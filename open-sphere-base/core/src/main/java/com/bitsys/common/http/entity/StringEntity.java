package com.bitsys.common.http.entity;

import static org.apache.commons.lang3.StringUtils.abbreviate;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

import com.bitsys.common.http.header.ContentType;

/**
 * This class represents a simple string form of an HTTP entity.
 *
 * @see org.apache.http.entity.StringEntity
 */
public class StringEntity extends AbstractHttpEntity
{
   /**
    * The content of this entity.
    */
   private final String content;

   /**
    * The byte array form of the content string.
    */
   private byte[] contentArray;

   /**
    * Constructs a <code>StringEntity</code>. Defaults the
    * <code>Content-Type</code> to "<code>text/plain</code>" and the character
    * encoding of the string to "<code>ISO-8859-1</code>".
    *
    * @param string
    *           the entity content.
    */
   public StringEntity(final String string)
   {
      this(string, ContentType.TEXT_PLAIN);
   }

   /**
    * Constructs a <code>StringEntity</code>. Defaults the
    * <code>Content-Type</code> to "<code>text/plain</code>".
    *
    * @param string
    *           the entity content.
    */
   public StringEntity(final String string, final Charset charset)
   {
      this(string, ContentType.create(ContentType.TEXT_PLAIN.getMimeType(), charset));
   }

   /**
    * Constructs a <code>StringEntity</code>.
    *
    * @param string
    *           the entity content.
    */
   public StringEntity(final String string, final ContentType contentType)
   {
      super(true);
      if (string == null)
      {
         throw new IllegalArgumentException("The entity content string is null");
      }
      content = string;

      if (contentType != null && contentType.getCharset() != null)
      {
         contentArray = string.getBytes(contentType.getCharset());
      }
      else
      {
         contentArray = string.getBytes();
      }
      setContentType(contentType);
   }

   /**
    * @see HttpEntity#getContent()
    */
   @Override
   public InputStream getContent()
   {
      return new ByteArrayInputStream(contentArray);
   }

   /**
    * Returns the value for the <code>Content-Length</code> header.
    *
    * @return the length of the content.
    *
    * @see HttpEntity#getContentLength()
    */
   @Override
   public long getContentLength()
   {
      return contentArray.length;
   }

   /**
    *
    * @see HttpEntity#writeTo(java.io.OutputStream)
    */
   @Override
   public void writeTo(final OutputStream outputStream) throws IOException
   {
      outputStream.write(contentArray);
   }

   /**
    * Returns the string content of this entity.
    *
    * @return the string content of this entity.
    * @see HttpEntity#asString()
    */
   @Override
   public String asString()
   {
      return content;
   }

   /**
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString()
   {
      final StringBuilder builder = new StringBuilder();
      builder.append(getClass().getName());
      builder.append(" [");
      if (content != null)
      {
         builder.append("content=");
         builder.append(abbreviate(content, 32));
         builder.append(", ");
      }
      builder.append("contentLength=");
      builder.append(getContentLength());
      builder.append(", ");
      if (getContentEncoding() != null)
      {
         builder.append("contentEncoding=");
         builder.append(getContentEncoding());
         builder.append(", ");
      }
      if (getContentType() != null)
      {
         builder.append("contentType=");
         builder.append(getContentType());
      }
      builder.append("]");
      return builder.toString();
   }
}

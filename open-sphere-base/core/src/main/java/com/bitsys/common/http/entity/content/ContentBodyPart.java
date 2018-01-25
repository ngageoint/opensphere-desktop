package com.bitsys.common.http.entity.content;

import java.nio.charset.Charset;

import com.bitsys.common.http.header.ContentType;

/**
 * This interface describes a content body part.
 */
public interface ContentBodyPart<T>
{
   /**
    * Returns the body's content.
    *
    * @return the body's content.
    */
   T getContent();

   /**
    * Returns the optional file name of the content.
    *
    * @return the file name of the content or <code>null</code>.
    */
   String getFileName();

   /**
    * Returns the content type of the content.
    *
    * @return the content's content type.
    */
   ContentType getContentType();

   /**
    * Returns the MIME type of the content.
    *
    * @return the content's MIME type or <code>null</code>.
    */
   String getMimeType();

   /**
    * Returns the character set of the content.
    *
    * @return the content's character set or <code>null</code>.
    */
   Charset getCharset();
}

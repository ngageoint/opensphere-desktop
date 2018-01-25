package com.bitsys.common.http.entity;

import java.io.IOException;
import java.nio.charset.Charset;

import org.apache.http.Header;

import com.bitsys.common.http.header.ContentType;

/**
 * <code>HttpEntityConversionUtils</code> aids in converting between this library's types and the
 * Apache equivalent.
 */
public class HttpEntityConversionUtils {
   /** Hide the default constructor. */
   private HttpEntityConversionUtils() {
   }

   /**
    * Converts a {@link HttpEntity} to Apache's {@link org.apache.http.HttpEntity HttpEntity}.
    *
    * @param entity
    *           the entity to convert.
    * @return the converted entity.
    */
   public static org.apache.http.HttpEntity toApacheEntity(final HttpEntity entity) {
      org.apache.http.HttpEntity apacheEntity = null;

      if (entity instanceof StringEntity) {
         apacheEntity = toApacheEntity((StringEntity)entity);
      }
      else if (entity instanceof InputStreamEntity) {
         apacheEntity = toApacheEntity((InputStreamEntity)entity);
      }
      else if (entity instanceof MultipartEntity) {
         apacheEntity = toApacheEntity((MultipartEntity)entity);
      }
      else if (entity != null) {
         throw new IllegalArgumentException("Unsupported HttpEntity type: "
            + entity.getClass().getName());
      }
      return apacheEntity;
   }

   /**
    * Converts this library's <code>StringEntity</code> to the Apache equivalent.
    *
    * @param stringEntity
    *           the entity to convert.
    * @return the Apache entity.
    */
   public static org.apache.http.HttpEntity toApacheEntity(final StringEntity stringEntity) {
      org.apache.http.HttpEntity apacheEntity;
      final ContentType contentType = stringEntity.getContentType();
      if (contentType != null) {
         apacheEntity =
            new org.apache.http.entity.StringEntity(stringEntity.asString(),
               org.apache.http.entity.ContentType.create(contentType.getMimeType(),
                                                         contentType.getCharset()));
      }
      else {
         apacheEntity =
            new org.apache.http.entity.StringEntity(stringEntity.asString(),
               Charset.defaultCharset());
      }
      return apacheEntity;
   }

   /**
    * Converts this library's <code>InputStreamEntity</code> to the Apache equivalent.
    *
    * @param inputStreamEntity
    *           the entity to convert.
    * @return the Apache entity.
    */
   public static org.apache.http.entity.InputStreamEntity toApacheEntity(final InputStreamEntity inputStreamEntity) {
      org.apache.http.entity.InputStreamEntity apacheEntity;
      final long contentLength = inputStreamEntity.getContentLength();
      org.apache.http.entity.ContentType contentType = null;
      if (inputStreamEntity.getContentType() != null) {
         contentType =
            org.apache.http.entity.ContentType.create(inputStreamEntity.getContentType()
               .getMimeType(), inputStreamEntity.getContentType().getCharset());
      }
      apacheEntity =
         new org.apache.http.entity.InputStreamEntity(inputStreamEntity.getContent(),
            contentLength, contentType);
      return apacheEntity;
   }

   /**
    * Converts this library's <code>MultipartEntity</code> to the Apache equivalent.
    *
    * @param multipartEntity
    *           the entity to convert.
    * @return the Apache entity.
    */
   public static org.apache.http.HttpEntity toApacheEntity(final MultipartEntity multipartEntity) {
      return multipartEntity.getEntity();
   }

   /**
    * Converts the Apache HTTP entity to this library's HTTP entity.
    *
    * @param apacheEntity
    *           the entity to convert.
    * @return the converted entity.
    * @throws IOException
    *            if the entity's content input stream could not be created.
    */
   public static HttpEntity fromApacheEntity(final org.apache.http.HttpEntity apacheEntity)
      throws IOException {
      AbstractHttpEntity entity = null;
      if (apacheEntity != null) {
         ContentType contentType = null;
         final Header contentTypeHeader = apacheEntity.getContentType();
         if (contentTypeHeader != null) {
            contentType = ContentType.parse(contentTypeHeader.getValue());
         }
         entity =
            new InputStreamEntity(apacheEntity.getContent(), apacheEntity.getContentLength(),
               contentType);
         if (apacheEntity.getContentEncoding() != null) {
            entity.setContentEncoding(apacheEntity.getContentEncoding().getValue());
         }
      }
      return entity;
   }
}

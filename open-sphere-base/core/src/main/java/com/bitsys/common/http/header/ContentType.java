package com.bitsys.common.http.header;

import static org.apache.commons.lang3.StringUtils.isBlank;

import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HeaderElement;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.message.BasicHeaderValueParser;

/**
 * This class represents the value for an HTTP <code>Content-Type</code>
 * consisting of a MIME type and optional parameters as defined by RFC 1341.
 *
 * @see org.apache.http.entity.ContentType
 */
public final class ContentType implements Serializable
{
   /** serialVersionUID */
   private static final long serialVersionUID = 6119843722060291866L;

   /**
    * The application/atom+xml content type. The character set is ISO-8859-1.
    */
   public static final ContentType APPLICATION_ATOM_XML = new ContentType(
      org.apache.http.entity.ContentType.APPLICATION_ATOM_XML);

   /**
    * The application/x-www-form-urlencoded content type. The character set is
    * ISO-8859-1.
    */
   public static final ContentType APPLICATION_FORM_URLENCODED = new ContentType(
      org.apache.http.entity.ContentType.APPLICATION_FORM_URLENCODED);

   /**
    * The application/json content type. The character set is UTF-8.
    */
   public static final ContentType APPLICATION_JSON = new ContentType(
      org.apache.http.entity.ContentType.APPLICATION_JSON);

   /**
    * The application/octet-stream content type. The character set is
    * <code>null</code>.
    */
   public static final ContentType APPLICATION_OCTET_STREAM = new ContentType(
      org.apache.http.entity.ContentType.APPLICATION_OCTET_STREAM);

   /**
    * The application/svg+xml content type. The character set is ISO-8859-1.
    */
   public static final ContentType APPLICATION_SVG_XML = new ContentType(
      org.apache.http.entity.ContentType.APPLICATION_SVG_XML);

   /**
    * The application/xhtml+xml content type. The character set is ISO-8859-1.
    */
   public static final ContentType APPLICATION_XHTML_XML = new ContentType(
      org.apache.http.entity.ContentType.APPLICATION_ATOM_XML);

   /**
    * The application/xml content type. The character set is ISO-8859-1.
    */
   public static final ContentType APPLICATION_XML = new ContentType(
      org.apache.http.entity.ContentType.APPLICATION_XML);

   /**
    * The application/form-data content type. The character set is ISO-8859-1.
    */
   public static final ContentType MULTIPART_FORM_DATA = new ContentType(
      org.apache.http.entity.ContentType.MULTIPART_FORM_DATA);

   /**
    * The text/html content type. The character set is ISO-8859-1.
    */
   public static final ContentType TEXT_HTML = new ContentType(
      org.apache.http.entity.ContentType.TEXT_HTML);

   /**
    * The text/plain content type. The character set is ISO-8859-1.
    */
   public static final ContentType TEXT_PLAIN = new ContentType(
      org.apache.http.entity.ContentType.TEXT_PLAIN);

   /**
    * The text/xml content type. The character set is ISO-8859-1.
    *
   public static final ContentType TEXT_XML = new ContentType(
      org.apache.http.entity.ContentType.TEXT_XML);

   /**
    * The *&#47;* content type. The character set is <code>null</code>.
    */
   public static final ContentType WILDCARD = new ContentType(
      org.apache.http.entity.ContentType.WILDCARD);

   /**
    * The standard "<code>charset</code>" parameter attribute name. This
    * parameter makes sense with several content types.
    */
   public static final String CHARSET = "charset";

   /**
    * The standard "<code>boundary</code>" parameter attribute name. This
    * parameter makes sense with "<code>multipart</code>" content type.
    */
   public static final String BOUNDARY = "boundary";

   /**
    * The MIME type.
    */
   private final String mimeType;

   /**
    * The optional character set encoding.
    */
   private final Charset charset;

   /**
    * The mapping of parameters.
    */
   private final Map<String, String> parameters;

   /**
    * Constructs a <code>ContentType</code>.
    *
    * @param contentType
    *           Apache's content type to copy.
    */
   ContentType(final org.apache.http.entity.ContentType contentType)
   {
      this(contentType.getMimeType(), contentType.getCharset());
   }

   /**
    * Constructs a <code>ContentType</code>.
    *
    * @param mimeType
    *           the MIME type.
    * @param charset
    *           the character set encoding or <code>null</code>.
    */
   ContentType(final String mimeType, final Charset charset)
   {
      this(mimeType, charset, null);
   }

   /**
    * Constructs a <code>ContentType</code>. The given charset will be added to
    * the internal parameters map if it is not already present in the given map.
    *
    * @param mimeType
    *           the MIME type.
    * @param charset
    *           the character set encoding or <code>null</code>.
    * @param parameters
    *           the mapping of parameters or <code>null</code> if there are no
    *           parameters.
    */
   ContentType(final String mimeType, final Charset charset, final Map<String, String> parameters)
   {
      this.mimeType = mimeType;
      this.charset = charset;
      final Map<String, String> map = new LinkedHashMap<>();

      // If the parameter map doesn't already contain the charset parameter,
      // add it first.
      if ((parameters == null || !parameters.containsKey(CHARSET))
         && charset != null)
      {
         map.put(CHARSET, charset.name());
      }

      if (parameters != null)
      {
         map.putAll(parameters);
      }

      this.parameters = Collections.unmodifiableMap(map);
   }

   /**
    * Returns the MIME type of this content type.
    *
    * @return the MIME type.
    */
   public String getMimeType()
   {
      return mimeType;
   }

   /**
    * Returns the character set encoding.
    *
    * @return the character set encoding or <code>null</code>.
    */
   public Charset getCharset()
   {
      return charset;
   }

   /**
    * Returns an unmodifiable map of content-type parameters. The order of these
    * parameters matches the order specified during construction. However,
    * according to RFC 1341, the order does not matter. This map will also
    * contain the {@link #CHARSET charset} parameter if provided.
    *
    * @return the mapping of parameters.
    */
   public Map<String, String> getParameters()
   {
      return parameters;
   }

   /**
    * Creates a new instance of a {@link ContentType}.
    *
    * @param mimeType
    *           the MIME type.
    * @return the new content type instance.
    * @see org.apache.http.entity.ContentType#create(String)
    */
   public static ContentType create(final String mimeType)
   {
      return create(mimeType, (Charset)null);
   }

   /**
    * Creates a new instance of a {@link ContentType}.
    *
    * @param mimeType
    *           the MIME type.
    * @param charset
    *           the character set encoding or <code>null</code>.
    * @return the new content type instance.
    * @see org.apache.http.entity.ContentType#create(String, Charset)
    */
   public static ContentType create(final String mimeType, final Charset charset)
   {
      final Map<String, String> parameters = Collections.emptyMap();
      return create(mimeType, charset, parameters);
   }

   /**
    * Creates a new instance of a {@link ContentType}. If the given charset is
    * not included in this map, it will be added.
    *
    * @param mimeType
    *           the MIME type.
    * @param charset
    *           the character set encoding or <code>null</code>.
    * @param parameters
    *           the parameter map or <code>null</code>.
    * @return the new content type instance.
    */
   public static ContentType create(final String mimeType, final Charset charset,
                                    final Map<String, String> parameters)
   {
      validateMimeType(mimeType);
      return new ContentType(mimeType, charset, parameters);
   }

   /**
    * Creates a new instance of a {@link ContentType}.
    *
    * @param headerElement
    *           the header element.
    * @return the new content type instance.
    * @throws IllegalArgumentException
    *            if the MIME type is invalid.
    * @throws UnsupportedCharsetException
    *            if the specified character set is not supported.
    */
   private static ContentType create(final HeaderElement headerElement)
   {
      final String mimeType = headerElement.getName();

      Charset charset = null;
      final Map<String, String> parameters = new LinkedHashMap<>();
      for (final NameValuePair pair : headerElement.getParameters())
      {
         if (StringUtils.equals(CHARSET, pair.getName()))
         {
            charset = Charset.forName(pair.getValue());
         }
         parameters.put(pair.getName(), pair.getValue());
      }

      return new ContentType(mimeType, charset, parameters);
   }

   /**
    * Attempts to parse the string representation of a <code>Content-Type</code>
    * value no validation is performed on the parsed content type.
    *
    * @param contentType
    *           the string to parse.
    * @return the new content type instance.
    * @throws ParseException
    *            if the string does not appear to represent a valid
    *            <code>Content-Type</code> value.
    * @throws UnsupportedCharsetException
    *            if the specified character set is not supported.
    * @see org.apache.http.entity.ContentType#parse(String)
    */
   public static ContentType parse(final String contentType)
   {
      if (contentType == null)
      {
         throw new IllegalArgumentException("The content type parameter cannot be null");
      }
      final HeaderElement[] elements =
         BasicHeaderValueParser.parseElements(contentType, null);
      if (elements.length != 1)
      {
         throw new ParseException("Invalid content type: " + contentType);
      }
      return create(elements[0]);
   }

   /**
    * Validates the MIME type.
    *
    * @param mimeType
    *           the MIME type to validate.
    * @throws IllegalArgumentException
    *            if the MIME type fails validation.
    */
   public static void validateMimeType(final String mimeType)
   {
      // Ensure that the MIME type is not null or blank.
      if (isBlank(mimeType))
      {
         throw new IllegalArgumentException("The MIME type cannot be null or blank");
      }

      // Verify that the MIME type contains a slash.
      final int slash = mimeType.indexOf('/');
      if (slash < 0)
      {
         throw new IllegalArgumentException("The MIME type, " + mimeType
            + ", must contain a '/'");
      }

      // Break the MIME type into its components.
      final String type = mimeType.substring(0, slash).toLowerCase(Locale.US);
      final String subtype = mimeType.substring(slash + 1).toLowerCase(Locale.US);

      String reason = Rfc4288Utils.validateRegName(type);
      if (reason != null)
      {
         throw new IllegalArgumentException(reason);
      }

      reason = Rfc4288Utils.validateRegName(subtype);
      if (reason != null)
      {
         throw new IllegalArgumentException(reason);
      }
   }

   /**
    * @see java.lang.Object#hashCode()
    */
   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + (charset == null ? 0 : charset.hashCode());
      result = prime * result + mimeType.hashCode();
      result = prime * result + parameters.hashCode();
      return result;
   }

   /**
    * @see java.lang.Object#equals(java.lang.Object)
    */
   @Override
   public boolean equals(final Object obj)
   {
      if (this == obj)
      {
         return true;
      }
      if (obj == null)
      {
         return false;
      }
      if (getClass() != obj.getClass())
      {
         return false;
      }
      final ContentType other = (ContentType)obj;
      if (charset == null)
      {
         if (other.charset != null)
         {
            return false;
         }
      }
      else if (!charset.equals(other.charset))
      {
         return false;
      }
      if (!mimeType.equals(other.mimeType))
      {
         return false;
      }
      if (!parameters.equals(other.parameters))
      {
         return false;
      }
      return true;
   }

   /**
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString()
   {
      final StringBuilder builder = new StringBuilder();
      builder.append(getMimeType());
      for (final Entry<String, String> entry : getParameters().entrySet())
      {
         builder.append("; ");
         builder.append(entry.getKey()).append('=');
         if (Rfc1341Utils.isToken(entry.getValue()))
         {
            builder.append(entry.getValue());
         }
         else
         {
            builder.append('"').append(entry.getValue()).append('"');
         }
      }
      return builder.toString();
   }
}

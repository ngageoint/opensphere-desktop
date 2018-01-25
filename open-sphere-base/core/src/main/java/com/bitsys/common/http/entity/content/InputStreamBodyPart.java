package com.bitsys.common.http.entity.content;

import java.io.InputStream;

import com.bitsys.common.http.header.ContentType;

/**
 * This class defines an input stream body part.
 */
public class InputStreamBodyPart extends AbstractContentBodyPart<InputStream> {
   /**
    * Constructs a new {@linkplain InputStreamBodyPart} from the given input
    * stream and file name. The content type defaults to
    * <code>application/octet-stream</code>.
    *
    * @param inputStream
    *           the input stream that provides the content.
    * @param fileName
    *           the optional file name.
    */
   public InputStreamBodyPart(final InputStream inputStream, final String fileName) {
      this(inputStream, ContentType.APPLICATION_OCTET_STREAM, fileName);
   }

   /**
    * Constructs a new {@linkplain InputStreamBodyPart} from the given input
    * stream, content type and file name.
    *
    * @param inputStream
    *           the input stream that provides the content.
    * @param contentType
    *           the content type or <code>null</code>.
    * @param fileName
    *           the optional file name or <code>null</code>.
    */
   public InputStreamBodyPart(final InputStream inputStream,
                              final ContentType contentType, final String fileName) {
      super(inputStream, fileName, contentType);
   }
}

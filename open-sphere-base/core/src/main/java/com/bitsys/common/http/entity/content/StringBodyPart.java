package com.bitsys.common.http.entity.content;

import com.bitsys.common.http.header.ContentType;

/**
 * This class defines a string body part.
 */
public class StringBodyPart extends AbstractContentBodyPart<String>
{
   /**
    * Constructs a new {@linkplain StringBodyPart} from the given text and
    * content type.
    *
    * @param text
    *           the content body.
    * @param contentType
    *           the content type.
    */
   public StringBodyPart(final String text, final ContentType contentType)
   {
      super(text, null, contentType);
   }
}

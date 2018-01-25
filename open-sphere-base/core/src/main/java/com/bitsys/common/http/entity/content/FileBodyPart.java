package com.bitsys.common.http.entity.content;

import java.io.File;

import com.bitsys.common.http.header.ContentType;

/**
 * The <code>FileBodyPart</code> represents a file included in an HTTP entity.
 */
public class FileBodyPart extends AbstractContentBodyPart<File>
{
   /**
    * Constructs a new {@link FileBodyPart} using the given file. The MIME type
    * defaults to <code>application/octet-stream</code>.
    *
    * @param file
    *           the file content.
    */
   public FileBodyPart(final File file)
   {
      this(file, ContentType.APPLICATION_OCTET_STREAM, file != null ? file.getName() : null);
   }

   /**
    * Constructs a new {@link FileBodyPart} using the given file and content type.
    *
    * @param file
    *           the file content.
    * @param contentType
    *           the content type or <code>null</code>.
    */
   public FileBodyPart(final File file, final ContentType contentType)
   {
      this(file,contentType, null);
   }

   /**
    * Constructs a new {@link FileBodyPart} using the given file, content type and file name.
    *
    * @param file
    *           the file content.
    * @param contentType
    *           the Content Type or <code>null</code>.
    * @param fileName
    *           the file name or <code>null</code>.
    */
   public FileBodyPart(final File file, final ContentType contentType, final String fileName)
   {
      super(file, fileName, contentType);
   }
}

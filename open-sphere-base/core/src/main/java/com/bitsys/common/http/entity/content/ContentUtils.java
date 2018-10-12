package com.bitsys.common.http.entity.content;

import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.entity.mime.content.StringBody;

/**
 * This class provides utility methods for working with entity contents.
 */
public final class ContentUtils
{
    /**
     * Hide the default constructor.
     */
    private ContentUtils()
    {
    }

    /**
     * Converts the {@link ContentBodyPart} to Apache's {@link ContentBody}.
     *
     * @param bodyPart the content body part.
     * @return Apache's content body.
     */
    public static ContentBody toContentBody(final ContentBodyPart<?> bodyPart)
    {
        ContentBody contentBody = null;
        if (bodyPart instanceof FileBodyPart)
        {
            final FileBodyPart part = (FileBodyPart)bodyPart;
            final String charset = part.getCharset() == null ? null : part.getCharset().name();
            ContentType contentType = ContentType.create(part.getMimeType(), charset);
            contentBody = new FileBody(part.getContent(), contentType, part.getFileName());
        }
        else if (bodyPart instanceof InputStreamBodyPart)
        {
            final InputStreamBodyPart part = (InputStreamBodyPart)bodyPart;
            ContentType contentType = ContentType.create(part.getMimeType());
            contentBody = new InputStreamBody(part.getContent(), contentType, part.getFileName());
        }
        else if (bodyPart instanceof StringBodyPart)
        {
            final StringBodyPart part = (StringBodyPart)bodyPart;
            ContentType contentType = ContentType.create(part.getMimeType(), part.getCharset());
            contentBody = new StringBody(part.getContent(), contentType);
        }
        return contentBody;
    }
}

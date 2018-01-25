package io.opensphere.core.util.net;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.ClosedByInterruptException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;

import org.apache.log4j.Logger;

import io.opensphere.core.util.io.StreamReader;

/**
 * Helper for generating useful logging messages when the HTTP response type is
 * not what was expected.
 */
public final class UnexpectedResponseReporter
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(UnexpectedResponseReporter.class);

    /**
     * Report an unexpected HTTP response.
     *
     * @param contentType The content type of the response.
     * @param stream The response stream.
     */
    public static void reportUnexpectedResponse(String contentType, InputStream stream)
    {
        String charsetName;
        int charsetIndex = contentType.indexOf("charset=");
        if (charsetIndex == -1)
        {
            // Use default charset.
            charsetName = "ISO-8859-1";
        }
        else
        {
            int charsetEndIndex = charsetIndex + 8;
            while (charsetEndIndex < contentType.length())
            {
                char ch = contentType.charAt(charsetEndIndex);
                if (ch == ';' || Character.isWhitespace(ch))
                {
                    break;
                }
                charsetEndIndex++;
            }
            charsetName = contentType.substring(charsetIndex + 8, charsetEndIndex);
        }
        try
        {
            Charset charset = Charset.forName(charsetName);
            int limit = 256;
            ByteBuffer buf = new StreamReader(stream, limit).readStreamIntoBuffer();
            CharsetDecoder decoder = charset.newDecoder();
            CharBuffer out = CharBuffer.allocate((int)(buf.remaining() * decoder.averageCharsPerByte()));
            CoderResult result = decoder.decode(buf, out, true);
            String text = out.flip().toString();
            if (result.isError())
            {
                if (text.length() > 0)
                {
                    LOGGER.warn("Response text could not be fully decoded using " + charset + " encoding.");
                    LOGGER.warn("Partially decoded response text is: [" + text + "]");
                }
                else
                {
                    LOGGER.warn("Response text could not be decoded using " + charset + " encoding.");
                }
            }
            else
            {
                LOGGER.warn("First " + limit + " bytes of response text decoded using " + charset + " is: [" + text + "].");
            }
        }
        catch (IllegalCharsetNameException e)
        {
            LOGGER.error("Charset name from response [" + charsetName + "] is illegal: " + e, e);
        }
        catch (UnsupportedCharsetException e)
        {
            LOGGER.error("Charset name from response [" + charsetName + "] is unsupported: " + e, e);
        }
        catch (IllegalArgumentException e)
        {
            LOGGER.error("Charset name from response [" + charsetName + "] is illegal: " + e, e);
        }
        catch (ClosedByInterruptException e)
        {
            if (LOGGER.isTraceEnabled())
            {
                LOGGER.trace("Reading response was closed by interrupt.", e);
            }
        }
        catch (IOException e)
        {
            LOGGER.error("Failed to read response stream: " + e, e);
        }
    }

    /** Disallow instantiation. */
    private UnexpectedResponseReporter()
    {
    }
}

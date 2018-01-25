package io.opensphere.core.common.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;

/**
 * A utility class to help detect the presence of any Unicode BOM (Byte Order
 * Mark). A BOM is sometimes included at the beginning of an
 * <code>InputerStream</code> and each encoding type uses their own BOM.
 *
 * <li>
 *
 * <pre>
 * 00 00 FE FF = UTF-32 big-endian
 * </pre>
 *
 * </li>
 * <li>
 *
 * <pre>
 * FF EE 00 00 = UTF-32 little-endian
 * </pre>
 *
 * </li>
 * <li>
 *
 * <pre>
 * FE FF       = UTF-16 big-endian
 * </pre>
 *
 * </li>
 * <li>
 *
 * <pre>
 * FF FE       = UTF-16 little-endian
 * </pre>
 *
 * </li>
 * <li>
 *
 * <pre>
 * EF BB BR    = UTF-8
 * </pre>
 *
 * </li>
 */
public class BOMUtil
{
    /**
     * Skips the BOM (Byte Order Mark) from the <code>InputStream</code> if it
     * exists.
     *
     * @param inputStream <code>InputStream</code> to remove the BOM from.
     * @return An <code>InputStream</code> without the BOM.
     * @throws IOException
     */
    public static InputStream skipBOM(InputStream inputStream) throws IOException
    {
        // number of bytes to put back on the input stream
        int putBack = 0;

        PushbackInputStream in = new PushbackInputStream(inputStream, 4);
        final byte[] bom = new byte[4];
        final int read = in.read(bom);

        if (bom[0] == (byte)0xFF && bom[1] == (byte)0xFE && bom[2] == (byte)0x00 && bom[3] == (byte)0x00)
        {
            // UTF-32 LE
            putBack = 0;
        }
        else if (bom[0] == (byte)0x00 && bom[1] == (byte)0x00 && bom[2] == (byte)0xFE && bom[3] == (byte)0xFF)
        {
            // UTF-32 BE
            putBack = 0;
        }
        else if (bom[0] == (byte)0xEF && bom[1] == (byte)0xBB && bom[2] == (byte)0xBF)
        {
            // UTF-8
            putBack = 1;
        }
        else if (bom[0] == (byte)0xFF && bom[1] == (byte)0xFE)
        {
            // UTF-16 LE
            putBack = 2;
        }
        else if (bom[0] == (byte)0xFF && bom[1] == (byte)0xFE)
        {
            // UTF-16 BE
            putBack = 2;
        }
        else
        {
            // no BOM
            putBack = 4;
        }

        if (putBack > 0)
        {
            // no BOM found so put the non-BOM bytes back on the input stream
            in.unread(bom, read - putBack, putBack);
        }

        return in;
    }
}

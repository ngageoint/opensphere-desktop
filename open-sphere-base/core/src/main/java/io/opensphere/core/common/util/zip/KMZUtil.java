package io.opensphere.core.common.util.zip;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class KMZUtil
{

    /**
     * Inspects the KMZ ZIP archive for the first file that ends with .kml and
     * returns it as an <code>InputStream</code>.
     * <p>
     * Call this method under a try-with-resources on the zipStream.
     *
     * @param zipStream KMZ file to inspect
     * @return The first KML file in the archive or <code>null</code> if none
     *         can be found.
     * @throws IOException
     */
    public static InputStream extractRootKMLFile(ZipInputStream zipStream) throws IOException
    {
        final int BUFFER_SIZE = 2048;

        ByteArrayOutputStream bos = null;
        ByteArrayInputStream bais = null;

        ZipEntry entry;

        while ((entry = zipStream.getNextEntry()) != null)
        {
            if (entry.getName().toLowerCase().endsWith(".kml") && !entry.isDirectory())
            {
                /* We found the root KML file. From Google's documentation:
                    *
                    * When Google Earth opens a KMZ file, it scans the file,
                    * looking for the first .kml file in this list. It ignores
                    * all subsequent .kml files, if any, in the archive. If the
                    * archive contains multiple .kml files, you cannot be sure
                    * which one will be found first, so you need to include
                    * only one.) */
                int count;
                byte[] data = new byte[BUFFER_SIZE];
                bos = new ByteArrayOutputStream();

                while ((count = zipStream.read(data, 0, BUFFER_SIZE)) != -1)
                {
                    bos.write(data, 0, count);
                }

                bos.flush();
                bais = new ByteArrayInputStream(bos.toByteArray());

                // we've found our file so we're done
                break;
            }
        }

        return bais;
    }
}

package io.opensphere.core.util.gdal;

import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;

import javax.imageio.ImageReader;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;

import org.apache.log4j.Logger;

/** GDAL image reader SPI. */
public class GDALImageReaderSpi extends ImageReaderSpi
{
    /** The SPI vendor. */
    private static final String VENDOR = "OpenSphere Desktop";

    /** The SPI version. */
    private static final String VERSION = "1.0.0";

    /** The supported format names. */
    private static final String[] NAMES = { "TIFF", "GeoTIFF", "NITF" };

    /** The supported format suffixes. */
    private static final String[] SUFFIXES = { "tif", "tiff", "gtif", "geotiff", "geotif", "ntf", "nitf" };

    /** The supported MIME types. */
    private static final String[] MIME_TYPES = { "image/tiff", "image/geotiff", "image/nitf" };

    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(GDALImageReaderSpi.class);

    /** NITF header. */
    private static final byte[] NITF_HEADER = new byte[] { 'N', 'I', 'T', 'F' };

    /** Big-endian TIFF header. */
    private static final byte[] TIFF_HEADER_BIG = new byte[] { 'M', 'M', 0, 42 };

    /** Little-endian TIFF header. */
    private static final byte[] TIFF_HEADER_LITTLE = new byte[] { 'I', 'I', 42, 0 };

    /** Constructor. */
    public GDALImageReaderSpi()
    {
        super(VENDOR, VERSION, NAMES, SUFFIXES, MIME_TYPES, GDALImageReader.class.getName(),
                new Class<?>[] { ImageInputStream.class }, null, false, null, null, null, null, false, null, null, null, null);
    }

    @Override
    public boolean canDecodeInput(Object source) throws IOException
    {
        if (source instanceof ImageInputStream)
        {
            ImageInputStream inp = (ImageInputStream)source;
            inp.mark();
            byte[] buf = new byte[4];
            try
            {
                inp.readFully(buf);
                return (Arrays.equals(buf, TIFF_HEADER_BIG) || Arrays.equals(buf, TIFF_HEADER_LITTLE)
                        || Arrays.equals(buf, NITF_HEADER)) && GDALGenericUtilities.loadGDAL();
            }
            catch (IOException e)
            {
                if (LOGGER.isDebugEnabled())
                {
                    LOGGER.debug("Failed to read image header: " + e, e);
                }
                return false;
            }
            finally
            {
                inp.reset();
            }
        }
        else
        {
            return false;
        }
    }

    @Override
    public ImageReader createReaderInstance(Object extension) throws IOException
    {
        return new GDALImageReader(this);
    }

    @Override
    public String getDescription(Locale locale)
    {
        return "OpenSphere Desktop GDAL Image Reader";
    }
}

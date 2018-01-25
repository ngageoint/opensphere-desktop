package io.opensphere.wps.response;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.ZipInputStream;

import org.apache.log4j.Logger;

import io.opensphere.core.Toolbox;
import io.opensphere.core.common.shapefile.utils.ShapefileZipUtil;
import io.opensphere.core.common.shapefile.v2.ESRIShapefile;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.wps.source.WPSResponse;

/** The Shapefile response handler class. */
public class WPSShapefileResponseHandler extends WPSResponseHandler
{
    /** The class logger. */
    private static final Logger LOGGER = Logger.getLogger(WPSShapefileResponseHandler.class);

    /**
     * Constructor.
     *
     * @param response The wps response.
     */
    public WPSShapefileResponseHandler(WPSResponse response)
    {
        super(response);
    }

    @Override
    public Object handleResponse(Toolbox toolbox, String name)
    {
        try
        {
            ESRIShapefile shp = unzipFilesToUserHome(getResponse().getResponseStream());
            return shp;
        }
        catch (IOException e)
        {
            LOGGER.error(e.getMessage());
            return null;
        }
    }

    /**
     * Unzips files to user home.
     *
     * @param inStream The input stream.
     * @return ESRIShapefile
     * @throws IOException The input/output exception that is thrown.
     */
    private ESRIShapefile unzipFilesToUserHome(InputStream inStream) throws IOException
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String dateStr = sdf.format(new Date());

        StringBuilder outputDir = new StringBuilder(
                StringUtilities.expandProperties(System.getProperty("opensphere.path.runtime"), System.getProperties()));
        outputDir.append(File.separator);
        outputDir.append("shapefiles");
        outputDir.append(File.separator);
        outputDir.append(dateStr);

        ZipInputStream zis = new ZipInputStream(inStream);
        ESRIShapefile shp = ShapefileZipUtil.createFromZipFile(zis, outputDir.toString());
        zis.close();
        inStream.close();
        return shp;
    }
}

package io.opensphere.core.common.shapefile.v2.projection;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Collections;

import org.apache.commons.io.IOUtils;

import io.opensphere.core.common.shapefile.v2.ESRIShapefile.Mode;

/**
 * Class maintaining information from the .prj
 */
public class ProjectionPortion
{
    /** The file. */
    private final File myFile;

    private String wkt;

    public ProjectionPortion(Mode mode, String filePath) throws FileNotFoundException, IOException
    {
        myFile = new File(filePath);
        if (mode == Mode.READ)
        {
            InputStream inputStream = new FileInputStream(myFile);
            wkt = IOUtils.toString(inputStream);
            inputStream.close();
        }
    }

    public String getWkt()
    {
        return wkt;
    }

    /**
     * Write the projection to the backing file.
     *
     * @throws IOException
     */
    public void writeProjection() throws IOException
    {
        String proj = "GEOGCS[\"GCS_North_American_1983\",DATUM[\"D_North_American_1983\",SPHEROID[\"GRS_1980\",6378137,298.257222101]],PRIMEM[\"Greenwich\",0],UNIT[\"Degree\",0.017453292519943295]]";
        Files.write(myFile.toPath(), Collections.singleton(proj));
    }
}

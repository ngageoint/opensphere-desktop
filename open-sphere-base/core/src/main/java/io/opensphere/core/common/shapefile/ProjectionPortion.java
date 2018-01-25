package io.opensphere.core.common.shapefile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;

/**
 * Class maintaining information from the .prj
 */
public class ProjectionPortion
{

    private String wkt;

    public ProjectionPortion(ESRIShapefile.Mode mode, String filePath) throws FileNotFoundException, IOException
    {
        File file = new File(filePath);
        if (mode == ESRIShapefile.Mode.READ)
        {
            InputStream inputStream = new FileInputStream(file);
            wkt = IOUtils.toString(inputStream);
            inputStream.close();
        }
    }

    public String getWkt()
    {
        return wkt;
    }

}
/**
 *
 */
package io.opensphere.core.common.shapefile;

import java.io.FileNotFoundException;
import java.util.List;

import io.opensphere.core.common.shapefile.utils.DBFColumnInfo;
import io.opensphere.core.common.shapefile.utils.ShapefileRecord;

/**
 * Hopefully someday a dual purpose program for testing our parsing code and for
 * determining the contents of a shapefile.
 *
 * Right now, its a piece of crap.
 */
public class ShapefileDumper
{

    /**
     * @param args
     */
    public static void main(String[] args)
    {
        try
        {
            ESRIShapefile shapeFile = null;
            if (args.length == 1)
            {
                shapeFile = new ESRIShapefile(ESRIShapefile.Mode.READ, args[0]);
            }
            else
            {
                System.exit(1);
            }

            shapeFile.setMetadataMode(ESRIShapefile.MetadataFormat.ACTUAL);
            List<DBFColumnInfo> fields = shapeFile.getMetadataHeader();

            int i = 0;
            for (ShapefileRecord row : shapeFile)
            {
                System.out.println("Record: " + (++i) + " " + "(Type: " + row.shape.getShapeType() + ")");
                System.out.println("Shape: " + row.shape.toString());
                for (int j = 0; j < fields.size(); j++)
                {
                    System.out.println(
                            fields.get(j).fieldName + ": (" + fields.get(j).getType().toString() + ") " + row.metadata[j]);
                }
            }
        }
        catch (FileNotFoundException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

}

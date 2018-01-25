package io.opensphere.myplaces.export;

import java.io.File;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;

/** Tests for {@link MyPlacesShapeFileExporter}. */
public class MyPlacesShapeFileExporterTest
{
    /**
     * Test for {@link MyPlacesShapeFileExporter#getExportFiles(File)} with a
     * file that has no extension.
     */
    @Test
    public void testGetExportFiles1()
    {
        String path1 = "Path/To A/File";
        Collection<? extends File> actual = new MyPlacesShapeFileExporter().getExportFiles(new File(path1));
        Assert.assertEquals(4, actual.size());
        for (String extension : new String[] { ".shp", ".prj", ".shx", ".dbf" })
        {
            boolean found = false;
            for (File file : actual)
            {
                if (file.getAbsolutePath().endsWith(extension))
                {
                    found = true;
                    break;
                }
            }
            Assert.assertTrue("Extension " + extension + " was not found.", found);
        }
    }

    /**
     * Test for {@link MyPlacesShapeFileExporter#getExportFiles(File)} with a
     * file that has an extension.
     */
    @Test
    public void testGetExportFiles2()
    {
        String path1 = "Path/To A/File.SHP";
        Collection<? extends File> actual = new MyPlacesShapeFileExporter().getExportFiles(new File(path1));
        Assert.assertEquals(4, actual.size());
        for (String extension : new String[] { ".SHP", ".prj", ".shx", ".dbf" })
        {
            boolean found = false;
            for (File file : actual)
            {
                if (file.getAbsolutePath().endsWith(extension))
                {
                    found = true;
                    break;
                }
            }
            Assert.assertTrue("Extension " + extension + " was not found.", found);
        }
    }
}

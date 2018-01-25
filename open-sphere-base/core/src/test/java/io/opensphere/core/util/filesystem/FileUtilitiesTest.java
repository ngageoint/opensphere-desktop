package io.opensphere.core.util.filesystem;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;

/** Tests for {@link FileUtilities}. */
public class FileUtilitiesTest
{
    /** A test string. */
    private static final String XYZ = "xyz";

    /** Test for {@link FileUtilities#ensureSuffix(File, String)}. */
    @Test
    public void testEnsureSuffix()
    {
        File fullName = new File("C:\\path.dir\\last." + XYZ);
        Assert.assertEquals(fullName, FileUtilities.ensureSuffix(fullName, XYZ));
        Assert.assertEquals(fullName, FileUtilities.ensureSuffix(new File("C:\\path.dir\\last"), XYZ));
    }

    /** Test for {@link FileUtilities#getBasename(java.io.File)}. */
    @Test
    public void testGetBasenameFile()
    {
        Assert.assertEquals(XYZ, FileUtilities.getBasename(new File(XYZ)));
        Assert.assertEquals("", FileUtilities.getBasename(new File("." + XYZ)));
        Assert.assertEquals("a", FileUtilities.getBasename(new File("a." + XYZ)));
        Assert.assertEquals("last",
                FileUtilities.getBasename(new File(File.separator + "path.dir" + File.separator + "last." + XYZ)));
    }

    /** Test for {@link FileUtilities#getSuffix(java.io.File)}. */
    @Test
    public void testGetSuffixFile()
    {
        Assert.assertNull(FileUtilities.getSuffix(new File(XYZ)));
        Assert.assertNull(FileUtilities.getSuffix(new File("." + XYZ)));
        Assert.assertEquals(XYZ, FileUtilities.getSuffix(new File("a." + XYZ)));
        Assert.assertEquals(XYZ, FileUtilities.getSuffix(new File("C:\\path.dir\\last." + XYZ)));
    }

    /** Test for {@link FileUtilities#getSuffix(String)}. */
    @Test
    public void testGetSuffixString()
    {
        Assert.assertNull(FileUtilities.getSuffix(XYZ));
        Assert.assertNull(FileUtilities.getSuffix("." + XYZ));
        Assert.assertEquals(XYZ, FileUtilities.getSuffix("a." + XYZ));
        Assert.assertEquals(XYZ, FileUtilities.getSuffix("C:\\path.dir\\last." + XYZ));
    }
}

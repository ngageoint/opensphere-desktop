package io.opensphere.core.util.lang;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;

/** Test for {@link JarUtilities}. */
public class JarUtilitiesTest
{
    /**
     * Test for {@link JarUtilities#listFiles(ClassLoader, String, int)}.
     *
     * @throws IOException If there is a failure.
     */
    @Test
    public void testListFiles() throws IOException
    {
        Collection<? extends URL> files = JarUtilities.listFiles(JarUtilitiesTest.class.getClassLoader(),
                "io/opensphere/core/util/lang", 1);
        Assert.assertFalse(files.isEmpty());
        for (URL url : files)
        {
            if (url.getFile().contains(JarUtilitiesTest.class.getSimpleName()))
            {
                Assert.assertEquals("application/java-vm", url.openConnection().getContentType());
                return;
            }
        }
        Assert.fail("Did not find JarUtilitiesTest class.");
    }
}

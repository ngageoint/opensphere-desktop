package io.opensphere.analysis.export.view;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import io.opensphere.core.util.MimeType;

/**
 * Unit test for the {@link MimeTypeFileFilter}.
 */
public class MimeTypeFileFilterTestFunctional
{
    /**
     * Tests the accept file function, make sure directories pass, and files
     * with the extension specified in the {@link MimeType} pass.
     *
     * @throws IOException Bad IO.
     */
    @Test
    public void testAcceptFile() throws IOException
    {
        MimeTypeFileFilter fileFilter = new MimeTypeFileFilter(MimeType.KML);

        File file = File.createTempFile("test", "." + MimeType.KML.getFileExtensions()[0]);
        file.deleteOnExit();

        File csvFile = File.createTempFile("test", "." + MimeType.CSV.getFileExtensions()[0]);
        csvFile.deleteOnExit();

        assertTrue(fileFilter.accept(file.getParentFile()));
        assertTrue(fileFilter.accept(file));
        assertFalse(fileFilter.accept(csvFile));
    }

    /**
     * Verifies it gets the description from the {@link MimeType}.
     */
    @Test
    public void testGetDescription()
    {
        MimeTypeFileFilter fileFilter = new MimeTypeFileFilter(MimeType.KML);

        assertEquals(fileFilter.getDescription(), MimeType.KML.getDescription());
    }
}

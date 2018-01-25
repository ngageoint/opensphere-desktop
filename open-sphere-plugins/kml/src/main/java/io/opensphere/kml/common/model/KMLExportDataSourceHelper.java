package io.opensphere.kml.common.model;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;

import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;

import io.opensphere.core.common.util.zip.Zip;
import io.opensphere.core.common.util.zip.Zip.ZipInputAdapter;
import io.opensphere.core.util.XMLUtilities;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.kml.common.model.KMLDataSource.Type;
import io.opensphere.mantle.datasources.IDataSource;

/**
 * Helper for exporting the data source.
 */
public final class KMLExportDataSourceHelper
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(KMLExportDataSourceHelper.class);

    /**
     * Export to file.
     *
     * @param dataSource the KML data source
     * @param selectedFile the selected file
     * @param callback the callback
     */
    public static void exportToFile(final KMLDataSource dataSource, File selectedFile, final ActionListener callback)
    {
        boolean success = true;

        // Create a copy of the data source suitable for export
        final KMLDataSource copySource = dataSource.createExportDataSource();

        // Write the data source config to the file system
        if (copySource.getType() == Type.FILE)
        {
            try
            {
                final File file = new File(copySource.getPath());

                // Update the path to be just the file name so that the path
                // will be correct when it imports
                copySource.setPath(file.getName());

                final ByteArrayOutputStream cfgXMLBAOS = new ByteArrayOutputStream();
                XMLUtilities.writeXMLObject(copySource, cfgXMLBAOS);

                final List<ZipInputAdapter> inputAdapters = new ArrayList<>(2);
                inputAdapters.add(
                        new Zip.ZipByteArrayInputAdapter("source.opensphere3d", null, cfgXMLBAOS.toByteArray(), ZipEntry.DEFLATED));
                inputAdapters.add(new Zip.ZipFileInputAdapter("data", file, ZipEntry.DEFLATED));

                Zip.zipfiles(selectedFile, inputAdapters, null, false);
            }
            catch (final JAXBException e)
            {
                LOGGER.error(e.getMessage(), e);
                success = false;
            }
            catch (final IOException e)
            {
                LOGGER.error(e.getMessage(), e);
                success = false;
                if (!selectedFile.delete())
                {
                    LOGGER.warn("Failed to delete file: " + selectedFile);
                }
            }
        }
        else
        {
            try
            {
                XMLUtilities.writeXMLObject(copySource, selectedFile);
            }
            catch (final JAXBException e)
            {
                LOGGER.error(e.getMessage(), e);
                success = false;
            }
        }

        // Notify the caller
        final String result = success ? IDataSource.EXPORT_SUCCESS : IDataSource.EXPORT_FAILED;
        EventQueueUtilities.runOnEDT(() -> callback.actionPerformed(new ActionEvent(dataSource, 0, result)));
    }

    /**
     * Private constructor.
     */
    private KMLExportDataSourceHelper()
    {
    }
}

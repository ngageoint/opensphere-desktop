package io.opensphere.core.event;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.TransferHandler.DropLocation;
import javax.swing.TransferHandler.TransferSupport;

import org.apache.log4j.Logger;

import edu.umd.cs.findbugs.annotations.Nullable;

/**
 * Event for importing data via drag-n-drop to the main frame.
 */
public class ImportDataEvent extends AbstractSingleStateEvent
{
    /** List of data flavors. */
    private static final List<DataFlavor> FLAVOR_LIST;

    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(ImportDataEvent.class);

    /** The drop location. */
    @Nullable
    private DropLocation myDropLocation;

    /** The transfer data. */
    private Object myTransferData;

    static
    {
        FLAVOR_LIST = new ArrayList<>(2);
        FLAVOR_LIST.add(DataFlavor.javaFileListFlavor);
        FLAVOR_LIST.add(new DataFlavor("text/uri-list;class=java.lang.String", "Text URI List"));
    }

    /**
     * Whether this TransferSupport is supported.
     *
     * @param support TransferSupport
     * @return Whether it is supported
     */
    public static boolean canImport(TransferSupport support)
    {
        for (DataFlavor flavor : FLAVOR_LIST)
        {
            if (support.isDataFlavorSupported(flavor))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Constructor.
     */
    public ImportDataEvent()
    {
    }

    /**
     * Constructor.
     *
     * @param support The transfer support
     */
    public ImportDataEvent(TransferSupport support)
    {
        for (DataFlavor flavor : FLAVOR_LIST)
        {
            if (support.isDataFlavorSupported(flavor))
            {
                try
                {
                    myDropLocation = support.getDropLocation();
                    myTransferData = support.getTransferable().getTransferData(flavor);
                    break;
                }
                catch (UnsupportedFlavorException e)
                {
                    LOGGER.error("Unsupported data flavor: " + e.getMessage(), e);
                }
                catch (IOException e)
                {
                    LOGGER.error(e.getMessage(), e);
                }
            }
        }

        if (myTransferData == null)
        {
            LOGGER.warn("No data flavors supported for TransferSupport");
        }
    }

    @Override
    public String getDescription()
    {
        return "Import Data";
    }

    /**
     * Get the drop location.
     *
     * @return The drop location.
     */
    @Nullable
    public DropLocation getDropLocation()
    {
        return myDropLocation;
    }

    /**
     * Gets a list of files from the transfer data.
     *
     * @return List of Files
     */
    public List<File> getFiles()
    {
        List<File> files = new ArrayList<>();

        // Handle List format
        if (myTransferData instanceof List)
        {
            List<?> listData = (List<?>)myTransferData;

            // Create the list of files
            for (Object elem : listData)
            {
                if (elem instanceof File)
                {
                    files.add((File)elem);
                }
            }
        }
        // Handle String format
        else if (myTransferData instanceof String)
        {
            String stringData = (String)myTransferData;

            // Create the list of files
            String[] fileURIStrings = stringData.split("\\s+");
            for (String fileURIString : fileURIStrings)
            {
                try
                {
                    URI fileURI = new URI(fileURIString);
                    files.add(new File(fileURI));
                }
                catch (URISyntaxException e)
                {
                    LOGGER.warn(e.getMessage(), e);
                }
            }
        }

        return files;
    }

    /**
     * Getter for transferData.
     *
     * @return the transferData
     */
    public Object getTransferData()
    {
        return myTransferData;
    }
}

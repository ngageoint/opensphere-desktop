package io.opensphere.myplaces.importer;

import java.awt.Component;
import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.TransferHandler.DropLocation;

import de.micromata.opengis.kml.v_2_2_0.Folder;
import io.opensphere.core.Toolbox;
import io.opensphere.core.dialog.alertviewer.event.UserMessageEvent;
import io.opensphere.core.importer.FileOrURLImporter;
import io.opensphere.core.importer.ImportCallback;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.mantle.mp.MapAnnotationPointRegistry;
import io.opensphere.mantle.util.MantleToolboxUtils;
import io.opensphere.myplaces.models.MyPlacesDataGroupInfo;
import io.opensphere.myplaces.models.MyPlacesModel;

/**
 * Abstract MyPlaces FileOrURLImporter.
 */
public abstract class AbstractMyPlacesImporter implements FileOrURLImporter
{
    /** The title key. */
    protected static final int TITLE_KEY = 0;

    /** The description key. */
    protected static final int DESC_KEY = 1;

    /** The latitude key. */
    protected static final int LAT_KEY = 2;

    /** The longitude key. */
    protected static final int LON_KEY = 3;

    /** The longitude key. */
    protected static final int ALTITUDE_KEY = 4;

    /** The geometry key. */
    protected static final int GEOM_KEY = 5;

    /** The toolbox. */
    private final Toolbox myToolbox;

    /** The my places model. */
    private final MyPlacesModel myModel;

    /** The map point registry. */
    private final MapAnnotationPointRegistry myPointRegistry;

    /**
     * Gets the index of each specific header item.
     *
     * @param headers the headers
     * @return the header indices
     */
    protected static int[] getHeaderIndices(List<String> headers)
    {
        int[] indices = new int[6];
        Arrays.fill(indices, -1);
        for (int index = 0, n = headers.size(); index < n; index++)
        {
            String header = headers.get(index).toLowerCase();
            if ("title".equals(header) || "name".equals(header))
            {
                indices[TITLE_KEY] = index;
            }
            else if (header.startsWith("desc"))
            {
                indices[DESC_KEY] = index;
            }
            else if (header.startsWith("lat"))
            {
                indices[LAT_KEY] = index;
            }
            else if (header.startsWith("lon"))
            {
                indices[LON_KEY] = index;
            }
            else if (header.startsWith("altitude"))
            {
                indices[ALTITUDE_KEY] = index;
            }
            else if (header.startsWith("geom"))
            {
                indices[GEOM_KEY] = index;
            }
        }
        return indices;
    }

    /**
     * Constructor.
     *
     * @param toolbox the toolbox
     * @param model the model
     */
    public AbstractMyPlacesImporter(Toolbox toolbox, MyPlacesModel model)
    {
        myToolbox = toolbox;
        myModel = model;
        myPointRegistry = MantleToolboxUtils.getMantleToolbox(toolbox).getMapAnnotationPointRegistry();
    }

    @Override
    public boolean canImport(File aFile, DropLocation dropLocation)
    {
        boolean canImport = false;
        if (aFile != null && aFile.canRead())
        {
            String fileName = aFile.getAbsolutePath().toLowerCase();
            for (String ext : getSupportedFileExtensions())
            {
                if (fileName.endsWith(ext.toLowerCase()))
                {
                    canImport = true;
                    break;
                }
            }
        }
        return canImport;
    }

    @Override
    public boolean canImport(URL aURL, DropLocation dropLocation)
    {
        return false;
    }

    @Override
    public String getDescription()
    {
        return null;
    }

    @Override
    public JComponent getFileChooserAccessory()
    {
        return null;
    }

    @Override
    public String getImportMultiFileMenuItemName()
    {
        return null;
    }

    @Override
    public String getImportSingleFileMenuItemName()
    {
        return null;
    }

    @Override
    public String getImportURLFileMenuItemName()
    {
        return null;
    }

    @Override
    public String getName()
    {
        return null;
    }

    @Override
    public void importFiles(List<File> fileList, ImportCallback callback)
    {
    }

    @Override
    public boolean importsFileGroups()
    {
        return false;
    }

    @Override
    public boolean importsFiles()
    {
        return false;
    }

    @Override
    public boolean importsURLs()
    {
        return false;
    }

    @Override
    public void importURL(URL aURL, Component component)
    {
    }

    @Override
    public void importURL(URL aURL, ImportCallback callback)
    {
    }

    /**
     * Adds the given folder to the my places and mantle models, or notifies the
     * user if there's nothing to add.
     *
     * @param folder the folder
     * @param file the file
     */
    protected void addFolderOrFail(Folder folder, File file)
    {
        if (folder != null && !folder.getFeature().isEmpty())
        {
            myModel.getDataGroups().getKmlFolder().addToFeature(folder);
            addFolderToMantle(folder);
        }
        else
        {
            notifyUser(file);
        }
    }

    /**
     * Gets the point registry.
     *
     * @return the point registry
     */
    protected MapAnnotationPointRegistry getPointRegistry()
    {
        return myPointRegistry;
    }

    /**
     * Gets the toolbox.
     *
     * @return the toolbox
     */
    protected Toolbox getToolbox()
    {
        return myToolbox;
    }

    /**
     * Adds the folder to mantle.
     *
     * @param folder the folder
     */
    private void addFolderToMantle(Folder folder)
    {
        MyPlacesDataGroupInfo dataGroup = new KmlTranslator(myToolbox).parseFolder(folder, false);
        myModel.getDataGroups().addChild(dataGroup, this);
        dataGroup.groupStream().forEach(g -> g.activationProperty().setActive(true));
    }

    /**
     * Notify the user the was a problem importing the file.
     *
     * @param file the file
     */
    private void notifyUser(File file)
    {
        String message = StringUtilities.concat("Unable to import ", file.getName(),
                ". The file is not in the My Places format or contains no data.");
        UserMessageEvent.warn(myToolbox.getEventManager(), message, false, this, null, true);
    }
}

package io.opensphere.myplaces.importer;

import java.io.File;
import java.util.List;

import io.opensphere.core.Toolbox;
import io.opensphere.core.importer.FileOrURLImporter;
import io.opensphere.core.importer.ImportCallback;
import io.opensphere.core.util.collections.New;
import io.opensphere.myplaces.models.MyPlacesModel;

/**
 * Master importer for My Places.
 */
public class MyPlacesMasterImporter extends AbstractMyPlacesImporter
{
    /** The supported file extensions. */
    private final List<String> myFileExtensions;

    /** The importers. */
    private final FileOrURLImporter[] myImporters;

    /**
     * Constructor.
     *
     * @param toolbox the toolbox
     * @param model the model
     */
    public MyPlacesMasterImporter(Toolbox toolbox, MyPlacesModel model)
    {
        super(toolbox, model);
        myImporters = new FileOrURLImporter[] { new MyPlacesKmlImporter(toolbox, model), new MyPlacesCsvImporter(toolbox, model),
            new MyPlacesShapeFileImporter(toolbox, model), };
        myFileExtensions = New.list();
        for (FileOrURLImporter importer : myImporters)
        {
            myFileExtensions.addAll(importer.getSupportedFileExtensions());
        }
    }

    @Override
    public String getDescription()
    {
        return "Importer for My Places files.";
    }

    @Override
    public String getImportSingleFileMenuItemName()
    {
        return "Import My Places File";
    }

    @Override
    public String getName()
    {
        return "My Places";
    }

    @Override
    public int getPrecedence()
    {
        return 1400;
    }

    @Override
    public List<String> getSupportedFileExtensions()
    {
        return myFileExtensions;
    }

    @Override
    public void importFile(File aFile, ImportCallback callback)
    {
        for (FileOrURLImporter importer : myImporters)
        {
            if (importer.canImport(aFile, null))
            {
                importer.importFile(aFile, callback);
                break;
            }
        }
    }

    @Override
    public boolean importsFiles()
    {
        return true;
    }
}

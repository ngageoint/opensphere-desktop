package io.opensphere.shapefile;

import java.awt.Component;
import java.awt.Dimension;
import java.io.File;
import java.util.HashSet;
import java.util.Set;

import io.opensphere.core.importer.ImportCallbackAdapter;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.impl.DefaultDataGroupInfoAssistant;
import io.opensphere.mantle.datasources.IDataSource;
import io.opensphere.shapefile.config.v1.ShapeFileSource;

/**
 * The Class ShapeFileDataGroupInfoAssistant.
 */
public class ShapeFileDataGroupInfoAssistant extends DefaultDataGroupInfoAssistant
{
    /** The controller. */
    private final ShapeFileDataSourceController myController;

    /**
     * Instantiates a new default data group info assistant.
     *
     * @param controller the controller
     */
    public ShapeFileDataGroupInfoAssistant(ShapeFileDataSourceController controller)
    {
        super();
        myController = controller;
    }

    @Override
    public boolean canDeleteGroup(DataGroupInfo dgi)
    {
        return true;
    }

    @Override
    public boolean canReImport(DataGroupInfo dgi)
    {
        return true;
    }

    @Override
    public void deleteGroup(DataGroupInfo dgi, Object source)
    {
        if (dgi.hasMembers(false))
        {
            DataTypeInfo dti = dgi.getMembers(false).iterator().next();
            if (dti instanceof ShapeFileDataTypeInfo)
            {
                ShapeFileDataTypeInfo cdti = (ShapeFileDataTypeInfo)dti;
                myController.removeSource(cdti.getFileSource(), true,
                        myController.getToolbox().getUIRegistry().getMainFrameProvider().get());
            }
        }
    }

    @Override
    public Component getSettingsUIComponent(Dimension preferredSize, DataGroupInfo dataGroup)
    {
        if (dataGroup.hasMembers(false))
        {
            DataTypeInfo dti = dataGroup.getMembers(false).iterator().next();
            if (dti instanceof ShapeFileDataTypeInfo)
            {
                ShapeFileDataTypeInfo cdti = (ShapeFileDataTypeInfo)dti;
                ShapeFileDataGroupSettingsPanel panel = new ShapeFileDataGroupSettingsPanel(myController, cdti.getFileSource(),
                        dataGroup);
                panel.setPreferredSize(preferredSize);
                panel.setMaximumSize(preferredSize);
                panel.setMinimumSize(preferredSize);
                panel.setSize(preferredSize);
                return panel;
            }
        }
        return null;
    }

    @Override
    public void reImport(DataGroupInfo dgi, Object source)
    {
        if (dgi.hasMembers(false))
        {
            DataTypeInfo dti = dgi.getMembers(false).iterator().next();
            if (dti instanceof ShapeFileDataTypeInfo)
            {
                final ShapeFileDataTypeInfo cdti = (ShapeFileDataTypeInfo)dti;
                final ShapeFileSource shapeFileSource = cdti.getFileSource();

                Set<String> namesInUse = new HashSet<>();
                for (IDataSource src : myController.getSourceList())
                {
                    namesInUse.add(src.getName());
                }
                namesInUse.remove(shapeFileSource.getName());

                // Save the source to be re-added on failure. We really only
                // want the original source to be removed when the replacement
                // with the new one succeeds.
                final ShapeFileSource backupSource = new ShapeFileSource(shapeFileSource);
                myController.removeSource(shapeFileSource, true,
                        myController.getToolbox().getUIRegistry().getMainFrameProvider().get());

                // It doesn't matter whether this is a file or a URL, I just
                // want to know whether it failed.
                ImportCallbackAdapter callback = new ImportCallbackAdapter()
                {
                    @Override
                    public void fileImportComplete(boolean success, File file, Object responseObject)
                    {
                        if (!success)
                        {
                            myController.addSource(backupSource);
                        }
                    }
                };

                myController.getFileImporter().importFileSource(shapeFileSource, namesInUse, callback, null);
            }
        }
    }
}

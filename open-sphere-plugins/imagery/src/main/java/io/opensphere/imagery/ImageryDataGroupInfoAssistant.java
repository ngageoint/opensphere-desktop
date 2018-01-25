package io.opensphere.imagery;

import java.awt.Component;
import java.awt.Dimension;
import java.io.File;

import io.opensphere.core.importer.ImportCallbackAdapter;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.impl.DefaultDataGroupInfoAssistant;

/**
 * The Class ShapeFileDataGroupInfoAssistant.
 */
public class ImageryDataGroupInfoAssistant extends DefaultDataGroupInfoAssistant
{
    /** The controller. */
    private final ImageryFileSourceController myController;

    /**
     * Instantiates a new default data group info assistant.
     *
     * @param controller the controller
     */
    public ImageryDataGroupInfoAssistant(ImageryFileSourceController controller)
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
        if (dgi instanceof ImageryDataGroupInfo)
        {
            ImageryDataGroupInfo idgi = (ImageryDataGroupInfo)dgi;
            myController.removeSource(idgi.getImagerySourceGroup(), true,
                    myController.getToolbox().getUIRegistry().getMainFrameProvider().get());
        }
    }

    @Override
    public Component getSettingsUIComponent(Dimension preferredSize, DataGroupInfo dataGroup)
    {
        if (dataGroup instanceof ImageryDataGroupInfo)
        {
            ImageryDataGroupInfo idgi = (ImageryDataGroupInfo)dataGroup;
            ImagerySettingsPanel panel = new ImagerySettingsPanel(myController, idgi);
            panel.setPreferredSize(preferredSize);
            panel.setMaximumSize(preferredSize);
            panel.setMinimumSize(preferredSize);
            panel.setSize(preferredSize);
            return panel;
        }
        return null;
    }

    @Override
    public void reImport(DataGroupInfo dgi, Object source)
    {
        if (dgi instanceof ImageryDataGroupInfo)
        {
            ImageryDataGroupInfo idgi = (ImageryDataGroupInfo)dgi;
            final ImagerySourceGroup sourceGroup = idgi.getImagerySourceGroup();

            final ImagerySourceGroup backupSource = new ImagerySourceGroup(sourceGroup);
            myController.removeSource(sourceGroup, true, myController.getToolbox().getUIRegistry().getMainFrameProvider().get());
            myController.getFileImporter().reimportGroup(sourceGroup, new ImportCallbackAdapter()
            {
                @Override
                public void fileImportComplete(boolean success, File aFile, Object responseObject)
                {
                    if (!success)
                    {
                        myController.addSource(backupSource);
                    }
                }
            });
        }
    }
}

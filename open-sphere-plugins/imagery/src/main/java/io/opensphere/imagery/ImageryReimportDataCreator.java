package io.opensphere.imagery;

import java.util.List;

import javax.swing.JDialog;

import io.opensphere.core.importer.ImportCallback;
import io.opensphere.mantle.data.DataGroupImportCallbackResponse;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.datasources.IDataSource;
import io.opensphere.mantle.datasources.IDataSourceCreator;

/**
 * Creates an imagery data source on reimport.
 */
public class ImageryReimportDataCreator implements IDataSourceCreator
{
    /**
     * The wizard dialog.
     */
    private final JDialog myWizardDialog;

    /** The controller. */
    private final ImageryFileSourceController myController;

    /**
     * The import callback.
     */
    private final ImportCallback myCallback;

    /**
     * The backup group.
     */
    private final ImagerySourceGroup myBackupGroup;

    /**
     * Constructor.
     *
     * @param wizardDialog The wizard dialog.
     * @param controller The controller.
     * @param callback The import callback.
     * @param backupGroup The backup group.
     */
    public ImageryReimportDataCreator(JDialog wizardDialog, ImageryFileSourceController controller, ImportCallback callback, ImagerySourceGroup backupGroup)
    {
        myWizardDialog = wizardDialog;
        myController = controller;
        myCallback = callback;
        myBackupGroup = backupGroup;
    }

    @Override
    public void sourceCreated(boolean successful, IDataSource source)
    {
        try
        {
            myController.addSource(successful ? source : myBackupGroup);
            if (myCallback != null)
            {
                final ImagerySourceGroup resultGroup = successful ? (ImagerySourceGroup)source : myBackupGroup;
                DataGroupImportCallbackResponse responseObject = new DataGroupImportCallbackResponse()
                {
                    @Override
                    public DataGroupInfo getNewOrChangedGroup()
                    {
                        return resultGroup.getDataGroupInfo();
                    }
                };
                myCallback.fileGroupImportComplete(successful, resultGroup.getFileList(), responseObject);
            }
        }
        finally
        {
            myWizardDialog.setVisible(false);
        }
    }

    @Override
    public void sourcesCreated(boolean successful, List<IDataSource> sources)
    {
        // Do nothing we won't get here.
    }
}

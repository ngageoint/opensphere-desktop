package io.opensphere.imagery;

import java.io.File;
import java.util.List;

import javax.swing.JDialog;

import io.opensphere.core.importer.ImportCallback;
import io.opensphere.mantle.data.DataGroupImportCallbackResponse;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.datasources.IDataSource;
import io.opensphere.mantle.datasources.IDataSourceCreator;

/**
 * Creates an imagery data source on import.
 */
public class ImageryImportDataCreator implements IDataSourceCreator
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
     * The list of files to import.
     */
    private final List<File> myFileList;

    /**
     * Constructor.
     *
     * @param wizardDialog The wizard dialog.
     * @param controller The controller.
     * @param callback The import callback.
     * @param fileList The list of files to import.
     */
    public ImageryImportDataCreator(JDialog wizardDialog, ImageryFileSourceController controller, ImportCallback callback,
            final List<File> fileList)
    {
        myWizardDialog = wizardDialog;
        myController = controller;
        myCallback = callback;
        myFileList = fileList;
    }

    @Override
    public void sourceCreated(boolean successful, IDataSource source)
    {
        try
        {
            if (successful)
            {
                myController.addSource(source);
            }
            if (myCallback != null)
            {
                myCallback.fileGroupImportComplete(successful, myFileList, new DataGroupImportCallbackResponse()
                {
                    @Override
                    public DataGroupInfo getNewOrChangedGroup()
                    {
                        return ((ImagerySourceGroup)source).getDataGroupInfo();
                    }
                });
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

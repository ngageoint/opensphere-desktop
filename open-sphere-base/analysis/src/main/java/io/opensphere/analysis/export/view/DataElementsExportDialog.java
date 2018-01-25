package io.opensphere.analysis.export.view;

import java.awt.Dimension;

import javax.swing.filechooser.FileFilter;

import io.opensphere.analysis.export.model.ExportOptionsModel;
import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.core.util.filesystem.MnemonicFileChooser;

/**
 * The export dialog used to get the user's desired export options and export
 * file.
 */
public class DataElementsExportDialog extends MnemonicFileChooser
{
    /**
     * Serialization id.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The options model, contains the user's inputs.
     */
    private final ExportOptionsModel myModel;

    /**
     * Keeps the options model and the options UI in sync.
     */
    private final ExportOptionsViewBinder myOptionsBinder;

    /**
     * The UI containing the options.
     */
    private ExportOptionsUI myOptionsUI;

    /**
     * Constructs a new dialog.
     *
     * @param prefsRegistry The preferences registry.
     * @param fileFilter The file filter to use or null if all files is ok.
     * @param model The options model.
     */
    public DataElementsExportDialog(PreferencesRegistry prefsRegistry, FileFilter fileFilter, ExportOptionsModel model)
    {
        super(prefsRegistry, null);
        myModel = model;
        initializeComponents(fileFilter);
        myOptionsBinder = new ExportOptionsViewBinder(myOptionsUI, myModel);
    }

    /**
     * Stops listening for UI and model changes.
     */
    public void close()
    {
        myOptionsBinder.close();
    }

    /**
     * Gets the export options view.
     *
     * @return The view containing different export options the user can choose
     *         from.
     */
    protected ExportOptionsView getExportOptions()
    {
        return myOptionsUI;
    }

    /**
     * Initializes all of the ui components.
     *
     * @param fileFilter The file filter to use.
     */
    private void initializeComponents(FileFilter fileFilter)
    {
        Dimension d = new Dimension(740, 400);
        setPreferredSize(d);

        if (fileFilter != null)
        {
            addChoosableFileFilter(fileFilter);
            super.setFileFilter(fileFilter);
        }

        myOptionsUI = new ExportOptionsUI();
        setAccessory(myOptionsUI);
    }
}

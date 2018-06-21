package io.opensphere.analysis.export;

import java.awt.Component;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.io.File;

import javafx.application.Platform;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;

import io.opensphere.analysis.export.controller.ExportCompleteListener;
import io.opensphere.analysis.export.controller.ExportExecutor;
import io.opensphere.analysis.export.model.ExportOptionsModel;
import io.opensphere.analysis.export.view.DataElementsExportDialog;
import io.opensphere.analysis.export.view.MimeTypeFileFilter;
import io.opensphere.analysis.listtool.model.ListToolTableModel;
import io.opensphere.analysis.table.model.MetaColumnsTableModel;
import io.opensphere.core.control.ui.UIRegistry;
import io.opensphere.core.event.EventManager;
import io.opensphere.core.export.Exporter;
import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.core.util.javafx.WebPanel;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.core.util.swing.AutohideMessageDialog;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.core.util.taskactivity.TaskActivity;
import io.opensphere.mantle.data.cache.DataElementCache;
import io.opensphere.mantle.data.element.DataElement;

/**
 * Given an exporter and a {@link MetaColumnsTableModel}, this class will ask
 * the user a file to export the data to as well as some other various export
 * options. Once the user initiates export this class will go ahead and pass in
 * the correct data for the {@link Exporter} to export.
 */
public class DataElementExporter implements ExportCompleteListener
{
    /**
     * Used to get {@link DataElement} data.
     */
    private final DataElementCache myCache;

    /**
     * Used to display toast messages if the export failed.
     */
    private final EventManager myEventManager;

    /**
     * Used to get export dialog preferences.
     */
    private final PreferencesRegistry myPrefsRegistry;

    /**
     * Used to show export {@link TaskActivity}.
     */
    private final UIRegistry myUIRegistry;

    /**
     * Constructs a new {@link DataElementExporter}.
     *
     * @param prefsRegistry Used to get export dialog preferences.
     * @param cache Used to get {@link DataElement} data.
     * @param uiRegistry Used to show export {@link TaskActivity}.
     * @param eventManager Used to display toast messages if the export failed.
     */
    public DataElementExporter(PreferencesRegistry prefsRegistry, DataElementCache cache, UIRegistry uiRegistry,
            EventManager eventManager)
    {
        myPrefsRegistry = prefsRegistry;
        myCache = cache;
        myUIRegistry = uiRegistry;
        myEventManager = eventManager;
    }

    @Override
    public boolean askUserForOverwrite(Component parent, File file)
    {
        boolean canOverwrite = false;

        int choice = JOptionPane.showConfirmDialog(SwingUtilities.getWindowAncestor(parent), "Overwrite the file: " + file + "?",
                "Export Data", JOptionPane.YES_NO_OPTION);
        canOverwrite = choice == JOptionPane.YES_OPTION;

        return canOverwrite;
    }

    /**
     * Asks the user for the export file and other various export options. If
     * user clicks save, the passed exporter will be executed in a background
     * thread.
     *
     * @param parent The parent component to show the dialog.
     * @param exporter The exporter to execute.
     * @param tableModel The table model to export.
     * @param table The table to export.
     */
    public void export(Component parent, Exporter exporter, ListToolTableModel tableModel, JTable table)
    {
        ExportOptionsModel optionsModel = new ExportOptionsModel();
        DataElementsExportDialog dialog = new DataElementsExportDialog(myPrefsRegistry,
                new MimeTypeFileFilter(exporter.getMimeType()), optionsModel);
        ExportExecutor exportExecutor = new ExportExecutor(myCache, myUIRegistry, myEventManager, myPrefsRegistry);
        exportExecutor.executeExport(dialog, parent, exporter, tableModel, table, optionsModel, this);
    }

    /**
     * Called when an export has completed.
     *
     * @param parent The parent component, used to properly place the completion
     *            dialog.
     * @param file The exported file.
     * @param count The number of records exported.
     */
    @Override
    public void exportComplete(Component parent, File file, int count)
    {
        EventQueueUtilities.runOnEDT(() -> showCompletionDialog(parent, file, count));
    }

    /**
     * Show a dialog letting the user know that the export is complete.
     *
     * @param parent The parent component, used to properly place the completion
     *            dialog.
     * @param file The destination file.
     * @param exportedCount The number of rows exported.
     */
    private void showCompletionDialog(Component parent, File file, int exportedCount)
    {
        assert EventQueue.isDispatchThread();

        WebPanel fxPanel = new WebPanel();

        AutohideMessageDialog dialog = new AutohideMessageDialog(parent, ModalityType.MODELESS);
        dialog.setTitle("Done");
        dialog.initialize(fxPanel, null, myPrefsRegistry.getPreferences(DataElementExporter.class), "hideDoneDialog");
        dialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        dialog.setPreferredSize(new Dimension(700, 150));
        dialog.setVisible(true);
        dialog.setLocationRelativeTo(parent);

        Platform.runLater(() ->
        {
            String content = StringUtilities.concat("<html><body bgcolor='#535366' style='color: white;'>", "Successfully saved ",
                    Integer.toString(exportedCount), " rows to <a style='color: white;' href='", file.toURI(), "'>", file,
                    "</a>.<p><a style='color: white;' href='", file.getParentFile().toURI(), "'>Parent directory</a>",
                    "</body></html>");

            fxPanel.loadContent(content);
        });
    }
}

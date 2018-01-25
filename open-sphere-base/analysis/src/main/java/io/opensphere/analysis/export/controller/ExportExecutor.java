package io.opensphere.analysis.export.controller;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JTable;

import org.apache.log4j.Logger;

import io.opensphere.analysis.export.DataElementExporter;
import io.opensphere.analysis.export.model.ExportOptionsModel;
import io.opensphere.analysis.export.view.DataElementsExportDialog;
import io.opensphere.analysis.table.model.MetaColumnsTableModel;
import io.opensphere.core.control.ui.UIRegistry;
import io.opensphere.core.dialog.alertviewer.event.UserMessageEvent;
import io.opensphere.core.event.EventManager;
import io.opensphere.core.export.ExportException;
import io.opensphere.core.export.Exporter;
import io.opensphere.core.preferences.ListToolPreferences;
import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.core.util.lang.ThreadUtilities;
import io.opensphere.core.util.taskactivity.TaskActivity;
import io.opensphere.mantle.data.cache.DataElementCache;
import io.opensphere.mantle.data.element.DataElement;

/**
 * Given an exporter and a {@link MetaColumnsTableModel} and a
 * {@link JFileChooser}, this class will ask the user a file to export the data
 * to as well as some other various export options. Once the user initiates
 * export this class will go ahead and pass in the correct data for the
 * {@link Exporter} to export.
 */
public class ExportExecutor
{
    /**
     * Used to log any messages.
     */
    private static final Logger LOGGER = Logger.getLogger(ExportExecutor.class);

    /**
     * Used to get {@link DataElement} data.
     */
    private final DataElementCache myCache;

    /**
     * Used to display toast messages if the export failed.
     */
    private final EventManager myEventManager;

    /**
     * Used to get the configured time precision.
     */
    private final PreferencesRegistry myPreferencesRegistry;

    /**
     * Used to show export {@link TaskActivity}.
     */
    private final UIRegistry myUIRegistry;

    /**
     * Constructs a new {@link DataElementExporter}.
     *
     * @param cache Used to get {@link DataElement} data.
     * @param uiRegistry Used to show export {@link TaskActivity}.
     * @param eventManager Used to display toast messages if the export failed.
     * @param prefsRegistry Used to get the configured time precision.
     */
    public ExportExecutor(DataElementCache cache, UIRegistry uiRegistry, EventManager eventManager,
            PreferencesRegistry prefsRegistry)
    {
        myCache = cache;
        myUIRegistry = uiRegistry;
        myEventManager = eventManager;
        myPreferencesRegistry = prefsRegistry;
    }

    /**
     * Asks the user for the export file and other various export options. If
     * user clicks save, the passed exporter will be executed in a background
     * thread.
     *
     * @param dialog The export dialog to show asking the user what file to
     *            export to, plus other various options.
     * @param parent The parent component to show the dialog.
     * @param exporter The exporter to execute.
     * @param tableModel The table model to export.
     * @param table The table to export.
     * @param optionsModel Contains the user's inputs for the other various
     *            export options.
     * @param completeListener An object wanting notification when the export is
     *            done.
     */
    public void executeExport(JFileChooser dialog, Component parent, Exporter exporter, MetaColumnsTableModel tableModel,
            JTable table, ExportOptionsModel optionsModel, ExportCompleteListener completeListener)
    {
        // calling provideElements() before showSaveDialog() means optionsModel
        // doesnt get updated, so we need to do it again. kind of a hack, should
        // find a better solution when we have time
        DataElementProvider preProvider = new DataElementProvider(optionsModel, myCache);
        int precision = myPreferencesRegistry.getPreferences(ListToolPreferences.class)
                .getInt(ListToolPreferences.LIST_TOOL_TIME_PRECISION_DIGITS, 0);
        List<DataElement> exportElements = preProvider.provideElements(tableModel, table, precision);

        exporter.setObjects(exportElements);
        if (exporter.preExport())
        {
            int selection = dialog.showSaveDialog(parent);
            if (DataElementsExportDialog.APPROVE_OPTION == selection)
            {
                exportElements = preProvider.provideElements(tableModel, table, precision);
                int size = exportElements.size();
                exporter.setObjects(exportElements);
                File exportFile = getExportFile(dialog, exporter);
                boolean canExport = !exportFile.exists();
                if (!canExport)
                {
                    canExport = completeListener.askUserForOverwrite(parent, exportFile);
                }

                if (canExport)
                {
                    TaskActivity activity = new TaskActivity();
                    activity.setActive(true);
                    activity.setLabelValue("Exporting data to " + exportFile);
                    myUIRegistry.getMenuBarRegistry().addTaskActivity(activity);
                    ThreadUtilities.runBackground(
                        () -> exportInBackground(parent, exporter, exportFile, activity, completeListener, size));
                }
            }
        }
    }

    /**
     * Executes the exporter.
     *
     * @param parent The parent component the export was initiated from.
     * @param exporter The exporter to execute, assumes setObjects has already
     *            been called on it.
     * @param exportFile The file to export to.
     * @param activity The {@link TaskActivity} to mark complete.
     * @param completeListener An object wanting notification when the export is
     *            done.
     * @param count The number of records exported.
     */
    private void exportInBackground(Component parent, Exporter exporter, File exportFile, TaskActivity activity,
            ExportCompleteListener completeListener, int count)
    {
        try
        {
            File actualFile = exporter.export(exportFile);
            completeListener.exportComplete(parent, actualFile, count);
        }
        catch (IOException | ExportException e)
        {
            LOGGER.error(e.getMessage(), e);
            UserMessageEvent.error(myEventManager, "Failed to export data to " + exportFile, false, this, e, true);
        }
        finally
        {
            activity.setComplete(true);
        }
    }

    /**
     * Gets the file to export to, also adds an extension if need be.
     *
     * @param dialog The file choosing dialog.
     * @param exporter The exporter, contains the valid file extension.
     * @return The file to export to.
     */
    private File getExportFile(JFileChooser dialog, Exporter exporter)
    {
        File exportFile = dialog.getSelectedFile();
        if (!exporter.getMimeType().getFileFilter().accept(exportFile) && exporter.getMimeType().getFileExtensions().length > 0)
        {
            exportFile = new File(exportFile.getAbsolutePath() + "." + exporter.getMimeType().getFileExtensions()[0]);
        }

        return exportFile;
    }
}

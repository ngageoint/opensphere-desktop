package io.opensphere.core.export;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.util.Collection;

import edu.umd.cs.findbugs.annotations.Nullable;
import javax.swing.JComponent;

import io.opensphere.core.Toolbox;
import io.opensphere.core.control.action.MenuOption;
import io.opensphere.core.util.MimeType;

/**
 * Generic exporter interface.
 */
public interface Exporter
{
    /**
     * Returns whether the exporter can export all the objects set with
     * {@link #setObjects(Collection)}.
     *
     * @param target The type of the export target.
     *
     * @return whether the objects can be exported
     */
    boolean canExport(Class<?> target);

    /**
     * Optionally provides any setup functionality, called before export
     * happens.
     *
     * @return true if success
     */
    boolean preExport();

    /**
     * Exports the objects to the given target.
     *
     * @param target The object to write to. This is just a hint; the exporter
     *            may not actually write to the given object.
     *
     * @throws IOException If a problem occurs writing to the target.
     * @throws ExportException If a problem occurs during export.
     * @return the file exported to.
     */
    File export(Object target) throws IOException, ExportException;

    /**
     * Get the actual files that will be written if an export operation is
     * performed using the given file.
     *
     * @param file The input file.
     * @return The collection of files chosen by the exporter.
     */
    Collection<? extends File> getExportFiles(File file);

    /**
     * Optionally provide an accessory display for the file chooser.
     *
     * @return The accessory or {@code null}.
     */
    @Nullable
    JComponent getFileChooserAccessory();

    /**
     * Gets the menu option.
     *
     * @return the menu option
     */
    MenuOption getMenuOption();

    /**
     * Provide a list of menu items for the context menu associated with this
     * key.
     *
     * @param contextId the context for the menus.
     * @param key Key for which the context menu will be created.
     * @return Menu items to supply in the context menu.
     */
    Collection<? extends Component> getMenuItems(String contextId, Object key);

    /**
     * Gets the MIME type.
     *
     * @return The MIME type.
     */
    MimeType getMimeType();

    /**
     * Get display text for this exporter's MIME type.
     *
     * @return The text.
     */
    String getMimeTypeString();

    /**
     * Set the objects to be exported.
     *
     * @param objects The objects to be exported.
     * @return this
     */
    Exporter setObjects(Collection<?> objects);

    /**
     * Set the system toolbox for the exporter's use.
     *
     * @param toolbox The system toolbox.
     */
    void setToolbox(Toolbox toolbox);
}

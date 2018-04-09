package io.opensphere.core.export;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.ServiceLoader;

import io.opensphere.core.Toolbox;
import io.opensphere.core.util.MimeType;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.collections.New;

/**
 * Facility for interfacing with export service providers.
 */
public final class Exporters
{
    /**
     * Export some objects using the first exporter found for the given MIME
     * type and objects.
     *
     * @param mimeType The MIME type of the export
     * @param objects The objects to be exported.
     * @param file The file to write to. The exporter may append an extension to
     *            this file, or may write multiple files based on this file
     *            name.
     * @throws IOException If there is a problem writing to the file.
     * @throws ExportException If there is a problem in the exporter, or if no
     *             exporter is found.
     */
    public static void export(MimeType mimeType, Collection<?> objects, File file) throws IOException, ExportException
    {
        loadExporter(mimeType, objects).export(file);
    }

    /**
     * Get the first available exporter for some objects.
     *
     * @param objects The objects to be exported.
     * @param toolbox The toolbox to provide to the exporters.
     * @param targetType The type of the target of the export.
     * @return The exporter.
     * @throws ExportException If no  exporter is available.
     */
    public static Exporter getExporter(Collection<?> objects, Toolbox toolbox, Class<?> targetType) throws ExportException
    {
        List<Exporter> exporters = getExporters(objects, toolbox, targetType);
        if (exporters.isEmpty())
        {
            throw new ExportException("No exporters availble for objects " + objects + " and type " + targetType);
        }
        return exporters.get(0);
    }

    /**
     * Get the available exporters for some objects.
     *
     * @param objects The objects to be exported.
     * @param toolbox The toolbox to provide to the exporters.
     * @param targetType The type of the target of the export.
     * @return The exporters.
     */
    public static List<Exporter> getExporters(Collection<?> objects, Toolbox toolbox, Class<?> targetType)
    {
        List<Exporter> result = New.list();
        for (Exporter exporter : getAllExporters())
        {
            if (exporter.setObjects(objects).canExport(targetType))
            {
                exporter.setToolbox(toolbox);
                result.add(exporter);
            }
        }
        return result;
    }

    /**
     * Loads the exporter for the given objects.
     *
     * @param mimeType The MIME type of the export
     * @param objects The objects to be exported.
     * @return The exporter
     * @throws ExportException If no  exporter is found.
     */
    public static Exporter loadExporter(MimeType mimeType, Collection<?> objects) throws ExportException
    {
        for (Exporter exporter : getAllExporters())
        {
            if (exporter.getMimeType() == mimeType && exporter.setObjects(objects).canExport(java.io.File.class))
            {
                return exporter;
            }
        }

        throw new ExportException("No exporter found for MIME type: " + mimeType);
    }

    /**
     * Provide a list of menu items for the context menu associated with this
     * key.
     *
     * @param contextId the context for the menus.
     * @param key Key for which the context menu will be created.
     * @param toolbox The toolbox
     * @return Menu items to supply in the context menu.
     */
    public static Collection<? extends Component> getMenuItems(String contextId, Object key, Toolbox toolbox)
    {
        Collection<Component> menuItems = New.list();
        for (Exporter exporter : getAllExporters())
        {
            exporter.setToolbox(toolbox);
            Collection<? extends Component> exporterMenuItems = exporter.getMenuItems(contextId, key);
            if (CollectionUtilities.hasContent(exporterMenuItems))
            {
                menuItems.addAll(exporterMenuItems);
            }
        }
        return menuItems;
    }

    /**
     * Gets all exporters.
     *
     * @return The exporters.
     */
    private static Iterable<Exporter> getAllExporters()
    {
        return ServiceLoader.load(Exporter.class);
    }

    /** Disallow instantiation. */
    private Exporters()
    {
    }
}

package io.opensphere.kml.export;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;

import io.opensphere.core.control.action.MenuOption;
import io.opensphere.core.export.AbstractExporter;
import io.opensphere.core.preferences.ListToolPreferences;
import io.opensphere.core.util.MimeType;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.element.DataElement;

/**
 * An export that exports {@link DataElement}s to a KML file.
 */
public class KMLFileExporter extends AbstractExporter
{
    /**
     * The pre export model.
     */
    private final KMLExportOptionsModel myModel = new KMLExportOptionsModel();

    @Override
    public boolean canExport(Class<?> target)
    {
        return target != null && File.class.isAssignableFrom(target)
                && getObjects().stream().allMatch(o -> o instanceof DataElement);
    }

    @Override
    public boolean preExport()
    {
        Map<String, Class<?>> metadata = null;
        Collection<DataElement> elements = getElements();
        if (!elements.isEmpty())
        {
            DataElement firstElement = getElements().iterator().next();
            metadata = firstElement.getDataTypeInfo().getMetaDataInfo().getKeyClassTypeMap();
        }

        KMLPreExportDialog dialog = new KMLPreExportDialog(getToolbox(), metadata, myModel);
        dialog.buildAndShow();
        if (dialog.getSelection() == JOptionPane.OK_OPTION)
        {
            dialog.getOptionsBinder().viewToModel();
            return true;
        }
        return false;
    }

    @Override
    public File export(File file) throws IOException
    {
        File actualFile = null;
        Collection<DataElement> elements = getElements();
        if (!elements.isEmpty())
        {
            DataElement firstElement = elements.iterator().next();
            DataTypeInfo dataType = firstElement.getDataTypeInfo();
            List<String> columnNames = firstElement.getMetaData().getKeys();
            int timePrecision = getToolbox().getPreferencesRegistry().getPreferences(ListToolPreferences.class)
                    .getInt(ListToolPreferences.LIST_TOOL_TIME_PRECISION_DIGITS, 0);
            SimpleDateFormat dateFormatter = ListToolPreferences.getSimpleDateFormatForPrecision(timePrecision);

            KMLExporter kmlExporter = new KML22Exporter();
            kmlExporter.generateKMLDocument(dataType, elements, columnNames, dateFormatter, myModel);
            actualFile = kmlExporter.writeKMLToFile(file);
        }
        return actualFile;
    }

    @Override
    public MenuOption getMenuOption()
    {
        String mimeType = getMimeTypeString();
        return new MenuOption("To " + mimeType + " (.kml)", mimeType, "Exports data to a KML file.");
    }

    @Override
    public MimeType getMimeType()
    {
        return MimeType.KML;
    }

    /**
     * Gets the stored data element objects.
     *
     * @return the collection of data elements
     */
    private Collection<DataElement> getElements()
    {
        return CollectionUtilities.filterDowncast(getObjects(), DataElement.class);
    }
}

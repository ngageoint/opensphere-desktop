package io.opensphere.kml.export;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;

import io.opensphere.core.export.AbstractExporter;
import io.opensphere.core.preferences.ListToolPreferences;
import io.opensphere.core.util.MimeType;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.element.DataElement;
import io.opensphere.mantle.util.MantleToolboxUtils;

/**
 * An exporter that exports a layer to a KML file.
 */
public class KMLFileLayerExporter extends AbstractExporter
{
    /**
     * The pre-export model.
     */
    private final KMLExportOptionsModel myModel = new KMLExportOptionsModel();

    @Override
    public boolean canExport(Class<?> target)
    {
        return target != null && File.class.isAssignableFrom(target)
                && getObjects().stream().allMatch(o -> o instanceof DataTypeInfo);
    }

    @Override
    public boolean preExport()
    {
        Map<String, Class<?>> metaData = null;
        Collection<DataTypeInfo> dataTypes = getElements();
        if (!dataTypes.isEmpty())
        {
            metaData = dataTypes.iterator().next().getMetaDataInfo().getKeyClassTypeMap();
        }

        KMLPreExportDialog dialog = new KMLPreExportDialog(getToolbox(), metaData, myModel);
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
        File kmlFile = null;
        Collection<DataTypeInfo> dataTypes = getElements();
        if (!dataTypes.isEmpty())
        {
            DataTypeInfo dataType = dataTypes.iterator().next();
            int timePrecision = getToolbox().getPreferencesRegistry().getPreferences(ListToolPreferences.class)
                    .getInt(ListToolPreferences.LIST_TOOL_TIME_PRECISION_DIGITS, 0);
            SimpleDateFormat dateFormatter = ListToolPreferences.getSimpleDateFormatForPrecision(timePrecision);
            List<DataElement> dataElements = MantleToolboxUtils.getDataElementLookupUtils(getToolbox())
                    .getDataElements(dataType);
            List<String> columnNames = dataElements.iterator().next().getMetaData().getKeys();

            KMLExporter kmlExporter = new KML22Exporter();
            kmlExporter.generateKMLDocument(dataType, dataElements, columnNames, dateFormatter, myModel);
            kmlFile = kmlExporter.writeKMLToFile(file);
        }

        EventQueueUtilities.runOnEDT(() ->JOptionPane.showMessageDialog(getToolbox().getUIRegistry().getMainFrameProvider().get(),
                "Successfully saved file:\n" + file.getAbsolutePath(), "Saved KML File", JOptionPane.INFORMATION_MESSAGE));

        return kmlFile;
    }

    @Override
    public MimeType getMimeType()
    {
        return MimeType.KML;
    }

    /**
     * Gets the stored DataTypeInfo for the layer.
     *
     * @return the DataTypeInfo
     */
    private Collection<DataTypeInfo> getElements()
    {
        return CollectionUtilities.filterDowncast(getObjects(), DataTypeInfo.class);
    }
}

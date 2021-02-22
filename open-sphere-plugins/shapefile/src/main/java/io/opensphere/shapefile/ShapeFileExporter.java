package io.opensphere.shapefile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.swing.JOptionPane;

import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import io.opensphere.core.common.shapefile.shapes.ShapeRecord;
import io.opensphere.core.common.shapefile.utils.ShapefileRecord;
import io.opensphere.core.common.shapefile.v2.ESRIShapefile;
import io.opensphere.core.common.shapefile.v2.ESRIShapefile.Mode;
import io.opensphere.core.export.AbstractExporter;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.preferences.ListToolPreferences;
import io.opensphere.core.util.MimeType;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.MetaDataInfo;
import io.opensphere.mantle.data.element.DataElement;
import io.opensphere.mantle.util.MantleToolboxUtils;
import io.opensphere.mantle.util.TimeSpanUtility;

/**
 * An exporter that exports a layer to a Shapefile.
 */
public class ShapeFileExporter extends AbstractExporter
{
    @Override
    public boolean canExport(Class<?> target)
    {
        return target != null && File.class.isAssignableFrom(target)
                && getObjects().stream().allMatch(o -> o instanceof DataTypeInfo);
    }

    @Override
    public File export(File file) throws IOException
    {
        DataTypeInfo dataType = getElements().iterator().next();
        MetaDataInfo metaData = dataType.getMetaDataInfo();
        List<DataElement> dataElements = MantleToolboxUtils.getDataElementLookupUtils(getToolbox())
                .getDataElements(dataType);
        final File esriFile = ShapeFileExportUtilities.enforceSuffix(file);

        ESRIShapefile shapefile = new ESRIShapefile(Mode.WRITE, esriFile.getAbsolutePath());

        List<String> columnNames = New.list(dataType.getMetaDataInfo().getKeyNames());

        // Lat/Lon go in the point record,
        // so remove them from the list for the meta-data file.
        int lonIndex = columnNames.indexOf(metaData.getLongitudeKey());
        if (lonIndex != -1)
        {
            columnNames.remove(lonIndex);
        }
        int latIndex = columnNames.indexOf(metaData.getLatitudeKey());
        if (latIndex != -1)
        {
            columnNames.remove(latIndex);
        }

        TObjectIntMap<String> colNameToLengthMap = determineMaxCharsForColumn(columnNames, dataElements);
        shapefile.setMetadataHeader(
                ShapeFileExportUtilities.getMetadataHeader(metaData, columnNames, colNameToLengthMap, getToolbox()));

        int timePrecision = getToolbox().getPreferencesRegistry().getPreferences(ListToolPreferences.class)
                .getInt(ListToolPreferences.LIST_TOOL_TIME_PRECISION_DIGITS, 0);
        SimpleDateFormat dateFormatter = ListToolPreferences.getSimpleDateFormatForPrecision(timePrecision);
        for (DataElement element : dataElements)
        {
            shapefile.add(createShapefileRecord(element, columnNames, dateFormatter));
        }

        shapefile.getPrj().writeProjection();

        boolean hadError = false;
        try
        {
            shapefile.doFinalWrite();
            shapefile.close();
        }
        catch (FileNotFoundException e)
        {
            hadError = true;
            JOptionPane.showMessageDialog(getToolbox().getUIRegistry().getMainFrameProvider().get(),
                    "Error encountered while saving shape file", "File Save Error", JOptionPane.ERROR_MESSAGE);
        }
        catch (IOException e)
        {
            hadError = true;
            JOptionPane.showMessageDialog(getToolbox().getUIRegistry().getMainFrameProvider().get(),
                    "Error encountered while saving shape file", "File Save Error", JOptionPane.ERROR_MESSAGE);
        }

        if (!hadError)
        {
            EventQueueUtilities.runOnEDT(() -> JOptionPane.showMessageDialog(getToolbox().getUIRegistry().getMainFrameProvider().get(),
                    "Successfully saved file:\n" + esriFile.getAbsolutePath(), "Saved ESRI Shape File",
                    JOptionPane.INFORMATION_MESSAGE));
        }

        return esriFile;
    }

    @Override
    public MimeType getMimeType()
    {
        return MimeType.SHAPE;
    }

    /**
     * Creates the Shapefile record.
     *
     * @param element the data element for the record
     * @param columnNames the column names of the layer
     * @param dateFormatter the date formatter
     * @return the shapefile record
     */
    private ShapefileRecord createShapefileRecord(DataElement element, List<String> columnNames,
            SimpleDateFormat dateFormatter)
    {
        ShapeRecord shape = ShapeFileExportUtilities.createShapeRecord(element);

        Object[] metaRow = new Object[columnNames.size()];
        for (int i = 0; i < columnNames.size(); i++)
        {
            Object value = element.getMetaData().getValue(columnNames.get(i));

            if (value instanceof Date)
            {
                value = dateFormatter.format(value);
            }
            else if (value instanceof TimeSpan)
            {
                value = TimeSpanUtility.formatTimeSpanSingleTimeOnly(dateFormatter, (TimeSpan) value);
            }

            metaRow[i] = value;
        }

        return new ShapefileRecord(shape, metaRow);
    }

    /**
     * Determine maximum length of data for each column.
     *
     * @param columnNames the column names of the layer
     * @param dataElements the list of data elements
     * @return the map of column name to max length
     */
    private TObjectIntMap<String> determineMaxCharsForColumn(List<String> columnNames, List<DataElement> dataElements)
    {
        TObjectIntMap<String> colNameToLengthMap = new TObjectIntHashMap<>();

        for (DataElement element : dataElements)
        {
            for (String column : columnNames)
            {
                Object value = element.getMetaData().getValue(column);
                String stringValue = value == null ? "" : value.toString();

                if (!colNameToLengthMap.containsKey(column) || colNameToLengthMap.get(column) < stringValue.length())
                {
                    colNameToLengthMap.put(column, stringValue.length());
                }
            }
        }

        return colNameToLengthMap;
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

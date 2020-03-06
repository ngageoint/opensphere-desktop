package io.opensphere.shapefile;

import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.JOptionPane;

import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import io.opensphere.core.common.shapefile.shapes.MultiPointRecord;
import io.opensphere.core.common.shapefile.shapes.PointRecord;
import io.opensphere.core.common.shapefile.shapes.PolyLineRecord;
import io.opensphere.core.common.shapefile.shapes.PolygonRecord;
import io.opensphere.core.common.shapefile.shapes.ShapeRecord;
import io.opensphere.core.common.shapefile.utils.DBFColumnInfo;
import io.opensphere.core.common.shapefile.utils.ShapefileRecord;
import io.opensphere.core.common.shapefile.v2.ESRIShapefile;
import io.opensphere.core.common.shapefile.v2.ESRIShapefile.Mode;
import io.opensphere.core.export.AbstractExporter;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.preferences.ListToolPreferences;
import io.opensphere.core.util.MimeType;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.collections.StreamUtilities;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.MetaDataInfo;
import io.opensphere.mantle.data.element.DataElement;
import io.opensphere.mantle.data.element.MapDataElement;
import io.opensphere.mantle.data.geom.MapGeometrySupport;
import io.opensphere.mantle.data.geom.MapLocationGeometrySupport;
import io.opensphere.mantle.data.geom.MapPolygonGeometrySupport;
import io.opensphere.mantle.data.geom.MapPolylineGeometrySupport;
import io.opensphere.mantle.data.util.DataElementLocationExtractUtil;
import io.opensphere.mantle.util.MantleToolboxUtils;
import io.opensphere.mantle.util.TimeSpanUtility;

/**
 * An exporter that exports a layer to a Shapefile.
 */
public class ShapeFileExporter extends AbstractExporter
{
    /** The Constant SHAPE_FILE_EXTENSION. */
    private static final String SHAPE_FILE_EXTENSION = ".shp";

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
        final File esriFile = enforceSuffix(file);

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
        shapefile.setMetadataHeader(getMetadataHeader(metaData, columnNames, colNameToLengthMap));

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
     * Gets the stored DataTypeInfo for the layer.
     *
     * @return the DataTypeInfo
     */
    private Collection<DataTypeInfo> getElements()
    {
        return CollectionUtilities.filterDowncast(getObjects(), DataTypeInfo.class);
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
        ShapeRecord shape = createShapeRecord(element);

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
     * Get the metadata header.
     *
     * @param metaData the meta data for the layer
     * @param columnNames the column names of the layer
     * @param colIndexToLengthMap the map of column names to the data length
     * @return the metadata header
     */
    private List<DBFColumnInfo> getMetadataHeader(final MetaDataInfo metaData, List<String> columnNames,
            TObjectIntMap<String> colIndexToLengthMap)
    {
        List<DBFColumnInfo> metadataHeader = new LinkedList<>();
        for (String column : columnNames)
        {
            char type = 'C';
            int length = Math.min(127, colIndexToLengthMap.containsKey(column) ? colIndexToLengthMap.get(column) : 100);
            if (metaData.hasKey(column) && metaData.isKeyNumeric(getToolbox(), column))
            {
                type = 'N';
                length = 18;
            }
            metadataHeader.add(new DBFColumnInfo(column, type, (byte) length));
        }
        return metadataHeader;
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
     * Creates a ShapeRecord for the given data element.
     *
     * @param element the data element
     * @return the ShapeRecord
     */
    private ShapeRecord createShapeRecord(DataElement element)
    {
        ShapeRecord shape = null;
        if (element instanceof MapDataElement)
        {
            MapGeometrySupport geometry = ((MapDataElement) element).getMapGeometrySupport();
            if (geometry instanceof MapLocationGeometrySupport)
            {
                MapLocationGeometrySupport pointGeom = (MapLocationGeometrySupport) geometry;
                shape = new PointRecord(toPoint(pointGeom.getLocation()));
                if (pointGeom.hasChildren())
                {
                    List<Point2D.Double> points = toPoints(StreamUtilities
                            .filterDowncast(pointGeom.getChildren().stream(), MapLocationGeometrySupport.class)
                            .map(ch -> ch.getLocation()));
                    if (!points.isEmpty())
                    {
                        points.add(toPoint(pointGeom.getLocation()));
                        shape = new MultiPointRecord(points);
                    }
                }
            }
            else if (geometry instanceof MapPolylineGeometrySupport)
            {
                MapPolylineGeometrySupport lineGeom = (MapPolylineGeometrySupport) geometry;
                PolyLineRecord lineRecord = new PolyLineRecord(toPoints(lineGeom.getLocations()));
                if (lineGeom.hasChildren())
                {
                    StreamUtilities.filterDowncast(lineGeom.getChildren().stream(), MapPolylineGeometrySupport.class)
                            .forEach(child -> lineRecord.addPart(toPoints(child.getLocations())));
                }
                shape = lineRecord;
            }
            else if (geometry instanceof MapPolygonGeometrySupport)
            {
                MapPolygonGeometrySupport polygonGeom = (MapPolygonGeometrySupport) geometry;
                PolygonRecord polygonRecord = new PolygonRecord(toPoints(polygonGeom.getLocations()));
                if (polygonGeom.hasChildren())
                {
                    StreamUtilities.filterDowncast(polygonGeom.getChildren().stream(), MapPolygonGeometrySupport.class)
                            .forEach(child -> polygonRecord.addPart(toPoints(child.getLocations())));
                }
                shape = polygonRecord;
            }
        }
        // Fall back to getting the shape from the metadata
        if (shape == null)
        {
            MetaDataInfo metaData = element.getDataTypeInfo().getMetaDataInfo();
            LatLonAlt lla = DataElementLocationExtractUtil.getPosition(false, metaData.getLongitudeKey(),
                    metaData.getLatitudeKey(), metaData.getAltitudeKey(), element);
            double lat = lla.getLatD();
            double lon = lla.getLonD();
            shape = new PointRecord(lon, lat);
        }
        return shape;
    }

    /**
     * Converts the locations to shape file points.
     *
     * @param locations the locations
     * @return the points
     */
    private static List<Point2D.Double> toPoints(Collection<? extends LatLonAlt> locations)
    {
        return toPoints(locations.stream());
    }

    /**
     * Converts the location stream to shape file points.
     *
     * @param stream the location stream
     * @return the points
     */
    private static List<Point2D.Double> toPoints(Stream<? extends LatLonAlt> stream)
    {
        return stream.map(l -> toPoint(l)).collect(Collectors.toList());
    }

    /**
     * Converts the location to a point.
     *
     * @param location the location
     * @return the point
     */
    private static Point2D.Double toPoint(LatLonAlt location)
    {
        return new Point2D.Double(location.getLonD(), location.getLatD());
    }

    /**
     * Enforce the shapefile suffix.
     *
     * @param file the file to enforce suffix on
     * @return the file post-enforcement
     */
    private static File enforceSuffix(final File file)
    {
        if (!file.getAbsolutePath().toLowerCase().endsWith(SHAPE_FILE_EXTENSION))
        {
            return new File(file.getAbsolutePath() + SHAPE_FILE_EXTENSION);
        }
        return file;
    }
}

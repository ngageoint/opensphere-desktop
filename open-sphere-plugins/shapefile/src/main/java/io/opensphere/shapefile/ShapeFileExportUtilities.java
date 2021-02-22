package io.opensphere.shapefile;

import java.awt.geom.Point2D;
import java.io.File;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import gnu.trove.map.TObjectIntMap;
import io.opensphere.core.Toolbox;
import io.opensphere.core.common.shapefile.shapes.MultiPointRecord;
import io.opensphere.core.common.shapefile.shapes.PointRecord;
import io.opensphere.core.common.shapefile.shapes.PolyLineRecord;
import io.opensphere.core.common.shapefile.shapes.PolygonRecord;
import io.opensphere.core.common.shapefile.shapes.ShapeRecord;
import io.opensphere.core.common.shapefile.utils.DBFColumnInfo;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.util.collections.StreamUtilities;
import io.opensphere.mantle.data.MetaDataInfo;
import io.opensphere.mantle.data.element.DataElement;
import io.opensphere.mantle.data.element.MapDataElement;
import io.opensphere.mantle.data.geom.MapGeometrySupport;
import io.opensphere.mantle.data.geom.MapLocationGeometrySupport;
import io.opensphere.mantle.data.geom.MapPolygonGeometrySupport;
import io.opensphere.mantle.data.geom.MapPolylineGeometrySupport;
import io.opensphere.mantle.data.util.DataElementLocationExtractUtil;

/**
 * Utility class containing methods related to exporting shape files.
 */
public class ShapeFileExportUtilities
{
    /** The Constant SHAPE_FILE_EXTENSION. */
    public static final String SHAPE_FILE_EXTENSION = ".shp";

    /**
     * Creates a ShapeRecord for the given data element.
     *
     * @param element the data element
     * @return the ShapeRecord
     */
    public static ShapeRecord createShapeRecord(DataElement element)
    {
        ShapeRecord shape = null;
        if (element instanceof MapDataElement)
        {
            MapGeometrySupport geometry = ((MapDataElement)element).getMapGeometrySupport();
            if (geometry instanceof MapLocationGeometrySupport)
            {
                MapLocationGeometrySupport pointGeom = (MapLocationGeometrySupport)geometry;
                shape = new PointRecord(toPoint(pointGeom.getLocation()));
                if (pointGeom.hasChildren())
                {
                    List<Point2D.Double> points = toPoints(
                            StreamUtilities.filterDowncast(pointGeom.getChildren().stream(), MapLocationGeometrySupport.class)
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
                MapPolylineGeometrySupport lineGeom = (MapPolylineGeometrySupport)geometry;
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
                MapPolygonGeometrySupport polygonGeom = (MapPolygonGeometrySupport)geometry;
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
    public static List<Point2D.Double> toPoints(Collection<? extends LatLonAlt> locations)
    {
        return toPoints(locations.stream());
    }

    /**
     * Converts the location stream to shape file points.
     *
     * @param stream the location stream
     * @return the points
     */
    public static List<Point2D.Double> toPoints(Stream<? extends LatLonAlt> stream)
    {
        return stream.map(l -> toPoint(l)).collect(Collectors.toList());
    }

    /**
     * Converts the location to a point.
     *
     * @param location the location
     * @return the point
     */
    public static Point2D.Double toPoint(LatLonAlt location)
    {
        return new Point2D.Double(location.getLonD(), location.getLatD());
    }

    /**
     * Enforce suffix.
     *
     * @param file The file.
     * @return the file
     */
    public static File enforceSuffix(final File file)
    {
        if (!file.getAbsolutePath().toLowerCase().endsWith(SHAPE_FILE_EXTENSION))
        {
            return new File(file.getAbsolutePath() + SHAPE_FILE_EXTENSION);
        }
        return file;
    }

    /**
     * Get the metadata header.
     *
     * @param metaData The meta data.
     * @param columnNames The column names.
     * @param colIndexToLengthMap The col index to length map.
     * @return The metadata header.
     */
    public static List<DBFColumnInfo> getMetadataHeader(final MetaDataInfo metaData, List<String> columnNames,
            TObjectIntMap<String> colIndexToLengthMap, Toolbox toolbox)
    {
        List<DBFColumnInfo> metadataHeader = new LinkedList<>();
        for (String column : columnNames)
        {
            char type = 'C';
            int length = Math.min(127, colIndexToLengthMap.containsKey(column) ? colIndexToLengthMap.get(column) : 100);
            if (metaData.hasKey(column) && metaData.isKeyNumeric(toolbox, column))
            {
                type = 'N';
                length = 18;
            }
            metadataHeader.add(new DBFColumnInfo(column, type, (byte) length));
        }
        return metadataHeader;
    }

    /**
     * Disallow instantiation.
     */
    private ShapeFileExportUtilities()
    {
    }
}

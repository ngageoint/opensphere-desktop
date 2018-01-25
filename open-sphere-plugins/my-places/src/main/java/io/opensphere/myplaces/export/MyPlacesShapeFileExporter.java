package io.opensphere.myplaces.export;

import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JOptionPane;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.vividsolutions.jtsexample.geom.ExtendedCoordinate;

import de.micromata.opengis.kml.v_2_2_0.Boundary;
import de.micromata.opengis.kml.v_2_2_0.Coordinate;
import de.micromata.opengis.kml.v_2_2_0.Data;
import de.micromata.opengis.kml.v_2_2_0.Geometry;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import de.micromata.opengis.kml.v_2_2_0.Point;
import de.micromata.opengis.kml.v_2_2_0.Polygon;
import de.micromata.opengis.kml.v_2_2_0.Style;
import de.micromata.opengis.kml.v_2_2_0.StyleSelector;
import de.micromata.opengis.kml.v_2_2_0.TimePrimitive;
import de.micromata.opengis.kml.v_2_2_0.TimeSpan;
import de.micromata.opengis.kml.v_2_2_0.TimeStamp;
import io.opensphere.core.common.shapefile.shapes.PointRecord;
import io.opensphere.core.common.shapefile.shapes.PointZRecord;
import io.opensphere.core.common.shapefile.shapes.PolyLineRecord;
import io.opensphere.core.common.shapefile.shapes.PolygonRecord;
import io.opensphere.core.common.shapefile.shapes.ShapeRecord;
import io.opensphere.core.common.shapefile.utils.DBFColumnInfo;
import io.opensphere.core.common.shapefile.utils.ShapefileRecord;
import io.opensphere.core.common.shapefile.v2.ESRIShapefile;
import io.opensphere.core.common.shapefile.v2.ESRIShapefile.Mode;
import io.opensphere.core.common.shapefile.v2.dbase.DbfFieldType;
import io.opensphere.core.util.MimeType;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.kml.gx.Track;
import io.opensphere.myplaces.constants.ImportExportHeader;

/**
 * Exporter from MyPlaces to Shapefile.
 */
public class MyPlacesShapeFileExporter extends AbstractMyPlacesExporter
{
    /** The logger. */
    private static final Logger LOGGER = Logger.getLogger(MyPlacesShapeFileExporter.class);

    /**
     * Get the geometry type to export, asking the user if the data contain more
     * than one.
     *
     * @param placemarks the placemarks
     * @return the geometry type to export
     */
    private static GeomType getGeomType(Collection<Placemark> placemarks)
    {
        GeomType geomType = null;

        // Determine the set of all geometry types in the data
        Set<Class<? extends Geometry>> dataGeomClasses = New.set();
        for (Placemark placemark : placemarks)
        {
            if (placemark.getGeometry() != null)
            {
                dataGeomClasses.add(placemark.getGeometry().getClass());
            }
        }

        if (dataGeomClasses.size() > 1)
        {
            Collection<GeomType> dataGeomTypes = New.list(dataGeomClasses.size());
            for (Class<? extends Geometry> dataGeomType : dataGeomClasses)
            {
                dataGeomTypes.add(GeomType.fromClass(dataGeomType));
            }
            final EnumSet<GeomType> geomTypesToSelect = EnumSet.copyOf(dataGeomTypes);

            geomType = EventQueueUtilities.happyOnEdt(() ->
            {
                Object[] selectionValues = geomTypesToSelect.toArray();
                GeomType value = (GeomType)JOptionPane.showInputDialog(null,
                        "Shapefiles only support one geometry type per file.\nPlease choose one:", "Select Type to Export",
                        JOptionPane.QUESTION_MESSAGE, null, selectionValues, null);
                return value;
            });
        }
        else if (dataGeomClasses.size() == 1)
        {
            geomType = GeomType.fromClass(dataGeomClasses.iterator().next());
        }

        return geomType;
    }

    /**
     * Gets the metadata for the given placemark.
     *
     * @param placemark the placemark
     * @param metadataFields the metadata fields extracted from the shapefile
     *            header.
     * @return the metadata
     */
    private static Object[] getMetadata(Placemark placemark, List<DBFColumnInfo> metadataFields)
    {
        List<Data> data = placemark.getExtendedData().getData();
        Map<String, String> extendedValues = New.map(data.size());
        for (Data dataPoint : data)
        {
            extendedValues.put(dataPoint.getName(), dataPoint.getValue());
        }

        List<StyleSelector> styleSelector = placemark.getStyleSelector();
        for (StyleSelector ss : styleSelector)
        {
            Style sty = (Style)ss;
            extendedValues.put(ImportExportHeader.BALLOON_COLOR.getTitle(), "0x" + sty.getIconStyle().getColor());
            extendedValues.put(ImportExportHeader.FONT_COLOR.getTitle(), "0x" + sty.getBalloonStyle().getTextColor());
        }

        TimePrimitive timePrimitive = placemark.getTimePrimitive();
        if (timePrimitive != null)
        {
            if (timePrimitive instanceof TimeStamp)
            {
                extendedValues.put(ImportExportHeader.START_TIME.getTitle(), ((TimeStamp)timePrimitive).getWhen());
            }
            else if (timePrimitive instanceof TimeSpan)
            {
                extendedValues.put(ImportExportHeader.START_TIME.getTitle(), ((TimeSpan)timePrimitive).getBegin());
                extendedValues.put(ImportExportHeader.END_TIME.getTitle(), ((TimeSpan)timePrimitive).getEnd());
            }
        }

        List<Object> metadata = New.list(metadataFields.size());

        for (DBFColumnInfo dbfColumnInfo : metadataFields)
        {
            String value = extendedValues.get(dbfColumnInfo.getFieldName());
            if (dbfColumnInfo.getFieldName().equals(ImportExportHeader.TITLE.getTitle()))
            {
                metadata.add(placemark.getName());
            }
            else if (dbfColumnInfo.getFieldName().equals(ImportExportHeader.DESCRIPTION.getTitle()))
            {
                metadata.add(placemark.getDescription());
            }
            else
            {
                switch (dbfColumnInfo.getType())
                {
                    case LOGICAL:
                        metadata.add(StringUtils.equals("true", value) ? "t" : "f");
                        break;
                    case CHARACTER:
                    case DOUBLE:
                    case FLOAT:
                    case INTEGER:
                    case NUMBER:
                    case DATE:
                        metadata.add(value);
                        break;
                    default:
                        metadata.add(value);
                        break;
                }
            }
        }

        return metadata.toArray();
    }

    /**
     * Gets the metadata header.
     *
     * @return the metadata header
     */
    @SuppressWarnings("PMD.AvoidUsingShortType")
    private static List<DBFColumnInfo> getMetadataHeader()
    {
        List<DBFColumnInfo> metadataHeader = New.list(2);
        metadataHeader.add(new DBFColumnInfo(ImportExportHeader.TITLE.getTitle(), DbfFieldType.CHARACTER.getValue(), (short)128));
        metadataHeader
                .add(new DBFColumnInfo(ImportExportHeader.DESCRIPTION.getTitle(), DbfFieldType.CHARACTER.getValue(), (short)255));
        metadataHeader
                .add(new DBFColumnInfo(ImportExportHeader.ALTITUDE_FLAG.getTitle(), DbfFieldType.LOGICAL.getValue(), (short)1));

        metadataHeader.add(new DBFColumnInfo(ImportExportHeader.DOT_FLAG.getTitle(), DbfFieldType.LOGICAL.getValue(), (short)1));
        metadataHeader
                .add(new DBFColumnInfo(ImportExportHeader.BALLOON_FLAG.getTitle(), DbfFieldType.LOGICAL.getValue(), (short)1));
        // bubble field checkboxes:
        metadataHeader
                .add(new DBFColumnInfo(ImportExportHeader.TITLE_FLAG.getTitle(), DbfFieldType.LOGICAL.getValue(), (short)1));
        metadataHeader.add(
                new DBFColumnInfo(ImportExportHeader.DESCRIPTION_FLAG.getTitle(), DbfFieldType.LOGICAL.getValue(), (short)1));
        metadataHeader.add(
                new DBFColumnInfo(ImportExportHeader.FIELD_TITLE_FLAG.getTitle(), DbfFieldType.LOGICAL.getValue(), (short)1));
        metadataHeader.add(new DBFColumnInfo(ImportExportHeader.MGRS_FLAG.getTitle(), DbfFieldType.LOGICAL.getValue(), (short)1));
        metadataHeader.add(
                new DBFColumnInfo(ImportExportHeader.DMS_LAT_LON_FLAG.getTitle(), DbfFieldType.LOGICAL.getValue(), (short)1));
        metadataHeader.add(
                new DBFColumnInfo(ImportExportHeader.DEC_LAT_LON_FLAG.getTitle(), DbfFieldType.LOGICAL.getValue(), (short)1));
        metadataHeader.add(
                new DBFColumnInfo(ImportExportHeader.BALLOON_FILLED_FLAG.getTitle(), DbfFieldType.LOGICAL.getValue(), (short)1));

        // time checkboxes
        metadataHeader
                .add(new DBFColumnInfo(ImportExportHeader.TIMELINE_FLAG.getTitle(), DbfFieldType.LOGICAL.getValue(), (short)1));
        metadataHeader
                .add(new DBFColumnInfo(ImportExportHeader.ANIMATE_FLAG.getTitle(), DbfFieldType.LOGICAL.getValue(), (short)1));

        metadataHeader.add(new DBFColumnInfo(ImportExportHeader.ALTITUDE.getTitle(), DbfFieldType.DOUBLE.getValue(), (short)64));
        metadataHeader
                .add(new DBFColumnInfo(ImportExportHeader.START_TIME.getTitle(), DbfFieldType.CHARACTER.getValue(), (short)32));
        metadataHeader
                .add(new DBFColumnInfo(ImportExportHeader.END_TIME.getTitle(), DbfFieldType.CHARACTER.getValue(), (short)32));
        metadataHeader
                .add(new DBFColumnInfo(ImportExportHeader.FONT_NAME.getTitle(), DbfFieldType.CHARACTER.getValue(), (short)32));
        metadataHeader.add(new DBFColumnInfo(ImportExportHeader.FONT_SIZE.getTitle(), DbfFieldType.INTEGER.getValue(), (short)3));
        metadataHeader
                .add(new DBFColumnInfo(ImportExportHeader.FONT_STYLE.getTitle(), DbfFieldType.INTEGER.getValue(), (short)1));
        metadataHeader
                .add(new DBFColumnInfo(ImportExportHeader.FONT_COLOR.getTitle(), DbfFieldType.CHARACTER.getValue(), (short)10));
        metadataHeader.add(
                new DBFColumnInfo(ImportExportHeader.BALLOON_COLOR.getTitle(), DbfFieldType.CHARACTER.getValue(), (short)10));

        metadataHeader.add(new DBFColumnInfo(ImportExportHeader.X_OFFSET.getTitle(), DbfFieldType.INTEGER.getValue(), (short)32));
        metadataHeader.add(new DBFColumnInfo(ImportExportHeader.Y_OFFSET.getTitle(), DbfFieldType.INTEGER.getValue(), (short)32));

        return metadataHeader;
    }

    /**
     * Gets a list of points from a list of KML coordinates.
     *
     * @param coordinates the KML coordinates
     * @return the list of points
     */
    private static List<ExtendedCoordinate> getPointListJTS(List<Coordinate> coordinates)
    {
        List<ExtendedCoordinate> pointList = New.list(coordinates.size());
        for (Coordinate coordinate : coordinates)
        {
            pointList.add(convertKml(coordinate));
        }
        return pointList;
    }

    /**
     * Gets a list of points from a list of KML coordinates.
     *
     * @param coordinates the KML coordinates
     * @return the list of points
     */
    private static List<Point2D.Double> getPointList(List<Coordinate> coordinates)
    {
        List<Point2D.Double> pointList = New.list(coordinates.size());
        for (Coordinate coordinate : coordinates)
        {
            pointList.add(new Point2D.Double(coordinate.getLongitude(), coordinate.getLatitude()));
        }
        return pointList;
    }

    /**
     * Converts the supplied KML {@link Coordinate} object to a JTS
     * {@link ExtendedCoordinate} object. Defaults the 'M' value of the returned
     * coordinate to {@link Double#NaN}.
     *
     * @param pCoordinate the coordinate to convert.
     * @return the JTS coordinate populated with data from the supplied KML
     *         coordinate.
     */
    private static ExtendedCoordinate convertKml(Coordinate pCoordinate)
    {
        return new ExtendedCoordinate(pCoordinate.getLongitude(), pCoordinate.getLatitude(), pCoordinate.getAltitude(),
                Double.NaN);
    }

    /**
     * Converts a KML Point into a shapefile PointRecord.
     *
     * @param point the KML Point
     * @return the shapefile PointRecord
     */
    private static PointRecord getPointRecord(Point point)
    {
        ExtendedCoordinate coordinate = getPointListJTS(point.getCoordinates()).get(0);

        if (coordinate.z != 0.0D)
        {
            // use altitude:
            return new PointZRecord(coordinate.x, coordinate.y, coordinate.z, Double.NaN);
        }

        return new PointRecord(coordinate.x, coordinate.y);
    }

    /**
     * Converts a KML Polygon into a shapefile PolygonRecord.
     *
     * @param polygon the KML Polygon
     * @return the shapefile PolygonRecord
     */
    private static PolygonRecord getPolygonRecord(Polygon polygon)
    {
        // For shape files, it is required that exterior rings wind clockwise
        // and interior rings wind counter-clockwise.
        List<Point2D.Double> allPositions = New.list(getPointList(polygon.getOuterBoundaryIs().getLinearRing().getCoordinates()));
        if (!isClockwise(allPositions))
        {
            Collections.reverse(allPositions);
        }
        if (CollectionUtilities.hasContent(polygon.getInnerBoundaryIs()))
        {
            for (Boundary ring : polygon.getInnerBoundaryIs())
            {
                List<Point2D.Double> innerRing = New.list(getPointList(ring.getLinearRing().getCoordinates()));
                if (isClockwise(innerRing))
                {
                    Collections.reverse(innerRing);
                }
                allPositions.addAll(innerRing);
            }
        }
        return new PolygonRecord(allPositions);
    }

    /**
     * Converts a KML Track into a shapefile PolyLineRecord.
     *
     * @param track the KML Track
     * @return the shapefile PolyLineRecord
     */
    private static PolyLineRecord getPolyLineRecord(Track track)
    {
        return new PolyLineRecord(getPointList(track.getCoordinates()));
    }

    /**
     * Determine whether the winding order for the ring is clockwise.
     *
     * @param ring the ring for which the winding order is desired.
     * @return true when the winding order is clockwise.
     */
    private static boolean isClockwise(List<Point2D.Double> ring)
    {
        if (ring.size() < 3)
        {
            return false;
        }

        double sum = 0.;
        Point2D.Double previous = ring.get(0);
        for (int i = 1; i < ring.size(); ++i)
        {
            Point2D.Double current = ring.get(i);
            sum += (current.x - previous.x) * (current.y + previous.y);
            previous = current;
        }
        return sum > 0.;
    }

    /**
     * Writes the placemarks to the file.
     *
     * @param file the file
     * @param placemarks the placemarks
     * @param geomType the geometry type to export
     * @return the file written to
     * @throws IOException Signals that an I/O exception has occurred
     */
    private static File writeToFile(File file, Collection<Placemark> placemarks, GeomType geomType) throws IOException
    {
        Utilities.checkNull(file, "file");
        Utilities.checkNull(placemarks, "placemarks");

        // Create the shape file object
        ESRIShapefile shapeFile = new ESRIShapefile(Mode.WRITE, file.getAbsolutePath());
        shapeFile.setMetadataHeader(getMetadataHeader());
        for (Placemark placemark : placemarks)
        {
            if (placemark.getGeometry() != null && placemark.getGeometry().getClass() == geomType.myGeomType)
            {
                ShapeRecord shapeRecord = null;
                if (placemark.getGeometry() instanceof Point)
                {
                    shapeRecord = getPointRecord((Point)placemark.getGeometry());
                }
                else if (placemark.getGeometry() instanceof Track)
                {
                    shapeRecord = getPolyLineRecord((Track)placemark.getGeometry());
                }
                else if (placemark.getGeometry() instanceof Polygon)
                {
                    shapeRecord = getPolygonRecord((Polygon)placemark.getGeometry());
                }

                if (shapeRecord != null)
                {
                    shapeFile.add(new ShapefileRecord(shapeRecord, getMetadata(placemark, shapeFile.getMetadataHeader())));
                }
            }
        }

        // Write to the file system
        try
        {
            shapeFile.doFinalWrite();
        }
        finally
        {
            try
            {
                shapeFile.close();
            }
            catch (IOException e)
            {
                LOGGER.warn(e);
            }
        }
        return file;
    }

    @Override
    public File export(File file) throws IOException
    {
        File actualFile = null;
        Collection<Placemark> placemarks = ExporterUtilities.getPlacemarks(getObjects());

        GeomType geomType = getGeomType(placemarks);

        if (geomType != null)
        {
            actualFile = writeToFile(getExportFiles(file).iterator().next(), placemarks, geomType);
        }
        return actualFile;
    }

    @Override
    public Collection<? extends File> getExportFiles(File file)
    {
        // Validate file extension (add if missing)
        String path = file.getAbsolutePath().toLowerCase();
        boolean found = false;
        String[] extensions = getMimeType().getFileExtensions();
        for (String extension : extensions)
        {
            if (path.endsWith("." + extension))
            {
                found = true;
                break;
            }
        }

        String base = found ? file.getAbsolutePath().substring(0, file.getAbsolutePath().length() - 4) : file.getAbsolutePath();

        Collection<File> results = New.collection(4);
        results.add(found ? file : new File(base + ESRIShapefile.POSTFIX_SHP));
        results.add(new File(base + ESRIShapefile.POSTFIX_DBF));
        results.add(new File(base + ESRIShapefile.POSTFIX_PRJ));
        results.add(new File(base + ESRIShapefile.POSTFIX_SHX));

        return results;
    }

    @Override
    public MimeType getMimeType()
    {
        return MimeType.SHAPE;
    }

    /** Geometry type enum. */
    private enum GeomType
    {
        /** POINT. */
        POINT("Points", Point.class),

        /** ROI. */
        ROI("ROIs", Polygon.class),

        /** TRACK. */
        TRACK("Tracks", Track.class);

        /** The display name. */
        private final String myDisplayName;

        /** The geometry class type. */
        private final Class<? extends Geometry> myGeomType;

        /**
         * Gets a GeomType from a class object.
         *
         * @param geomType The geometry class type
         * @return the GeomType or null
         */
        public static GeomType fromClass(Class<? extends Geometry> geomType)
        {
            GeomType type = null;
            for (GeomType value : GeomType.values())
            {
                if (value.myGeomType == geomType)
                {
                    type = value;
                    break;
                }
            }
            return type;
        }

        /**
         * Constructor.
         *
         * @param displayName The display name
         * @param geomType The geometry class type
         */
        GeomType(String displayName, Class<? extends Geometry> geomType)
        {
            myDisplayName = displayName;
            myGeomType = geomType;
        }

        @Override
        public String toString()
        {
            return myDisplayName;
        }
    }
}

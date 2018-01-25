package io.opensphere.myplaces.importer;

import java.awt.Color;
import java.awt.Font;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import de.micromata.opengis.kml.v_2_2_0.Folder;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import io.opensphere.core.Toolbox;
import io.opensphere.core.common.shapefile.shapes.PointRecord;
import io.opensphere.core.common.shapefile.shapes.PointZRecord;
import io.opensphere.core.common.shapefile.shapes.PolyLineRecord;
import io.opensphere.core.common.shapefile.shapes.PolygonRecord;
import io.opensphere.core.common.shapefile.utils.ShapefileRecord;
import io.opensphere.core.common.shapefile.v2.ESRIShapefile;
import io.opensphere.core.common.shapefile.v2.ESRIShapefile.Mode;
import io.opensphere.core.importer.ImportCallback;
import io.opensphere.core.mgrs.MGRSConverter;
import io.opensphere.core.mgrs.UTM;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.util.DateTimeUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.collections.StreamUtilities;
import io.opensphere.core.util.filesystem.FileUtilities;
import io.opensphere.core.util.model.GeographicUtilities;
import io.opensphere.mantle.mp.impl.DefaultMapAnnotationPoint;
import io.opensphere.mantle.mp.impl.DefaultMapAnnotationPointSettings;
import io.opensphere.myplaces.constants.ImportExportHeader;
import io.opensphere.myplaces.models.MyPlacesModel;
import io.opensphere.myplaces.specific.points.utils.PointUtils;
import io.opensphere.myplaces.specific.regions.utils.RegionUtils;
import io.opensphere.tracktool.model.TrackNode;
import io.opensphere.tracktool.model.impl.DefaultTrack;
import io.opensphere.tracktool.model.impl.DefaultTrackNode;
import io.opensphere.tracktool.util.TrackUtils;

/**
 * MyPlaces Shape File importer.
 */
public class MyPlacesShapeFileImporter extends AbstractMyPlacesImporter
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(MyPlacesShapeFileImporter.class);

    /** The supported file extensions. */
    private static final List<String> ourFileExtensions = New.unmodifiableList("shp");

    /**
     * Create a shape file reader. Note that this does not actually read in the
     * file, records are read from as necessary.
     *
     * @param shapefile The path to the file.
     * @return The shape file reader.
     */
    public static ESRIShapefile readFile(String shapefile)
    {
        InputStream inputStream = null;
        try
        {
            if (shapefile.toLowerCase().endsWith(".zip"))
            {
                inputStream = new FileInputStream(shapefile);
                List<String> files = FileUtilities.explodeZip(inputStream, null, shapefile.hashCode());
                for (String file : files)
                {
                    if (file.toLowerCase().endsWith(".shp"))
                    {
                        return new ESRIShapefile(Mode.READ, file);
                    }
                }
            }
            else
            {
                return new ESRIShapefile(ESRIShapefile.Mode.READ, shapefile);
            }
        }
        catch (FileNotFoundException e)
        {
            LOGGER.error("Could not read shapefile." + e, e);
        }
        finally
        {
            try
            {
                if (inputStream != null)
                {
                    inputStream.close();
                }
            }
            catch (IOException e)
            {
                LOGGER.error("Failed to close stream." + e, e);
            }
        }

        return null;
    }

    /**
     * Gets the description.
     *
     * @param headerIndices the header indices
     * @param metadata the metadata array
     * @return the description
     */
    private static String getDescription(int[] headerIndices, Object[] metadata)
    {
        Object desc = getValue(headerIndices, metadata, DESC_KEY);
        return desc instanceof String ? (String)desc : null;
    }

    /**
     * Gets the name.
     *
     * @param headerIndices the header indices
     * @param metadata the metadata array
     * @return the name
     */
    private static String getName(int[] headerIndices, Object[] metadata)
    {
        Object title = getValue(headerIndices, metadata, TITLE_KEY);
        return title instanceof String ? (String)title : "UNKNOWN";
    }

    /**
     * Gets the value for the header specified by index in the metadata array.
     *
     * @param headerIndices the header indices
     * @param metadata the metadata array
     * @param key the header key to look up
     * @return the value
     */
    private static Object getValue(int[] headerIndices, Object[] metadata, int key)
    {
        int headerIndex = headerIndices[key];
        return headerIndex != -1 ? metadata[headerIndex] : null;
    }

    /**
     * Constructor.
     *
     * @param toolbox the toolbox
     * @param model the model
     */
    public MyPlacesShapeFileImporter(Toolbox toolbox, MyPlacesModel model)
    {
        super(toolbox, model);
    }

    @Override
    public int getPrecedence()
    {
        return 200;
    }

    @Override
    public List<String> getSupportedFileExtensions()
    {
        return ourFileExtensions;
    }

    @Override
    public void importFile(File aFile, ImportCallback callback)
    {
        ESRIShapefile shapeFile = readFile(aFile.getAbsolutePath());
        Folder folder = getFolderWithPlacemarks(shapeFile, aFile.getName());
        addFolderOrFail(folder, aFile);
    }

    /**
     * Converts the metadata array into a map point.
     *
     * @param headers the set of headers extracted from the database file,
     *            listed in the order found from the source file. The order
     *            corresponds with the order of the metadata values extracted
     *            from the rows, and the index of a given header within the list
     *            is inferred as its columnar position within the shapefile's
     *            data table.
     * @param metadata the metadata array
     * @param shapePoint the shapefile point
     * @return the map point
     */
    private DefaultMapAnnotationPoint createMapPoint(List<ImportExportHeader> headers, Object[] metadata, PointRecord shapePoint)
    {
        DefaultMapAnnotationPoint point = new DefaultMapAnnotationPoint(getPointRegistry().getUserDefaultPoint());

        Date startDate = null;
        Date endDate = null;
        int fontSize = 12;
        String fontName = null;
        int fontStyle = 0;

        for (int i = 0; i < headers.size(); i++)
        {
            ImportExportHeader header = headers.get(i);
            if (header != null)
            {
                DefaultMapAnnotationPointSettings settings = (DefaultMapAnnotationPointSettings)point.getAnnoSettings();
                switch (header)
                {
                    case DOT_FLAG:
                        point.setVisible(StringUtils.equals("t", (String)metadata[i]), this);
                        break;
                    case BALLOON_FLAG:
                        settings.setAnnohide(StringUtils.equals("t", (String)metadata[i]), this);
                        break;
                    case TIMELINE_FLAG:

                        break;
                    case ANIMATE_FLAG:

                        break;
                    case HEADING_FLAG:
                        settings.setHeading(StringUtils.equals("t", (String)metadata[i]), this);
                        break;
                    case VELOCITY_FLAG:
                        settings.setVelocity(StringUtils.equals("t", (String)metadata[i]), this);
                        break;
                    case DISTANCE_FLAG:
                        settings.setDistance(StringUtils.equals("t", (String)metadata[i]), this);
                        break;
                    case ALTITUDE_FLAG:
                        settings.setAltitude(StringUtils.equals("t", (String)metadata[i]), this);
                        break;
                    case TITLE_FLAG:
                        settings.setTitle(StringUtils.equals("t", (String)metadata[i]), this);
                        break;
                    case FIELD_TITLE_FLAG:
                        settings.setFieldTitle(StringUtils.equals("t", (String)metadata[i]), this);
                        break;
                    case DESCRIPTION_FLAG:
                        settings.setDesc(StringUtils.equals("t", (String)metadata[i]), this);
                        break;
                    case DMS_LAT_LON_FLAG:
                        settings.setDms(StringUtils.equals("t", (String)metadata[i]), this);
                        break;
                    case DEC_LAT_LON_FLAG:
                        settings.setLatLon(StringUtils.equals("t", (String)metadata[i]), this);
                        break;
                    case MGRS_FLAG:
                        settings.setMgrs(StringUtils.equals("t", (String)metadata[i]), this);
                        break;
                    case BALLOON_FILLED_FLAG:
                        point.setFilled(StringUtils.equals("t", (String)metadata[i]), this);
                        break;
                    case TITLE:
                        point.setTitle((String)metadata[i], this);
                        break;
                    case DESCRIPTION:
                        point.setDescription((String)metadata[i], this);
                        break;
                    case X_OFFSET:
                        point.setxOffset(Integer.parseInt((String)metadata[i]), this);
                        break;
                    case Y_OFFSET:
                        point.setyOffset(Integer.parseInt((String)metadata[i]), this);
                        break;
                    case MAP_VISUALIZATION_TYPE:

                        break;
                    case FONT_STYLE:
                        fontStyle = Integer.parseInt((String)metadata[i]);
                        break;
                    case FONT_NAME:
                        fontName = (String)metadata[i];
                        break;
                    case FONT_SIZE:
                        fontSize = Integer.parseInt((String)metadata[i]);
                        break;
                    case FONT_COLOR:
                        String value = (String)metadata[i];
                        if (StringUtils.isNotBlank(value))
                        {
                            // color comes as 0xAABBGGRR
                            value = value.replace("0x", "");
                            int alpha = Integer.parseInt(value.substring(0, 2), 16);
                            int blue = Integer.parseInt(value.substring(2, 4), 16);
                            int green = Integer.parseInt(value.substring(4, 6), 16);
                            int red = Integer.parseInt(value.substring(6, 8), 16);

                            Color parsedColor = new Color(red, green, blue, alpha);
                            point.setFontColor(parsedColor, this);
                        }
                        break;
                    case BALLOON_COLOR:
                        String balloonValue = (String)metadata[i];
                        if (StringUtils.isNotBlank(balloonValue))
                        {
                            // color comes as 0xAABBGGRR
                            balloonValue = balloonValue.replace("0x", "");
                            int alpha = Integer.parseInt(balloonValue.substring(0, 2), 16);
                            int blue = Integer.parseInt(balloonValue.substring(2, 4), 16);
                            int green = Integer.parseInt(balloonValue.substring(4, 6), 16);
                            int red = Integer.parseInt(balloonValue.substring(6, 8), 16);

                            Color parsedColor = new Color(red, green, blue, alpha);
                            point.setBackgroundColor(parsedColor, this);
                            point.setColor(parsedColor, this);
                        }
                        break;
                    case ASSOCIATED_VIEW:
                        point.setAssociatedViewName((String)metadata[i], this);
                        break;
                    case START_TIME:
                        try
                        {
                            startDate = DateTimeUtilities.parseISO8601Date((String)metadata[i]);
                        }
                        catch (ParseException e)
                        {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        break;
                    case END_TIME:
                        try
                        {
                            endDate = DateTimeUtilities.parseISO8601Date((String)metadata[i]);
                        }
                        catch (ParseException e)
                        {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        break;
                    case ALTITUDE:
                        if (StringUtils.isNotBlank((String)metadata[i]))
                        {
                            point.setAltitude(Double.parseDouble((String)metadata[i]), this);
                        }
                        break;
                    default:
                        LOGGER.warn("Unrecognized field type: " + header.name());
                        break;
                }
            }
        }

        if (startDate != null)
        {
            point.setTimeEnabled(true, this);
            io.opensphere.core.model.time.TimeSpan time = null;
            if (endDate != null)
            {
                time = io.opensphere.core.model.time.TimeSpan.get(startDate, endDate);
            }
            else
            {
                time = io.opensphere.core.model.time.TimeSpan.get(startDate);
            }

            point.setTime(time);
        }

        point.setFont(new Font(fontName, fontStyle, fontSize), this);
        point.setLat(shapePoint.getY(), this);
        point.setLon(shapePoint.getX(), this);
        if (shapePoint instanceof PointZRecord)
        {
            point.setAltitude(((PointZRecord)shapePoint).getZ(), this);
        }

        if (point.getAnnoSettings().isMgrs())
        {
            LatLonAlt location = LatLonAlt.createFromDegrees(point.getLat(), point.getLon());
            GeographicPosition gp = new GeographicPosition(location);
            MGRSConverter converter = new MGRSConverter();
            UTM utmCoords = new UTM(gp);
            point.setMGRS(converter.createString(utmCoords), this);
        }

        return point;
    }

    /**
     * Converts the metadata array into a track.
     *
     * @param headerIndices the header indices
     * @param metadata the metadata array
     * @param shapePolyLine the shapefile polyline
     * @return the track
     */
    private DefaultTrack createTrack(int[] headerIndices, Object[] metadata, PolyLineRecord shapePolyLine)
    {
        Collection<LatLonAlt> points = getLatLonAlts(shapePolyLine.getPoints());

        List<TrackNode> nodes = New.list(points.size());
        for (LatLonAlt point : points)
        {
            nodes.add(new DefaultTrackNode(point));
        }

        return TrackUtils.createDefaultTrack(getToolbox(), null, getName(headerIndices, metadata), nodes);
    }

    /**
     * Converts the ESRIShapefile into placemarks under a folder.
     *
     * @param shapeFile the ESRIShapefile
     * @param folderName the name of the folder
     * @return the folder
     */
    private Folder getFolderWithPlacemarks(ESRIShapefile shapeFile, String folderName)
    {
        if (shapeFile == null)
        {
            return null;
        }

        Folder folder = new Folder();
        folder.setName(folderName);
        folder.setVisibility(Boolean.TRUE);

        List<ImportExportHeader> headerEnums = StreamUtilities.map(shapeFile.getMetadataHeader(),
                input -> ImportExportHeader.getByPrefix(input.fieldName));
        List<String> headers = StreamUtilities.map(shapeFile.getMetadataHeader(), input -> input.fieldName);

        int[] headerIndices = getHeaderIndices(headers);

        for (ShapefileRecord record : shapeFile)
        {
            Placemark placemark = null;
            if (record.shape instanceof PointRecord)
            {
                PointRecord point = (PointRecord)record.shape;

                DefaultMapAnnotationPoint mapPoint = createMapPoint(headerEnums, record.metadata, point);
                placemark = PointUtils.toKml(folder, mapPoint);
            }
            else if (record.shape instanceof PolyLineRecord)
            {
                PolyLineRecord polyLine = (PolyLineRecord)record.shape;

                DefaultTrack track = createTrack(headerIndices, record.metadata, polyLine);
                placemark = TrackUtils.toKml(folder, track);
            }
            else if (record.shape instanceof PolygonRecord)
            {
                PolygonRecord polygon = (PolygonRecord)record.shape;

                String name = getName(headerIndices, record.metadata);
                List<LatLonAlt> points = getLatLonAlts(polygon.getPoints());
                Map<List<LatLonAlt>, Collection<List<LatLonAlt>>> parts = GeographicUtilities
                        .decomposePositionsToPolygons(points);
                Entry<List<LatLonAlt>, Collection<List<LatLonAlt>>> part = parts.entrySet().iterator().next();
                placemark = RegionUtils.createRegionFromLLAs(folder, name, part.getKey(), part.getValue());
                placemark.setDescription(getDescription(headerIndices, record.metadata));
            }

            if (placemark != null)
            {
                folder.addToFeature(placemark);
            }
        }

        return folder;
    }

    /**
     * Converts an array of shape file points to LatLonAlts.
     *
     * @param shapePoints the shape file points
     * @return the LatLonAlts
     */
    private List<LatLonAlt> getLatLonAlts(Point2D.Double[] shapePoints)
    {
        List<LatLonAlt> points = New.list(shapePoints.length);
        for (Point2D.Double point : shapePoints)
        {
            points.add(LatLonAlt.createFromDegrees(point.y, point.x));
        }
        return points;
    }
}

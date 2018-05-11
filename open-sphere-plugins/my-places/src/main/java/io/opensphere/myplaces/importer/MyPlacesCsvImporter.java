package io.opensphere.myplaces.importer;

import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.apache.log4j.Logger;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

import de.micromata.opengis.kml.v_2_2_0.Folder;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import io.opensphere.core.Toolbox;
import io.opensphere.core.importer.ImportCallback;
import io.opensphere.core.mgrs.MGRSConverter;
import io.opensphere.core.mgrs.UTM;
import io.opensphere.core.model.Altitude.ReferenceLevel;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.util.DateTimeUtilities;
import io.opensphere.core.util.QuotingBufferedReader;
import io.opensphere.core.util.jts.JTSUtilities;
import io.opensphere.core.util.lang.Pair;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.mantle.mp.impl.DefaultMapAnnotationPoint;
import io.opensphere.mantle.mp.impl.DefaultMapAnnotationPointSettings;
import io.opensphere.mantle.util.StringUtils;
import io.opensphere.myplaces.constants.Constants;
import io.opensphere.myplaces.constants.ImportExportHeader;
import io.opensphere.myplaces.models.MyPlacesModel;
import io.opensphere.myplaces.specific.points.utils.PointUtils;
import io.opensphere.myplaces.specific.regions.utils.RegionUtils;
import io.opensphere.tracktool.model.TrackNode;
import io.opensphere.tracktool.model.impl.DefaultTrackNode;
import io.opensphere.tracktool.util.TrackUtils;

/**
 * MyPlaces CSV importer.
 */
public class MyPlacesCsvImporter extends AbstractMyPlacesImporter
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(MyPlacesCsvImporter.class);

    /** Quote character for QuotingBufferedReader (cf. getInput). */
    private static final char QUOTE = '"';

    /**
     * Create the geometry from the WKT.
     *
     * @param wkt the text which represents the geometry.
     * @return the newly created geometry
     */
    private static Geometry createGeometry(String wkt)
    {
        try
        {
            return new WKTReader().read(wkt);
        }
        catch (ParseException e)
        {
            LOGGER.error("Failed to read WKT geometry." + e, e);
            return null;
        }
    }

    /**
     * Reads the file.
     *
     * @param file the file
     * @return the header and data
     */
    private static Pair<String[], List<String[]>> readFile(File file)
    {
        String[] headers = null;
        List<String[]> data = new LinkedList<>();
        try (QuotingBufferedReader lineReader = getInput(file))
        {
            while (lineReader.ready())
            {
                String[] row = StringUtils.parseCsv(lineReader.readLine());
                if (headers == null)
                {
                    headers = row;
                }
                else
                {
                    data.add(row);
                }
            }
        }
        catch (IOException e)
        {
            LOGGER.error(e, e);
        }
        return new Pair<>(headers, data);
    }

    /**
     * Constructor.
     *
     * @param toolbox the toolbox
     * @param model the model
     */
    public MyPlacesCsvImporter(Toolbox toolbox, MyPlacesModel model)
    {
        super(toolbox, model);
    }

    @Override
    public int getPrecedence()
    {
        return 300;
    }

    @Override
    public List<String> getSupportedFileExtensions()
    {
        return Arrays.asList("csv");
    }

    @Override
    public void importFile(File aFile, ImportCallback callback)
    {
        Pair<String[], List<String[]>> csvData = readFile(aFile);
        Folder folder = createFolder(csvData.getFirstObject(), csvData.getSecondObject(), aFile.getName());
        addFolderOrFail(folder, aFile);
    }

    /**
     * Construct a DefaultMapAnnotationPoint from data contained in a row from
     * the CSV input.
     *
     * @param row a row of CSV data values
     * @param fMap a Map of column headers to numerical indices
     * @return the derived construct
     */
    private DefaultMapAnnotationPoint defMapPoint(String[] row, Map<String, Integer> fMap)
    {
        DefaultMapAnnotationPoint pt = new DefaultMapAnnotationPoint(getPointRegistry().getUserDefaultPoint());
        DefaultMapAnnotationPointSettings ss = (DefaultMapAnnotationPointSettings)pt.getAnnoSettings();
        doNonNull(b -> pt.setVisible(b.booleanValue(), null), metaBool(ImportExportHeader.DOT_FLAG, row, fMap));
        doNonNull(b -> ss.setAnnohide(b.booleanValue(), null), metaBool(ImportExportHeader.BALLOON_FLAG, row, fMap));
        doNonNull(b -> ss.setHeading(b.booleanValue(), null), metaBool(ImportExportHeader.HEADING_FLAG, row, fMap));
        doNonNull(b -> ss.setVelocity(b.booleanValue(), null), metaBool(ImportExportHeader.VELOCITY_FLAG, row, fMap));
        doNonNull(b -> ss.setDistance(b.booleanValue(), null), metaBool(ImportExportHeader.DISTANCE_FLAG, row, fMap));
        doNonNull(b -> ss.setAltitude(b.booleanValue(), null), metaBool(ImportExportHeader.ALTITUDE_FLAG, row, fMap));
        doNonNull(b -> ss.setTitle(b.booleanValue(), null), metaBool(ImportExportHeader.TITLE_FLAG, row, fMap));
        doNonNull(b -> ss.setFieldTitle(b.booleanValue(), null), metaBool(ImportExportHeader.FIELD_TITLE_FLAG, row, fMap));
        doNonNull(b -> ss.setDesc(b.booleanValue(), null), metaBool(ImportExportHeader.DESCRIPTION_FLAG, row, fMap));
        doNonNull(b -> ss.setDms(b.booleanValue(), null), metaBool(ImportExportHeader.DMS_LAT_LON_FLAG, row, fMap));
        doNonNull(b -> ss.setLatLon(b.booleanValue(), null), metaBool(ImportExportHeader.DEC_LAT_LON_FLAG, row, fMap));
        doNonNull(b -> ss.setMgrs(b.booleanValue(), null), metaBool(ImportExportHeader.MGRS_FLAG, row, fMap));
        doNonNull(b -> pt.setFilled(b.booleanValue(), null), metaBool(ImportExportHeader.BALLOON_FILLED_FLAG, row, fMap));
        doNonNull(b -> pt.setTitle(b, null), metaVal(ImportExportHeader.TITLE, row, fMap));
        doNonNull(b -> pt.setDescription(b, null), metaVal(ImportExportHeader.DESCRIPTION, row, fMap));
        doNonNull(b -> pt.setxOffset(b.intValue(), null), metaInt(ImportExportHeader.X_OFFSET, row, fMap));
        doNonNull(b -> pt.setyOffset(b.intValue(), null), metaInt(ImportExportHeader.Y_OFFSET, row, fMap));
        doNonNull(b -> pt.setFontColor(b, null), parseColor(metaVal(ImportExportHeader.FONT_COLOR, row, fMap)));
        Color balloonColor = parseColor(metaVal(ImportExportHeader.BALLOON_COLOR, row, fMap));
        if (balloonColor != null)
        {
            pt.setBackgroundColor(balloonColor, null);
            pt.setColor(balloonColor, null);
        }
        doNonNull(b -> pt.setAssociatedViewName(b, null), metaVal(ImportExportHeader.ASSOCIATED_VIEW, row, fMap));
        doNonNull(b -> pt.setLat(b.doubleValue(), null), metaDbl(ImportExportHeader.LATITUDE, row, fMap));
        doNonNull(b -> pt.setLon(b.doubleValue(), null), metaDbl(ImportExportHeader.LONGITUDE, row, fMap));
        doNonNull(b -> pt.setAltitude(b.doubleValue(), null), metaDbl(ImportExportHeader.ALTITUDE, row, fMap));

        Date startDate = parseDate(metaVal(ImportExportHeader.START_TIME, row, fMap));
        Date endDate = parseDate(metaVal(ImportExportHeader.END_TIME, row, fMap));
        if (startDate != null)
        {
            pt.setTimeEnabled(true, null);
            if (endDate != null)
            {
                pt.setTime(TimeSpan.get(startDate, endDate));
            }
            else
            {
                pt.setTime(TimeSpan.get(startDate));
            }
        }

        String fontName = metaVal(ImportExportHeader.FONT_NAME, row, fMap);
        Integer fontStyle = metaInt(ImportExportHeader.FONT_STYLE, row, fMap);
        if (fontStyle == null)
        {
            fontStyle = Integer.valueOf(0);
        }
        Integer fontSize = metaInt(ImportExportHeader.FONT_SIZE, row, fMap);
        if (fontSize == null)
        {
            fontSize = Integer.valueOf(12);
        }
        pt.setFont(new Font(fontName, fontStyle.intValue(), fontSize.intValue()), this);

        if (pt.getAnnoSettings().isMgrs())
        {
            LatLonAlt location = LatLonAlt.createFromDegrees(pt.getLat(), pt.getLon());
            UTM utmCoords = new UTM(new GeographicPosition(location));
            pt.setMGRS(new MGRSConverter().createString(utmCoords), this);
        }

        return pt;
    }

    /**
     * Create a Folder and populate it with Placemarks derived from the CSV
     * dataset.
     *
     * @param headers column headers for the CSV
     * @param data rows of values from the CSV
     * @param name Folder name
     * @return a Folder
     */
    private Folder createFolder(String[] headers, List<String[]> data, String name)
    {
        if (headers == null)
        {
            return null;
        }
        Folder f = new Folder();
        f.setName(name);
        f.setVisibility(Boolean.TRUE);

        Map<String, Integer> fMap = mapFields(headers);
        for (String[] row : data)
        {
            doNonNull(p -> f.addToFeature(p), getPlacemark(fMap, row));
        }

        return f;
    }

    /**
     * Create a placemark from a row of values from the CSV input.
     *
     * @param fMap a Map of column name to numerical index
     * @param row a row of input values
     * @return a Placemark
     */
    private Placemark getPlacemark(Map<String, Integer> fMap, String[] row)
    {
        Integer geomIndex = fMap.get(Constants.GEOM_KEY);
        if (geomIndex != null)
        {
            Geometry geom = createGeometry(row[geomIndex.intValue()]);
            String name = metaVal(ImportExportHeader.TITLE, row, fMap);
            String desc = metaVal(ImportExportHeader.DESCRIPTION, row, fMap);
            if (geom instanceof LineString)
            {
                return trackMark(name, (LineString)geom);
            }
            else if (geom instanceof Polygon)
            {
                return polygonMark(name, desc, (Polygon)geom);
            }
        }

        DefaultMapAnnotationPoint pt = defMapPoint(row, fMap);
        if (pt != null)
        {
            return PointUtils.toKml(null, pt);
        }
        return null;
    }

    /**
     * Construct a Placemark with a track-like geometry.
     *
     * @param name name
     * @param geom geometry (a JTS LineString)
     * @return a Placemark
     */
    private Placemark trackMark(String name, LineString geom)
    {
        Collection<LatLonAlt> points = JTSUtilities.convertToLatLonAlt(geom.getCoordinates(), ReferenceLevel.TERRAIN);
        List<TrackNode> nodes = new LinkedList<>();
        for (LatLonAlt point : points)
        {
            nodes.add(new DefaultTrackNode(point));
        }
        return TrackUtils.toKml(TrackUtils.createDefaultTrack(getToolbox(), null, name, nodes));
    }

    /**
     * Construct a Placemark with a Polygon geometry.
     *
     * @param name name
     * @param desc description
     * @param geom geometry (a JTS Polygon)
     * @return a Placemark
     */
    private static Placemark polygonMark(String name, String desc, Polygon geom)
    {
        Pair<List<LatLonAlt>, Collection<List<LatLonAlt>>> rings = JTSUtilities.convertToLatLonAlt(geom, ReferenceLevel.TERRAIN);
        Placemark p = RegionUtils.regionPlacemark(name, rings.getFirstObject(), rings.getSecondObject());
        p.setDescription(desc);
        return p;
    }

    /**
     * Construct a mapping of the column headers to their positions within the
     * array. The result can be used to find the named fields within each row.
     *
     * @param headers the array of column headers
     * @return the mapping of those headers to their numerical indices
     */
    private static Map<String, Integer> mapFields(String[] headers)
    {
        Map<String, Integer> ret = new HashMap<>();
        for (int i = 0; i < headers.length; i++)
        {
            ret.put(headers[i], Integer.valueOf(i));
        }
        return ret;
    }

    /**
     * Extract a Color from a String in the form of "0xAABBGGRR", where "0x" is
     * literal, and 'A', 'B', 'G', and 'R' are the hexadecimal digits
     * respectively of "alpha", "blue", "green", and "red" components of the
     * encoded Color.
     *
     * @param txt a String
     * @return a Color
     */
    private static Color parseColor(String txt)
    {
        if (txt == null)
        {
            return null;
        }
        // by using replace, we allow tolerance of missing "0x", though that is
        // probably unnecessary
        txt = txt.replace("0x", "");
        int alpha = Integer.parseInt(txt.substring(0, 2), 16);
        int blue = Integer.parseInt(txt.substring(2, 4), 16);
        int green = Integer.parseInt(txt.substring(4, 6), 16);
        int red = Integer.parseInt(txt.substring(6, 8), 16);
        return new Color(red, green, blue, alpha);
    }

    /**
     * Turn any non-null String into a Date according to the ISO8601 standard.
     *
     * @param d the text-formatted date
     * @return the encoded Date, if any, or null
     */
    private static Date parseDate(String d)
    {
        try
        {
            if (d != null)
            {
                return DateTimeUtilities.parseISO8601Date(d);
            }
        }
        catch (java.text.ParseException e)
        {
            LOGGER.error(e);
        }
        return null;
    }

    /**
     * Extract the raw String of a field contained in <i>row</i>, if it is
     * present. This is sufficient for String-valued fields, but for others some
     * additional processing may be necessary before use.
     *
     * @param h the enumerated field name
     * @param row the row of data
     * @param fieldMap the mapping of field names to position within <i>row</i>
     * @return the String field value or null
     */
    private static String metaVal(ImportExportHeader h, String[] row, Map<String, Integer> fieldMap)
    {
        Integer index = fieldMap.get(h.getTitle());
        if (index == null)
        {
            return null;
        }
        String str = row[index.intValue()];
        if (str == null || str.trim().isEmpty())
        {
            return null;
        }
        return str;
    }

    /**
     * Extract a field from the <i>row</i> and, if it is present, convert it to
     * an Integer value.
     *
     * @param h the enumerated field name
     * @param row the row of data
     * @param fieldMap the mapping of field names to position within <i>row</i>
     * @return the Integer field value, or null
     */
    private static Integer metaInt(ImportExportHeader h, String[] row, Map<String, Integer> fieldMap)
    {
        String str = metaVal(h, row, fieldMap);
        if (str == null)
        {
            return null;
        }
        return Integer.valueOf(str);
    }

    /**
     * Extract a field from the <i>row</i> and, if it is present, convert it to
     * a Double value.
     *
     * @param h the enumerated field name
     * @param row the row of data
     * @param fieldMap the mapping of field names to position within <i>row</i>
     * @return the Double field value, or null
     */
    private static Double metaDbl(ImportExportHeader h, String[] row, Map<String, Integer> fieldMap)
    {
        String str = metaVal(h, row, fieldMap);
        if (str == null)
        {
            return null;
        }
        return Double.valueOf(str);
    }

    /**
     * Extract a field from the <i>row</i> and, if it is present, convert it to
     * a Boolean value.
     *
     * @param h the enumerated field name
     * @param row the row of data
     * @param fieldMap the mapping of field names to position within <i>row</i>
     * @return the Boolean field value, or null
     */
    private static Boolean metaBool(ImportExportHeader h, String[] row, Map<String, Integer> fieldMap)
    {
        String val = metaVal(h, row, fieldMap);
        if (val == null)
        {
            return null;
        }
        return Boolean.valueOf(val);
    }

    /**
     * A generic means of calling a function that short-circuits on a null
     * argument. This mechanism prevents breakage when the underlying
     * implementation may not be null-tolerant.
     *
     * @param f a function to call
     * @param v an argument for <i>f</i>, if it is not null
     */
    private static <V> void doNonNull(Consumer<V> f, V v)
    {
        if (v != null)
        {
            f.accept(v);
        }
    }

    /**
     * Create a QuotingBufferedReader (q.v.) for to read the specified File.
     *
     * @param f the input File
     * @return the reader
     * @throws FileNotFoundException if the file is not found, don't you know
     */
    private static QuotingBufferedReader getInput(File f) throws FileNotFoundException
    {
        return new QuotingBufferedReader(new InputStreamReader(new FileInputStream(f), StringUtilities.DEFAULT_CHARSET),
                new char[] { QUOTE }, null);
    }
}

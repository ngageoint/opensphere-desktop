package io.opensphere.myplaces.export;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.io.WKTWriter;

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
import io.opensphere.core.util.MimeType;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.kml.gx.Track;
import io.opensphere.myplaces.constants.Constants;
import io.opensphere.myplaces.constants.ImportExportHeader;

/**
 * Exporter from MyPlaces to CSV.
 */
public class MyPlacesCsvExporter extends AbstractMyPlacesExporter
{
    /** The column headers for the CSV output. One copy suffices. */
    private static final String CSV_HEADER = createCsvHeader();

    /**
     * Create the column header String for CSV output. The values of the
     * ImportExportHeader enum are shown in their natural order and will be
     * visited in the same order when a Placemark is expressed as CSV.
     * 
     * @return see above
     */
    private static String createCsvHeader()
    {
        ImportExportHeader[] heads = ImportExportHeader.values();
        if (heads.length == 0)
        {
            return Constants.GEOM_KEY;
        }
        StringBuilder buf = new StringBuilder(heads[0].getTitle());
        for (int i = 1; i < heads.length; i++)
        {
            buf.append("," + heads[i].getTitle());
        }
        buf.append("," + Constants.GEOM_KEY);
        return buf.toString();
    }

    /**
     * Convert a Placemark into a single row of CSV output.
     * 
     * @param p a Placemark
     * @return CSV
     */
    private static String createCsvEntry(Placemark p)
    {
        String wkt = toWKT(p.getGeometry());
        if (wkt == null)
        {
            return null;
        }
        Map<ImportExportHeader, String> meta = getMeta(p);
        StringBuilder buf = new StringBuilder();
        boolean first = true;
        for (ImportExportHeader head : ImportExportHeader.values())
        {
            if (!first)
            {
                buf.append(',');
            }
            first = false;
            String val = meta.get(head);
            if (val != null)
            {
                buf.append(val);
            }
        }
        buf.append(',');
        buf.append('"');
        buf.append(wkt);
        buf.append('"');
        return buf.toString();
    }

    /**
     * Come on, Arthur, get "meta" with me!
     * 
     * @param p a Placemark
     * @return Placemark metadata as a map of enumerated column to value
     */
    private static Map<ImportExportHeader, String> getMeta(Placemark p)
    {
        // collect the relevant data and place it into a Map for easy access;
        // "extended data" is organized in unfortunate fashion typical of XML;
        // other fields accessible from Placemark that are also needed
        Map<ImportExportHeader, String> meta = new LinkedHashMap<>();
        for (Data d : p.getExtendedData().getData())
        {
            ImportExportHeader h = ImportExportHeader.getByTitle(d.getName());
            if (h != null)
            {
                meta.put(h, d.getValue());
            }
        }

        // include the name of the Placemark
        meta.put(ImportExportHeader.TITLE, p.getName());

        // include the description of the Placemark
        meta.put(ImportExportHeader.DESCRIPTION, p.getDescription());

        // include latitude, longitude, and altitude values
        Geometry geom = p.getGeometry();
        if (geom instanceof Point)
        {
            Coordinate c = ((Point)geom).getCoordinates().get(0);
            meta.put(ImportExportHeader.LATITUDE, String.valueOf(c.getLatitude()));
            meta.put(ImportExportHeader.LONGITUDE, String.valueOf(c.getLongitude()));
            // before calling getAltitude, check to see if it is included
            meta.put(ImportExportHeader.ALTITUDE, String.valueOf(c.getAltitude()));
        }

        // include balloon fill and font colors from the Placemark
        for (StyleSelector ss : p.getStyleSelector())
        {
            Style sty = (Style)ss;
            meta.put(ImportExportHeader.BALLOON_COLOR, "0x" + sty.getIconStyle().getColor());
            meta.put(ImportExportHeader.FONT_COLOR, "0x" + sty.getBalloonStyle().getTextColor());
        }

        // include time instant or time span from the Placemark
        TimePrimitive time = p.getTimePrimitive();
        if (time instanceof TimeStamp)
        {
            meta.put(ImportExportHeader.START_TIME, ((TimeStamp)time).getWhen());
        }
        else if (time instanceof TimeSpan)
        {
            TimeSpan span = (TimeSpan)time;
            meta.put(ImportExportHeader.START_TIME, span.getBegin());
            meta.put(ImportExportHeader.END_TIME, span.getEnd());
        }
        return meta;
    }

    /**
     * Converts a KML Geometry to a WKT String via JTS Geometry.
     *
     * @param kmlGeom the KML geometry
     * @return the WKT String
     */
    private static String toWKT(Geometry kmlGeom)
    {
        // Create a JTS geometry
        com.vividsolutions.jts.geom.Geometry jtsGeom = null;
        GeometryFactory f = new GeometryFactory();
        if (kmlGeom instanceof Point)
        {
            Point point = (Point)kmlGeom;
            com.vividsolutions.jts.geom.Coordinate[] jtsCoordinates = toJts(point.getCoordinates());
            if (jtsCoordinates.length > 0)
            {
                jtsGeom = f.createPoint(jtsCoordinates[0]);
            }
        }
        else if (kmlGeom instanceof Track)
        {
            jtsGeom = f.createLineString(toJts(((Track)kmlGeom).getCoordinates()));
        }
        else if (kmlGeom instanceof Polygon)
        {
            Polygon poly = (Polygon)kmlGeom;
            LinearRing outer = f.createLinearRing(toJts(poly.getOuterBoundaryIs().getLinearRing().getCoordinates()));
            jtsGeom = f.createPolygon(outer, toJtsRings(poly.getInnerBoundaryIs()));
        }
        // Write to WKT, if possible
        if (jtsGeom == null)
        {
            return null;
        }
        return new WKTWriter(3).write(jtsGeom);
    }

    /**
     * Converts a list of KML coordinates to JTS coordinates.
     *
     * @param kml the KML coordinates
     * @return The JTS coordinates
     */
    private static com.vividsolutions.jts.geom.Coordinate[] toJts(List<Coordinate> kml)
    {
        com.vividsolutions.jts.geom.Coordinate[] ret = new com.vividsolutions.jts.geom.Coordinate[kml.size()];
        int i = 0;
        for (Coordinate k : kml)
        {
            ret[i++] = toJts(k);
        }
        return ret;
    }

    /**
     * Converts a KML Coordinate to a JTS Coordinate. How do you like that?
     * 
     * @param k KML Coordinate
     * @return JTS Coordinate
     */
    private static com.vividsolutions.jts.geom.Coordinate toJts(Coordinate k)
    {
        return new com.vividsolutions.jts.geom.Coordinate(k.getLongitude(), k.getLatitude(), k.getAltitude());
    }

    /**
     * Convert a List of KML Boundary objects into a corresponding array of JTS
     * LinearRings. If the argument is null or empty, then this method returns
     * null.
     * 
     * @param bnds List of KML Boundary
     * @return array of JTS LinearRing
     */
    private static LinearRing[] toJtsRings(List<Boundary> bnds)
    {
        if (bnds == null || bnds.isEmpty())
        {
            return null;
        }
        GeometryFactory f = new GeometryFactory();
        LinearRing[] rings = new LinearRing[bnds.size()];
        int i = 0;
        for (Boundary b : bnds)
        {
            rings[i++] = f.createLinearRing(toJts(b.getLinearRing().getCoordinates()));
        }
        return rings;
    }

    /**
     * Writes the placemarks to the file.
     *
     * @param file the file
     * @param placemarks the placemarks
     * @return the file written to
     * @throws IOException Signals that an I/O exception has occurred
     */
    private static File writeToFile(File file, Collection<Placemark> placemarks) throws IOException
    {
        Utilities.checkNull(file, "file");
        Utilities.checkNull(placemarks, "placemarks");

        PrintWriter writer = new PrintWriter(
                new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StringUtilities.DEFAULT_CHARSET)));
        try
        {
            writer.println(CSV_HEADER);
            for (Placemark placemark : placemarks)
            {
                String entry = createCsvEntry(placemark);
                if (entry != null)
                {
                    writer.println(entry);
                }
            }
        }
        finally
        {
            writer.flush();
            writer.close();
        }
        return file;
    }

    @Override
    public File export(File file) throws IOException
    {
        return writeToFile(getExportFiles(file).iterator().next(), ExporterUtilities.getPlacemarks(getObjects()));
    }

    @Override
    public MimeType getMimeType()
    {
        return MimeType.CSV;
    }
}

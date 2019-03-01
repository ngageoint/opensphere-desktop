package io.opensphere.kml.envoy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.sax.SAXSource;

import org.apache.log4j.Logger;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequenceFactory;

import de.micromata.opengis.kml.v_2_2_0.Boundary;
import de.micromata.opengis.kml.v_2_2_0.Document;
import de.micromata.opengis.kml.v_2_2_0.Feature;
import de.micromata.opengis.kml.v_2_2_0.Folder;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import de.micromata.opengis.kml.v_2_2_0.LinearRing;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import de.micromata.opengis.kml.v_2_2_0.Polygon;
import de.micromata.opengis.kml.v_2_2_0.gx.Track;
import io.opensphere.core.util.XMLUtilities;
import io.opensphere.core.util.jts.JTSUtilities;
import io.opensphere.core.util.xml.WrappedXMLReader;
import io.opensphere.kml.common.model.Processor;
import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

/**
 * A pool of parsers.
 */
@ThreadSafe
public final class KMLParserPool implements Processor<InputStream, Kml>
{
    /** Logger. */
    private static final Logger LOGGER = Logger.getLogger(KMLParserPool.class);

    /** The capacity of the pool. */
    private final int myCapacity;

    /** The pool itself. */
    @GuardedBy("myParserPool")
    private final LinkedBlockingQueue<Parser> myParserPool;

    /** The number of parsers created. */
    @GuardedBy("this")
    private int myParserCount;

    /**
     * Constructor.
     */
    public KMLParserPool()
    {
        myCapacity = Math.max(Runtime.getRuntime().availableProcessors() - 2, 1);
        myParserPool = new LinkedBlockingQueue<>(myCapacity);
    }

    @Override
    public Kml process(InputStream input) throws JAXBException
    {
        try
        {
            return takeParser().unmarshalKml(input);
        }
        catch (InterruptedException e)
        {
            LOGGER.error(e.getMessage());
        }
        return null;
    }

    /**
     * Takes a parser, blocking until one is available.
     *
     * @return A parser
     * @throws InterruptedException if interrupted while waiting
     */
    private Parser takeParser() throws InterruptedException
    {
        synchronized (this)
        {
            if (myParserPool.isEmpty() && myParserCount < myCapacity && myParserPool.offer(new Parser()))
            {
                myParserCount++;
                if (LOGGER.isDebugEnabled())
                {
                    LOGGER.debug("Parser count is now: " + myParserCount);
                }
            }
        }
        return myParserPool.take();
    }

    /**
     * A JAK Parser.
     */
    private final class Parser
    {
        /** The JAK Unmarshaller. */
        private Unmarshaller myUnmarshaller;

        /** The JAK XMLReader. */
        private XMLReader myXMLReader;

        /**
         * Constructor.
         */
        public Parser()
        {
            try
            {
                myUnmarshaller = JAXBContext.newInstance(Kml.class).createUnmarshaller();
                myXMLReader = new WrappedXMLReader(false, handler -> new KMLNamespaceFilterHandler(handler));
            }
            catch (RuntimeException | JAXBException | ParserConfigurationException | SAXException e)
            {
                LOGGER.error(e, e);
            }
        }

        /**
         * Convenience method to parse an InputStream using JAK.
         *
         * @param content The InputStream
         * @return The Kml object
         * @throws JAXBException If any unexpected errors occur while
         *             unmarshalling
         */
        public Kml unmarshalKml(InputStream content) throws JAXBException
        {
            Kml kml = null;
            try
            {
                if (content != null)
                {
                    String encoding = "UTF-8";
                    try
                    {
                        encoding = StreamUtilities.getEncoding(content);
                    }
                    catch (IOException e)
                    {
                        LOGGER.error(e.getMessage(), e);
                    }
                    if (LOGGER.isDebugEnabled())
                    {
                        LOGGER.debug("Using " + encoding + " encoding.");
                    }

                    InputSource input = new InputSource(content);
                    input.setEncoding(encoding);
                    SAXSource saxSource = new SAXSource(myXMLReader, input);
                    kml = (Kml)myUnmarshaller.unmarshal(saxSource);
                    replaceGeometries(kml);
                }
            }
            finally
            {
                myParserPool.offer(this);
            }
            return kml;
        }

        /**
         * Replaces the jak geometries with any custom ones we have created.
         *
         * @param kml The kml to inspect, and if any geometries are encountered
         *            where we have custom ones, we will replace it with the
         *            custom one.
         */
        private void replaceGeometries(Kml kml)
        {
            if (kml.getFeature() instanceof Document)
            {
                Document document = (Document)kml.getFeature();
                replaceGeometries(document.getFeature());
            }
        }

        /**
         * Replaces the jak geometries with any custom ones we have created.
         *
         * @param features The features to inspect, and if any geometries are
         *            encountered where we have custom ones, we will replace it
         *            with the custom one.
         */
        private void replaceGeometries(List<Feature> features)
        {
            for (Feature feature : features)
            {
                if (feature instanceof Document)
                {
                    replaceGeometries(((Document)feature).getFeature());
                }
                else if (feature instanceof Folder)
                {
                    replaceGeometries(((Folder)feature).getFeature());
                }
                else if (feature instanceof Placemark)
                {
                    Placemark placemark = (Placemark)feature;
                    if (placemark.getGeometry() instanceof Track)
                    {
                        Track jakTrack = (Track)placemark.getGeometry();
                        ByteArrayOutputStream output = new ByteArrayOutputStream();
                        try
                        {
                            XMLUtilities.writeXMLObject(jakTrack, output);
                            io.opensphere.kml.gx.Track customTrack = XMLUtilities.readXMLObject(
                                    new ByteArrayInputStream(output.toByteArray()), io.opensphere.kml.gx.Track.class);
                            placemark.setGeometry(customTrack);
                        }
                        catch (JAXBException e)
                        {
                            LOGGER.error(e, e);
                        }
                    }
                    else if (placemark.getGeometry() instanceof Polygon)
                    {
                        placemark.setGeometry(normalize((Polygon)placemark.getGeometry()));
                    }
                }
            }
        }

        /**
         * "Normalizes" the supplied polygon. KML polygons may be specified
         * erroneously with overlapping sections, which can cause failures
         * during rendering. The normalization will calculate the true external
         * boundary of the polygon without including any overlapped regions,
         * eliminating this issue.
         * 
         * @param polygon the polygon to normalize.
         * @return the normalized polygon.
         */
        private Polygon normalize(final Polygon polygon)
        {
            // this seems very strange, but it seems to be the only way to get
            // around some very strange polygons:
            com.vividsolutions.jts.geom.Geometry jtsGeometry = convertToJTS(polygon).buffer(.001).buffer(-.001);

            if (jtsGeometry instanceof com.vividsolutions.jts.geom.Polygon)
            {
                return convertToKML((com.vividsolutions.jts.geom.Polygon)jtsGeometry);
            }

            // TODO: temporary fix because JTS Geometry.buffer() can sometimes
            // fail and return a badly formed multipolygon when a polygon
            // crosses the meridian, antimeridian, or equator - if the polygon
            // also overlaps, it may cause further issues with the kml loading
            // to return the polygon without normalizing it, so this needs to be
            // fixed in a better way later.
            return polygon;
        }

        /**
         * Converts the supplied JTS Polygon to a KML Polygon.
         * 
         * @param jtsPolygon the JTS polygon to convert.
         * @return a KML Polygon created from the converted JTS polygon.
         */
        private Polygon convertToKML(com.vividsolutions.jts.geom.Polygon jtsPolygon)
        {
            Polygon polygon = new Polygon();
            polygon.setOuterBoundaryIs(convertToKmlBoundary(jtsPolygon.getExteriorRing()));

            for (int i = 0; i < jtsPolygon.getNumInteriorRing(); i++)
            {
                polygon.addToInnerBoundaryIs(convertToKmlBoundary(jtsPolygon.getInteriorRingN(i)));
            }

            return polygon;
        }

        /**
         * Converts the supplied KML Polygon to a JTS Polygon.
         * 
         * @param polygon the KML polygon to convert.
         * @return a JTS Polygon created from the converted KML polygon.
         */
        private com.vividsolutions.jts.geom.Polygon convertToJTS(Polygon polygon)
        {
            com.vividsolutions.jts.geom.LinearRing jtsOuterRing = convertToJTSLinearRing(
                    polygon.getOuterBoundaryIs().getLinearRing());
            com.vividsolutions.jts.geom.LinearRing[] holes = polygon.getInnerBoundaryIs().stream()
                    .map(b -> convertToJTSLinearRing(b.getLinearRing())).toArray(com.vividsolutions.jts.geom.LinearRing[]::new);

            com.vividsolutions.jts.geom.Polygon jtsPolygon = new com.vividsolutions.jts.geom.Polygon(jtsOuterRing, holes,
                    JTSUtilities.GEOMETRY_FACTORY);

            return jtsPolygon;
        }

        /**
         * Converts the supplied JTS Line string to a KML {@link Boundary}.
         * 
         * @param ring the ring to convert to a LinearRing.
         * @return a LinearRing generated from the supplied line string.
         */
        private Boundary convertToKmlBoundary(com.vividsolutions.jts.geom.LineString ring)
        {
            LinearRing kmlRing = new LinearRing();
            Arrays.stream(ring.getCoordinates()).forEach(c -> kmlRing.addToCoordinates(c.x, c.y, c.z));

            Boundary boundary = new Boundary();
            boundary.setLinearRing(kmlRing);
            return boundary;
        }

        /**
         * Converts the supplied KML {@link LinearRing} to a JTS
         * {@link com.vividsolutions.jts.geom.LinearRing} instance.
         * 
         * @param ring the KML linear ring to convert.
         * @return a JTS linear ring generated from the supplied KML linear
         *         ring.
         */
        private com.vividsolutions.jts.geom.LinearRing convertToJTSLinearRing(LinearRing ring)
        {
            com.vividsolutions.jts.geom.Coordinate[] jtsCoordinates = ring.getCoordinates().stream()
                    .map(c -> new com.vividsolutions.jts.geom.Coordinate(c.getLongitude(), c.getLatitude(), c.getAltitude()))
                    .toArray(com.vividsolutions.jts.geom.Coordinate[]::new);
            CoordinateSequence sequence = CoordinateArraySequenceFactory.instance().create(jtsCoordinates);

            com.vividsolutions.jts.geom.LinearRing jtsOuterRing = new com.vividsolutions.jts.geom.LinearRing(sequence,
                    JTSUtilities.GEOMETRY_FACTORY);
            return jtsOuterRing;
        }
    }
}

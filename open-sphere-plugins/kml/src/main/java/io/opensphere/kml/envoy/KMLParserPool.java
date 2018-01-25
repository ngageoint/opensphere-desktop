package io.opensphere.kml.envoy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.sax.SAXSource;

import org.apache.log4j.Logger;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import de.micromata.opengis.kml.v_2_2_0.Document;
import de.micromata.opengis.kml.v_2_2_0.Feature;
import de.micromata.opengis.kml.v_2_2_0.Folder;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import de.micromata.opengis.kml.v_2_2_0.gx.Track;
import io.opensphere.core.util.XMLUtilities;
import io.opensphere.core.util.xml.WrappedXMLReader;
import io.opensphere.kml.common.model.Processor;

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
                if (feature instanceof Folder)
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
                }
            }
        }
    }
}

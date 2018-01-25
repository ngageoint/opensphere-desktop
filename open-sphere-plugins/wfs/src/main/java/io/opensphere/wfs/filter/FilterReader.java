package io.opensphere.wfs.filter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.mantle.data.geom.AbstractMapGeometrySupport;

/** A reader of WFS filter geometry. */
public final class FilterReader
{
    /** The logger. */
    private static final Logger LOGGER = Logger.getLogger(FilterReader.class);

    /**
     * Parses the XML text into a geometries.
     *
     * @param xmlText the XML text
     * @return the geometry
     */
    public static List<AbstractMapGeometrySupport> parse(String xmlText)
    {
        List<AbstractMapGeometrySupport> geometries;

        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        try
        {
            SAXParser saxParser = factory.newSAXParser();
            FilterHandler handler = new FilterHandler();
            byte[] bytes = xmlText.getBytes(StringUtilities.DEFAULT_CHARSET);
            saxParser.parse(new ByteArrayInputStream(bytes), handler);
            geometries = handler.getGeometries();
        }
        catch (ParserConfigurationException | SAXException | IOException e)
        {
            geometries = Collections.emptyList();
            LOGGER.error(e);
        }

        return geometries;
    }

    /** Private constructor. */
    private FilterReader()
    {
    }
}

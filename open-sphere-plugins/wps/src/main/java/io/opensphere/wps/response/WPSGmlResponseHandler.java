package io.opensphere.wps.response;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import io.opensphere.core.Toolbox;
import io.opensphere.wps.source.WPSResponse;

/** The gml response handling class. */
public class WPSGmlResponseHandler extends WPSResponseHandler
{
    /** The class logger. */
    private static final Logger LOGGER = Logger.getLogger(WPSGmlResponseHandler.class);

    /**
     * Constructor.
     *
     * @param response The wps response.
     */
    public WPSGmlResponseHandler(WPSResponse response)
    {
        super(response);
    }

    @Override
    public Object handleResponse(Toolbox toolbox, String name)
    {
        GmlWpsSaxHandler311 handler = new GmlWpsSaxHandler311(name);
        return handleResponse(handler);
    }

    /**
     * Handles the response.
     *
     * @param handler the response handler
     * @return the result
     */
    protected Object handleResponse(GmlWpsSaxHandler311 handler)
    {
        LOGGER.info("Attempting to parse WPS xml response");

        // TODO Eventually get away from craptastic sax parser and use jaxb or
        // something respectable.
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        SAXParser saxParser;

        Exception ex;
        try
        {
            saxParser = factory.newSAXParser();
            long start = System.currentTimeMillis();
            saxParser.parse(getResponse().getResponseStream(), handler);
            long end = System.currentTimeMillis();

            LOGGER.info("Took " + (end - start) + " ms to parse " + handler.getProcessResult().getFeatures().size() + " objects");
            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug("The time span for process results = " + handler.getProcessResult().getTimespan().toString());
            }
            ex = null;
        }
        catch (NumberFormatException | SAXException | IOException | ParserConfigurationException e)
        {
            ex = e;
        }
        if (ex != null)
        {
            LOGGER.error("Error while parsing WPS response: " + ex, ex);
        }

        return handler.getProcessResult();
    }
}

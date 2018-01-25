package io.opensphere.kml.datasource.controller;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.GeneralSecurityException;

import javax.xml.stream.events.StartElement;

import org.apache.log4j.Logger;

import de.micromata.opengis.kml.v_2_2_0.Kml;
import io.opensphere.core.Toolbox;
import io.opensphere.core.util.StartElementInspector;
import io.opensphere.core.util.XMLUtilities;
import io.opensphere.core.util.net.HttpUtilities;
import io.opensphere.kml.envoy.StreamUtilities;

/**
 * Inspects the beginning of an input stream to see if it is actually kml data
 * or not.
 *
 */
public class KMLPeeker implements StartElementInspector
{
    /**
     * Used to log error messages.
     */
    private static final Logger LOGGER = Logger.getLogger(KMLPeeker.class);

    /**
     * The system toolbox.
     */
    private final Toolbox myToolbox;

    /**
     * Constructs a new kml peeker.
     *
     * @param toolbox The system toolbox.
     */
    public KMLPeeker(Toolbox toolbox)
    {
        myToolbox = toolbox;
    }

    /**
     * Checks to see if the url contains KML data.
     *
     * @param url The url to check.
     * @return True if the url is KML data, false otherwise.
     */
    public boolean isKml(URL url)
    {
        boolean isKml = false;

        try
        {
            InputStream stream = openStream(url);

            if (stream != null)
            {
                try
                {
                    isKml = XMLUtilities.canUnmarshal(stream, this);

                    // Now check if a kmz.
                    if (!isKml)
                    {
                        stream.close();
                        stream = openStream(url);

                        if (stream != null)
                        {
                            isKml = StreamUtilities.isZipInputStreamNoReset(stream);
                        }
                    }
                }
                finally
                {
                    if (stream != null)
                    {
                        stream.close();
                    }
                }
            }
        }
        catch (IOException | GeneralSecurityException | URISyntaxException e)
        {
            LOGGER.error(e.getMessage(), e);
        }

        return isKml;
    }

    @Override
    public boolean isValidStartElement(StartElement element)
    {
        String kmlTag = Kml.class.getSimpleName().toLowerCase();

        boolean isValid = element.getName().getLocalPart().equals(kmlTag);

        if (!isValid)
        {
            String namespace = element.getName().getNamespaceURI();
            isValid = namespace.contains(kmlTag);
        }

        return isValid;
    }

    /**
     * Opens the stream.
     *
     * @param url The url to open.
     * @return The opened stream.
     * @throws GeneralSecurityException Thrown if there are security issues
     *             trying to open the url.
     * @throws IOException Thrown if there IO issues opening the url.
     * @throws URISyntaxException If the url could not be converted to a URI.
     */
    private InputStream openStream(URL url) throws GeneralSecurityException, IOException, URISyntaxException
    {
        InputStream stream = null;

        if (!url.getProtocol().equalsIgnoreCase("file"))
        {
            try
            {
                stream = HttpUtilities.sendGet(url, myToolbox.getServerProviderRegistry());
            }
            catch (IOException e)
            {
                LOGGER.info(e);
            }
        }
        else
        {
            stream = new FileInputStream(url.getFile());
        }

        return stream;
    }
}

package io.opensphere.kml.marshal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.Writer;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.sax.SAXSource;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import de.micromata.opengis.kml.v_2_2_0.Kml;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.core.util.xml.WrappedXMLReader;
import io.opensphere.kml.gx.Track;

/**
 * Handles marshalling and unmarshling kml files.
 *
 */
public final class KmlMarshaller
{
    /**
     * Used to log messages.
     */
    private static final Logger LOGGER = Logger.getLogger(KmlMarshaller.class);

    /**
     * The instance of this class.
     */
    private static KmlMarshaller ourInstance;

    /**
     * The jaxb context.
     */
    private JAXBContext myContext;

    /**
     * Used to write out to kml format.
     */
    private Marshaller myMarshaller;

    /**
     * Used to read in kml formatted data.
     */
    private Unmarshaller myUnmarshaller;

    /**
     * Gets the instance of this class.
     *
     * @return The instance of this class.
     */
    public static synchronized KmlMarshaller getInstance()
    {
        if (ourInstance == null)
        {
            ourInstance = new KmlMarshaller();
        }
        return ourInstance;
    }

    /**
     * Private constructor.
     */
    private KmlMarshaller()
    {
        try
        {
            myContext = JAXBContext.newInstance(Kml.class, Track.class);
            myMarshaller = myContext.createMarshaller();
            myMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            myMarshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper", new NamespaceMapper());

            myUnmarshaller = myContext.createUnmarshaller();
        }
        catch (JAXBException e)
        {
            LOGGER.error(e.getMessage(), e);
        }
    }

    /**
     * Marhsals the kml to the specified file.
     *
     * @param kml The kml to marshal.
     * @param filename The file to write to.
     * @return True upon success, false otherwise.
     * @throws FileNotFoundException If the file does not exist.
     */
    public boolean marshal(Kml kml, final File filename) throws FileNotFoundException
    {
        boolean isSuccess = false;

        try (OutputStream out = new FileOutputStream(filename))
        {
            myMarshaller.marshal(kml, out);
            isSuccess = true;
        }
        catch (JAXBException | IOException e)
        {
            LOGGER.error(e.getMessage(), e);
        }

        return isSuccess;
    }

    /**
     * Marhsals the kml to the specified node.
     *
     * @param kml The kml to marshal.
     * @param node The node.
     * @return True upon success, false otherwise.
     */
    public boolean marshal(Kml kml, Node node)
    {
        boolean isSuccess = false;

        try
        {
            myMarshaller.marshal(kml, node);
            isSuccess = true;
        }
        catch (JAXBException e)
        {
            LOGGER.error(e.getMessage(), e);
        }

        return isSuccess;
    }

    /**
     * Marshalls the kml object to the writer.
     *
     * @param kml The kml to write.
     * @param writer Used to output the marhalled data.
     * @return True upon success false otherwise.
     */
    public boolean marshal(Kml kml, final Writer writer)
    {
        boolean isSuccess = true;
        try
        {
            myMarshaller.marshal(kml, writer);
        }
        catch (JAXBException e)
        {
            LOGGER.error(e.getMessage(), e);
        }

        return isSuccess;
    }

    /**
     * Unmarshals the file into a kml object.
     *
     * @param file The kml formatted file.
     * @return The kml object.
     * @throws FileNotFoundException if the files not found.
     */
    public Kml unmarshal(File file) throws FileNotFoundException
    {
        Kml kml = null;

        try (FileInputStream fileStream = new FileInputStream(file))
        {
            InputStreamReader reader = new InputStreamReader(fileStream, StringUtilities.DEFAULT_CHARSET);
            InputSource input = new InputSource(reader);

            kml = unmarshal(input);
        }
        catch (IOException e)
        {
            LOGGER.error(e, e);
        }

        return kml;
    }

    /**
     * Unmarshals the kml content string.
     *
     * @param kmlContent The kml formatted string to unmarshal.
     * @return The kml object.
     */
    public Kml unmarshal(String kmlContent)
    {
        InputSource input = new InputSource(new StringReader(kmlContent));

        return unmarshal(input);
    }

    /**
     * Unmarshals the input into a kml object.
     *
     * @param input The kml formatted data.
     * @return A kml object.
     */
    private Kml unmarshal(InputSource input)
    {
        Kml jaxbRootElement = null;

        SAXSource saxSource;
        try
        {
            saxSource = new SAXSource(new WrappedXMLReader(false, handler -> new NamespaceFilterHandler(handler)), input);

            jaxbRootElement = (Kml)myUnmarshaller.unmarshal(saxSource);
        }
        catch (ParserConfigurationException | SAXException | JAXBException e)
        {
            LOGGER.error(e.getMessage(), e);
        }

        return jaxbRootElement;
    }
}

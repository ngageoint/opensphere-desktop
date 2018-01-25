package io.opensphere.core.image;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

import javax.xml.bind.JAXBException;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import io.opensphere.core.image.exception.ServiceExceptionReport;
import io.opensphere.core.util.XMLUtilities;

/**
 * Processes a textual error in various formats to produce a human-readable and
 * loggable error message.
 */
public final class ImageErrorProcessor
{
    /**
     * The <code>Log</code> instance used for logging.
     */
    private static final Logger LOG = Logger.getLogger(ImageErrorProcessor.class);

    /**
     * An XML Header used to determine if the supplied {@link String} contains
     * XML content.
     */
    private static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";

    /**
     * Private constructor, to prevent instantiation of utility class.
     */
    private ImageErrorProcessor()
    {
        throw new UnsupportedOperationException("Instantiation of utility classes is not permitted.");
    }

    /**
     * Processes the supplied input stream, reading its contents into a string,
     * and processing the contents.
     *
     * @param pSourceErrorStream the input stream from which to extract the
     *            error.
     * @return a String in which a textual error message is contained. If the
     *         source error could not be processed further, the source error is
     *         returned.
     */
    public static String processError(InputStream pSourceErrorStream)
    {
        String returnValue = null;
        try
        {
            returnValue = processError(IOUtils.toString(pSourceErrorStream));
        }
        catch (IOException e)
        {
            LOG.error("Unable to read error message from InputStream.", e);
        }
        return returnValue;
    }

    /**
     * Processes the supplied input reader, reading its contents into a string,
     * and processing the contents.
     *
     * @param pSourceErrorReader the reader from which to extract the error.
     * @return a String in which a textual error message is contained. If the
     *         source error could not be processed further, the source error is
     *         returned.
     */
    public static String processError(Reader pSourceErrorReader)
    {
        String returnValue = null;
        try
        {
            returnValue = processError(IOUtils.toString(pSourceErrorReader));
        }
        catch (IOException e)
        {
            LOG.error("Unable to read error message from Reader.", e);
        }
        return returnValue;
    }

    /**
     * Processes the supplied error text.
     *
     * @param pSourceError the {@link CharSequence} from which to extract the
     *            error.
     * @return a String in which a textual error message is contained. If the
     *         source error could not be processed further, the source error is
     *         returned.
     */
    public static String processError(CharSequence pSourceError)
    {
        String workingValue = pSourceError.toString().trim();
        // default the return value to the supplied String:
        String returnValue = workingValue;
        if (workingValue.startsWith(XML_HEADER))
        {
            returnValue = processXmlError(workingValue);
        }

        return returnValue;
    }

    /**
     * Processes the supplied string, extracting an error from one of a number
     * of known XML formats.
     *
     * @param pSourceError the source string from which to extract the error
     *            message.
     * @return A string in which the source error is contained.
     */
    public static String processXmlError(String pSourceError)
    {
        String returnValue = null;
        // check for known XML error types:
        if (pSourceError.contains("ServiceExceptionReport"))
        {
            // this is an ArcMap service exception report:
            try
            {
                ServiceExceptionReport report = XMLUtilities
                        .readXMLObject(new ByteArrayInputStream(pSourceError.getBytes("UTF-8")), ServiceExceptionReport.class);
                returnValue = report.getServiceException().getValue();
            }
            catch (JAXBException | UnsupportedEncodingException e)
            {
                LOG.error("Unable to unmarshal XML Service Exception Report.", e);
            }
        }

        return returnValue;
    }
}

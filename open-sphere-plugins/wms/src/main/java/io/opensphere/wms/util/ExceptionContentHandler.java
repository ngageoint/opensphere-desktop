package io.opensphere.wms.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.ContentHandler;
import java.net.URL;
import java.net.URLConnection;

import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;

import io.opensphere.core.util.XMLUtilities;
import io.opensphere.core.util.lang.StringUtilities;
import net.opengis.ogc._100.ServiceExceptionType;
import net.opengis.wms._111.ServiceException;
import net.opengis.wms._111.ServiceExceptionReport;

/**
 * Content handler for {@link ServiceExceptionReport}s.
 */
public class ExceptionContentHandler extends ContentHandler
{
    /** The exception MIME type. */
    public static final String MIME_TYPE = "application/vnd.ogc.se_xml";

    /** The exception MIME type for version 1.3. */
    public static final String MIME_TYPE_1_3 = "text/xml";

    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(ExceptionContentHandler.class);

    /**
     * Format a {@link ServiceExceptionReport} into a String that looks better
     * when displayed to a user. Mostly involves breaking really long messages
     * into multiple lines based on the passed-in line length.
     *
     * @param ser the {@link ServiceExceptionReport} to format
     * @param lineLength the length to use as guidance when breaking long lines
     * @return the formatted string
     */
    public static String formatServiceException(ServiceExceptionReport ser, int lineLength)
    {
        StringBuilder sb = new StringBuilder();
        for (ServiceException se : ser.getServiceException())
        {
            String exStr = StringUtilities.concat("Code: ", se.getCode(), " Message: ", se.getvalue());
            if (exStr == null || lineLength > exStr.length() || lineLength <= 0)
            {
                sb.append(exStr);
            }
            else
            {
                String[] words = exStr.split(" ");
                int currentLength = 0;
                for (int i = 0; i < words.length; i++)
                {
                    if (currentLength >= lineLength)
                    {
                        sb.append("\n   ");
                        currentLength = 0;
                    }
                    sb.append(words[i]);
                    currentLength += words[i].length();

                    // Don't append a space to the end of the original string.
                    if (i != words.length - 1)
                    {
                        sb.append(' ');
                        currentLength += 1;
                    }
                }
            }
            sb.append('\n');
        }
        return sb.toString();
    }

    /**
     * Get the content from a stream.
     *
     * @param stream The input stream.
     * @param url The URL, used for logging.
     * @param contentType The content type, used for logging.
     * @return The content, or <code>null</code> if it cannot be unmarshalled.
     */
    public ServiceExceptionReport getContent(InputStream stream, URL url, String contentType)
    {
        ServiceExceptionReport ser = getReport(stream, url, contentType);

        if (ser != null)
        {
            for (ServiceException se : ser.getServiceException())
            {
                LOGGER.warn("Service exception received for url [" + url + "]: code [" + se.getCode() + "] value ["
                        + se.getvalue() + "]");
            }
        }

        return ser;
    }

    @Override
    public ServiceExceptionReport getContent(URLConnection urlc) throws IOException
    {
        return getContent(urlc.getInputStream(), urlc.getURL(), urlc.getContentType());
    }

    /**
     * Gets the service exception from the stream.
     *
     * @param stream The stream containing the service exception.
     * @param url The url the stream came from.
     * @param contentType The content type of the stream.
     * @return The {@link ServiceExceptionReport} or null if it could not be
     *         parsed.
     */
    private ServiceExceptionReport getReport(InputStream stream, URL url, String contentType)
    {
        ServiceExceptionReport report = null;
        try
        {
            if (contentType.contains(MIME_TYPE))
            {
                report = XMLUtilities.readXMLObject(stream, ServiceExceptionReport.class);
            }
            else
            {
                net.opengis.ogc._100.ServiceExceptionReport report130 = XMLUtilities.readXMLObject(stream,
                        net.opengis.ogc._100.ServiceExceptionReport.class);
                if (report130 != null)
                {
                    report = new ServiceExceptionReport();
                    report.setVersion(report130.getVersion());

                    for (ServiceExceptionType exception130 : report130.getServiceException())
                    {
                        ServiceException exception = new ServiceException();
                        exception.setCode(exception130.getCode());
                        exception.setvalue(exception130.getValue());

                        report.getServiceException().add(exception);
                    }
                }
            }
        }
        catch (JAXBException e)
        {
            LOGGER.error("Could not unmarshal " + contentType + " data returned for url [" + url + "]: " + e, e);
        }

        return report;
    }
}

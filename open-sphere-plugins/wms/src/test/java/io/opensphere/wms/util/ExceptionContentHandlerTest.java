package io.opensphere.wms.util;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Test;

import io.opensphere.core.util.lang.StringUtilities;
import net.opengis.wms._111.ServiceException;
import net.opengis.wms._111.ServiceExceptionReport;

/**
 * Tests the {@link ExceptionContentHandler} class.
 */
public class ExceptionContentHandlerTest
{
    /**
     * Tests the getContent for a service exception returned from wms 1.1.
     *
     * @throws MalformedURLException Bad Url.
     */
    @Test
    public void testGetContentv1dot1() throws MalformedURLException
    {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<!DOCTYPE WMT_MS_Capabilities SYSTEM \"ogc/schemas/wms/1.1.1/exception_1_1_1.dtd\">"
                + "<ServiceExceptionReport version=\"1.1.1\"><ServiceException>"
                + "The value for parameter HEIGHT \"0\" is invalid: The height must be at least 1.</ServiceException></ServiceExceptionReport>";

        ByteArrayInputStream stream = new ByteArrayInputStream(xml.getBytes(StringUtilities.DEFAULT_CHARSET));

        ExceptionContentHandler handler = new ExceptionContentHandler();
        ServiceExceptionReport report = handler.getContent(stream, new URL("http://somehost"), ExceptionContentHandler.MIME_TYPE);

        assertEquals(1, report.getServiceException().size());

        ServiceException exception = report.getServiceException().get(0);

        assertEquals("The value for parameter HEIGHT \"0\" is invalid: The height must be at least 1.", exception.getvalue());
    }

    /**
     * Tests the getContent for a service exception returned from wms 1.3.
     *
     * @throws MalformedURLException Bad Url.
     */
    @Test
    public void testGetContentv1dot3() throws MalformedURLException
    {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><ogc:ServiceExceptionReport version=\"1.3.0\""
                + " xsi:schemaLocation=\"http://www.opengis.net/ogc ogc/schemas/wms/1.3.0/exceptions_1_3_0.xsd\""
                + " xmlns:ogc=\"http://www.opengis.net/ogc\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">"
                + "<ogc:ServiceException locator=\"Parameter HEIGHT\">"
                + "The value for parameter HEIGHT \"0\" is invalid: The height must be at least 1.</ogc:ServiceException></ogc:ServiceExceptionReport>";

        ByteArrayInputStream stream = new ByteArrayInputStream(xml.getBytes(StringUtilities.DEFAULT_CHARSET));

        ExceptionContentHandler handler = new ExceptionContentHandler();
        ServiceExceptionReport report = handler.getContent(stream, new URL("http://somehost"),
                ExceptionContentHandler.MIME_TYPE_1_3);

        assertEquals(1, report.getServiceException().size());

        ServiceException exception = report.getServiceException().get(0);

        assertEquals("The value for parameter HEIGHT \"0\" is invalid: The height must be at least 1.", exception.getvalue());
    }
}

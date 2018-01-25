package io.opensphere.wps.streaming.beans;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;

import javax.xml.bind.JAXBException;

import org.junit.Test;

import io.opensphere.core.util.XMLUtilities;
import io.opensphere.core.util.lang.StringUtilities;

/**
 * Tests the {@link ConfigurationTest} class.
 */
public class ConfigurationTest
{
    /**
     * Tests unmarshalling the {@link ConfigurationTest} class.
     *
     * @throws JAXBException Bad jaxb.
     */
    @Test
    public void test() throws JAXBException
    {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><configuration><url base=\"https://somehost/nrt/streamingServlet\">"
                + "<requestParameter><!--Required. The name of the message container that contains the queue of messages."
                + "  This value was provided from the server when a subscription request was initiated.-->filterId</requestParameter>"
                + "<requestParameter><!--Optional. This setting will tell the server how long (in milliseconds)"
                + " to keep the socket connection  open to stream messages. Defaults to 30000 milliseconds.-->pollInterval</requestParameter>"
                + "<requestParameter><!--Optional. If this is set to true then only debug information for all message containers will be returned."
                + "  Default is false.-->debug</requestParameter></url>" + "<filterId>aFilterId</filterId></configuration>";

        ByteArrayInputStream stream = new ByteArrayInputStream(xml.getBytes(StringUtilities.DEFAULT_CHARSET));

        Configuration configuration = XMLUtilities.readXMLObject(stream, Configuration.class);

        assertEquals("aFilterId", configuration.getFilterId());
        assertEquals("https://somehost/nrt/streamingServlet", configuration.getUrl().getBase());
    }
}

package io.opensphere.server.source;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;

import javax.xml.bind.JAXBException;

import org.junit.Test;

import io.opensphere.core.util.XMLUtilities;

/**
 * Tests the OGCServerSource class.
 *
 */
public class OGCServerSourceTest
{
    /**
     * Tests the permalink property.
     *
     * @throws JAXBException Bad Jaxb.
     */
    @Test
    public void testPermalink() throws JAXBException
    {
        OGCServerSource source = new OGCServerSource();
        assertEquals("/file-store/v1", source.getPermalinkUrl());

        source.setPermalinkUrl("permalink");

        InputStream stream = XMLUtilities.writeXMLObjectToInputStreamSync(source);
        OGCServerSource actual = XMLUtilities.readXMLObject(stream, OGCServerSource.class);

        assertEquals("permalink", actual.getPermalinkUrl());
    }
}

package io.opensphere.server.state.activate.serversource.genericserver;

import static org.junit.Assert.assertEquals;

import java.net.URL;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.junit.Test;
import org.w3c.dom.Node;

/**
 * Tests the WmsUrlRetriever class.
 */
public class WmsUrlRetrieverTest
{
    /**
     * Tests getting the wms urls.
     *
     * @throws XPathExpressionException Bad path.
     * @throws ParserConfigurationException Bad parse.
     * @throws JAXBException Bad jaxb.
     */
    @Test
    public void testGetUrls() throws XPathExpressionException, ParserConfigurationException, JAXBException
    {
        Node node = StateNodeUtils.createNodeWithWMSLayers();

        WmsUrlRetriever wmsRetriever = new WmsUrlRetriever();
        List<URL> urls = wmsRetriever.getUrls(node);

        assertEquals(2, urls.size());
        assertEquals(StateNodeUtils.WMS_URL, urls.get(0).toString());
        assertEquals(StateNodeUtils.WMS_URL, urls.get(1).toString());
    }
}

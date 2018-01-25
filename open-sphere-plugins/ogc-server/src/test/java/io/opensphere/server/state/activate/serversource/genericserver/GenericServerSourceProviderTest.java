package io.opensphere.server.state.activate.serversource.genericserver;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.lang3.StringUtils;
import org.easymock.EasyMock;
import org.junit.Test;
import org.w3c.dom.Node;

import io.opensphere.core.PluginToolboxRegistry;
import io.opensphere.core.Toolbox;
import io.opensphere.core.modulestate.StateXML;
import io.opensphere.core.util.XMLUtilities;
import io.opensphere.mantle.datasources.IDataSource;
import io.opensphere.server.customization.DefaultCustomization;
import io.opensphere.server.source.OGCServerSource;
import io.opensphere.server.state.StateConstants;
import io.opensphere.server.state.DefaultWFSLayerConfiguration;
import io.opensphere.server.toolbox.ServerToolbox;
import io.opensphere.server.toolbox.WFSLayerConfigurationManager;

/**
 * Tests the GenericServerSourceProvider.
 */
public class GenericServerSourceProviderTest
{
    /**
     * The WFS URL, with the same host name as the WMS url's created in
     * StateNodeUtils call.
     */
    private static final String ourWfsUrl = "http://somehost/ogc/wfs";

    /**
     * Another WMS URL with an IP address in the URL.
     */
    private static final String ourWmsIpUrl = "http://10.42.211.83/ogc/wms";

    /**
     * Another WFS URL.
     */
    private static final String ourOtherWfsUrl = "http://anotherhost/ogc/wfs";

    /**
     * Tests creating server sources for a server with both WMS and WFS, just
     * WMS, and just WFS. Also tests what happens with duplicate URLs, should
     * create one server source for that.
     *
     * @throws ParserConfigurationException Bad parse.
     * @throws XPathExpressionException Bad XPath.
     * @throws JAXBException Bad JAXB.
     * @throws MalformedURLException Bad URL.
     */
    @Test
    public void testGetServersInNode()
        throws ParserConfigurationException, XPathExpressionException, JAXBException, MalformedURLException
    {
        Toolbox tbMock = EasyMock.createNiceMock(Toolbox.class);

        PluginToolboxRegistry registry = EasyMock.createNiceMock(PluginToolboxRegistry.class);
        ServerToolbox serverToolbox = EasyMock.createNiceMock(ServerToolbox.class);
        WFSLayerConfigurationManager manager = new WFSLayerConfigurationManager();

        EasyMock.expect(tbMock.getPluginToolboxRegistry()).andReturn(registry).anyTimes();
        EasyMock.expect(registry.getPluginToolbox(ServerToolbox.class)).andReturn(serverToolbox).anyTimes();
        EasyMock.expect(serverToolbox.getLayerConfigurationManager()).andReturn(manager).anyTimes();

        EasyMock.replay(tbMock, registry, serverToolbox);

        DefaultWFSLayerConfiguration nrtConfiguration = new DefaultWFSLayerConfiguration("nrt", DefaultCustomization.class);
        manager.addServerConfiguration(nrtConfiguration);

        GenericServerSourceProvider sourceProvider = new GenericServerSourceProvider(tbMock);
        List<IDataSource> servers = sourceProvider.getServersInNode(createNode());

        OGCServerSource wmsServer = null;

        assertEquals(2, servers.size());

        for (IDataSource dataSource : servers)
        {
            OGCServerSource server = (OGCServerSource)dataSource;
            if (StringUtils.isEmpty(server.getWFSServerURL()))
            {
                wmsServer = server;
            }
        }

        assertNotNull(wmsServer);

        assertEquals(ourWmsIpUrl, wmsServer.getWMSServerURL());
        assertEquals(new URL(ourWmsIpUrl).getHost(), wmsServer.getName());
        assertNull(wmsServer.getWFSServerURL());
    }

    /**
     * Creates a state node.
     *
     * @return The state node.
     * @throws ParserConfigurationException Bad parse.
     * @throws XPathExpressionException Bad XPath.
     * @throws JAXBException Bad JAXB.
     */
    private Node createNode() throws ParserConfigurationException, XPathExpressionException, JAXBException
    {
        Node stateNode = StateNodeUtils.createWfsNodeWithData(ourWfsUrl);

        Node dataGroup = StateXML.getChildNode(stateNode, StateConstants.DATA_LAYERS_PATH);

        FeatureLayerMockUp wfsLayer = new FeatureLayerMockUp();
        wfsLayer.setUrl(ourOtherWfsUrl);

        XMLUtilities.marshalJAXBObjectToElement(wfsLayer, dataGroup);

        Node mapGroup = StateXML.getChildNode(stateNode, StateConstants.MAP_LAYERS_PATH);

        WMSLayerMockUp wmsLayer = new WMSLayerMockUp();
        wmsLayer.setWmsUrl(ourWmsIpUrl);

        XMLUtilities.marshalJAXBObjectToElement(wmsLayer, mapGroup);

        return stateNode;
    }
}

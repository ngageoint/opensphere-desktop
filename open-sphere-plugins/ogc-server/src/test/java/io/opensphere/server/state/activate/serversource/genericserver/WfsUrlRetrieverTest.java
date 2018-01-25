package io.opensphere.server.state.activate.serversource.genericserver;

import static org.junit.Assert.assertEquals;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.easymock.EasyMock;
import org.junit.Test;
import org.w3c.dom.Node;

import io.opensphere.core.PluginToolboxRegistry;
import io.opensphere.core.Toolbox;
import io.opensphere.server.customization.DefaultCustomization;
import io.opensphere.server.state.DefaultWFSLayerConfiguration;
import io.opensphere.server.toolbox.ServerToolbox;
import io.opensphere.server.toolbox.WFSLayerConfigurationManager;

/**
 * Tests the WfsUrlRetriever class.
 *
 */
public class WfsUrlRetrieverTest
{
    /**
     * The WFS URL.
     */
    private static final String ourWfsUrl = "http://somehost/ogc//wfs";

    /**
     * The WPS URL.
     */
    private static final String ourWpsUrl = "http://somehost/ogc/wps";

    /**
     * Tests getting the WFS URLs.
     *
     * @throws XPathExpressionException Bad path.
     * @throws ParserConfigurationException Bad parse.
     * @throws JAXBException Bad JAXB.
     * @throws URISyntaxException Bad syntax.
     */
    @Test
    public void testGetUrls() throws XPathExpressionException, ParserConfigurationException, JAXBException, URISyntaxException
    {
        Node node = StateNodeUtils.createWfsNodeWithData(ourWfsUrl);

        Toolbox toolbox = EasyMock.createNiceMock(Toolbox.class);
        PluginToolboxRegistry registry = EasyMock.createNiceMock(PluginToolboxRegistry.class);
        ServerToolbox serverToolbox = EasyMock.createNiceMock(ServerToolbox.class);
        WFSLayerConfigurationManager manager = new WFSLayerConfigurationManager();

        DefaultWFSLayerConfiguration wfsConfiguration = new DefaultWFSLayerConfiguration("wfs", DefaultCustomization.class);
        manager.addServerConfiguration(wfsConfiguration);

        EasyMock.expect(toolbox.getPluginToolboxRegistry()).andReturn(registry).anyTimes();
        EasyMock.expect(registry.getPluginToolbox(ServerToolbox.class)).andReturn(serverToolbox).anyTimes();
        EasyMock.expect(serverToolbox.getLayerConfigurationManager()).andReturn(manager).anyTimes();

        EasyMock.replay(toolbox, registry, serverToolbox);
        WfsUrlRetriever wfsRetriever = new WfsUrlRetriever(toolbox);
        List<URL> urls = wfsRetriever.getUrls(node);

        assertEquals(1, urls.size());
        assertEquals(new URI(ourWfsUrl).normalize().toString(), urls.get(0).toString());
    }

    /**
     * Tests getting the WFS URLs for NRT layers..
     *
     * @throws XPathExpressionException Bad path.
     * @throws ParserConfigurationException Bad parse.
     * @throws JAXBException Bad JAXB.
     * @throws URISyntaxException Bad syntax.
     */
    @Test
    public void testGetUrlsForNrtLayers()
        throws XPathExpressionException, ParserConfigurationException, JAXBException, URISyntaxException
    {
        Node node = StateNodeUtils.createWfsNodeWithData(ourWpsUrl);

        Toolbox toolbox = EasyMock.createNiceMock(Toolbox.class);
        PluginToolboxRegistry registry = EasyMock.createNiceMock(PluginToolboxRegistry.class);
        ServerToolbox serverToolbox = EasyMock.createNiceMock(ServerToolbox.class);
        WFSLayerConfigurationManager manager = new WFSLayerConfigurationManager();

        DefaultWFSLayerConfiguration wfsConfiguration = new DefaultWFSLayerConfiguration("wfs", DefaultCustomization.class);
        manager.addServerConfiguration(wfsConfiguration);
        DefaultWFSLayerConfiguration nrtConfiguration = new DefaultWFSLayerConfiguration("nrt", DefaultCustomization.class);
        manager.addServerConfiguration(nrtConfiguration);

        EasyMock.expect(toolbox.getPluginToolboxRegistry()).andReturn(registry).anyTimes();
        EasyMock.expect(registry.getPluginToolbox(ServerToolbox.class)).andReturn(serverToolbox).anyTimes();
        EasyMock.expect(serverToolbox.getLayerConfigurationManager()).andReturn(manager).anyTimes();

        EasyMock.replay(toolbox, registry, serverToolbox);
        WfsUrlRetriever wfsRetriever = new WfsUrlRetriever(toolbox);
        List<URL> urls = wfsRetriever.getUrls(node);

        assertEquals(1, urls.size());
        assertEquals(new URI(ourWfsUrl).normalize().toString(), urls.get(0).toString());
    }

    /**
     * Tests getting URLs but the data has invalid URL.
     *
     * @throws XPathExpressionException Bad path.
     * @throws ParserConfigurationException Bad parse.
     * @throws JAXBException Bad JAXB.
     */
    @Test
    public void testGetUrlsBadUrl() throws XPathExpressionException, ParserConfigurationException, JAXBException
    {
        Node node = StateNodeUtils.createWfsNodeWithData("bad url");

        Toolbox toolbox = EasyMock.createNiceMock(Toolbox.class);
        PluginToolboxRegistry registry = EasyMock.createNiceMock(PluginToolboxRegistry.class);
        ServerToolbox serverToolbox = EasyMock.createNiceMock(ServerToolbox.class);
        WFSLayerConfigurationManager manager = new WFSLayerConfigurationManager();

        EasyMock.expect(toolbox.getPluginToolboxRegistry()).andReturn(registry).anyTimes();
        EasyMock.expect(registry.getPluginToolbox(ServerToolbox.class)).andReturn(serverToolbox).anyTimes();
        EasyMock.expect(serverToolbox.getLayerConfigurationManager()).andReturn(manager).anyTimes();

        EasyMock.replay(toolbox, registry, serverToolbox);
        WfsUrlRetriever wfsRetriever = new WfsUrlRetriever(toolbox);
        List<URL> urls = wfsRetriever.getUrls(node);

        assertEquals(0, urls.size());
    }

    /**
     * Test getting URLs without layer groups.
     *
     * @throws ParserConfigurationException Bad parse.
     */
    @Test
    public void testGetUrlsNoDataLayerGroup() throws ParserConfigurationException
    {
        Node node = StateNodeUtils.createStateNode();

        Toolbox toolbox = EasyMock.createNiceMock(Toolbox.class);
        PluginToolboxRegistry registry = EasyMock.createNiceMock(PluginToolboxRegistry.class);
        ServerToolbox serverToolbox = EasyMock.createNiceMock(ServerToolbox.class);
        WFSLayerConfigurationManager manager = new WFSLayerConfigurationManager();

        EasyMock.expect(toolbox.getPluginToolboxRegistry()).andReturn(registry).anyTimes();
        EasyMock.expect(registry.getPluginToolbox(ServerToolbox.class)).andReturn(serverToolbox).anyTimes();
        EasyMock.expect(serverToolbox.getLayerConfigurationManager()).andReturn(manager).anyTimes();

        EasyMock.replay(toolbox, registry, serverToolbox);
        WfsUrlRetriever wfsRetriever = new WfsUrlRetriever(toolbox);
        List<URL> urls = wfsRetriever.getUrls(node);

        assertEquals(0, urls.size());
    }

    /**
     * Tests getting URLs without feature layers.
     *
     * @throws XPathExpressionException Bad path.
     * @throws ParserConfigurationException Bad parse.
     * @throws JAXBException Bad JAXB.
     */
    @Test
    public void testGetUrlsNoWfsLayers() throws XPathExpressionException, ParserConfigurationException, JAXBException
    {
        Node node = StateNodeUtils.createNodeWithWMSLayers();

        Toolbox toolbox = EasyMock.createNiceMock(Toolbox.class);
        PluginToolboxRegistry registry = EasyMock.createNiceMock(PluginToolboxRegistry.class);
        ServerToolbox serverToolbox = EasyMock.createNiceMock(ServerToolbox.class);
        WFSLayerConfigurationManager manager = new WFSLayerConfigurationManager();

        EasyMock.expect(toolbox.getPluginToolboxRegistry()).andReturn(registry).anyTimes();
        EasyMock.expect(registry.getPluginToolbox(ServerToolbox.class)).andReturn(serverToolbox).anyTimes();
        EasyMock.expect(serverToolbox.getLayerConfigurationManager()).andReturn(manager).anyTimes();

        EasyMock.replay(toolbox, registry, serverToolbox);
        WfsUrlRetriever wfsRetriever = new WfsUrlRetriever(toolbox);
        List<URL> urls = wfsRetriever.getUrls(node);

        assertEquals(0, urls.size());
    }
}

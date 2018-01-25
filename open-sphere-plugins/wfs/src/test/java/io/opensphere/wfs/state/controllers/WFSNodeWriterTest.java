package io.opensphere.wfs.state.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import io.opensphere.core.modulestate.ModuleStateController;
import io.opensphere.core.modulestate.StateXML;
import io.opensphere.core.util.collections.New;
import io.opensphere.server.customization.ArcGisCustomization;
import io.opensphere.server.customization.DefaultCustomization;
import io.opensphere.server.customization.GeoServerCustomization;
import io.opensphere.server.state.DefaultWFSLayerConfiguration;
import io.opensphere.server.state.StateConstants;
import io.opensphere.server.toolbox.LayerConfiguration;
import io.opensphere.server.toolbox.WFSLayerConfigurationManager;
import io.opensphere.wfs.state.model.WFSLayerState;

/**
 * Tests the WFS node writer.
 */
public class WFSNodeWriterTest
{
    /** The layer configuration manager used in the test. */
    private WFSLayerConfigurationManager myLayerConfigurationManager;

    /** Configures necessary test resources. */
    @Before
    public void setup()
    {
        myLayerConfigurationManager = new WFSLayerConfigurationManager();

        myLayerConfigurationManager
                .addServerConfiguration(new DefaultWFSLayerConfiguration(StateConstants.WFS_LAYER_TYPE, DefaultCustomization.class));
        myLayerConfigurationManager.addServerConfiguration(new DefaultWFSLayerConfiguration("arcWFS", ArcGisCustomization.class, true));
        myLayerConfigurationManager
                .addServerConfiguration(new DefaultWFSLayerConfiguration("geoserverWFS", GeoServerCustomization.class, true));
    }

    /**
     * Test.
     *
     * @throws JAXBException the jAXB exception
     * @throws ParserConfigurationException the parser configuration exception
     * @throws XPathExpressionException the x path expression exception
     */
    @Test
    public void test() throws JAXBException, ParserConfigurationException, XPathExpressionException
    {
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Node node = doc.appendChild(StateXML.createElement(doc, ModuleStateController.STATE_NAME));

        List<WFSLayerState> layerStates = New.list();
        for (LayerConfiguration configuration : myLayerConfigurationManager.getAllConfigurations())
        {
            createLayerStates(layerStates, configuration.getName());
        }

        WFSNodeWriter.writeToNode(node, layerStates);

        NodeList nodes = node.getFirstChild().getChildNodes();

        assertTrue(nodes.getLength() == 3);
        assertEquals("wfs", nodes.item(0).getAttributes().getNamedItem("type").getTextContent());
        assertEquals("geoserverWFS", nodes.item(1).getAttributes().getNamedItem("type").getTextContent());
        assertEquals("arcWFS", nodes.item(2).getAttributes().getNamedItem("type").getTextContent());
    }

    /**
     * Creates the layer states.
     *
     * @param layerStates the layer states
     * @param type the type
     */
    private void createLayerStates(List<WFSLayerState> layerStates, String type)
    {
        WFSLayerState layerState = new WFSLayerState();
        layerState.setType(type);
        layerStates.add(layerState);
    }
}

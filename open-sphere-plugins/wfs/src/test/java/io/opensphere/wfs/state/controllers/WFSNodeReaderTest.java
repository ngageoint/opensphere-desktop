package io.opensphere.wfs.state.controllers;

import static org.junit.Assert.assertEquals;

import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import io.opensphere.core.modulestate.ModuleStateController;
import io.opensphere.core.modulestate.StateXML;
import io.opensphere.core.util.XMLUtilities;
import io.opensphere.server.customization.ArcGisCustomization;
import io.opensphere.server.customization.DefaultCustomization;
import io.opensphere.server.customization.GeoServerCustomization;
import io.opensphere.server.state.DefaultWFSLayerConfiguration;
import io.opensphere.server.state.StateConstants;
import io.opensphere.server.toolbox.LayerConfiguration;
import io.opensphere.server.toolbox.WFSLayerConfigurationManager;
import io.opensphere.wfs.state.model.WFSLayerState;

/**
 * Tests the WFSNodeReader.
 */
public class WFSNodeReaderTest
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
        Node node = createWFSNodes();

        WFSNodeReader reader = new WFSNodeReader(myLayerConfigurationManager);
        List<WFSLayerState> states = reader.readNode(node);

        assertEquals(3, states.size());
        assertEquals("wfs", states.get(0).getType());
        assertEquals("geoserverWFS", states.get(1).getType());
        assertEquals("arcWFS", states.get(2).getType());
    }

    /**
     * Creates a layer element for each WFS layer type and add it to the doc.
     *
     * @param doc the doc
     * @param node the node
     * @param type the type
     * @throws JAXBException the jAXB exception
     */
    private void createLayerElement(Document doc, Node node, String type) throws JAXBException
    {
        Element element = StateXML.createElement(doc, "layer");
        element.setAttribute("type", type);
        WFSLayerState layerState = new WFSLayerState();
        layerState.setType(type);
        XMLUtilities.marshalJAXBObjectToElement(layerState, element);
        node.appendChild(element);
    }

    /**
     * Creates a WFS type node for each of the types in the WFSLayerTypes.
     *
     * @return the node
     * @throws ParserConfigurationException the parser configuration exception
     * @throws JAXBException the jAXB exception
     * @throws XPathExpressionException the x path expression exception
     */
    private Node createWFSNodes() throws ParserConfigurationException, JAXBException, XPathExpressionException
    {
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Node stateNode = doc.appendChild(StateXML.createElement(doc, ModuleStateController.STATE_NAME));

        Element layersNode = (Element)stateNode.appendChild(StateXML.createElement(doc, "layers"));
        layersNode.setAttribute("type", StateConstants.DATA_LAYERS_TYPE);
        for (LayerConfiguration configuration : myLayerConfigurationManager.getAllConfigurations())
        {
            createLayerElement(doc, layersNode, configuration.getName());
        }

        stateNode.appendChild(layersNode);

        return stateNode;
    }
}

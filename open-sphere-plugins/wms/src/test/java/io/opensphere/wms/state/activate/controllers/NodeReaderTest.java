package io.opensphere.wms.state.activate.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import io.opensphere.core.modulestate.ModuleStateController;
import io.opensphere.core.modulestate.StateXML;
import io.opensphere.core.util.XMLUtilities;
import io.opensphere.server.state.StateConstants;
import io.opensphere.wms.state.model.WMSLayerState;

/**
 * Tests the NodeReader class.
 *
 */
public class NodeReaderTest
{
    /**
     * The first data layer name.
     */
    private static final String ourDataLayerName1 = "dataLayer1";

    /**
     * The second data layer name.
     */
    private static final String ourDataLayerName2 = "dataLayer2";

    /**
     * The first map layer name.
     */
    private static final String ourMapLayerName1 = "mapLayer1";

    /**
     * The second map layer name.
     */
    private static final String ourMapLayerName2 = "mapLayer2";

    /**
     * Tests the can activate method.
     *
     * @throws JAXBException Bad jaxb.
     * @throws ParserConfigurationException Bad parse.
     * @throws XPathExpressionException bad xpath.
     */
    @Test
    public void testCanActivate() throws XPathExpressionException, ParserConfigurationException, JAXBException
    {
        Node node = createStateNodeWithGroups();

        NodeReader reader = new NodeReader();
        boolean actual = reader.canActivateState(node, false);

        assertTrue(actual);

        node = createStateNodeWithoutMapGroup();

        actual = reader.canActivateState(node, false);

        assertFalse(actual);

        actual = reader.canActivateState(node, true);

        assertTrue(actual);
    }

    /**
     * Tests the can activate when expecting false.
     *
     * @throws ParserConfigurationException Bad parse.
     */
    @Test
    public void testCanActivateJustGroups() throws ParserConfigurationException
    {
        Node node = createStateNodeWithDataGroup();

        NodeReader reader = new NodeReader();
        boolean actual = reader.canActivateState(node, true);

        assertFalse(actual);
    }

    /**
     * Tests the can activate when expecting false.
     *
     * @throws ParserConfigurationException Bad parse.
     */
    @Test
    public void testCanActivateJustLayers() throws ParserConfigurationException
    {
        Node node = createStateNode();
        Document doc = node.getOwnerDocument();
        Element layers = StateXML.createElement(doc, "layers");
        node.appendChild(layers);

        NodeReader reader = new NodeReader();
        boolean actual = reader.canActivateState(node, true);

        assertFalse(actual);

        actual = reader.canActivateState(node, false);

        assertFalse(actual);
    }

    /**
     * Tests the can activate when expecting false.
     *
     * @throws ParserConfigurationException Bad parse.
     */
    @Test
    public void testCanActivateNothing() throws ParserConfigurationException
    {
        Node node = createStateNode();

        NodeReader reader = new NodeReader();
        boolean actual = reader.canActivateState(node, true);

        assertFalse(actual);

        actual = reader.canActivateState(node, false);

        assertFalse(actual);
    }

    /**
     * Tests reading a state node and retrieving the WMS data layer states.
     *
     * @throws ParserConfigurationException Bad parse.
     * @throws JAXBException Bad jaxb.
     * @throws XPathExpressionException Bad xpath.
     */
    @Test
    public void testReadNodeData() throws ParserConfigurationException, JAXBException, XPathExpressionException
    {
        Node node = createStateNodeWithGroups();

        NodeReader reader = new NodeReader();
        List<WMSLayerState> layerStates = reader.readNode(node, true);

        assertEquals(ourDataLayerName1, layerStates.get(0).getId());
        assertEquals(ourDataLayerName2, layerStates.get(1).getId());
    }

    /**
     * Tests reading a state node and retrieving the WMS map layer states.
     *
     * @throws ParserConfigurationException Bad parse.
     * @throws JAXBException Bad jaxb.
     * @throws XPathExpressionException Bad xpath.
     */
    @Test
    public void testReadNodeMap() throws ParserConfigurationException, JAXBException, XPathExpressionException
    {
        Node node = createStateNodeWithGroups();

        NodeReader reader = new NodeReader();
        List<WMSLayerState> layerStates = reader.readNode(node, false);

        assertEquals(ourMapLayerName1, layerStates.get(0).getId());
        assertEquals(ourMapLayerName2, layerStates.get(1).getId());
    }

    /**
     * Tests reading a state node without any layer group information.
     *
     * @throws JAXBException Bad jaxb.
     * @throws ParserConfigurationException Bad parse.
     * @throws XPathExpressionException Bad path.
     */
    @Test
    public void testReadNodeNoLayerGroup() throws ParserConfigurationException, JAXBException, XPathExpressionException
    {
        Node node = createStateNodeWithoutMapGroup();

        NodeReader reader = new NodeReader();
        List<WMSLayerState> layerStates = reader.readNode(node, false);

        assertTrue(layerStates.isEmpty());
    }

    /**
     * Tests reading a state node without a layers element.
     *
     * @throws ParserConfigurationException Bad parse.
     */
    @Test
    public void testReadNodeNoLayers() throws ParserConfigurationException
    {
        Node node = createStateNode();

        NodeReader reader = new NodeReader();
        List<WMSLayerState> layerStates = reader.readNode(node, false);

        assertTrue(layerStates.isEmpty());
    }

    /**
     * Creates the data layer elements and adds them to the layer group node.
     *
     * @param groupNode The layer group node.
     * @throws JAXBException Bad jaxb.
     */
    private void createDataLayerNodes(Node groupNode) throws JAXBException
    {
        WMSLayerState layerState = new WMSLayerState();
        layerState.setId(ourDataLayerName1);
        XMLUtilities.marshalJAXBObjectToElement(layerState, groupNode);

        layerState = new WMSLayerState();
        layerState.setId(ourDataLayerName2);
        XMLUtilities.marshalJAXBObjectToElement(layerState, groupNode);
    }

    /**
     * Creates the map layer elements and adds them to the layer group node.
     *
     * @param groupNode The layer group node.
     * @throws JAXBException Bad Jaxb.
     */
    private void createMapLayerNodes(Node groupNode) throws JAXBException
    {
        WMSLayerState layerState = new WMSLayerState();
        layerState.setId(ourMapLayerName1);
        XMLUtilities.marshalJAXBObjectToElement(layerState, groupNode);

        layerState = new WMSLayerState();
        layerState.setId(ourMapLayerName2);
        XMLUtilities.marshalJAXBObjectToElement(layerState, groupNode);
    }

    /**
     * Creates an empty state node.
     *
     * @return The state node.
     * @throws ParserConfigurationException Bad parse.
     */
    private Node createStateNode() throws ParserConfigurationException
    {
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Node stateNode = doc.appendChild(StateXML.createElement(doc, ModuleStateController.STATE_NAME));

        return stateNode;
    }

    /**
     * Creates all nodes up to the layer group for data layers.
     *
     * @return The root state node.
     * @throws ParserConfigurationException Bad parse.
     */
    private Node createStateNodeWithDataGroup() throws ParserConfigurationException
    {
        Node stateNode = createStateNode();

        Document doc = stateNode.getOwnerDocument();

        Element layersElement = StateXML.createElement(doc, "layers");
        layersElement.setAttribute("type", StateConstants.DATA_LAYERS_TYPE);
        stateNode.appendChild(layersElement);

        return stateNode;
    }

    /**
     * Creates a state node with all layer groups.
     *
     * @return The state node.
     * @throws ParserConfigurationException Bad parse.
     * @throws JAXBException Bad jaxb.
     * @throws XPathExpressionException Bad xpath.
     */
    private Node createStateNodeWithGroups() throws ParserConfigurationException, JAXBException, XPathExpressionException
    {
        Node stateNode = createStateNodeWithoutMapGroup();

        Document doc = stateNode.getOwnerDocument();

        Element mapGroup = StateXML.createElement(doc, "layers");
        mapGroup.setAttribute("type", StateConstants.MAP_LAYERS_TYPE);

        stateNode.appendChild(mapGroup);

        createMapLayerNodes(mapGroup);

        return stateNode;
    }

    /**
     * Creates a state no with just the data layers layer group.
     *
     * @return The state node.
     * @throws ParserConfigurationException Bad parse.
     * @throws JAXBException Bad jaxb.
     * @throws XPathExpressionException Bad path.
     */
    private Node createStateNodeWithoutMapGroup() throws ParserConfigurationException, JAXBException, XPathExpressionException
    {
        Node stateNode = createStateNodeWithDataGroup();

        Node dataGroup = StateXML.getChildNode(stateNode, "/" + ModuleStateController.STATE_QNAME + "/:layers");

        createDataLayerNodes(dataGroup);

        return stateNode;
    }
}

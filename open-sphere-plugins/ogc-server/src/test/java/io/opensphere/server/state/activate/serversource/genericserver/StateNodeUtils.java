package io.opensphere.server.state.activate.serversource.genericserver;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import io.opensphere.core.modulestate.ModuleStateController;
import io.opensphere.core.modulestate.StateXML;
import io.opensphere.core.util.XMLUtilities;
import io.opensphere.server.state.StateConstants;

/**
 * Constains state node create methods used for the tests.
 */
public final class StateNodeUtils
{
    /**
     * The wms url.
     */
    public static final String WMS_URL = "http://somehost/ogc/wms";

    /**
     * Creates a state node with just wms layers.
     *
     * @return The state node.
     * @throws ParserConfigurationException Bad parse.
     * @throws XPathExpressionException Bad xpath.
     * @throws JAXBException Bad jaxb.
     */
    public static Node createNodeWithWMSLayers() throws ParserConfigurationException, XPathExpressionException, JAXBException
    {
        Node stateNode = createStateNodeWithDataGroup();
        Node dataGroup = StateXML.getChildNode(stateNode, StateConstants.DATA_LAYERS_PATH);
        WMSLayerMockUp wmsLayer = new WMSLayerMockUp();
        wmsLayer.setWmsUrl(WMS_URL);
        XMLUtilities.marshalJAXBObjectToElement(wmsLayer, dataGroup);
        Node mapGroup = StateXML.getChildNode(stateNode, StateConstants.MAP_LAYERS_PATH);
        XMLUtilities.marshalJAXBObjectToElement(wmsLayer, mapGroup);
        return stateNode;
    }

    /**
     * Creates the state node with just a map layer group.
     *
     * @return The state node.
     * @throws ParserConfigurationException Bad parse.
     */
    public static Node createStateNode() throws ParserConfigurationException
    {
        Document document = XMLUtilities.newDocument();
        Element element = StateXML.createElement(document, ModuleStateController.STATE_NAME);
        document.appendChild(element);
        Element mapLayers = StateXML.createElement(document, StateConstants.LAYERS_NAME);
        mapLayers.setAttribute("type", StateConstants.MAP_LAYERS_TYPE);
        element.appendChild(mapLayers);

        return element;
    }

    /**
     * Creates a state node with map and data layer groups.
     *
     * @return The state node.
     * @throws ParserConfigurationException Bad parse.
     * @throws XPathExpressionException Bad xpath.
     */
    public static Node createStateNodeWithDataGroup() throws ParserConfigurationException, XPathExpressionException
    {
        Node stateNode = createStateNode();
        Document document = stateNode.getOwnerDocument();
        Element dataGroup = StateXML.createElement(document, "layers");
        dataGroup.setAttribute("type", "data");
        stateNode.appendChild(dataGroup);
        return stateNode;
    }

    /**
     * Creates a state node with both wms layers and feature layers.
     *
     * @param url The url to use for the feature layer.
     * @return The state node.
     * @throws ParserConfigurationException Bad parse.
     * @throws XPathExpressionException Bad xpath.
     * @throws JAXBException Bad jaxb.
     */
    public static Node createWfsNodeWithData(String url)
        throws XPathExpressionException, ParserConfigurationException, JAXBException
    {
        Node stateNode = createNodeWithWMSLayers();
        Node dataGroup = StateXML.getChildNode(stateNode, StateConstants.DATA_LAYERS_PATH);
        FeatureLayerMockUp wfsLayer = new FeatureLayerMockUp();
        wfsLayer.setUrl(url);
        XMLUtilities.marshalJAXBObjectToElement(wfsLayer, dataGroup);
        return stateNode;
    }

    /**
     * Not constructible.
     */
    private StateNodeUtils()
    {
    }
}

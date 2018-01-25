package io.opensphere.wms.state.activate.controllers;

import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import io.opensphere.core.modulestate.StateXML;
import io.opensphere.core.util.XMLUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.server.state.StateConstants;
import io.opensphere.wms.state.model.WMSLayerState;

/**
 * Reads a state node and pulls out all WMSLayerState information.
 */
public class NodeReader
{
    /**
     * Used to log messages.
     */
    private static final Logger LOGGER = Logger.getLogger(NodeReader.class);

    /**
     * Checks to see if node contains the necessary data so that the layers can
     * be activated.
     *
     * @param node The node to inspect.
     * @param activateDataLayer True if it should inspect for data layer
     *            information, or if it should look for map layer information.
     * @return True if node contains the layer information, false otherwise.
     */
    public boolean canActivateState(Node node, boolean activateDataLayer)
    {
        boolean canActivate = false;

        try
        {
            String path = activateDataLayer ? StateConstants.WMS_DATA_LAYER_PATH : StateConstants.WMS_MAP_LAYERS_PATH;

            NodeList wmsNodes = StateXML.getChildNodes(node, path);

            canActivate = wmsNodes.getLength() > 0;
        }
        catch (XPathExpressionException e)
        {
            LOGGER.error(e.getMessage(), e);
        }

        return canActivate;
    }

    /**
     * Reads the WMS layer state information from the specified node.
     *
     * @param node The node containing wms layer state information.
     * @param activateDataLayer True if WMS data layers should be read, false if
     *            WMS map layers should be read.
     * @return The WMS layer states read from the node.
     */
    public List<WMSLayerState> readNode(Node node, boolean activateDataLayer)
    {
        List<WMSLayerState> layerStates = New.list();

        try
        {
            NodeList children = null;

            String path = activateDataLayer ? StateConstants.WMS_DATA_LAYER_PATH : StateConstants.WMS_MAP_LAYERS_PATH;
            children = StateXML.getChildNodes(node, path);

            if (children != null)
            {
                for (int i = 0; i < children.getLength(); i++)
                {
                    Node child = children.item(i);
                    try
                    {
                        WMSLayerState layerState = XMLUtilities.readXMLObject(child, WMSLayerState.class);
                        layerStates.add(layerState);
                    }
                    catch (JAXBException e)
                    {
                        StringBuilder sb = new StringBuilder();
                        sb.append("Failed to read WMS layer state for index ");
                        sb.append(i);
                        sb.append(", isDataLayer = ");
                        sb.append(activateDataLayer);
                        sb.append(e.getMessage());
                        LOGGER.error(sb.toString(), e);
                    }
                }
            }
        }
        catch (XPathExpressionException e)
        {
            LOGGER.error(e.getMessage(), e);
        }

        return layerStates;
    }
}

package io.opensphere.wfs.state.controllers;

import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import io.opensphere.core.modulestate.StateXML;
import io.opensphere.core.util.XMLUtilities;
import io.opensphere.server.state.StateConstants;
import io.opensphere.wfs.state.model.WFSLayerState;

/**
 * Given a set of WFSLayerState models, this class will write the models to a
 * specified node.
 */
public final class WFSNodeWriter
{
    /** The Constant LOGGER. */
    private static final Logger LOGGER = Logger.getLogger(WFSNodeWriter.class);

    /**
     * Write to node.
     *
     * @param node the node
     * @param layerStates the layer states
     */
    public static void writeToNode(Node node, List<WFSLayerState> layerStates)
    {
        Document doc = node instanceof Document ? (Document)node : node.getOwnerDocument();
        try
        {
            Element dataLayerNode = (Element)StateXML.createChildNode(node, doc, node, StateConstants.DATA_LAYERS_PATH,
                    StateConstants.LAYERS_NAME);
            dataLayerNode.setAttribute("type", StateConstants.DATA_LAYERS_TYPE);
            addToNode(dataLayerNode, layerStates);
        }
        catch (XPathExpressionException e)
        {
            LOGGER.error(e.getMessage(), e);
        }
    }

    /**
     * Adds the marshalled layer states to the specified node.
     *
     * @param node The node to add elements to.
     * @param layerStates The layer states to marshal.
     */
    private static void addToNode(Node node, List<WFSLayerState> layerStates)
    {
        for (WFSLayerState layerState : layerStates)
        {
            try
            {
                XMLUtilities.marshalJAXBObjectToElement(layerState, node);
            }
            catch (JAXBException e)
            {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

    /** Disallow instantiation. */
    private WFSNodeWriter()
    {
    }
}

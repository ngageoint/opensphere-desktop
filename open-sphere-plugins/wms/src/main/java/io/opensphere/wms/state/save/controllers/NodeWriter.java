package io.opensphere.wms.state.save.controllers;

import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.modulestate.StateXML;
import io.opensphere.core.util.XMLUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.LoadsTo;
import io.opensphere.server.state.StateConstants;
import io.opensphere.wms.layer.WMSLayerValueProvider;
import io.opensphere.wms.state.model.WMSLayerAndState;
import io.opensphere.wms.state.model.WMSLayerState;

/**
 * Given a set of WMSLayer models, this class will write the models to a
 * specified node.
 */
public class NodeWriter
{
    /**
     * Used to log messages.
     */
    private static final Logger LOGGER = Logger.getLogger(NodeWriter.class);

    /**
     * Writes the specified layer states to the node.
     *
     * @param node The node to write to.
     * @param layerStates Contains the values to write to the node.
     * @param saveDataLayers True if the data layers should be saved, false if
     *            the map layers should be saved.
     */
    public void writeToNode(Node node, List<WMSLayerAndState> layerStates, boolean saveDataLayers)
    {
        List<WMSLayerState> dataLayers = New.list();
        List<WMSLayerState> mapLayers = New.list();

        segregateLayers(layerStates, dataLayers, mapLayers);

        Document doc = node instanceof Document ? (Document)node : node.getOwnerDocument();

        try
        {
            if (saveDataLayers)
            {
                if (!dataLayers.isEmpty())
                {
                    Element dataLayerNode = (Element)StateXML.createChildNode(node, doc, node, StateConstants.DATA_LAYERS_PATH,
                            StateConstants.LAYERS_NAME);
                    dataLayerNode.setAttribute("type", StateConstants.DATA_LAYERS_TYPE);
                    addToNode(dataLayerNode, dataLayers);
                }
            }
            else if (!mapLayers.isEmpty())
            {
                Element mapLayerNode = (Element)StateXML.createChildNode(node, doc, node, StateConstants.MAP_LAYERS_PATH,
                        StateConstants.LAYERS_NAME);
                mapLayerNode.setAttribute("type", StateConstants.MAP_LAYERS_TYPE);
                addToNode(mapLayerNode, mapLayers);
            }
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
    private void addToNode(Node node, List<WMSLayerState> layerStates)
    {
        for (WMSLayerState layerState : layerStates)
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

    /**
     * Separates the map layer states from the data layer states.
     *
     * @param layerStates The layers to separate.
     * @param dataLayers The list to add data layer states to.
     * @param mapLayers The list to add map layer states to.
     */
    public static void segregateLayers(List<WMSLayerAndState> layerStates, List<WMSLayerState> dataLayers,
            List<WMSLayerState> mapLayers)
    {
        for (WMSLayerAndState layerState : layerStates)
        {
            WMSLayerValueProvider layer = layerState.getLayer();
            DataTypeInfo dataType = layer.getTypeInfo();
            if (dataType.getMetaDataInfo() == null
                    && (dataType.getTimeExtents() == null || dataType.getTimeExtents().getExtent().equals(TimeSpan.ZERO))
                    && dataType.getBasicVisualizationInfo().getLoadsTo() == LoadsTo.BASE)
            {
                mapLayers.add(layerState.getState());
            }
            else
            {
                dataLayers.add(layerState.getState());
            }
        }
    }
}

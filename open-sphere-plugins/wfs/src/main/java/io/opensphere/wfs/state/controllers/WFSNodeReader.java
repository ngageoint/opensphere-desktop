package io.opensphere.wfs.state.controllers;

import java.util.Collection;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import io.opensphere.core.modulestate.StateXML;
import io.opensphere.core.util.XMLUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.server.toolbox.LayerConfiguration;
import io.opensphere.server.toolbox.WFSLayerConfigurationManager;
import io.opensphere.wfs.state.model.WFSLayerState;

/**
 * Reads a state node and create WFSLayerStates.
 */
public class WFSNodeReader
{
    /** The Constant LOGGER. */
    private static final Logger LOGGER = Logger.getLogger(WFSNodeReader.class);

    /**
     * The configuration manager used to locate state file-related
     * configurations.
     */
    private WFSLayerConfigurationManager myStateConfigurationManager;

    /**
     * Creates a new node reader, using the supplied configuration manager to
     * locate the WFS-specific state configuration.
     *
     * @param stateConfigurationManager The configuration manager used to locate
     *            state file-related configurations.
     */
    public WFSNodeReader(WFSLayerConfigurationManager stateConfigurationManager)
    {
        myStateConfigurationManager = stateConfigurationManager;
    }

    /**
     * Gets the value of the {@link #myStateConfigurationManager} field.
     *
     * @return the value stored in the {@link #myStateConfigurationManager}
     *         field.
     */
    protected WFSLayerConfigurationManager getStateConfigurationManager()
    {
        return myStateConfigurationManager;
    }

    /**
     * Read node.
     *
     * @param node the node
     * @return the list
     */
    public List<WFSLayerState> readNode(Node node)
    {
        List<WFSLayerState> layerStates = New.list();
        findChildren(node, layerStates);
        return layerStates;
    }

    /**
     * Creates the layer states.
     *
     * @param layerStates the layer states
     * @param children the children
     */
    private void createLayerStates(List<WFSLayerState> layerStates, NodeList children)
    {
        if (children != null)
        {
            for (int i = 0; i < children.getLength(); i++)
            {
                Node child = children.item(i);

                try
                {
                    WFSLayerState layerState = XMLUtilities.readXMLObject(child, WFSLayerState.class);
                    layerStates.add(layerState);
                }
                catch (JAXBException e)
                {
                    LOGGER.error(e.getMessage(), e);
                }
            }
        }
    }

    /**
     * Find children given the set of paths in the WFSLayerPaths enum.
     *
     * @param node the node
     * @param layerStates the layer states
     */
    private void findChildren(Node node, List<WFSLayerState> layerStates)
    {
        try
        {
            Collection<LayerConfiguration> configurations = getStateConfigurationManager().getAllConfigurations();
            for (LayerConfiguration configuration : configurations)
            {
                createLayerStates(layerStates, StateXML.getChildNodes(node, configuration.getStateXPath()));
            }
        }
        catch (XPathExpressionException e)
        {
            LOGGER.error(e.getMessage(), e);
        }
    }
}

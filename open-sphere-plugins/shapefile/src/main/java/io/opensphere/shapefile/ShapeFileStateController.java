package io.opensphere.shapefile;

import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.xml.bind.JAXBException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.bitsys.fade.mist.state.v4.LayerType;
import com.bitsys.fade.mist.state.v4.LayersType;
import com.bitsys.fade.mist.state.v4.StateType;

import io.opensphere.core.Toolbox;
import io.opensphere.core.modulestate.AbstractLayerStateController;
import io.opensphere.core.modulestate.StateUtilities;
import io.opensphere.core.modulestate.StateXML;
import io.opensphere.core.util.XMLUtilities;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.net.UrlUtilities;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.mantle.controller.DataGroupController;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.util.MantleToolboxUtils;
import io.opensphere.shapefile.ShapeFileImporter.CallbackCaller;
import io.opensphere.shapefile.config.v1.ShapeFileSource;

/**
 * Handles Shape File state activation, de activation and determines if a state
 * can be activated.
 */
public class ShapeFileStateController extends AbstractLayerStateController<ShapeFileSource>
{
    /** The Constant LOGGER. */
    private static final Logger LOGGER = Logger.getLogger(ShapeFileStateController.class);

    /** The Data group controller. */
    private final DataGroupController myDataGroupController;

    /** The Envoy. */
    private final ShapeFileEnvoy myEnvoy;

    /** The Toolbox. */
    private final Toolbox myToolbox;

    /**
     * Instantiates a new shape file state controller.
     *
     * @param envoy the envoy
     */
    public ShapeFileStateController(ShapeFileEnvoy envoy)
    {
        myEnvoy = envoy;
        myToolbox = myEnvoy.getToolbox();
        myDataGroupController = MantleToolboxUtils.getMantleToolbox(myToolbox).getDataGroupController();
    }

    @Override
    public void activateState(String id, String description, Collection<? extends String> tags, Node node)
    {
        try
        {
            NodeList shpNodes = StateXML.getChildNodes(node, ShapeFileStateConstants.SHAPE_DATA_LAYER_PATH);
            for (int i = 0; i < shpNodes.getLength(); i++)
            {
                Node shpNode = shpNodes.item(i);
                NodeList shpSourceNodes = shpNode.getChildNodes();
                for (int j = 0; j < shpSourceNodes.getLength(); j++)
                {
                    Node shpSourceNode = shpSourceNodes.item(j);
                    if (ShapeFileStateConstants.SHAPE_LAYER_NAME.equals(shpSourceNode.getNodeName()))
                    {
                        try
                        {
                            ShapeFileSource shapeSource = XMLUtilities.readXMLObject(shpSourceNode, ShapeFileSource.class);
                            if (shapeSource != null)
                            {
                                String idDisplayName = "-(" + id + ")";
                                String displayName = shapeSource.getName() + idDisplayName;
                                shapeSource.setName(displayName);
                                shapeSource.setActive(true);
                                shapeSource.setTransient(true);

                                myEnvoy.getController().addSource(shapeSource);
                                addResource(id, shapeSource);
                            }
                        }
                        catch (JAXBException e)
                        {
                            LOGGER.error(e.getMessage(), e);
                        }
                    }
                }
            }
        }
        catch (XPathExpressionException e)
        {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Override
    public void activateState(String id, String description, Collection<? extends String> tags, StateType state)
        throws InterruptedException
    {
        List<LayerType> shapeLayers = StateUtilities.getLayers(state.getLocalData(), state.getDataLayers(),
                ShapeFileStateConstants.SHAPE_LAYER_TYPE);
        if (!shapeLayers.isEmpty())
        {
            CallbackCaller callback = (successful, source) ->
            {
                if (successful)
                {
                    addResource(id, source);
                }
            };

            for (LayerType layer : shapeLayers)
            {
                ShapeFileSource shapeSource = toDataSource(layer, id);
                SwingUtilities.invokeLater(() -> myEnvoy.getController().getFileImporter().importSource(shapeSource,
                        Collections.emptySet(), callback, ImportState.INTRODUCTION));
            }
        }
    }

    @Override
    public boolean canActivateState(Node node)
    {
        return StateXML.anyMatch(node, ShapeFileStateConstants.SHAPE_DATA_LAYER_PATH);
    }

    @Override
    public boolean canActivateState(StateType state)
    {
        return !StateUtilities.getLayers(state.getLocalData(), state.getDataLayers(), ShapeFileStateConstants.SHAPE_LAYER_TYPE)
                .isEmpty();
    }

    @Override
    public boolean canSaveState()
    {
        return !myDataGroupController.findActiveDataGroupInfo(new ShapeFileStateGroupFilter(), true).isEmpty();
    }

    @Override
    public boolean isSaveStateByDefault()
    {
        return true;
    }

    @Override
    public void saveState(Node node)
    {
        for (ShapeFileDataTypeInfo shapeDTI : getShapeFileDataTypes())
        {
            Document doc = node instanceof Document ? (Document)node : node.getOwnerDocument();
            try
            {
                Node layersNode = StateXML.createChildNode(node, doc, node, ShapeFileStateConstants.DATA_LAYERS_PATH,
                        ShapeFileStateConstants.DATA_LAYERS_NAME);
                ((Element)layersNode).setAttribute("type", "data");
                Node dataLayerNode = StateXML.getChildNode(node, ShapeFileStateConstants.SHAPE_PATH);
                if (dataLayerNode == null)
                {
                    dataLayerNode = StateXML.createElement(doc, ShapeFileStateConstants.LAYER_NAME);
                    ((Element)dataLayerNode).setAttribute("type", ShapeFileStateConstants.SHAPE_LAYER_TYPE);
                    layersNode.appendChild(dataLayerNode);
                }

                try
                {
                    ShapeFileSource shpSource = new ShapeFileSource(shapeDTI.getFileSource());
                    shpSource.setFromStateSource(true);
                    shpSource.setVisible(shapeDTI.isVisible());
                    XMLUtilities.marshalJAXBObjectToElement(shpSource, dataLayerNode);
                }
                catch (JAXBException e)
                {
                    LOGGER.error(e.getMessage(), e);
                }
            }
            catch (XPathExpressionException e)
            {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

    @Override
    public void saveState(StateType state)
    {
        Collection<ShapeFileDataTypeInfo> dataTypes = getShapeFileDataTypes();
        if (!dataTypes.isEmpty())
        {
            LayersType localData = StateUtilities.getLocalData(state);
            for (ShapeFileDataTypeInfo dataType : dataTypes)
            {
                LayerType layer = toLayer(dataType);
                localData.getLayer().add(layer);
            }
        }
    }

    @Override
    protected void deactivate(ShapeFileSource stateGroup)
    {
        DataGroupInfo dgi = myDataGroupController.getDataGroupInfo(stateGroup.generateTypeKey());
        dgi.activationProperty().setActive(false);
        myDataGroupController.removeDataGroupInfo(dgi, this);
        myEnvoy.getController().removeSource(stateGroup, true, null);
    }

    /**
     * Converts the ShapeFile data type to a state v4 layer.
     *
     * @param dataType the ShapeFile data type
     * @return the layer
     */
    private static LayerType toLayer(ShapeFileDataTypeInfo dataType)
    {
        ShapeFileSource dataSource = dataType.getFileSource();

        LayerType layer = new LayerType();
        URL url = UrlUtilities.toURLNew(dataSource.getPath());
        if (url != null)
        {
            layer.setUrl(url.toString());
        }
        layer.setTitle(dataSource.getName());
        layer.setType(ShapeFileStateConstants.SHAPE_LAYER_TYPE);
        layer.setVisible(dataSource.isVisible());
        layer.setBaseColor(StateUtilities.formatColor(dataSource.getShapeColor()));
        layer.setTemporal(Boolean.valueOf(dataType.getBasicVisualizationInfo().getLoadsTo().isTimelineEnabled()));
        layer.setAnalyze(Boolean.valueOf(dataType.getBasicVisualizationInfo().getLoadsTo().isAnalysisEnabled()));
        layer.setAnimate(Boolean.TRUE.toString());
        layer.setLoad(Boolean.TRUE);
        layer.setSpatial(Boolean.TRUE);

        return layer;
    }

    /**
     * Converts the state v4 layer to a shape file data source.
     *
     * @param layer the state v4 layer
     * @param stateId the state ID
     * @return the shape file data source
     */
    private static ShapeFileSource toDataSource(LayerType layer, String stateId)
    {
        ShapeFileSource dataSource = new ShapeFileSource();
        dataSource.setName(new StringBuilder(layer.getTitle()).append(" (").append(stateId).append(')').toString());
        dataSource.setActive(true);
        dataSource.setVisible(layer.isVisible());
        dataSource.setTransient(true);
        dataSource.setFromStateSource(true);
        dataSource.setPath(removeFilePrefix(layer.getUrl()));
//        dataSource.setSourceClassificationHeader(ch);
        // TODO
        return dataSource;
    }

    /**
     * Gets the ShapeFile data types.
     *
     * @return the ShapeFile data types
     */
    private Collection<ShapeFileDataTypeInfo> getShapeFileDataTypes()
    {
        Collection<ShapeFileDataTypeInfo> dataTypes = Collections.emptyList();

        Set<DataGroupInfo> dataGroups = myDataGroupController.findActiveDataGroupInfo(new ShapeFileStateGroupFilter(), false);
        validateDataGroups(dataGroups);
        Map<String, DataTypeInfo> uniqueDataTypes = getUniqueFiles(dataGroups);
        if (!uniqueDataTypes.isEmpty())
        {
            dataTypes = CollectionUtilities.filterDowncast(uniqueDataTypes.values(), ShapeFileDataTypeInfo.class);
        }

        return dataTypes;
    }

    /**
     * Make sure we have a unique set of shape file URL's so the state doesn't
     * contain the same file more than once.
     *
     * @param dataGroups the data groups
     * @return the unique files
     */
    private Map<String, DataTypeInfo> getUniqueFiles(Set<DataGroupInfo> dataGroups)
    {
        Map<String, DataTypeInfo> uniqueDataTypes = New.map();
        for (DataGroupInfo shapeGroup : dataGroups)
        {
            for (DataTypeInfo shapeType : shapeGroup.getMembers(false))
            {
                ShapeFileDataTypeInfo shapeDTI = (ShapeFileDataTypeInfo)shapeType;
                if (!uniqueDataTypes.containsKey(shapeDTI.getUrl()))
                {
                    if (shapeDTI.getFileSource().isFromStateSource())
                    {
                        String[] nameTok = shapeDTI.getFileSource().getName().split("-\\(");
                        shapeDTI.getFileSource().setName(nameTok[0]);
                    }
                    uniqueDataTypes.put(shapeDTI.getUrl(), shapeDTI);
                }
            }
        }
        return uniqueDataTypes;
    }

    /**
     * Validate data groups by making sure there is at least one.
     *
     * @param dataGroups the data groups
     */
    private void validateDataGroups(Set<DataGroupInfo> dataGroups)
    {
        if (dataGroups.isEmpty())
        {
            EventQueueUtilities.runOnEDT(new Runnable()
            {
                @Override
                public void run()
                {
                    JOptionPane.showMessageDialog(myToolbox.getUIRegistry().getMainFrameProvider().get(),
                            "There are no active Shape File layers to save.", "State Error", JOptionPane.ERROR_MESSAGE);
                }
            });
            return;
        }
    }

    /**
     * Checks for active Shape File layers.
     */
    public static class ShapeFileStateGroupFilter implements Predicate<DataGroupInfo>
    {
        @Override
        public boolean test(DataGroupInfo value)
        {
            if (value.hasMembers(false) && value.activationProperty().isActiveOrActivating())
            {
                for (DataTypeInfo info : value.getMembers(false))
                {
                    if (info instanceof ShapeFileDataTypeInfo)
                    {
                        return true;
                    }
                }
            }
            return false;
        }
    }
}

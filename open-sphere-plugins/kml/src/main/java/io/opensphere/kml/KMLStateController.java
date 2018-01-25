package io.opensphere.kml;

import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

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

import io.opensphere.core.modulestate.AbstractLayerStateController;
import io.opensphere.core.modulestate.StateUtilities;
import io.opensphere.core.modulestate.StateXML;
import io.opensphere.core.util.XMLUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.BooleanUtilities;
import io.opensphere.core.util.net.UrlUtilities;
import io.opensphere.kml.common.model.KMLDataSource;
import io.opensphere.kml.common.model.KMLDataSource.Type;
import io.opensphere.kml.datasource.controller.KMLDataSourceControllerImpl;
import io.opensphere.mantle.controller.DataGroupController;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.util.MantleToolboxUtils;

/**
 * Handles KML state activation, de activation and determines if a state can be
 * activated.
 */
public class KMLStateController extends AbstractLayerStateController<KMLDataSource>
{
    /** The Constant LOGGER. */
    private static final Logger LOGGER = Logger.getLogger(KMLStateController.class);

    /** The Controller. */
    private final KMLDataSourceControllerImpl myController;

    /** The Data group controller. */
    private final DataGroupController myDataGroupController;

    /**
     * Instantiates a new KML state controller.
     *
     * @param kmlController the kml controller
     */
    public KMLStateController(KMLDataSourceControllerImpl kmlController)
    {
        myController = kmlController;
        myDataGroupController = MantleToolboxUtils.getMantleToolbox(kmlController.getToolbox()).getDataGroupController();
    }

    @Override
    public void activateState(String id, String description, Collection<? extends String> tags, Node node)
    {
        List<Node> kmlSourceNodes = New.list();
        try
        {
            NodeList kmlNodes = StateXML.getChildNodes(node, KMLStateConstants.KML_DATA_LAYER_PATH);
            for (int i = 0; i < kmlNodes.getLength(); i++)
            {
                Node kmlNode = kmlNodes.item(i);
                NodeList children = kmlNode.getChildNodes();
                for (int j = 0; j < children.getLength(); j++)
                {
                    Node child = children.item(j);
                    if (KMLStateConstants.KML_LAYER_NAME.equals(child.getNodeName()))
                    {
                        kmlSourceNodes.add(child);
                    }
                }
            }
        }
        catch (XPathExpressionException e)
        {
            LOGGER.error(e.getMessage(), e);
        }

        for (Node kmlSourceNode : kmlSourceNodes)
        {
            try
            {
                KMLDataSource kmlSource = XMLUtilities.readXMLObject(kmlSourceNode, KMLDataSource.class);
                if (kmlSource != null)
                {
                    String idDisplayName = " (" + id + ")";
                    String displayName = kmlSource.getName() + idDisplayName;
                    kmlSource.setName(displayName);
                    kmlSource.setActive(true);
                    kmlSource.setTransient(true);
                    addSource(id, kmlSource);
                }
            }
            catch (JAXBException e)
            {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

    @Override
    public void activateState(String id, String description, Collection<? extends String> tags, StateType state)
        throws InterruptedException
    {
        List<LayerType> kmlLayers = StateUtilities.getLayers(state.getLocalData(), state.getDataLayers(),
                KMLStateConstants.KML_LAYER_TYPE);
        for (LayerType layer : kmlLayers)
        {
            KMLDataSource kmlSource = toDataSource(layer, id);
            addSource(id, kmlSource);
        }
    }

    @Override
    public boolean canActivateState(Node node)
    {
        return StateXML.anyMatch(node, KMLStateConstants.KML_DATA_LAYER_PATH);
    }

    @Override
    public boolean canActivateState(StateType state)
    {
        return !StateUtilities.getLayers(state.getLocalData(), state.getDataLayers(), KMLStateConstants.KML_LAYER_TYPE).isEmpty();
    }

    @Override
    public boolean canSaveState()
    {
        return !myDataGroupController.findActiveDataGroupInfo(new KMLStateGroupFilter(), true).isEmpty();
    }

    @Override
    public boolean isSaveStateByDefault()
    {
        return true;
    }

    @Override
    public void saveState(Node node)
    {
        for (DataTypeInfo kmlDTI : getKmlDataTypes())
        {
            Document doc = node instanceof Document ? (Document)node : node.getOwnerDocument();
            try
            {
                Node layersNode = StateXML.createChildNode(node, doc, node, KMLStateConstants.DATA_LAYERS_PATH,
                        KMLStateConstants.DATA_LAYERS_NAME);
                ((Element)layersNode).setAttribute("type", "data");
                Node dataLayerNode = StateXML.getChildNode(node, KMLStateConstants.KML_PATH);
                if (dataLayerNode == null)
                {
                    dataLayerNode = StateXML.createElement(doc, KMLStateConstants.LAYER_NAME);
                    ((Element)dataLayerNode).setAttribute("type", KMLStateConstants.KML_LAYER_TYPE);
                    layersNode.appendChild(dataLayerNode);
                }

                KMLDataSource kmlSource = getExportSource(kmlDTI);
                try
                {
                    XMLUtilities.marshalJAXBObjectToElement(kmlSource, dataLayerNode);
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
        Collection<DataTypeInfo> dataTypes = getKmlDataTypes();
        if (!dataTypes.isEmpty())
        {
            LayersType localData = StateUtilities.getLocalData(state);
            for (DataTypeInfo dataType : dataTypes)
            {
                KMLDataSource kmlSource = getExportSource(dataType);
                LayerType layer = toLayer(dataType, kmlSource);
                localData.getLayer().add(layer);
            }
        }
    }

    @Override
    protected void deactivate(KMLDataSource stateGroup)
    {
        DataGroupInfo dgi = myDataGroupController.getDataGroupInfo(stateGroup.getDataTypeKey());
        dgi.activationProperty().setActive(false);
        myDataGroupController.removeDataGroupInfo(dgi, this);
        myController.removeSource(stateGroup);
    }

    /**
     * Adds the data source to the controller and map.
     *
     * @param kmlSource the data source
     * @param id the state ID
     */
    private void addSource(String id, KMLDataSource kmlSource)
    {
        myController.addSource(kmlSource);
        addResource(id, kmlSource);
    }

    /**
     * Gets the KML data types.
     *
     * @return the KML data types
     */
    private Collection<DataTypeInfo> getKmlDataTypes()
    {
        Collection<DataTypeInfo> dataTypes;

        Set<DataGroupInfo> dataGroups = myDataGroupController.findActiveDataGroupInfo(new KMLStateGroupFilter(), false);
        Map<String, DataTypeInfo> uniqueDataTypes = getUniqueFiles(dataGroups);
        dataTypes = uniqueDataTypes.values();

        return dataTypes;
    }

    /**
     * Make sure we have a unique set of KML file URL's so the state doesn't
     * contain the same file more than once.
     *
     * @param dataGroups the data groups
     * @return the unique files
     */
    private Map<String, DataTypeInfo> getUniqueFiles(Set<DataGroupInfo> dataGroups)
    {
        // Make sure we have a unique set of file URL's so the state doesn't
        // contain the same file more than once.
        Map<String, DataTypeInfo> uniqueDataTypes = New.map();
        for (DataGroupInfo dgi : dataGroups)
        {
            for (DataTypeInfo dti : dgi.getMembers(false))
            {
                if ("KML".equals(dti.getSourcePrefix()) && !uniqueDataTypes.containsKey(dti.getUrl()))
                {
                    KMLDataSource kmlDataSource = myController.getDataSource(dti.getTypeKey());
                    if (kmlDataSource != null)
                    {
                        if (kmlDataSource.isFromStateSource())
                        {
                            String[] nameTok = kmlDataSource.getName().split("-\\(");
                            kmlDataSource.setName(nameTok[0]);
                        }
                        uniqueDataTypes.put(dti.getUrl(), dti);
                    }
                }
            }
        }
        return uniqueDataTypes;
    }

    /**
     * Gets the data source to export.
     *
     * @param dataType the data type
     * @return the data source
     */
    private KMLDataSource getExportSource(DataTypeInfo dataType)
    {
        KMLDataSource origSource = myController.getDataSource(dataType.getTypeKey());
        KMLDataSource kmlSource = origSource.createExportDataSource();
        kmlSource.setVisible(dataType.isVisible());
        kmlSource.setFromStateSource(true);
        return kmlSource;
    }

    /**
     * Converts the KML data type/source to a state v4 layer.
     *
     * @param dataType the KML data type
     * @param dataSource the KML data source
     * @return the layer
     */
    private static LayerType toLayer(DataTypeInfo dataType, KMLDataSource dataSource)
    {
        LayerType layer = new LayerType();
        URL url = UrlUtilities.toURLNew(dataSource.getPath());
        if (url != null)
        {
            layer.setUrl(url.toString());
        }
        layer.setTitle(dataSource.getName());
        layer.setType(KMLStateConstants.KML_LAYER_TYPE);
        layer.setVisible(dataSource.isVisible());
        layer.setTemporal(Boolean.valueOf(dataSource.isIncludeInTimeline()));
        layer.setAnalyze(Boolean.TRUE);
        layer.setAnimate(Boolean.TRUE.toString());
        layer.setLoad(Boolean.TRUE);
        layer.setSpatial(Boolean.TRUE);
        layer.setShowLabels(String.valueOf(dataSource.isShowLabels()));
        return layer;
    }

    /**
     * Converts the state v4 layer to a KML data source.
     *
     * @param layer the state v4 layer
     * @param stateId the state ID
     * @return the KML data source
     */
    private static KMLDataSource toDataSource(LayerType layer, String stateId)
    {
        KMLDataSource dataSource = new KMLDataSource();
        dataSource.setName(new StringBuilder(layer.getTitle()).append(" (").append(stateId).append(')').toString());
        dataSource.setActive(true);
        dataSource.setVisible(layer.isVisible());
        dataSource.setTransient(true);
        dataSource.setFromStateSource(true);
//        dataSource.setSourceClassificationHeader(ch);
        dataSource.setPath(removeFilePrefix(layer.getUrl()));
        dataSource.setType(layer.getUrl().startsWith("file:") ? Type.FILE : Type.URL);
        dataSource.setIncludeInTimeline(BooleanUtilities.toBoolean(layer.isTemporal()));
        if ("false".equals(layer.getShowLabels()))
        {
            dataSource.setShowLabels(false);
        }
        return dataSource;
    }

    /**
     * Checks for active KML layers.
     */
    protected static class KMLStateGroupFilter implements Predicate<DataGroupInfo>
    {
        @Override
        public boolean test(DataGroupInfo value)
        {
            if (value.hasMembers(false) && value.activationProperty().isActiveOrActivating())
            {
                for (DataTypeInfo info : value.getMembers(false))
                {
                    if ("KML".equals(info.getSourcePrefix()))
                    {
                        return true;
                    }
                }
            }
            return false;
        }
    }
}

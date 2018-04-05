package io.opensphere.csv;

import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import javax.xml.bind.JAXBException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.lang3.StringUtils;
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
import io.opensphere.core.util.JAXBContextHelper;
import io.opensphere.core.util.XMLUtilities;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.BooleanUtilities;
import io.opensphere.csv.config.v2.CSVDataSource;
import io.opensphere.csvcommon.CSVStateConstants;
import io.opensphere.csvcommon.config.v2.CSVColumnFormat;
import io.opensphere.csvcommon.config.v2.CSVDelimitedColumnFormat;
import io.opensphere.csvcommon.config.v2.CSVParseParameters;
import io.opensphere.mantle.controller.DataGroupController;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.util.MantleToolboxUtils;

/**
 * Handles CSV state activation, de activation and determines if a state can be
 * activated.
 */
public class CSVStateController extends AbstractLayerStateController<CSVDataSource>
{
    /** The Constant LOGGER. */
    private static final Logger LOGGER = Logger.getLogger(CSVStateController.class);

    /** The Data group controller. */
    private final DataGroupController myDataGroupController;

    /** The Envoy. */
    private final CSVEnvoy myEnvoy;

    /**
     * Instantiates a new cSV state controller.
     *
     * @param envoy the envoy
     */
    public CSVStateController(CSVEnvoy envoy)
    {
        myEnvoy = envoy;
        myDataGroupController = MantleToolboxUtils.getMantleToolbox(envoy.getToolbox()).getDataGroupController();
    }

    @Override
    public void activateState(String id, String description, Collection<? extends String> tags, Node node)
    {
        try
        {
            NodeList csvNodes = StateXML.getChildNodes(node, CSVStateConstants.CSV_DATA_LAYER_PATH);
            for (int i = 0; i < csvNodes.getLength(); i++)
            {
                Node csvNode = csvNodes.item(i);
                NodeList csvSourceNodes = csvNode.getChildNodes();
                for (int j = 0; j < csvSourceNodes.getLength(); j++)
                {
                    Node csvSourceNode = csvSourceNodes.item(j);
                    String nodeName = csvSourceNode.getNodeName();
                    if (nodeName != null && nodeName.startsWith("CSV") && nodeName.endsWith("Source"))
                    {
                        CSVDataSource csvSource = CSVConfigurationManager.getDataSource(csvSourceNode);
                        if (csvSource != null)
                        {
                            String idDisplayName = " (" + id + ")";
                            String displayName = csvSource.getName() + idDisplayName;
                            csvSource.setName(displayName);
                            csvSource.setActive(true);
                            csvSource.setTransient(true);
                            csvSource.setFromStateSource(true);

                            myEnvoy.getCSVFileController().addSource(csvSource);
                            addResource(id, csvSource);
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
        List<LayerType> csvLayers = StateUtilities.getLayers(state.getLocalData(), state.getDataLayers(),
                CSVStateConstants.CSV_LAYER_TYPE);
        for (LayerType layer : csvLayers)
        {
            CSVDataSource csvSource = toDataSource(layer, id);
            myEnvoy.getCSVFileController().getFileImporter().importSource(csvSource, Collections.emptySet(),
                () -> addResource(id, csvSource));
        }
    }

    @Override
    public boolean canActivateState(Node node)
    {
        return StateXML.anyMatch(node, CSVStateConstants.CSV_DATA_LAYER_PATH);
    }

    @Override
    public boolean canActivateState(StateType state)
    {
        return !StateUtilities.getLayers(state.getLocalData(), state.getDataLayers(), CSVStateConstants.CSV_LAYER_TYPE).isEmpty();
    }

    @Override
    public boolean canSaveState()
    {
        return !myDataGroupController.findActiveDataGroupInfo(new CSVStateGroupFilter(), true).isEmpty();
    }

    @Override
    public boolean isSaveStateByDefault()
    {
        return true;
    }

    @Override
    public void saveState(Node node)
    {
        for (CSVDataTypeInfo csvDTI : getCsvDataTypes())
        {
            Document doc = node instanceof Document ? (Document)node : node.getOwnerDocument();
            try
            {
                Node layersNode = StateXML.createChildNode(node, doc, node, CSVStateConstants.DATA_LAYERS_PATH,
                        CSVStateConstants.DATA_LAYERS_NAME);
                ((Element)layersNode).setAttribute("type", "data");
                Node dataLayerNode = StateXML.getChildNode(node, CSVStateConstants.CSV_PATH);
                if (dataLayerNode == null)
                {
                    dataLayerNode = StateXML.createElement(doc, CSVStateConstants.LAYER_NAME);
                    ((Element)dataLayerNode).setAttribute("type", CSVStateConstants.CSV_LAYER_TYPE);
                    layersNode.appendChild(dataLayerNode);
                }

                try
                {
                    CSVDataSource csvSource = csvDTI.getFileSource().clone();
                    csvSource.setFromStateSource(true);
                    csvSource.setVisible(csvDTI.isVisible());
                    XMLUtilities.marshalJAXBObjectToElement(csvSource, dataLayerNode,
                            JAXBContextHelper.getCachedContext(CSVDataSource.class.getPackage()));
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
        Collection<CSVDataTypeInfo> dataTypes = getCsvDataTypes();
        if (!dataTypes.isEmpty())
        {
            LayersType localData = StateUtilities.getLocalData(state);
            for (CSVDataTypeInfo dataType : dataTypes)
            {
                LayerType layer = toLayer(dataType);
                localData.getLayer().add(layer);
            }
        }
    }

    @Override
    protected void deactivate(CSVDataSource stateGroup)
    {
        DataGroupInfo dgi = myDataGroupController.getDataGroupInfo(stateGroup.generateTypeKey());
        dgi.activationProperty().setActive(false);
        myDataGroupController.removeDataGroupInfo(dgi, this);
        myEnvoy.getCSVFileController().removeSource(stateGroup, true, null);
    }

    /**
     * Converts the CSV data type to a state v4 layer.
     *
     * @param dataType the CSV data type
     * @return the layer
     */
    private static LayerType toLayer(CSVDataTypeInfo dataType)
    {
        CSVDataSource dataSource = dataType.getFileSource();

        LayerType layer = new LayerType();
        layer.setUrl(dataSource.getSourceUri().toString());
        layer.setTitle(dataSource.getName());
        layer.setType(CSVStateConstants.CSV_LAYER_TYPE);
        layer.setVisible(dataSource.isVisible());
        layer.setBaseColor(StateUtilities.formatColor(dataSource.getLayerSettings().getColor()));
        layer.setTemporal(Boolean.valueOf(dataSource.getLayerSettings().isTimelineEnabled()));
        layer.setAnimate(Boolean.TRUE.toString());
        layer.setLoad(Boolean.TRUE);
        layer.setSpatial(Boolean.TRUE);

        CSVColumnFormat columnFormat = dataSource.getParseParameters().getColumnFormat();
        if (columnFormat instanceof CSVDelimitedColumnFormat)
        {
            CSVDelimitedColumnFormat delimitedFormat = (CSVDelimitedColumnFormat)columnFormat;
            layer.setDelimiter(delimitedFormat.getTokenDelimiter());
            layer.setQuote(delimitedFormat.getTextDelimiter());
        }
        layer.setComment(dataSource.getParseParameters().getCommentIndicator());
        layer.setHeaderRow(toBigInteger(dataSource.getParseParameters().getHeaderLine()));
        layer.setDataRow(toBigInteger(dataSource.getParseParameters().getDataStartLine()));
        return layer;
    }

    /**
     * Converts the state v4 layer to a CSV data source.
     *
     * @param layer the state v4 layer
     * @param stateId the state ID
     * @return the CSV data source
     */
    private static CSVDataSource toDataSource(LayerType layer, String stateId)
    {
        CSVDataSource dataSource = new CSVDataSource(parseURI(layer.getUrl()));
        dataSource.setName(new StringBuilder(layer.getTitle()).append(" - ").append(stateId).toString());
        dataSource.setActive(true);
        dataSource.setVisible(layer.isVisible());
        dataSource.setTransient(true);
        dataSource.setFromStateSource(true);
//        dataSource.setSourceClassificationHeader(ch);
        dataSource.getLayerSettings().setColor(StateUtilities.parseColor(getColor(layer)));
        dataSource.getLayerSettings().setTimelineEnabled(BooleanUtilities.toBoolean(layer.isTemporal()));
        dataSource.getLayerSettings().setMetadataEnabled(true);
        dataSource.setParseParameters(toParameters(layer));
        return dataSource;
    }

    /**
     * Converts the state v4 layer to CSVParseParameters.
     *
     * @param layer the state v4 layer
     * @return the CSVParseParameters
     */
    private static CSVParseParameters toParameters(LayerType layer)
    {
        CSVParseParameters parseParameters = new CSVParseParameters();
        parseParameters.setColumnFormat(new CSVDelimitedColumnFormat(layer.getDelimiter(), layer.getQuote(), 0));
//        parseParameters.setColumnNames(layer.getColumns().getColumn());
        parseParameters.setCommentIndicator(layer.getComment());
        parseParameters.setHeaderLine(layer.getHeaderRow() != null ? Integer.valueOf(layer.getHeaderRow().intValue()) : null);
        parseParameters.setDataStartLine(layer.getDataRow() != null ? Integer.valueOf(layer.getDataRow().intValue()) : null);

//        layer.getMappings().getMapping();
//        private Set<SpecialColumn> mySpecialColumns = New.set();
//        parseParameters.getSpecialColumns().addAll(c);

        return parseParameters;
    }

    /**
     * Gets the color of the layer.
     *
     * @param layer the layer
     * @return the color, or null
     */
    private static String getColor(LayerType layer)
    {
        return layer.getBaseColor() != null ? layer.getBaseColor()
                : layer.getBasicFeatureStyle() != null ? layer.getBasicFeatureStyle().getPointColor() : null;
    }

    /**
     * Parses a URI string into a URI.
     *
     * @param s the string
     * @return the URI, or null if there was a problem
     */
    private static URI parseURI(String s)
    {
        URI uri = null;
        if (StringUtils.isNotEmpty(s))
        {
            try
            {
                uri = new URI(s);
            }
            catch (URISyntaxException e)
            {
                LOGGER.warn(e);
            }
        }
        return uri;
    }

    /**
     * Converts an Integer to a BigInteger.
     *
     * @param i the Integer
     * @return the BigInteger
     */
    private static BigInteger toBigInteger(Integer i)
    {
        return i != null ? BigInteger.valueOf(i.longValue()) : null;
    }

    /**
     * Gets the CSV data types.
     *
     * @return the CSV data types
     */
    private Collection<CSVDataTypeInfo> getCsvDataTypes()
    {
        Collection<CSVDataTypeInfo> dataTypes = Collections.emptyList();

        Set<DataGroupInfo> dataGroups = myDataGroupController.findActiveDataGroupInfo(new CSVStateGroupFilter(), false);
        Map<String, DataTypeInfo> uniqueDataTypes = getUniqueFiles(dataGroups);
        if (!uniqueDataTypes.isEmpty())
        {
            dataTypes = CollectionUtilities.filterDowncast(uniqueDataTypes.values(), CSVDataTypeInfo.class);
        }

        return dataTypes;
    }

    /**
     * Make sure we have a unique set of CSV file URL's so the state doesn't
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
        for (DataGroupInfo csvGroup : dataGroups)
        {
            for (DataTypeInfo csvType : csvGroup.getMembers(false))
            {
                CSVDataTypeInfo csvDTI = (CSVDataTypeInfo)csvType;
                if (!uniqueDataTypes.containsKey(csvDTI.getUrl()))
                {
                    if (csvDTI.getFileSource().isFromStateSource())
                    {
                        String[] nameTok = csvDTI.getFileSource().getName().split("-\\(");
                        csvDTI.getFileSource().setName(nameTok[0]);
                    }
                    uniqueDataTypes.put(csvDTI.getUrl(), csvDTI);
                }
            }
        }
        return uniqueDataTypes;
    }

    /**
     * Checks for active CSV layers.
     */
    static class CSVStateGroupFilter implements Predicate<DataGroupInfo>
    {
        @Override
        public boolean test(DataGroupInfo value)
        {
            if (value.hasMembers(false) && value.activationProperty().isActiveOrActivating())
            {
                for (DataTypeInfo info : value.getMembers(false))
                {
                    if (info instanceof CSVDataTypeInfo)
                    {
                        CSVDataTypeInfo csvInfo = (CSVDataTypeInfo)info;
                        if (!csvInfo.getFileSource().isFromStateSource())
                        {
                            return true;
                        }
                    }
                }
            }
            return false;
        }
    }
}

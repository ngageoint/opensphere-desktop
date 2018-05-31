package io.opensphere.wfs.envoy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;

import org.apache.avro.Schema.Field;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.util.Utf8;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.vividsolutions.jts.geom.Geometry;

import io.opensphere.core.Toolbox;
import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.util.XMLUtilities;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.Pair;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.core.util.xml.NodeIterator;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.LoadsTo;
import io.opensphere.mantle.data.MapVisualizationType;
import io.opensphere.mantle.data.MetaDataInfo;
import io.opensphere.mantle.data.SpecialKey;
import io.opensphere.mantle.data.element.MapDataElement;
import io.opensphere.mantle.data.element.MetaDataProvider;
import io.opensphere.mantle.data.element.factory.AvroTimeHelper;
import io.opensphere.mantle.data.element.factory.DataElementFactory;
import io.opensphere.mantle.data.element.impl.DefaultMapDataElement;
import io.opensphere.mantle.data.element.impl.MDILinkedMetaDataProvider;
import io.opensphere.mantle.data.element.mdfilter.OGCFilterGenerator;
import io.opensphere.mantle.data.element.mdfilter.OGCFilterParameters;
import io.opensphere.mantle.data.geom.MapGeometrySupport;
import io.opensphere.mantle.data.impl.specialkey.AltitudeKey;
import io.opensphere.mantle.data.impl.specialkey.EndTimeKey;
import io.opensphere.mantle.data.impl.specialkey.LatitudeKey;
import io.opensphere.mantle.data.impl.specialkey.LongitudeKey;
import io.opensphere.mantle.data.impl.specialkey.TimeKey;
import io.opensphere.server.customization.ServerCustomization;
import io.opensphere.server.services.ServerConnectionParams;
import io.opensphere.server.toolbox.LayerConfiguration;
import io.opensphere.server.toolbox.ServerToolbox;
import io.opensphere.server.toolbox.WFSLayerConfigurationManager;
import io.opensphere.server.util.OGCOutputFormat;
import io.opensphere.wfs.layer.WFSDataType;
import io.opensphere.wfs.layer.WFSLayerColumnManager;
import io.opensphere.wfs.layer.WFSMetaDataInfo;
import io.opensphere.wfs.util.DOMUtilities;
import io.opensphere.wfs.util.WFSConstants;
import net.opengis.ows._100.KeywordsType;
import net.opengis.wfs._110.FeatureTypeType;
import net.opengis.wfs._110.GetFeatureType;
import net.opengis.wfs._110.ObjectFactory;
import net.opengis.wfs._110.QueryType;
import net.opengis.wfs._110.ResultTypeType;
import net.opengis.wfs._110.WFSCapabilitiesType;

/** Helper class for the WMS envoy. */
@SuppressWarnings("PMD.GodClass")
public class WFSEnvoyHelper
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(WFSEnvoyHelper.class);

    /** Assign IDs by incrementing this counter. */
    private static final AtomicLong ID_COUNTER = new AtomicLong();

    /**
     * Supported GML geometry types and which MapVisualizationType they map to.
     */
    private final Map<String, MapVisualizationType> myGmlPrimitives = configureGMLPrimitives();

    /**
     * SpecialKeys and the potential column names they map to. NOTE: The key
     * names in this map should be treated as case-insensitive.
     */
    private final Map<String, SpecialKey> mySpecialKeys = Collections.unmodifiableMap(configureSpecialKeys());

    /**
     * Configures the dictionary of GML primitives supported by the WFS plugin.
     *
     * @return the dictionary of GML primitives supported by the WFS plugin.
     */
    protected Map<String, MapVisualizationType> configureGMLPrimitives()
    {
        Map<String, MapVisualizationType> tmpPrimitives = New.map();
        tmpPrimitives.put("PointPropertyType", MapVisualizationType.POINT_ELEMENTS);
        tmpPrimitives.put("PointArrayPropertyType", MapVisualizationType.POINT_ELEMENTS);
        tmpPrimitives.put("CurvePropertyType", MapVisualizationType.TRACK_ELEMENTS);
        tmpPrimitives.put("CurveArrayPropertyType", MapVisualizationType.TRACK_ELEMENTS);
        tmpPrimitives.put("LineStringPropertyType", MapVisualizationType.POLYLINE_ELEMENTS);
        tmpPrimitives.put("SurfacePropertyType", MapVisualizationType.POLYGON_ELEMENTS);
        tmpPrimitives.put("SurfaceArrayPropertyType", MapVisualizationType.POLYGON_ELEMENTS);
        tmpPrimitives.put("LinearRingPropertyType", MapVisualizationType.POLYLINE_ELEMENTS);
        tmpPrimitives.put("PolygonPropertyType", MapVisualizationType.POLYGON_ELEMENTS);
        tmpPrimitives.put("MultiPointPropertyType", MapVisualizationType.POINT_ELEMENTS);
        tmpPrimitives.put("MultiLineStringPropertyType", MapVisualizationType.POLYLINE_ELEMENTS);
        tmpPrimitives.put("MultiSurfacePropertyType", MapVisualizationType.POLYGON_ELEMENTS);
        tmpPrimitives.put("MultiCurvePropertyType", MapVisualizationType.TRACK_ELEMENTS);
        tmpPrimitives.put("GeometryPropertyType", MapVisualizationType.MIXED_ELEMENTS);
        return Collections.unmodifiableMap(tmpPrimitives);
    }

    /**
     * Configures the dictionary of special column names supported by the WFS
     * Plugin. These keys are used to infer meaning in WFS Data.
     *
     * @return the dictionary of GML primitives supported by the WFS plugin.
     */
    protected Map<String, SpecialKey> configureSpecialKeys()
    {
        Map<String, SpecialKey> tmpKeys = New.insertionOrderMap();
        tmpKeys.put("LAT", LatitudeKey.DEFAULT);
        tmpKeys.put("LATITUDE", LatitudeKey.DEFAULT);
        tmpKeys.put("LON", LongitudeKey.DEFAULT);
        tmpKeys.put("LONG", LongitudeKey.DEFAULT);
        tmpKeys.put("LONGITUDE", LongitudeKey.DEFAULT);
        tmpKeys.put("UP_DATE_TIME", TimeKey.DEFAULT);
        tmpKeys.put("TIME", TimeKey.DEFAULT);
        tmpKeys.put("DOWN_DATE_TIME", EndTimeKey.DEFAULT);
        tmpKeys.put("END_TIME", EndTimeKey.DEFAULT);
        tmpKeys.put("ALTITUDE", AltitudeKey.DEFAULT);
        tmpKeys.put("ALT", AltitudeKey.DEFAULT);
        return tmpKeys;
    }

    /**
     * Gets the map of special keys.
     *
     * @return the map of special keys
     */
    public Map<String, SpecialKey> getSpecialKeyMap()
    {
        return mySpecialKeys;
    }

    /**
     * Build the URL for the get capabilities service.
     *
     * @param conn The connection information needed to build the WFS URL.
     * @return The URL.
     * @throws MalformedURLException If the URL cannot be formed.
     */
    public URL buildGetCapabilitiesURL(ServerConnectionParams conn) throws MalformedURLException
    {
        return new URL(buildBaseWfsURL(conn, WFSRequestType.GET_CAPABLITIES));
    }

    /**
     * Adds describe feature data to {@link WFSDataType}.
     *
     * @param type the data type
     * @param describeDocs the collection of DescribeFeatureType documents from
     *            an OGC Server
     */
    public void addDescribeFeatureDataToType(WFSDataType type, Collection<org.w3c.dom.Document> describeDocs)
    {
        for (org.w3c.dom.Document doc : describeDocs)
        {
            Node layerTypeNode = getTypeNode(doc, type.getTypeName());

            setFeatureProperties(type, layerTypeNode, null, (WFSMetaDataInfo)type.getMetaDataInfo());
            Pair<String, MapVisualizationType> holder = getFeatureDataType(layerTypeNode);
            type.getMapVisualizationInfo().setVisualizationType(holder.getSecondObject());
            type.getMetaDataInfo().setGeometryColumn(holder.getFirstObject());
        }
    }

    /**
     * Find the node identifying the complex type for the specified layer name.
     * Note that while it is theoretically possible for this method to return a
     * null value, it is not expected ever to happen in practice.
     *
     * @param doc an XML Document
     * @param layer the layer name
     * @return the type node, if found, or null
     */
    protected Node getTypeNode(Document doc, String layer)
    {
        for (Node node : new NodeIterator(doc.getDocumentElement()))
        {
            if (node == null || !"element".equals(node.getLocalName()))
            {
                continue;
            }
            String name = XMLUtilities.getAttributeValue(node, "name");
            if (!name.equals(layer))
            {
                continue;
            }

            // Type name may be in the form "namespace:layerName"
            String type = XMLUtilities.stripNamespace(XMLUtilities.getAttributeValue(node, "type"));
            // find and return the "complexType" whose "name" attribute matches

            NodeIterator iterator = new NodeIterator(doc.getElementsByTagNameNS("*", "complexType"));
            for (Node complexTypeNode : iterator)
            {
                if (StringUtils.equals(type, XMLUtilities.getAttributeValue(complexTypeNode, "name")))
                {
                    return complexTypeNode;
                }
            }
            break;
        }
        return null;
    }

    /**
     * Build the URL for the describe feature service.
     *
     * @param conn The connection information needed to build the WFS URL.
     * @param type The data type used for type specific information in the
     *            request, or null to request all layers.
     * @return The URL.
     * @throws MalformedURLException If the URL cannot be formed.
     */
    public URL buildDescribeFeatureTypeURL(ServerConnectionParams conn, WFSDataType type) throws MalformedURLException
    {
        StringBuilder sb = new StringBuilder(buildBaseWfsURL(conn, WFSRequestType.DESCRIBE_FEATURE_TYPE));
        if (type != null)
        {
            sb.append("&typename=").append(type.getTypeName());
        }
        return new URL(sb.toString());
    }

    /**
     * Build the URL for the get feature service.
     *
     * @param filterParams The parameters which describe the filter being built.
     * @param type The data type used for type specific information in the
     *            request.
     * @param conn The connection information needed to build the WFS URL.
     * @param prefs The WFS preferences.
     * @return The URL.
     * @throws MalformedURLException If the URL cannot be formed.
     * @throws UnsupportedEncodingException If the output format cannot be
     *             converted to a URL-friendly format
     */
    public URL buildGetFeatureURL(OGCFilterParameters filterParams, WFSDataType type, ServerConnectionParams conn,
            Preferences prefs)
        throws MalformedURLException, UnsupportedEncodingException
    {
        StringBuilder sb = new StringBuilder(buildBaseWfsURL(conn, WFSRequestType.GET_FEATURE));
        sb.append("&typename=").append(type.getTypeName());

        List<String> columns = getQueryColumns(type);
        if (CollectionUtilities.hasContent(columns))
        {
            sb.append("&propertyname=").append(StringUtilities.join(",", columns));
        }

        sb.append("&maxfeatures=").append(filterParams.getMaxFeatures().intValue());
        sb.append("&filter=").append(OGCFilterGenerator.buildQueryString(filterParams, type.getDisplayName()));
        sb.append("&outputformat=").append(URLEncoder.encode(filterParams.getOutputFormat(), "UTF-8"));
        if (OGCOutputFormat.isStreaming(filterParams.getOutputFormat()))
        {
            sb.append("&STREAMING=true");
        }
        return new URL(sb.toString());
    }

    /**
     * Builds a list of URL parameters that makeup a GetFeature request. This
     * can be used as the body of a POST request or to build the query string
     * part of a GET request.
     *
     * @param filterParams The parameters which describe the filter being built.
     * @param type The data type used for type specific information in the
     *            request.
     * @return The map of parameter keys and values.
     */
    public InputStream buildPostQuery(OGCFilterParameters filterParams, WFSDataType type)
    {
        ObjectFactory wfsObjectFactory = new ObjectFactory();
        GetFeatureType request = wfsObjectFactory.createGetFeatureType();

        request.setService("WFS");
        request.setVersion(getWFSVersion());
        request.setMaxFeatures(filterParams.getMaxFeatures());
        request.setOutputFormat(filterParams.getOutputFormat());
        request.setResultType(ResultTypeType.RESULTS);

        // Put the filter into the request by way of a new Query
        QueryType query = new QueryType();
        query.getTypeName().add(new QName(type.getTypeName()));
        query.setSrsName(filterParams.getSrs());
        boolean endInclusive = false;
        if (type.getTimeExtents() != null)
        {
            endInclusive = filterParams.getTimeSpan().getEnd() == type.getTimeExtents().getExtent().getEnd();
        }
        query.setFilter(OGCFilterGenerator.buildQuery(filterParams, type.getDisplayName(), endInclusive));

        List<String> filteredColumns = getQueryColumns(type);
        if (CollectionUtilities.hasContent(filteredColumns))
        {
            query.getPropertyNameOrXlinkPropertyNameOrFunction().addAll(filteredColumns);
        }
        request.getQuery().add(query);

        InputStream result = null;
        try
        {
            Class<?>[] classes = { GetFeatureType.class };
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            XMLUtilities.writeXMLObject(wfsObjectFactory.createGetFeature(request), os, classes);
            byte[] arr = os.toByteArray();
            result = new ByteArrayInputStream(arr);

            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug("Built post query: " + new String(arr, StringUtilities.DEFAULT_CHARSET));
            }
        }
        catch (JAXBException e)
        {
            LOGGER.warn("Failed to marshall WFS POST Query.", e);
        }
        return result;
    }

    /**
     * Builds a set of {@link WFSDataType}s from the WFSCapabilitiesType from
     * the server.
     *
     * @param wfsCapabilities the WFSCapabilitiesType from the server
     * @param toolbox the core toolbox
     * @param serverConfig server configuration with a URL for the WFS server
     * @param columnManager the WFS plugin's column manager
     * @param defaultOutputFormat the default output format
     * @return a collection of {@link WFSDataType}s
     */
    public Collection<WFSDataType> buildWFSTypes(WFSCapabilitiesType wfsCapabilities, Toolbox toolbox,
            ServerConnectionParams serverConfig, WFSLayerColumnManager columnManager, OGCOutputFormat defaultOutputFormat)
    {
        Collection<WFSDataType> layers = New.list();

        WFSLayerConfigurationManager layerConfigurationManager = toolbox.getPluginToolboxRegistry()
                .getPluginToolbox(ServerToolbox.class).getLayerConfigurationManager();

        LayerConfiguration layerConfiguration = layerConfigurationManager
                .getConfigurationFromCustomization(serverConfig.getServerCustomization());

        Boolean isLatBeforeLon = null;
        if (serverConfig.getServerCustomization() != null)
        {
            isLatBeforeLon = Boolean.valueOf(serverConfig.getServerCustomization().getLatLonOrder(wfsCapabilities)
                    .equals(ServerCustomization.LatLonOrder.LATLON));
        }

        for (FeatureTypeType featureType : wfsCapabilities.getFeatureTypeList().getFeatureType())
        {
            String elementName = featureType.getName().getLocalPart();
            String title = featureType.getTitle();
            String layerKey = serverConfig.getWfsUrl() + WFSConstants.LAYERNAME_SEPARATOR + elementName;

            WFSDataType type = new WFSDataType(toolbox, serverConfig.getServerTitle(), layerKey, elementName, title,
                    new WFSMetaDataInfo(toolbox, columnManager), layerConfiguration);

            /* Set to POINT_ELEMENTS until the describe feature can really set
             * it. This is to make the group show up in the right type category
             * while activating. */
            type.getMapVisualizationInfo().setVisualizationType(MapVisualizationType.POINT_ELEMENTS);
            type.setWFSVersion(getWFSVersion());
            type.setUrl(serverConfig.getWfsUrl());
            if (isLatBeforeLon != null)
            {
                type.setLatBeforeLon(isLatBeforeLon.booleanValue());
            }

            String description = featureType.getAbstract();
            if (description != null && !description.isEmpty() && !"null".equalsIgnoreCase(description))
            {
                type.setDescription(description);
            }

            List<String> tagsToRemove = type.getTags().stream().filter(t -> t.indexOf(':') != -1).collect(Collectors.toList());
            for (String tag : tagsToRemove)
            {
                type.removeTag(tag, this);
            }
            for (KeywordsType keywords : featureType.getKeywords())
            {
                for (String key : keywords.getKeyword())
                {
                    type.addTag(key, null);
                }
            }

            if (featureType.getOutputFormats() != null)
            {
                OGCOutputFormat format = OGCOutputFormat.getPreferredFormat(featureType.getOutputFormats().getFormat());
                type.setOutputFormat(format);
            }
            else
            {
                type.setOutputFormat(defaultOutputFormat);
            }

            layers.add(type);
        }

        return layers;
    }

    /**
     * Adds data from the child node to the column map.
     *
     * @param dataType the data type
     * @param child The child node
     * @param columns The map of property names to java types.
     * @param source the requesting object.
     */
    protected void addColumn(final WFSDataType dataType, final Node child, final WFSMetaDataInfo columns, Object source)
    {
        NamedNodeMap attributes = child.getAttributes();
        Node name = attributes.getNamedItem("name");

        // Ignore the styleVariation element.
        if ("styleVariation".equals(name.getNodeValue()))
        {
            return;
        }

        Class<?> type = null;
        // Ignore the Geometry (GML type) element
        String elemType = null;
        Node tempItem = attributes.getNamedItem("type");
        if (tempItem != null)
        {
            elemType = attributes.getNamedItem("type").getNodeValue();
        }

        // Add the columns to the set of columns
        Node simpleType = child.getFirstChild();

        if (simpleType != null && simpleType.hasChildNodes())
        {
            Node restriction = simpleType.getFirstChild();
            if (restriction.hasAttributes())
            {
                attributes = restriction.getAttributes();
                Node base = attributes.getNamedItem("base");
                type = getTypeFromBase(base);
            }
        }
        // type may be specified in node attributes
        else if (elemType != null && elemType.length() > 0)
        {
            String[] typeSplit = elemType.split(":");
            if (typeSplit.length == 2)
            {
                if (typeSplit[0].equals("gml"))
                {
                    type = Geometry.class;
                }
                else
                {
                    type = getTypeFromString(typeSplit[1]);
                }
            }
        }

        if (type != null)
        {
            /* If the type is a Date, save it off. */
            if (type == Date.class)
            {
                columns.addDateKey(name.getTextContent());
            }
            columns.addWFSKey(name.getTextContent(), type, source);
        }
        else
        {
            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug("Failed to parse property [" + name + "]");
            }
        }
    }

    /**
     * Build the base URL for a WFS request type.
     *
     * @param conn The connection information needed to build the WFS URL.
     * @param type The type of request being made
     * @return the string-formatted URL
     */
    public String buildBaseWfsURL(ServerConnectionParams conn, WFSRequestType type)
    {
        StringBuilder sb = new StringBuilder(conn.getWfsUrl());
        sb.append("?service=WFS&version=");
        sb.append(getWFSVersion());
        sb.append("&request=");
        sb.append(type.getValue());
        return sb.toString();
    }

    /**
     * Convert one Avro record into a MapDataElement for the specified layer.
     *
     * @param rec Avro record
     * @param help the layer, decorated with time span helper
     * @return MapDataElement
     */
    public MapDataElement createDataElement(GenericRecord rec, AvroTimeHelper help)
    {
        if (rec == null)
        {
            return null;
        }
        MapGeometrySupport mgs = DataElementFactory.avroObjectToGeom(rec, help);
        DataTypeInfo dti = help.getType();
        MetaDataProvider metaData = getAvroMeta(rec, dti.getMetaDataInfo());
        MapDataElement elt = new DefaultMapDataElement(ID_COUNTER.getAndIncrement(), mgs.getTimeSpan(), dti, metaData, mgs);
        elt.getVisualizationState().setColor(DataElementFactory.avroElementColor(rec, dti));
        return elt;
    }

    /**
     * Get the metadata for an Avro record.
     *
     * @param rec GenericRecord
     * @param metaDataInfo the meta data info
     * @return MetaDataProvider
     */
    protected MetaDataProvider getAvroMeta(GenericRecord rec, MetaDataInfo metaDataInfo)
    {
        MetaDataProvider provider = new MDILinkedMetaDataProvider(metaDataInfo);
        for (Field field : rec.getSchema().getFields())
        {
            String name = field.name();
            Object value = rec.get(name);
            if (value instanceof Utf8)
            {
                provider.setValue(name, value.toString());
            }
            else if (value instanceof Serializable)
            {
                provider.setValue(name, (Serializable)value);
            }
        }
        return provider;
    }

    /**
     * Recursive function to get the type of data returned by getFeature
     * requests from the layer defined by the passed-in node.
     *
     * @param inputNode The node from the DescribeFeatures doc that contains the
     *            layer definition with the features' type (dots, tracks,
     *            polygons).
     * @return the features' data type
     */
    protected Pair<String, MapVisualizationType> getFeatureDataType(Node inputNode)
    {
        if (inputNode.hasAttributes())
        {
            String elemType = XMLUtilities.getAttributeValue(inputNode, "type");
            if (elemType != null && elemType.length() > 0)
            {
                String[] typeSplit = elemType.split(":");
                if (typeSplit.length == 2 && typeSplit[0].equals("gml"))
                {
                    MapVisualizationType mapVisualizationType = getGmlPrimitives().get(typeSplit[1]);
                    if (mapVisualizationType != null)
                    {
                        return new Pair<>(XMLUtilities.getAttributeValue(inputNode, "name"), mapVisualizationType);
                    }
                }
            }
        }

        if (inputNode.hasChildNodes())
        {
            NodeList elements = inputNode.getChildNodes();
            for (int i = 0; i < elements.getLength(); i++)
            {
                Pair<String, MapVisualizationType> childType = getFeatureDataType(elements.item(i));
                if (childType.getSecondObject() != MapVisualizationType.UNKNOWN)
                {
                    return childType;
                }
            }
        }

        return new Pair<>(null, MapVisualizationType.UNKNOWN);
    }

    /**
     * Gets the columns that should be requested as part of GetFeature requests.
     *
     * @param type the {@link WFSDataType} for the layer being requested
     * @return the list of column names that should be requested
     */
    protected List<String> getQueryColumns(WFSDataType type)
    {
        List<String> columns = New.list(type.getMetaDataInfo().getKeyNames());
        if (CollectionUtilities.hasContent(columns))
        {
            // Fix up the time columns
            if (!type.isTimeless() && ((WFSMetaDataInfo)type.getMetaDataInfo()).isDynamicTime()
                    && WFSConstants.DEFAULT_TIME_FIELD.equals(type.getMetaDataInfo().getTimeKey()))
            {
                columns.remove(type.getMetaDataInfo().getTimeKey());
                columns.add(WFSConstants.DEFAULT_TIME_QUERY_KEY);
            }

            // If valid, add the geometry column as well.
            if (StringUtils.isNotEmpty(type.getMetaDataInfo().getGeometryColumn())
                    && !columns.contains(type.getMetaDataInfo().getGeometryColumn()))
            {
                columns.add(type.getMetaDataInfo().getGeometryColumn());
            }
        }
        return columns;
    }

    /**
     * Get a Java type from a base node.
     *
     * @param base The base node.
     * @return The Java type.
     */
    protected Class<?> getTypeFromBase(Node base)
    {
        return getTypeFromString(base.getTextContent());
    }

    /**
     * Get a Java type from a base node.
     *
     * @param base the base node.
     * @return the Java type (default = String.class)
     */
    protected Class<?> getTypeFromString(String base)
    {
        Class<?> type = String.class;

        if ("decimal".equalsIgnoreCase(base))
        {
            type = Double.class;
        }
        else if ("double".equalsIgnoreCase(base))
        {
            type = Double.class;
        }
        else if ("integer".equalsIgnoreCase(base))
        {
            type = Integer.class;
        }
        else if ("int".equalsIgnoreCase(base))
        {
            type = Integer.class;
        }
        else if ("long".equalsIgnoreCase(base))
        {
            type = Integer.class;
        }
        else if ("datetime".equalsIgnoreCase(base))
        {
            type = Date.class;
        }

        return type;
    }

    /**
     * Get the WFS version to request from the server.
     *
     * @return The WFS version.
     */
    protected String getWFSVersion()
    {
        return "1.1.0";
    }

    /**
     * Get the properties of the feature.
     *
     * @param type the WFSDataType
     * @param complexTypeNode The node from the DescribeFeatures doc that
     *            contains the layer definition with property names.
     * @param source the requesting object.
     * @param columns the columns
     */
    protected void setFeatureProperties(WFSDataType type, Node complexTypeNode, Object source, WFSMetaDataInfo columns)
    {
        if (complexTypeNode.getLocalName().equals("complexType"))
        {
            /* Check for nodes that are gml:DynamicFeatureType. These have
             * special handling regarding time. */
            Node extensionNode = DOMUtilities.getChildByElementName(complexTypeNode, "extension");
            if (extensionNode != null && extensionNode.getAttributes() != null)
            {
                String base = XMLUtilities.getAttributeValue(extensionNode, "base");
                if (base != null && base.contains("Dynamic"))
                {
                    columns.setDynamicTime(true);
                }
            }

            // Get the corresponding sequence node
            Node sequenceNode = DOMUtilities.getChildByElementName(complexTypeNode, "sequence");
            if (sequenceNode != null)
            {
                NodeList elements = sequenceNode.getChildNodes();

                for (int i = 0; i < elements.getLength(); i++)
                {
                    Node child = elements.item(i);
                    if (child.hasAttributes())
                    {
                        addColumn(type, child, columns, source);
                    }
                }
            }
        }
        setSpecialKeys(columns, source);
        columns.copyKeysToOriginalKeys();

        /* If a date column was found, switch the type to timeline. */
        if (type.getBasicVisualizationInfo().getLoadsTo() == LoadsTo.STATIC && !columns.getDateKeys().isEmpty())
        {
            type.getBasicVisualizationInfo().setSupportedLoadsToTypes(Arrays.asList(LoadsTo.TIMELINE, LoadsTo.STATIC));
            type.getBasicVisualizationInfo().setLoadsTo(LoadsTo.TIMELINE, null);
        }
    }

    /**
     * Sets the special keys.
     *
     * @param columns the columns
     * @param source the source
     */
    protected void setSpecialKeys(MetaDataInfo columns, Object source)
    {
        for (Map.Entry<String, SpecialKey> entry : mySpecialKeys.entrySet())
        {
            String key = entry.getKey();
            SpecialKey specialKey = entry.getValue();

            /* For each special key, if it exists as a column in the Data Type,
             * but there is not a special key assigned to it, add it. */
            if (columns.getKeyForSpecialType(specialKey) == null)
            {
                for (String column : columns.getKeyNames())
                {
                    if (column.equalsIgnoreCase(key))
                    {
                        columns.setSpecialKey(column, specialKey, source);
                    }
                }
            }
        }
    }

    /**
     * Gets the value of the {@link #myGmlPrimitives} field.
     *
     * @return the value stored in the {@link #myGmlPrimitives} field.
     */
    protected Map<String, MapVisualizationType> getGmlPrimitives()
    {
        return myGmlPrimitives;
    }
}

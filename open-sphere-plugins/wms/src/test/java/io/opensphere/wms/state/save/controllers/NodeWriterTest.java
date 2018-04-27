package io.opensphere.wms.state.save.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.StringUtils;
import org.easymock.EasyMock;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.modulestate.ModuleStateController;
import io.opensphere.core.modulestate.StateXML;
import io.opensphere.core.util.XMLUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.data.BasicVisualizationInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.LoadsTo;
import io.opensphere.mantle.data.MetaDataInfo;
import io.opensphere.mantle.data.TimeExtents;
import io.opensphere.server.state.StateConstants;
import io.opensphere.wms.layer.WMSLayerValueProvider;
import io.opensphere.wms.state.model.WMSLayerAndState;
import io.opensphere.wms.state.model.WMSLayerState;

/**
 * Tests the node writer class.
 */
public class NodeWriterTest
{
    /**
     * Tests writing to the state node without any layer elements already
     * created.
     *
     * @throws ParserConfigurationException Bad parse.
     * @throws JAXBException Bad jaxb.
     * @throws SAXException Bad sax.
     * @throws IOException Bad io.
     */
    @Test
    public void testWriteToNode() throws ParserConfigurationException, JAXBException, SAXException, IOException
    {
        Document doc = XMLUtilities.newDocument();
        Node stateElement = doc.appendChild(StateXML.createElement(doc, ModuleStateController.STATE_NAME));

        performTest(stateElement);
    }

    /**
     * Test writing to node with layer groups already existing.
     *
     * @throws ParserConfigurationException Bad parse.
     * @throws JAXBException Bad jaxb.
     * @throws SAXException Bad sax.
     * @throws IOException Bad io.
     */
    @Test
    public void testWriteToNodeExistingLayerGroup() throws ParserConfigurationException, JAXBException, SAXException, IOException
    {
        Document doc = XMLUtilities.newDocument();
        Node stateElement = doc.appendChild(StateXML.createElement(doc, ModuleStateController.STATE_NAME));
        Element dataLayersElement = (Element)stateElement.appendChild(StateXML.createElement(doc, "layers"));
        dataLayersElement.setAttribute("type", StateConstants.DATA_LAYERS_TYPE);
        Element mapLayersElement = (Element)stateElement.appendChild(StateXML.createElement(doc, "layers"));
        mapLayersElement.setAttribute("type", StateConstants.MAP_LAYERS_TYPE);
        performTest(stateElement);
    }

    /**
     * Tests writing to the state node with a laer element already existing.
     *
     * @throws ParserConfigurationException Bad parse.
     * @throws JAXBException Bad jaxb.
     * @throws SAXException Bad sax.
     * @throws IOException Bad io.
     */
    @Test
    public void testWriteToNodeExistingLayers() throws ParserConfigurationException, JAXBException, SAXException, IOException
    {
        Document doc = XMLUtilities.newDocument();
        Node stateElement = doc.appendChild(StateXML.createElement(doc, ModuleStateController.STATE_NAME));

        performTest(stateElement);
    }

    /**
     * Creates the time extents expected for a data layer.
     *
     * @return Easy mocked time extents.
     */
    private TimeExtents createDataExtents()
    {
        TimeExtents extents = EasyMock.createMock(TimeExtents.class);

        extents.getExtent();
        EasyMock.expectLastCall().andReturn(TimeSpan.get(System.currentTimeMillis() - 100000, System.currentTimeMillis()));
        EasyMock.expectLastCall().anyTimes();

        return extents;
    }

    /**
     * Creates the easy mocked visualization info for a data type that should be
     * a data layer.
     *
     * @return The visualization info.
     */
    private BasicVisualizationInfo createDataInfo()
    {
        BasicVisualizationInfo dataInfo = EasyMock.createMock(BasicVisualizationInfo.class);
        dataInfo.getLoadsTo();
        EasyMock.expectLastCall().andReturn(LoadsTo.TIMELINE);
        EasyMock.expectLastCall().anyTimes();

        return dataInfo;
    }

    /**
     * Create data type info.
     *
     * @param index The index of the infor.
     * @param dataInfo The data layer visualization info.
     * @param mapInfo The map layer visualization info.
     * @param dataExtents The time extents for a data layer.
     * @param mapExtents The time extents for a map layer.
     * @return The data type info.
     */
    private DataTypeInfo createDataTypeInfo(int index, BasicVisualizationInfo dataInfo, BasicVisualizationInfo mapInfo,
            TimeExtents dataExtents, TimeExtents mapExtents)
    {
        MetaDataInfo metadata = EasyMock.createMock(MetaDataInfo.class);
        DataTypeInfo dataTypeInfo = EasyMock.createMock(DataTypeInfo.class);
        dataTypeInfo.getMetaDataInfo();

        if (index == 0)
        {
            EasyMock.expectLastCall().andReturn(metadata);
            EasyMock.expectLastCall().times(2);
        }
        else
        {
            EasyMock.expectLastCall().andReturn(null);
            EasyMock.expectLastCall().times(2);

            dataTypeInfo.getTimeExtents();

            if (index == 2)
            {
                EasyMock.expectLastCall().andReturn(dataExtents);
                EasyMock.expectLastCall().times(4);
            }
            else
            {
                EasyMock.expectLastCall().andReturn(mapExtents);
                EasyMock.expectLastCall().times(4);

                dataTypeInfo.getBasicVisualizationInfo();

                if (index == 3)
                {
                    EasyMock.expectLastCall().andReturn(dataInfo);
                    EasyMock.expectLastCall().times(2);
                }
                else
                {
                    EasyMock.expectLastCall().andReturn(mapInfo);
                    EasyMock.expectLastCall().times(2);
                }
            }
        }

        return dataTypeInfo;
    }

    /**
     * Creates the data type infos for test.
     *
     * @param dataInfo The data layer visualization info.
     * @param mapInfo The map layer visualization info.
     * @param dataExtents The time extents for a data layer.
     * @param mapExtents The time extents for a map layer.
     * @return The list of data type infos.
     */
    private List<DataTypeInfo> createDataTypeInfos(BasicVisualizationInfo dataInfo, BasicVisualizationInfo mapInfo,
            TimeExtents dataExtents, TimeExtents mapExtents)
    {
        List<DataTypeInfo> dataTypes = New.list();

        for (int i = 0; i < 4; i++)
        {
            dataTypes.add(createDataTypeInfo(i, dataInfo, mapInfo, dataExtents, mapExtents));
        }

        return dataTypes;
    }

    /**
     * Creates the a layer and state to use for testing.
     *
     * @param index The index.
     * @param dataTypeInfo The data type info returned by the layer.
     * @return The layer and state.
     */
    private WMSLayerAndState createLayerState(int index, DataTypeInfo dataTypeInfo)
    {
        WMSLayerValueProvider layer = EasyMock.createMock(WMSLayerValueProvider.class);
        layer.getTypeInfo();
        EasyMock.expectLastCall().andReturn(dataTypeInfo);
        EasyMock.expectLastCall().times(2);

        WMSLayerState state = new WMSLayerState();
        state.setId("layer" + index);

        WMSLayerAndState layerState = new WMSLayerAndState(layer, state);

        return layerState;
    }

    /**
     * Create the layer and states to use for testing.
     *
     * @param dataTypeInfos The data type infos to be returned by the layer
     *            value providers.
     * @return The layers and states.
     */
    private List<WMSLayerAndState> createLayerStates(List<DataTypeInfo> dataTypeInfos)
    {
        List<WMSLayerAndState> layerStates = New.list();

        int index = 0;
        for (DataTypeInfo dataTypeInfo : dataTypeInfos)
        {
            layerStates.add(createLayerState(index, dataTypeInfo));
            index++;
        }

        return layerStates;
    }

    /**
     * Creates the time extents expected for a map layer.
     *
     * @return Easy mocked time extents.
     */
    private TimeExtents createMapExtents()
    {
        TimeExtents extents = EasyMock.createMock(TimeExtents.class);

        extents.getExtent();
        EasyMock.expectLastCall().andReturn(TimeSpan.ZERO);
        EasyMock.expectLastCall().anyTimes();

        return extents;
    }

    /**
     * Creates the easy mocked visualization info for a data type that should be
     * a map layer.
     *
     * @return The visualization info.
     */
    private BasicVisualizationInfo createMapInfo()
    {
        BasicVisualizationInfo mapInfo = EasyMock.createMock(BasicVisualizationInfo.class);
        mapInfo.getLoadsTo();
        EasyMock.expectLastCall().andReturn(LoadsTo.BASE);
        EasyMock.expectLastCall().anyTimes();

        return mapInfo;
    }

    /**
     * Performs the test using the specified state node.
     *
     * @param stateElement The state node.
     * @throws ParserConfigurationException Bad parse.
     * @throws JAXBException Bad jaxb.
     * @throws SAXException Bad sax.
     * @throws IOException Bad io.
     */
    private void performTest(Node stateElement) throws JAXBException, SAXException, IOException, ParserConfigurationException
    {
        TimeExtents dataExtents = createDataExtents();
        TimeExtents mapExtents = createMapExtents();
        BasicVisualizationInfo mapInfo = createMapInfo();
        BasicVisualizationInfo dataInfo = createDataInfo();
        List<DataTypeInfo> dataTypeInfos = createDataTypeInfos(dataInfo, mapInfo, dataExtents, mapExtents);
        List<WMSLayerAndState> layerAndStates = createLayerStates(dataTypeInfos);

        EasyMock.replay(mapInfo, dataInfo, dataExtents, mapExtents);

        for (DataTypeInfo dataTypeInfo : dataTypeInfos)
        {
            EasyMock.replay(dataTypeInfo);
        }

        for (WMSLayerAndState layerState : layerAndStates)
        {
            EasyMock.replay(layerState.getLayer());
        }

        NodeWriter writer = new NodeWriter();
        writer.writeToNode(stateElement, layerAndStates, true);
        writer.writeToNode(stateElement, layerAndStates, false);

        String xml = XMLUtilities.format(stateElement);

        assertEquals(1, StringUtils.countMatches(xml, "<layers type=\"data\">"));
        assertEquals(1, StringUtils.countMatches(xml, "<layers type=\"map\">"));
        assertEquals(4, StringUtils.countMatches(xml, "<layer type=\"wms\""));

        ByteArrayInputStream stream = new ByteArrayInputStream(xml.getBytes());
        Document actual = XMLUtilities.newDocumentBuilderNS().parse(stream);

        NodeList children = actual.getChildNodes();

        assertEquals(1, children.getLength());

        Node state = children.item(0);

        children = state.getChildNodes();

        assertEquals(5, children.getLength());

        boolean mapAsserted = false;
        boolean dataAsserted = false;

        for (int i = 0; i < 5; i++)
        {
            Node layerGroup = children.item(i);
            if (!layerGroup.getNodeName().equals("#text"))
            {
                if (layerGroup.getNodeName().equals("layers")
                        && layerGroup.getAttributes().getNamedItem("type").getTextContent().equals("map"))
                {
                    NodeList layers = layerGroup.getChildNodes();
                    assertEquals(3, layers.getLength());

                    Node dataLayer = layers.item(1);

                    WMSLayerState layerState = XMLUtilities.readXMLObject(dataLayer, WMSLayerState.class);
                    assertEquals("layer1", layerState.getId());

                    mapAsserted = true;
                }
                else
                {
                    NodeList layers = layerGroup.getChildNodes();
                    assertEquals(7, layers.getLength());

                    dataAsserted = true;
                }
            }
        }

        assertTrue(mapAsserted);
        assertTrue(dataAsserted);

        EasyMock.verify(mapInfo, dataInfo, dataExtents, mapExtents);

        for (DataTypeInfo dataTypeInfo : dataTypeInfos)
        {
            EasyMock.verify(dataTypeInfo);
        }

        for (WMSLayerAndState layerState : layerAndStates)
        {
            EasyMock.verify(layerState.getLayer());
        }
    }
}

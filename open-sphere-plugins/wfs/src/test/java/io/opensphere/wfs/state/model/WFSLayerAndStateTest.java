package io.opensphere.wfs.state.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.bind.JAXBException;

import org.junit.Test;

import io.opensphere.core.util.XMLUtilities;
import io.opensphere.core.util.lang.StringUtilities;

/**
 * Tests the WFSLayerState class verifies the getters and setters. Verifies that
 * it can serialize/deserialize and verifies that the xml format is as expected.
 *
 */
public class WFSLayerAndStateTest
{
    /**
     * Tests the getters.
     *
     * @throws JAXBException bad xml.
     * @throws IOException bad stream.
     */
    @Test
    public void test() throws JAXBException, IOException
    {
        WFSLayerState expected = new WFSLayerState();
        expected.setType("serverType");
        WFSStateParameters params = new WFSStateParameters();
        params.setTypeName("BLUE");
        params.setVersion("1.0.0");
        expected.setWFSParameters(params);
        expected.setId("uniqueKey1");
        expected.setDisplayName("BLUE_DisplayName");
        expected.setUrl("https://server1/ogc");
        expected.setVisible(true);
        expected.getTags().getTags().add("TAG1");
        expected.getTags().getTags().add("TAG2");
        expected.setAnimate(true);
        expected.setDisableEmptyColumns(true);

        BasicFeatureStyle bfs = new BasicFeatureStyle();
        bfs.setPointColor("0000ff");
        bfs.setPointOpacity(255);
        bfs.setPointSize(4f);
        bfs.setLift(10.0);
        bfs.setAltitudeRef("Automatic: Provided by source data");
        bfs.setUseAltitude(true);
        bfs.setAltitudeColumn("ALT");
        bfs.setAltitudeColUnits("meters");
        bfs.setLabelColumn("NOTTYPINE");
        bfs.setLabelSize(8);
        bfs.setLabelColor("ffffffff");

        expected.setBasicFeatureStyle(bfs);

        InputStream stream = XMLUtilities.writeXMLObjectToInputStreamSync(expected);

        StringBuilder builder = new StringBuilder();
        int character = stream.read();
        while (character > 0)
        {
            builder.append((char)character);
            character = stream.read();
        }

        String xmlString = builder.toString();

        assertTrue(xmlString.contains("<typename>BLUE</typename>"));
        assertTrue(xmlString.contains("<version>1.0.0</version>"));
        assertTrue(xmlString.contains("<id>uniqueKey1</id>"));
        assertTrue(xmlString.contains("<title>BLUE_DisplayName</title>"));
        assertTrue(xmlString.contains("<url>https://server1/ogc</url>"));
        assertTrue(xmlString.contains("<visible>true</visible>"));
        assertTrue(xmlString.matches("(?s).*<tags>.*<tag>TAG1</tag>.*<tag>TAG2</tag>.*</tags>.*"));
        assertTrue(xmlString.contains("<animate>true</animate>"));
        assertTrue(xmlString.contains("<disabledEmptyColumns>true</disabledEmptyColumns>"));
        assertTrue(xmlString.contains("<basicFeatureStyle>"));
        assertTrue(xmlString.contains("<pointColor>0000ff</pointColor>"));
        assertTrue(xmlString.contains("<pointOpacity>255</pointOpacity>"));
        assertTrue(xmlString.contains("<pointSize>4.0</pointSize>"));
        assertTrue(xmlString.contains("<lift>10.0</lift>"));
        assertTrue(xmlString.contains("<altitudeRef>Automatic: Provided by source data</altitudeRef>"));
        assertTrue(xmlString.contains("<useAltitude>true</useAltitude>"));
        assertTrue(xmlString.contains("<altitudeColumn>ALT</altitudeColumn>"));
        assertTrue(xmlString.contains("<altColUnits>meters</altColUnits>"));
        assertTrue(xmlString.contains("<labelColumn>NOTTYPINE</labelColumn>"));
        assertTrue(xmlString.contains("<labelSize>8</labelSize>"));
        assertTrue(xmlString.contains("<labelColor>ffffffff</labelColor>"));
        assertTrue(xmlString.contains("</basicFeatureStyle>"));
        assertTrue(xmlString.contains("<layer xmlns=\"http://www.bit-sys.com/state/v2\" type=\"serverType\">"));

        stream.reset();

        WFSLayerState actual = XMLUtilities.readXMLObject(stream, WFSLayerState.class);

        assertEquals(expected.getWFSParameters().getTypeName(), actual.getWFSParameters().getTypeName());
        assertEquals(expected.getTypeKey(), actual.getTypeKey());
        assertEquals(expected.getDisplayName(), actual.getDisplayName());
        assertEquals(expected.getUrl(), actual.getUrl());
        assertEquals(Boolean.valueOf(expected.isVisible()), Boolean.valueOf(actual.isVisible()));
        assertEquals(expected.getTags().getTags(), actual.getTags().getTags());
        assertEquals(Boolean.valueOf(expected.isAnimate()), Boolean.valueOf(actual.isAnimate()));
        assertEquals(expected.getDisabledColumns(), actual.getDisabledColumns());
        assertEquals(expected.getBasicFeatureStyle().getPointColor(), actual.getBasicFeatureStyle().getPointColor());
        assertEquals(expected.getBasicFeatureStyle().getPointOpacity(), actual.getBasicFeatureStyle().getPointOpacity());
        assertEquals(String.valueOf(expected.getBasicFeatureStyle().getPointSize()),
                String.valueOf(actual.getBasicFeatureStyle().getPointSize()));
        assertEquals(String.valueOf(expected.getBasicFeatureStyle().getLift()),
                String.valueOf(actual.getBasicFeatureStyle().getLift()));
        assertEquals(expected.getBasicFeatureStyle().getAltitudeRef(), actual.getBasicFeatureStyle().getAltitudeRef());
        assertEquals(Boolean.valueOf(expected.getBasicFeatureStyle().isUseAltitude()),
                Boolean.valueOf(actual.getBasicFeatureStyle().isUseAltitude()));
        assertEquals(expected.getBasicFeatureStyle().getAltitudeColumn(), actual.getBasicFeatureStyle().getAltitudeColumn());
        assertEquals(expected.getBasicFeatureStyle().getAltitudeColUnits(), actual.getBasicFeatureStyle().getAltitudeColUnits());
        assertEquals(expected.getBasicFeatureStyle().getLabelColumn(), actual.getBasicFeatureStyle().getLabelColumn());
        assertEquals(String.valueOf(expected.getBasicFeatureStyle().getLabelSize()),
                String.valueOf(actual.getBasicFeatureStyle().getLabelSize()));
        assertEquals(expected.getBasicFeatureStyle().getLabelColor(), actual.getBasicFeatureStyle().getLabelColor());
        assertEquals(expected.getType(), actual.getType());
    }

    /**
     * Tests reading a sample 2d layer state xml.
     *
     * @throws JAXBException Bad jaxb.
     */
    @Test
    public void testRead2dLayer() throws JAXBException
    {
        String xml = "<layer xmlns=\"http://www.bit-sys.com/state/v2\" type=\"wfs\"><exclusions>true</exclusions><title>title</title>"
                + "<url>https://somehost/ogc/wfsServer</url><params><srsname>EPSG:4326</srsname>"
                + "<version>1.1.0</version><service>WFS</service><typename>typename</typename>"
                + "<request>GetFeature</request><maxfeatures>10000</maxfeatures>"
                + "<namespace>xmlns:bubba=\"http://www.bit-sys.com/bubba\"</namespace></params><temporal>true</temporal>"
                + "<visible>true</visible><dataProvider>OGC Server (default)</dataProvider><spatial>true</spatial>"
                + "<layerType>Tile and Feature Groups</layerType><colorize>true</colorize><filter>true</filter>"
                + "<refreshRate>0</refreshRate><usePost>true</usePost><style>Default</style><animate>true</animate><load>true</load>"
                + "<id>default#id#features</id><basicFeatureStyle><pointColor>ffff00</pointColor>"
                + "<pointOpacity>255</pointOpacity><pointSize>6</pointSize></basicFeatureStyle><tags><tag>tag0</tag>"
                + "<tag>tag1</tag><tag>tag2</tag><tag>tag3</tag><tag>tag4</tag><tag>tag5</tag>"
                + "<tag>tag6</tag><tag>tag7</tag></tags></layer>";

        WFSLayerState layerState = XMLUtilities
                .readXMLObject(new ByteArrayInputStream(xml.getBytes(StringUtilities.DEFAULT_CHARSET)), WFSLayerState.class);

        assertEquals("wfs", layerState.getType());
        assertEquals("title", layerState.getDisplayName());
        assertEquals("https://somehost/ogc/wfsServer", layerState.getUrl());
        assertEquals("1.1.0", layerState.getWFSParameters().getVersion());
        assertEquals("typename", layerState.getWFSParameters().getTypeName());
        assertEquals(true, layerState.isVisible());
        assertEquals(true, layerState.isAnimate());
        assertEquals("default#id#features", layerState.getId());
        assertEquals("ffff00", layerState.getBasicFeatureStyle().getPointColor());
        assertEquals(255, layerState.getBasicFeatureStyle().getPointOpacity());
        assertEquals(6f, layerState.getBasicFeatureStyle().getPointSize(), 0f);
        assertEquals(8, layerState.getTags().getTags().size());
        for (int i = 0; i < layerState.getTags().getTags().size(); i++)
        {
            assertEquals("tag" + i, layerState.getTags().getTags().get(i));
        }

        assertEquals("https://somehost/ogc/wfsServer!!typename", layerState.getTypeKey());
    }
}

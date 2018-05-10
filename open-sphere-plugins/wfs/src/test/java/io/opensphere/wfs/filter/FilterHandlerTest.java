package io.opensphere.wfs.filter;

import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.AttributesImpl;

import io.opensphere.core.model.Altitude.ReferenceLevel;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.mantle.data.geom.AbstractMapGeometrySupport;
import io.opensphere.mantle.data.geom.impl.DefaultMapPolygonGeometrySupport;
import io.opensphere.wfs.gml311.AbstractGmlGeometryHandler;
import io.opensphere.wfs.gml311.GeometryHandlerFactory;

/**
 * Test used to exercise the functionality of the {@link FilterHandler} class.
 */
@SuppressWarnings("boxing")
public class FilterHandlerTest
{
    /**
     * The object on which tests are performed.
     */
    private FilterHandler myTestObject;

    /**
     * Value used in testing.
     */
    private String myUri;

    /**
     * Value used in testing.
     */
    private String myLocalName;

    /**
     * Value used in testing.
     */
    private String myQName;

    /**
     * Value used in testing.
     */
    private Attributes myAttributes;

    /**
     * The field used to access the geometry handler.
     */
    private static Field ourCurrentGeometryHandlerField;

    /**
     * Creates the resources needed to execute the tests.
     *
     * @throws SecurityException if the field cannot be accessed.
     * @throws NoSuchFieldException if the field cannot be accessed.
     */
    @BeforeClass
    public static void setUpBeforeClass() throws NoSuchFieldException, SecurityException
    {
        ourCurrentGeometryHandlerField = FilterHandler.class.getDeclaredField("myCurrentGeometryHandler");
        ourCurrentGeometryHandlerField.setAccessible(true);
    }

    /**
     * Creates the resources needed to execute the tests.
     */
    @Before
    public void setUp()
    {
        myUri = "http://www.bitsys.com";
        myLocalName = GeometryHandlerFactory.GML_POLYGON_TAG;
        myQName = "http://www.bitsys.com/testNode";
        myAttributes = createStrictMock(Attributes.class);

        expect(myAttributes.getLength()).andReturn(2);
        expect(myAttributes.getQName(0)).andReturn("zero");
        expect(myAttributes.getValue(0)).andReturn("zero-value");
        expect(myAttributes.getQName(1)).andReturn("one");
        expect(myAttributes.getValue(1)).andReturn("one-value");

        replay(myAttributes);
        myTestObject = new FilterHandler();
    }

    /**
     * Test method to verify that there are no private methods in the
     * {@link FilterHandler} class.
     */
    @Test
    public void testNonPrivateMethods()
    {
        Method[] declaredMethods = FilterHandler.class.getDeclaredMethods();

        for (Method method : declaredMethods)
        {
            if (!method.getName().startsWith("$") && !method.getName().startsWith("lambda$"))
            {
                assertFalse(method.getName() + " is private. No private methods are permitted.",
                        Modifier.isPrivate(method.getModifiers()));
            }
        }
    }

    /**
     * Test method for
     * {@link FilterHandler#startElement(String, String, String, org.xml.sax.Attributes)}
     * .
     *
     * @throws IllegalAccessException if the internal fields cannot be accessed.
     * @throws IllegalArgumentException if the internal fields cannot be
     *             accessed.
     */
    @Test
    public void testStartElement() throws IllegalArgumentException, IllegalAccessException
    {
        myTestObject.startElement(myUri, myLocalName, myQName, myAttributes);
        assertEquals(1, myTestObject.getElementStack().size());

        SaxElement element = myTestObject.getElementStack().getFirst();
        assertEquals(myUri, element.getUri());
        assertEquals(myLocalName, element.getLocalName());
        assertEquals(myQName, element.getQName());

        Map<String, String> attributes = element.getAttributes();
        assertEquals(2, attributes.size());
        assertEquals(attributes.get("zero"), "zero-value");
        assertEquals(attributes.get("one"), "one-value");

        assertEquals("", myTestObject.getCurrentValue());
        AbstractGmlGeometryHandler currentFilterHandler = (AbstractGmlGeometryHandler)ourCurrentGeometryHandlerField
                .get(myTestObject);
        assertNotNull(currentFilterHandler);
        assertEquals(myLocalName, currentFilterHandler.getTagName());
    }

    /**
     * Test method for {@link FilterHandler#endElement(String, String, String)}.
     *
     * @throws IllegalAccessException if the internal fields cannot be accessed.
     * @throws IllegalArgumentException if the internal fields cannot be
     *             accessed.
     */
    @Test
    public void testEndElementStringStringString() throws IllegalArgumentException, IllegalAccessException
    {
        myTestObject.startElement(myUri, myLocalName, myQName, myAttributes);

        myTestObject.startElement(myUri, "exterior", myUri + "/exterior", new AttributesImpl());
        myTestObject.startElement(myUri, "posList", myUri + "/posList", new AttributesImpl());
        String pointString = "0.0 0.0 1.0 0.0 1.0 1.0 0.0 1.0";
        myTestObject.characters(pointString.toCharArray(), 0, pointString.length());
        myTestObject.endElement(myUri, "posList", myUri + "/posList");
        myTestObject.endElement(myUri, "exterior", myUri + "/exterior");

        myTestObject.endElement(myUri, myLocalName, myQName);

        List<AbstractMapGeometrySupport> geometries = myTestObject.getGeometries();
        assertEquals(1, geometries.size());
        assertTrue(geometries.get(0) instanceof DefaultMapPolygonGeometrySupport);
        DefaultMapPolygonGeometrySupport geometry = (DefaultMapPolygonGeometrySupport)geometries.get(0);
        assertEquals(5, geometry.getLocations().size());
        assertEquals(LatLonAlt.createFromDegrees(0, 0, ReferenceLevel.TERRAIN), geometry.getLocations().get(0));
        assertEquals(LatLonAlt.createFromDegrees(0, 1, ReferenceLevel.TERRAIN), geometry.getLocations().get(1));
        assertEquals(LatLonAlt.createFromDegrees(1, 1, ReferenceLevel.TERRAIN), geometry.getLocations().get(2));
        assertEquals(LatLonAlt.createFromDegrees(1, 0, ReferenceLevel.TERRAIN), geometry.getLocations().get(3));
        assertEquals(LatLonAlt.createFromDegrees(0, 0, ReferenceLevel.TERRAIN), geometry.getLocations().get(4));

        assertEquals(Color.ORANGE, geometries.get(0).getColor());
    }
}

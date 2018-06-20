package io.opensphere.mantle.transformer.impl.worker;

import static org.easymock.EasyMock.createNiceMock;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.apache.commons.lang3.RandomUtils;
import org.junit.Before;
import org.junit.Test;

import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.mantle.data.element.MetaDataProvider;
import io.opensphere.mantle.data.element.VisualizationState;
import io.opensphere.mantle.data.geom.MapGeometrySupport;

/**
 * Test class for the {@link ElementData} class.
 */
@SuppressWarnings("boxing")
public class ElementDataTest
{
    /**
     * The object on which tests are performed.
     */
    private ElementData myTestObject;

    /**
     * The ID used in the test.
     */
    private Long myTestID;

    /**
     * Mock Map geometry support for testing.
     */
    private MapGeometrySupport myMapGeometrySupport;

    /**
     * Mock Meta data provider for testing.
     */
    private MetaDataProvider myMetaDataProvider;

    /**
     * The Time span used for testing.
     */
    private TimeSpan myTimeSpan;

    /**
     * The Visualization state used for testing.
     */
    private VisualizationState myVisualizationState;

    /**
     * Creates the resources needed to execute the tests.
     *
     * @throws Exception if the resources cannot be initialized.
     */
    @Before
    public void setUp() throws Exception
    {
        myTestID = RandomUtils.nextLong();
        myMapGeometrySupport = createNiceMock(MapGeometrySupport.class);
        myMetaDataProvider = createNiceMock(MetaDataProvider.class);
        myTimeSpan = TimeSpan.get(System.currentTimeMillis());
        myVisualizationState = new VisualizationState(false);

        myTestObject = new ElementData(myTestID, myTimeSpan, myVisualizationState, myMetaDataProvider, myMapGeometrySupport);
    }

    /**
     * Test method for {@link ElementData#ElementData()}.
     */
    @Test
    public void testElementData()
    {
        assertNotNull(new ElementData());
    }

    /**
     * Test method for
     * {@link ElementData#ElementData(Long, TimeSpan, VisualizationState, MetaDataProvider, MapGeometrySupport)}
     * .
     */
    @Test
    public void testElementDataLongTimeSpanVisualizationStateMetaDataProviderMapGeometrySupport()
    {
        assertNotNull(myTestObject);
    }

    /**
     * Test method for {@link ElementData#found()}.
     */
    @Test
    public void testFound()
    {
        assertTrue(myTestObject.found());

        myTestObject = new ElementData(myTestID, myTimeSpan, null, myMetaDataProvider, myMapGeometrySupport);
        assertTrue(myTestObject.found());

        myTestObject = new ElementData(myTestID, null, myVisualizationState, myMetaDataProvider, myMapGeometrySupport);
        assertTrue(myTestObject.found());

        myTestObject = new ElementData(myTestID, myTimeSpan, myVisualizationState, null, myMapGeometrySupport);
        assertTrue(myTestObject.found());

        myTestObject = new ElementData(myTestID, myTimeSpan, myVisualizationState, myMetaDataProvider, null);
        assertTrue(myTestObject.found());

        myTestObject = new ElementData(myTestID, null, null, null, null);
        assertFalse(myTestObject.found());
    }

    /**
     * Test method for {@link ElementData#getID()}.
     */
    @Test
    public void testGetID()
    {
        assertEquals(myTestID, myTestObject.getID());
    }

    /**
     * Test method for {@link ElementData#getMapGeometrySupport()}.
     */
    @Test
    public void testGetMapGeometrySupport()
    {
        assertEquals(myMapGeometrySupport, myTestObject.getMapGeometrySupport());
    }

    /**
     * Test method for {@link ElementData#getMetaDataProvider()}.
     */
    @Test
    public void testGetMetaDataProvider()
    {
        assertEquals(myMetaDataProvider, myTestObject.getMetaDataProvider());
    }

    /**
     * Test method for {@link ElementData#getTimeSpan()}.
     */
    @Test
    public void testGetTimeSpan()
    {
        assertEquals(myTimeSpan, myTestObject.getTimeSpan());
    }

    /**
     * Test method for {@link ElementData#getVisualizationState()}.
     */
    @Test
    public void testGetVisualizationState()
    {
        assertEquals(myVisualizationState, myTestObject.getVisualizationState());
    }
}

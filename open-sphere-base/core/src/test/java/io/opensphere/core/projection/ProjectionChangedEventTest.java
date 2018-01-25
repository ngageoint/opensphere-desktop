package io.opensphere.core.projection;

import static org.easymock.EasyMock.createStrictMock;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import io.opensphere.core.model.GeographicBoundingBox;

/**
 * Test class to validate the functionality of the
 * {@link ProjectionChangedEvent} class.
 */
public class ProjectionChangedEventTest
{
    /**
     * The object on which tests are performed.
     */
    private ProjectionChangedEvent myTestObject;

    /**
     * A test projection used in the object.
     */
    private Projection myTestProjection;

    /**
     * A test projection used in the object.
     */
    private Projection myTestProjectionSnapshot;

    /**
     * Creates the resources needed to execute the tests.
     *
     * @throws java.lang.Exception if the resources cannot be initialized.
     */
    @Before
    public void setUp() throws Exception
    {
        myTestProjection = createStrictMock(Projection.class);
        myTestProjectionSnapshot = createStrictMock(Projection.class);

        myTestObject = new ProjectionChangedEvent(myTestProjection, myTestProjectionSnapshot, false);
    }

    /**
     * Test method for
     * {@link ProjectionChangedEvent#ProjectionChangedEvent(Projection, Projection, boolean)}
     * .
     */
    @Test
    public void testProjectionChangedEventProjectionProjectionBoolean()
    {
        // set up method created the object, verify that it's not null.
        assertNotNull(myTestObject);
    }

    /**
     * Test method for
     * {@link ProjectionChangedEvent#ProjectionChangedEvent(Projection, Projection, Collection)}
     * .
     */
    @Test
    public void testProjectionChangedEventProjectionProjectionCollection()
    {
        List<GeographicBoundingBox> testData = new ArrayList<>();

        myTestObject = new ProjectionChangedEvent(myTestProjection, myTestProjectionSnapshot, testData);
        assertNotNull(myTestObject);
    }

    /**
     * Test method for {@link ProjectionChangedEvent#getBounds()} .
     */
    @Test(expected = UnsupportedOperationException.class)
    public void testBoundsAreUnmodifiable()
    {
        // object created by the setUp method has no bounds:
        assertNull(myTestObject.getBounds());

        List<GeographicBoundingBox> testBounds = new ArrayList<>();
        myTestObject = new ProjectionChangedEvent(myTestProjection, myTestProjectionSnapshot, testBounds);
        myTestObject.getBounds().add(null);
        assertTrue(myTestObject.getBounds().isEmpty());
    }

    /**
     * Test method for {@link ProjectionChangedEvent#getBounds()} .
     */
    @Test
    public void testGetBounds()
    {
        // object created by the setUp method has no bounds:
        assertNull(myTestObject.getBounds());

        List<GeographicBoundingBox> testBounds = new ArrayList<>();
        myTestObject = new ProjectionChangedEvent(myTestProjection, myTestProjectionSnapshot, testBounds);
        assertNotNull(myTestObject.getBounds());
        assertTrue(myTestObject.getBounds().isEmpty());
        assertTrue(testBounds != myTestObject.getBounds());
    }

    /**
     * Test method for {@link ProjectionChangedEvent#getProjection()} .
     */
    @Test
    public void testGetProjection()
    {
        assertEquals(myTestProjection, myTestObject.getProjection());
    }

    /**
     * Test method for {@link ProjectionChangedEvent#getProjectionSnapshot()} .
     */
    @Test
    public void testGetProjectionSnapshot()
    {
        assertEquals(myTestProjectionSnapshot, myTestObject.getProjectionSnapshot());
    }

    /**
     * Test method for {@link ProjectionChangedEvent#isFullClear()} .
     */
    @Test
    public void testIsFullClear()
    {
        assertFalse(myTestObject.isFullClear());
        List<GeographicBoundingBox> testBounds = new ArrayList<>();
        myTestObject = new ProjectionChangedEvent(myTestProjection, myTestProjectionSnapshot, testBounds);
        assertFalse(myTestObject.isFullClear());
        myTestObject = new ProjectionChangedEvent(myTestProjection, myTestProjectionSnapshot, true);
        assertTrue(myTestObject.isFullClear());
    }
}

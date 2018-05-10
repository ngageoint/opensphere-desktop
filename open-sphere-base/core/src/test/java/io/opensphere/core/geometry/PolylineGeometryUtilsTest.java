package io.opensphere.core.geometry;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;

import io.opensphere.core.geometry.constraint.Constraints;
import io.opensphere.core.geometry.renderproperties.PolylineRenderProperties;
import io.opensphere.core.geometry.util.PolylineGeometryUtils;
import io.opensphere.core.math.Vector2d;
import io.opensphere.core.math.Vector3d;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.Position;
import io.opensphere.core.model.time.TimeSpan;

/**
 * Test harness for exercising {@link PolylineGeometryUtils}.
 *
 * The test exercises across a graph of lines. The graph looks like this:
 *
 * <pre>
 *                   + (2,4)
 *                   |
 *                   |
 *        (1,3) +----+ (2,3)
 *              |    |
 *              |    |
 *   (0,2) +----+----+ (2,2)
 *              | (1,2)
 *              |
 *              + (1,1)
 *              |
 *              |
 *   (0,0) +----+ (1,0)
 * </pre>
 *
 * The line between (2,3) and (2,4) is directed and added twice, once directed
 * from (2,3) to (2,4), and once in reverse, from (2,4) to (2,3). This is done
 * to verify that the algorithm properly handles repeated directed lines.
 */
@SuppressWarnings("boxing")
public class PolylineGeometryUtilsTest
{
    /** Test data. */
    private Set<PolylineGeometry> myPolylineSource;

    /** Test data. */
    private Set<LineString> myLineStringSource;

    /** Test data: (2,4) -> (2,3). */
    private PolylineGeometry myPolylineTen;

    /** Test data: (2,3) -> (2,4). */
    private PolylineGeometry myPolylineNine;

    /** Test data: (1,3) -> (2,3). */
    private PolylineGeometry myPolylineEight;

    /** Test data: (1,2) -> (1,3). */
    private PolylineGeometry myPolylineSeven;

    /** Test data: (2,2) -> (2,3). */
    private PolylineGeometry myPolylineSix;

    /** Test data: (1,2) -> (2,2). */
    private PolylineGeometry myPolylineFive;

    /** Test data: (1,2) -> (0,2). */
    private PolylineGeometry myPolylineFour;

    /** Test data: (1,1) -> (1,2). */
    private PolylineGeometry myPolylineThree;

    /** Test data: (1,0) -> (1,1). */
    private PolylineGeometry myPolylineTwo;

    /** Test data: (0,0) -> (1,0). */
    private PolylineGeometry myPolylineOne;

    /** Test data: (2,4) -> (2,3). */
    private LineString myLineStringTen;

    /** Test data: (2,3) -> (2,4). */
    private LineString myLineStringNine;

    /** Test data: (1,3) -> (2,3). */
    private LineString myLineStringEight;

    /** Test data: (1,2) -> (1,3). */
    private LineString myLineStringSeven;

    /** Test data: (2,2) -> (2,3). */
    private LineString myLineStringSix;

    /** Test data: (1,2) -> (2,2). */
    private LineString myLineStringFive;

    /** Test data: (1,2) -> (0,2). */
    private LineString myLineStringFour;

    /** Test data: (1,1) -> (1,2). */
    private LineString myLineStringThree;

    /** Test data: (1,0) -> (1,1). */
    private LineString myLineStringTwo;

    /** Test data: (0,0) -> (1,0). */
    private LineString myLineStringOne;

    /** Test resource. */
    private GeometryFactory myGeometryFactory;

    /** Sets up resources before each test is run. */
    @Before
    public void setUpBeforeClass()
    {
        myPolylineSource = new HashSet<>();
        myLineStringSource = new HashSet<>();
        myGeometryFactory = new GeometryFactory();

        myPolylineOne = createTestData(0, 0, 1, 0);
        myPolylineTwo = createTestData(1, 0, 1, 1);
        myPolylineThree = createTestData(1, 1, 1, 2);
        myPolylineFour = createTestData(1, 2, 0, 2);
        myPolylineFive = createTestData(1, 2, 2, 2);
        myPolylineSix = createTestData(2, 2, 2, 3);
        myPolylineSeven = createTestData(1, 2, 1, 3);
        myPolylineEight = createTestData(1, 3, 2, 3);
        myPolylineNine = createTestData(2, 3, 2, 4);
        myPolylineTen = createTestData(2, 4, 2, 3);

        myLineStringOne = createTestLineString(0, 0, 1, 0);
        myLineStringTwo = createTestLineString(1, 0, 1, 1);
        myLineStringThree = createTestLineString(1, 1, 1, 2);
        myLineStringFour = createTestLineString(1, 2, 0, 2);
        myLineStringFive = createTestLineString(1, 2, 2, 2);
        myLineStringSix = createTestLineString(2, 2, 2, 3);
        myLineStringSeven = createTestLineString(1, 2, 1, 3);
        myLineStringEight = createTestLineString(1, 3, 2, 3);
        myLineStringNine = createTestLineString(2, 3, 2, 4);
        myLineStringTen = createTestLineString(2, 4, 2, 3);
    }

    /**
     * Creates a line string using the supplied coordinates, spanning from point
     * 'start' to point 'end'.
     *
     * @param pStartPositionX the x coordinate of the start position.
     * @param pStartPositionY the y coordinate of the start position.
     * @param pEndPositionX the x coordinate of the end position.
     * @param pEndPositionY the y coordinate of the end position.
     * @return a line string with the supplied start / end position.
     */
    public LineString createTestLineString(double pStartPositionX, double pStartPositionY, double pEndPositionX,
            double pEndPositionY)
    {
        Coordinate start = new Coordinate(pStartPositionX, pStartPositionY);
        Coordinate end = new Coordinate(pEndPositionX, pEndPositionY);

        return new LineString(new CoordinateArraySequence(new Coordinate[] { start, end }), myGeometryFactory);
    }

    /**
     * Creates a polyline from the start x/y to the end x/y.
     *
     * @param pStartX the x coordinate of the starting position of the line
     * @param pStartY the y coordinate of the starting position of the line
     * @param pEndX the x coordinate of the ending position of the line
     * @param pEndY the y coordinate of the ending position of the line
     * @return a polyline with the supplied start / end coordinates.
     */
    public PolylineGeometry createTestData(double pStartX, double pEndX, double pStartY, double pEndY)
    {
        GeographicPosition lineStart = new GeographicPosition(LatLonAlt.createFromVec2d(new Vector2d(pStartX, pEndX)));
        GeographicPosition lineEnd = new GeographicPosition(LatLonAlt.createFromVec2d(new Vector2d(pStartY, pEndY)));

        PolylineGeometry.Builder<GeographicPosition> lineBuilder = new PolylineGeometry.Builder<>();
        lineBuilder.setVertices(Arrays.asList(lineStart, lineEnd));

        PolylineRenderProperties renderProperties = EasyMock.createNiceMock(PolylineRenderProperties.class);
        EasyMock.expect(renderProperties.getWidth()).andReturn(1.0F).anyTimes();
        EasyMock.replay(renderProperties);

        return new PolylineGeometry(lineBuilder, renderProperties, Constraints.createTimeOnlyConstraint(TimeSpan.TIMELESS));
    }

    /** Cleans up resources after each test has completed. */
    @After
    public void tearDownAfterClass()
    {
        myPolylineSource = null;
        myPolylineOne = null;
        myPolylineTwo = null;
        myPolylineThree = null;
        myPolylineFour = null;
        myPolylineFive = null;
    }

    /**
     * Test method for
     * {@link PolylineGeometryUtils#convertToPosition(Coordinate)}.
     */
    @Test
    public void testConvertToPosition()
    {
        Position position1 = PolylineGeometryUtils.convertToPosition(new Coordinate(0, 0));
        Assert.assertEquals(new Vector3d(0, 0, 0), position1.asVector3d());

        Position position2 = PolylineGeometryUtils.convertToPosition(new Coordinate(0, 1));
        Assert.assertEquals(new Vector3d(0, 1, 0), position2.asVector3d());

        Position position3 = PolylineGeometryUtils.convertToPosition(new Coordinate(1, 0));
        Assert.assertEquals(new Vector3d(1, 0, 0), position3.asVector3d());
    }

    /**
     * Test method for
     * {@link PolylineGeometryUtils#convertToCoordinate(Vector3d)}.
     */
    @Test
    public void testConvertToCoordinate()
    {
        Vector3d vector1 = new Vector3d(0, 0, 0);
        Coordinate coordinate1 = PolylineGeometryUtils.convertToCoordinate(vector1);
        Assert.assertEquals(0.0, coordinate1.x, 0.);
        Assert.assertEquals(0.0, coordinate1.y, 0.);
        Assert.assertEquals(0.0, coordinate1.z, 0.);

        Vector3d vector2 = new Vector3d(0, 1, 0);
        Coordinate coordinate2 = PolylineGeometryUtils.convertToCoordinate(vector2);
        Assert.assertEquals(0.0, coordinate2.x, 0.);
        Assert.assertEquals(1.0, coordinate2.y, 0.);
        Assert.assertEquals(0.0, coordinate2.z, 0.);

        Vector3d vector3 = new Vector3d(1, 0, 0);
        Coordinate coordinate3 = PolylineGeometryUtils.convertToCoordinate(vector3);
        Assert.assertEquals(1.0, coordinate3.x, 0.);
        Assert.assertEquals(0.0, coordinate3.y, 0.);
        Assert.assertEquals(0.0, coordinate3.z, 0.);
    }

    /**
     * Test method for
     * {@link PolylineGeometryUtils#convertToCoordinate(Vector3d)}.
     */
    @Test
    public void testConvertRoundTrip()
    {
        Position position1 = PolylineGeometryUtils.convertToPosition(new Coordinate(0, 0));
        Position position2 = PolylineGeometryUtils.convertToPosition(new Coordinate(0, 1));
        Position position3 = PolylineGeometryUtils.convertToPosition(new Coordinate(1, 0));

        Coordinate coordinate1 = PolylineGeometryUtils.convertToCoordinate(position1.asVector3d());
        Position positionResult1 = PolylineGeometryUtils.convertToPosition(coordinate1);
        Assert.assertEquals(position1, positionResult1);

        Coordinate coordinate2 = PolylineGeometryUtils.convertToCoordinate(position2.asVector3d());
        Position positionResult2 = PolylineGeometryUtils.convertToPosition(coordinate2);
        Assert.assertEquals(position2, positionResult2);

        Coordinate coordinate3 = PolylineGeometryUtils.convertToCoordinate(position3.asVector3d());
        Position positionResult3 = PolylineGeometryUtils.convertToPosition(coordinate3);
        Assert.assertEquals(position3, positionResult3);
    }

    /**
     * Test method for
     * {@link PolylineGeometryUtils#createSingleLineString(java.util.Set)}.
     */
    @Test
    public void testCreateSingleLineStringOneInput()
    {
        myLineStringSource.add(myLineStringOne);

        LineString unifiedResult = PolylineGeometryUtils.createSingleLineString(myLineStringSource);
        Assert.assertNotNull(unifiedResult);
        Assert.assertEquals(2, unifiedResult.getCoordinates().length);
        Assert.assertEquals(new Coordinate(0, 0), unifiedResult.getCoordinateN(0));
        Assert.assertEquals(new Coordinate(1, 0), unifiedResult.getCoordinateN(1));
    }

    /**
     * Test method for
     * {@link PolylineGeometryUtils#createSingleLineString(java.util.Set)}.
     */
    @Test
    public void testCreateSingleLineString2JoinedInput()
    {
        myLineStringSource.add(myLineStringOne);
        myLineStringSource.add(myLineStringTwo);

        LineString unifiedResult = PolylineGeometryUtils.createSingleLineString(myLineStringSource);
        Assert.assertNotNull(unifiedResult);
        Assert.assertEquals(3, unifiedResult.getCoordinates().length);
        Assert.assertEquals(new Coordinate(0, 0), unifiedResult.getCoordinateN(0));
        Assert.assertEquals(new Coordinate(1, 0), unifiedResult.getCoordinateN(1));
        Assert.assertEquals(new Coordinate(1, 1), unifiedResult.getCoordinateN(2));
    }

    /**
     * Test method for
     * {@link PolylineGeometryUtils#createSingleLineString(java.util.Set)}.
     */
    @Test
    public void testCreateSingleLineString3JoinedInput()
    {
        myLineStringSource.clear();
        myLineStringSource.add(myLineStringOne);
        myLineStringSource.add(myLineStringTwo);
        myLineStringSource.add(myLineStringThree);

        LineString unifiedResult = PolylineGeometryUtils.createSingleLineString(myLineStringSource);
        Assert.assertNotNull(unifiedResult);
        Assert.assertEquals(4, unifiedResult.getCoordinates().length);
        Assert.assertEquals(new Coordinate(0, 0), unifiedResult.getCoordinateN(0));
        Assert.assertEquals(new Coordinate(1, 0), unifiedResult.getCoordinateN(1));
        Assert.assertEquals(new Coordinate(1, 1), unifiedResult.getCoordinateN(2));
        Assert.assertEquals(new Coordinate(1, 2), unifiedResult.getCoordinateN(3));
    }

    /**
     * Test method for
     * {@link PolylineGeometryUtils#createSingleLineString(java.util.Set)}.
     */
    @Test
    public void testCreateSingleLineString4JoinedInput()
    {
        myLineStringSource.clear();
        myLineStringSource.add(myLineStringOne);
        myLineStringSource.add(myLineStringTwo);
        myLineStringSource.add(myLineStringThree);
        myLineStringSource.add(myLineStringFour);

        LineString unifiedResult = PolylineGeometryUtils.createSingleLineString(myLineStringSource);
        Assert.assertNotNull(unifiedResult);
        Assert.assertEquals(5, unifiedResult.getCoordinates().length);
        Assert.assertEquals(new Coordinate(0, 0), unifiedResult.getCoordinateN(0));
        Assert.assertEquals(new Coordinate(1, 0), unifiedResult.getCoordinateN(1));
        Assert.assertEquals(new Coordinate(1, 1), unifiedResult.getCoordinateN(2));
        Assert.assertEquals(new Coordinate(1, 2), unifiedResult.getCoordinateN(3));
        Assert.assertEquals(new Coordinate(0, 2), unifiedResult.getCoordinateN(4));
    }

    /**
     * Test method for
     * {@link PolylineGeometryUtils#createSingleLineString(java.util.Set)}.
     */
    @Test
    public void testCreateSingleLineString5JoinedInput()
    {
        myLineStringSource.clear();
        myLineStringSource.add(myLineStringOne);
        myLineStringSource.add(myLineStringTwo);
        myLineStringSource.add(myLineStringThree);
        myLineStringSource.add(myLineStringFour);
        myLineStringSource.add(myLineStringFive);

        LineString unifiedResult = PolylineGeometryUtils.createSingleLineString(myLineStringSource);
        Assert.assertNotNull(unifiedResult);
        Assert.assertEquals(6, unifiedResult.getCoordinates().length);
        Assert.assertEquals(new Coordinate(0, 0), unifiedResult.getCoordinateN(0));
        Assert.assertEquals(new Coordinate(1, 0), unifiedResult.getCoordinateN(1));
        Assert.assertEquals(new Coordinate(1, 1), unifiedResult.getCoordinateN(2));
        Assert.assertEquals(new Coordinate(1, 2), unifiedResult.getCoordinateN(3));
        Assert.assertEquals(new Coordinate(0, 2), unifiedResult.getCoordinateN(4));
        Assert.assertEquals(new Coordinate(2, 2), unifiedResult.getCoordinateN(5));
    }

    /**
     * Test method for
     * {@link PolylineGeometryUtils#createSingleLineString(java.util.Set)}.
     */
    @Test
    public void testCreateSingleLineString6JoinedInput()
    {
        myLineStringSource.clear();
        myLineStringSource.add(myLineStringOne);
        myLineStringSource.add(myLineStringTwo);
        myLineStringSource.add(myLineStringThree);
        myLineStringSource.add(myLineStringFour);
        myLineStringSource.add(myLineStringFive);
        myLineStringSource.add(myLineStringSix);

        LineString unifiedResult = PolylineGeometryUtils.createSingleLineString(myLineStringSource);
        Assert.assertNotNull(unifiedResult);
        Assert.assertEquals(7, unifiedResult.getCoordinates().length);
        Assert.assertEquals(new Coordinate(0, 0), unifiedResult.getCoordinateN(0));
        Assert.assertEquals(new Coordinate(1, 0), unifiedResult.getCoordinateN(1));
        Assert.assertEquals(new Coordinate(1, 1), unifiedResult.getCoordinateN(2));
        Assert.assertEquals(new Coordinate(1, 2), unifiedResult.getCoordinateN(3));
        Assert.assertEquals(new Coordinate(0, 2), unifiedResult.getCoordinateN(4));
        Assert.assertEquals(new Coordinate(2, 2), unifiedResult.getCoordinateN(5));
        Assert.assertEquals(new Coordinate(2, 3), unifiedResult.getCoordinateN(6));
    }

    /**
     * Test method for
     * {@link PolylineGeometryUtils#createSingleLineString(java.util.Set)}.
     */
    @Test
    public void testCreateSingleLineString7JoinedInput()
    {
        myLineStringSource.clear();
        myLineStringSource.add(myLineStringOne);
        myLineStringSource.add(myLineStringTwo);
        myLineStringSource.add(myLineStringThree);
        myLineStringSource.add(myLineStringFour);
        myLineStringSource.add(myLineStringFive);
        myLineStringSource.add(myLineStringSix);
        myLineStringSource.add(myLineStringSeven);

        LineString unifiedResult = PolylineGeometryUtils.createSingleLineString(myLineStringSource);
        Assert.assertNotNull(unifiedResult);
        Assert.assertEquals(8, unifiedResult.getCoordinates().length);
        Assert.assertEquals(new Coordinate(0, 0), unifiedResult.getCoordinateN(0));
        Assert.assertEquals(new Coordinate(1, 0), unifiedResult.getCoordinateN(1));
        Assert.assertEquals(new Coordinate(1, 1), unifiedResult.getCoordinateN(2));
        Assert.assertEquals(new Coordinate(1, 2), unifiedResult.getCoordinateN(3));
        Assert.assertEquals(new Coordinate(0, 2), unifiedResult.getCoordinateN(4));
        Assert.assertEquals(new Coordinate(2, 2), unifiedResult.getCoordinateN(5));
        Assert.assertEquals(new Coordinate(2, 3), unifiedResult.getCoordinateN(6));
        Assert.assertEquals(new Coordinate(1, 3), unifiedResult.getCoordinateN(7));
    }

    /**
     * Test method for
     * {@link PolylineGeometryUtils#createSingleLineString(java.util.Set)}.
     */
    @Test
    public void testCreateSingleLineString8JoinedInput()
    {
        myLineStringSource.clear();
        myLineStringSource.add(myLineStringOne);
        myLineStringSource.add(myLineStringTwo);
        myLineStringSource.add(myLineStringThree);
        myLineStringSource.add(myLineStringFour);
        myLineStringSource.add(myLineStringFive);
        myLineStringSource.add(myLineStringSix);
        myLineStringSource.add(myLineStringSeven);
        myLineStringSource.add(myLineStringEight);

        LineString unifiedResult = PolylineGeometryUtils.createSingleLineString(myLineStringSource);
        Assert.assertNotNull(unifiedResult);
        Assert.assertEquals(8, unifiedResult.getCoordinates().length);
        Assert.assertEquals(new Coordinate(0, 0), unifiedResult.getCoordinateN(0));
        Assert.assertEquals(new Coordinate(1, 0), unifiedResult.getCoordinateN(1));
        Assert.assertEquals(new Coordinate(1, 1), unifiedResult.getCoordinateN(2));
        Assert.assertEquals(new Coordinate(1, 2), unifiedResult.getCoordinateN(3));
        Assert.assertEquals(new Coordinate(0, 2), unifiedResult.getCoordinateN(4));
        Assert.assertEquals(new Coordinate(2, 2), unifiedResult.getCoordinateN(5));
        Assert.assertEquals(new Coordinate(1, 3), unifiedResult.getCoordinateN(6));
        Assert.assertEquals(new Coordinate(2, 3), unifiedResult.getCoordinateN(7));
    }

    /**
     * Test method for
     * {@link PolylineGeometryUtils#createSingleLineString(java.util.Set)}.
     */
    @Test
    public void testCreateSingleLineString9JoinedInput()
    {
        myLineStringSource.clear();
        myLineStringSource.add(myLineStringOne);
        myLineStringSource.add(myLineStringTwo);
        myLineStringSource.add(myLineStringThree);
        myLineStringSource.add(myLineStringFour);
        myLineStringSource.add(myLineStringFive);
        myLineStringSource.add(myLineStringSix);
        myLineStringSource.add(myLineStringSeven);
        myLineStringSource.add(myLineStringEight);
        myLineStringSource.add(myLineStringNine);

        LineString unifiedResult = PolylineGeometryUtils.createSingleLineString(myLineStringSource);
        Assert.assertNotNull(unifiedResult);
        Assert.assertEquals(9, unifiedResult.getCoordinates().length);
        Assert.assertEquals(new Coordinate(0, 0), unifiedResult.getCoordinateN(0));
        Assert.assertEquals(new Coordinate(1, 0), unifiedResult.getCoordinateN(1));
        Assert.assertEquals(new Coordinate(1, 1), unifiedResult.getCoordinateN(2));
        Assert.assertEquals(new Coordinate(1, 2), unifiedResult.getCoordinateN(3));
        Assert.assertEquals(new Coordinate(0, 2), unifiedResult.getCoordinateN(4));
        Assert.assertEquals(new Coordinate(2, 2), unifiedResult.getCoordinateN(5));
        Assert.assertEquals(new Coordinate(1, 3), unifiedResult.getCoordinateN(6));
        Assert.assertEquals(new Coordinate(2, 3), unifiedResult.getCoordinateN(7));
        Assert.assertEquals(new Coordinate(2, 4), unifiedResult.getCoordinateN(8));
    }

    /**
     * Test method for
     * {@link PolylineGeometryUtils#createSingleLineString(java.util.Set)}.
     */
    @Test
    public void testCreateSingleLineString10JoinedInput()
    {
        myLineStringSource.clear();
        myLineStringSource.add(myLineStringOne);
        myLineStringSource.add(myLineStringTwo);
        myLineStringSource.add(myLineStringThree);
        myLineStringSource.add(myLineStringFour);
        myLineStringSource.add(myLineStringFive);
        myLineStringSource.add(myLineStringSix);
        myLineStringSource.add(myLineStringSeven);
        myLineStringSource.add(myLineStringEight);
        myLineStringSource.add(myLineStringNine);
        myLineStringSource.add(myLineStringTen);

        LineString unifiedResult = PolylineGeometryUtils.createSingleLineString(myLineStringSource);
        Assert.assertNotNull(unifiedResult);
        Assert.assertEquals(9, unifiedResult.getCoordinates().length);
        Assert.assertEquals(new Coordinate(0, 0), unifiedResult.getCoordinateN(0));
        Assert.assertEquals(new Coordinate(1, 0), unifiedResult.getCoordinateN(1));
        Assert.assertEquals(new Coordinate(1, 1), unifiedResult.getCoordinateN(2));
        Assert.assertEquals(new Coordinate(1, 2), unifiedResult.getCoordinateN(3));
        Assert.assertEquals(new Coordinate(0, 2), unifiedResult.getCoordinateN(4));
        Assert.assertEquals(new Coordinate(2, 2), unifiedResult.getCoordinateN(5));
        Assert.assertEquals(new Coordinate(1, 3), unifiedResult.getCoordinateN(6));
        Assert.assertEquals(new Coordinate(2, 3), unifiedResult.getCoordinateN(7));
        Assert.assertEquals(new Coordinate(2, 4), unifiedResult.getCoordinateN(8));
    }

    /**
     * Test method for {@link PolylineGeometryUtils#createMultiLineString(Set)}.
     */
    @Test
    public void testCreateMultiLineString()
    {
        myLineStringSource.clear();
        myLineStringSource.add(myLineStringOne);
        myLineStringSource.add(myLineStringTwo);
        myLineStringSource.add(myLineStringThree);
        myLineStringSource.add(myLineStringFour);
        myLineStringSource.add(myLineStringFive);
        myLineStringSource.add(myLineStringSix);
        myLineStringSource.add(myLineStringSeven);
        myLineStringSource.add(myLineStringEight);
        myLineStringSource.add(myLineStringNine);
        myLineStringSource.add(myLineStringTen);

        MultiLineString unifiedResult = PolylineGeometryUtils.createMultiLineString(myLineStringSource);

        Assert.assertEquals(10, unifiedResult.getNumGeometries());

        Assert.assertTrue(unifiedResult.contains(myLineStringOne));
        Assert.assertTrue(unifiedResult.contains(myLineStringTwo));
        Assert.assertTrue(unifiedResult.contains(myLineStringThree));
        Assert.assertTrue(unifiedResult.contains(myLineStringFour));
        Assert.assertTrue(unifiedResult.contains(myLineStringFive));
        Assert.assertTrue(unifiedResult.contains(myLineStringSix));
        Assert.assertTrue(unifiedResult.contains(myLineStringSeven));
        Assert.assertTrue(unifiedResult.contains(myLineStringEight));
        Assert.assertTrue(unifiedResult.contains(myLineStringNine));
        Assert.assertTrue(unifiedResult.contains(myLineStringTen));
    }

    /**
     * Test method for
     * {@link PolylineGeometryUtils#convertToMultiLineString(Set)}.
     */
    @Test
    public void testConvertToMultiLineString()
    {
        myPolylineSource.clear();
        myPolylineSource.add(myPolylineOne);
        myPolylineSource.add(myPolylineTwo);
        myPolylineSource.add(myPolylineThree);
        myPolylineSource.add(myPolylineFour);
        myPolylineSource.add(myPolylineFive);
        myPolylineSource.add(myPolylineSix);
        myPolylineSource.add(myPolylineSeven);
        myPolylineSource.add(myPolylineEight);
        myPolylineSource.add(myPolylineNine);
        myPolylineSource.add(myPolylineTen);

        MultiLineString unifiedResult = PolylineGeometryUtils.convertToMultiLineString(myPolylineSource);

        Assert.assertEquals(10, unifiedResult.getNumGeometries());

        Assert.assertTrue(unifiedResult.contains(myLineStringOne));
        Assert.assertTrue(unifiedResult.contains(myLineStringTwo));
        Assert.assertTrue(unifiedResult.contains(myLineStringThree));
        Assert.assertTrue(unifiedResult.contains(myLineStringFour));
        Assert.assertTrue(unifiedResult.contains(myLineStringFive));
        Assert.assertTrue(unifiedResult.contains(myLineStringSix));
        Assert.assertTrue(unifiedResult.contains(myLineStringSeven));
        Assert.assertTrue(unifiedResult.contains(myLineStringEight));
        Assert.assertTrue(unifiedResult.contains(myLineStringNine));
        Assert.assertTrue(unifiedResult.contains(myLineStringTen));
    }

    /**
     * Test method for
     * {@link PolylineGeometryUtils#createSinglePolyline(java.util.Set)}.
     */
    @Test
    public void testCreateSinglePolylineOneInput()
    {
        myPolylineSource.clear();
        myPolylineSource.add(myPolylineOne);

        PolylineGeometry unifiedResult = PolylineGeometryUtils.createSinglePolyline(myPolylineSource);
        Assert.assertNotNull(unifiedResult);
        Assert.assertEquals(2, unifiedResult.getVertices().size());

        Set<Vector3d> vertices = new HashSet<>(unifiedResult.getVertices().size());
        unifiedResult.getVertices().forEach(p -> vertices.add(p.asVector3d()));

        Assert.assertTrue(vertices.contains(new Vector3d(0, 0, 0)));
        Assert.assertTrue(vertices.contains(new Vector3d(1, 0, 0)));
    }

    /**
     * Test method for
     * {@link PolylineGeometryUtils#createSinglePolyline(java.util.Set)}.
     */
    @Test
    public void testCreateSinglePolyline2JoinedInput()
    {
        myPolylineSource.clear();
        myPolylineSource.add(myPolylineOne);
        myPolylineSource.add(myPolylineTwo);

        PolylineGeometry unifiedResult = PolylineGeometryUtils.createSinglePolyline(myPolylineSource);
        Assert.assertNotNull(unifiedResult);
        Assert.assertEquals(3, unifiedResult.getVertices().size());

        Set<Vector3d> vertices = new HashSet<>(unifiedResult.getVertices().size());
        unifiedResult.getVertices().forEach(p -> vertices.add(p.asVector3d()));

        Assert.assertTrue(vertices.contains(new Vector3d(0, 0, 0)));
        Assert.assertTrue(vertices.contains(new Vector3d(1, 0, 0)));
        Assert.assertTrue(vertices.contains(new Vector3d(1, 1, 0)));
    }

    /**
     * Test method for
     * {@link PolylineGeometryUtils#createSinglePolyline(java.util.Set)}.
     */
    @Test
    public void testCreateSinglePolyline3JoinedInput()
    {
        myPolylineSource.clear();
        myPolylineSource.add(myPolylineOne);
        myPolylineSource.add(myPolylineTwo);
        myPolylineSource.add(myPolylineThree);

        PolylineGeometry unifiedResult = PolylineGeometryUtils.createSinglePolyline(myPolylineSource);
        Assert.assertNotNull(unifiedResult);
        Assert.assertEquals(4, unifiedResult.getVertices().size());

        Set<Vector3d> vertices = new HashSet<>(unifiedResult.getVertices().size());
        unifiedResult.getVertices().forEach(p -> vertices.add(p.asVector3d()));

        Assert.assertTrue(vertices.contains(new Vector3d(0, 0, 0)));
        Assert.assertTrue(vertices.contains(new Vector3d(1, 0, 0)));
        Assert.assertTrue(vertices.contains(new Vector3d(1, 1, 0)));
        Assert.assertTrue(vertices.contains(new Vector3d(1, 2, 0)));
    }

    /**
     * Test method for
     * {@link PolylineGeometryUtils#createSinglePolyline(java.util.Set)}.
     */
    @Test
    public void testCreateSinglePolyline4JoinedInput()
    {
        myPolylineSource.clear();
        myPolylineSource.add(myPolylineOne);
        myPolylineSource.add(myPolylineTwo);
        myPolylineSource.add(myPolylineThree);
        myPolylineSource.add(myPolylineFour);

        PolylineGeometry unifiedResult = PolylineGeometryUtils.createSinglePolyline(myPolylineSource);
        Assert.assertNotNull(unifiedResult);
        Assert.assertEquals(5, unifiedResult.getVertices().size());

        Set<Vector3d> vertices = new HashSet<>(unifiedResult.getVertices().size());
        unifiedResult.getVertices().forEach(p -> vertices.add(p.asVector3d()));

        Assert.assertTrue(vertices.contains(new Vector3d(0, 0, 0)));
        Assert.assertTrue(vertices.contains(new Vector3d(1, 0, 0)));
        Assert.assertTrue(vertices.contains(new Vector3d(1, 1, 0)));
        Assert.assertTrue(vertices.contains(new Vector3d(1, 2, 0)));
        Assert.assertTrue(vertices.contains(new Vector3d(0, 2, 0)));
    }

    /**
     * Test method for
     * {@link PolylineGeometryUtils#createSinglePolyline(java.util.Set)}.
     */
    @Test
    public void testCreateSinglePolyline5JoinedInput()
    {
        myPolylineSource.clear();
        myPolylineSource.add(myPolylineOne);
        myPolylineSource.add(myPolylineTwo);
        myPolylineSource.add(myPolylineThree);
        myPolylineSource.add(myPolylineFour);
        myPolylineSource.add(myPolylineFive);

        PolylineGeometry unifiedResult = PolylineGeometryUtils.createSinglePolyline(myPolylineSource);
        Assert.assertNotNull(unifiedResult);
        Assert.assertEquals(6, unifiedResult.getVertices().size());

        Set<Vector3d> vertices = new HashSet<>(unifiedResult.getVertices().size());
        unifiedResult.getVertices().forEach(p -> vertices.add(p.asVector3d()));

        Assert.assertTrue(vertices.contains(new Vector3d(0, 0, 0)));
        Assert.assertTrue(vertices.contains(new Vector3d(1, 0, 0)));
        Assert.assertTrue(vertices.contains(new Vector3d(1, 1, 0)));
        Assert.assertTrue(vertices.contains(new Vector3d(1, 2, 0)));
        Assert.assertTrue(vertices.contains(new Vector3d(0, 2, 0)));
        Assert.assertTrue(vertices.contains(new Vector3d(2, 2, 0)));
    }

    /**
     * Test method for
     * {@link PolylineGeometryUtils#createSinglePolyline(java.util.Set)}.
     */
    @Test
    public void testCreateSinglePolyline6JoinedInput()
    {
        myPolylineSource.clear();
        myPolylineSource.add(myPolylineOne);
        myPolylineSource.add(myPolylineTwo);
        myPolylineSource.add(myPolylineThree);
        myPolylineSource.add(myPolylineFour);
        myPolylineSource.add(myPolylineFive);
        myPolylineSource.add(myPolylineSix);

        PolylineGeometry unifiedResult = PolylineGeometryUtils.createSinglePolyline(myPolylineSource);
        Assert.assertNotNull(unifiedResult);
        Assert.assertEquals(7, unifiedResult.getVertices().size());

        Set<Vector3d> vertices = new HashSet<>(unifiedResult.getVertices().size());
        unifiedResult.getVertices().forEach(p -> vertices.add(p.asVector3d()));

        Assert.assertTrue(vertices.contains(new Vector3d(0, 0, 0)));
        Assert.assertTrue(vertices.contains(new Vector3d(1, 0, 0)));
        Assert.assertTrue(vertices.contains(new Vector3d(1, 1, 0)));
        Assert.assertTrue(vertices.contains(new Vector3d(1, 2, 0)));
        Assert.assertTrue(vertices.contains(new Vector3d(0, 2, 0)));
        Assert.assertTrue(vertices.contains(new Vector3d(2, 2, 0)));
        Assert.assertTrue(vertices.contains(new Vector3d(2, 3, 0)));
    }

    /**
     * Test method for
     * {@link PolylineGeometryUtils#createSinglePolyline(java.util.Set)}.
     */
    @Test
    public void testCreateSinglePolyline7JoinedInput()
    {
        myPolylineSource.clear();
        myPolylineSource.add(myPolylineOne);
        myPolylineSource.add(myPolylineTwo);
        myPolylineSource.add(myPolylineThree);
        myPolylineSource.add(myPolylineFour);
        myPolylineSource.add(myPolylineFive);
        myPolylineSource.add(myPolylineSix);
        myPolylineSource.add(myPolylineSeven);

        PolylineGeometry unifiedResult = PolylineGeometryUtils.createSinglePolyline(myPolylineSource);
        Assert.assertNotNull(unifiedResult);
        Assert.assertEquals(8, unifiedResult.getVertices().size());

        Set<Vector3d> vertices = new HashSet<>(unifiedResult.getVertices().size());
        unifiedResult.getVertices().forEach(p -> vertices.add(p.asVector3d()));

        Assert.assertTrue(vertices.contains(new Vector3d(0, 0, 0)));
        Assert.assertTrue(vertices.contains(new Vector3d(1, 0, 0)));
        Assert.assertTrue(vertices.contains(new Vector3d(1, 1, 0)));
        Assert.assertTrue(vertices.contains(new Vector3d(1, 2, 0)));
        Assert.assertTrue(vertices.contains(new Vector3d(0, 2, 0)));
        Assert.assertTrue(vertices.contains(new Vector3d(2, 2, 0)));
        Assert.assertTrue(vertices.contains(new Vector3d(2, 3, 0)));
        Assert.assertTrue(vertices.contains(new Vector3d(1, 3, 0)));
    }

    /**
     * Test method for
     * {@link PolylineGeometryUtils#createSinglePolyline(java.util.Set)}.
     */
    @Test
    public void testCreateSinglePolyline8JoinedInput()
    {
        myPolylineSource.clear();
        myPolylineSource.add(myPolylineOne);
        myPolylineSource.add(myPolylineTwo);
        myPolylineSource.add(myPolylineThree);
        myPolylineSource.add(myPolylineFour);
        myPolylineSource.add(myPolylineFive);
        myPolylineSource.add(myPolylineSix);
        myPolylineSource.add(myPolylineSeven);
        myPolylineSource.add(myPolylineEight);

        PolylineGeometry unifiedResult = PolylineGeometryUtils.createSinglePolyline(myPolylineSource);
        Assert.assertNotNull(unifiedResult);
        Assert.assertEquals(8, unifiedResult.getVertices().size());

        Set<Vector3d> vertices = new HashSet<>(unifiedResult.getVertices().size());
        unifiedResult.getVertices().forEach(p -> vertices.add(p.asVector3d()));

        Assert.assertTrue(vertices.contains(new Vector3d(0, 0, 0)));
        Assert.assertTrue(vertices.contains(new Vector3d(1, 0, 0)));
        Assert.assertTrue(vertices.contains(new Vector3d(1, 1, 0)));
        Assert.assertTrue(vertices.contains(new Vector3d(1, 2, 0)));
        Assert.assertTrue(vertices.contains(new Vector3d(0, 2, 0)));
        Assert.assertTrue(vertices.contains(new Vector3d(2, 2, 0)));
        Assert.assertTrue(vertices.contains(new Vector3d(2, 3, 0)));
        Assert.assertTrue(vertices.contains(new Vector3d(1, 3, 0)));
    }

    /**
     * Test method for
     * {@link PolylineGeometryUtils#createSinglePolyline(java.util.Set)}.
     */
    @Test
    public void testCreateSinglePolyline9JoinedInput()
    {
        myPolylineSource.clear();
        myPolylineSource.add(myPolylineOne);
        myPolylineSource.add(myPolylineTwo);
        myPolylineSource.add(myPolylineThree);
        myPolylineSource.add(myPolylineFour);
        myPolylineSource.add(myPolylineFive);
        myPolylineSource.add(myPolylineSix);
        myPolylineSource.add(myPolylineSeven);
        myPolylineSource.add(myPolylineEight);
        myPolylineSource.add(myPolylineNine);

        PolylineGeometry unifiedResult = PolylineGeometryUtils.createSinglePolyline(myPolylineSource);
        Assert.assertNotNull(unifiedResult);
        Assert.assertEquals(9, unifiedResult.getVertices().size());

        Set<Vector3d> vertices = new HashSet<>(unifiedResult.getVertices().size());
        unifiedResult.getVertices().forEach(p -> vertices.add(p.asVector3d()));

        Assert.assertTrue(vertices.contains(new Vector3d(0, 0, 0)));
        Assert.assertTrue(vertices.contains(new Vector3d(1, 0, 0)));
        Assert.assertTrue(vertices.contains(new Vector3d(1, 1, 0)));
        Assert.assertTrue(vertices.contains(new Vector3d(1, 2, 0)));
        Assert.assertTrue(vertices.contains(new Vector3d(0, 2, 0)));
        Assert.assertTrue(vertices.contains(new Vector3d(2, 2, 0)));
        Assert.assertTrue(vertices.contains(new Vector3d(2, 3, 0)));
        Assert.assertTrue(vertices.contains(new Vector3d(1, 3, 0)));
        Assert.assertTrue(vertices.contains(new Vector3d(2, 4, 0)));
    }

    /**
     * Test method for
     * {@link PolylineGeometryUtils#createSinglePolyline(java.util.Set)}.
     */
    @Test
    public void testCreateSinglePolyline10JoinedInput()
    {
        myPolylineSource.clear();
        myPolylineSource.add(myPolylineOne);
        myPolylineSource.add(myPolylineTwo);
        myPolylineSource.add(myPolylineThree);
        myPolylineSource.add(myPolylineFour);
        myPolylineSource.add(myPolylineFive);
        myPolylineSource.add(myPolylineSix);
        myPolylineSource.add(myPolylineSeven);
        myPolylineSource.add(myPolylineEight);
        myPolylineSource.add(myPolylineNine);
        myPolylineSource.add(myPolylineTen);

        PolylineGeometry unifiedResult = PolylineGeometryUtils.createSinglePolyline(myPolylineSource);
        Assert.assertNotNull(unifiedResult);
        Assert.assertEquals(9, unifiedResult.getVertices().size());

        Set<Vector3d> vertices = new HashSet<>(unifiedResult.getVertices().size());
        unifiedResult.getVertices().forEach(p -> vertices.add(p.asVector3d()));

        Assert.assertTrue(vertices.contains(new Vector3d(0, 0, 0)));
        Assert.assertTrue(vertices.contains(new Vector3d(1, 0, 0)));
        Assert.assertTrue(vertices.contains(new Vector3d(1, 1, 0)));
        Assert.assertTrue(vertices.contains(new Vector3d(1, 2, 0)));
        Assert.assertTrue(vertices.contains(new Vector3d(0, 2, 0)));
        Assert.assertTrue(vertices.contains(new Vector3d(2, 2, 0)));
        Assert.assertTrue(vertices.contains(new Vector3d(2, 3, 0)));
        Assert.assertTrue(vertices.contains(new Vector3d(1, 3, 0)));
        Assert.assertTrue(vertices.contains(new Vector3d(2, 4, 0)));
    }
}

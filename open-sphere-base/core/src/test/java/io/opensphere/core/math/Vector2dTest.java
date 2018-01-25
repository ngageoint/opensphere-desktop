package io.opensphere.core.math;

import org.junit.Assert;
import org.junit.Test;

/** Tests for {@link Vector2d}. */
public class Vector2dTest
{
    /**
     * Test hash code.
     */
    @Test
    public void testHashCode()
    {
        Assert.assertEquals(new Vector2d(1., 2.).hashCode(), new Vector2d(1., 2.).hashCode());
        Assert.assertFalse(new Vector2d(1., 2.).hashCode() == new Vector2d(1., 3.).hashCode());
        Assert.assertFalse(new Vector2d(1., 2.).hashCode() == new Vector2d(3., 2.).hashCode());
    }

    /**
     * Test distance squared.
     */
    @Test
    public void testDistanceSquared()
    {
        Assert.assertEquals(25, new Vector2d(4., 5.).distanceSquared(new Vector2d(1., 1.)), 0.);
    }

    /**
     * Test get length squared.
     */
    @Test
    public void testGetLengthSquared()
    {
        Assert.assertEquals(25, new Vector2d(3., 4.).getLengthSquared(), 0.);
    }

    /**
     * Test counter clockwise.
     */
    @Test
    public void testCounterClockwise()
    {
        Assert.assertEquals(1., Vector2d.counterClockwise(new Vector2d(1., 0.), new Vector2d(0., 1.), new Vector2d(0., 0.)), 0.);
        Assert.assertEquals(-1., Vector2d.counterClockwise(new Vector2d(1., 0.), new Vector2d(0., 0.), new Vector2d(0., 1.)), 0.);
        Assert.assertEquals(0., Vector2d.counterClockwise(new Vector2d(1., 0.), new Vector2d(0., 0.), new Vector2d(.5, 0.)), 0.);
    }

    /**
     * Test add vector2d.
     */
    @Test
    public void testAddVector2d()
    {
        Assert.assertEquals(new Vector2d(4., 5.), new Vector2d(3., 4.).add(new Vector2d(1., 1.)));
    }

    /**
     * Test add double double.
     */
    @Test
    public void testAddDoubleDouble()
    {
        Assert.assertEquals(new Vector2d(4., 5.), new Vector2d(3., 4.).add(1., 1.));
    }

    /**
     * Test angle between.
     */
    @Test
    public void testAngleBetween()
    {
        Assert.assertEquals(Math.PI * .5, new Vector2d(1., 0.).angleBetween(new Vector2d(0., 1.)), 0.);
        Assert.assertEquals(Math.PI * -.5, new Vector2d(0., 1.).angleBetween(new Vector2d(1., 0.)), 0.);
    }

    /**
     * Test determinant.
     */
    @Test
    public void testDeterminant()
    {
        Assert.assertEquals(1., new Vector2d(1., 0.).determinant(new Vector2d(0., 1.)), 0.);
        Assert.assertEquals(-1., new Vector2d(1., 1.).determinant(new Vector2d(-1., -2.)), 0.);
    }

    /**
     * Test divide.
     */
    @Test
    public void testDivide()
    {
        Assert.assertEquals(new Vector2d(1., 2.), new Vector2d(4., 8.).divide(4.));
    }

    /**
     * Test dot.
     */
    @Test
    public void testDot()
    {
        Assert.assertEquals(0., new Vector2d(1., 0.).dot(new Vector2d(0., 1.)), 0.);
        Assert.assertEquals(1., new Vector2d(1., 0.).dot(new Vector2d(1., 1.)), 0.);
        Assert.assertEquals(.5, new Vector2d(1., 0.).dot(new Vector2d(.5, 1.)), 0.);
        Assert.assertEquals(.5, new Vector2d(1., 0.).dot(new Vector2d(.5, -1.)), 0.);
    }

    /**
     * Test equals object.
     */
    @Test
    public void testEqualsObject()
    {
        Assert.assertTrue(new Vector2d(1., 2.).equals(new Vector2d(1., 2.)));
        Assert.assertFalse(new Vector2d(1., 2.).equals(new Vector2d(1., 3.)));
        Assert.assertFalse(new Vector2d(1., 2.).equals(new Vector2d(3., 2.)));
    }

    /**
     * Test get angle.
     */
    @Test
    public void testGetAngle()
    {
        Assert.assertEquals(Math.PI * .25, new Vector2d(1., 1.).getAngle(), 0.);
    }

    /**
     * Test get normalized.
     */
    @Test
    public void testGetNormalized()
    {
        Assert.assertEquals(new Vector2d(-.5 * Math.sqrt(2.), -.5 * Math.sqrt(2.)), new Vector2d(-2., -2.).getNormalized());
    }

    /**
     * Test get perpendicular.
     */
    @Test
    public void testGetPerpendicular()
    {
        Assert.assertEquals(new Vector2d(0., -1.), new Vector2d(1., 0.).getPerpendicular());
    }

    /**
     * Test interpolate.
     */
    @Test
    public void testInterpolate()
    {
        Assert.assertEquals(new Vector2d(.2, 1.), new Vector2d(0., 1.).interpolate(new Vector2d(1., 1.), .2));
    }

    /**
     * Test multiply.
     */
    @Test
    public void testMultiply()
    {
        Assert.assertEquals(new Vector2d(4., 8.), new Vector2d(1., 2.).multiply(4.));
    }

    /**
     * Test rotate around origin.
     */
    @Test
    public void testRotateAroundOrigin()
    {
        Assert.assertEquals(new Vector2d(-1., 1.), new Vector2d(1., 1.).rotateAroundOrigin(Math.PI * .5));
    }

    /**
     * Test smallest angle between.
     */
    @Test
    public void testSmallestAngleBetween()
    {
        Assert.assertEquals(Math.PI * .5, new Vector2d(1., 0.).smallestAngleBetween(new Vector2d(0., 1.)), 0.);
        Assert.assertEquals(Math.PI * .5, new Vector2d(0., 1.).smallestAngleBetween(new Vector2d(1., 0.)), 0.);
        Assert.assertEquals(Math.PI, new Vector2d(1., 0.).smallestAngleBetween(new Vector2d(-1., 0.)), 0.);
    }

    /**
     * Test subtract.
     */
    @Test
    public void testSubtract()
    {
        Assert.assertEquals(new Vector2d(1., 1.), new Vector2d(4., 5.).subtract(new Vector2d(3., 4.)));
    }

    /**
     * Test to array.
     */
    @Test
    public void testToArray()
    {
        Assert.assertArrayEquals(new double[] { 3., 4. }, new Vector2d(3., 4.).toArray(new double[2]), 0.);
    }
}

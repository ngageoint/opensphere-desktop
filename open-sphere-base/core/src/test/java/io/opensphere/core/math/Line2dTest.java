package io.opensphere.core.math;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test for {@link Line2d}.
 */
public class Line2dTest
{
    /** Test for {@link Line2d#getSegmentIntersection(Vector2d, Vector2d)}. */
    @Test
    public void testGetSegmentIntersection()
    {
        Line2d line = new Line2d(new Vector2d(0., 0.), new Vector2d(0., 2.));

        // Line segment is on line.
        Assert.assertEquals(Arrays.asList(new Vector2d(0., 0.), new Vector2d(2., 0.)),
                line.getSegmentIntersection(new Vector2d(0., 0.), new Vector2d(2., 0.)));

        // Line intersects line segment at one end.
        Assert.assertEquals(Collections.singletonList(new Vector2d(0., 0.)),
                line.getSegmentIntersection(new Vector2d(0., 0.), new Vector2d(0., 2.)));
        Assert.assertEquals(Collections.singletonList(new Vector2d(1., 0.)),
                line.getSegmentIntersection(new Vector2d(1., 0.), new Vector2d(1., 2.)));

        // Line segment is parallel to line.
        Assert.assertTrue(line.getSegmentIntersection(new Vector2d(0., 2.), new Vector2d(1., 2.)).isEmpty());

        // Line segment crosses line.
        Assert.assertEquals(Collections.singletonList(new Vector2d(1., 0.)),
                line.getSegmentIntersection(new Vector2d(1., -1.), new Vector2d(1., 1.)));
    }

    /**
     * Test for {@link Line2d#getSegmentIntersection(Vector2d, Vector2d)} with
     * zero-length line segment.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testGetSegmentIntersectionBadArgument()
    {
        Line2d line = new Line2d(new Vector2d(0., 0.), new Vector2d(0., 2.));
        line.getSegmentIntersection(new Vector2d(0., 0.), new Vector2d(0., 0.));
    }

    /**
     * Test for {@link Line2d#getIntersection(Ray2d, boolean)}.
     */
    @Test
    public void testIntersectionRay2d()
    {
        // X axis
        Line2d line = new Line2d(new Vector2d(0., 0.), new Vector2d(0., 1.));

        // Parallel ray.
        Ray2d parallelRay = new Ray2d(new Vector2d(0., 1.), new Vector2d(2., 0.));

        Assert.assertNull(line.getIntersection(parallelRay, true));

        Ray2d ray = new Ray2d(new Vector2d(0., 1.), new Vector2d(4., -1.));

        Assert.assertEquals(new Vector2d(4., 0.), line.getIntersection(ray, true));
    }
}

package io.opensphere.core.math;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import io.opensphere.core.util.MathUtil;

/**
 * Test for {@link Plane}.
 */
public class PlaneTest
{
    /**
     * Test for {@link Plane#getIntersection(Ray3d ray)}.
     */
    @Test
    public void testGetIntersectionWithRay()
    {
        Vector3d planePos = new Vector3d(-40.78934, -154.897456, 50.56);
        Vector3d planeNormal = new Vector3d(0., 0., -1.);
        Plane plane = new Plane(planePos, planeNormal);

        Vector3d rayPos = new Vector3d(20.987, 25.678, 5.);
        Vector3d rayDir = new Vector3d(0., 0., 1.);
        Ray3d ray = new Ray3d(rayPos, rayDir);

        Vector3d intersect = plane.getIntersection(ray);

        // With the plane being parallel to the xy-plane and ray direction being
        // parallel to the z-axis, we can obtain the expected intersect from the
        // components of the appropriate vectors.
        Vector3d expected = new Vector3d(20.987, 25.678, 50.56);
        assertEquals(intersect, expected);
        assertTrue(MathUtil.isZero(planeNormal.dot(intersect.subtract(planePos))));
    }
}

package io.opensphere.core.math;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Assert;
import org.junit.Test;

import io.opensphere.core.util.MathUtil;

/**
 * Tests for the {@link DefaultEllipsoid} class.
 */
public class DefaultEllipsoidTest
{
    /**
     * Test for {@link DefaultEllipsoid#directionToLocal(Vector3d)}.
     */
    @Test
    public void testDirectionToLocal()
    {
        Vector3d center = new Vector3d(3., 4., 13.);
        Vector3d xAxis = new Vector3d(7., 0., 0.);
        Vector3d yAxis = new Vector3d(0., 11., 0.);
        Vector3d zAxis = new Vector3d(0., 0., 13.);
        Ellipsoid ell = new DefaultEllipsoid(center, xAxis, yAxis, zAxis);

        Assert.assertEquals(new Vector3d(2., 3., 5.), ell.directionToLocal(new Vector3d(14., 33., 65.)));
    }

    /**
     * Test for {@link DefaultEllipsoid#directionToModel(Vector3d)}.
     */
    @Test
    public void testDirectionToModel()
    {
        Vector3d center = new Vector3d(3., 4., 13.);
        Vector3d xAxis = new Vector3d(7., 0., 0.);
        Vector3d yAxis = new Vector3d(0., 11., 0.);
        Vector3d zAxis = new Vector3d(0., 0., 13.);
        Ellipsoid ell = new DefaultEllipsoid(center, xAxis, yAxis, zAxis);

        Assert.assertEquals(new Vector3d(14., 33., 65.), ell.directionToModel(new Vector3d(2., 3., 5.)));
    }

    /**
     * Test to ensure that the relative direction stays the same when
     * transforming from model coordinates to local coordinates and back.
     */
    @Test
    public void testGetDirectionLength()
    {
        double xyRadius = 200;
        double zRadius = 20;

        Vector3d center = new Vector3d(3., 4., 13.);
        Vector3d loc1Direction = new Vector3d(200, 150, 45).getNormalized();
        Vector3d loc2Direction = new Vector3d(-200, 100, 25).getNormalized();
        Vector3d loc1 = loc1Direction.multiply(xyRadius).add(center);
        Vector3d loc2 = loc2Direction.multiply(xyRadius).add(center);

        Ellipsoid ell = createEllipsoid(xyRadius, zRadius, center, loc1, loc2);

        Vector3d normal = new Vector3d(4., 1., 1.).getNormalized();
        Vector3d local = ell.directionToLocal(normal).getNormalized();
        Vector3d recNormal = ell.directionToModel(local).getNormalized();

        assertEquals(normal, recNormal);

        Vector3d normal2 = Vector3d.UNIT_X;
        Vector3d local2 = ell.directionToLocal(normal2).getNormalized();
        Vector3d recNormal2 = ell.directionToModel(local2).getNormalized();

        assertEquals(normal2, recNormal2);
    }

    /**
     * Test a 45-degree ray intersecting a 2x2x1 ellipsoid.
     */
    @Test
    public void testGetIntersectionEllipsoid45()
    {
        final double xRadius = 20;
        final double yRadius = 20;
        final double zRadius = 10;
        Ellipsoid shape = new DefaultEllipsoid(xRadius, yRadius, zRadius);
        double zInt = 1. / Math.sqrt(1. / xRadius / xRadius + 1. / zRadius / zRadius);
        double sqrt2 = Math.sqrt(2.);
        Ray3d ray = new Ray3d(new Vector3d(0., 0., zInt * 2.), new Vector3d(1. / sqrt2, 1. / sqrt2, -1.));
        Vector3d intersection = shape.getIntersection(ray);
        Vector3d expected = new Vector3d(zInt / sqrt2, zInt / sqrt2, zInt);
        assertEquals(expected, intersection);
    }

    /**
     * Test a 30-degree ray intersecting the sphere.
     */
    @Test
    public void testGetIntersectionSphere30()
    {
        final double radius = 10.;
        Ellipsoid sphere = new DefaultEllipsoid(radius, radius, radius);
        final double sqrt3 = Math.sqrt(3);
        Ray3d ray = new Ray3d(new Vector3d(radius * sqrt3, 0., 0.), new Vector3d(-sqrt3, 1., 0.));
        Vector3d intersection = sphere.getIntersection(ray);
        Vector3d expected = new Vector3d(radius * sqrt3 / 2., radius / 2., 0.);
        assertEquals(expected, intersection);
    }

    /**
     * Test a ray that does not intersect the sphere.
     */
    @Test
    public void testGetIntersectionSphere46()
    {
        final double radius = 10.;
        Ellipsoid sphere = new DefaultEllipsoid(radius, radius, radius);
        double sqrt2 = Math.sqrt(2);
        final int ang = 46;
        Ray3d ray = new Ray3d(new Vector3d(radius * sqrt2, 0., 0.), new Vector3d(-1, Math.tan(Math.toRadians(ang)), 0.));
        Vector3d intersection = sphere.getIntersection(ray);
        assertNull(intersection);
    }

    /**
     * Test for {@link DefaultEllipsoid#getTangent(Vector3d)}.
     */
    @Test
    public void testGetTangent()
    {
        double sqrt2 = Math.sqrt(2.);
        double sqrt3 = Math.sqrt(3.);
        Vector3d center = new Vector3d(3., 4., 13.);

        // X': length 1, 60 degrees clockwise
        Vector3d xAxis = new Vector3d(.5, -sqrt3 * .5, 0.);

        // Y': length 2, 60 degree clockwise then 45 degrees around X'
        Vector3d yAxis = new Vector3d(sqrt3 / sqrt2, 1. / sqrt2, sqrt2);

        // Z': length sqrt(8), 45 degrees around X'.
        Vector3d zAxis = new Vector3d(-sqrt3, -1., 2.);

        Ellipsoid ell = new DefaultEllipsoid(center, xAxis, yAxis, zAxis);

        // A point on the ellipsoid in ellipsoid-local coordinates.
        double x = .5;
        double y = .5;
        double z = Math.sqrt(5.5);

        // The normal to the ellipse at the point, in ellipsoid-local
        // coordinates.
        double normalX = 4. * x / y;
        double normalY = 1.;
        double normalZ = .5 * z / y;

        // The same point, rotated to absolute coordinates.
        double x2 = x * .5 + (y - z) * sqrt2 * sqrt3 * .25;
        double y2 = -x * sqrt3 * .5 + (y - z) * sqrt2 * .25;
        double z2 = (y + z) * sqrt2 * .5;

        // The normal, rotated to absolute coordinates.
        double normalX2 = normalX * .5 + (normalY - normalZ) * sqrt2 * sqrt3 * .25;
        double normalY2 = -normalX * sqrt3 * .5 + (normalY - normalZ) * sqrt2 * .25;
        double normalZ2 = (normalY + normalZ) * sqrt2 * .5;

        Vector3d targetPoint = new Vector3d(x2, y2, z2).add(center);

        // Construct a plane normal to the ellipsoid at the target point.
        Vector3d planeNormal = new Vector3d(normalX2, normalY2, normalZ2).getNormalized();

        Vector3d tangentPoint = ell.getTangent(planeNormal);

        Assert.assertEquals(targetPoint, tangentPoint);

        // Reverse the normal and see if the other side of the ellipsoid is
        // found.
        Vector3d reversedNormal = planeNormal.multiply(-1.);

        Vector3d tangentPoint2 = ell.getTangent(reversedNormal);

        Vector3d targetPoint2 = new Vector3d(-x2, -y2, -z2).add(center);

        Assert.assertEquals(targetPoint2, tangentPoint2);
    }

    /**
     * Test for {@link DefaultEllipsoid#localToModel(Vector3d)}.
     */
    @Test
    public void testLocalToModel()
    {
        Vector3d center = new Vector3d(3., 4., 13.);
        Vector3d xAxis = new Vector3d(7., 0., 0.);
        Vector3d yAxis = new Vector3d(0., 11., 0.);
        Vector3d zAxis = new Vector3d(0., 0., 13.);
        Ellipsoid ell = new DefaultEllipsoid(center, xAxis, yAxis, zAxis);

        Assert.assertEquals(new Vector3d(17., 37., 78.), ell.localToModel(new Vector3d(2., 3., 5.)));
    }

    /**
     * Test for {@link DefaultEllipsoid#modelToLocal(Vector3d)}.
     */
    @Test
    public void testModelToLocal()
    {
        Vector3d center = new Vector3d(3., 4., 13.);
        Vector3d xAxis = new Vector3d(7., 0., 0.);
        Vector3d yAxis = new Vector3d(0., 11., 0.);
        Vector3d zAxis = new Vector3d(0., 0., 13.);
        Ellipsoid ell = new DefaultEllipsoid(center, xAxis, yAxis, zAxis);

        Assert.assertEquals(new Vector3d(2., 3., 5.), ell.modelToLocal(new Vector3d(17., 37., 78.)));
    }

    /**
     * Test for {@link DefaultEllipsoid#normalToLocal(Vector3d)}.
     */
    @Test
    public void testNormalToLocal()
    {
        Vector3d center = new Vector3d(3., 4., 13.);
        Vector3d xAxis = new Vector3d(2., 0., 0.);
        Vector3d yAxis = new Vector3d(0., 3., 0.);
        Vector3d zAxis = new Vector3d(0., 0., 5.);
        Ellipsoid ell = new DefaultEllipsoid(center, xAxis, yAxis, zAxis);

        Assert.assertEquals(new Vector3d(2., 3., 5.), ell.normalToLocal(new Vector3d(1., 1., 1.)));
    }

    /**
     * Transform the x axis in the ellipsoids local coordinates to the real x
     * axis.
     */
    @Test
    public void testTransformToUnitSphere()
    {
        Vector3d center = new Vector3d(3., 0., 0.);
        Vector3d xAxis = new Vector3d(2., 1., 0.);
        Vector3d yAxis = new Vector3d(-1., 2., 0.);
        Vector3d zAxis = Vector3d.UNIT_Z;
        Ellipsoid ell = new DefaultEllipsoid(center, xAxis, yAxis, zAxis);

        Vector3d realX = Vector3d.UNIT_X;

        assertEquals(realX, ell.modelToLocal(xAxis.add(center)));
    }

    /**
     * Create a complicated ellipsoid and transform two known points onto the
     * unit sphere.
     */
    @Test
    public void testTransformToUnitSphere2()
    {
        double xyRadius = 200;
        double zRadius = 20;

        Vector3d center = new Vector3d(3., 4., 13.);
        Vector3d loc1Direction = new Vector3d(200, 150, 45).getNormalized();
        Vector3d loc2Direction = new Vector3d(-200, 100, 25).getNormalized();
        Vector3d loc1 = loc1Direction.multiply(xyRadius).add(center);
        Vector3d loc2 = loc2Direction.multiply(xyRadius).add(center);

        Ellipsoid ell = createEllipsoid(xyRadius, zRadius, center, loc1, loc2);

        Vector3d realX = Vector3d.UNIT_X;

        assertEquals(realX, ell.modelToLocal(loc1));
        assertTrue(MathUtil.isZero(ell.modelToLocal(loc2).getLength() - 1.));
    }

    /**
     * Create an ellipsoid for testing.
     *
     * @param xyRadius The radius in the x and y directions.
     * @param zRadius The radius in the z direction.
     * @param center The center of the ellipsoid.
     * @param loc1 A point along the x axis.
     * @param loc2 A point in the x-y plane.
     * @return An ellipsoid.
     */
    private Ellipsoid createEllipsoid(double xyRadius, double zRadius, Vector3d center, Vector3d loc1, Vector3d loc2)
    {
        Vector3d xAxis = loc1.subtract(center).getNormalized();
        Vector3d yAxis = loc2.subtract(center).getNormalized();
        Vector3d zAxis = xAxis.cross(yAxis).getNormalized();
        // make the y axis perpendicular to both other axes.
        yAxis = zAxis.cross(xAxis).getNormalized();

        Ellipsoid ell = new DefaultEllipsoid(center, xAxis.multiply(xyRadius), yAxis.multiply(xyRadius), zAxis.multiply(zRadius));
        return ell;
    }
}

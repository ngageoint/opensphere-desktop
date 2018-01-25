package io.opensphere.core.math;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import io.opensphere.core.util.collections.New;

/**
 * Test for {@link Matrix3d}.
 */
public class Matrix3dTest
{
    /**
     * Test for
     * {@link Matrix3d#getQuadToQuadTransform(java.util.List, java.util.List)}.
     */
    @Test
    public void testQuadToQuad()
    {
        List<Vector2d> source = New.list();
        source.add(new Vector2d(10., 10.));
        source.add(new Vector2d(20., 9.));
        source.add(new Vector2d(25., 25.));
        source.add(new Vector2d(11., 20.));

        List<Vector2d> destination = New.list();
        destination.add(new Vector2d(50., 50.));
        destination.add(new Vector2d(40., 49.));
        destination.add(new Vector2d(39., 41.));
        destination.add(new Vector2d(50., 40.));

        Matrix3d squareToQuad = Matrix3d.getQuadToQuadTransform(source, destination);

        // Test to make sure that the corners map correctly.
        Vector2d transformed = squareToQuad.applyPerspectiveTransform(source.get(0));
        Assert.assertEquals(destination.get(0), transformed);
        transformed = squareToQuad.applyPerspectiveTransform(source.get(1));
        Assert.assertEquals(destination.get(1), transformed);
        transformed = squareToQuad.applyPerspectiveTransform(source.get(2));
        Assert.assertEquals(destination.get(2), transformed);
        transformed = squareToQuad.applyPerspectiveTransform(source.get(3));
        Assert.assertEquals(destination.get(3), transformed);
    }

    /**
     * Test for
     * {@link Matrix3d#getQuadToSquareTransform(Vector2d, Vector2d, Vector2d, Vector2d)}
     * .
     */
    @Test
    public void testQuadToSquare()
    {
        Vector2d ll = new Vector2d(10., 10.);
        Vector2d lr = new Vector2d(20., 9.);
        Vector2d ur = new Vector2d(25., 25.);
        Vector2d ul = new Vector2d(11., 20.);

        Matrix3d squareToQuad = Matrix3d.getQuadToSquareTransform(ll, lr, ur, ul);

        // Test to make sure that the corners map correctly.
        Vector2d transformed = squareToQuad.applyPerspectiveTransform(ll);
        Assert.assertEquals(new Vector2d(0., 0.), transformed);
        transformed = squareToQuad.applyPerspectiveTransform(lr);
        Assert.assertEquals(new Vector2d(1., 0.), transformed);
        transformed = squareToQuad.applyPerspectiveTransform(ur);
        Assert.assertEquals(new Vector2d(1., 1.), transformed);
        transformed = squareToQuad.applyPerspectiveTransform(ul);
        Assert.assertEquals(new Vector2d(0., 1.), transformed);
    }

    /**
     * Test for
     * {@link Matrix3d#getSquareToQuadTransform(Vector2d, Vector2d, Vector2d, Vector2d)}
     * .
     */
    @Test
    public void testSquareToQuad()
    {
        Vector2d ll = new Vector2d(10., 10.);
        Vector2d lr = new Vector2d(20., 9.);
        Vector2d ur = new Vector2d(25., 25.);
        Vector2d ul = new Vector2d(11., 20.);

        Matrix3d squareToQuad = Matrix3d.getSquareToQuadTransform(ll, lr, ur, ul);

        // Test to make sure that the corners map correctly.
        Vector2d transformed = squareToQuad.applyPerspectiveTransform(new Vector2d(0., 0.));
        Assert.assertEquals(ll, transformed);
        transformed = squareToQuad.applyPerspectiveTransform(new Vector2d(1., 0.));
        Assert.assertEquals(lr, transformed);
        transformed = squareToQuad.applyPerspectiveTransform(new Vector2d(1., 1.));
        Assert.assertEquals(ur, transformed);
        transformed = squareToQuad.applyPerspectiveTransform(new Vector2d(0., 1.));
        Assert.assertEquals(ul, transformed);
    }
}

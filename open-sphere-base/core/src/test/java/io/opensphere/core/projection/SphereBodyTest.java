package io.opensphere.core.projection;

import org.junit.Test;

import io.opensphere.core.math.Vector3d;
import io.opensphere.core.model.Altitude.ReferenceLevel;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.util.lang.UnexpectedEnumException;
import org.junit.Assert;

/** Test for {@link SphereBody}. */
public class SphereBodyTest
{
    /**
     * Test {@link SphereBody#convertToModel(GeographicPosition, Vector3d)}.
     */
    @Test
    public void testConvertToModel()
    {
        double r = 5.3;
        SphereBody body = new SphereBody(r);
        Assert.assertEquals(new Vector3d(r, 0., 0.),
                body.convertToModel(new GeographicPosition(LatLonAlt.createFromDegrees(0., 0.)), Vector3d.ORIGIN));
        Assert.assertEquals(new Vector3d(0., r, 0.),
                body.convertToModel(new GeographicPosition(LatLonAlt.createFromDegrees(0., 90.)), Vector3d.ORIGIN));
        Assert.assertEquals(new Vector3d(-r, 0., 0.),
                body.convertToModel(new GeographicPosition(LatLonAlt.createFromDegrees(0., 180.)), Vector3d.ORIGIN));
        Assert.assertEquals(new Vector3d(0., -r, 0.),
                body.convertToModel(new GeographicPosition(LatLonAlt.createFromDegrees(0., 270.)), Vector3d.ORIGIN));
        Assert.assertEquals(new Vector3d(0., -r, 0.),
                body.convertToModel(new GeographicPosition(LatLonAlt.createFromDegrees(0., -90.)), Vector3d.ORIGIN));
        Assert.assertEquals(new Vector3d(-r, 0., 0.),
                body.convertToModel(new GeographicPosition(LatLonAlt.createFromDegrees(0., -180.)), Vector3d.ORIGIN));

        Assert.assertEquals(new Vector3d(0., 0., r),
                body.convertToModel(new GeographicPosition(LatLonAlt.createFromDegrees(90., 0.)), Vector3d.ORIGIN));
        Assert.assertEquals(new Vector3d(0., 0., -r),
                body.convertToModel(new GeographicPosition(LatLonAlt.createFromDegrees(-90., 0.)), Vector3d.ORIGIN));

        Assert.assertEquals(new Vector3d(r / 2., r / 2., r / Math.sqrt(2)),
                body.convertToModel(new GeographicPosition(LatLonAlt.createFromDegrees(45., 45.)), Vector3d.ORIGIN));
    }

    /**
     * Test {@link SphereBody#convertToPosition(Vector3d, ReferenceLevel)}.
     */
    @Test
    public void testConvertToPosition()
    {
        double r = 5.3;
        SphereBody body = new SphereBody(r);
        Assert.assertEquals(new GeographicPosition(LatLonAlt.createFromDegreesMeters(0., 0., 0., ReferenceLevel.ELLIPSOID)),
                body.convertToPosition(new Vector3d(r, 0., 0.), ReferenceLevel.ELLIPSOID));
        Assert.assertEquals(new GeographicPosition(LatLonAlt.createFromDegreesMeters(0., 90., 0., ReferenceLevel.ELLIPSOID)),
                body.convertToPosition(new Vector3d(0., r, 0.), ReferenceLevel.ELLIPSOID));
        Assert.assertEquals(new GeographicPosition(LatLonAlt.createFromDegreesMeters(0., 180., 0., ReferenceLevel.ELLIPSOID)),
                body.convertToPosition(new Vector3d(-r, 0., 0.), ReferenceLevel.ELLIPSOID));
        Assert.assertEquals(new GeographicPosition(LatLonAlt.createFromDegreesMeters(0., -90., 0., ReferenceLevel.ELLIPSOID)),
                body.convertToPosition(new Vector3d(0., -r, 0.), ReferenceLevel.ELLIPSOID));

        Assert.assertEquals(new GeographicPosition(LatLonAlt.createFromDegreesMeters(90., 0., 0., ReferenceLevel.ELLIPSOID)),
                body.convertToPosition(new Vector3d(0., 0., r), ReferenceLevel.ELLIPSOID));
        Assert.assertEquals(new GeographicPosition(LatLonAlt.createFromDegreesMeters(-90., 0., 0., ReferenceLevel.ELLIPSOID)),
                body.convertToPosition(new Vector3d(0., 0., -r), ReferenceLevel.ELLIPSOID));

        Assert.assertEquals(new GeographicPosition(LatLonAlt.createFromDegreesMeters(45., 45., 0., ReferenceLevel.ELLIPSOID)),
                body.convertToPosition(new Vector3d(r / 2., r / 2., r / Math.sqrt(2)), ReferenceLevel.ELLIPSOID));

        Assert.assertEquals(new GeographicPosition(LatLonAlt.createFromDegreesMeters(0., 0., r, ReferenceLevel.ORIGIN)),
                body.convertToPosition(new Vector3d(r, 0., 0.), ReferenceLevel.ORIGIN));
        Assert.assertEquals(new GeographicPosition(LatLonAlt.createFromDegreesMeters(0., 90., r, ReferenceLevel.ORIGIN)),
                body.convertToPosition(new Vector3d(0., r, 0.), ReferenceLevel.ORIGIN));
        Assert.assertEquals(new GeographicPosition(LatLonAlt.createFromDegreesMeters(0., 180., r, ReferenceLevel.ORIGIN)),
                body.convertToPosition(new Vector3d(-r, 0., 0.), ReferenceLevel.ORIGIN));
        Assert.assertEquals(new GeographicPosition(LatLonAlt.createFromDegreesMeters(0., -90., r, ReferenceLevel.ORIGIN)),
                body.convertToPosition(new Vector3d(0., -r, 0.), ReferenceLevel.ORIGIN));

        Assert.assertEquals(new GeographicPosition(LatLonAlt.createFromDegreesMeters(90., 0., r, ReferenceLevel.ORIGIN)),
                body.convertToPosition(new Vector3d(0., 0., r), ReferenceLevel.ORIGIN));
        Assert.assertEquals(new GeographicPosition(LatLonAlt.createFromDegreesMeters(-90., 0., r, ReferenceLevel.ORIGIN)),
                body.convertToPosition(new Vector3d(0., 0., -r), ReferenceLevel.ORIGIN));

        Assert.assertEquals(new GeographicPosition(LatLonAlt.createFromDegreesMeters(45., 45., r, ReferenceLevel.ORIGIN)),
                body.convertToPosition(new Vector3d(r / 2., r / 2., r / Math.sqrt(2)), ReferenceLevel.ORIGIN));
    }

    /**
     * Test {@link SphereBody#convertToPosition(Vector3d, ReferenceLevel)} with
     * a bad {@link ReferenceLevel}..
     */
    @Test(expected = UnexpectedEnumException.class)
    public void testConvertToPositionBadReferenceLevel()
    {
        double r = 5.3;
        SphereBody body = new SphereBody(r);
        body.convertToPosition(new Vector3d(r, 0., 0.), ReferenceLevel.TERRAIN);
    }

    /**
     * Test {@link SphereBody#getDefaultNormalAtPosition(GeographicPosition)}.
     */
    @Test
    public void testGetDefaultNormalAtPosition()
    {
        Assert.assertEquals(new Vector3d(1., 0., 0.),
                SphereBody.getDefaultNormalAtPosition(new GeographicPosition(LatLonAlt.createFromDegrees(0., 0.))));
        Assert.assertEquals(new Vector3d(0., 1., 0.),
                SphereBody.getDefaultNormalAtPosition(new GeographicPosition(LatLonAlt.createFromDegrees(0., 90.))));
        Assert.assertEquals(new Vector3d(-1., 0., 0.),
                SphereBody.getDefaultNormalAtPosition(new GeographicPosition(LatLonAlt.createFromDegrees(0., 180.))));
        Assert.assertEquals(new Vector3d(0., -1., 0.),
                SphereBody.getDefaultNormalAtPosition(new GeographicPosition(LatLonAlt.createFromDegrees(0., 270.))));
        Assert.assertEquals(new Vector3d(0., -1., 0.),
                SphereBody.getDefaultNormalAtPosition(new GeographicPosition(LatLonAlt.createFromDegrees(0., -90.))));
        Assert.assertEquals(new Vector3d(-1., 0., 0.),
                SphereBody.getDefaultNormalAtPosition(new GeographicPosition(LatLonAlt.createFromDegrees(0., -180.))));

        Assert.assertEquals(new Vector3d(0., 0., 1.),
                SphereBody.getDefaultNormalAtPosition(new GeographicPosition(LatLonAlt.createFromDegrees(90., 0.))));
        Assert.assertEquals(new Vector3d(0., 0., -1.),
                SphereBody.getDefaultNormalAtPosition(new GeographicPosition(LatLonAlt.createFromDegrees(-90., 0.))));

        Assert.assertEquals(new Vector3d(.5, .5, 1. / Math.sqrt(2)),
                SphereBody.getDefaultNormalAtPosition(new GeographicPosition(LatLonAlt.createFromDegrees(45., 45.))));
    }

    /** Test constructor. */
    @SuppressWarnings("unused")
    @Test(expected = IllegalArgumentException.class)
    public void testSphereBody()
    {
        new SphereBody(-1.);
    }
}

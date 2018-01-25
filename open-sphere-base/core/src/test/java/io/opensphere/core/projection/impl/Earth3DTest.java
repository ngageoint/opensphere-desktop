package io.opensphere.core.projection.impl;

import org.junit.Test;

import io.opensphere.core.math.Vector3d;
import io.opensphere.core.model.Altitude;
import io.opensphere.core.model.Altitude.ReferenceLevel;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.util.MathUtil;
import org.junit.Assert;

/** Test for {@link Earth3D}. */
public class Earth3DTest
{
    /**
     * Test for {@link Earth3D#convertToModel(GeographicPosition, Vector3d)} .
     * This test only ensures that convertToModel and convertToPosition are
     * opposite operations.
     */
    @Test
    public void testConvertToModel()
    {
        final double latD = 22.5;
        final double lonD = 67.5;
        final double elevationM = 4557.75;
        Earth3D earth = new Earth3D();

        GeographicPosition origGeo = new GeographicPosition(
                LatLonAlt.createFromDegreesMeters(latD, lonD, elevationM, Altitude.ReferenceLevel.ELLIPSOID));

        // convert the geographic position to a model position.
        Vector3d model = earth.convertToModel(origGeo, Vector3d.ORIGIN);

        // convert the model position back to a geographic position which should
        // be the same as the original.
        GeographicPosition reGeo = earth.convertToPosition(model, ReferenceLevel.ELLIPSOID);

        Assert.assertTrue(MathUtil.isZero(latD - reGeo.getLatLonAlt().getLatD(), 0.00001));
        Assert.assertTrue(MathUtil.isZero(lonD - reGeo.getLatLonAlt().getLonD(), 0.00001));
        Assert.assertTrue(MathUtil.isZero(elevationM - reGeo.getLatLonAlt().getAltM(), 0.00001));
    }
}

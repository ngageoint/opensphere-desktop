package io.opensphere.core.viewer.impl;

import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Test;

import io.opensphere.core.MapManager;
import io.opensphere.core.math.Vector3d;
import io.opensphere.core.model.Altitude.ReferenceLevel;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.projection.impl.EquirectangularProjection;
import io.opensphere.core.viewer.impl.DynamicViewer.KMLCompatibleCamera;
import io.opensphere.core.viewer.impl.Viewer2D.ViewerPosition2D;

/**
 * Tests for {@link Viewer2D}.
 */
public class Viewer2DTest
{
    /** A builder for creating viewers. */
    private final AbstractDynamicViewer.Builder myBuilder;

    /** Map manager for testing. */
    private final MapManager myMapManager;

    /**
     * A projection used to convert between model and geographic coordinates.
     */
    private final EquirectangularProjection myProjection = new EquirectangularProjection();

    /** Constructor. */
    public Viewer2DTest()
    {
        myBuilder = new AbstractDynamicViewer.Builder().maxZoom(1000000.).minZoom(1.);
        myBuilder.modelWidth(myProjection.getModelWidth());
        myBuilder.modelHeight(myProjection.getModelHeight());

        myMapManager = EasyMock.createMock(MapManager.class);
        EasyMock.reset(myMapManager);
        EasyMock.expect(myMapManager.getProjection(Viewer2D.class)).andReturn(myProjection).anyTimes();
        EasyMock.replay(myMapManager);
    }

    /**
     * Test for {@link Viewer2D#getAltitude()}..
     */
    @Test
    public void testGetAltitude()
    {
        Viewer2D viewer = new Viewer2D(myBuilder);
        viewer.setMapContext(myMapManager);
        ViewerPosition2D pos = new ViewerPosition2D(Vector3d.ORIGIN, 2.);

        viewer.reshape(800, 400);
        viewer.setPosition(pos);
        double eightMagAltitude = 2.4160380260805476E7;
        double alt = viewer.getAltitude();
        Assert.assertEquals(eightMagAltitude, alt, 0.00001);

        // When the full width of the view port is visible in the window, making
        // the aspect wider will not affect the altitude, so this should give
        // the same value as the previous calculation.
        viewer.reshape(1000, 400);
        alt = viewer.getAltitude();
        Assert.assertEquals(eightMagAltitude, alt, 0.00001);
    }

    /**
     * Test for {@link Viewer2D#getAltitude()} when the aspect ration is more
     * narrow than the model aspect ration.
     */
    @Test
    public void testGetAltitudeReverseAspect()
    {
        Viewer2D viewer = new Viewer2D(myBuilder);
        viewer.setMapContext(myMapManager);
        ViewerPosition2D pos = new ViewerPosition2D(Vector3d.ORIGIN, 8.);

        viewer.reshape(1421, 985);
        viewer.setPosition(pos);
        double eightMagAltitude = 4356840.146015809;
        double alt = viewer.getAltitude();
        Assert.assertEquals(eightMagAltitude, alt, 0.00001);

        // When the full width of the view port is not visible in the window,
        // the altitude is adjusted to correct for the visible area. Making the
        // aspect more narrow will decrease the altitude.
        viewer.reshape(1172, 985);
        eightMagAltitude = 3593396.65807919;
        alt = viewer.getAltitude();
        Assert.assertEquals(eightMagAltitude, alt, 0.00001);
    }

    /**
     * Test for {@link Viewer2D#getViewerPosition(KMLCompatibleCamera)}. This
     * test ensures that when we use a viewer to get the altitude, when we
     * reverse the operation the original viewer position is produced.
     */
    @Test
    public void testViewerConversion()
    {
        Viewer2D viewer = new Viewer2D(myBuilder);
        viewer.setMapContext(myMapManager);

        // This aspect ratio is more narrow than the model's aspect ratio.
        ViewerPosition2D pos = new ViewerPosition2D(new Vector3d(.56, .32, 0.), 8.3);
        viewer.reshape(1421, 985);
        viewer.setPosition(pos);
        double alt = viewer.getAltitude();
        GeographicPosition geoLoc = myProjection.convertToPosition(pos.getLocation(), ReferenceLevel.ELLIPSOID);
        LatLonAlt lla = LatLonAlt.createFromDegreesMeters(geoLoc.getLatLonAlt().getLatD(), geoLoc.getLatLonAlt().getLonD(), alt,
                ReferenceLevel.ELLIPSOID);
        ViewerPosition2D calcPos = viewer.getViewerPosition(new KMLCompatibleCamera(lla, 0, 0, 0));
        Assert.assertEquals(viewer.getPosition(), calcPos);

        // The calculation is affected by the viewer's aspect ratio, so test
        // with the aspect ratio wider than the model's aspect ratio.
        pos = new ViewerPosition2D(new Vector3d(.3746, .284, 0.), 18.35839);
        viewer.reshape(1421, 564);
        viewer.setPosition(pos);
        alt = viewer.getAltitude();
        geoLoc = myProjection.convertToPosition(pos.getLocation(), ReferenceLevel.ELLIPSOID);
        lla = LatLonAlt.createFromDegreesMeters(geoLoc.getLatLonAlt().getLatD(), geoLoc.getLatLonAlt().getLonD(), alt,
                ReferenceLevel.ELLIPSOID);
        calcPos = viewer.getViewerPosition(new KMLCompatibleCamera(lla, 0, 0, 0));

        Assert.assertEquals(viewer.getPosition().getScale(), calcPos.getScale(), 0.000001);
    }
}

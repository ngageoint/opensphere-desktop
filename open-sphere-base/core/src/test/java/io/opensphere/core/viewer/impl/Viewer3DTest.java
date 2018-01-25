package io.opensphere.core.viewer.impl;

import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Test;

import io.opensphere.core.MapManager;
import io.opensphere.core.math.Vector3d;
import io.opensphere.core.model.Altitude.ReferenceLevel;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.projection.ProjectionChangeSupport;
import io.opensphere.core.projection.impl.Earth3D;
import io.opensphere.core.viewer.impl.DynamicViewer.KMLCompatibleCamera;
import io.opensphere.core.viewer.impl.Viewer3D.ViewerPosition3D;

/**
 * Tests for {@link Viewer3D}.
 */
public class Viewer3DTest
{
    /**
     * Allow for a reasonable amount of error as a result of converting the view
     * twice.
     */
    private static final double LARGE_EPSILON = 0.000001;

    /** A builder for creating viewers. */
    private final AbstractDynamicViewer.Builder myBuilder;

    /** Map manager for testing. */
    private final MapManager myMapManager;

    /**
     * A projection used to convert between model and geographic coordinates.
     */
    private final Earth3D myProjection = new Earth3D();

    /**
     * When the map context is set for the viewer it tries to register with the
     * projection for changes.
     */
    private final ProjectionChangeSupport myProjectionChangeSupport = new ProjectionChangeSupport();

    /** Constructor. */
    public Viewer3DTest()
    {
        myBuilder = new AbstractDynamicViewer.Builder().maxZoom(1000000.).minZoom(1.);
        myBuilder.modelWidth(myProjection.getModelWidth());
        myBuilder.modelHeight(myProjection.getModelHeight());

        myMapManager = EasyMock.createMock(MapManager.class);
        EasyMock.reset(myMapManager);
        EasyMock.expect(myMapManager.getProjection(Viewer3D.class)).andReturn(myProjection).anyTimes();
        EasyMock.expect(myMapManager.getProjectionChangeSupport()).andReturn(myProjectionChangeSupport).anyTimes();
        EasyMock.replay(myMapManager);
    }

    /**
     * Test for {@link Viewer3D#getViewerPosition(KMLCompatibleCamera)} and
     * {@link Viewer3D#getCamera(io.opensphere.core.viewer.Viewer.ViewerPosition)}
     * . This test ensures that getting the view position from a camera can be
     * reversed to the original camera.
     */
    @Test
    public void testCameraToViewerToCamera()
    {
        Viewer3D viewer = new Viewer3D(myBuilder);
        viewer.setMapContext(myMapManager);

        LatLonAlt lla = LatLonAlt.createFromDegreesMeters(0., 0., 10000., ReferenceLevel.ELLIPSOID);
        KMLCompatibleCamera camera = new KMLCompatibleCamera(lla, 20., 11., 15.);
        ViewerPosition3D calcPos = viewer.getViewerPosition(camera);
        KMLCompatibleCamera calcCamera = viewer.getCamera(calcPos);
        compareCameras(camera, calcCamera);

        lla = LatLonAlt.createFromDegreesMeters(10., -6., 10000., ReferenceLevel.ELLIPSOID);
        camera = new KMLCompatibleCamera(lla, 27., 111., 115.);
        calcPos = viewer.getViewerPosition(camera);
        calcCamera = viewer.getCamera(calcPos);
        compareCameras(camera, calcCamera);

        lla = LatLonAlt.createFromDegreesMeters(40., 20., 10000., ReferenceLevel.ELLIPSOID);
        camera = new KMLCompatibleCamera(lla, -27., 15., 17.);
        calcPos = viewer.getViewerPosition(camera);
        calcCamera = viewer.getCamera(calcPos);
        compareCameras(camera, calcCamera);
    }

    /**
     * Test for {@link Viewer3D#getViewerPosition(KMLCompatibleCamera)} and
     * {@link Viewer3D#getCamera(io.opensphere.core.viewer.Viewer.ViewerPosition)}
     * . This test ensures that getting the camera can be reversed to the
     * original viewer position.
     */
    @Test
    public void testViewerToCameraToViewer()
    {
        Viewer3D viewer = new Viewer3D(myBuilder);
        viewer.setMapContext(myMapManager);

        ViewerPosition3D pos = viewer.getPosition();
        KMLCompatibleCamera camera = viewer.getCamera(pos);
        ViewerPosition3D calcPos = viewer.getViewerPosition(camera);
        compareViewers(pos, calcPos);

        pos = new ViewerPosition3D(new Vector3d(2.3036037647855375E7, 0.0, -3394759.3353254735),
                new Vector3d(-0.9777670146175065, 0.0, 0.20969421815102315),
                new Vector3d(0.20969421815102315, 0.0, 0.9777670146175065));
        camera = viewer.getCamera(pos);
        calcPos = viewer.getViewerPosition(camera);
        compareViewers(pos, calcPos);
    }

    /**
     * Verify that the two cameras are the same.
     *
     * @param camera1 The first camera.
     * @param camera2 The second camera.
     */
    private void compareCameras(KMLCompatibleCamera camera1, KMLCompatibleCamera camera2)
    {
        Assert.assertEquals(camera1.getLocation().getLatD(), camera2.getLocation().getLatD(), LARGE_EPSILON);
        Assert.assertEquals(camera1.getLocation().getLonD(), camera2.getLocation().getLonD(), LARGE_EPSILON);
        Assert.assertEquals(camera1.getLocation().getAltM(), camera2.getLocation().getAltM(), LARGE_EPSILON);
        Assert.assertEquals(camera1.getHeading(), camera2.getHeading(), LARGE_EPSILON);
        Assert.assertEquals(camera1.getRoll(), camera2.getRoll(), LARGE_EPSILON);
        Assert.assertEquals(camera1.getTilt(), camera2.getTilt(), LARGE_EPSILON);
    }

    /**
     * Verify that the two viewer positions are the same.
     *
     * @param pos1 The first viewer position.
     * @param pos2 The second viewer position.
     */
    private void compareViewers(ViewerPosition3D pos1, ViewerPosition3D pos2)
    {
        Assert.assertTrue(pos1.getLocation().equals(pos2.getLocation(), 0.01));
        Assert.assertTrue(pos1.getDir().equals(pos2.getDir(), LARGE_EPSILON));
        Assert.assertTrue(pos1.getUp().equals(pos2.getUp(), LARGE_EPSILON));
    }
}

package io.opensphere.terrainprofile;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

import io.opensphere.core.MapManager;
import io.opensphere.core.hud.framework.TransformerHelper;
import io.opensphere.core.hud.framework.Window.ResizeOption;
import io.opensphere.core.hud.framework.Window.ToolLocation;
import io.opensphere.core.math.Plane;
import io.opensphere.core.math.Ray3d;
import io.opensphere.core.math.Vector2i;
import io.opensphere.core.math.Vector3d;
import io.opensphere.core.math.WGS84EarthConstants;
import io.opensphere.core.model.Altitude.ReferenceLevel;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.ScreenBoundingBox;
import io.opensphere.core.model.ScreenPosition;
import io.opensphere.core.units.UnitsProvider;
import io.opensphere.core.units.length.Length;
import io.opensphere.core.util.concurrent.ProcrastinatingExecutor;
import io.opensphere.core.viewer.ViewChangeSupport;
import io.opensphere.core.viewer.ViewChangeSupport.ViewChangeListener;
import io.opensphere.core.viewer.Viewer;
import io.opensphere.core.viewer.impl.DynamicViewer;
import io.opensphere.core.viewer.impl.Viewer3D;
import io.opensphere.core.viewer.impl.Viewer3D.ViewerPosition3D;

/** Manager the items which will be rendered for the terrain profile. */
public class TerrainProfileManager
{
    /** Helper class for handling publishing of geometries. */
    private final TransformerHelper myHelper;

    /** Listen to events from the main viewer. */
    private final ViewChangeListener myMainViewListener = new ViewChangeListener()
    {
        @Override
        public void viewChanged(Viewer viewer, ViewChangeSupport.ViewChangeType type)
        {
            updateView();
        }
    };

    /** Location of the end points of the profile in geographic coordinates. */
    private final List<GeographicPosition> myProfileGeoEnds = new ArrayList<>(2);

    /** Location of the end points of the profile in model coordinates. */
    private final List<Vector3d> myProfileModelEnds = new ArrayList<>(2);

    /** The terrain profile chart. */
    private final TerrainProfile myTerrainProfile;

    /** Helper class which draws the profile indicator along the terrain. */
    private final TerrainProfileMarker myTerrainProfileMarker;

    /** Executor to handle view changes. */
    private final ProcrastinatingExecutor myViewChangeExecutor;

    /**
     * Constructor.
     *
     * @param helper Transformer helper for access to the toolbox and publishing
     *            geometries.
     * @param executor Executor shared by HUD components.
     * @param unitsProvider The length units provider.
     */
    public TerrainProfileManager(TransformerHelper helper, ScheduledExecutorService executor, UnitsProvider<Length> unitsProvider)
    {
        myHelper = helper;
        myTerrainProfileMarker = new TerrainProfileMarker(helper);
        myViewChangeExecutor = new ProcrastinatingExecutor(executor);

        ScreenPosition terrainUpLeft = new ScreenPosition(100, 700);
        ScreenPosition terrainLowRight = new ScreenPosition(350, 875);
        ScreenBoundingBox terrainProfileLocation = new ScreenBoundingBox(terrainUpLeft, terrainLowRight);
        myTerrainProfile = new TerrainProfile(myHelper, terrainProfileLocation, ToolLocation.SOUTHWEST,
                ResizeOption.RESIZE_KEEP_FIXED_SIZE, unitsProvider);
        myTerrainProfile.init();
        myTerrainProfile.moveToDefaultLocation();
        myTerrainProfile.display();

        myTerrainProfile.addMapListener(myTerrainProfileMarker);
    }

    /** Perform any required cleanup. */
    public void close()
    {
        myHelper.getToolbox().getMapManager().getViewChangeSupport().removeViewChangeListener(myMainViewListener);
        myTerrainProfileMarker.close();
        if (myTerrainProfile != null)
        {
            myTerrainProfile.closeWindow();
        }
    }

    /** Initialize the manager. */
    public void open()
    {
        myHelper.getToolbox().getMapManager().getViewChangeSupport().addViewChangeListener(myMainViewListener);
        updateView();
    }

    /**
     * Find the intersection of the given planes which lies within the view
     * port, if any, and add it to the list.
     *
     * @param points List of points to add the result.
     * @param ellipsePlane plane along which the ellipse lies.
     * @param clip clip plane with which to intersect.
     * @param view3d Viewer which defines the view port.
     */
    private void addIntersection(List<Vector3d> points, Plane ellipsePlane, Plane clip, Viewer3D view3d)
    {
        // Find the intersection of the ellipse plane with the left clip
        // plane.
        Ray3d ray = clip.getIntersection(ellipsePlane);
        Ray3d reverse = new Ray3d(ray.getPosition(), ray.getDirection().multiply(-1));

        // intersect the line with the model.
        Vector3d candidate1 = view3d.getModel().getIntersection(ray);
        Vector3d candidate2 = view3d.getModel().getIntersection(reverse);

        if (candidate1 != null)
        {
            Vector3d location = view3d.getPosition().getLocation();
            double dist1 = location.distance(candidate1);
            double dist2 = location.distance(candidate2);
            if (dist1 < dist2)
            {
                points.add(candidate1);
            }
            else
            {
                points.add(candidate2);
            }
        }
    }

    /** Find the ends of the profile based on the current viewer position. */
    private synchronized void getProfileEnds()
    {
        myProfileModelEnds.clear();
        MapManager mapMan = myHelper.getToolbox().getMapManager();
        DynamicViewer view = mapMan.getStandardViewer();

        if (view instanceof Viewer3D)
        {
            Viewer3D view3d = (Viewer3D)view;

            // Get the Intersection of the viewer with the earth
            Vector3d intersect = view3d.getModelIntersection();
            ViewerPosition3D position = view3d.getPosition();
            Vector3d location = position.getLocation();
            Vector3d right = position.getRight();

            Vector3d normal = intersect.getNormalized().cross(right);

            Plane ellipsePlane = new Plane(intersect, normal);
            List<Vector3d> endPoints = new ArrayList<>(2);

            addIntersection(endPoints, ellipsePlane, view3d.getLeftClip(), view3d);
            addIntersection(endPoints, ellipsePlane, view3d.getRightClip(), view3d);
            addIntersection(endPoints, ellipsePlane, view3d.getTopClip(), view3d);
            addIntersection(endPoints, ellipsePlane, view3d.getBottomClip(), view3d);

            // TODO change this to use the horizon point any time it is in view.

            if (endPoints.size() < 2)
            {
                // TODO if the earth is off-center from the view (tilted off
                // to left or right). Some special handling will be
                // required.

                double viewDistance = location.getLength();
                Vector3d adjustedViewDir = intersect.getNormalized().multiply(-1d);
                Vector3d adjustedViewLoc = adjustedViewDir.multiply(-viewDistance);

                // get the square up vector (right hasn't changed)
                Vector3d adjustedUp = adjustedViewDir.cross(right).getNormalized();

                // rotate the adjusted direction around the up vector.
                double sinAngle = (WGS84EarthConstants.RADIUS_POLAR_M - 20) / viewDistance;
                double angle = Math.asin(sinAngle);

                // left
                Ray3d ray1 = new Ray3d(adjustedViewLoc, adjustedViewDir.rotate(adjustedUp, -angle));
                myProfileModelEnds.add(view3d.getModel().getIntersection(ray1));

                // right
                Ray3d ray2 = new Ray3d(adjustedViewLoc, adjustedViewDir.rotate(adjustedUp, angle));
                myProfileModelEnds.add(view3d.getModel().getIntersection(ray2));
            }
            else
            {
                myProfileModelEnds.add(endPoints.get(0));
                myProfileModelEnds.add(endPoints.get(1));
            }
        }
        else
        {
            // In 2-d we can always use the viewport to find profile
            // (assuming we can't rotate 2-d view)
            Vector2i leftCenter = new Vector2i(0, view.getViewportHeight() / 2);
            Vector2i rightCenter = new Vector2i(view.getViewportWidth(), view.getViewportHeight() / 2);
            myProfileModelEnds.add(view.windowToModelCoords(leftCenter));
            myProfileModelEnds.add(view.windowToModelCoords(rightCenter));
        }

        myProfileGeoEnds.clear();
        for (Vector3d model : myProfileModelEnds)
        {
            GeographicPosition geo = mapMan.getProjection().convertToPosition(model, ReferenceLevel.ELLIPSOID);
            myProfileGeoEnds.add(geo);
        }
    }

    /** Draw everything. */
    private void updateView()
    {
        myViewChangeExecutor.execute(new Runnable()
        {
            @Override
            public void run()
            {
                getProfileEnds();
                myTerrainProfileMarker.drawMapProfile(myProfileGeoEnds);
                myTerrainProfile.drawProfileChard(myProfileModelEnds);
            }
        });
    }
}

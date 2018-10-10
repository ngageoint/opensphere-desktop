package io.opensphere.core.hud.widget;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.geometry.PolygonMeshGeometry;
import io.opensphere.core.geometry.renderproperties.DefaultPolygonMeshRenderProperties;
import io.opensphere.core.geometry.renderproperties.LightingModelConfigGL;
import io.opensphere.core.geometry.renderproperties.PolygonMeshRenderProperties;
import io.opensphere.core.hud.framework.Component;
import io.opensphere.core.hud.framework.ControlEventSupport;
import io.opensphere.core.hud.framework.Renderable;
import io.opensphere.core.hud.widget.Slider.Orientation;
import io.opensphere.core.math.Vector3d;
import io.opensphere.core.model.ScreenBoundingBox;
import io.opensphere.core.model.ScreenPosition;
import io.opensphere.core.util.MathUtil;

/** Puck which shows the location of a slider. */
public class SliderPuck extends Renderable
{
    /** Radius of the rounded edge. */
    private static final double ourEdgeRadius = 3d;

    /** Number of segments in the mesh. */
    private static final double ourEdgeSegments = 20d;

    /** Warp factor for the mesh. */
    private static final double ourWarpFactor = 0.9d;

    /** The percentage along the slider at which the puck is located. */
    private double myLocation;

    /** Support class for events from the control context. */
    private final ControlEventSupport myMouseSupport;

    /** The geometry drawn to show the puck. */
    private Geometry myPuckGeometry;

    /**
     * Construct a RadioButton.
     *
     * @param parent parent component.
     */
    public SliderPuck(Component parent)
    {
        super(parent);
        if (!(parent instanceof Slider))
        {
            throw new IllegalArgumentException("A SliderBackground's parent must be a Slider.");
        }
        setBaseColor(ClassicHUDPalette.ourSliderPuckDisarmColor);
        myMouseSupport = new ControlEventSupport(this, getTransformer().getToolbox().getControlRegistry());
    }

    @Override
    public void handleCleanupListeners()
    {
        myMouseSupport.cleanupListeners();
    }

    @Override
    public void init()
    {
        ScreenBoundingBox puckBox = getPuckBounds();
        if (getOrientation() == Orientation.HORIZONTAL)
        {
            setMesh(puckBox, getPuckSize(), getThickness());
        }
        else
        {
            setMesh(puckBox, getThickness(), getPuckSize());
        }
    }

    @Override
    public void mouseDragged(Geometry geom, Point dragStart, MouseEvent evt)
    {
        ScreenBoundingBox absBbox = getAbsoluteLocation();
        ScreenBoundingBox bbox = getDrawBounds();

        // The amount of distance which the puck can travel is the bounds of the
        // box in the correct direction less the puck size.
        if (getOrientation() == Orientation.HORIZONTAL)
        {
            if (absBbox.getUpperLeft().getX() > dragStart.x || absBbox.getLowerRight().getX() < dragStart.x)
            {
                return;
            }
            double travelLength = bbox.getLowerRight().getX() - bbox.getUpperLeft().getX() - getPuckSize();
            double delta = (evt.getPoint().x - dragStart.x) / travelLength;
            myLocation += delta;
        }
        else
        {
            if (absBbox.getUpperLeft().getY() > dragStart.y || absBbox.getLowerRight().getY() < dragStart.y)
            {
                return;
            }
            double travelLength = bbox.getLowerRight().getY() - bbox.getUpperLeft().getY() - getPuckSize();
            double delta = (evt.getPoint().y - dragStart.y) / travelLength;
            myLocation += delta;
        }
        myLocation = Math.max(0d, myLocation);
        myLocation = Math.min(1d, myLocation);

        redrawPuck();
    }

    @Override
    public void mousePressed(Geometry geom, MouseEvent event)
    {
        setBaseColor(ClassicHUDPalette.ourSliderPuckArmColor);
        redrawPuck();
    }

    @Override
    public void mouseReleased(Geometry geom, MouseEvent event)
    {
        setBaseColor(ClassicHUDPalette.ourSliderPuckDisarmColor);
        redrawPuck();
    }

    /**
     * Set up the third set of positions and normals.
     *
     * @param puckBox bounding box for the puck.
     * @param positions Vertices of the mesh.
     * @param normals Normals of the mesh.
     */
    private void doHalfPiToPi(ScreenBoundingBox puckBox, List<ScreenPosition> positions, List<Vector3d> normals)
    {
        double upper = puckBox.getUpperLeft().getY();
        double left = puckBox.getUpperLeft().getX();
        double lower = puckBox.getLowerRight().getY();
        double segmentAngle = Math.PI / ourEdgeSegments;

        for (double angle = MathUtil.HALF_PI; angle <= Math.PI; angle += segmentAngle)
        {
            double cos = Math.cos(angle);
            double sin = Math.sin(angle);
            double x = left + ourEdgeRadius + ourEdgeRadius * cos;
            double y = upper + ourEdgeRadius - ourEdgeRadius * sin;
            normals.add(new Vector3d(cos * ourWarpFactor, sin * ourWarpFactor, 1d));
            positions.add(new ScreenPosition(x, y));
        }
        normals.add(new Vector3d(-ourWarpFactor, 0f, 1f));
        positions.add(new ScreenPosition(left, upper + ourEdgeRadius));
        normals.add(new Vector3d(-ourWarpFactor, 0f, 1f));
        positions.add(new ScreenPosition(left, lower - ourEdgeRadius));
    }

    /**
     * Set up the second set of positions and normals.
     *
     * @param puckBox bounding box for the puck.
     * @param positions Vertices of the mesh.
     * @param positions2 Some reusable vertices in the mesh.
     * @param normals Normals of the mesh.
     * @param normals2 Some reusable normals in the mesh.
     */
    private void doThreePiHalfToTwoPi(ScreenBoundingBox puckBox, List<ScreenPosition> positions, List<ScreenPosition> positions2,
            List<Vector3d> normals, List<Vector3d> normals2)
    {
        double upper = puckBox.getUpperLeft().getY();
        double lower = puckBox.getLowerRight().getY();
        double right = puckBox.getLowerRight().getX();
        double segmentAngle = Math.PI / ourEdgeSegments;

        for (double angle = 3 * MathUtil.HALF_PI; angle <= MathUtil.TWO_PI; angle += segmentAngle)
        {
            double cos = Math.cos(angle);
            double sin = Math.sin(angle);
            double x = right - ourEdgeRadius + ourEdgeRadius * cos;
            double y = lower - ourEdgeRadius - ourEdgeRadius * sin;
            normals.add(new Vector3d(cos * ourWarpFactor, sin * ourWarpFactor, 1d));
            positions.add(new ScreenPosition(x, y));
        }
        normals.add(new Vector3d(ourWarpFactor, 0d, 1d));
        positions.add(new ScreenPosition(right, lower - ourEdgeRadius));
        normals.add(new Vector3d(ourWarpFactor, 0d, 1d));
        positions.add(new ScreenPosition(right, upper + ourEdgeRadius));

        positions.addAll(positions2);
        normals.addAll(normals2);
    }

    /**
     * Set up the first set of positions and normals.
     *
     * @param puckBox bounding box for the puck.
     * @param positions Vertices of the mesh.
     * @param positions2 Some reusable vertices in the mesh.
     * @param normals Normals of the mesh.
     * @param normals2 Some reusable normals in the mesh.
     */
    private void doZeroToHalfPI(ScreenBoundingBox puckBox, List<ScreenPosition> positions, List<ScreenPosition> positions2,
            List<Vector3d> normals, List<Vector3d> normals2)
    {
        double upper = puckBox.getUpperLeft().getY();
        double left = puckBox.getUpperLeft().getX();
        double lower = puckBox.getLowerRight().getY();
        double right = puckBox.getLowerRight().getX();

        normals.add(new Vector3d(-ourWarpFactor, 0d, 1d));
        positions.add(new ScreenPosition(left, lower - ourEdgeRadius));
        double segmentAngle = Math.PI / ourEdgeSegments;

        for (double angle = 0; angle <= MathUtil.HALF_PI; angle += segmentAngle)
        {
            double cos = Math.cos(angle);
            double sin = Math.sin(angle);
            double x = left + ourEdgeRadius - ourEdgeRadius * cos;
            double y = upper - ourEdgeRadius + ourEdgeRadius * sin;
            normals.add(new Vector3d(-cos * ourWarpFactor, sin * ourWarpFactor, 1d));
            positions.add(new ScreenPosition(x, y));

            x = right - ourEdgeRadius + ourEdgeRadius * cos;
            y = upper + ourEdgeRadius - ourEdgeRadius * sin;
            normals2.add(new Vector3d(cos * ourWarpFactor, sin * ourWarpFactor, 1d));
            positions2.add(new ScreenPosition(x, y));
        }
    }

    /**
     * Get the orientation of the slider which owns me.
     *
     * @return the orientation of the slider which owns me.
     */
    private Orientation getOrientation()
    {
        return ((Slider)getParent()).getOrientation();
    }

    /**
     * Get the bounding box for the puck.
     *
     * @return bounding box for the puck.
     */
    private ScreenBoundingBox getPuckBounds()
    {
        ScreenBoundingBox bbox = getDrawBounds();
        double left = bbox.getUpperLeft().getX();
        double right = bbox.getLowerRight().getX();
        double upper = bbox.getUpperLeft().getY();
        double lower = bbox.getLowerRight().getY();

        double thickness = getThickness();
        if (getOrientation() == Orientation.HORIZONTAL)
        {
            upper += (lower - upper - thickness) * getVerticalAlignment();
            lower = upper + thickness;
            double travelLength = right - left - getPuckSize();
            double midPoint = travelLength * myLocation;
            left += midPoint;
            right = left + getPuckSize();
        }
        else
        {
            left += (right - left - thickness) * getHorizontalAlignment();
            right = left + thickness;
            double travelLength = lower - upper - getPuckSize();
            double midPoint = travelLength * myLocation;
            upper += midPoint;
            lower = upper + getPuckSize();
        }

        return new ScreenBoundingBox(new ScreenPosition(left, upper), new ScreenPosition(right, lower));
    }

    /**
     * Get the puck size from the slider which owns me.
     *
     * @return the puck size from the slider which owns me.
     */
    private double getPuckSize()
    {
        return ((Slider)getParent()).getPuckSize();
    }

    /**
     * Get the thickness of the slider which owns me.
     *
     * @return the thickness of the slider which owns me.
     */
    private double getThickness()
    {
        return ((Slider)getParent()).getThickness();
    }

    /**
     * Redraw the puck.
     */
    private void redrawPuck()
    {
        Set<Geometry> oldGeoms = Collections.singleton(myPuckGeometry);
        getGeometries().removeAll(oldGeoms);

        ScreenBoundingBox puckBox = getPuckBounds();
        if (getOrientation() == Orientation.HORIZONTAL)
        {
            setMesh(puckBox, getPuckSize(), getThickness());
        }
        else
        {
            setMesh(puckBox, getThickness(), getPuckSize());
        }
        Set<Geometry> newGeoms = Collections.singleton(myPuckGeometry);

        updateGeometries(newGeoms, oldGeoms);
    }

    /**
     * Generate the mesh for the puck.
     *
     * @param puckBox bounding box for the puck.
     * @param width width of the puck.
     * @param height height of the puck.
     */
    private void setMesh(ScreenBoundingBox puckBox, double width, double height)
    {
        PolygonMeshGeometry.Builder<ScreenPosition> polyBuilder = new PolygonMeshGeometry.Builder<>();

        List<ScreenPosition> positions = new ArrayList<>();
        List<ScreenPosition> positions2 = new ArrayList<>();
        List<Vector3d> normals = new ArrayList<>();
        List<Vector3d> normals2 = new ArrayList<>();

        doZeroToHalfPI(puckBox, positions, positions2, normals, normals2);

        doThreePiHalfToTwoPi(puckBox, positions, positions2, normals, normals2);

        doHalfPiToPi(puckBox, positions, normals);

        polyBuilder.setPositions(positions);
        polyBuilder.setNormals(normals);
        polyBuilder.setPolygonVertexCount(positions.size());
        PolygonMeshRenderProperties props = new DefaultPolygonMeshRenderProperties(getBaseZOrder() + 4, true, true, true);
        props.setColor(getBaseColor());
        props.setLighting(LightingModelConfigGL.getDefaultLight());

        PolygonMeshGeometry mesh = new PolygonMeshGeometry(polyBuilder, props, null);
        myPuckGeometry = mesh;
        myMouseSupport.setActionGeometry(myPuckGeometry);
        getGeometries().add(mesh);
    }
}

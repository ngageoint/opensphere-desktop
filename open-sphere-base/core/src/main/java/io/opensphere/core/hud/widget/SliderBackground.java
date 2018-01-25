package io.opensphere.core.hud.widget;

import java.util.ArrayList;
import java.util.List;

import io.opensphere.core.geometry.PolygonMeshGeometry;
import io.opensphere.core.geometry.PolylineGeometry;
import io.opensphere.core.geometry.renderproperties.DefaultPolygonMeshRenderProperties;
import io.opensphere.core.geometry.renderproperties.DefaultPolylineRenderProperties;
import io.opensphere.core.geometry.renderproperties.LightingModelConfigGL;
import io.opensphere.core.geometry.renderproperties.PolygonMeshRenderProperties;
import io.opensphere.core.geometry.renderproperties.PolylineRenderProperties;
import io.opensphere.core.hud.framework.Component;
import io.opensphere.core.hud.framework.Renderable;
import io.opensphere.core.hud.widget.Slider.Orientation;
import io.opensphere.core.math.Vector3d;
import io.opensphere.core.model.ScreenBoundingBox;
import io.opensphere.core.model.ScreenPosition;

/** Background for a slider. */
public class SliderBackground extends Renderable
{
    /** Warp factor for the background. */
    private static final double ourWarpFactor = 0.8d;

    /**
     * Construct a RadioButton.
     *
     * @param parent parent component.
     */
    public SliderBackground(Component parent)
    {
        super(parent);
        if (!(parent instanceof Slider))
        {
            throw new IllegalArgumentException("A SliderBackground's parent must be a Slider.");
        }
        setBaseColor(ClassicHUDPalette.ourSliderBackgroundColor);
    }

    @Override
    public void init()
    {
        ScreenBoundingBox bbox = getDrawBounds();
        double left = bbox.getUpperLeft().getX();
        double right = bbox.getLowerRight().getX();
        double upper = bbox.getUpperLeft().getY();
        double lower = bbox.getLowerRight().getY();

        double thickness = ((Slider)getParent()).getThickness();
        if (((Slider)getParent()).getOrientation() == Orientation.HORIZONTAL)
        {
            upper += (lower - upper - thickness) * getVerticalAlignment();
            lower = upper + thickness;
        }
        else
        {
            left += (right - left - thickness) * getHorizontalAlignment();
            right = left + thickness;
        }

        setBorder(left, right, upper, lower);
        setMesh(left, right, upper, lower);
        setTrack(left, right, upper, lower);
    }

    /**
     * Place a line border around the outside.
     *
     * @param left left most edge of the drawing box.
     * @param right right most edge of the drawing box.
     * @param upper top most edge of the drawing box.
     * @param lower bottom most edge of the drawing box.
     */
    private void setBorder(double left, double right, double upper, double lower)
    {
        // TODO this is an exact match for how we build the BoxToggleBackground.
        // Maybe this be put in a utility class.
        PolylineGeometry.Builder<ScreenPosition> lineBldr = new PolylineGeometry.Builder<ScreenPosition>();
        PolylineRenderProperties props = new DefaultPolylineRenderProperties(getBaseZOrder() + 2, true, false);
        props.setColor(getBaseColor());
        lineBldr.setLineSmoothing(false);

        List<ScreenPosition> positions = new ArrayList<>();
        ScreenPosition lowerLeft = new ScreenPosition(left, lower);
        positions.add(lowerLeft);
        positions.add(new ScreenPosition(right, lower));
        positions.add(new ScreenPosition(right, upper));
        positions.add(new ScreenPosition(left, upper));
        positions.add(lowerLeft);

        lineBldr.setVertices(positions);
        PolylineGeometry line = new PolylineGeometry(lineBldr, props, null);
        getGeometries().add(line);
    }

    /**
     * Place a line border around the outside.
     *
     * @param left left most edge of the drawing box.
     * @param right right most edge of the drawing box.
     * @param upper top most edge of the drawing box.
     * @param lower bottom most edge of the drawing box.
     */
    private void setMesh(double left, double right, double upper, double lower)
    {
        PolygonMeshGeometry.Builder<ScreenPosition> polyBuilder = new PolygonMeshGeometry.Builder<ScreenPosition>();
        polyBuilder.setPolygonVertexCount(8);

        List<ScreenPosition> positions = new ArrayList<>();
        List<Vector3d> normals = new ArrayList<>();

        normals.add(new Vector3d(ourWarpFactor, ourWarpFactor, 1d));
        normals.add(new Vector3d(0d, ourWarpFactor, 1d));
        normals.add(new Vector3d(-ourWarpFactor, ourWarpFactor, 1d));
        normals.add(new Vector3d(-ourWarpFactor, 0d, 1d));
        normals.add(new Vector3d(-ourWarpFactor, -ourWarpFactor, 1d));
        normals.add(new Vector3d(0d, -ourWarpFactor, 1d));
        normals.add(new Vector3d(ourWarpFactor, -ourWarpFactor, 1d));
        normals.add(new Vector3d(ourWarpFactor, 0d, 1d));

        double centerX = (left + right) / 2d;
        double centerY = (lower + upper) / 2d;
        positions.add(new ScreenPosition(left, lower));
        positions.add(new ScreenPosition(centerX, lower));
        positions.add(new ScreenPosition(right, lower));
        positions.add(new ScreenPosition(right, centerY));
        positions.add(new ScreenPosition(right, upper));
        positions.add(new ScreenPosition(centerX, upper));
        positions.add(new ScreenPosition(left, upper));
        positions.add(new ScreenPosition(left, centerY));

        polyBuilder.setPositions(positions);
        polyBuilder.setNormals(normals);
        PolygonMeshRenderProperties props = new DefaultPolygonMeshRenderProperties(getBaseZOrder() + 1, true, true, true);
        props.setColor(getBaseColor());
        props.setLighting(LightingModelConfigGL.getDefaultLight());

        PolygonMeshGeometry mesh = new PolygonMeshGeometry(polyBuilder, props, null);
        getGeometries().add(mesh);
    }

    /**
     * Place a line border around the outside.
     *
     * @param left left most edge of the drawing box.
     * @param right right most edge of the drawing box.
     * @param upper top most edge of the drawing box.
     * @param lower bottom most edge of the drawing box.
     */
    private void setTrack(double left, double right, double upper, double lower)
    {
        double centerX = (left + right) / 2d;
        double centerY = (lower + upper) / 2d;

        PolylineGeometry.Builder<ScreenPosition> lineBldr = new PolylineGeometry.Builder<ScreenPosition>();
        PolylineRenderProperties props = new DefaultPolylineRenderProperties(getBaseZOrder() + 2, true, false);
        props.setColor(ClassicHUDPalette.ourSliderTrackColor);
        props.setWidth(2f);
        lineBldr.setLineSmoothing(false);

        List<ScreenPosition> positions = new ArrayList<>();
        if (((Slider)getParent()).getOrientation() == Orientation.HORIZONTAL)
        {
            positions.add(new ScreenPosition(left + 1, centerY));
            positions.add(new ScreenPosition(right - 1, centerY));
        }
        else
        {
            positions.add(new ScreenPosition(centerX, upper + 1));
            positions.add(new ScreenPosition(centerX, lower - 1));
        }

        lineBldr.setVertices(positions);
        PolylineGeometry line = new PolylineGeometry(lineBldr, props, null);
        getGeometries().add(line);
    }
}

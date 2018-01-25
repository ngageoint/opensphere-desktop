package io.opensphere.core.hud.widget.buttons;

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
import io.opensphere.core.hud.framework.ControlEventSupport;
import io.opensphere.core.hud.framework.Renderable;
import io.opensphere.core.hud.widget.ClassicHUDPalette;
import io.opensphere.core.math.Vector3d;
import io.opensphere.core.model.ScreenBoundingBox;
import io.opensphere.core.model.ScreenPosition;

/** Square check box with the classic hud look and feel. */
public class BoxToggleBackground extends Renderable
{
    /** Height in which I render. */
    private static final int ourHeight = 12;

    /** Warp for box polymesh background. */
    private static final double ourWarpFactor = 0.9d;

    /** Width in which I render. */
    private static final int ourWidth = 12;

    /** Support class for events from the control context. */
    private final ControlEventSupport myMouseSupport;

    /**
     * Constructor.
     *
     * @param parent parent component.
     */
    public BoxToggleBackground(Component parent)
    {
        super(parent);
        setBaseColor(ClassicHUDPalette.ourCheckBoxBorderColor);
        myMouseSupport = new ControlEventSupport(this, getTransformer().getToolbox().getControlRegistry());
    }

    /** Add the background border. */
    public void addBorder()
    {
        ScreenBoundingBox bbox = getDrawBounds();
        ScreenPosition center = new ScreenPosition(Math.round(bbox.getCenter().getX()), Math.round(bbox.getCenter().getY()));
        double left = center.getX() - ourWidth / 2;
        double right = left + ourWidth;
        double upper = center.getY() - ourHeight / 2;
        double lower = upper + ourHeight;

        List<ScreenPosition> poss = new ArrayList<>();
        ScreenPosition lowerLeft = new ScreenPosition(left, lower);
        poss.add(lowerLeft);
        poss.add(new ScreenPosition(right, lower));
        poss.add(new ScreenPosition(right, upper));
        poss.add(new ScreenPosition(left, upper));
        poss.add(lowerLeft);

        PolylineGeometry.Builder<ScreenPosition> bitter = new PolylineGeometry.Builder<ScreenPosition>();
        PolylineRenderProperties props = new DefaultPolylineRenderProperties(getBaseZOrder() + 2, true, false);
        props.setColor(getBaseColor());
        bitter.setLineSmoothing(false);

        bitter.setVertices(poss);
        PolylineGeometry line = new PolylineGeometry(bitter, props, null);
        getGeometries().add(line);
    }

    @Override
    public double getDrawHeight()
    {
        return ourHeight;
    }

    @Override
    public double getDrawWidth()
    {
        return ourWidth;
    }

    @Override
    public void handleCleanupListeners()
    {
        myMouseSupport.cleanupListeners();
    }

    @Override
    public void init()
    {
        super.init();
        addBorder();
        addMesh();
    }

    /** Add the mesh geometry for the background. */
    private void addMesh()
    {
        ScreenBoundingBox bbox = getDrawBounds();
        ScreenPosition center = new ScreenPosition(Math.round(bbox.getCenter().getX()), Math.round(bbox.getCenter().getY()));
        double left = center.getX() - ourWidth / 2;
        double right = left + ourWidth;
        double upper = center.getY() - ourHeight / 2;
        double lower = upper + ourHeight;

        PolygonMeshGeometry.Builder<ScreenPosition> polyBuilder = new PolygonMeshGeometry.Builder<ScreenPosition>();
        polyBuilder.setPolygonVertexCount(8);

        List<ScreenPosition> poss = new ArrayList<>();
        List<Vector3d> norms = new ArrayList<>();

        poss.add(new ScreenPosition(left, lower));
        poss.add(new ScreenPosition(center.getX(), lower));
        poss.add(new ScreenPosition(right, lower));
        poss.add(new ScreenPosition(right, center.getY()));
        poss.add(new ScreenPosition(right, upper));
        poss.add(new ScreenPosition(center.getX(), upper));
        poss.add(new ScreenPosition(left, upper));
        poss.add(new ScreenPosition(left, center.getY()));

        norms.add(new Vector3d(ourWarpFactor, ourWarpFactor, 1d));
        norms.add(new Vector3d(0d, ourWarpFactor, 1d));
        norms.add(new Vector3d(-ourWarpFactor, ourWarpFactor, 1d));
        norms.add(new Vector3d(-ourWarpFactor, 0, 1d));
        norms.add(new Vector3d(-ourWarpFactor, -ourWarpFactor, 1d));
        norms.add(new Vector3d(0d, -ourWarpFactor, 1d));
        norms.add(new Vector3d(ourWarpFactor, -ourWarpFactor, 1d));
        norms.add(new Vector3d(ourWarpFactor, 0, 1d));

        polyBuilder.setPositions(poss);
        polyBuilder.setNormals(norms);
        PolygonMeshRenderProperties props = new DefaultPolygonMeshRenderProperties(getBaseZOrder() + 1, true, true, true);
        props.setColor(getBaseColor());
        props.setLighting(LightingModelConfigGL.getDefaultLight());

        PolygonMeshGeometry mesh = new PolygonMeshGeometry(polyBuilder, props, null);
        myMouseSupport.setActionGeometry(mesh);
        getGeometries().add(mesh);
    }
}

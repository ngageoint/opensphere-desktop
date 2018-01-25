package io.opensphere.core.hud.widget.buttons;

import java.awt.Color;

import io.opensphere.core.geometry.CircularMeshGeometry;
import io.opensphere.core.geometry.EllipseGeometry;
import io.opensphere.core.geometry.renderproperties.DefaultPolygonRenderProperties;
import io.opensphere.core.geometry.renderproperties.DefaultPolylineRenderProperties;
import io.opensphere.core.geometry.renderproperties.LightingModelConfigGL;
import io.opensphere.core.geometry.renderproperties.PolygonRenderProperties;
import io.opensphere.core.geometry.renderproperties.PolylineRenderProperties;
import io.opensphere.core.hud.framework.Component;
import io.opensphere.core.hud.framework.ControlEventSupport;
import io.opensphere.core.hud.framework.Renderable;
import io.opensphere.core.hud.widget.ClassicHUDPalette;
import io.opensphere.core.model.ScreenBoundingBox;
import io.opensphere.core.model.ScreenPosition;

/** Square check box with the classic hud look and feel. */
public class RadioToggleBackground extends Renderable
{
    /** Radius for the circle. */
    private static final int ourCircleRadius = 7;

    /** Warp for box polymesh background. */
    private static final double ourWarpFactor = 0.33d;

    /** Support class for events from the control context. */
    private final ControlEventSupport myMouseSupport = new ControlEventSupport(this,
            getTransformer().getToolbox().getControlRegistry());

    /**
     * Constructor.
     *
     * @param parent parent component.
     */
    public RadioToggleBackground(Component parent)
    {
        super(parent);
        setBaseColor(ClassicHUDPalette.ourRadioButtonBackgroundColor);
    }

    @Override
    public double getDrawHeight()
    {
        return 12;
    }

    @Override
    public double getDrawWidth()
    {
        return 12;
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

        ScreenBoundingBox bbox = getDrawBounds();
        // It is important to make sure that these circles look uniform.
        // Making the center start at whole values ensures that rounding
        // occurs in the same manner for like positions in different
        // circles.
        ScreenPosition center = new ScreenPosition(Math.round(bbox.getCenter().getX()), Math.round(bbox.getCenter().getY()));
        final int vertexCount = Math.min(37, (ourCircleRadius + 2) * 2);

        CircularMeshGeometry.Builder<ScreenPosition> circBldr = new CircularMeshGeometry.Builder<ScreenPosition>();
        circBldr.setCenter(center);
        circBldr.setWarpFactor(ourWarpFactor);
        circBldr.setNumVertices(vertexCount);
        circBldr.setRadius(ourCircleRadius);
        PolylineRenderProperties props = new DefaultPolylineRenderProperties(getBaseZOrder() + 1, true, true);
        props.setColor(getBaseColor());
        props.setLighting(LightingModelConfigGL.getDefaultLight());

        CircularMeshGeometry buttonBg = new CircularMeshGeometry(circBldr, props, null);
        myMouseSupport.setActionGeometry(buttonBg);
        getGeometries().add(buttonBg);

        EllipseGeometry.AngleBuilder<ScreenPosition> builder = new EllipseGeometry.AngleBuilder<ScreenPosition>();
        builder.setAngle(0.);
        builder.setCenter(center);
        builder.setSemiMajorAxis(ourCircleRadius);
        builder.setSemiMinorAxis(ourCircleRadius);
        builder.setVertexCount(vertexCount);
        PolygonRenderProperties props2 = new DefaultPolygonRenderProperties(getBaseZOrder() + 2, true, false);
        props2.setColor(Color.BLACK);

        EllipseGeometry circle = new EllipseGeometry(builder, props2, null);
        getGeometries().add(circle);
    }
}

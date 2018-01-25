package io.opensphere.core.hud.widget.buttons;

import io.opensphere.core.geometry.CircularMeshGeometry;
import io.opensphere.core.geometry.renderproperties.DefaultPolygonMeshRenderProperties;
import io.opensphere.core.geometry.renderproperties.LightingModelConfigGL;
import io.opensphere.core.geometry.renderproperties.PolygonMeshRenderProperties;
import io.opensphere.core.hud.framework.Component;
import io.opensphere.core.hud.framework.Renderable;
import io.opensphere.core.hud.widget.ClassicHUDPalette;
import io.opensphere.core.model.ScreenBoundingBox;
import io.opensphere.core.model.ScreenPosition;

/** Radio selection with the classic HUD look and feel. */
public class RadioSelectionMark extends Renderable
{
    /** Size of the dot for selection. */
    private static final int ourDotRadius = 4;

    /**
     * Constructor.
     *
     * @param parent parent component.
     */
    public RadioSelectionMark(Component parent)
    {
        super(parent);
        setBaseColor(ClassicHUDPalette.ourRadioButtonSelectorColor);
    }

    @Override
    public void init()
    {
        ScreenBoundingBox bbox = getDrawBounds();
        ScreenPosition center = new ScreenPosition(Math.round(bbox.getCenter().getX()), Math.round(bbox.getCenter().getY()));
        final int vertexCount = Math.min(37, (ourDotRadius + 2) * 2);
        final double warpFactor = -0.3;

        CircularMeshGeometry.Builder<ScreenPosition> circBldr = new CircularMeshGeometry.Builder<ScreenPosition>();
        circBldr.setCenter(center);
        circBldr.setWarpFactor(warpFactor);
        circBldr.setNumVertices(vertexCount);
        circBldr.setRadius(ourDotRadius);
        PolygonMeshRenderProperties props = new DefaultPolygonMeshRenderProperties(getBaseZOrder() + 5, true, false, true);
        props.setColor(getBaseColor());
        props.setLighting(LightingModelConfigGL.getDefaultLight());

        getGeometries().add(new CircularMeshGeometry(circBldr, props, null));
    }
}

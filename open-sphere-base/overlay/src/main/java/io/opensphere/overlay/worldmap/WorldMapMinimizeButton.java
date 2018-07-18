package io.opensphere.overlay.worldmap;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.util.List;

import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.geometry.PolygonGeometry;
import io.opensphere.core.geometry.renderproperties.ColorRenderProperties;
import io.opensphere.core.geometry.renderproperties.DefaultColorRenderProperties;
import io.opensphere.core.geometry.renderproperties.DefaultPolygonRenderProperties;
import io.opensphere.core.hud.framework.Component;
import io.opensphere.core.hud.framework.ControlEventSupport;
import io.opensphere.core.model.ScreenPosition;
import io.opensphere.core.util.ColorUtilities;
import io.opensphere.core.util.collections.New;

/**
 * 
 */
public class WorldMapMinimizeButton extends AbstractWorldMapRenderable
{
    /** Support class for events from the control context. */
    private final ControlEventSupport myMouseSupport;

    private PolygonGeometry myButtonGeometry;

    /**
     * @param parent
     */
    public WorldMapMinimizeButton(Component parent)
    {
        super(parent);
        myMouseSupport = new ControlEventSupport(this, getTransformer().getToolbox().getControlRegistry());
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.hud.framework.Component#handleCleanupListeners()
     */
    @Override
    public void handleCleanupListeners()
    {
        myMouseSupport.cleanupListeners();
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.hud.framework.Renderable#init()
     */
    @Override
    public void init()
    {
        super.init();

        ScreenPosition topLeft = new ScreenPosition(3, 2);
        ScreenPosition topRight = new ScreenPosition(30, 2);
        ScreenPosition bottomLeft = new ScreenPosition(3, 27);
        ScreenPosition bottomRight = new ScreenPosition(30, 27);

        List<ScreenPosition> positions = New.list(topLeft, topRight, bottomRight, bottomLeft, topLeft);

        PolygonGeometry.Builder<ScreenPosition> polyBuilder = new PolygonGeometry.Builder<>();

        ColorRenderProperties fillColor = new DefaultColorRenderProperties(getBaseZOrder() + 5, true, true, true);
        fillColor.setColor(ColorUtilities.opacitizeColor(Color.GRAY, 0.8f));
        fillColor.setHighlightColor(ColorUtilities.opacitizeColor(Color.BLUE, 0.8f));
        DefaultPolygonRenderProperties props = new DefaultPolygonRenderProperties(getBaseZOrder() + 5, true, true, fillColor);
        polyBuilder.setVertices(positions);
        myButtonGeometry = new PolygonGeometry(polyBuilder, props, null);
        getGeometries().add(myButtonGeometry);

        myMouseSupport.setActionGeometry(myButtonGeometry);
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.hud.framework.Component#mouseClicked(io.opensphere.core.geometry.Geometry,
     *      java.awt.event.MouseEvent)
     */
    @Override
    public void mouseClicked(Geometry geom, MouseEvent event)
    {
        // TODO Auto-generated method stub
        super.mouseClicked(geom, event);
    }
}

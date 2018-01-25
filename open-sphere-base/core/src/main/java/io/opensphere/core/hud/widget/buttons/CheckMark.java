package io.opensphere.core.hud.widget.buttons;

import java.util.ArrayList;
import java.util.List;

import io.opensphere.core.geometry.PolylineGeometry;
import io.opensphere.core.geometry.renderproperties.DefaultPolylineRenderProperties;
import io.opensphere.core.geometry.renderproperties.PolylineRenderProperties;
import io.opensphere.core.hud.framework.Component;
import io.opensphere.core.hud.framework.Renderable;
import io.opensphere.core.hud.widget.ClassicHUDPalette;
import io.opensphere.core.model.ScreenBoundingBox;
import io.opensphere.core.model.ScreenPosition;

/** Check mark with the classic HUD look and feel. */
public class CheckMark extends Renderable
{
    /** Height of area to draw in. */
    private static final int ourHeight = 12;

    /** Width of area to draw in. */
    private static final int ourWidth = 12;

    /**
     * Constructor.
     *
     * @param parent parent component.
     */
    public CheckMark(Component parent)
    {
        super(parent);
        setBaseColor(ClassicHUDPalette.ourCheckColor);
    }

    @Override
    public void init()
    {
        ScreenBoundingBox bbox = getDrawBounds();
        ScreenPosition center = new ScreenPosition(Math.round(bbox.getCenter().getX()), Math.round(bbox.getCenter().getY()));
        double left = center.getX() - ourWidth / 2;
        double right = left + ourWidth;
        double upper = center.getY() - ourHeight / 2;
        double lower = upper + ourHeight;

        PolylineGeometry.Builder<ScreenPosition> lineBldr = new PolylineGeometry.Builder<ScreenPosition>();
        PolylineRenderProperties props = new DefaultPolylineRenderProperties(getBaseZOrder() + 5, true, false);
        props.setColor(getBaseColor());
        props.setWidth(2f);
        lineBldr.setLineSmoothing(false);

        List<ScreenPosition> positions = new ArrayList<>();
        positions.add(new ScreenPosition(left + 1, center.getY()));
        positions.add(new ScreenPosition(center.getX(), lower - 1));
        positions.add(new ScreenPosition(right - 2, upper + 1));

        lineBldr.setVertices(positions);

        getGeometries().add(new PolylineGeometry(lineBldr, props, null));
    }
}

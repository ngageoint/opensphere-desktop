package io.opensphere.overlay.terrainprofile;

import java.awt.Color;
import java.util.HashSet;
import java.util.Set;

import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.geometry.LabelGeometry;
import io.opensphere.core.geometry.renderproperties.DefaultLabelRenderProperties;
import io.opensphere.core.geometry.renderproperties.LabelRenderProperties;
import io.opensphere.core.hud.framework.Component;
import io.opensphere.core.hud.framework.Renderable;
import io.opensphere.core.model.ScreenBoundingBox;
import io.opensphere.core.model.ScreenPosition;
import io.opensphere.core.units.length.Length;

/** The label for the min and max elevations on the chart. */
//TODO seems like we should be able to use TextLabel for this.
public class TerrainChartMinMaxLabel extends Renderable
{
    /**
     * Construct me.
     *
     * @param parent parent component.
     */
    public TerrainChartMinMaxLabel(Component parent)
    {
        super(parent);
    }

    /**
     * Draw the min/max label for the terrain profile.
     *
     * @param minElev The minimum elevation.
     * @param maxElev The maximum elevation.
     */
    public void drawChartLabel(Length minElev, Length maxElev)
    {
        Set<Geometry> startGeoms = new HashSet<>();
        startGeoms.addAll(getGeometries());
        clearGeometries();

        ScreenBoundingBox bbox = getDrawBounds();

        // Draw the min/max label
        String labelText = String.format("min: %s max: %s", minElev.toShortLabelString(6, 1), maxElev.toShortLabelString(6, 1));

        LabelGeometry.Builder<ScreenPosition> labelBuilder = new LabelGeometry.Builder<>();
        LabelRenderProperties props = new DefaultLabelRenderProperties(getBaseZOrder() + 5, true, false);
        props.setColor(Color.ORANGE);
        labelBuilder.setText(labelText);
        labelBuilder.setFont("");

        int xLoc = 0;
        int yLoc = 0;

        // keep at left horizontally
        xLoc = (int)bbox.getLowerLeft().getX() + 5;
        labelBuilder.setHorizontalAlignment(0.0f);

        // keep at middle vertically
        yLoc = (int)bbox.getCenter().getY();
        // was .5
        labelBuilder.setVerticalAlignment(0.0f);

        labelBuilder.setPosition(new ScreenPosition(xLoc, yLoc));

        LabelGeometry label = new LabelGeometry(labelBuilder, props, null);
        getGeometries().add(label);

        Set<Geometry> endGeoms = getGeometries();
        updateGeometries(endGeoms, startGeoms);
    }
}

package io.opensphere.overlay.terrainprofile;

import java.awt.Color;
import java.util.Collection;
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
import io.opensphere.core.units.UnitsProvider;
import io.opensphere.core.units.UnitsProvider.UnitsChangeListener;
import io.opensphere.core.units.length.Length;
import io.opensphere.core.units.length.Meters;

/** Text label to give the current elevation at the cross hair position. */
//TODO seems like we should be able to use TextLabel for this.
public class TerrainChartElevationLabel extends Renderable
{
    /** The current units. */
    private volatile Class<? extends Length> myUnits;

    /** Listener for units changes. */
    private final UnitsChangeListener<Length> myListener = new UnitsChangeListener<Length>()
    {
        @Override
        public void preferredUnitsChanged(Class<? extends Length> type)
        {
            myUnits = type;
        }
    };

    /**
     * Constructor.
     *
     * @param parent parent component.
     * @param unitsProvider The units provider.
     */
    public TerrainChartElevationLabel(Component parent, UnitsProvider<Length> unitsProvider)
    {
        super(parent);
        unitsProvider.addListener(myListener);
        myUnits = unitsProvider.getPreferredUnits();
    }

    /**
     * Displays the current elevation for cursor position.
     *
     * @param elevationMeters The elevation (in meters) to display
     */
    public void drawElevation(double elevationMeters)
    {
        Length elevation = Length.create(myUnits, new Meters(elevationMeters));
        Set<Geometry> startGeoms = new HashSet<>();
        startGeoms.addAll(getGeometries());
        clearGeometries();

        String elevLabel = elevation.toShortLabelString(0, 1);

        // Update our current elevation label
        LabelGeometry.Builder<ScreenPosition> labelBuilder = new LabelGeometry.Builder<ScreenPosition>();
        LabelRenderProperties props = new DefaultLabelRenderProperties(getBaseZOrder() + 6, true, false);
        props.setColor(Color.ORANGE);
        labelBuilder.setText(elevLabel);
        labelBuilder.setFont("");

        int xElevLoc = 0;
        int yElevLoc = 0;

        ScreenBoundingBox bbox = getDrawBounds();

        // keep at right horizontally
        // Subtract a little padding from border
        xElevLoc = (int)bbox.getLowerRight().getX() - 15;
        labelBuilder.setHorizontalAlignment(1f);

        // keep at center vertically
        yElevLoc = (int)bbox.getCenter().getY();
        labelBuilder.setVerticalAlignment(0.0f);

        labelBuilder.setPosition(new ScreenPosition(xElevLoc, yElevLoc));

        LabelGeometry elevLabelGeometry = new LabelGeometry(labelBuilder, props, null);

        getGeometries().add(elevLabelGeometry);

        Set<Geometry> endGeoms = getGeometries();
        updateGeometries(endGeoms, startGeoms);
    }

    /** This will remove the elevation graph from tool. */
    public void removeElevation()
    {
        Set<Geometry> startGeoms = new HashSet<>();
        startGeoms.addAll(getGeometries());
        clearGeometries();

        Set<Geometry> endGeoms = getGeometries();
        updateGeometries(endGeoms, startGeoms);
    }
}

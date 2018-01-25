package io.opensphere.terrainprofile;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.opensphere.core.geometry.PolylineGeometry;
import io.opensphere.core.geometry.renderproperties.DefaultPolylineRenderProperties;
import io.opensphere.core.geometry.renderproperties.PolylineRenderProperties;
import io.opensphere.core.geometry.renderproperties.ZOrderRenderProperties;
import io.opensphere.core.hud.framework.TransformerHelper;
import io.opensphere.core.model.Altitude;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.LineType;

/** Marker on the terrain which indicates the terrain profile being shown. */
public class TerrainProfileMarker implements MapProfileListener
{
    /** The altitude to use for drawing profile and marker. */
    private static final double ourAltM = 10.0;

    /** Helper class for handling publishing of geometries. */
    private final TransformerHelper myHelper;

    /**
     * When the mouse is over the profile graph, this marker shows the matching
     * position along the profile marker.
     */
    private PolylineGeometry myMarkerLine;

    /** The currently rendered profile line. */
    private PolylineGeometry myProfileLine;

    /**
     * Constructor.
     *
     * @param helper Helper for the tool box and publishing geometries.
     */
    public TerrainProfileMarker(TransformerHelper helper)
    {
        myHelper = helper;
    }

    /** Perform any required cleanup. */
    public void close()
    {
        Set<PolylineGeometry> removes = new HashSet<>();
        if (myProfileLine != null)
        {
            removes.add(myProfileLine);
        }
        if (myMarkerLine != null)
        {
            removes.add(myMarkerLine);
        }
        myHelper.publishGeometries(Collections.<PolylineGeometry>emptySet(), removes);
    }

    /**
     * Draw a Marker (vertical line) on the map profile line.
     *
     * @param markerPositions The points that determine the marker line.
     */
    public void drawMapMarker(List<GeographicPosition> markerPositions)
    {
        PolylineGeometry oldLine = myMarkerLine;

        ArrayList<PolylineGeometry> polys = new ArrayList<>();
        if (markerPositions != null && !markerPositions.isEmpty())
        {
            // Go back through and add an altitude to these points
            List<GeographicPosition> markerAlt = new ArrayList<>();
            for (GeographicPosition position : markerPositions)
            {
                markerAlt.add(new GeographicPosition(LatLonAlt.createFromDegreesMeters(position.getLatLonAlt().getLatD(),
                        position.getLatLonAlt().getLonD(), ourAltM, Altitude.ReferenceLevel.TERRAIN)));
            }

            // now create the poly line
            PolylineGeometry.Builder<GeographicPosition> polyBuilder = new PolylineGeometry.Builder<GeographicPosition>();
            PolylineRenderProperties props = new DefaultPolylineRenderProperties(ZOrderRenderProperties.TOP_Z, true, false);
            props.setColor(Color.ORANGE);
            props.setWidth(2f);
            polyBuilder.setVertices(markerAlt);
            polyBuilder.setLineSmoothing(true);
            myMarkerLine = new PolylineGeometry(polyBuilder, props, null);

            polys.add(myMarkerLine);
        }

        Set<PolylineGeometry> removes = oldLine == null ? Collections.<PolylineGeometry>emptySet()
                : Collections.singleton(oldLine);

        myHelper.publishGeometries(polys, removes);
    }

    /**
     * Draw a terrain profile line on the map with the given List of positions.
     *
     * @param profileEnds The points that describe the profile line.
     */
    public void drawMapProfile(List<GeographicPosition> profileEnds)
    {
        PolylineGeometry oldLine = myProfileLine;

        ArrayList<PolylineGeometry> polys = new ArrayList<>();

        if (profileEnds != null && !profileEnds.isEmpty())
        {
            // Go back through and add an altitude to these points
            List<GeographicPosition> altProfile = new ArrayList<>();
            for (GeographicPosition position : profileEnds)
            {
                altProfile.add(new GeographicPosition(LatLonAlt.createFromDegreesMeters(position.getLatLonAlt().getLatD(),
                        position.getLatLonAlt().getLonD(), ourAltM, Altitude.ReferenceLevel.TERRAIN)));
            }

            PolylineGeometry.Builder<GeographicPosition> polyBuilder = new PolylineGeometry.Builder<GeographicPosition>();
            PolylineRenderProperties props = new DefaultPolylineRenderProperties(ZOrderRenderProperties.TOP_Z, true, false);
            props.setColor(Color.ORANGE);
            props.setWidth(2f);
            polyBuilder.setVertices(altProfile);
            polyBuilder.setLineSmoothing(true);
            polyBuilder.setLineType(LineType.GREAT_CIRCLE);
            myProfileLine = new PolylineGeometry(polyBuilder, props, null);

            polys.add(myProfileLine);
        }

        Set<PolylineGeometry> removes = oldLine == null ? Collections.<PolylineGeometry>emptySet()
                : Collections.singleton(oldLine);

        myHelper.publishGeometries(polys, removes);
    }

    /** Remove the map marker. */
    @Override
    public void removeMapMarker()
    {
        PolylineGeometry oldLine = myMarkerLine;

        Set<PolylineGeometry> removes = oldLine == null ? Collections.<PolylineGeometry>emptySet()
                : Collections.singleton(oldLine);

        myHelper.publishGeometries(Collections.<PolylineGeometry>emptySet(), removes);
    }

    @Override
    public void updateMapMarker(List<GeographicPosition> markerPositions)
    {
        drawMapMarker(markerPositions);
    }
}

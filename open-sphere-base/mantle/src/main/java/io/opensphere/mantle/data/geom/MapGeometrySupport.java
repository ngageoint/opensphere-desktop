package io.opensphere.mantle.data.geom;

import java.awt.Color;
import java.io.Serializable;
import java.util.List;

import io.opensphere.core.cache.util.PropertyDescriptor;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.projection.Projection;
import io.opensphere.mantle.data.MapVisualizationType;
import io.opensphere.mantle.data.VisualizationSupport;

/**
 * Base interface for a MapGeometrySupport that will help the transformers build
 * the real geometries that are going to be built and displayed on the map.
 */
public interface MapGeometrySupport extends Serializable, VisualizationSupport
{
    /** Property descriptor used in the data registry. */
    PropertyDescriptor<MapGeometrySupport> PROPERTY_DESCRIPTOR = new PropertyDescriptor<MapGeometrySupport>("mapGeometrySupport",
            MapGeometrySupport.class);

    // TODO remove this until surfaces is re-implemented
    // SurfaceFeatureVisualizationStyle.class

    /**
     * True if this geometry should follow terrain.
     *
     * @return true if follow terrain, false if not
     */
    boolean followTerrain();

    /**
     * Gets the bounding box of the geometry.
     *
     * @param projection The map projection to use when calculating the bounding
     *            box.
     * @return the bounding box
     */
    GeographicBoundingBox getBoundingBox(Projection projection);

    /**
     * Gets the {@link CallOutSupport} for this Geometry may be null if there is
     * no call out support.
     *
     * @return the {@link CallOutSupport}
     */
    CallOutSupport getCallOutSupport();

    /**
     * Returns the {@link List} of {@link MapGeometrySupport} that are the
     * children of this geometry. Check hasChildren().
     *
     * @return the list, or null if there are none, or an empty list.
     */
    List<MapGeometrySupport> getChildren();

    /**
     * Gets the color for the geometry.
     *
     * @return the {@link Color}
     */
    Color getColor();

    /**
     * Gets the {@link TimeSpan}.
     *
     * @return the {@link TimeSpan}
     */
    TimeSpan getTimeSpan();

    /**
     * Gets the tool tip for this geometry. Note: Null indicates no tool tip
     *
     * @return the tool tip or null if none
     */
    String getToolTip();

    /**
     * Gets the category of visualization type for this support.
     *
     * @return the visualization type
     */
    MapVisualizationType getVisualizationType();

    /**
     * Returns true if this geometry has child geometries.
     *
     * @return true if there are child geometries.
     */
    boolean hasChildren();

    /**
     * Sets the {@link CallOutSupport} for this geometry. Note: Null indicates
     * no call out.
     *
     * @param cos the {@link CallOutSupport} or null.
     */
    void setCallOutSupport(CallOutSupport cos);

    /**
     * Sets the color for the geometry.
     *
     * @param c - the {@link Color}
     * @param source - the object making the change.
     */
    void setColor(Color c, Object source);

    /**
     * Sets if this geometry should follow terrain.
     *
     * @param follow - true to follow, false not
     * @param source - the object making the change.
     */
    void setFollowTerrain(boolean follow, Object source);

    /**
     * Sets the {@link TimeSpan}.
     *
     * @param ts the time span
     */
    void setTimeSpan(TimeSpan ts);

    /**
     * Sets the tool tip for this geometry. Note: Null indicates no tip.
     *
     * @param tip the tool tip
     */
    void setToolTip(String tip);
}

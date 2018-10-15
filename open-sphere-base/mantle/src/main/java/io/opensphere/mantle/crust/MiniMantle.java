package io.opensphere.mantle.crust;

import java.awt.Color;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import io.opensphere.core.event.EventListenerService;
import io.opensphere.core.event.EventManager;
import io.opensphere.core.geometry.ConstrainableGeometry;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.geometry.constraint.Constraints;
import io.opensphere.core.geometry.constraint.MutableConstraints;
import io.opensphere.core.geometry.renderproperties.BaseRenderProperties;
import io.opensphere.core.geometry.renderproperties.ColorRenderProperties;
import io.opensphere.core.util.ColorUtilities;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.data.event.DataTypeInfoColorChangeEvent;
import io.opensphere.mantle.data.event.DataTypeInfoLoadsToChangeEvent;
import io.opensphere.mantle.data.event.DataTypeVisibilityChangeEvent;

/** Mini Mantle Controller. */
public class MiniMantle extends EventListenerService
{
    /** Map of data type key to geometries. */
    private final Map<String, Collection<Geometry>> myTypeToGeoms = New.map();

    /** Map of data type key to layer settings. */
    private final Map<String, LayerSettings> myTypeToSettings = New.map();

    /**
     * Constructor.
     *
     * @param eventManager the event manager
     */
    public MiniMantle(EventManager eventManager)
    {
        super(eventManager);
        bindEvent(DataTypeVisibilityChangeEvent.class, this::handleDataTypeVisibilityChange);
        bindEvent(DataTypeInfoColorChangeEvent.class, this::handleDataTypeInfoColorChange);
        bindEvent(DataTypeInfoLoadsToChangeEvent.class, this::handleDataTypeInfoLoadsToChange);
    }

    /**
     * Adds geometries for the data type key.
     *
     * @param dataTypeKey the data type key
     * @param geoms the geometries
     */
    public synchronized void addGeometries(String dataTypeKey, Collection<? extends Geometry> geoms)
    {
        myTypeToGeoms.computeIfAbsent(dataTypeKey, k -> New.set()).addAll(geoms);

        // // Update the geometries' settings
        // LayerSettings layerSettings = myTypeToSettings.get(dataTypeKey);
        // if (layerSettings != null && layerSettings.getColor() != null)
        // {
        // setColor(layerSettings.getColor(), geoms);
        // }
    }

    /**
     * Removes geometries for the data type key.
     *
     * @param dataTypeKey the data type key
     * @param geoms the geometries
     */
    public synchronized void removeGeometries(String dataTypeKey, Collection<? extends Geometry> geoms)
    {
        Collection<Geometry> allGeoms = myTypeToGeoms.get(dataTypeKey);
        if (allGeoms != null)
        {
            allGeoms.removeAll(geoms);
        }
    }

    /**
     * Removes all geometries for the data type key.
     *
     * @param dataTypeKey the data type key
     * @return the geometries that were removed, or null
     */
    public synchronized Collection<Geometry> removeGeometries(String dataTypeKey)
    {
        return myTypeToGeoms.remove(dataTypeKey);
    }

    /**
     * Gets the geometries for the data type key.
     *
     * @param dataTypeKey the data type key
     * @return the geometries
     */
    public synchronized Collection<Geometry> getGeometries(String dataTypeKey)
    {
        return myTypeToGeoms.getOrDefault(dataTypeKey, Collections.emptySet());
    }

    /**
     * Handles a DataTypeVisibilityChangeEvent.
     *
     * @param event the event
     */
    private synchronized void handleDataTypeVisibilityChange(DataTypeVisibilityChangeEvent event)
    {
        Collection<Geometry> geoms = myTypeToGeoms.get(event.getDataTypeKey());
        if (CollectionUtilities.hasContent(geoms))
        {
            geoms.stream().filter(g -> g.getRenderProperties() instanceof BaseRenderProperties)
                    .map(g -> (BaseRenderProperties)g.getRenderProperties()).forEach(g -> g.setHidden(!event.isVisible()));
        }
    }

    /**
     * Handles a DataTypeInfoColorChangeEvent.
     *
     * @param event the event
     */
    private synchronized void handleDataTypeInfoColorChange(DataTypeInfoColorChangeEvent event)
    {
        Collection<Geometry> geoms = myTypeToGeoms.get(event.getDataTypeKey());
        if (CollectionUtilities.hasContent(geoms))
        {
            for (Geometry geom : geoms)
            {
                if (geom.getRenderProperties() instanceof ColorRenderProperties)
                {
                    ColorRenderProperties properties = (ColorRenderProperties)geom.getRenderProperties();
                    if (event.isOpacityChangeOnly())
                    {
                        properties.opacitizeColor(event.getColor().getAlpha() / (float)ColorUtilities.COLOR_COMPONENT_MAX_VALUE);
                    }
                    else
                    {
                        properties.setColor(event.getColor());
                    }
                }
            }

            myTypeToSettings.computeIfAbsent(event.getDataTypeKey(), k -> new LayerSettings()).setColor(event.getColor());
        }
    }

    /**
     * Handles a DataTypeInfoLoadsToChangeEvent.
     *
     * @param event the event
     */
    private synchronized void handleDataTypeInfoLoadsToChange(DataTypeInfoLoadsToChangeEvent event)
    {
        Collection<Geometry> geoms = myTypeToGeoms.get(event.getDataTypeKey());
        if (CollectionUtilities.hasContent(geoms))
        {
            for (Geometry geom : geoms)
            {
                if (geom instanceof ConstrainableGeometry)
                {
                    Constraints constraints = ((ConstrainableGeometry)geom).getConstraints();
                    if (constraints instanceof MutableConstraints)
                    {
                        MutableConstraints mutableConstraints = (MutableConstraints)constraints;
                        if (event.getLoadsTo().isTimelineEnabled())
                        {
                            mutableConstraints.resetTimeConstraint();
                        }
                        else
                        {
                            mutableConstraints.setTimeConstraint(null);
                        }
                    }
                }
            }
        }
    }

    /** Layer settings. */
    private static class LayerSettings
    {
        /** The color. */
        private Color myColor;

        /**
         * Gets the color.
         *
         * @return the color
         */
        @SuppressWarnings("unused")
        public Color getColor()
        {
            return myColor;
        }

        /**
         * Sets the color.
         *
         * @param color the color
         */
        public void setColor(Color color)
        {
            myColor = color;
        }
    }
}

package io.opensphere.controlpanels.component.map.background;

import java.util.Collection;
import java.util.List;
import java.util.Observable;

import io.opensphere.core.geometry.TileGeometry;
import io.opensphere.core.util.collections.New;

/**
 * Contains the data the BackgroundOverlay uses to display the background.
 */
public class BackgroundModel extends Observable
{
    /**
     * The property name for geometries.
     */
    public static final String GEOMETRIES_PROP = "geometry";

    /**
     * The collection of geometries.
     */
    private final List<TileGeometry> myGeometries = New.list();

    /**
     * The scale factors of each geometry.
     */
    private final List<Double> myGeometryScaleFactors = New.list();

    /**
     * Adds the geometry.
     *
     * @param geometry The geometries to add.
     */
    public void add(Collection<TileGeometry> geometry)
    {
        myGeometries.addAll(geometry);

        setChanged();
        notifyObservers(GEOMETRIES_PROP);
    }

    /**
     * Gets the background geometries.
     *
     * @return The collection of background geometries.
     */
    public Collection<TileGeometry> getGeometries()
    {
        return New.<TileGeometry>unmodifiableList(myGeometries);
    }

    /**
     * Gets the individual geometry scale factors.
     *
     * @return The number to increase the width and height of the geometry's
     *         images.
     */
    public List<Double> getGeometryScaleFactors()
    {
        return myGeometryScaleFactors;
    }

    /**
     * Removes the geometry.
     *
     * @param geometry The geometry to remove.
     */
    public void remove(Collection<TileGeometry> geometry)
    {
        for (TileGeometry aGeometry : geometry)
        {
            myGeometries.remove(aGeometry);
        }

        setChanged();
        notifyObservers(GEOMETRIES_PROP);
    }
}

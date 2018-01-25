package io.opensphere.core.geometry;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import io.opensphere.core.geometry.renderproperties.ScalableMeshRenderProperties;
import io.opensphere.core.geometry.renderproperties.ZOrderRenderProperties;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.util.lang.Pair;

/**
 * A geometry of grouped points that individually display another geometry. The
 * sub geometry heights are variable to the intensity of other nearby points.
 * The individual sub geometries that are created can be accessed.
 */
public abstract class AbstractGroupHeightGeometry extends GeometryGroupGeometry
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(AbstractGroupHeightGeometry.class);

    /** The map that holds information for each map coordinate. */
    private final Map<Pair<Integer, Integer>, GridCoordinateInfo> myGridCoordMap;

    /** The grid size. */
    private final int myGridSize;

    /** The locations of my points. */
    private final Collection<GeographicPosition> myLocations;

    /** The power value used in scaling the repeated points. */
    private final float myPowerValue;

    /** The render properties. */
    private final ScalableMeshRenderProperties myRenderProperties;

    /**
     * Construct the geometry.
     *
     * @param builder The builder for the geometry.
     * @param renderProperties The render properties.
     */
    public AbstractGroupHeightGeometry(AbstractGroupHeightGeometry.Builder builder, ZOrderRenderProperties renderProperties)
    {
        super(builder, renderProperties);
        myRenderProperties = builder.getRenderProperties();
        myLocations = builder.getLocations();
        myGridSize = builder.getGridSize();
        myPowerValue = builder.getPowerValue();
        myGridCoordMap = new HashMap<>();

        // Go calculate the height adjustments.
        calculateHeightAdjustments();
        // And create sub-geometries
        createGeometries();
    }

    @Override
    public AbstractGroupHeightGeometry clone()
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Access the map that holds information for each grid coordinate.
     *
     * @return The map that holds information for each grid coordinate.
     */
    public Map<Pair<Integer, Integer>, GridCoordinateInfo> getGridCoordinatesMap()
    {
        return myGridCoordMap;
    }

    /**
     * Get the grid size value.
     *
     * @return The grid size.
     */
    public int getGridSize()
    {
        return myGridSize;
    }

    /**
     * Access the internal sub geometries.
     *
     * @return The internal sub geometries.
     */
    public Collection<Geometry> getInternalGeometries()
    {
        return getGeometries();
    }

    /**
     * Get the locations.
     *
     * @return The Positions that describe feature locations.
     */
    public Collection<GeographicPosition> getLocations()
    {
        return myLocations;
    }

    /**
     * Get the value used in adding height to repeated features (features that
     * are at the same location).
     *
     * @return The power value.
     */
    public float getPowerValue()
    {
        return myPowerValue;
    }

    @Override
    public ScalableMeshRenderProperties getRenderProperties()
    {
        return myRenderProperties;
    }

    /**
     * Create the geometries for the locations.
     */
    protected abstract void createGeometries();

    /**
     * This method goes through the locations and determines height adjustments
     * for the features. So if there are multiple features on top of each other,
     * they will have a proportionally greater height.
     */
    private void calculateHeightAdjustments()
    {
        GeographicBoundingBox boundingBox = GeographicBoundingBox.getMinimumBoundingBox(myLocations);
        double minLat = boundingBox.getLowerLeft().getLatLonAlt().getLatD();
        double minLon = boundingBox.getLowerLeft().getLatLonAlt().getLonD();

        // Handle special case of crossing 180 longitude border.
        double lonDiff = Math
                .abs(boundingBox.getLowerRight().getLatLonAlt().getLonD() - boundingBox.getLowerLeft().getLatLonAlt().getLonD());
        double deltaLon = boundingBox.getDeltaLonD();
        if (lonDiff > 180.0)
        {
            deltaLon = boundingBox.getLowerRight().getLatLonAlt().getLonD() + 360
                    - boundingBox.getLowerLeft().getLatLonAlt().getLonD();
        }

        // Need to do some special processing to get square (not rectangular)
        // grid sizes.
        double gridDegreeSize = 0;
        if (deltaLon > boundingBox.getDeltaLatD())
        {
            gridDegreeSize = deltaLon / myGridSize;
        }
        else
        {
            gridDegreeSize = boundingBox.getDeltaLatD() / myGridSize;
        }

        int numGridWidth = (int)Math.ceil(deltaLon / gridDegreeSize);
        int numGridHeight = (int)Math.ceil(boundingBox.getDeltaLatD() / gridDegreeSize);

        // This assumes myLocations will remain in the same order.
        int[][] grid = new int[numGridWidth][numGridHeight];

        int x = 0;
        int y = 0;

        // Go through and find locations which are on top of each other or very
        // close proximity (granularity specified by grid size). Then
        // increment their value so they will have a larger height.
        for (GeographicPosition pos : myLocations)
        {
            double lat = pos.getLatLonAlt().getLatD();
            double lon = pos.getLatLonAlt().getLonD();

            // Need to check if we cross the 180 degree boundary and if so, do
            // some special processing.
            if (lonDiff > 180.0 && lon < 0.0)
            {
                x = (int)Math.round((lon - (minLon - 360)) / boundingBox.getDeltaLonD() * numGridWidth);
            }
            else
            {
                x = (int)Math.round((lon - minLon) / boundingBox.getDeltaLonD() * numGridWidth);
            }

            y = (int)Math.round((lat - minLat) / boundingBox.getDeltaLatD() * numGridHeight);

            if (x > numGridWidth - 1)
            {
                x = numGridWidth - 1;
            }
            if (y > numGridHeight - 1)
            {
                y = numGridHeight - 1;
            }

            grid[x][y] = grid[x][y] + 1;
            double adjustedHeight = Math.pow(grid[x][y], myPowerValue) * myRenderProperties.getHeight();
            if (LOGGER.isTraceEnabled())
            {
                LOGGER.trace(" grid[" + x + "][" + y + "] = " + grid[x][y] + " Adjusted height = " + adjustedHeight);
            }

            Pair<Integer, Integer> gridCoords = new Pair<>(Integer.valueOf(x), Integer.valueOf(y));

            // Populate grid coordinate information
            if (myGridCoordMap.containsKey(gridCoords))
            {
                myGridCoordMap.get(gridCoords).getPositions().add(pos);
                myGridCoordMap.get(gridCoords).setAdjustedHeight(adjustedHeight);
            }
            else
            {
                GridCoordinateInfo gridInfo = new GridCoordinateInfo();
                gridInfo.setAdjustedHeight(adjustedHeight);
                gridInfo.getPositions().add(pos);

                myGridCoordMap.put(gridCoords, gridInfo);
            }
        }
    }

    /**
     * Builder for the geometry.
     */
    public static class Builder extends GeometryGroupGeometry.Builder
    {
        /** The grid size. */
        private int myGridSize;

        /** The locations of my points. */
        private Collection<GeographicPosition> myLocations;

        /** The power value used in scaling the repeated points. */
        private float myPowerValue;

        /** The render properties. */
        private ScalableMeshRenderProperties myRenderProperties;

        /**
         * Constructor for the builder.
         */
        public Builder()
        {
            super(GeographicPosition.class);
        }

        /**
         * Access the grid size value.
         *
         * @return The grid size.
         */
        public int getGridSize()
        {
            return myGridSize;
        }

        /**
         * Access the locations.
         *
         * @return The Positions that describe feature locations.
         */
        public Collection<GeographicPosition> getLocations()
        {
            return myLocations;
        }

        /**
         * Access the value used in scaling the height of features.
         *
         * @return The value used in scaling the height of features.
         */
        public float getPowerValue()
        {
            return myPowerValue;
        }

        /**
         * Access the render properties.
         *
         * @return The render properties.
         */
        public ScalableMeshRenderProperties getRenderProperties()
        {
            return myRenderProperties;
        }

        /**
         * Set the grid size value.
         *
         * @param size The grid size.
         */
        public void setGridSize(int size)
        {
            myGridSize = size;
        }

        /**
         * Set the initialGeometries.
         *
         * @param initialGeometries the initialGeometries to set
         */
        @Override
        public void setInitialGeometries(Collection<Geometry> initialGeometries)
        {
            // Disallow setting initial geometries by doing nothing.
            LOGGER.warn("Should not set initial Geometries for this geometry, they will be ignored.");
        }

        /**
         * Set the locations of the features.
         *
         * @param locations The locations of the features.
         */
        public void setLocations(Collection<GeographicPosition> locations)
        {
            myLocations = locations;
        }

        /**
         * Set the value used in scaling the height of features.
         *
         * @param powerValue The value used in scaling the height of features.
         */
        public void setPowerValue(float powerValue)
        {
            myPowerValue = powerValue;
        }

        /**
         * Set the render properties for this geometry.
         *
         * @param renderProperties The track render properties.
         */
        public void setRenderProperties(ScalableMeshRenderProperties renderProperties)
        {
            myRenderProperties = renderProperties;
        }
    }
}

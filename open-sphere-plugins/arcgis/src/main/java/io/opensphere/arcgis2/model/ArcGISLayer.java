package io.opensphere.arcgis2.model;

import java.net.URL;
import java.util.Collections;
import java.util.List;

import io.opensphere.core.cache.util.PropertyDescriptor;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.util.MathUtil;
import io.opensphere.core.util.collections.New;

/** Model for an ArcGIS layer. */
public class ArcGISLayer
{
    /** Descriptor for active property. */
    public static final PropertyDescriptor<Boolean> ACTIVE_PROPERTY = new PropertyDescriptor<Boolean>("active", Boolean.class);

    /** Property descriptor for a layer. */
    public static final PropertyDescriptor<ArcGISLayer> LAYER_PROPERTY = new PropertyDescriptor<ArcGISLayer>("layer",
            ArcGISLayer.class);

    /** The bounds of the layer. */
    private final GeographicBoundingBox myBounds;

    /** The width of the tile. */
    private final int myCols;

    /** The layer description. */
    private final String myDescription;

    /** The layer ID. */
    private final String myId;

    /** The keywords for the layer. */
    private final List<? extends String> myKeywords;

    /** The layer name. */
    private final String myLayerName;

    /**
     * The maximum number of zoom levels.
     */
    private final int myMaxLevels;

    /**
     * The path to this layer (a list of parent layers starting with the top).
     */
    private final List<String> myPath;

    /** The height of the tile. */
    private final int myRows;

    /** A more detailed description of the layer. */
    private final String myServiceDescription;

    /**
     * True if this layer has a tile cache, false otherwise.
     */
    private final boolean mySingleFusedMapCache;

    /**
     * The spatial reference of the layer.
     */
    private final int mySpatialReference;

    /** The URL for the layer. */
    private final URL myURL;

    /**
     * Constructor.
     *
     * @param builder The builder.
     */
    public ArcGISLayer(Builder builder)
    {
        myId = builder.myId;
        myURL = builder.myURL;
        myLayerName = builder.myLayerName;
        myDescription = builder.myDescription;
        myServiceDescription = builder.myServiceDescription;
        myRows = builder.myRows;
        myCols = builder.myCols;
        myBounds = builder.myBounds;
        myKeywords = New.unmodifiableList(builder.myKeywords);
        myPath = New.unmodifiableList(builder.myPath);
        mySpatialReference = builder.getSpatialReference();
        myMaxLevels = builder.getMaxLevels();
        mySingleFusedMapCache = builder.isSingleFusedMapCache();
    }

    /**
     * Convert a latitude to a Y coordinate.
     *
     * @param resolution The resolution in degrees/row.
     * @param lat The latitude degrees.
     * @return The Y coordinate.
     */
    public double convertLatToY(double resolution, double lat)
    {
        return (myBounds.getMaxLatD() - lat) / resolution / myRows;
    }

    /**
     * Convert a longitude to a X coordinate.
     *
     * @param resolution The resolution in degrees/row.
     * @param lon The longitude degrees.
     * @return The X coordinate.
     */
    public double convertLonToX(double resolution, double lon)
    {
        return (lon - myBounds.getMinLonD()) / resolution / myCols;
    }

    /**
     * Convert an X coordinate to longitude degrees.
     *
     * @param resolution The resolution in degrees/row.
     * @param x The X coordinate.
     * @return The longitude degrees.
     */
    public double convertXtoLon(double resolution, int x)
    {
        return MathUtil.clamp(myBounds.getMinLonD() + resolution * myCols * x, -180, 180);
    }

    /**
     * Convert a Y coordinate to latitude degrees.
     *
     * @param resolution The resolution in degrees/row.
     * @param y The Y coordinate.
     * @return The latitude degrees.
     */
    public double convertYtoLat(double resolution, int y)
    {
        return MathUtil.clamp(myBounds.getMaxLatD() - resolution * myRows * y, -90, 90);
    }

    /**
     * Get the width of the tile.
     *
     * @return The width of the tile.
     */
    public int getCols()
    {
        return myCols;
    }

    /**
     * Get the description.
     *
     * @return The description.
     */
    public String getDescription()
    {
        return myDescription;
    }

    /**
     * Gets the id.
     *
     * @return the id
     */
    public String getId()
    {
        return myId;
    }

    /**
     * Get the keywords for this layer.
     *
     * @return The keywords.
     */
    public List<? extends String> getKeywords()
    {
        return myKeywords;
    }

    /**
     * Get the layer name.
     *
     * @return The layer name.
     */
    public String getLayerName()
    {
        return myLayerName;
    }

    /**
     * Gets the maximum number of zoom levels.
     *
     * @return The maximum number of zoom levels.
     */
    public int getMaxLevels()
    {
        return myMaxLevels;
    }

    /**
     * Get the path to this layer (a list of parent layers starting with the
     * top).
     *
     * @return The path.
     */
    public List<String> getPath()
    {
        return myPath;
    }

    /**
     * Get the height of the tile.
     *
     * @return The height of the tile.
     */
    public int getRows()
    {
        return myRows;
    }

    /**
     * Get the service description.
     *
     * @return The service description.
     */
    public String getServiceDescription()
    {
        return myServiceDescription;
    }

    /**
     * Gets the spatial reference of the layer.
     *
     * @return The spatial reference.
     */
    public int getSpatialReference()
    {
        return mySpatialReference;
    }

    /**
     * Get the URL for the layer.
     *
     * @return The URL.
     */
    public URL getURL()
    {
        return myURL;
    }

    /**
     * Get the full URL for the layer.
     *
     * @return The full URL.
     */
    public String getFullURL()
    {
        return new StringBuilder(myURL.toString()).append('/').append(myId).toString();
    }

    /**
     * Indicates if this layer is cached on the server.
     *
     * @return True if this layer has a tile cache, false otherwise.
     */
    public boolean isSingleFusedMapCache()
    {
        return mySingleFusedMapCache;
    }

    @Override
    public String toString()
    {
        return new StringBuilder(256).append(getClass().getSimpleName()).append(" [").append(myLayerName).append(']').toString();
    }

    /**
     * Builder class.
     */
    public static class Builder
    {
        /** The bounds of the layer. */
        private GeographicBoundingBox myBounds;

        /** The width of the tile. */
        private int myCols;

        /** The layer description. */
        private String myDescription;

        /** The layer ID. */
        private String myId;

        /** The keywords for the layer. */
        private List<String> myKeywords = Collections.emptyList();

        /** The layer name. */
        private String myLayerName;

        /**
         * The maximum number of zoom levels.
         */
        private int myMaxLevels = 18;

        /** The path to this layer. */
        private List<String> myPath = Collections.emptyList();

        /** The height of the tile. */
        private int myRows;

        /** A more detailed description of the layer. */
        private String myServiceDescription;

        /**
         * True if this layer has a tile cache, false otherwise.
         */
        private boolean mySingleFusedMapCache;

        /**
         * The default spatial reference.
         */
        private int mySpatialReference = 3857;

        /** The URL for the layer. */
        private URL myURL;

        /**
         * Gets the id.
         *
         * @return the id
         */
        public String getId()
        {
            return myId;
        }

        /**
         * Get the layer name.
         *
         * @return The layer name.
         */
        public String getLayerName()
        {
            return myLayerName;
        }

        /**
         * Gets the maximum number of zoom levels.
         *
         * @return The maximum number of zoom levels.
         */
        public int getMaxLevels()
        {
            return myMaxLevels;
        }

        /**
         * Gets the spatial reference of the layer.
         *
         * @return The spatial reference.
         */
        public int getSpatialReference()
        {
            return mySpatialReference;
        }

        /**
         * Indicates if this layer is cached on the server.
         *
         * @return True if this layer has a tile cache, false otherwise.
         */
        public boolean isSingleFusedMapCache()
        {
            return mySingleFusedMapCache;
        }

        /**
         * Set the bounds of the layer.
         *
         * @param bounds The bounds.
         */
        public void setBounds(GeographicBoundingBox bounds)
        {
            myBounds = bounds;
        }

        /**
         * Set the width of the tile.
         *
         * @param cols The width of the tile.
         */
        public void setCols(int cols)
        {
            myCols = cols;
        }

        /**
         * Set the description.
         *
         * @param description The description.
         */
        public void setDescription(String description)
        {
            myDescription = description;
        }

        /**
         * Sets the id.
         *
         * @param id the id
         */
        public void setId(String id)
        {
            myId = id;
        }

        /**
         * Set the keywords for this layer.
         *
         * @param list The keywords.
         */
        public void setKeywords(List<String> list)
        {
            myKeywords = list;
        }

        /**
         * Set the layer name.
         *
         * @param layerName The layer name.
         */
        public void setLayerName(String layerName)
        {
            myLayerName = layerName;
        }

        /**
         * Sets The maximum number of zoom levels.
         *
         * @param maxLevels The maximum number of zoom levels.
         */
        public void setMaxLevels(int maxLevels)
        {
            myMaxLevels = maxLevels;
        }

        /**
         * Set the path to this layer (a list of parent layers starting with the
         * top).
         *
         * @param path The path.
         */
        public void setPath(List<String> path)
        {
            myPath = path;
        }

        /**
         * Set the height of the tile.
         *
         * @param rows The height of the tile.
         */
        public void setRows(int rows)
        {
            myRows = rows;
        }

        /**
         * Set the service description.
         *
         * @param serviceDescription The service description.
         */
        public void setServiceDescription(String serviceDescription)
        {
            myServiceDescription = serviceDescription;
        }

        /**
         * Sets if this layer is cached on the server or not.
         *
         * @param singleFusedMapCache True if this layer has a tile cache, false
         *            otherwise.
         */
        public void setSingleFusedMapCache(boolean singleFusedMapCache)
        {
            mySingleFusedMapCache = singleFusedMapCache;
        }

        /**
         * Sets the spatial reference.
         *
         * @param spatialReference The spatial reference.
         */
        public void setSpatialReference(int spatialReference)
        {
            mySpatialReference = spatialReference;
        }

        /**
         * Set the URL for the layer.
         *
         * @param url The URL.
         */
        public void setURL(URL url)
        {
            myURL = url;
        }
    }
}

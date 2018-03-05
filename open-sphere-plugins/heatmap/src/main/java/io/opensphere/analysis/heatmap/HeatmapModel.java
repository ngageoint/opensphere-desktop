package io.opensphere.analysis.heatmap;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Polygon;
import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import io.opensphere.analysis.util.MutableInteger;
import io.opensphere.core.MapManager;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.projection.Projection;
import io.opensphere.core.util.MathUtil;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.controller.DataTypeController;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.geom.MapGeometrySupport;
import io.opensphere.mantle.data.geom.MapLocationGeometrySupport;
import io.opensphere.mantle.data.geom.MapPathGeometrySupport;
import io.opensphere.mantle.data.geom.MapPolygonGeometrySupport;
import io.opensphere.mantle.data.geom.MapPolylineGeometrySupport;
import io.opensphere.mantle.data.geom.style.FeatureVisualizationStyle;
import io.opensphere.mantle.transformer.MapDataElementTransformer;
import io.opensphere.mantle.transformer.impl.StyleMapDataElementTransformer;

/** The heat map data model. */
public class HeatmapModel
{
    /** The projection. */
    private final Projection myProjection;

    /** The data type controller. */
    private final DataTypeController myDataTypeController;

    /** The bounding box. */
    private GeographicBoundingBox myBbox;

    /** The image size. */
    private Dimension myImageSize;

    /** Map of pixel (x, y) to count. */
    private final TIntObjectMap<MutableInteger> myPixelToCountMap;

    /** The polygons. */
    private final List<Polygon> myPolygons;

    /** The polylines. */
    private final List<Polygon> myPolylines;

    /** The collection of geometries used to create the heatmap. */
    private List<MapGeometrySupport> myGeometries;

    /** the manager used to access map state. */
    private MapManager myMapManager;

    /**
     * Constructor.
     *
     * @param geomResults the geometry results
     * @param projection the projection
     * @param dataTypeController the data type controller
     * @param mapManager the map manager through which the visible bounding box
     *            is retrieved.
     */
    public HeatmapModel(Collection<? extends GeometryInfo> geomResults, Projection projection,
            DataTypeController dataTypeController, MapManager mapManager)
    {
        myProjection = projection;
        myDataTypeController = dataTypeController;
        myMapManager = mapManager;
        myGeometries = geomResults.stream().map(this::convertGeometry).collect(Collectors.toList());

        myBbox = getBoundingBox(getAllPoints(myGeometries));
        myImageSize = calculateImageSize(myBbox);

        myPixelToCountMap = createMap(myGeometries);
        myPolygons = myGeometries.stream().filter(g -> g instanceof MapPolygonGeometrySupport)
                .map(g -> toPolygon(((MapPolygonGeometrySupport)g).getLocations())).collect(Collectors.toList());
        myPolylines = myGeometries.stream().filter(g -> g instanceof MapPolylineGeometrySupport)
                .map(g -> toPolygon(((MapPolylineGeometrySupport)g).getLocations())).collect(Collectors.toList());
    }

    /**
     * Resets the bounding box and image size based on zoom level, and the
     * visible geometries.
     */
    public void resetBoundingBox()
    {
        myBbox = getBoundingBox(getAllPoints(myGeometries));
        myImageSize = calculateImageSize(myBbox);
    }

    /**
     * Gets the bbox.
     *
     * @return the bbox
     */
    public GeographicBoundingBox getBbox()
    {
        return myBbox;
    }

    /**
     * Gets the image size.
     *
     * @return the image size
     */
    public Dimension getImageSize()
    {
        return myImageSize;
    }

    /**
     * Calculates the maximum pixel count.
     *
     * @return the maximum pixel count
     */
    public int calculateMaxCount()
    {
        return myPixelToCountMap.valueCollection().stream().mapToInt(v -> v.get()).max().orElse(0);
    }

    /**
     * Iterates over the pixels that have counts, supplying the pixel coordinate
     * and count.
     *
     * @param consumer the consumer
     */
    public void forEachValue(BiConsumer<Point, Integer> consumer)
    {
        Point coord = new Point();
        myPixelToCountMap.forEachEntry((int key, MutableInteger value) ->
        {
            updateCoordinate(key, coord);
            int count = value.get();
            consumer.accept(coord, Integer.valueOf(count));
            return true;
        });
    }

    /**
     * Gets the polygons.
     *
     * @return the polygons
     */
    public List<Polygon> getPolygons()
    {
        return myPolygons;
    }

    /**
     * Gets the polylines.
     *
     * @return the polylines
     */
    public List<Polygon> getPolylines()
    {
        return myPolylines;
    }

    /**
     * Converts the geometry as necessary.
     *
     * @param geomInfo the geometry info
     * @return the converted geometry
     */
    private MapGeometrySupport convertGeometry(GeometryInfo geomInfo)
    {
        MapGeometrySupport geom = geomInfo.getGeometry();

        MapGeometrySupport convertedGeom = geom;
        // Don't need to convert lines and polygons
        if (geom instanceof MapLocationGeometrySupport)
        {
            FeatureVisualizationStyle style = getStyle(geom, geomInfo.getDataTypeKey());
            if (HeatmapGeometryUtilities.needsConversion(style))
            {
                DataTypeInfo dataType = myDataTypeController.getDataTypeInfoForType(geomInfo.getDataTypeKey());
                convertedGeom = HeatmapGeometryUtilities.convertGeometry(geomInfo, style, dataType, myProjection);
            }
        }
        return convertedGeom;
    }

    /**
     * Gets the style for the geometry, if any.
     *
     * @param geom the geometry
     * @param dataTypeKey the data type key
     * @return the style, or null
     */
    private FeatureVisualizationStyle getStyle(MapGeometrySupport geom, String dataTypeKey)
    {
        FeatureVisualizationStyle style = null;
        MapDataElementTransformer transformer = myDataTypeController.getTransformerForType(dataTypeKey);
        if (transformer instanceof StyleMapDataElementTransformer)
        {
            StyleMapDataElementTransformer styleTransformer = (StyleMapDataElementTransformer)transformer;
            // should pass the real data element ID here in case feature actions
            // change the geometry type
            style = styleTransformer.getGeometryProcessor().getStyle(geom, -1);
        }
        return style;
    }

    /**
     * Gets all the points of the geometries.
     *
     * @param geometries the geometries
     * @return all points
     */
    private List<LatLonAlt> getAllPoints(Collection<? extends MapGeometrySupport> geometries)
    {
        List<LatLonAlt> locations = New.list();
        for (MapGeometrySupport geom : geometries)
        {
            if (geom instanceof MapLocationGeometrySupport)
            {
                locations.add(((MapLocationGeometrySupport)geom).getLocation());
            }
            else if (geom instanceof MapPathGeometrySupport)
            {
                locations.addAll(((MapPathGeometrySupport)geom).getLocations());
            }
        }
        return locations;
    }

    /**
     * Gets the bounding box for the points.
     *
     * @param points the points
     * @return the bounding box
     */
    private GeographicBoundingBox getBoundingBox(Collection<? extends LatLonAlt> points)
    {
        GeographicBoundingBox bbox = GeographicBoundingBox.getMinimumBoundingBoxLLA(points);

        // adjust the border based on the size and camera elevation:
        final double borderPercent = .05;

        double borderWidth = bbox.getWidth() + borderPercent;
        double borderHeight = bbox.getHeight() + borderPercent;
        LatLonAlt ll = bbox.getLowerLeft().getLatLonAlt();
        LatLonAlt ur = bbox.getUpperRight().getLatLonAlt();
        LatLonAlt newLL = LatLonAlt.createFromDegrees(ll.getLatD() - borderHeight, ll.getLonD() - borderWidth);
        LatLonAlt newUR = LatLonAlt.createFromDegrees(ur.getLatD() + borderHeight, ur.getLonD() + borderWidth);
        bbox = new GeographicBoundingBox(newLL, newUR);
        return bbox;
    }

    /**
     * Calculates the image size.
     *
     * @param bbox the bounding box
     * @return the image size
     */
    public Dimension calculateImageSize(GeographicBoundingBox bbox)
    {
        Dimension size;
        final int maxDimension = 4096;
        double width = bbox.getWidth();
        double height = bbox.getHeight();
        if (width > height)
        {
            size = new Dimension(maxDimension, (int)Math.round(maxDimension * (height / width)));
        }
        else
        {
            size = new Dimension((int)Math.round(maxDimension * (width / height)), maxDimension);
        }
        return size;
    }

    /**
     * Creates the map of pixel location to count.
     *
     * @param geometries the geometries
     * @return the map
     */
    private TIntObjectMap<MutableInteger> createMap(Collection<? extends MapGeometrySupport> geometries)
    {
        TIntObjectMap<MutableInteger> pixelToCountMap = new TIntObjectHashMap<>();

        for (MapGeometrySupport geom : geometries)
        {
            if (geom instanceof MapLocationGeometrySupport)
            {
                LatLonAlt point = ((MapLocationGeometrySupport)geom).getLocation();

                // Get the pixel coordinates
                int x = toX(point.getLonD());
                int y = toY(point.getLatD());

                // Increment the count in the map
                int key = toKey(x, y);
                MutableInteger count = pixelToCountMap.get(key);
                if (count == null)
                {
                    count = new MutableInteger();
                    pixelToCountMap.put(key, count);
                }
                count.increment();
            }
        }

        return pixelToCountMap;
    }

    /**
     * Converts the locations to a java.awt.Polygon.
     *
     * @param locations the locations
     * @return the polygon
     */
    private Polygon toPolygon(Collection<? extends LatLonAlt> locations)
    {
        int[] xpoints = new int[locations.size()];
        int[] ypoints = new int[locations.size()];
        int npoints = 0;
        for (LatLonAlt location : locations)
        {
            xpoints[npoints] = toX(location.getLonD());
            ypoints[npoints] = toY(location.getLatD());
            npoints++;
        }
        return new Polygon(xpoints, ypoints, npoints);
    }

    /**
     * Converts the longitude to a Y coordinate in the image.
     *
     * @param lonD the longitude in degrees
     * @return the Y coordinate
     */
    private int toX(double lonD)
    {
        return (int)Math.round(map(lonD, myBbox.getMinLonD(), myBbox.getMaxLonD(), 0, myImageSize.getWidth()));
    }

    /**
     * Converts the latitude to an X coordinate in the image.
     *
     * @param latD the latitude in degrees
     * @return the X coordinate
     */
    private int toY(double latD)
    {
        return myImageSize.height
                - (int)Math.round(map(latD, myBbox.getMinLatD(), myBbox.getMaxLatD(), 0, myImageSize.getHeight()));
    }

    /**
     * Converts an x/y location to a key for the map.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @return the key
     */
    private int toKey(int x, int y)
    {
        return x << 16 | y;
    }

    /**
     * Converts a map key to a coordinate.
     *
     * @param key the map key
     * @param coord the coordinate to update, reused to avoid object creation
     */
    private void updateCoordinate(int key, Point coord)
    {
        coord.x = key >> 16;
        coord.y = key & 0xFFFF;
    }

    /**
     * Maps a value in a range to a value in another range.
     *
     * @param value the value
     * @param minValue the minimum value
     * @param maxValue the maximum value
     * @param toMin the minimum value of the other range
     * @param toMax the maximum value of the other range
     * @return the mapped value
     */
    private double map(double value, double minValue, double maxValue, double toMin, double toMax)
    {
        double percent = (value - minValue) / (maxValue - minValue);
        return MathUtil.lerp(percent, toMin, toMax);
    }

    /**
     * Gets the value of the {@link #myProjection} field.
     *
     * @return the value stored in the {@link #myProjection} field.
     */
    public Projection getProjection()
    {
        return myProjection;
    }

    /**
     * Gets the value of the {@link #myMapManager} field.
     *
     * @return the value stored in the {@link #myMapManager} field.
     */
    public MapManager getMapManager()
    {
        return myMapManager;
    }
}

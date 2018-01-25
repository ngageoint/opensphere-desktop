package io.opensphere.imagery;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import io.opensphere.core.TimeManager;
import io.opensphere.core.Toolbox;
import io.opensphere.core.geometry.GeometryRegistry;
import io.opensphere.core.geometry.TileGeometry;
import io.opensphere.core.geometry.constraint.Constraints;
import io.opensphere.core.geometry.constraint.TimeConstraint;
import io.opensphere.core.image.DDSImage;
import io.opensphere.core.image.Image;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.model.time.TimeSpanList;

/**
 * This is a helper class for the ExportSvc (q.v.). It performs the tasks of
 * retrieving imagery tiles, synthesizing the overall data image for each data
 * layer, and joining the layers together into a single BufferedImage.
 */
public class ExportHelper
{
    /** An essential substring of the type key for basic map imagery. */
    private static final String MAP_IMG_KEY = "NGA_World_Imagery_2D";

    /** Bla. */
    private Toolbox tools;

    /** Bla. */
    private GeometryRegistry geomReg;

    /** Bla. */
    private TimeManager timeMan;

    /**
     * Pass in a reference to the Toolbox, and grab some other utilities in the
     * process.
     *
     * @param t bla
     */
    public void setTools(Toolbox t)
    {
        tools = t;
        geomReg = tools.getGeometryRegistry();
        timeMan = tools.getTimeManager();
    }

    /**
     * Get the layer image for a single data type. This method is obsolete,
     * though it may still be useful for testing.
     *
     * @param typeKey the requested data type
     * @param gbb the geographic bounds
     * @param zoom the maximum zoom
     * @return an image of the requested data
     */
    public BufferedImage getTileComposite(String typeKey, GeographicBoundingBox gbb, int zoom)
    {
        final TileWalker w = new TileWalker();
        w.reg = geomReg;
        w.man = timeMan;
        w.bounds = gbb;
        w.maxZoom = zoom;
        return compositeImage(w.getTiles(typeKey), gbb);
    }

    /**
     * Get a composite image for each of the listed datatypes representing the
     * specified geographic region and combine them into a single image.
     *
     * @param typeKeys the requested data types
     * @param gbb geographic bounds of the request
     * @param zoom the maximum zoom level allowed.
     * @return the combination of images generated for the specified types
     */
    public BufferedImage stackComposites(List<String> typeKeys, GeographicBoundingBox gbb, int zoom)
    {
        // setup the tile retriever
        final TileWalker w = new TileWalker();
        w.reg = geomReg;
        w.man = timeMan;
        w.bounds = gbb;
        w.maxZoom = zoom;
        // map the tiles by type key, ignoring nulls
        final Map<String, BufferedImage> images = new TreeMap<>();
        for (final String k : typeKeys)
        {
            putNonNull(images, k, compositeImage(w.getTiles(k), gbb));
        }
        // if we got no images (i.e., they were all null), then punt
        if (images.isEmpty())
        {
            return null;
        }
        // choose one for drawing
        final BufferedImage baseImage = selectBase(images);
        // draw all remaining images onto the base
        final Graphics2D g = baseImage.createGraphics();
        for (final BufferedImage im : images.values())
        {
            g.drawImage(im, 0, 0, null);
        }
        g.dispose();
        // return the combined image
        return baseImage;
    }

    /**
     * Technical helper function. For a non-empty map of images, one is chosen
     * to be removed from the map and returned. Specifically, the one so chosen
     * is to be used as a base onto which the others are drawn, creating a
     * combined image. If the opaque map imagery layer is one of those present,
     * it is automatically selected so that all other data may be visible on top
     * of it.
     *
     * @param images images, indexed by data type key
     * @return one image to be used as a base
     */
    private static BufferedImage selectBase(Map<String, BufferedImage> images)
    {
        String key = null;
        for (final Map.Entry<String, BufferedImage> ent : images.entrySet())
        {
            if (key == null || ent.getKey().contains(MAP_IMG_KEY))
            {
                key = ent.getKey();
            }
        }
        return images.remove(key);
    }

    /**
     * Construct an image from a set of image tiles, cropped to contain only
     * imagery from the specified geographic region.
     *
     * @param tiles the component tiles
     * @param gbb bounds of the desired imagery
     * @return the constructed image
     */
    private BufferedImage compositeImage(List<TileImage> tiles, GeographicBoundingBox gbb)
    {
        // No tiles => Punt!
        if (tiles == null || tiles.isEmpty())
        {
            return null;
        }
        // dLat and dLon should be the same for all tiles; pull one example
        final TileImage tRep = tiles.stream().filter(t -> t.img != null).findAny().orElse(null);
        if (tRep == null)
        {
            return null;
        }
        // pixel width and height are also probably the same
        final int pixelW = tRep.img.getWidth();
        final int pixelH = tRep.img.getHeight();

        // find the lat/lon extremes of the bounding box
        final double qLat0 = gbb.getMinLatD();
        final double qLat1 = gbb.getMaxLatD();
        final double qLon0 = gbb.getMinLonD();
        final double qLon1 = gbb.getMaxLonD();

        // predict the tiles that will be in the set
        final double tLat0 = tRep.lat0 + tRep.dLat * Math.floor((qLat0 - tRep.lat0) / tRep.dLat);
        final double tLat1 = tRep.lat0 + tRep.dLat * Math.ceil((qLat1 - tRep.lat0) / tRep.dLat);
        final double tLon0 = tRep.lon0 + tRep.dLon * Math.floor((qLon0 - tRep.lon0) / tRep.dLon);
        final double tLon1 = tRep.lon0 + tRep.dLon * Math.ceil((qLon1 - tRep.lon0) / tRep.dLon);

        // index in the x- and y-directions
        for (final TileImage t : tiles)
        {
            t.index(tLat0, tLon0);
        }

        final int maxY = rint((tLat1 - tLat0) / tRep.dLat) - 1;
        final int cw = pixelW * rint((tLon1 - tLon0) / tRep.dLon);
        final int ch = pixelH * (maxY + 1);

        // define the requested bounds in pixel terms
        final int qx0 = rint(rescale(qLon0, tLon0, tLon1, cw));
        final int qx1 = rint(rescale(qLon1, tLon0, tLon1, cw));
        final int qdx = qx1 - qx0;
        final int qy0 = ch - 1 - rint(rescale(qLat1, tLat0, tLat1, ch));
        final int qy1 = ch - 1 - rint(rescale(qLat0, tLat0, tLat1, ch));
        final int qdy = qy1 - qy0;

        final BufferedImage bim = new BufferedImage(qdx, qdy, BufferedImage.TYPE_INT_ARGB);
        final Graphics2D g = bim.createGraphics();
        // now lay the tiles into the composite image
        // note: y-index is increasing northward, whereas y-pixel increases
        // southward, so y-indices must be reversed
        for (final TileImage t : tiles)
        {
            final int tileX = pixelW * t.xIndex - qx0;
            final int tileY = pixelH * (maxY - t.yIndex) - qy0;
            g.drawImage(t.img, tileX, tileY, null);
        }
        g.dispose();
        return bim;
    }

    /**
     * Maps hither to yonder.
     *
     * @param x domain value
     * @param x0 domain min
     * @param x1 domain max
     * @param len range length
     * @return range value
     */
    private static double rescale(double x, double x0, double x1, double len)
    {
        return len * (x - x0) / (x1 - x0);
    }

    /**
     * Round the argument to the nearest integer and return as a 32-bit int.
     *
     * @param x bla
     * @return bla
     */
    private static int rint(double x)
    {
        return (int)Math.round(x);
    }

    /**
     * An amalgamation of data used by the containing class. Most of the data
     * contained herein is derived from the TileGeometry used to create it.
     */
    private static class TileImage
    {
        /** Zero-based numerical position along x (increases to east). */
        public int xIndex = -1;

        /** Zero-based numerical position along y (increases to north). */
        public int yIndex = -1;

        /** minimum latitude. */
        public double lat0;

        /** extent in latitude. */
        public double dLat;

        /** minimum longitude. */
        public double lon0;

        /** extent in longitude. */
        public double dLon;

        /** The imagery representing this tile. */
        public BufferedImage img;

        /**
         * Factory method for TileImage instances.
         *
         * @param tile bla
         * @return bla
         */
        public static TileImage create(TileGeometry tile)
        {
            final TileImage tim = new TileImage();
            tim.setup(tile);
            return tim;
        }

        /**
         * Extract useful information from a TileGeometry and discard the
         * reference.
         *
         * @param tile bla
         */
        public void setup(TileGeometry tile)
        {
            setGeom((GeographicBoundingBox)tile.getBounds());
            final Image i = tile.getImageManager().getImage();
            if (i == null || i.isBlank())
            {
                return;
            }
            final byte[] bar = getImageBytes(i);
            if (bar == null)
            {
                return;
            }
            img = getImagePixels(bar);
        }

        /**
         * Get useful fields from the GeographicBoundingBox.
         *
         * @param gbb bla
         */
        private void setGeom(GeographicBoundingBox gbb)
        {
            lat0 = gbb.getMinLatD();
            dLat = gbb.getMaxLatD() - lat0;
            lon0 = gbb.getMinLonD();
            dLon = gbb.getMaxLonD() - lon0;
        }

        /**
         * Calculate the numerical x- and y-positions for this tile given the
         * lat/lon origin of the selected region.
         *
         * @param tLat0 minimum latitude
         * @param tLon0 minimum longitude
         */
        public void index(double tLat0, double tLon0)
        {
            xIndex = rint((lon0 - tLon0) / dLon);
            yIndex = rint((lat0 - tLat0) / dLat);
        }
    }

    /**
     * Extract image data from the provided byte array. To be specific, the
     * bytes are ingested as if they were the contents of an image file being
     * read from disk. The result is a BufferedImage or null (in case errors are
     * encountered).
     *
     * @param b array of bytes
     * @return the BufferedImage or null
     */
    private static BufferedImage getImagePixels(byte[] b)
    {
        try
        {
            return ImageIO.read(new ByteArrayInputStream(b));
        }
        catch (final IOException eek)
        {
            eek.printStackTrace();
        }
        return null;
    }

    /**
     * Convert an OpenSphere Image into an array of bytes that can be ingested
     * as a BufferedImage (cf. getImagePixels). The case of DDSImage (a subclass
     * of Image) is handled specially, probably because the java ImageIO class
     * is not able to decode DDS directly. The approach is highly inefficient
     * (cf. DDSImage::toJpg), but it appears to work anyway.
     *
     * @param img the OpenSphere Image
     * @return a byte array or null
     */
    private static byte[] getImageBytes(Image img)
    {
        try
        {
            if (!(img instanceof DDSImage))
            {
                return img.getByteBuffer().array();
            }
            // DDS is a special case, I guess
            return ((DDSImage)img).toJpg();
        }
        catch (final IOException eek)
        {
            eek.printStackTrace();
        }
        return null;
    }

    /**
     * This class manages traversing the tile hierarchy to find the tiles
     * required by a specific request. One instance of this class should be used
     * to request multiple data layers for the same geographic region.
     */
    private static class TileWalker
    {
        /** Bla. */
        private GeometryRegistry reg;

        /** Bla. */
        private TimeManager man;

        /** Geographic bounds for this TileWalker instance. */
        private GeographicBoundingBox bounds;

        /** The maximum zoom level for tiles considered by this TileWalker. */
        private int maxZoom = -1;

        /**
         * Request tiles for the specified type and convert them into TileImage
         * instances for local ease of use.
         *
         * @param typeKey the type of data sought
         * @return A list of TileImage instances or null
         */
        public List<TileImage> getTiles(String typeKey)
        {
            final List<TileGeometry> tiles = getTopGeoms(typeKey);
            if (tiles == null)
            {
                return null;
            }
            return tiles.stream().map(t -> TileImage.create(t)).collect(Collectors.toList());
        }

        /**
         * Mainly delegates scaleToBounds (q.v.), but also calls method
         * clearChildren on all of the returned TileGeometries. Whether the
         * latter step is actually necessary, I do not know--this code was
         * "borrowed" from elsewhere, and it appears to work.
         *
         * @param typeKey the type of imagery to acquire
         * @return the matching tiles (with children cleared) or null
         */
        private List<TileGeometry> getTopGeoms(String typeKey)
        {
            final List<TileGeometry> ret = scaleToBounds(filterGeoms(typeKey));
            if (ret != null)
            {
                for (final TileGeometry tile : ret)
                {
                    tile.clearChildren();
                }
            }
            return ret;
        }

        /**
         * Filter the set of geometries registered with (you guessed it) the
         * GeometryRegistry down to the relevant set. To be relevant, each must
         * be a TileGeometry from the specified layer with geometry and time
         * span intersecting the query bounds and selected time span,
         * respectively.
         *
         * @param typeKey an identifier for the desired layer
         * @return the discovered set of TileGeometry instances
         */
        private List<TileGeometry> filterGeoms(String typeKey)
        {
            final TimeSpanList times = man.getPrimaryActiveTimeSpans();
            final List<TileGeometry> ret = reg.getGeometries().stream().filter(t -> t instanceof TileGeometry)
                    .map(t -> (TileGeometry)t).filter(t -> typeKey.equals(t.getLayerId())).filter(t -> isectSpan(times, t))
                    .filter(t -> intersectsTile(bounds, t)).collect(Collectors.toList());
            return ret;
        }

        /**
         * Test for intersection of time span, with some tolerance for nulls.
         *
         * @param tsl bla
         * @param t bla
         * @return bla
         */
        private static boolean isectSpan(TimeSpanList tsl, TileGeometry t)
        {
            if (t == null)
            {
                return true;
            }
            final Constraints c = t.getConstraints();
            if (c == null)
            {
                return true;
            }
            final TimeConstraint tc = c.getTimeConstraint();
            if (tc == null)
            {
                return true;
            }
            final TimeSpan tsp = tc.getTimeSpan();
            if (tsp == null)
            {
                return true;
            }
            return tsl.intersects(tsp);
        }

        /**
         * From a set of tiles covering the query region at a certain zoom, find
         * the covering tiles at the next level. When this method is called, we
         * assume that the provided tiles all intersect the query region; the
         * return value is a non-empty list of the children of those tiles that
         * intersect the query region.
         *
         * @param tiles some tiles
         * @return some more tiles
         */
        private List<TileGeometry> refine(List<TileGeometry> tiles)
        {
            return tiles.stream().flatMap(t -> t.getChildren(true).stream()).filter(t -> intersectsTile(bounds, t))
                    .collect(Collectors.toList());
        }

        /**
         * Find tiles at the lowest zoom level such that one tile is completely
         * inside of the query region. It works by calling refine (q.v.) on the
         * provided TileGeometries repeatedly until the criterion is met.
         *
         * @param tiles original tiles
         * @return refined tiles
         */
        private List<TileGeometry> scaleToBounds(List<TileGeometry> tiles)
        {
            if (tiles == null || tiles.isEmpty())
            {
                return null;
            }
            int zoom = 0;
            while (!anyInBounds(tiles))
            {
                zoom++;
                if (zoom > maxZoom)
                {
                    return null;
                }
                tiles = refine(tiles);
            }
            return tiles;
        }

        /**
         * See if any of the provided tiles are completely within the resident
         * geographical bounds.
         *
         * @param geoms bla
         * @return bla
         */
        private boolean anyInBounds(List<TileGeometry> geoms)
        {
            for (final TileGeometry g : geoms)
            {
                if (containsTile(bounds, g))
                {
                    return true;
                }
            }
            return false;
        }

        /**
         * Checks for intersection of a tile with a set of bounds; the test
         * automatically fails if the tile bounds are not GBB.
         *
         * @param b bla
         * @param t bla
         * @return bla
         */
        private static boolean intersectsTile(GeographicBoundingBox b, TileGeometry t)
        {
            return t.getBounds() instanceof GeographicBoundingBox && b.intersects((GeographicBoundingBox)t.getBounds());
        }

        /**
         * Checks for containment of a tile within a set of bounds; the test
         * automatically fails if the tile bounds are not GBB.
         *
         * @param b bla
         * @param t bla
         * @return bla
         */
        private static boolean containsTile(GeographicBoundingBox b, TileGeometry t)
        {
            return t.getBounds() instanceof GeographicBoundingBox && b.contains((GeographicBoundingBox)t.getBounds());
        }
    }

    /**
     * Null-tolerant "put" function for maps.
     *
     * @param m the map
     * @param k the key
     * @param v the value
     * @param <K> key type
     * @param <V> value type
     */
    private static <K, V> void putNonNull(Map<K, V> m, K k, V v)
    {
        if (k != null && v != null)
        {
            m.put(k, v);
        }
    }

    // Commentary on the GeoPackage export stuff:

    // the geopackage stuff takes an "ExportModel", which has
    // - output file path
    // - layers (datatypes) to export
    // - zoom level
    // - its relevant features come from a dialog called "ExportOptionsPanel"
    // it creates an "ExportResources", which
    // - marries the ExportModel to a thread pool
    // - botches the encapsulation of the download implementation
    // it then delegates to "doExport" for each exportable data type:
    // - invokes TileWalker::getGeometries with
    // - the type key
    // - the bounds of the request
    // - a callback, which is invoked for each tile visited by the walker:
    // - uses the thread pool of the ExportResources to do its work
    // - obtains the image data
    // - writes the image into a "database", which is really an abstraction
    // for the output file in the ExportModel
    // - one of the functions of the "database" is to keep track of the
    // tiles as they are written out
    // - when all tiles for the type are written, the ExportResources is
    // notified through its CountDownLatch
    // when all of the tiles for all of the datatypes are written, the close
    // method of the ExportResources is called, finishing the export
}

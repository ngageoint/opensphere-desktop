package io.opensphere.core.pipeline.processor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.jogamp.opengl.util.texture.TextureCoords;
import com.vividsolutions.jts.geom.Polygon;

import io.opensphere.core.geometry.TileGeometry;
import io.opensphere.core.math.Matrix3d;
import io.opensphere.core.math.Vector2d;
import io.opensphere.core.math.Vector3d;
import io.opensphere.core.model.GeoScreenBoundingBox;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.GeographicBoxAnchor;
import io.opensphere.core.model.GeographicConvexQuadrilateral;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.ModelBoundingBox;
import io.opensphere.core.model.Quadrilateral;
import io.opensphere.core.model.ScreenBoundingBox;
import io.opensphere.core.model.ScreenPosition;
import io.opensphere.core.model.TesseraList;
import io.opensphere.core.pipeline.cache.CacheProvider;
import io.opensphere.core.pipeline.processor.TileData.TileMeshData;
import io.opensphere.core.projection.AbstractGeographicProjection.GeographicProjectedTesseraVertex;
import io.opensphere.core.projection.Projection;
import io.opensphere.core.util.MathUtil;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.ConcurrentLazyMap;
import io.opensphere.core.util.collections.LazyMap;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.jts.JTSUtilities;
import io.opensphere.core.util.lang.HashCodeHelper;
import io.opensphere.core.viewer.impl.PositionConverter;

/** Utility class to build the TileData. */
public final class TileDataBuilder
{
    /** Texture coordinates that cover a full quad. */
    private static final List<Vector2d> FULL_TEXTURE_COORDS = Collections.unmodifiableList(
            Arrays.asList(new Vector2d(0d, 1d), new Vector2d(0d, 0d), new Vector2d(1d, 0d), new Vector2d(1d, 1d)));

    /**
     * Polygon meshes which come from petrified tessera blocks. These meshes may
     * span multiple projection snapshots; using a weak map ensures that they
     * will not be removed until all associated projection snapshots are no
     * longer in use.
     */
    private static final Map<TesseraList.TesseraBlock<?>, PolygonMeshData> ourPetrifiedMeshes = New.weakMap();

    /** Lock for modification of the tile data map. */
    private static final Lock ourTileDataLock = new ReentrantLock();

    /**
     * Keep track of the tile data which has been produced for a particular
     * bounding box and a particular projection.
     */
    private static final Map<Projection, LazyMap<TileDataKey, TileData>> ourTileDataMap = LazyMap.create(
            New.<Projection, LazyMap<TileDataKey, TileData>>map(), Projection.class,
            key -> new ConcurrentLazyMap<>(
                    Collections.<TileDataKey, TileData>synchronizedMap(New.<TileDataKey, TileData>map()),
                    TileDataKey.class));

    /**
     * Build the tile data for a geometry.
     *
     * @param geom The tile geometry.
     * @param imageTexCoords The image texture coordinates which describe the
     *            portion of the image used by the tile.
     * @param projection Projection which is current for the geometry.
     * @param converter Utility class for converting positions between
     *            coordinate systems.
     * @param cache The cache for the associated processor.
     * @param modelCenter The model coordinate origin of the processor
     *            associated with the given tile.
     * @return The tile data for rendering.
     */
    public static TileData buildTileData(TileGeometry geom, TextureCoords imageTexCoords, final Projection projection,
            final PositionConverter converter, final CacheProvider cache, final Vector3d modelCenter)
    {
        Utilities.checkNull(imageTexCoords, "imageTexCoords");
        if (geom.getBounds() instanceof GeoScreenBoundingBox)
        {
            return buildGeoScreenTileData(imageTexCoords, projection, converter, (GeoScreenBoundingBox)geom.getBounds());
        }
        else if (geom.getBounds() instanceof ScreenBoundingBox)
        {
            return buildTileData(imageTexCoords, converter, (ScreenBoundingBox)geom.getBounds());
        }
        else if (geom.getBounds() instanceof ModelBoundingBox)
        {
            return buildModelTileData(imageTexCoords, converter, (ModelBoundingBox)geom.getBounds());
        }
        else if (geom.getBounds() instanceof GeographicConvexQuadrilateral)
        {
            return buildGeoQuadTileData(imageTexCoords, projection, (GeographicConvexQuadrilateral)geom.getBounds(), modelCenter);
        }
        // TODO what about Screen and model quads?
        else
        {
            LazyMap<TileDataKey, TileData> projectionMap;
            ourTileDataLock.lock();
            try
            {
                projectionMap = ourTileDataMap.get(projection);
            }
            finally
            {
                ourTileDataLock.unlock();
            }
            LazyMap.Factory<TileDataKey, TileData> factory = tdk ->
            {
                Quadrilateral<?> bbox = tdk.getBoundingBox();
                TextureCoords keyTexCoords = tdk.getImageTextureCoords();
                if (bbox instanceof GeographicBoundingBox)
                {
                    return buildTileData(keyTexCoords, projection, (GeographicBoundingBox)bbox, cache, modelCenter);
                }
                return null;
            };

            return projectionMap.get(new TileDataKey(geom.getBounds(), imageTexCoords), factory);
        }
    }

    /**
     * Get the cached {@link TileData} if it's available for a certain bounding
     * box and image texture coordinates.
     *
     * @param bbox The bounding box.
     * @param imageTexCoords The image texture coordinates.
     * @param projection The projection.
     * @return The cached {@link TileData}, or {@code null}.
     */
    public static TileData getCachedTileData(Quadrilateral<?> bbox, TextureCoords imageTexCoords, final Projection projection)
    {
        if (bbox instanceof GeoScreenBoundingBox)
        {
            return null;
        }
        LazyMap<TileDataKey, TileData> projectionMap;
        ourTileDataLock.lock();
        try
        {
            projectionMap = ourTileDataMap.get(projection);
        }
        finally
        {
            ourTileDataLock.unlock();
        }
        return projectionMap.getIfExists(new TileDataKey(bbox, imageTexCoords));
    }

    /**
     * When a projection becomes the active projection, processors should begin
     * requesting data for the new projection. Remove old map entries for
     * generated TileData.
     *
     * @param projection The projection which is now the latest
     */
    public static void setActiveProjection(Projection projection)
    {
        if (projection == null)
        {
            return;
        }
        ourTileDataLock.lock();
        try
        {
            Iterator<Projection> iter = ourTileDataMap.keySet().iterator();
            while (iter.hasNext())
            {
                Projection pj = iter.next();
                if (pj != null && pj.getActivationTimestamp() < projection.getActivationTimestamp())
                {
                    iter.remove();
                }
            }
        }
        finally
        {
            ourTileDataLock.unlock();
        }
    }

    /**
     * Build the tile data for a geographic quadrilateral.
     *
     * @param imageTexCoords The image texture coordinates.
     * @param projection The projection used to generate the mesh which backs
     *            the image.
     * @param quad The quad which defines the bounds of the image.
     * @param modelCenter The model coordinate origin of the processor
     *            associated with the given tile.
     * @return The tile data for rendering.
     */
    private static TileData buildGeoQuadTileData(TextureCoords imageTexCoords, Projection projection,
            GeographicConvexQuadrilateral quad, Vector3d modelCenter)
    {
        Polygon poly = JTSUtilities.createJTSPolygon(quad.getVertices(), null);
        TesseraList<? extends GeographicProjectedTesseraVertex> tesserae = projection.convertPolygonToModelMesh(poly,
                projection.getModelCenter());
        List<TileMeshData> meshes = New.list();

        if (tesserae != null)
        {
            for (TesseraList.TesseraBlock<? extends GeographicProjectedTesseraVertex> block : tesserae.getTesseraBlocks())
            {
                List<? extends GeographicProjectedTesseraVertex> vertices = block.getVertices();
                List<Vector2d> textureCoords = new ArrayList<>(vertices.size());
                handleImageTexCoords(imageTexCoords, quad, vertices, textureCoords);

                List<Vector3d> modelCoords = New.list(vertices.size());
                for (GeographicProjectedTesseraVertex vertex : vertices)
                {
                    modelCoords.add(vertex.getModelCoordinates());
                }

                PolygonMeshData meshData = new PolygonMeshData(modelCoords, null, block.getIndices(), null, null,
                        block.getTesseraVertexCount(), false);
                TileMeshData data = new TileMeshData(meshData, textureCoords);
                meshes.add(data);
            }
        }

        return new TileData(imageTexCoords, meshes, projection.hashCode());
    }

    /**
     * Build the tile data for a geographically attached, screen coordinate
     * located geometry.
     *
     * @param imageTexCoords The image texture coordinates.
     * @param projection Projection which is current for the geometry.
     * @param gsbb The bounding box.
     * @param converter Utility class for converting positions between
     *            coordinate systems.
     * @return The tile data for rendering.
     */
    private static TileData buildGeoScreenTileData(TextureCoords imageTexCoords, Projection projection,
            PositionConverter converter, GeoScreenBoundingBox gsbb)
    {
        GeographicBoxAnchor anchor = gsbb.getAnchor();
        Vector3d windowOffset = converter.convertPositionToWindow(anchor.getGeographicAnchor(), projection);

        double offsetX = windowOffset.getX() - gsbb.getWidth() * anchor.getHorizontalAlignment();
        double offsetY = windowOffset.getY() + gsbb.getHeight() * (1. - anchor.getVerticalAlignment());
        // Text which is not set on an integer pixel renders poorly, so
        // round.
        int anchorOffsetX = anchor.getAnchorOffset() == null ? 0 : anchor.getAnchorOffset().getX();
        int anchorOffsetY = anchor.getAnchorOffset() == null ? 0 : anchor.getAnchorOffset().getY();
        windowOffset = new Vector3d(Math.round(offsetX) + anchorOffsetX, -Math.round(offsetY) + anchorOffsetY, 0.);

        ScreenPosition ul = gsbb.getUpperLeft();
        ScreenPosition ll = gsbb.getLowerLeft();
        ScreenPosition lr = gsbb.getLowerRight();
        ScreenPosition ur = gsbb.getUpperRight();

        // Do not use the position converter since it will adjust negative
        // values to wrap based on the viewport size.
        List<Vector3d> modelCoords = new ArrayList<>(4);
        modelCoords.add(new Vector3d(windowOffset.getX() + ul.getX(), -windowOffset.getY() - ul.getY(), 0.));
        modelCoords.add(new Vector3d(windowOffset.getX() + ll.getX(), -windowOffset.getY() - ll.getY(), 0.));
        modelCoords.add(new Vector3d(windowOffset.getX() + lr.getX(), -windowOffset.getY() - lr.getY(), 0.));
        modelCoords.add(new Vector3d(windowOffset.getX() + ur.getX(), -windowOffset.getY() - ur.getY(), 0.));

        List<Vector2d> textureCoords = new ArrayList<>(4);
        textureCoords.add(new Vector2d(imageTexCoords.left(), imageTexCoords.top()));
        textureCoords.add(new Vector2d(imageTexCoords.left(), imageTexCoords.bottom()));
        textureCoords.add(new Vector2d(imageTexCoords.right(), imageTexCoords.bottom()));
        textureCoords.add(new Vector2d(imageTexCoords.right(), imageTexCoords.top()));

        return new TileData(imageTexCoords, modelCoords, textureCoords, null, 4, projection.hashCode());
    }

    /**
     * Build the tile data for a model coordinate located geometry.
     *
     * @param imageTexCoords The image texture coordinates.
     * @param bb The bounding box.
     * @param converter Utility class for converting positions between
     *            coordinate systems.
     * @return The tile data for rendering.
     */
    // TODO if we want model coordinate based geometries to have a movable model
    // center, they will have to be projection sensitive and use the projection
    // snapshot here.
    private static TileData buildModelTileData(TextureCoords imageTexCoords, PositionConverter converter, ModelBoundingBox bb)
    {
        List<Vector3d> modelCoords = new ArrayList<>(4);
        modelCoords.add(converter.convertPositionToModel(bb.getUpperLeft(), null, Vector3d.ORIGIN));
        modelCoords.add(converter.convertPositionToModel(bb.getLowerLeft(), null, Vector3d.ORIGIN));
        modelCoords.add(converter.convertPositionToModel(bb.getLowerRight(), null, Vector3d.ORIGIN));
        modelCoords.add(converter.convertPositionToModel(bb.getUpperRight(), null, Vector3d.ORIGIN));

        List<Vector2d> textureCoords;
        if (imageTexCoords == null)
        {
            textureCoords = FULL_TEXTURE_COORDS;
        }
        else
        {
            textureCoords = new ArrayList<>(4);
            textureCoords.add(new Vector2d(imageTexCoords.left(), imageTexCoords.bottom()));
            textureCoords.add(new Vector2d(imageTexCoords.right(), imageTexCoords.bottom()));
            textureCoords.add(new Vector2d(imageTexCoords.right(), imageTexCoords.top()));
            textureCoords.add(new Vector2d(imageTexCoords.left(), imageTexCoords.top()));
        }
        return new TileData(imageTexCoords, modelCoords, textureCoords, null, 4, -1);
    }

    /**
     * Build the tile data for a screen coordinate located geometry.
     *
     * @param imageTexCoords The image texture coordinates.
     * @param converter Utility class for converting positions between
     *            coordinate systems.
     * @param sbb The bounding box.
     * @return The tile data for rendering.
     */
    private static TileData buildTileData(TextureCoords imageTexCoords, PositionConverter converter, ScreenBoundingBox sbb)
    {
        List<Vector3d> modelCoords = new ArrayList<>(4);
        if (sbb != null)
        {
            modelCoords.add(converter.convertPositionToModel(sbb.getUpperLeft(), Vector3d.ORIGIN));
            modelCoords.add(converter.convertPositionToModel(sbb.getLowerLeft(), Vector3d.ORIGIN));
            modelCoords.add(converter.convertPositionToModel(sbb.getLowerRight(), Vector3d.ORIGIN));
            modelCoords.add(converter.convertPositionToModel(sbb.getUpperRight(), Vector3d.ORIGIN));
        }

        List<Vector2d> textureCoords = new ArrayList<>(4);
        if (imageTexCoords != null)
        {
            textureCoords.add(new Vector2d(imageTexCoords.left(), imageTexCoords.top()));
            textureCoords.add(new Vector2d(imageTexCoords.left(), imageTexCoords.bottom()));
            textureCoords.add(new Vector2d(imageTexCoords.right(), imageTexCoords.bottom()));
            textureCoords.add(new Vector2d(imageTexCoords.right(), imageTexCoords.top()));
        }

        return new TileData(imageTexCoords, modelCoords, textureCoords, null, 4, -1);
    }

    /**
     * Build the tile data for a geometry.
     *
     * @param imageTexCoords The image texture coordinates.
     * @param gbb The bounding box.
     * @param projection Projection which is current for the geometry.
     * @param cache The cache for the associated processor.
     * @param modelCenter The model coordinate origin of the processor
     *            associated with the given tile.
     * @return The tile data for rendering.
     */
    private static TileData buildTileData(TextureCoords imageTexCoords, Projection projection, GeographicBoundingBox gbb,
            CacheProvider cache, Vector3d modelCenter)
    {
        GeographicPosition lowerLeft = gbb.getLowerLeft();
        GeographicPosition upperRight = gbb.getUpperRight();
        GeographicPosition lowerRight = gbb.getLowerRight();
        GeographicPosition upperLeft = gbb.getUpperLeft();

        TesseraList<? extends GeographicProjectedTesseraVertex> tesserae = projection.convertQuadToModel(lowerLeft, lowerRight,
                upperRight, upperLeft, modelCenter == null ? Vector3d.ORIGIN : modelCenter);
        List<TileMeshData> meshes = new ArrayList<>();

        for (TesseraList.TesseraBlock<? extends GeographicProjectedTesseraVertex> block : tesserae.getTesseraBlocks())
        {
            PolygonMeshData meshData = null;
            if (block.isPetrified())
            {
                meshData = ourPetrifiedMeshes.get(block);
            }

            List<? extends GeographicProjectedTesseraVertex> vertices = block.getVertices();
            List<Vector2d> textureCoords = new ArrayList<>(vertices.size());
            handleImageTexCoords(imageTexCoords, gbb, vertices, textureCoords);

            if (meshData == null)
            {
                List<Vector3d> modelCoords = new ArrayList<>(vertices.size());
                for (GeographicProjectedTesseraVertex vertex : vertices)
                {
                    modelCoords.add(vertex.getModelCoordinates());
                }

                meshData = new PolygonMeshData(modelCoords, null, block.getIndices(), null, null, block.getTesseraVertexCount(),
                        false);
            }
            if (block.isPetrified())
            {
                ourPetrifiedMeshes.put(block, meshData);
            }
            TileMeshData data = new TileMeshData(meshData, textureCoords);
            meshes.add(data);
        }

        return new TileData(imageTexCoords, meshes, projection.hashCode());
    }

    /**
     * Generate the texture coordinates which map to the given vertices.
     *
     * @param imageTexCoords The image texture coordinates.
     * @param gbb The geographic bounding box for the tile.
     * @param vertices The vertices which are within the tile.
     * @param textureCoords The texture coordinate list to populate.
     */
    private static void handleImageTexCoords(TextureCoords imageTexCoords, GeographicBoundingBox gbb,
            List<? extends GeographicProjectedTesseraVertex> vertices, List<Vector2d> textureCoords)
    {
        LatLonAlt lowerLeft = gbb.getLowerLeft().getLatLonAlt();
        LatLonAlt upperRight = gbb.getUpperRight().getLatLonAlt();

        double geoLeft = lowerLeft.getLonD();
        double geoRight = upperRight.getLonD();
        double geoBottom = lowerLeft.getLatD();
        double geoTop = upperRight.getLatD();

        double imageXmin = imageTexCoords.left();
        double imageXmax = imageTexCoords.right();
        // The image may be flipped.
        double imageYmin = Math.min(imageTexCoords.top(), imageTexCoords.bottom());
        double imageYmax = Math.max(imageTexCoords.top(), imageTexCoords.bottom());

        // To find the position in texture coordinates, we divide the geographic
        // offset by the geographic width to get the percentage into the tile,
        // then we multiply by the texture coordinate width to get the texture
        // coordinate offset.
        double xScale = (imageXmax - imageXmin) / (geoRight - geoLeft);
        double yScale = (imageYmax - imageYmin) / (geoTop - geoBottom);

        for (GeographicProjectedTesseraVertex vertex : vertices)
        {
            LatLonAlt lla = vertex.getCoordinates().getLatLonAlt();
            double x = (lla.getLonD() - geoLeft) * xScale + imageXmin;
            double y = (geoTop - lla.getLatD()) * yScale + imageYmin;
            Vector2d texCoords = new Vector2d(x, y);
            textureCoords.add(texCoords);
        }
    }

    /**
     * Generate the texture coordinates which map to the given vertices.
     *
     * @param imageTexCoords The image texture coordinates.
     * @param quad The geographic bounds for the tile.
     * @param vertices The vertices which are within the tile.
     * @param textureCoords The texture coordinate list to populate.
     */
    private static void handleImageTexCoords(TextureCoords imageTexCoords, Quadrilateral<GeographicPosition> quad,
            List<? extends GeographicProjectedTesseraVertex> vertices, List<Vector2d> textureCoords)
    {
        Vector2d ll = quad.getVertices().get(0).getLatLonAlt().asVec2d();
        Vector2d lr = quad.getVertices().get(1).getLatLonAlt().asVec2d();
        Vector2d ur = quad.getVertices().get(2).getLatLonAlt().asVec2d();
        Vector2d ul = quad.getVertices().get(3).getLatLonAlt().asVec2d();

        Matrix3d trans = Matrix3d.getQuadToSquareTransform(ll, lr, ur, ul);

        for (GeographicProjectedTesseraVertex vertex : vertices)
        {
            Vector2d lonLat = vertex.getCoordinates().getLatLonAlt().asVec2d();
            Vector2d transformed = trans.applyPerspectiveTransform(lonLat);
            textureCoords.add(transformed);
        }
    }

    /** Disallow instantiation. */
    private TileDataBuilder()
    {
    }

    /**
     * A key for uniquely determining a tile data to use for one or more
     * geometries.
     */
    private static class TileDataKey
    {
        /** The bounding box. */
        private final Quadrilateral<?> myBoundingBox;

        /** The image texture coordinates. */
        private final TextureCoords myImageTextureCoords;

        /**
         * Constructor.
         *
         * @param boundingBox The bounding box.
         * @param imageTexCoords The image texture coordinates.
         */
        public TileDataKey(Quadrilateral<?> boundingBox, TextureCoords imageTexCoords)
        {
            myBoundingBox = boundingBox;
            myImageTextureCoords = imageTexCoords;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (!(obj instanceof TileDataKey))
            {
                return false;
            }

            TileDataKey otherKey = (TileDataKey)obj;
            if (!myBoundingBox.equals(otherKey.getBoundingBox()))
            {
                return false;
            }

            TextureCoords otherTexCoords = otherKey.getImageTextureCoords();
            return MathUtil.isZero(myImageTextureCoords.left() - otherTexCoords.left())
                    && MathUtil.isZero(myImageTextureCoords.right() - otherTexCoords.right())
                    && MathUtil.isZero(myImageTextureCoords.top() - otherTexCoords.top())
                    && MathUtil.isZero(myImageTextureCoords.bottom() - otherTexCoords.bottom());
        }

        /**
         * Get the boundingBox.
         *
         * @return the boundingBox
         */
        public Quadrilateral<?> getBoundingBox()
        {
            return myBoundingBox;
        }

        /**
         * Get the imageTextureCoords.
         *
         * @return the imageTextureCoords
         */
        public TextureCoords getImageTextureCoords()
        {
            return myImageTextureCoords;
        }

        @Override
        public int hashCode()
        {
            if (myImageTextureCoords == null)
            {
                return myBoundingBox.hashCode();
            }
            return myBoundingBox.hashCode() + HashCodeHelper.getHashCode(myImageTextureCoords.left())
            + HashCodeHelper.getHashCode(myImageTextureCoords.right())
            + HashCodeHelper.getHashCode(myImageTextureCoords.top())
            + HashCodeHelper.getHashCode(myImageTextureCoords.bottom());
        }
    }
}

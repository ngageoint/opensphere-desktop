package io.opensphere.core.pipeline.processor;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import org.apache.log4j.Logger;

import com.jogamp.opengl.util.texture.TextureCoords;

import io.opensphere.core.geometry.AbstractTileGeometry;
import io.opensphere.core.geometry.AbstractTileGeometry.SplitJoinRequestListener;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.geometry.ImageManager;
import io.opensphere.core.geometry.SingletonImageProvider;
import io.opensphere.core.geometry.TileGeometry;
import io.opensphere.core.geometry.renderproperties.DefaultTileRenderProperties;
import io.opensphere.core.geometry.renderproperties.RenderPropertyChangedEvent;
import io.opensphere.core.geometry.renderproperties.TileRenderProperties;
import io.opensphere.core.math.Ellipsoid;
import io.opensphere.core.math.Vector2d;
import io.opensphere.core.math.Vector3d;
import io.opensphere.core.model.BoundingBox;
import io.opensphere.core.model.GeoScreenBoundingBox;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.Position;
import io.opensphere.core.pipeline.cache.CacheProvider;
import io.opensphere.core.pipeline.renderer.AbstractRenderer;
import io.opensphere.core.pipeline.renderer.AbstractRenderer.ModelData;
import io.opensphere.core.pipeline.renderer.AbstractTileRenderer;
import io.opensphere.core.pipeline.renderer.DelegatingRenderer;
import io.opensphere.core.pipeline.renderer.GeometryRenderer;
import io.opensphere.core.pipeline.util.MagnifiedTextureGroup;
import io.opensphere.core.pipeline.util.TextureGroup;
import io.opensphere.core.pipeline.util.TileSplitJoinHelper;
import io.opensphere.core.projection.Projection;
import io.opensphere.core.projection.ProjectionChangedEvent;
import io.opensphere.core.util.TimeBudget;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.CollectionProvider;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.collections.LazyCollectionProvider;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.concurrent.ProcrastinatingExecutor;
import io.opensphere.core.util.concurrent.ThreadedStateMachine;
import io.opensphere.core.util.concurrent.ThreadedStateMachine.StateController;
import io.opensphere.core.viewer.ViewChangeSupport;
import io.opensphere.core.viewer.Viewer;

/**
 * Processor for {@link TileGeometry}s. This class determines the model
 * coordinates and texture coordinates of input geometries and putting them in
 * the cache for use by the renderer.
 */
@SuppressWarnings("PMD.GodClass")
public class TileProcessor extends TextureProcessor<TileGeometry> implements SplitJoinRequestListener
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(TileProcessor.class);

    /** Updater for the split join helper. */
    private static final AtomicReferenceFieldUpdater<TileProcessor, TileSplitJoinHelper> SPLIT_JOIN_HELPER_UPDATER = AtomicReferenceFieldUpdater
            .newUpdater(TileProcessor.class, TileSplitJoinHelper.class, "mySplitJoinHelper");

    /** Executor used to throttle execution of {@link #confirmDeferred()}. */
    private final ProcrastinatingExecutor myConfirmDeferredExecutor;

    /** Flag indicating if I have any geographic-screen geometries. */
    private volatile boolean myHandlingGeoScreenGeometries;

    /** Comparator that determines geometry processing priority. */
    private final Comparator<? super TileGeometry> myPriorityComparator;

    /** Helper class for handling tile splits and joins. */
    private volatile TileSplitJoinHelper mySplitJoinHelper;

    /**
     * Construct a tile processor.
     *
     * @param builder The builder for the processor.
     * @param renderer The renderer for the geometries handled by this
     *            processor.
     */
    public TileProcessor(ProcessorBuilder builder, GeometryRenderer<TileGeometry> renderer)
    {
        super(TileGeometry.class, builder, renderer, 0);

        GeometryRenderer<?> actualRenderer = renderer instanceof DelegatingRenderer
                ? ((DelegatingRenderer<?>)renderer).getRenderer() : renderer;
        if (!(actualRenderer instanceof AbstractTileRenderer))
        {
            throw new IllegalArgumentException("Renderer must be an " + AbstractTileRenderer.class.getSimpleName());
        }

        Utilities.checkNull(builder.getPriorityComparator(), "builder.getPriorityComparator()");
        myPriorityComparator = builder.getPriorityComparator();

        myConfirmDeferredExecutor = new ProcrastinatingExecutor(builder.getFixedPoolExecutorService());
    }

    @Override
    public void close()
    {
        TileSplitJoinHelper splitJoinHelper;
        do
        {
            splitJoinHelper = mySplitJoinHelper;
            if (splitJoinHelper != null)
            {
                splitJoinHelper.stop();
            }
        }
        while (!SPLIT_JOIN_HELPER_UPDATER.compareAndSet(this, splitJoinHelper, null));

        super.close();
    }

    @Override
    public void generateDryRunGeometries()
    {
        TileGeometry.Builder<GeographicPosition> builder = new TileGeometry.Builder<>();
        builder.setBounds(new GeographicBoundingBox(LatLonAlt.createFromDegrees(0, 0), LatLonAlt.createFromDegrees(10, 10)));
        BufferedImage image = new BufferedImage(10, 10, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics g = image.getGraphics();
        g.setColor(Color.GREEN);
        g.fillRect(0, 0, 10, 10);
        builder.setImageManager(new ImageManager(null, new SingletonImageProvider(image)));
        TileRenderProperties renderProperties = new DefaultTileRenderProperties(0, true, true);

        Collection<TileGeometry> geoms = New.collection();
        geoms.add(new TileGeometry(builder, renderProperties, null));
        receiveObjects(this, geoms, Collections.<TileGeometry>emptySet());
    }

    @Override
    public AbstractRenderer.ModelData getModelData(TileGeometry geom, Projection projectionSnapshot,
            AbstractRenderer.ModelData override, TimeBudget timeBudget)
    {
        Lock readLock = getProjectionChangeLock().readLock();
        readLock.lock();
        try
        {
            TextureModelData data;

            // If the input projection snapshot does not match the latest one,
            // do not use the cache for the tile data, because it may contain
            // newer data than what is requested.
            boolean useCache = projectionSnapshot == null || Utilities.sameInstance(projectionSnapshot, getProjectionSnapshot());

            TextureModelData cachedData = getCachedData(geom, override);

            if (useCache)
            {
                data = cachedData;
            }
            else if (cachedData.getTextureGroup() != null)
            {
                // If the cache has a texture, it's okay to use that.
                TileData td = (TileData)(override == null ? null : ((TextureModelData)override).getModelData());
                data = new TextureModelData(td, cachedData.getTextureGroup());
            }
            else if (override == null)
            {
                data = null;
            }
            else
            {
                data = new TextureModelData(((TextureModelData)override).getModelData(),
                        ((TextureModelData)override).getTextureGroup());
            }

            // If any data is missing, try to generate it.
            if (data == null || data.getModelData() == null || data.getTextureGroup() == null && geom.getImageManager() != null)
            {
                data = processGeometry(geom, projectionSnapshot, data, timeBudget);
                // If the tile data is not in the cache and this data was
                // generated using the latest projection, go ahead and cache
                // it.
                if (data != null && data.getModelData() != null && useCache && cachedData.getModelData() == null)
                {
                    // TODO If this is called for backup tiles it sometimes
                    // causes problems.
                    getCache().putCacheAssociation(geom, (TileData)data.getModelData(), TileData.class,
                            ((TileData)data.getModelData()).getSizeBytes(), 0L);
                }
            }

            return data;
        }
        finally
        {
            readLock.unlock();
        }
    }

    @Override
    public void handleProjectionChanged(ProjectionChangedEvent evt)
    {
        if (!sensitiveToProjectionChanges())
        {
            setProjectionSnapshot(null);
            return;
        }

        Lock lock = getProjectionChangeLock().writeLock();
        lock.lock();
        try
        {
            setProjectionSnapshot(evt.getProjectionSnapshot());

            // Clear must come first because super's implementation may trigger
            // drawing to occur.
            if (evt.isFullClear())
            {
                getCache().clearCacheAssociations(TileData.class);
            }
            else
            {
                Collection<AbstractTileGeometry<?>> overlapGeoms = New.collection();
                // Only clear tile data when the geometry overlaps the changed
                // areas.
                for (TileGeometry geom : getGeometries())
                {
                    for (GeographicBoundingBox bounds : evt.getBounds())
                    {
                        geom.getOverlapping(bounds, overlapGeoms);
                    }
                }

                getCache().clearCacheAssociations(overlapGeoms, TileData.class);
            }

            // Clear all magnified tiles since there shouldn't be very many,
            // and it's easier to regenerate them rather than figuring out
            // which ones have tile data that overlap the projection change.
            removeMagnifiedTiles();

            super.handleProjectionChanged(evt);
        }
        finally
        {
            lock.unlock();
        }

        TileSplitJoinHelper splitJoinHelper = mySplitJoinHelper;
        if (splitJoinHelper != null && !AbstractProcessor.isSplitJoinPaused())
        {
            if (evt.isFullClear())
            {
                // Re-split now to avoid starting image requests for
                // tiles that are split too deep for the new projection.
                splitJoinHelper.doSplitsAndJoins(getGeometries());
            }
            else
            {
                // Terrain changes can cause out of view tiles to come into
                // view. Those tiles might need to be split.
                splitJoinHelper.scheduleSplitJoin();
            }
        }
    }

    @Override
    public boolean hasGeometry(Geometry geo)
    {
        return geo instanceof TileGeometry && super.hasGeometry(((TileGeometry)geo).getTopAncestor());
    }

    @Override
    public boolean sensitiveToProjectionChanges()
    {
        return myHandlingGeoScreenGeometries || super.sensitiveToProjectionChanges();
    }

    @Override
    public void splitJoinRequest()
    {
        TileSplitJoinHelper splitJoinHelper = mySplitJoinHelper;
        if (splitJoinHelper != null && !AbstractProcessor.isSplitJoinPaused())
        {
            splitJoinHelper.scheduleSplitJoin();
        }
    }

    /**
     * Adjust the texture coordinates of the image associated with a tile based
     * on the offset and magnification of the tile.
     *
     * @param magnification The magnification factors.
     * @param origin The origin of the image within the source image.
     * @param imageTexCoords The image texture coordinates of the source
     *            texture.
     * @return The transformed texture coordinates.
     */
    public TextureCoords transformTextureCoords(Vector2d magnification, Vector2d origin, TextureCoords imageTexCoords)
    {
        TextureCoords result = imageTexCoords;

        double xOffset = (imageTexCoords.right() - imageTexCoords.left()) * origin.getX();
        double yOffset = (imageTexCoords.top() - imageTexCoords.bottom()) * origin.getY();
        double left = imageTexCoords.left() + xOffset;
        double right = left + (imageTexCoords.right() - imageTexCoords.left()) / magnification.getX();
        double bottom = imageTexCoords.bottom() + yOffset;
        double top = bottom + (imageTexCoords.top() - imageTexCoords.bottom()) / magnification.getY();
        result = new TextureCoords((float)left, (float)bottom, (float)right, (float)top);

        return result;
    }

    @Override
    protected void cacheData(TileGeometry geom, AbstractRenderer.ModelData data)
    {
    }

    @Override
    protected void clearCachedData(Collection<? extends Geometry> geoms)
    {
        getCache().clearCacheAssociations(geoms, TileData.class, Vector3d.class);
        removeMagnifiedTiles(geoms);
    }

    /**
     * Create a tile to use as a magnification of an ancestor of the given tile.
     *
     * @param geom The tile for which a magnification tile is desired.
     * @return The newly created magnification tile.
     */
    protected TileGeometry createMagnificationTile(TileGeometry geom)
    {
        TileGeometry.Builder<Position> builder = geom.createBuilder();
        builder.setParent(null);
        builder.setBounds(geom.getBounds());
        builder.setDivider(null);
        builder.setImageManager(new DummyImageManager());
        return new TileGeometry(builder, geom.getRenderProperties(), geom.getConstraints());
    }

    /**
     * Create a magnified texture for the given geometry using the closest
     * ancestor with an existing texture.
     *
     * @param geom The geometry for which to create a magnified texture.
     * @return the magnified texture.
     */
    protected TextureGroup createMagnifiedTexture(TileGeometry geom)
    {
        TileGeometry ancestor = geom;
        while ((ancestor = (TileGeometry)ancestor.getParent()) != null)
        {
            TextureGroup texture = getCache().getCacheAssociation(ancestor.getImageManager(), TextureGroup.class);
            if (texture != null)
            {
                return createMagnifiedTextureGroup(ancestor, geom, texture);
            }
        }
        return null;
    }

    /**
     * Create the texture group with the appropriate magnification information.
     * This should not be used with a texture group which is already magnified.
     *
     * @param owner The owner of the texture.
     * @param desc The descendant of the owner which requires a magnified
     *            texture.
     * @param texture The unmagnified texture.
     * @return the magnified texture.
     */
    protected TextureGroup createMagnifiedTextureGroup(TileGeometry owner, AbstractTileGeometry<?> desc, TextureGroup texture)
    {
        @SuppressWarnings("unchecked")
        BoundingBox<Position> ownerBoundingBox = (BoundingBox<Position>)owner.getBounds();
        @SuppressWarnings("unchecked")
        BoundingBox<Position> boundingBox = (BoundingBox<Position>)desc.getBounds();

        Vector3d origin = boundingBox.getOffset(ownerBoundingBox);
        Vector2d realOrigin = new Vector2d(origin.getX(), origin.getY());

        double magX = ownerBoundingBox.getWidth() / boundingBox.getWidth();
        double magY = ownerBoundingBox.getHeight() / boundingBox.getHeight();
        Vector2d magnification = new Vector2d(magX, magY);

        TextureCoords magImageTexCoords = transformTextureCoords(magnification, realOrigin, texture.getImageTexCoords());
        magImageTexCoords = new TextureCoords(magImageTexCoords.left(), magImageTexCoords.bottom(), magImageTexCoords.right(),
                magImageTexCoords.top());
        return new MagnifiedTextureGroup(texture.getTextureMap(), magImageTexCoords, magnification, realOrigin);
    }

    /**
     * {@inheritDoc}
     *
     * This implementation extends the one from the superclass by handling tile
     * splitting/joining and backup tile generation.
     */
    @Override
    protected void doDetermineOnscreen(boolean forcePreRender)
    {
        Collection<? extends TileGeometry> ready = getReadyGeometries();
        Collection<? extends TileGeometry> topLevelGeoms = getGeometries();

        Collection<TileGeometry> onscreen = New.collection(ready.size());
        for (TileGeometry geom : topLevelGeoms)
        {
            if (isOnScreen(geom, true))
            {
                onscreen.addAll(getOnscreenCoverage(geom, ready));
            }
        }

        replaceOnscreen(filterOnscreen(onscreen), forcePreRender);
    }

    /**
     * {@inheritDoc}
     *
     * {@link TileProcessor} additionally removes the children of any removed
     * geometries.
     */
    @Override
    protected void doReceiveObjects(Object source, Collection<? extends TileGeometry> adds,
            Collection<? extends Geometry> removes)
    {
        // Determine whether we are handling geo-screen geometries as early as
        // possible.
        synchronized (getGeometrySet())
        {
            myHandlingGeoScreenGeometries = false;
            for (Geometry geom : getGeometrySet())
            {
                if (((TileGeometry)geom).getBounds() instanceof GeoScreenBoundingBox)
                {
                    myHandlingGeoScreenGeometries = true;
                    break;
                }
            }
        }

        // Set the position type before we do splits and joins so that when we
        // get the projection, we will know whether we are projection sensitive.
        if (!adds.isEmpty())
        {
            setPositionType(adds);
        }

        TileSplitJoinHelper splitJoinHelper = mySplitJoinHelper;
        if (splitJoinHelper == null)
        {
            boolean needSplitJoin = false;
            for (TileGeometry geom : adds)
            {
                TileGeometry tile = geom;
                if (tile.isDivisible())
                {
                    needSplitJoin = true;
                    break;
                }
            }
            if (needSplitJoin)
            {
                splitJoinHelper = createSplitJoinHelper();
                SPLIT_JOIN_HELPER_UPDATER.compareAndSet(this, null, splitJoinHelper);
                splitJoinHelper = mySplitJoinHelper;
            }
        }

        Collection<? extends Geometry> removesPlusChildren = splitJoinHelper == null ? removes
                : AbstractTileGeometry.getGeometriesPlusDescendants(removes);
        removeSplitJoinRequestListenersForRemoves(removesPlusChildren);

        Collection<? extends TileGeometry> addsPlusChildren;
        if (splitJoinHelper != null && !adds.isEmpty())
        {
            Set<AbstractTileGeometry<?>> splitAdds = New.set();
            Set<AbstractTileGeometry<?>> splitRemoves = New.set();
            splitJoinHelper.doSplitsAndJoins(adds, splitAdds, splitRemoves);
            splitAdds.addAll(adds);
            addsPlusChildren = CollectionUtilities.filterDowncast(splitAdds, TileGeometry.class);
        }
        else
        {
            addsPlusChildren = adds;
        }

        super.doReceiveObjects(source, addsPlusChildren, removesPlusChildren);
        addSplitJoinRequestListenersForAdds(adds);
    }

    @Override
    protected TileData getCachedModelData(TileGeometry geom, TextureCoords imageTexCoords, ModelData override)
    {
        TileData td = (TileData)(override == null ? null : ((TextureModelData)override).getModelData());
        if (td == null)
        {
            td = getCache().getCacheAssociation(geom, TileData.class);
            if (td == null && imageTexCoords != null)
            {
                td = TileDataBuilder.getCachedTileData(geom.getBounds(), imageTexCoords, getProjectionSnapshot());
            }
        }
        return td == null || !texCoordsEquals(imageTexCoords, td.getImageTextureCoords()) ? null : td;
    }

    /**
     * Get the magnified tile for the for the given tile, creating it if
     * necessary. The magnified texture will also be created if necessary.
     *
     * @param tile The tile for which to create a magnified tile.
     * @return The magnified tile.
     */
    protected TileGeometry getMagnifiedTile(AbstractTileGeometry<?> tile)
    {
        if (tile.getParent() == null)
        {
            return null;
        }
        TileGeometry geom = (TileGeometry)tile;
        TileGeometry magTile = getCache().getCacheAssociation(geom, TileGeometry.class);

        TextureGroup magTextureGroup = null;
        if (magTile == null)
        {
            magTile = createMagnificationTile(geom);
            final TileGeometry old = getCache().putCacheAssociation(geom, magTile, TileGeometry.class, 0L, 0L);
            if (old != null)
            {
                getPickManagerGeometryRemover().add(old);
            }
        }
        else
        {
            magTextureGroup = getCache().getCacheAssociation(magTile, TextureGroup.class);
        }

        if (magTextureGroup == null)
        {
            magTextureGroup = createMagnifiedTexture(geom);
            if (magTextureGroup != null)
            {
                getCache().putCacheAssociation(magTile.getImageManager(), magTextureGroup, TextureGroup.class,
                        magTextureGroup.getSizeBytes(), 0L);
            }
        }

        return magTextureGroup == null ? null : magTile;
    }

    @Override
    protected Comparator<? super TileGeometry> getPriorityComparator()
    {
        return myPriorityComparator;
    }

    @Override
    protected void handleBlankImage(TileGeometry geom)
    {
        if (isImageBlank(geom))
        {
            removeMagnifiedTiles(Collections.singleton(geom));
        }
    }

    @Override
    protected void handleImageReady(TileGeometry geom)
    {
        // Check to make sure that this tile is not orphaned.
        if (!geom.isOrphan())
        {
            super.handleImageReady(geom);
        }
    }

    @Override
    protected void handlePropertyChanged(RenderPropertyChangedEvent evt)
    {
        confirmDeferred();
        super.handlePropertyChanged(evt);
    }

    @Override
    protected void handleTimeSpansChanged()
    {
        super.handleTimeSpansChanged();

        if (isCheckingTimeConstraintsNeeded())
        {
            confirmDeferred();
        }
    }

    @Override
    protected void handleViewChanged(Viewer view, ViewChangeSupport.ViewChangeType type)
    {
        if (myHandlingGeoScreenGeometries)
        {
            handleViewChangedForGeoScreenBoundingBoxes();
        }
        else if (GeographicPosition.class.isAssignableFrom(getPositionType()))
        {
            handleViewChangedForGeographicBoundingBoxes();
        }
        super.handleViewChanged(view, type);
    }

    /**
     * Handle a view change for any geometries with geographic bounding boxes.
     */
    protected void handleViewChangedForGeographicBoundingBoxes()
    {
        Collection<TileGeometry> deferred = doConfirmDeferred();
        Collection<TileGeometry> onscreen = New.collection(getOnscreenDrawableGeometries());
        if (deferred != null)
        {
            onscreen.removeAll(deferred);
        }

        Collection<TileGeometry> toDefer = removeOffscreen(onscreen, New.<TileGeometry>collectionFactory());

        if (toDefer != null)
        {
            // If a tile is already in UNPROCESSED, it cannot be reset to
            // DEFERRED. This is will not be a problem since
            // processUnprocessed(...) will move it to DEFERRED.
            resetState(toDefer, State.DEFERRED);
        }

        TileSplitJoinHelper splitJoinHelper = mySplitJoinHelper;
        if (splitJoinHelper != null && !AbstractProcessor.isSplitJoinPaused())
        {
            splitJoinHelper.scheduleSplitJoin();
        }
    }

    /**
     * Handle a view change for any geometries with geographically attached
     * screen position bounding boxes. These geometries need new model
     * coordinates whenever the view is moved.
     */
    protected void handleViewChangedForGeoScreenBoundingBoxes()
    {
        getCache().clearCacheAssociations(getGeometries(), TileData.class, Vector3d.class);

        Collection<TileGeometry> needsNewModel = null;
        Collection<TileGeometry> onscreen = getReadyGeometries();
        removeOffscreen(onscreen, null);
        for (TileGeometry geom : onscreen)
        {
            if (geom.getBounds() instanceof GeoScreenBoundingBox)
            {
                needsNewModel = CollectionUtilities.lazyAdd(geom, needsNewModel);
            }
        }
        if (needsNewModel != null)
        {
            resetState(needsNewModel, TextureState.TEXTURE_LOADED);
        }
    }

    @Override
    protected boolean isOnScreen(TileGeometry geom, boolean useTime)
    {
        if (!super.isOnScreen(geom, useTime))
        {
            return false;
        }

        boolean inView;
        Object bbox = geom.getBounds();
        if (bbox instanceof GeographicBoundingBox)
        {
            Ellipsoid bounds = getProjectionSnapshot().getBoundingEllipsoid((GeographicBoundingBox)bbox, Vector3d.ORIGIN, false);

            inView = getViewer().isInView(bounds, ELLIPSOID_CULL_COSINE);

            if (LOGGER.isTraceEnabled())
            {
                LOGGER.trace("Tested visibility for geom [" + geom + "]. Ellipsoid is [" + bounds + "]. InView: " + inView);
            }
        }
        else if (bbox instanceof GeoScreenBoundingBox)
        {
            CacheProvider cache = getCache();
            Vector3d position = cache.getCacheAssociation(geom, Vector3d.class);
            if (position == null)
            {
                // Do not offset by the model center since this is used with the
                // viewer which is in the projection's coordinates system.
                position = getProjectionSnapshot().convertToModel(((GeoScreenBoundingBox)bbox).getAnchor().getGeographicAnchor(),
                        Vector3d.ORIGIN);
                if (position == null)
                {
                    return true;
                }
                cache.putCacheAssociation(geom, position, Vector3d.class, Vector3d.SIZE_BYTES, 0L);
            }
            inView = getViewer().isInView(position, 0.) && !isObscured(position);
        }
        else
        {
            inView = true;
        }
        return inView;
    }

    @Override
    protected void processDeferred(Collection<? extends TileGeometry> deferred, StateController<TileGeometry> controller)
    {
        if (deferred != null && !deferred.isEmpty())
        {
            super.processDeferred(deferred, controller);
            getCache().clearCacheAssociations(deferred, TileData.class, Vector3d.class);
            removeMagnifiedTiles(deferred);
        }
    }

    @Override
    protected TextureModelData processGeometry(TileGeometry geom, Projection projectionSnapshot,
            AbstractRenderer.ModelData override, TimeBudget timeBudget)
    {
        TileData tileData = (TileData)(override == null ? null : ((TextureModelData)override).getModelData());
        TextureGroup textureGroup = processTextureForGeometry(geom, override, timeBudget);
        if (textureGroup != null
                && (tileData == null || !texCoordsEquals(tileData.getImageTextureCoords(), textureGroup.getImageTexCoords())))
        {
            TextureCoords imageTexCoords = textureGroup.getImageTexCoords();
            Vector3d modelCenter = getProjectionSnapshot() == null ? Vector3d.ORIGIN : getProjectionSnapshot().getModelCenter();
            tileData = TileDataBuilder.buildTileData(geom, imageTexCoords, projectionSnapshot, getPositionConverter(), getCache(),
                    modelCenter);
        }

        return new TextureModelData(tileData, textureGroup);
    }

    @Override
    protected void processReady(Collection<? extends TileGeometry> ready, StateController<TileGeometry> controller)
    {
        super.processReady(ready, controller);
        removeMagnifiedTiles(ready);
    }

    @Override
    protected void processRemoves(Collection<? extends Geometry> removes)
    {
        if (removes != null && !removes.isEmpty())
        {
            super.processRemoves(removes);

            for (Geometry geometry : removes)
            {
                if (geometry instanceof AbstractTileGeometry)
                {
                    removeObserverFromGeometryChildren(geometry);
                }
            }
            if (sensitiveToProjectionChanges())
            {
                getCache().clearCacheAssociations(removes, TileData.class, Vector3d.class);
            }
            removeMagnifiedTiles(removes);
        }
    }

    @Override
    protected void processTextureLoaded(Collection<? extends TileGeometry> textureLoaded,
            StateController<TileGeometry> controller)
    {
        if (textureLoaded.isEmpty())
        {
            return;
        }
        Collection<TileGeometry> ready = New.collection(textureLoaded.size());
        Collection<TileGeometry> reload = null;

        // Lock the projection so that all the tiles in this block will match.
        Lock readLock = getProjectionChangeLock().readLock();
        readLock.lock();
        try
        {
            Vector3d modelCenter = getProjectionSnapshot() == null ? Vector3d.ORIGIN : getProjectionSnapshot().getModelCenter();
            for (TileGeometry geom : textureLoaded)
            {
                TileData tileData = getCache().getCacheAssociation(geom, TileData.class);
                if (tileData == null)
                {
                    TextureGroup textureGroup = getCache().getCacheAssociation(geom.getImageManager(), TextureGroup.class);
                    TextureCoords imageTexCoords;
                    if (textureGroup == null)
                    {
                        // If the geometry is not drawable, it's okay if
                        // there's no texture: just use the full quad.
                        imageTexCoords = geom.getRenderProperties().isDrawable() ? null : FULL_QUAD;
                    }
                    else
                    {
                        // If there is more than one texture the texture
                        // coordinates need to match, so use the first one
                        // available.
                        imageTexCoords = textureGroup.getImageTexCoords();
                    }
                    if (imageTexCoords != null)
                    {
                        tileData = TileDataBuilder.buildTileData(geom, imageTexCoords, getProjectionSnapshot(),
                                getPositionConverter(), getCache(), modelCenter);
                        if (tileData != null)
                        {
                            getCache().putCacheAssociation(geom, tileData, TileData.class, tileData.getSizeBytes(), 0L);
                            setOnscreenDirty();
                        }
                    }
                }

                if (tileData == null)
                {
                    reload = CollectionUtilities.lazyAdd(geom, reload);
                }
                else
                {
                    ready.add(geom);
                }
            }
        }
        finally
        {
            readLock.unlock();
        }

        if (!ready.isEmpty())
        {
            controller.changeState(ready, State.READY);
        }
        resetState(reload, State.UNPROCESSED);
    }

    @Override
    protected void processUnprocessed(Collection<? extends TileGeometry> unprocessed, StateController<TileGeometry> controller)
    {
        Collection<TileGeometry> defer = New.set();
        List<TileGeometry> unprocessedToProcess = New.list(unprocessed.size());
        for (TileGeometry geom : unprocessed)
        {
            // TODO For now never defer geographic screen tiles.
            if (!isOnScreen(geom, true) && !myHandlingGeoScreenGeometries)
            {
                defer.add(geom);
            }
            else
            {
                unprocessedToProcess.add(geom);
            }

            // If this geometry has a parent, defer processing on the parent.
            if (geom.getParent() != null)
            {
                defer.add((TileGeometry)geom.getParent());
            }
        }

        if (!defer.isEmpty())
        {
            unprocessedToProcess.removeAll(defer);
        }
        if (!unprocessedToProcess.isEmpty())
        {
            Collection<TileGeometry> ready = New.collection();
            processGeometries(unprocessedToProcess, ready, controller);
            if (!ready.isEmpty())
            {
                controller.changeState(ready, State.READY);
            }
        }
        if (!defer.isEmpty())
        {
            controller.changeState(defer, State.DEFERRED);
        }
    }

    /**
     * Remove the observer from all the children of a geometry.
     *
     * @param geometry The geometry.
     */
    protected void removeObserverFromGeometryChildren(Geometry geometry)
    {
        Collection<? extends AbstractTileGeometry<?>> children = ((AbstractTileGeometry<?>)geometry).getChildren(false);
        if (!children.isEmpty())
        {
            for (AbstractTileGeometry<?> child : children)
            {
                removeObserverFromGeometry(child);
                removeObserverFromGeometryChildren(child);
            }
        }
    }

    @Override
    protected void resetState(Collection<? extends TileGeometry> objects, ThreadedStateMachine.State toState)
    {
        if (CollectionUtilities.hasContent(objects))
        {
            Collection<TileGeometry> processorGeometries = getGeometrySet();
            synchronized (processorGeometries)
            {
                Collection<TileGeometry> toReset = New.collection(objects.size());
                Collection<TileGeometry> orphans = null;
                for (TileGeometry geom : objects)
                {
                    if (!geom.isOrphan() && processorGeometries.contains(geom.getTopAncestor()))
                    {
                        toReset.add(geom);
                    }
                    else
                    {
                        orphans = CollectionUtilities.lazyAdd(geom, orphans);
                    }
                }

                super.resetState(toReset, toState);

                processRemoves(orphans);
            }
        }
    }

    @Override
    protected void resetStateDueToProjectionChange()
    {
        // For geo-screen tiles, make sure that the position used for visibility
        // checks is cleared.
        getCache().clearCacheAssociations(getGeometries(), Vector3d.class);
        confirmDeferred();
        super.resetStateDueToProjectionChange();
    }

    @Override
    protected boolean sensitiveToViewChanges()
    {
        return myHandlingGeoScreenGeometries || super.sensitiveToViewChanges();
    }

    /**
     * Adds the split join request listeners for adds.
     *
     * @param adds the adds
     */
    private void addSplitJoinRequestListenersForAdds(Collection<? extends TileGeometry> adds)
    {
        if (adds != null && !adds.isEmpty())
        {
            for (Geometry g : adds)
            {
                if (g instanceof AbstractTileGeometry<?>)
                {
                    AbstractTileGeometry<?> atg = (AbstractTileGeometry<?>)g;
                    if (atg.getSplitJoinRequestProvider() != null)
                    {
                        atg.getSplitJoinRequestProvider().addSplitJoinRequestListener(this);
                    }
                }
            }
        }
    }

    /** Confirm deferred asynchronously, using the executor. */
    private void confirmDeferred()
    {
        myConfirmDeferredExecutor.execute(() -> doConfirmDeferred());
    }

    /**
     * Create the split/join helper.
     *
     * @return The helper.
     */
    private TileSplitJoinHelper createSplitJoinHelper()
    {
        return new TileSplitJoinHelper(getNonGLScheduledExecutor())
        {
            @Override
            protected Collection<? extends TileGeometry> getProcessorGeometries()
            {
                return TileProcessor.this.getGeometries();
            }

            @Override
            protected Projection getProjection()
            {
                return TileProcessor.this.getProjectionSnapshot();
            }

            @Override
            protected Collection<? extends AbstractTileGeometry<?>> getReadyGeometries()
            {
                return TileProcessor.this.getReadyGeometries();
            }

            @Override
            protected Viewer getViewer()
            {
                return TileProcessor.this.getViewer();
            }

            @Override
            protected boolean isInView(AbstractTileGeometry<?> geom)
            {
                return TileProcessor.this.isOnScreen((TileGeometry)geom, false);
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void receiveGeometries(Collection<? extends AbstractTileGeometry<?>> splitAdds,
                    Collection<? extends AbstractTileGeometry<?>> joinRemoves)
            {
                WriteLock lock = getProjectionChangeLock().writeLock();
                lock.lock();
                try
                {
                    // Do the removes first since resetState checks for orphaned
                    // tiles
                    processRemoves(joinRemoves);
                    if (!splitAdds.isEmpty())
                    {
                        // Do not to reset geometries which are already READY.
                        splitAdds.removeAll(getReadyGeometries());
                        resetState((Collection<? extends TileGeometry>)splitAdds, State.UNPROCESSED);
                    }
                }
                finally
                {
                    lock.unlock();
                }
                determineOnscreen();
            }
        };
    }

    /**
     * Check the deferred geometries to verify that they should still be
     * deferred. For geometries which should no longer be deferred, resume
     * processing.
     *
     * @return The geometries which are remaining deferred.
     */
    private Collection<TileGeometry> doConfirmDeferred()
    {
        Collection<TileGeometry> deferred = getStateMachine().getObjectsInState(State.DEFERRED, New.<TileGeometry>setFactory());
        Collection<TileGeometry> toOnscreen = null;
        Collection<TileGeometry> stillDeferred = null;
        if (!deferred.isEmpty())
        {
            for (TileGeometry geom : deferred)
            {
                if (isOnScreen(geom, true))
                {
                    toOnscreen = CollectionUtilities.lazyAdd(geom, toOnscreen);
                }
                else
                {
                    stillDeferred = CollectionUtilities.lazyAdd(geom, stillDeferred, New.<TileGeometry>setFactory());
                }
            }
        }
        if (toOnscreen != null)
        {
            resetState(toOnscreen, State.UNPROCESSED);
        }

        return stillDeferred;
    }

    /**
     * Get the set of children required to cover the on-screen portion of the
     * given tile. This may include a mix of children which are ready and
     * magnified tiles from ancestors which have textures loaded.
     *
     * @param tile The tile for which coverage is desired.
     * @param ready The set of geometries which are ready to render.
     * @return The set of tiles which covers the on-screen portion of the given
     *         tile.
     */
    private Collection<? extends TileGeometry> getOnscreenCoverage(AbstractTileGeometry<?> tile,
            Collection<? extends TileGeometry> ready)
    {
        Collection<TileGeometry> cover = New.collection();

        Collection<? extends AbstractTileGeometry<?>> children = tile.getChildren(false);
        if (children.isEmpty())
        {
            if (ready.contains(tile))
            {
                cover.add((TileGeometry)tile);
                removeMagnifiedTiles(Collections.singleton(tile));
            }
            else if (!isImageBlank(tile))
            {
                TileGeometry magTile = getMagnifiedTile(tile);
                // If no ancestor has a texture loaded, the magnified tile may
                // not be created.
                if (magTile != null)
                {
                    cover.add(magTile);
                }
                else
                {
                    if (LOGGER.isDebugEnabled())
                    {
                        LOGGER.debug("Could not create magnified tile for " + tile);
                    }
                }
            }
        }
        else
        {
            removeMagnifiedTiles(Collections.singleton(tile));
            for (AbstractTileGeometry<?> child : children)
            {
                if (isOnScreen((TileGeometry)child, false))
                {
                    cover.addAll(getOnscreenCoverage(child, ready));
                }
            }
        }

        return cover;
    }

    /**
     * Remove the magnified tiles associated with all geometries.
     */
    private void removeMagnifiedTiles()
    {
        LazyCollectionProvider<TileGeometry> clearedProvider = New.lazyCollectionProvider(New.<TileGeometry>collectionFactory());
        getCache().clearCacheAssociations(TileGeometry.class, clearedProvider);
        getPickManagerGeometryRemover().addAll(clearedProvider.getUnmodifiable());
    }

    /**
     * Remove the magnified tiles associated with some geometries.
     *
     * @param geometries The geometries.
     */
    private void removeMagnifiedTiles(Collection<? extends Geometry> geometries)
    {
        LazyCollectionProvider<TileGeometry> clearedProvider = New.lazyCollectionProvider(New.<TileGeometry>collectionFactory());
        getCache().clearCacheAssociations(geometries, TileGeometry.class, clearedProvider);
        getPickManagerGeometryRemover().addAll(clearedProvider.getUnmodifiable());
    }

    /**
     * Remove the geometries from the input collection that are off-screen and
     * return them in a separate collection.
     *
     * @param geometries The input geometries.
     * @param provider Optional provider to produce the collection to contain
     *            the off-screen geometries.
     * @return The off-screen geometries, or {@code null} if no provider was
     *         provided.
     */
    private Collection<TileGeometry> removeOffscreen(Collection<TileGeometry> geometries,
            CollectionProvider<TileGeometry> provider)
    {
        Collection<TileGeometry> offscreen;
        if (geometries.isEmpty())
        {
            offscreen = provider == null ? null : provider.getEmpty();
        }
        else
        {
            offscreen = null;
            for (Iterator<TileGeometry> iter = geometries.iterator(); iter.hasNext();)
            {
                TileGeometry geom = iter.next();
                if (!isOnScreen(geom, false))
                {
                    if (provider != null)
                    {
                        if (offscreen == null)
                        {
                            offscreen = provider.get();
                        }
                        offscreen.add(geom);
                    }
                    iter.remove();
                }
            }
        }
        return offscreen;
    }

    /**
     * Removes the split join request listeners for removes.
     *
     * @param removesPlusChildren the removes plus children
     */
    private void removeSplitJoinRequestListenersForRemoves(Collection<? extends Geometry> removesPlusChildren)
    {
        if (removesPlusChildren != null && !removesPlusChildren.isEmpty())
        {
            for (Geometry g : removesPlusChildren)
            {
                if (g instanceof AbstractTileGeometry<?>)
                {
                    AbstractTileGeometry<?> atg = (AbstractTileGeometry<?>)g;
                    if (atg.getSplitJoinRequestProvider() != null)
                    {
                        atg.getSplitJoinRequestProvider().removeSplitJoinRequestListener(this);
                    }
                }
            }
        }
    }

    /**
     * Determine if two {@link TextureCoords} objects have equal coordinates.
     *
     * @param tc1 The first texture coordinates.
     * @param tc2 The second texture coordinates.
     * @return {@code true} if the coordinates are equal.
     */
    private boolean texCoordsEquals(TextureCoords tc1, TextureCoords tc2)
    {
        if (tc1 == null || tc2 == null)
        {
            return false;
        }
        return tc1.left() == tc2.left() && tc1.right() == tc2.right() && tc1.top() == tc2.top() && tc1.bottom() == tc2.bottom();
    }

    /**
     * A dummy image manager for magnified tiles.
     */
    private static class DummyImageManager extends ImageManager
    {
        /**
         * Constructor.
         */
        public DummyImageManager()
        {
            super(null, null);
        }

        @Override
        public boolean equals(Object obj)
        {
            return Utilities.sameInstance(this, obj);
        }

        @Override
        public int hashCode()
        {
            return System.identityHashCode(this);
        }
    }
}

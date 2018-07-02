package io.opensphere.core.geometry;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.umd.cs.findbugs.annotations.OverrideMustInvoke;

import io.opensphere.core.geometry.renderproperties.ZOrderRenderProperties;
import io.opensphere.core.model.BoundingBox;
import io.opensphere.core.model.GeoScreenBoundingBox;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.GeographicQuadrilateral;
import io.opensphere.core.model.Position;
import io.opensphere.core.model.Quadrilateral;
import io.opensphere.core.util.ChangeSupport.Callback;
import io.opensphere.core.util.TimeBudget;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.WeakChangeSupport;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.collections.New;

/**
 * A {@link Geometry} that models an image that covers a rectangular area. If
 * this <tt>AbstractTileGeometry</tt> can be split into sub-tiles, it has a
 * {@link io.opensphere.core.geometry.AbstractTileGeometry.Divider} that knows
 * how to do that.
 *
 * @param <E> The type of geometry provided to the geometry observer.
 */
@SuppressWarnings("PMD.GodClass")
public abstract class AbstractTileGeometry<E extends AbstractTileGeometry<? super E>> extends AbstractGeometry
        implements HierarchicalGeometry<AbstractTileGeometry<?>>, ImageProvidingGeometry<E>
{
    /**
     * A cache that stores the divider override data for a specific key so that
     * we can remember the override settings for a particular data type within
     * our session just in case the type is added/removed during the session.
     */
    private static final Map<String, DividerLevelOverrideData> ourDividerOverrideDataCache = New.map();

    /** The bounds that defines where the image is rendered. */
    private final Quadrilateral<? extends Position> myBounds;

    /** The children of this geometry, or <code>null</code> for none. */
    private transient volatile Collection<E> myChildren;

    /**
     * Listeners interested when children are added to this geometry.
     */
    private final List<Runnable> myChildrenListeners = Collections.synchronizedList(New.list());

    /** This holds on to the executor used for requesting data. */
    private final DataRequestAgent myDataRequestAgent = new DataRequestAgent();

    /**
     * The functor that knows how to divide me, or <code>null</code> if I am
     * indivisible.
     */
    private final Divider<? extends Position> myDivider;

    /** The image manager. */
    private final ImageManager myImageManager;

    /**
     * Helper that creates {@link ImageManager.Observer}s for us. This ensures
     * that duplicate observers are not created.
     */
    private final ImageProvidingGeometryHelper<E> myImageProvidingGeometryHelper;

    /**
     * The id of the layer this geometry belongs to.
     */
    private final String myLayerId;

    /** The maximum display size in pixels. */
    private final int myMaximumDisplaySize;

    /** The minimum display size in pixels. */
    private final int myMinimumDisplaySize;

    /** The parent of this tile, or <code>null</code>. */
    private final AbstractTileGeometry<?> myParent;

    /**
     * Aggregate all of the tiles with all of their descendants.
     *
     * @param geometries Tiles to aggregates along with all of their
     *            descendants.
     * @return All of the tiles with all of their descendants.
     */
    public static Set<Geometry> getGeometriesPlusDescendants(Collection<? extends Geometry> geometries)
    {
        Set<Geometry> tilesPlusChildren;
        if (geometries.isEmpty())
        {
            tilesPlusChildren = Collections.emptySet();
        }
        else
        {
            tilesPlusChildren = New.set(geometries);
            for (Geometry geom : geometries)
            {
                if (geom instanceof AbstractTileGeometry)
                {
                    AbstractTileGeometry<?> tile = (AbstractTileGeometry<?>)geom;
                    tile.getDescendants(tilesPlusChildren);
                }
            }
        }
        return tilesPlusChildren;
    }

    /**
     * Construct the geometry.
     *
     * @param builder The builder for the geometry.
     * @param renderProperties The render properties.
     */
    public AbstractTileGeometry(AbstractTileGeometry.Builder<?> builder, ZOrderRenderProperties renderProperties)
    {
        this(builder, renderProperties, null);
    }

    /**
     * Construct the geometry.
     *
     * @param builder The builder for the geometry.
     * @param renderProperties The render properties.
     * @param layerId The id of the layer this geometry belongs to.
     */
    public AbstractTileGeometry(AbstractTileGeometry.Builder<?> builder, ZOrderRenderProperties renderProperties, String layerId)
    {
        super(builder, renderProperties);
        myLayerId = layerId;
        myBounds = Utilities.checkNull(builder.getBounds(), "builder.getBounds()");
        myImageManager = builder.getImageManager();
        myDivider = builder.getDivider();
        myParent = builder.getParent();
        if (myParent != null)
        {
            getDataRequestAgent().setDataRetrieverExecutor(myParent.getDataRequestAgent().getDataRetrieverExecutor());
        }

        myMinimumDisplaySize = builder.getMinimumDisplaySize();
        myMaximumDisplaySize = builder.getMaximumDisplaySize();

        myImageProvidingGeometryHelper = myImageManager == null ? null : new ImageProvidingGeometryHelper<E>(getObservable());
    }

    /**
     * Sets a listener that will be notified when children are added to this
     * tile.
     *
     * @param listener The object wanting notification.
     */
    public void addChildrenListener(Runnable listener)
    {
        myChildrenListeners.add(listener);
    }

    @Override
    public void addObserver(Observer<E> observer)
    {
        if (myImageProvidingGeometryHelper != null)
        {
            myImageManager.addObserver(myImageProvidingGeometryHelper.getObserver(observer));
        }
    }

    /**
     * Determine if all of descendant geometries of this geometry at any
     * generation level are contained in the {@code geometries} sets. If all of
     * the immediate children of the geometry are in {@code geometries}, this
     * returns <code>true</code>. If the immediate children are not all in
     * {@code geometries}, but all of the grand-children of the geometry are,
     * this returns <code>true</code>, and so-on.
     *
     * @param geometries The sets of geometries.
     * @return If the children of the geometry are contained.
     */
    public boolean childrenAreInCollections(Collection<Collection<? extends AbstractTileGeometry<?>>> geometries)
    {
        Collection<? extends AbstractTileGeometry<?>> children = getChildren(false);
        if (children.isEmpty())
        {
            return false;
        }

        for (AbstractTileGeometry<?> child : children)
        {
            boolean found = false;
            for (Collection<? extends AbstractTileGeometry<?>> subGeoms : geometries)
            {
                if (subGeoms.contains(child) || child.childrenAreInCollections(geometries))
                {
                    found = true;
                    break;
                }
            }
            if (!found)
            {
                return false;
            }
        }
        return true;
    }

    /** Remove all children. */
    public void clearChildren()
    {
        Collection<? extends AbstractTileGeometry<?>> children = myChildren;
        myChildren = null;

        if (CollectionUtilities.hasContent(children))
        {
            for (AbstractTileGeometry<?> child : children)
            {
                if (child.getImageManager() != null)
                {
                    child.getImageManager().cancelRequest(true);
                }
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public AbstractTileGeometry<E> clone()
    {
        if (getParent() != null)
        {
            // Avoid problems with orphan tiles.
            throw new UnsupportedOperationException("Cannot clone a geometry with a parent.");
        }
        AbstractTileGeometry<E> clone = (AbstractTileGeometry<E>)super.clone();
        clone.clearChildren();
        return clone;
    }

    @Override
    public abstract AbstractTileGeometry.Builder<Position> createBuilder();

    /**
     * Create a new tile that is identical to this one, except for the bounding
     * box, image key, and divider.
     *
     * @param bbox The bounding box for the new tile.
     * @param imageKey The image key for the new tile.
     * @param divider The divider for the new tile.
     * @return The new tile.
     */
    public abstract AbstractTileGeometry<?> createSubTile(BoundingBox<? extends Position> bbox, Object imageKey,
            Divider<? extends Position> divider);

    /**
     * Create a new tile that is identical to this one, except for the bounding
     * box, image key, divider and image manager.
     *
     * @param bbox The bounding box for the new tile.
     * @param imageKey The image key for the new tile.
     * @param divider The divider for the new tile.
     * @param imageManager The image manager for the new tile.
     * @return The new tile.
     */
    public abstract AbstractTileGeometry<?> createSubTile(BoundingBox<? extends Position> bbox, Object imageKey,
            Divider<? extends Position> divider, ImageManager imageManager);

    /**
     * Get the bounds of this tile.
     *
     * @return the bounds
     */
    public Quadrilateral<? extends Position> getBounds()
    {
        return myBounds;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Collection<E> getChildren(boolean allowDivide)
    {
        Collection<E> children = myChildren;
        if (children == null)
        {
            if (isDivisible())
            {
                if (allowDivide)
                {
                    children = new LinkedList<>();
                    children.addAll((Collection<E>)myDivider.divide(this));
                    myChildren = (Collection<E>)New.unmodifiableCollection(children);
                    for (Runnable childrenListener : myChildrenListeners.toArray(new Runnable[myChildrenListeners.size()]))
                    {
                        childrenListener.run();
                    }
                }
                else
                {
                    children = Collections.emptySet();
                }
            }
            else
            {
                children = Collections.emptySet();
                myChildren = children;
            }
        }
        return children;
    }

    @Override
    public DataRequestAgent getDataRequestAgent()
    {
        return myDataRequestAgent;
    }

    @Override
    public void getDescendants(Collection<? super AbstractTileGeometry<?>> result)
    {
        for (AbstractTileGeometry<?> child : getChildren(false))
        {
            result.add(child);
            child.getDescendants(result);
        }
    }

    @Override
    public int getDivisionHoldGeneration()
    {
        return myDivider == null ? -1 : myDivider.getHoldGeneration();
    }

    @Override
    public int getGeneration()
    {
        int count = 0;
        AbstractTileGeometry<?> geom = this;
        while (geom.getParent() != null)
        {
            count++;
            geom = geom.getParent();
        }
        return count;
    }

    @Override
    public ImageManager getImageManager()
    {
        return myImageManager;
    }

    /**
     * Gets the id of the layer this geometry belongs to.
     *
     * @return The layer id or null if there isn't a layer associated with this
     *         geometry.
     */
    public String getLayerId()
    {
        return myLayerId;
    }

    /**
     * Get the approximate maximum number of pixels this geometry should occupy
     * before it is split. This value is ignored if the geometry is not
     * divisible.
     *
     * @return The number of pixels.
     */
    public int getMaximumDisplaySize()
    {
        return myMaximumDisplaySize;
    }

    /**
     * Get the approximate minimum number of pixels this geometry should occupy
     * before it is joined. This value is ignored if the geometry has no parent.
     *
     * @return The number of pixels.
     */
    public int getMinimumDisplaySize()
    {
        return myMinimumDisplaySize;
    }

    /**
     * Get this tile and any descendants that overlap a bounding box.
     *
     * @param boundingBox The bounding box.
     * @param overlapping The collection to contain the filtered geometries.
     */
    public void getOverlapping(GeographicBoundingBox boundingBox, Collection<? super AbstractTileGeometry<?>> overlapping)
    {
        // TODO handle overlap for generic quads.
        Quadrilateral<? extends Position> bbox = getBounds();
        if (bbox instanceof GeographicBoundingBox && ((GeographicBoundingBox)bbox).overlaps(boundingBox, 0.)
                || bbox instanceof GeoScreenBoundingBox
                        && boundingBox.contains(((GeoScreenBoundingBox)bbox).getAnchor().getGeographicAnchor(), 0.)
                || bbox instanceof GeographicQuadrilateral && ((GeographicQuadrilateral)bbox).overlaps(boundingBox, 0.))
        {
            overlapping.add(this);
            for (AbstractTileGeometry<?> child : getChildren(false))
            {
                child.getOverlapping(boundingBox, overlapping);
            }
        }
    }

    @Override
    public AbstractTileGeometry<?> getParent()
    {
        return myParent;
    }

    @Override
    public Class<? extends Position> getPositionType()
    {
        return getBounds().getPositionType();
    }

    @Override
    public Position getReferencePoint()
    {
        return getBounds().getCenter();
    }

    /**
     * Gets the split join request provider.
     *
     * @return the split join request provider
     */
    public SplitJoinRequestProvider getSplitJoinRequestProvider()
    {
        return myDivider;
    }

    @Override
    public AbstractTileGeometry<?> getTopAncestor()
    {
        AbstractTileGeometry<?> top = this;
        while (top.getParent() != null)
        {
            top = top.getParent();
        }
        return top;
    }

    @Override
    public boolean hasChildren()
    {
        return CollectionUtilities.hasContent(myChildren);
    }

    @Override
    public boolean isDescendant(AbstractTileGeometry<?> geom)
    {
        if (myParent == null)
        {
            return false;
        }
        if (Utilities.sameInstance(geom, myParent))
        {
            return true;
        }
        return myParent.isDescendant(geom);
    }

    @Override
    public boolean isDivisible()
    {
        return myDivider != null;
    }

    @Override
    public boolean isDivisionOverride()
    {
        return myDivider != null && myDivider.isDivisionOverride();
    }

    /**
     * The tile has been orphaned if the parent does not have the tile as one of
     * its children or if one its ancestors has been orphaned.
     *
     * @return true when this tile is an orphan
     */
    public boolean isOrphan()
    {
        if (myParent != null)
        {
            Collection<? extends AbstractTileGeometry<?>> parentsChildren = myParent.getChildren(false);
            if (parentsChildren == null || !parentsChildren.contains(this) || myParent.isOrphan())
            {
                return true;
            }
        }

        return false;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Position> boolean overlaps(BoundingBox<T> boundingBox, double tolerance)
    {
        if (boundingBox.getPositionType() == getPositionType())
        {
            return ((BoundingBox<T>)getBounds()).overlaps(boundingBox, tolerance);
        }
        else
        {
            throw new IllegalArgumentException("Cannot check overlap for different position types: "
                    + boundingBox.getPositionType() + " <> " + getPositionType());
        }
    }

    /**
     * The number of listeners wanting notification when children are added.
     *
     * @return The listener count.
     */
    public int getChildrenListenerCount()
    {
        return myChildrenListeners.size();
    }

    /**
     * Removes the listener.
     *
     * @param listener The listener to remove.
     */
    public void removeChildrenListener(Runnable listener)
    {
        myChildrenListeners.remove(listener);
    }

    @Override
    public void removeObserver(io.opensphere.core.geometry.ImageProvidingGeometry.Observer<E> observer)
    {
        if (myImageProvidingGeometryHelper != null)
        {
            ImageManager.Observer obs = myImageProvidingGeometryHelper.removeObserver(observer);
            if (obs != null)
            {
                myImageManager.removeObserver(obs);
            }
        }
    }

    @Override
    public final void requestImageData()
    {
        requestImageData(null, TimeBudget.INDEFINITE);
    }

    @Override
    public final void requestImageData(Comparator<? super E> comparator, TimeBudget timeBudget)
    {
        if (getImageManager() != null)
        {
            getImageManager().requestImageData(comparator, getObservable(), getDataRequestAgent().getDataRetrieverExecutor(),
                    timeBudget);
        }
    }

    @Override
    public boolean sharesImage()
    {
        return false;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(128);
        sb.append(getClass().getSimpleName()).append(" [").append(getBounds()).append(']');
        return sb.toString();
    }

    @Override
    protected Builder<Position> createRawBuilder()
    {
        return new Builder<Position>();
    }

    @Override
    @SuppressWarnings("unchecked")
    @OverrideMustInvoke
    protected Builder<Position> doCreateBuilder()
    {
        Builder<Position> builder = (Builder<Position>)super.doCreateBuilder();
        builder.setBounds(getBounds());
        builder.setDivider((Divider<Position>)myDivider);
        builder.setImageManager(getImageManager());
        builder.setParent(getParent());
        builder.setMinimumDisplaySize(getMinimumDisplaySize());
        builder.setMaximumDisplaySize(getMaximumDisplaySize());
        return builder;
    }

    /**
     * Get the observable (correctly cast {@code this}) for the image manager
     * observer.
     *
     * @return The observable.
     */
    protected abstract E getObservable();

    /**
     * The Class AbstractDivider.
     *
     * @param <T> the generic type
     */
    public abstract static class AbstractDivider<T extends Position> implements Divider<T>
    {
        /** The Change support. */
        private final WeakChangeSupport<SplitJoinRequestListener> myChangeSupport;

        /** The Divider level override data. */
        private DividerLevelOverrideData myDividerLevelOverrideData;

        /**
         * Instantiates a new abstract divider.
         *
         * @param dividerUniqueKey the divider unique key - a key to lookup the
         *            divider data for the session if the type has been added
         *            and removed we can remember the old state.
         */
        public AbstractDivider(String dividerUniqueKey)
        {
            Utilities.checkNull(dividerUniqueKey, "dividerUniqueKey");
            synchronized (ourDividerOverrideDataCache)
            {
                myDividerLevelOverrideData = ourDividerOverrideDataCache.get(dividerUniqueKey);
                if (myDividerLevelOverrideData == null)
                {
                    myDividerLevelOverrideData = new DividerLevelOverrideData();
                    ourDividerOverrideDataCache.put(dividerUniqueKey, myDividerLevelOverrideData);
                }
            }
            myChangeSupport = new WeakChangeSupport<>();
        }

        @Override
        public void addSplitJoinRequestListener(SplitJoinRequestListener listener)
        {
            myChangeSupport.removeListener(listener);
            myChangeSupport.addListener(listener);
        }

        /**
         * Fire split join change request.
         */
        public void fireSplitJoinChangeRequest()
        {
            myChangeSupport.notifyListeners(new Callback<AbstractTileGeometry.SplitJoinRequestListener>()
            {
                @Override
                public void notify(SplitJoinRequestListener listener)
                {
                    listener.splitJoinRequest();
                }
            });
        }

        @Override
        public int getHoldGeneration()
        {
            return myDividerLevelOverrideData.getHoldGeneration();
        }

        @Override
        public boolean isDivisionOverride()
        {
            return myDividerLevelOverrideData.isDividerOveride();
        }

        @Override
        public void removeSplitJoinRequestListener(SplitJoinRequestListener listener)
        {
            myChangeSupport.removeListener(listener);
        }

        /**
         * Sets the division hold generation.
         *
         * @param gen the new division hold generation
         */
        public void setDivisionHoldGeneration(int gen)
        {
            if (gen != myDividerLevelOverrideData.getHoldGeneration())
            {
                myDividerLevelOverrideData.setHoldGeneration(gen);
                if (myDividerLevelOverrideData.isDividerOveride())
                {
                    fireSplitJoinChangeRequest();
                }
            }
        }

        /**
         * Sets the division override enabled flag ( true to override ).
         *
         * @param enable true to enable the division override.
         */
        public void setDivisionOverrideEnabled(boolean enable)
        {
            if (myDividerLevelOverrideData.isDividerOveride() != enable)
            {
                myDividerLevelOverrideData.setDividerOveride(enable);
                fireSplitJoinChangeRequest();
            }
        }
    }

    /**
     * Builder for the geometry.
     *
     * @param <S> the position type used by this geometry.
     */
    public static class Builder<S extends Position> extends AbstractGeometry.Builder
    {
        /** The bounds that defines where the image is rendered. */
        private Quadrilateral<? extends S> myBounds;

        /**
         * The functor that knows how to divide me, or <code>null</code> if I am
         * indivisible.
         */
        private Divider<S> myDivider;

        /** The image manager. */
        private ImageManager myImageManager;

        /** The maximum display size. */
        private int myMaximumDisplaySize = Integer.MAX_VALUE;

        /** The minimum display size. */
        private int myMinimumDisplaySize;

        /** The parent of this tile, or <code>null</code>. */
        private AbstractTileGeometry<?> myParent;

        /**
         * Accessor for the bounds.
         *
         * @return The bounds.
         */
        public Quadrilateral<? extends S> getBounds()
        {
            return myBounds;
        }

        /**
         * Accessor for the divider.
         *
         * @return The divider.
         */
        public Divider<S> getDivider()
        {
            return myDivider;
        }

        /**
         * Accessor for the imageManager.
         *
         * @return The imageManager.
         */
        public ImageManager getImageManager()
        {
            return myImageManager;
        }

        /**
         * Get the approximate maximum number of pixels this geometry should
         * occupy before it is split. This value is ignored if the geometry is
         * not divisible.
         *
         * @return The maximumDisplaySize.
         */
        public int getMaximumDisplaySize()
        {
            return myMaximumDisplaySize;
        }

        /**
         * Get the approximate minimum number of pixels this geometry should
         * occupy before it is joined. This value is ignored if the geometry has
         * no parent.
         *
         * @return The minimumDisplaySize.
         */
        public int getMinimumDisplaySize()
        {
            return myMinimumDisplaySize;
        }

        /**
         * Accessor for the parent.
         *
         * @return The parent.
         */
        public AbstractTileGeometry<?> getParent()
        {
            return myParent;
        }

        /**
         * Set the bounds of the tile.
         *
         * @param bounds The bounds to set.
         */
        public void setBounds(Quadrilateral<? extends S> bounds)
        {
            myBounds = bounds;
        }

        /**
         * Set the division facility.
         *
         * @param divider The divider to set.
         */
        public void setDivider(Divider<S> divider)
        {
            myDivider = divider;
        }

        /**
         * Mutator for the imageManager.
         *
         * @param imageManager The imageManager to set.
         */
        public void setImageManager(ImageManager imageManager)
        {
            myImageManager = imageManager;
        }

        /**
         * Set the approximate maximum number of pixels this geometry should
         * occupy before it is split. This value is ignored if the geometry is
         * not divisible.
         *
         * @param maximumDisplaySize The maximumDisplaySize to set.
         */
        public void setMaximumDisplaySize(int maximumDisplaySize)
        {
            myMaximumDisplaySize = maximumDisplaySize;
        }

        /**
         * Set the approximate minimum number of pixels this geometry should
         * occupy before it is joined. This value is ignored if the geometry has
         * no parent.
         *
         * @param minimumDisplaySize The minimumDisplaySize to set.
         */
        public void setMinimumDisplaySize(int minimumDisplaySize)
        {
            myMinimumDisplaySize = minimumDisplaySize;
        }

        /**
         * Set the parent of the tile.
         *
         * @param parent The parent to set.
         */
        public void setParent(AbstractTileGeometry<?> parent)
        {
            myParent = parent;
        }
    }

    /**
     * Interface for a functor that knows how to divide a geometry into
     * sub-tiles.
     *
     * @param <T> The type for the image provider.
     */
    public interface Divider<T extends Position> extends SplitJoinRequestProvider
    {
        /**
         * Divide a geometry and return the children.
         *
         * @param parent The geometry being divided.
         * @return The children.
         */
        Collection<AbstractTileGeometry<?>> divide(AbstractTileGeometry<?> parent);

        /**
         * Gets the hold generation.
         *
         * @return the hold generation
         */
        int getHoldGeneration();

        /**
         * Returns true if the divider is being overridden with a specific hold
         * generation.
         *
         * @return true if division override is on.
         */
        boolean isDivisionOverride();
    }

    /**
     * The Class DividerLevelOverrideData.
     */
    public static class DividerLevelOverrideData
    {
        /** The Divider override. */
        private boolean myDividerOverride;

        /** The Hold generation. */
        private int myHoldGeneration;

        /**
         * Instantiates a new divider level override data.
         */
        public DividerLevelOverrideData()
        {
        }

        /**
         * Gets the hold generation.
         *
         * @return the hold generation
         */
        public final int getHoldGeneration()
        {
            return myHoldGeneration;
        }

        /**
         * Checks if is divider override.
         *
         * @return true, if is divider override
         */
        public final boolean isDividerOveride()
        {
            return myDividerOverride;
        }

        /**
         * Sets the divider override.
         *
         * @param dividerOveride the new divider override
         */
        public final void setDividerOveride(boolean dividerOveride)
        {
            myDividerOverride = dividerOveride;
        }

        /**
         * Sets the hold generation.
         *
         * @param holdGeneration the new hold generation
         */
        public final void setHoldGeneration(int holdGeneration)
        {
            myHoldGeneration = holdGeneration;
        }
    }

    /**
     * Listener interface for split/join requests.
     */
    @FunctionalInterface
    public interface SplitJoinRequestListener
    {
        /**
         * Split join request.
         */
        void splitJoinRequest();
    }

    /**
     * The Interface SplitJoinRequestProvider.
     */
    public interface SplitJoinRequestProvider
    {
        /**
         * Adds the split join request listener.
         *
         * @param listener the listener
         */
        void addSplitJoinRequestListener(SplitJoinRequestListener listener);

        /**
         * Removes the split join request listener.
         *
         * @param listener the listener
         */
        void removeSplitJoinRequestListener(SplitJoinRequestListener listener);
    }
}

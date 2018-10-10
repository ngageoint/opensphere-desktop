package io.opensphere.core.projection;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.Executor;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import io.opensphere.core.math.Ellipsoid;
import io.opensphere.core.math.Ray3d;
import io.opensphere.core.math.Vector3d;
import io.opensphere.core.model.Altitude.ReferenceLevel;
import io.opensphere.core.model.BoundingBox;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.order.OrderManager;
import io.opensphere.core.terrain.util.AbsoluteElevationProvider;
import io.opensphere.core.terrain.util.ElevationChangeListener;
import io.opensphere.core.terrain.util.ElevationChangedEvent;
import io.opensphere.core.terrain.util.ElevationChangedEvent.ProviderChangeType;
import io.opensphere.core.terrain.util.ElevationManager;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.concurrent.CommonTimer;
import io.opensphere.core.viewer.ViewChangeSupport;
import io.opensphere.core.viewer.ViewChangeSupport.ViewChangeListener;
import io.opensphere.core.viewer.Viewer;

/**
 * A geographic projection which is mutable. This supports getting an immutable
 * snapshot of the current state.
 */
@SuppressWarnings("PMD.GodClass")
public abstract class MutableGeographicProjection extends AbstractMutableGeographicProjection implements ViewChangeListener
{
    /**
     * The center for eye coordinates which should be used with this projection.
     */
    private boolean myModelCenterDirty;

    /** Listener for changes to the elevation providers. */
    private final ElevationChangeListener myElevationListener = new ElevationChangeListener()
    {
        @Override
        public Collection<GeographicBoundingBox> handleElevationChange(ElevationChangedEvent event)
        {
            return MutableGeographicProjection.this.handleElevationChange(event);
        }
    };

    /** An executor that procrastinates before running tasks. */
    private final Executor myMergeSplitExecutor = CommonTimer.createProcrastinatingExecutor(1000);

    /** The globe model that performs tessellation. */
    private final GeographicProjectionModel myModel;

    /** The bounds for the next projection change event. */
    private final Collection<GeographicBoundingBox> myProjectionChangeBounds = New.collection();

    /**
     * A lock for protecting changes to the projection and the projection change
     * bounds.
     */
    private final Lock myProjectionChangeLock = new ReentrantLock();

    /** An executor that procrastinates before running tasks. */
    private final Executor myProjectionChangeNotificationExecutor = CommonTimer.createProcrastinatingExecutor(200);

    /** The snapshot of the most recent state of the projection. */
    private volatile ImmutableGeographicProjection mySnapshot;

    /**
     * Create a terrain projection which uses the given globe.
     *
     * @param model the globe which backs this projection.
     */
    protected MutableGeographicProjection(GeographicProjectionModel model)
    {
        myModel = model;
        myModel.getCelestialBody().getElevationManager().addElevationChangeListener(myElevationListener);
    }

    /**
     * Generate a snapshot of the current projection state.
     */
    public abstract void generateSnapshot();

    @Override
    public double getDistanceFromModelCenterM(GeographicPosition position)
    {
        return mySnapshot.getDistanceFromModelCenterM(position);
    }

    @Override
    public ElevationManager getElevationManager()
    {
        return myModel.getElevationManager();
    }

    @Override
    public double getElevationOnTerrainM(GeographicPosition position)
    {
        return mySnapshot.getElevationOnTerrainM(position);
    }

    @Override
    public double getMinimumTerrainDistance(Viewer view)
    {
        return mySnapshot.getMinimumTerrainDistance(view);
    }

    /**
     * Get the model.
     *
     * @return the model
     */
    public GeographicProjectionModel getModel()
    {
        return myModel;
    }

    @Override
    public Vector3d getNormalAtPosition(GeographicPosition inPos)
    {
        return mySnapshot.getNormalAtPosition(inPos);
    }

    @Override
    public Projection getSnapshot()
    {
        if (mySnapshot == null)
        {
            generateSnapshot();
        }
        return mySnapshot;
    }

    @Override
    public Vector3d getSurfaceIntersection(Vector3d pointA, Vector3d pointB)
    {
        return mySnapshot.getSurfaceIntersection(pointA, pointB);
    }

    @Override
    public Vector3d getTerrainIntersection(Ray3d ray, Viewer view)
    {
        return mySnapshot.getTerrainIntersection(ray, view);
    }

    @Override
    public Collection<GeographicBoundingBox> handleElevationChange(ElevationChangedEvent event)
    {
        if (AbstractProjection.isTerrainLocked() || event.getChangedProviders().isEmpty())
        {
            return null;
        }

        myProjectionChangeLock.lock();
        try
        {
            if (event.getChangeType() == ProviderChangeType.PROVIDER_ADDED)
            {
                AbsoluteElevationProvider provider = event.getChangedProviders().iterator().next();
                if (provider.petrifiesTerrain())
                {
                    double latD = provider.getBoundingBox().getCenter().getLatLonAlt().getLatD();
                    double lonD = provider.getBoundingBox().getCenter().getLatLonAlt().getLonD();
                    GeographicPosition center = new GeographicPosition(
                            LatLonAlt.createFromDegrees(latD, lonD, ReferenceLevel.ELLIPSOID));
                    Vector3d modelCenter = myModel.getCelestialBodyModelPosition(center, Vector3d.ORIGIN);
                    setModelCenter(modelCenter);
                    setModelCenterLocked(true);
                    myModelCenterDirty = true;
                }
            }
            else if (event.getChangeType() == ProviderChangeType.PROVIDER_REMOVED)
            {
                AbsoluteElevationProvider provider = event.getChangedProviders().iterator().next();
                if (provider.petrifiesTerrain())
                {
                    setModelCenterLocked(false);
                }
            }

            Collection<GeographicBoundingBox> combinedBounds = myModel.handleElevationChange(event);
            updateProjectionBounds(combinedBounds);

            sendProjectionUpdate();

            return combinedBounds;
        }
        finally
        {
            myProjectionChangeLock.unlock();
        }
    }

    @Override
    public Collection<GeographicBoundingBox> handleModelDensityChanged(final int density)
    {
        if (AbstractProjection.isTerrainLocked())
        {
            return null;
        }
        Runnable runner = () ->
        {
            myProjectionChangeLock.lock();
            try
            {
                final Collection<GeographicBoundingBox> box = myModel.handleModelDensityChanged(density);
                if (box != null)
                {
                    updateProjectionBounds(box);
                    sendProjectionUpdate();
                }
            }
            finally
            {
                myProjectionChangeLock.unlock();
            }
        };

        // Terrain updates are comparatively slow, so wait a moment after the
        // user finishes moving the view to update the terrain.
        myMergeSplitExecutor.execute(runner);

        return null;
    }

    @Override
    public boolean isOutsideModel(Vector3d modelCoordinates)
    {
        return mySnapshot.isOutsideModel(modelCoordinates);
    }

    @Override
    public void resetProjection(boolean highAccuracy)
    {
        myProjectionChangeLock.lock();
        try
        {
            myModel.setHighAccuracy(highAccuracy);

            // Generate a new snapshot since the model may have changed.
            generateSnapshot();
            ProjectionChangeSupport.ProjectionChangeListener projectionChangeListener = getProjectionChangeListener();
            if (projectionChangeListener != null)
            {
                projectionChangeListener.projectionChanged(new ProjectionChangedEvent(this, mySnapshot, true));
            }
            myProjectionChangeBounds.clear();
        }
        finally
        {
            myProjectionChangeLock.unlock();
        }
    }

    @Override
    public void useElevationOrderManager(OrderManager elevationOrderManager)
    {
        myModel.getCelestialBody().getElevationManager().useOrderManager(elevationOrderManager);
    }

    @Override
    public void viewChanged(final Viewer view, final ViewChangeSupport.ViewChangeType type)
    {
        if (AbstractProjection.isTerrainLocked())
        {
            return;
        }
        Runnable runner = () ->
        {
            myProjectionChangeLock.lock();
            try
            {
                boolean sendUpdate = verifyModelCenter(view);

                final Collection<GeographicBoundingBox> box = myModel.updateModelForView(view);
                if (box != null)
                {
                    updateProjectionBounds(box);
                    sendUpdate = true;
                }

                if (sendUpdate)
                {
                    sendProjectionUpdate();
                }
            }
            finally
            {
                myProjectionChangeLock.unlock();
            }
        };

        // Terrain updates are comparatively slow, so wait a moment after the
        // user finishes moving the view to update the terrain.
        myMergeSplitExecutor.execute(runner);
    }

    @Override
    protected void cacheEllipsoid(BoundingBox<GeographicPosition> bbox, Ellipsoid ellipsoid)
    {
        // Caching is not supported for mutable projections.
    }

    @Override
    protected Ellipsoid getEllipsoidFromCache(BoundingBox<GeographicPosition> bbox)
    {
        // Caching is not supported for mutable projections.
        return null;
    }

    /**
     * Send a projection change event.
     */
    protected void sendProjectionUpdate()
    {
        myProjectionChangeNotificationExecutor.execute(() ->
        {
            myProjectionChangeLock.lock();
            try
            {
                if (myModelCenterDirty || !myProjectionChangeBounds.isEmpty())
                {
                    generateSnapshot();
                    ProjectionChangeSupport.ProjectionChangeListener projectionChangeListener = getProjectionChangeListener();
                    if (projectionChangeListener != null)
                    {
                        ProjectionChangedEvent evt;
                        if (myModelCenterDirty)
                        {
                            evt = new ProjectionChangedEvent(this, mySnapshot,
                                    Collections.singleton(GeographicBoundingBox.WHOLE_GLOBE));
                        }
                        else
                        {
                            evt = new ProjectionChangedEvent(this, mySnapshot, myProjectionChangeBounds);
                        }
                        projectionChangeListener.projectionChanged(evt);
                    }
                    myProjectionChangeBounds.clear();
                    myModelCenterDirty = false;
                }
            }
            finally
            {
                myProjectionChangeLock.unlock();
            }
        });
    }

    /**
     * Set the snapshot.
     *
     * @param snapshot the snapshot to set
     */
    protected void setSnapshot(ImmutableGeographicProjection snapshot)
    {
        mySnapshot = snapshot;
    }

    /**
     * Update the bounds for the next projection change event. If the bounds
     * already exist, merge these bounds with the existing bounds.
     *
     * @param bounds Changed bounds.
     */
    protected void updateProjectionBounds(Collection<GeographicBoundingBox> bounds)
    {
        myProjectionChangeLock.lock();
        try
        {
            myProjectionChangeBounds.addAll(bounds);
        }
        finally
        {
            myProjectionChangeLock.unlock();
        }
    }

    /**
     * Check whether the model center is still valid given the model distance
     * and threshold for high accuracy.
     *
     * @param view The current viewer.
     * @return True when the model center has been updated as a result of this
     *         call.
     */
    private boolean verifyModelCenter(Viewer view)
    {
        if (HIGH_ACCURACY_ALLOWED && !isModelCenterLocked() && view.supportsAdjustedModelView())
        {
            Vector3d nearestModel = view.getClosestModelPosition();
            if (nearestModel.distance(view.getPosition().getLocation()) > HIGH_ACCURACY_MIN_MODEL_DISTANCE)
            {
                if (!Utilities.sameInstance(getModelCenter(), Vector3d.ORIGIN))
                {
                    setModelCenter(Vector3d.ORIGIN);
                    myModelCenterDirty = true;
                    return true;
                }
            }
            else if (nearestModel.distance(getModelCenter()) > HIGH_ACCURACY_THRESHOLD)
            {
                setModelCenter(nearestModel);
                myModelCenterDirty = true;
                return true;
            }
        }

        return false;
    }
}

package io.opensphere.core.projection;

import java.util.Collection;

import org.apache.log4j.Logger;

import io.opensphere.core.math.Matrix4d;
import io.opensphere.core.math.Vector3d;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.order.OrderManager;
import io.opensphere.core.terrain.util.ElevationChangedEvent;
import io.opensphere.core.util.Utilities;

/**
 * Abstract projection implementation that provides a reference to the current
 * viewer.
 */
public abstract class AbstractProjection implements Projection
{
    /**
     * When true the model center may be moved close to the viewer to increase
     * rendering accuracy.
     */
    public static final boolean HIGH_ACCURACY_ALLOWED = Boolean.getBoolean("opensphere.pipeline.highAccuracy.allowed");

    /**
     * When the viewer is farther than the min distance from the model, high
     * accuracy will not be used even when allowed.
     */
    public static final int HIGH_ACCURACY_MIN_MODEL_DISTANCE = Integer
            .getInteger("opensphere.pipeline.highAccuracy.minModelDistance", 50000).intValue();

    /**
     * When the best model center is more than the threshold from the current
     * model center, the center will be updated to the best location.
     */
    public static final int HIGH_ACCURACY_THRESHOLD = Integer.getInteger("opensphere.pipeline.highAccuracy.threshold", 750000)
            .intValue();

    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(AbstractProjection.class);

    /** Flag to indicate whether terrain changes are allowed. */
    private static boolean ourTerrainLocked;

    /**
     * The time at which this projection was last used as the current
     * projection.
     */
    private long myActivationTimestamp;

    /** The time at which this projection was created. */
    private final long myCreationTimestamp = System.currentTimeMillis();

    /** The origin for the space in which the model coordinates are defined. */
    private Vector3d myModelCenter = Vector3d.ORIGIN;

    /** When true, the model center may not be updated. */
    private boolean myModelCenterLocked;

    /**
     * The adjustment to world eye coordinates to yield eye coordinates based on
     * the model center.
     */
    private Matrix4d myModelViewAdjustment;

    /**
     * This is the listener that will receive any {@link ProjectionChangedEvent}
     * s that I produce.
     */
    private volatile ProjectionChangeSupport.ProjectionChangeListener myProjectionChangeListener;

    /**
     * Get the terrainLocked.
     *
     * @return the terrainLocked
     */
    public static boolean isTerrainLocked()
    {
        return ourTerrainLocked;
    }

    /**
     * Set the terrainLocked.
     *
     * @param lock the terrainLocked to set
     */
    public static void setTerrainLocked(boolean lock)
    {
        ourTerrainLocked = lock;
        if (lock)
        {
            LOGGER.info("Terrain Locked.");
        }
        else
        {
            LOGGER.info("Terrain Unlocked.");
        }
    }

    /** Toggle whether terrain changes are allowed. */
    public static void toggleTerrainLocked()
    {
        setTerrainLocked(!ourTerrainLocked);
    }

    @Override
    public long getActivationTimestamp()
    {
        return myActivationTimestamp;
    }

    @Override
    public long getCreationTimestamp()
    {
        return myCreationTimestamp;
    }

    @Override
    public double getDistanceFromModelCenterM(GeographicPosition position)
    {
        return 0;
    }

    @Override
    public double getElevationOnTerrainM(GeographicPosition position)
    {
        return 0;
    }

    @Override
    public Vector3d getModelCenter()
    {
        return myModelCenter;
    }

    @Override
    public Matrix4d getModelViewAdjustment()
    {
        return myModelViewAdjustment;
    }

    @Override
    public Projection getSnapshot()
    {
        return this;
    }

    @Override
    public Collection<GeographicBoundingBox> handleElevationChange(ElevationChangedEvent event)
    {
        return null;
    }

    @Override
    public Collection<GeographicBoundingBox> handleModelDensityChanged(int density)
    {
        return null;
    }

    /**
     * Get the modelCenterLocked.
     *
     * @return the modelCenterLocked
     */
    public boolean isModelCenterLocked()
    {
        return myModelCenterLocked;
    }

    @Override
    public boolean isOutsideModel(Vector3d modelCoordinates)
    {
        return false;
    }

    @Override
    public void setActivationTimestamp()
    {
        myActivationTimestamp = System.currentTimeMillis();
    }

    /**
     * Set the modelCenter.
     *
     * @param modelCenter the modelCenter to set
     */
    public void setModelCenter(Vector3d modelCenter)
    {
        myModelCenter = modelCenter;
        if (!Utilities.sameInstance(modelCenter, Vector3d.ORIGIN))
        {
            myModelViewAdjustment = new Matrix4d();
            myModelViewAdjustment.setTranslation(myModelCenter);
        }
        else
        {
            myModelViewAdjustment = null;
        }
    }

    /**
     * Set the modelCenterLocked.
     *
     * @param modelCenterLocked the modelCenterLocked to set
     */
    public void setModelCenterLocked(boolean modelCenterLocked)
    {
        if (!myModelCenterLocked)
        {
            myModelCenterLocked = modelCenterLocked;
        }
    }

    /**
     * Set the listener that will receive my change events.
     *
     * @param projectionChangeListener The listener.
     */
    public void setProjectionChangeListener(ProjectionChangeSupport.ProjectionChangeListener projectionChangeListener)
    {
        myProjectionChangeListener = projectionChangeListener;
    }

    /**
     * Use the given order manager for determining the order of elevation
     * providers.
     *
     * @param elevationOrderManager the order manager to use.
     */
    public void useElevationOrderManager(OrderManager elevationOrderManager)
    {
        // Projections which can use elevation should override this method.
    }

    /**
     * Accessor for the receiver of my projection change events. This may be
     * <code>null</code> if this projection is not active.
     *
     * @return The projection change listener.
     */
    protected ProjectionChangeSupport.ProjectionChangeListener getProjectionChangeListener()
    {
        return myProjectionChangeListener;
    }
}

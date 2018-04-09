package io.opensphere.core.pipeline;

import io.opensphere.core.MemoryManager;
import io.opensphere.core.MemoryManager.MemoryListener;
import io.opensphere.core.MemoryManager.Status;
import io.opensphere.core.pipeline.cache.LRUMemoryCache;
import io.opensphere.core.util.Utilities;

/**
 * Memory listener that adjusts the size of the cache based on the current
 * memory state.
 */
public class PipelineMemoryListener implements MemoryListener
{
    /** The cache. */
    private final LRUMemoryCache myCache;

    /**
     * Constructor.
     *
     * @param cache The cache whose size is to be adjusted.
     */
    public PipelineMemoryListener(LRUMemoryCache cache)
    {
        myCache = cache;
        handleMemoryStatusChange(null, MemoryManager.Status.NOMINAL);
    }

    @Override
    public final void handleMemoryStatusChange(Status oldStatus, Status newStatus)
    {
        final double defaultSizeFraction = .25;

        double maxSizeRatio;
        switch (newStatus)
        {
            case NOMINAL:
                maxSizeRatio = Utilities.parseSystemProperty("opensphere.geometryCache.vmNominalSizeRatio", defaultSizeFraction);
                break;
            case WARNING:
                maxSizeRatio = Utilities.parseSystemProperty("opensphere.geometryCache.vmWarningSizeRatio", defaultSizeFraction);
                break;
            case CRITICAL:
                maxSizeRatio = Utilities.parseSystemProperty("opensphere.geometryCache.vmCriticalSizeRatio", defaultSizeFraction);
                break;
            default:
                maxSizeRatio = 0.;
                break;
        }

        final long maxSizeBytesVM = (long)(maxSizeRatio * Runtime.getRuntime().maxMemory());
        final double defaultLowWaterFraction = .7;

        final double lowWaterFraction = Utilities.parseSystemProperty("opensphere.geometryCache.vmLowWaterFraction",
                defaultLowWaterFraction);

        myCache.setLowWaterBytesVM((int)(maxSizeBytesVM * lowWaterFraction));
        myCache.setMaxSizeBytesVM(maxSizeBytesVM);
    }
}

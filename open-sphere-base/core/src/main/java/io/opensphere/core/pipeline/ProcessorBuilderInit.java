package io.opensphere.core.pipeline;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

import io.opensphere.core.Toolbox;
import io.opensphere.core.pipeline.cache.CacheProvider;
import io.opensphere.core.pipeline.processor.LabelOcclusionManager;
import io.opensphere.core.pipeline.processor.ProcessorBuilder;
import io.opensphere.core.pipeline.processor.ProjectionSyncManager;
import io.opensphere.core.pipeline.processor.SpatialTemporalGeometryComparator;
import io.opensphere.core.pipeline.util.PickManager;
import io.opensphere.core.pipeline.util.RepaintListener;
import io.opensphere.core.util.concurrent.FixedThreadPoolExecutor;
import io.opensphere.core.util.lang.NamedThreadFactory;

/** Initializer for the processor builder. */
public final class ProcessorBuilderInit
{
    /**
     * Create a processor builder.
     *
     * @param toolbox The system toolbox.
     * @param cache The geometry cache.
     * @param pickManager The pick manager.
     * @param executorService The fast executor service.
     * @param scheduledExecutorService The scheduled executor service.
     * @param glExecutor The GL executor.
     * @param repaintListener The repaint listener.
     * @return The processor builder.
     */
    public static ProcessorBuilder createProcessorBuilder(Toolbox toolbox, CacheProvider cache, PickManager pickManager,
            ExecutorService executorService, ScheduledExecutorService scheduledExecutorService, GLExecutor glExecutor,
            RepaintListener repaintListener)
    {
        ProcessorBuilder processorBuilder = new ProcessorBuilder();
        processorBuilder.setCache(cache).setMapContext(toolbox.getMapManager()).setPickManager(pickManager);
        processorBuilder.setScheduledExecutorService(scheduledExecutorService);
        processorBuilder
                .setPriorityComparator(new SpatialTemporalGeometryComparator(toolbox.getMapManager(), toolbox.getTimeManager()));
        processorBuilder.setDataRetriever(toolbox.getGeometryRegistry().getDataRetrieverExecutor());
        processorBuilder.setTimeManager(toolbox.getTimeManager());
        processorBuilder.setAnimationManager(toolbox.getAnimationManager());
        processorBuilder.setRepaintListener(repaintListener);
        processorBuilder.setProjectionSyncManager(new ProjectionSyncManager());
        processorBuilder.setLabelOcclusionManager(new LabelOcclusionManager());
        processorBuilder.setExecutorService(executorService);
        processorBuilder.setGLExecutor(glExecutor).setLoadSensitiveExecutor(glExecutor.getLoadSensitiveExecutor(executorService));
        processorBuilder
                .setFixedPoolExecutorService(new FixedThreadPoolExecutor(1, new NamedThreadFactory("Pipeline-FixedPool")));

        return processorBuilder;
    }

    /** Disallow instantiation. */
    private ProcessorBuilderInit()
    {
    }
}

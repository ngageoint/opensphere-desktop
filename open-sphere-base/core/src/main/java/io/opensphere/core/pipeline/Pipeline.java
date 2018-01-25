package io.opensphere.core.pipeline;

import java.awt.Component;
import java.awt.Dimension;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

import io.opensphere.core.Toolbox;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.geometry.GeometryRegistry;
import io.opensphere.core.geometry.RenderingCapabilities;
import io.opensphere.core.messaging.GenericSubscriber;
import io.opensphere.core.pipeline.processor.GeometryProcessor;

/**
 * The pipeline is responsible for managing the rendering thread and drawing the
 * {@link Geometry}s on the screen. It receives the geometries from the
 * {@link GeometryRegistry} and organizes them into groups that can be passed to
 * {@link GeometryProcessor}s for processing and rendering.
 */
public interface Pipeline
{
    /** Perform any required cleanup before shutting down the pipeline. */
    void close();

    /**
     * Get my geometry subscriber.
     *
     * @return The geometry subscriber.
     */
    GenericSubscriber<Geometry> getGeometrySubscriber();

    /**
     * Get the rendering capabilities of the pipeline.
     *
     * @return The rendering capabilities.
     */
    RenderingCapabilities getRenderingCapabilities();

    /**
     * Initialize the pipeline.
     *
     * @param preferredSize The size for the canvas.
     * @param toolbox The toolbox.
     * @param executorService The executor for pipeline activities.
     * @param scheduledExecutorService The scheduled executor service for
     *            pipeline activities.
     *
     * @return The top-level AWT component.
     */
    Component initialize(Dimension preferredSize, Toolbox toolbox, ExecutorService executorService,
            ScheduledExecutorService scheduledExecutorService);

    /**
     * Set the target frame rate for the animator.
     *
     * <ul>
     * <li><tt>-1</tt> indicates that the canvas should be rendered as many
     * times as possible.</li>
     * <li><tt>0</tt> indicates that the canvas should only be rendered when
     * required.</li>
     * <li>Any other number indicates a target number of frames per second.</li>
     * </ul>
     *
     * @param framesPerSecond The frames per second.
     */
    void setFrameRate(int framesPerSecond);
}

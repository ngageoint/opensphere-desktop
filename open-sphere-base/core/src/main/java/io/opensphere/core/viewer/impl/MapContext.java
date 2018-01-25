package io.opensphere.core.viewer.impl;

import java.util.Collection;
import java.util.Map;

import io.opensphere.core.projection.Projection;
import io.opensphere.core.projection.ProjectionChangeSupport;
import io.opensphere.core.viewer.ViewChangeSupport;
import io.opensphere.core.viewer.Viewer;

/**
 * A map context that geometries are drawn within. This describes the viewers
 * and projections used to display the geometries.
 *
 * @param <T> The type for the standard viewer.
 */
public interface MapContext<T extends Viewer>
{
    /** Close the map context. */
    void close();

    /**
     * Get the control translators for all projections, used for translating a
     * control event to a command to the viewer.
     *
     * @return The translators. This collection may be empty when the context
     *         supports controls, but none current exist or {@code null} for
     *         contexts with no controls.
     */
    Collection<ViewControlTranslator> getAllControlTranslators();

    /**
     * Get the current control translator, used for translating a control event
     * to a command to the current viewer.
     *
     * @return The translator or {@code null} for contexts with no controls.
     */
    ViewControlTranslator getCurrentControlTranslator();

    /**
     * Access the support for draw enable/disable notification.
     *
     * @return The draw-enable support.
     */
    DrawEnableSupport getDrawEnableSupport();

    /**
     * Get the current projection, used for transforming geographic coordinates
     * to/from model coordinates. This may be <code>null</code> if geographic
     * components are not supported by this viewer set.
     *
     * @return The projection.
     */
    Projection getProjection();

    /**
     * Get the projection based on a 2D or 3D viewer.
     *
     * @param viewerType the viewer type for which the projection is desired
     * @return the projection which is used with the given viewer type.
     */
    Projection getProjection(Class<? extends AbstractDynamicViewer> viewerType);

    /**
     * Get the change support for projection changes.
     *
     * @return The projection change support.
     */
    ProjectionChangeSupport getProjectionChangeSupport();

    /**
     * Get the projections.
     *
     * @return the projections
     */
    Map<Projection, Class<? extends AbstractDynamicViewer>> getProjections();

    /**
     * Get the current projection, used for transforming geographic coordinates
     * to/from model coordinates. This may be <code>null</code> if geographic
     * components are not supported by this viewer set.
     *
     * @return The projection.
     */
    Projection getRawProjection();

    /**
     * Get the 2-D viewer used for overlay components.
     *
     * @return The screen viewer.
     */
    ScreenViewer getScreenViewer();

    /**
     * Get the standard viewer for geometries, used for transforming from model
     * coordinates to window coordinates and back.
     *
     * @return The standard viewer.
     */
    T getStandardViewer();

    /**
     * Get the change support for view changes.
     *
     * @return The view change support.
     */
    ViewChangeSupport getViewChangeSupport();

    /**
     * Gets the viewer type for this projection.
     *
     * @param proj The projection.
     * @return The viewer type that is compatible with the projection.
     */
    Class<? extends AbstractDynamicViewer> getViewerTypeForProjection(Projection proj);

    /**
     * Reshape my viewers.
     *
     * @param width The viewport width.
     * @param height The viewport height.
     */
    void reshape(int width, int height);

    /**
     * Sets the projection based on a 2D or 3D viewer.
     *
     * @param viewerType the viewer type for which the associated projection
     *            should be set.
     */
    void setProjection(Class<? extends AbstractDynamicViewer> viewerType);

    /**
     * Listener interface for commands to start or stop drawing.
     */
    @FunctionalInterface
    public interface DrawEnableListener
    {
        /**
         * Method called when drawing is enabled or disabled.
         *
         * @param flag <code>true</code> if drawing is enabled.
         */
        void drawEnabled(boolean flag);
    }
}

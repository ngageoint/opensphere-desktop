package io.opensphere.search.controller;

import java.util.Collection;

import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.viewer.impl.DynamicViewer;
import io.opensphere.core.viewer.impl.ViewerAnimator;

/**
 * Creates a view animator.
 */
public class ViewerAnimatorCreator
{
    /**
     * Construct the animator.
     *
     * @param viewer The viewer to manipulate.
     * @param points the points which will be used to determine the center of
     *            the viewer destination.
     * @param zoom When true, zoom the destination to fit the points.
     * @return A new {@link ViewerAnimator}.
     */
    public ViewerAnimator createAnimator(DynamicViewer viewer, Collection<?> points, boolean zoom)
    {
        return new ViewerAnimator(viewer, points, zoom);
    }

    /**
     * Construct the animator.
     *
     * @param viewer The viewer to manipulate.
     * @param destination The destination for the viewer in geographic
     *            coordinates.
     * @return A new {@link ViewerAnimator}.
     */
    public ViewerAnimator createAnimator(DynamicViewer viewer, GeographicPosition destination)
    {
        return new ViewerAnimator(viewer, destination);
    }
}

package io.opensphere.mantle.mappoint.impl;

import io.opensphere.core.callout.Callout;

/**
 * CalloutDragListener.
 */
@FunctionalInterface
public interface CalloutDragListener
{
    /**
     * Call out dragged.
     *
     * @param callOut the call out
     * @param xOffset the x offset as a result of the drag.
     * @param yOffset the y offset as a result of the drag.
     */
    void callOutDragged(Callout callOut, int xOffset, int yOffset);
}

package io.opensphere.overlay;

import java.awt.event.MouseEvent;
import java.util.List;

import io.opensphere.core.control.action.MenuOptionListener;
import io.opensphere.core.geometry.PolygonGeometry;

/**
 * Interface for things that handle region selections.
 */
public interface SelectionHandler
{
    /**
     * Adds the menu option listener.
     *
     * @param listener the listener
     */
    void addMenuOptionListener(MenuOptionListener listener);

    /**
     * Removes the menu option listener.
     *
     * @param listener the listener
     */
    void removeMenuOptionListener(MenuOptionListener listener);

    /**
     * Completes a region.
     *
     * @param mouseEvent The mouse event at completion.
     * @param context The context to notify.
     * @param geometries The geometries of the region.
     */
    void selectionRegionCompleted(MouseEvent mouseEvent, String context, List<PolygonGeometry> geometries);

    /**
     * Set a description of the current selection region, which may be
     * {@code null}.
     *
     * @param desc The description of the region for logging.
     */
    void setSelectionRegionDescription(String desc);
}

package io.opensphere.controlpanels.component.map.overlay;

import java.awt.Graphics;

/**
 * Used to interface with overlays to draw images within the map.
 */
public interface Overlay
{
    /**
     * Stops the overlay from responding to model events.
     */
    void close();

    /**
     * Draws the map background.
     *
     * @param graphics The graphics to draw to.
     */
    void draw(Graphics graphics);
}

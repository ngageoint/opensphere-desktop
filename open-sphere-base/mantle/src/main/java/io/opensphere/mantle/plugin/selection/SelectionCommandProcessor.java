package io.opensphere.mantle.plugin.selection;

import java.util.Collection;

import io.opensphere.core.geometry.PolygonGeometry;

/**
 * The Interface SelectionCommandProcessor.
 */
@FunctionalInterface
public interface SelectionCommandProcessor
{
    /**
     * Notifies a processor that a selection command occurred.
     *
     * @param bounds the bounds
     * @param cmd the command.
     */
    void selectionOccurred(Collection<? extends PolygonGeometry> bounds, SelectionCommand cmd);
}

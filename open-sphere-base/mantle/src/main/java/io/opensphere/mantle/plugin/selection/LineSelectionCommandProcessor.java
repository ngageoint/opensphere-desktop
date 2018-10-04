package io.opensphere.mantle.plugin.selection;

import java.util.Collection;

import io.opensphere.core.geometry.PolylineGeometry;

/**
 * The Interface SelectionCommandProcessor.
 */
@FunctionalInterface
public interface LineSelectionCommandProcessor
{
    /**
     * Notifies a processor that a selection command occurred.
     *
     * @param lines the polylines that were selected.
     * @param cmd the command.
     */
    void selectionOccurred(Collection<? extends PolylineGeometry> lines, SelectionCommand cmd);
}

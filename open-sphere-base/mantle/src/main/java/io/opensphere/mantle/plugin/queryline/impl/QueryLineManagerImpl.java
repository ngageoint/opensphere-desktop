package io.opensphere.mantle.plugin.queryline.impl;

import java.util.Collection;

import io.opensphere.core.Toolbox;
import io.opensphere.core.geometry.PolylineGeometry;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.mantle.plugin.queryline.QueryLineManager;
import io.opensphere.mantle.plugin.selection.BufferRegionCreator;
import io.opensphere.mantle.plugin.selection.SelectionCommand;

/**
 * Default implementation of a Query line manager.
 */
public class QueryLineManagerImpl implements QueryLineManager
{
    /** The buffer region creator **/
    private final BufferRegionCreator myBufferRegionCreator;

    /**
     * Creates a new manager instance using the supplied toolbox.
     * 
     * @param toolbox the toolbox through which application state is accessed.
     */
    public QueryLineManagerImpl(Toolbox toolbox)
    {
        myBufferRegionCreator = new BufferRegionCreator(toolbox);
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.plugin.selection.LineSelectionCommandProcessor#selectionOccurred(java.util.Collection,
     *      io.opensphere.mantle.plugin.selection.SelectionCommand)
     */
    @Override
    public void selectionOccurred(Collection<? extends PolylineGeometry> lines, SelectionCommand cmd)
    {
        switch (cmd)
        {
            case CREATE_BUFFER_REGION:
                lines.forEach(l -> EventQueueUtilities.runOnEDT(() -> myBufferRegionCreator.createBuffer(l)));
                break;
            default:
                break;
        }
    }
}

package io.opensphere.core.appl;

import javax.swing.TransferHandler;

import io.opensphere.core.Toolbox;
import io.opensphere.core.event.ImportDataEvent;

/**
 * Transfer handler that sends an import data event.
 */
public class EventTransferHandler extends TransferHandler
{
    /** The serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The toolbox. */
    private final Toolbox myToolbox;

    /**
     * Constructor.
     *
     * @param toolbox The toolbox
     */
    public EventTransferHandler(Toolbox toolbox)
    {
        myToolbox = toolbox;
    }

    @Override
    public boolean canImport(TransferSupport support)
    {
        return ImportDataEvent.canImport(support);
    }

    @Override
    public boolean importData(TransferSupport support)
    {
        ImportDataEvent event = new ImportDataEvent(support);
        if (event.getTransferData() != null)
        {
            myToolbox.getEventManager().publishEvent(event);
            return true;
        }
        return false;
    }
}

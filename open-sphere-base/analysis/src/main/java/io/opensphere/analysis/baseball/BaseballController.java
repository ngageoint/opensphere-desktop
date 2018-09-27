package io.opensphere.analysis.baseball;

import javax.swing.WindowConstants;

import io.opensphere.analysis.util.MGRSUtilities;
import io.opensphere.core.Toolbox;
import io.opensphere.core.event.EventListenerService;
import io.opensphere.core.util.ThreadConfined;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.mantle.data.element.DataElement;
import io.opensphere.mantle.data.element.MapDataElement;
import io.opensphere.mantle.data.element.event.DataElementDoubleClickedEvent;
import io.opensphere.mantle.data.util.DataElementLookupUtils;
import io.opensphere.mantle.util.MantleToolboxUtils;

/**
 * The baseball card controller.
 */
public class BaseballController extends EventListenerService
{
    /** The toolbox. */
    private final Toolbox myToolbox;

    /** The dialog. */
    @ThreadConfined("EDT")
    private BaseballDialog myDialog;

    /**
     * Constructor.
     *
     * @param toolbox The toolbox
     */
    public BaseballController(Toolbox toolbox)
    {
        super(toolbox.getEventManager());
        myToolbox = toolbox;
        bindEvent(DataElementDoubleClickedEvent.class, this::handleDoubleClickEvent);
    }

    /**
     * Handles a data element double click event.
     *
     * @param event the event
     */
    private void handleDoubleClickEvent(DataElementDoubleClickedEvent event)
    {
        if (!event.isConsumed())
        {
            DataElementLookupUtils lookupUtils = MantleToolboxUtils.getDataElementLookupUtils(myToolbox);
            DataElement element = lookupUtils.getDataElement(event.getRegistryId(), null, event.getDataTypeKey());

            // If the element is a MapDataElement, generate an element with MGRS Derived data
            if (element instanceof MapDataElement)
            {
                element = MGRSUtilities.getMGRSDataElement((MapDataElement)element, MGRSUtilities.DEFAULT_MGRS_PRECISION, this);
            }
            showDialog(element);
        }
    }

    /**
     * Shows the dialog for the data element.
     *
     * @param element the data element
     */
    private void showDialog(DataElement element)
    {
        if (element != null)
        {
            EventQueueUtilities.invokeLater(() ->
            {
                if (myDialog == null)
                {
                    myDialog = new BaseballDialog(myToolbox.getUIRegistry().getMainFrameProvider().get(),
                            myToolbox.getPreferencesRegistry());
                    myDialog.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
                }
                myDialog.setDataElement(element);
                myDialog.setVisible(true);
            });
        }
    }
}

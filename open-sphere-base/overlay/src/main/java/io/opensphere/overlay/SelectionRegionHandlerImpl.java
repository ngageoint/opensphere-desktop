package io.opensphere.overlay;

import java.awt.event.MouseEvent;
import java.util.List;

import org.apache.log4j.Logger;

import io.opensphere.core.control.action.MenuOptionListener;
import io.opensphere.core.geometry.PolygonGeometry;
import io.opensphere.core.util.collections.CollectionUtilities;

/**
 * Displays a selection region.
 */
public class SelectionRegionHandlerImpl implements SelectionHandler
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(SelectionRegionHandlerImpl.class);

    /**
     * The provider for the menu that allows the user to take action on the
     * selection region.
     */
    private final SelectionRegionMenuProvider myMenuProvider;

    /** A description of the latest selection region. */
    private volatile String mySelectionRegionDesc;

    /**
     * Constructor.
     *
     * @param transformer The transformer which will generate the selection
     *            region geometries.
     * @param menuProvider The provider for the menu that allows the user to
     *            take action on the selection region.
     */
    public SelectionRegionHandlerImpl(SelectionRegionTransformer transformer, SelectionRegionMenuProvider menuProvider)
    {
        myMenuProvider = menuProvider;
    }

    @Override
    public void addMenuOptionListener(MenuOptionListener listener)
    {
        myMenuProvider.addMenuOptionListener(listener);
    }

    /**
     * Release resources associated with this handler.
     */
    public void close()
    {
        if (myMenuProvider != null)
        {
            myMenuProvider.close();
        }
    }

    @Override
    public void removeMenuOptionListener(MenuOptionListener listener)
    {
        myMenuProvider.removeMenuOptionListener(listener);
    }

    @Override
    public void selectionRegionCompleted(MouseEvent mouseEvent, String contextId, List<PolygonGeometry> selectionBoxGeometries)
    {
        if (CollectionUtilities.hasContent(selectionBoxGeometries))
        {
            String desc = mySelectionRegionDesc;
            if (desc != null)
            {
                LOGGER.info(desc);
            }
            myMenuProvider.showMenu(mouseEvent, contextId, selectionBoxGeometries);
        }
    }

    @Override
    public void setSelectionRegionDescription(String desc)
    {
        synchronized (this)
        {
            mySelectionRegionDesc = desc;
        }
    }
}

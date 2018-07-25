package io.opensphere.overlay;

import java.awt.Dimension;
import java.util.Map;

import io.opensphere.core.Toolbox;
import io.opensphere.core.hud.framework.Window;
import io.opensphere.core.hud.framework.Window.ToolLocation;
import io.opensphere.core.model.ScreenPosition;
import io.opensphere.core.util.collections.New;

/**
 * 
 */
public class DefaultControlsLayoutManager implements ControlsLayoutManager
{
    private Map<ToolLocation, SequentialLayoutContainer<SequentialLayoutContainer<Window<?, ?>>>> myGrids;

    private Toolbox myToolbox;

    /**
     * 
     */
    public DefaultControlsLayoutManager(Toolbox toolbox)
    {
        myToolbox = toolbox;
        java.awt.EventQueue.invokeLater(() ->
        {
            Dimension screenSize = myToolbox.getUIRegistry().getMainFrameProvider().get().getSize();

            ScreenPosition northWestOrigin = new ScreenPosition(0, 0);
            ScreenPosition northEastOrigin = new ScreenPosition(screenSize.getWidth(), 0);
            ScreenPosition southEastOrigin = new ScreenPosition(screenSize.getWidth(), screenSize.getHeight());
            ScreenPosition southWestOrigin = new ScreenPosition(0, screenSize.getHeight());

            myGrids = New.map();

            myGrids.put(ToolLocation.NORTHWEST, new SequentialLayoutContainer<>(northWestOrigin, 30, LayoutDirection.EAST));
            myGrids.put(ToolLocation.NORTHEAST, new SequentialLayoutContainer<>(northEastOrigin, 30, LayoutDirection.WEST));
            myGrids.put(ToolLocation.SOUTHEAST, new SequentialLayoutContainer<>(southEastOrigin, 30, LayoutDirection.WEST));
            myGrids.put(ToolLocation.SOUTHWEST, new SequentialLayoutContainer<>(southWestOrigin, 30, LayoutDirection.EAST));
        });
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.overlay.ControlsLayoutManager#registerControl(Window,
     *      Window.ToolLocation, int, Dimension)
     */
    @Override
    public void registerControl(Window window, ToolLocation location, int columnNumber, Dimension size)
    {
        SequentialLayoutContainer<SequentialLayoutContainer<Window<?, ?>>> grid = myGrids.get(location);
        ScreenPosition rowOrigin = grid.getOrigin();
        double originY = rowOrigin.getY();

        if (grid.getItem(columnNumber) == null)
        {
            LayoutDirection columnDirection;
            if (location == ToolLocation.NORTHWEST || location == ToolLocation.NORTHEAST)
            {
                columnDirection = LayoutDirection.SOUTH;
            }
            else
            {
                columnDirection = LayoutDirection.NORTH;
            }

            int totalOffset = 30;

            for (int i = 0; i < columnNumber; i++)
            {
                SequentialLayoutContainer<Window<?, ?>> column = grid.getItem(i);
                if (column == null)
                {
                    column = new SequentialLayoutContainer<>(new ScreenPosition(totalOffset, originY), 30, columnDirection);
                    grid.addItem(column, new Dimension(0, 0));
                }
                totalOffset += 30;
                totalOffset += column.getWidth();
            }
        }

        SequentialLayoutContainer<Window<?, ?>> column = grid.getItem(columnNumber);
        column.addItem(window, size);

    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.overlay.ControlsLayoutManager#removeControl(io.opensphere.core.hud.framework.Window)
     */
    @Override
    public void removeControl(Window window)
    {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.overlay.ControlsLayoutManager#getNextScreenPosition(ToolLocation,
     *      int, Dimension)
     */
    @Override
    public ScreenPosition getNextScreenPosition(ToolLocation location, int columnNumber, Dimension dimension)
    {
        SequentialLayoutContainer<SequentialLayoutContainer<Window<?, ?>>> grid = myGrids.get(location);

        SequentialLayoutContainer<Window<?, ?>> column = grid.getItem(columnNumber);
        return column.getTopLeftOfNextItem(dimension);
    }
}

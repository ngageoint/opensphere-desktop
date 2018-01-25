package io.opensphere.controlpanels.timeline;

import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.List;

import javax.swing.JMenuItem;

import org.apache.log4j.Logger;

import io.opensphere.core.control.action.context.TimespanContextKey;
import io.opensphere.core.units.duration.Duration;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.StringUtilities;

/**
 * A composite timeline layer.
 */
public class CompositeLayer extends AbstractTimelineLayer
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(CompositeLayer.class);

    /** The layers that make up this layer. */
    private final List<TimelineLayer> myLayers;

    /**
     * Gets an aggregate list of all temporary layers in the given layers.
     *
     * @param layers the layers
     * @return the temporary layers
     */
    public static List<TimelineLayer> getTemporaryLayers(List<TimelineLayer> layers)
    {
        List<TimelineLayer> temporaryLayers = New.list();
        for (TimelineLayer layer : layers)
        {
            temporaryLayers.addAll(layer.getTemporaryLayers());
        }
        return temporaryLayers;
    }

    /**
     * Constructor.
     */
    public CompositeLayer()
    {
        myLayers = New.list();
    }

    /**
     * Constructor.
     *
     * @param layers the layers
     */
    public CompositeLayer(TimelineLayer... layers)
    {
        myLayers = New.list(layers);
    }

    /**
     * Adds a layer to the composite layer.
     *
     * @param layer the layer to add
     */
    public void addLayer(TimelineLayer layer)
    {
        layer.setUIModel(getUIModel());
        myLayers.add(layer);
    }

    @Override
    public boolean canDrag(Point p)
    {
        return myLayers.stream().anyMatch(l -> l.canDrag(p));
    }

    @Override
    public int getDragPriority(Point p)
    {
        return myLayers.stream().mapToInt(l -> l.getDragPriority(p)).max().orElse(0);
    }

    /**
     * Clears the layers.
     */
    public void clearLayers()
    {
        myLayers.clear();
    }

    @Override
    public Object drag(Object dragObject, Point from, Point to, boolean beginning, Duration dragTime)
    {
        Object actualDragObject = null;

        TimelineLayer dragLayer = null;
        if (dragObject != null)
        {
            dragLayer = myLayers.stream().filter(l -> l.hasDragObject(dragObject)).findAny().orElse(null);
        }

        if (dragLayer == null)
        {
            int maxPriority = Integer.MIN_VALUE;
            int priority;
            for (int i = myLayers.size() - 1; i >= 0; --i)
            {
                TimelineLayer layer = myLayers.get(i);
                if (layer.canDrag(from) && (priority = layer.getDragPriority(from)) > maxPriority)
                {
                    dragLayer = layer;
                    maxPriority = priority;
                }
            }
        }

        if (dragLayer != null)
        {
            actualDragObject = dragLayer.drag(dragObject, from, to, beginning, dragTime);
        }

        return actualDragObject;
    }

    @Override
    public void getMenuItems(Point p, List<JMenuItem> menuItems)
    {
        super.getMenuItems(p, menuItems);
        for (TimelineLayer layer : myLayers)
        {
            layer.getMenuItems(p, menuItems);
        }
    }

    @Override
    public List<? extends Component> getMenuItems(String contextId, TimespanContextKey key)
    {
        List<Component> items = New.list();
        for (TimelineLayer layer : myLayers)
        {
            Collection<? extends Component> menuItems = layer.getMenuItems(contextId, key);
            if (menuItems != null)
            {
                items.addAll(menuItems);
            }
        }

        return items;
    }

    @Override
    public List<TimelineLayer> getTemporaryLayers()
    {
        return getTemporaryLayers(myLayers);
    }

    @Override
    public String getToolTipText(MouseEvent event, String incoming)
    {
        String toolTip = incoming;
        for (int i = myLayers.size() - 1; i >= 0; --i)
        {
            TimelineLayer layer = myLayers.get(i);
            String tmp = layer.getToolTipText(event, toolTip);
            if (tmp != null)
            {
                toolTip = tmp;
            }
        }
        return toolTip;
    }

    @Override
    public boolean hasDragObject(Object dragObject)
    {
        return myLayers.stream().anyMatch(l -> l.hasDragObject(dragObject));
    }

    @Override
    public void mouseEvent(MouseEvent e)
    {
        for (TimelineLayer layer : myLayers)
        {
            layer.mouseEvent(e);
        }
    }

    @Override
    public void paint(Graphics2D g2d)
    {
        super.paint(g2d);

        if (LOGGER.isTraceEnabled())
        {
            for (TimelineLayer layer : myLayers)
            {
                long t0 = System.nanoTime();

                layer.paint(g2d);

                LOGGER.trace(StringUtilities.formatTimingMessage("Time to paint layer " + layer.getClass().getSimpleName() + ": ",
                        System.nanoTime() - t0));
            }
        }
        else
        {
            for (TimelineLayer layer : myLayers)
            {
                layer.paint(g2d);
            }
        }
    }

    /**
     * Removes a layer from the composite layer.
     *
     * @param layer the layer to remove
     */
    public void removeLayer(TimelineLayer layer)
    {
        myLayers.remove(layer);
    }

    @Override
    public void setUIModel(TimelineUIModel model)
    {
        super.setUIModel(model);
        for (TimelineLayer layer : myLayers)
        {
            layer.setUIModel(model);
        }
    }

    /**
     * Gets the layers.
     *
     * @return the layers
     */
    protected List<TimelineLayer> getLayers()
    {
        return myLayers;
    }
}

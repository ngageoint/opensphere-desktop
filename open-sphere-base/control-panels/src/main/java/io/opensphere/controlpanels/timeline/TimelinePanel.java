package io.opensphere.controlpanels.timeline;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.Collection;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.apache.log4j.Logger;

import io.opensphere.core.util.lang.StringUtilities;

/** The timeline panel. */
public class TimelinePanel extends JComponent
{
    /** The logger. */
    private static final Logger LOGGER = Logger.getLogger(TimelinePanel.class);

    /** The serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The master layer. */
    private final CompositeLayer myMasterLayer;

    /** The timeline UI model. */
    private final TimelineUIModel myUIModel;

    /**
     * Constructor.
     *
     * @param uiModel the UI model
     * @param masterLayer The master layer.
     */
    public TimelinePanel(TimelineUIModel uiModel, CompositeLayer masterLayer)
    {
        super();
        setFocusable(true);
        myUIModel = uiModel;
        myUIModel.setComponent(this);

        myMasterLayer = masterLayer;
        myMasterLayer.getLayers().add(new BaseTimelineLayer(myUIModel));
    }

    @Override
    public void addNotify()
    {
        super.addNotify();
        myUIModel.updateBounds();
        myUIModel.calculateRatios();
    }

    @Override
    protected void paintComponent(Graphics g)
    {
        long t0 = System.nanoTime();

        super.paintComponent(g);

        // Fill the background
        g.setColor(getBackground());
        g.fillRect(0, 0, getWidth(), getHeight());

        // Paint the layers
        if (myUIModel.getTimelinePanelBounds() != null)
        {
            Graphics2D g2d = (Graphics2D)g.create();
            try
            {
                myMasterLayer.paint(g2d);

                // Paint the temporary layers
                List<TimelineLayer> temporaryLayers = myMasterLayer.getTemporaryLayers();
                while (!temporaryLayers.isEmpty())
                {
                    for (TimelineLayer layer : temporaryLayers)
                    {
                        layer.setUIModel(myUIModel);
                        layer.paint(g2d);
                    }
                    temporaryLayers = CompositeLayer.getTemporaryLayers(temporaryLayers);
                }
            }
            finally
            {
                g2d.dispose();
            }
        }

        if (LOGGER.isTraceEnabled())
        {
            LOGGER.trace(StringUtilities.formatTimingMessage("Time to paint timeline: ", System.nanoTime() - t0));
        }
    }

    /**
     * Shows a popup menu.
     *
     * @param point The point to show the popup.
     * @param menuItems The menu items.
     */
    void showPopup(Point point, Collection<? extends JMenuItem> menuItems)
    {
        JPopupMenu menu = new JPopupMenu();
        for (JMenuItem menuItem : menuItems)
        {
            menu.add(menuItem);
        }
        menu.show(this, point.x, point.y);
    }
}

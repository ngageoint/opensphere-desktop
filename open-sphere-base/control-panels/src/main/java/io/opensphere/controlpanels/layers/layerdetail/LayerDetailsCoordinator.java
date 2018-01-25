package io.opensphere.controlpanels.layers.layerdetail;

import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JDialog;

import io.opensphere.controlpanels.layers.event.ShowGroupLayerDetailsEvent;
import io.opensphere.core.Toolbox;
import io.opensphere.core.event.EventListener;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.impl.DefaultDataGroupInfo;

/**
 * The Class LayerDetailsCoordinator.
 */
public class LayerDetailsCoordinator
{
    /** A map of data group info id to the associated published frame. */
    private final Map<String, LayerDetailFrame> myFrames = New.map();

    /** The Show group details listener. */
    private final EventListener<ShowGroupLayerDetailsEvent> myShowGroupDetailsListener = new EventListener<ShowGroupLayerDetailsEvent>()
    {
        @Override
        public void notify(final ShowGroupLayerDetailsEvent event)
        {
            final DataGroupInfo dgi = DefaultDataGroupInfo.getKeyMap().getGroupForKey(event.getDataGroupId());
            if (dgi != null)
            {
                EventQueueUtilities.runOnEDT(() -> showLayerDetailsForGroup(dgi, event.getTab()));
            }
        }
    };

    /** The toolbox. */
    private final Toolbox myToolbox;

    /**
     * Instantiates a new layer details coordinator.
     *
     * @param tb the tb
     */
    public LayerDetailsCoordinator(Toolbox tb)
    {
        myToolbox = tb;
        myToolbox.getEventManager().subscribe(ShowGroupLayerDetailsEvent.class, myShowGroupDetailsListener);
    }

    /**
     * Show the details for the given data group info.
     *
     * @param dgi The data group info for which details are desired.
     * @param tab The tab to select for initial display.
     */
    public synchronized void showLayerDetailsForGroup(DataGroupInfo dgi, String tab)
    {
        LayerDetailFrame existingFrame = myFrames.get(dgi.getId());
        if (existingFrame != null)
        {
            existingFrame.setVisible(true);
            existingFrame.toFront();
            return;
        }

        String replaceFrame = null;
        for (Entry<String, LayerDetailFrame> frame : myFrames.entrySet())
        {
            if (!frame.getValue().getDetailPanel().isLocked())
            {
                replaceFrame = frame.getKey();
            }
        }

        if (replaceFrame != null)
        {
            LayerDetailFrame frame = myFrames.remove(replaceFrame);
            Rectangle bounds = frame.getBounds();
            createFrame(dgi, tab, bounds);
            frame.setVisible(false);
        }
        else
        {
            int width = 326;
            int height = 450;
            if (dgi.getAssistant().getSettingsPreferredSize() != null)
            {
                width = dgi.getAssistant().getSettingsPreferredSize().width;
                height = dgi.getAssistant().getSettingsPreferredSize().height;
            }

            Component mainFrame = myToolbox.getUIRegistry().getMainFrameProvider().get();
            int xLoc = (mainFrame.getWidth() - width) / 2 + mainFrame.getX();
            int yLoc = (mainFrame.getHeight() - height) / 2 + mainFrame.getY();

            createFrame(dgi, tab, new Rectangle(xLoc, yLoc, width, height));
        }
    }

    /**
     * Create the frame for the data group info.
     *
     * @param dgi The data group info for which details are desired.
     * @param tab The tab to select on initial display.
     * @param frameBounds The location and size of the frame on initial display.
     */
    private void createFrame(final DataGroupInfo dgi, String tab, Rectangle frameBounds)
    {
        // create the frame
        LayerDetailFrame frame = new LayerDetailFrame(myToolbox, dgi);
        frame.setBounds(frameBounds);
        myFrames.put(dgi.getId(), frame);

        // remove the frame when closing
        frame.addComponentListener(new ComponentAdapter()
        {
            @Override
            public void componentHidden(ComponentEvent e)
            {
                myFrames.remove(dgi.getId());
            }
        });

        frame.getDetailPanel().showTab(tab);
        frame.setVisible(true);
    }

    /** The frame which contains the details for a layer. */
    private static class LayerDetailFrame extends JDialog
    {
        /** serial version UID. */
        private static final long serialVersionUID = 1L;

        /** The detail panel. */
        private final LayerDetailPanel myDetailPanel;

        /**
         * Constructor.
         *
         * @param tb The tool box.
         * @param dgi The data group info for which this frame shows details.
         */
        public LayerDetailFrame(Toolbox tb, DataGroupInfo dgi)
        {
            super(tb.getUIRegistry().getMainFrameProvider().get(), "Layer Details");
            myDetailPanel = new LayerDetailPanel(tb, dgi, this);
            setContentPane(myDetailPanel);
        }

        /**
         * Get the detailPanel.
         *
         * @return the detailPanel
         */
        public LayerDetailPanel getDetailPanel()
        {
            return myDetailPanel;
        }
    }
}

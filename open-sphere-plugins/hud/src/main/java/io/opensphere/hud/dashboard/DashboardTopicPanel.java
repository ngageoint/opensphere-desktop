package io.opensphere.hud.dashboard;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import io.opensphere.core.metrics.MetricsRegistry;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.swing.AbstractHUDPanel;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.hud.dashboard.widget.DashboardWidget;

/**
 * The Class DashboardTopicPanel.
 */
public class DashboardTopicPanel extends AbstractHUDPanel
{
    /**
     * serialVersionUID.
     */
    private static final long serialVersionUID = 1L;

    /** The Scroll pane. */
    @SuppressWarnings("PMD.SingularField")
    private final JScrollPane myScrollPane;

    /** The Widget panel. */
    private final JPanel myWidgetPanel;

    /** The Widgets. */
    private final Set<DashboardWidget> myWidgets;

    /** The Topic. */
    private final String myTopic;

    /**
     * Instantiates a new dashboard topic panel.
     *
     * @param topic the topic
     */
    public DashboardTopicPanel(String topic)
    {
        super(new BorderLayout());
        myTopic = topic;
        setOpaque(false);
        myWidgets = New.set();
        myWidgetPanel = new JPanel();
        myWidgetPanel.setOpaque(false);
        myWidgetPanel.setBackground(new Color(0, 0, 0, 0));
        myWidgetPanel.setLayout(new BoxLayout(myWidgetPanel, BoxLayout.Y_AXIS));
        myScrollPane = new JScrollPane(myWidgetPanel);
        myScrollPane.setOpaque(false);
        add(myScrollPane, BorderLayout.CENTER);
    }

    /**
     * Adds the widget.
     *
     * @param widget the widget
     */
    public void addWidget(DashboardWidget widget)
    {
        boolean changed = false;
        synchronized (myWidgets)
        {
            if (!myWidgets.contains(widget))
            {
                changed = true;
                myWidgets.add(widget);
            }
        }
        if (changed)
        {
            rebuildPanel();
        }
    }

    /**
     * Clear.
     */
    public void clear()
    {
        EventQueueUtilities.runOnEDT(new Runnable()
        {
            @Override
            public void run()
            {
                myWidgets.clear();
                myWidgetPanel.removeAll();
                myWidgetPanel.revalidate();
                myWidgetPanel.repaint();
            }
        });
    }

    /**
     * Gets the topic.
     *
     * @return the topic
     */
    public String getTopic()
    {
        return myTopic;
    }

    /**
     * Num widgets.
     *
     * @return the int
     */
    public int numWidgets()
    {
        return myWidgets.size();
    }

    /**
     * Removes the widget.
     *
     * @param widget the widget
     */
    public void removeWidget(DashboardWidget widget)
    {
        boolean changed = false;
        synchronized (myWidgets)
        {
            changed = myWidgets.remove(widget);
        }
        if (changed)
        {
            rebuildPanel();
        }
    }

    /**
     * Rebuild panel.
     */
    private void rebuildPanel()
    {
        EventQueueUtilities.runOnEDT(new Runnable()
        {
            @Override
            public void run()
            {
                myWidgetPanel.removeAll();

                Map<String, Set<DashboardWidget>> subTopicToWidgetListMap = New.map();
                synchronized (myWidgets)
                {
                    for (DashboardWidget w : myWidgets)
                    {
                        String subTopic = w.getProvider().getSubTopic();
                        if (subTopicToWidgetListMap.containsKey(subTopic))
                        {
                            subTopicToWidgetListMap.get(subTopic).add(w);
                        }
                        else
                        {
                            Set<DashboardWidget> set = New.set();
                            set.add(w);
                            subTopicToWidgetListMap.put(subTopic, set);
                        }
                    }
                }
                List<String> subTopicList = New.list(subTopicToWidgetListMap.keySet());
                Collections.sort(subTopicList);

                for (String subTopic : subTopicList)
                {
                    JPanel stPanel = new JPanel();
                    stPanel.setOpaque(false);
                    stPanel.setLayout(new BoxLayout(stPanel, BoxLayout.Y_AXIS));
                    if (!MetricsRegistry.DEFAULT_SUB_TOPIC.equals(subTopic))
                    {
                        stPanel.setBorder(BorderFactory.createTitledBorder(subTopic));
                    }
                    stPanel.setBackground(new Color(0, 0, 0, 0));
                    List<DashboardWidget> widList = New.list(subTopicToWidgetListMap.get(subTopic));
                    Collections.sort(widList, new DashboardWidget.CompareByPriorityThenLabel());
                    for (DashboardWidget w : widList)
                    {
                        stPanel.add(w);
                    }
                    myWidgetPanel.add(stPanel);
                }

                myWidgetPanel.revalidate();
                myWidgetPanel.repaint();
            }
        });
    }
}

package io.opensphere.hud.dashboard;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import io.opensphere.core.Toolbox;
import io.opensphere.core.metrics.MetricsProvider;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.swing.AbstractHUDPanel;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.core.util.swing.ListComboBoxModel;
import io.opensphere.hud.dashboard.DashboardController.DashboardControllerListener;
import io.opensphere.hud.dashboard.widget.DashboardWidget;
import io.opensphere.hud.dashboard.widget.DefaultLabelValueDashboardWidget;

/**
 * The Class DashboardPanel.
 */
public class DashboardPanel extends AbstractHUDPanel implements DashboardControllerListener
{
    /**
     * serialVersionUID.
     */
    private static final long serialVersionUID = 1L;

    /** The Topic combo box. */
    private JComboBox<String> myTopicComboBox;

    /** The Topic select panel. */
    private JPanel myTopicSelectPanel;

    /** The Topic panel. */
    private JPanel myTopicPanel;

    /** The Topic to topic panel map. */
    private final Map<String, DashboardTopicPanel> myTopicToTopicPanelMap;

    /** The Card layout. */
    private CardLayout myCardLayout;

    /** The Provider to widget map. */
    private final IdentityHashMap<MetricsProvider, DashboardWidget> myProviderToWidgetMap;

    /** The Controller. */
    @SuppressWarnings("PMD.SingularField")
    private final DashboardController myController;

    /**
     * Instantiates a new dashboard panel.
     *
     * @param tb the tb
     */
    public DashboardPanel(Toolbox tb)
    {
        super(tb.getPreferencesRegistry());
        this.setSize(getTopLevelPanelDim());
        setMinimumSize(getSize());
        setPreferredSize(getSize());
        setLayout(new BorderLayout());
        setBackground(getBackgroundColor());
        add(getTopicSelectcPanel(), BorderLayout.NORTH);
        add(getTopicPanel(), BorderLayout.CENTER);
        myTopicToTopicPanelMap = New.map();
        myProviderToWidgetMap = new IdentityHashMap<>();
        myController = new DashboardController(tb);
        myController.addListener(this);
        myController.requestSync();
    }

    @Override
    public void providerAdded(final MetricsProvider provider)
    {
        EventQueueUtilities.runOnEDT(new Runnable()
        {
            @Override
            public void run()
            {
                synchronized (myProviderToWidgetMap)
                {
                    if (!myProviderToWidgetMap.containsKey(provider))
                    {
                        String topic = provider.getTopic();
                        DashboardTopicPanel topicPanel = myTopicToTopicPanelMap.get(topic);
                        if (topicPanel == null)
                        {
                            topicPanel = new DashboardTopicPanel(topic);
                            myTopicToTopicPanelMap.put(topic, topicPanel);
                            getTopicPanel().add(topicPanel, topic);
                            rebuildTopicComboBox();
                        }

                        // Build widget
                        DefaultLabelValueDashboardWidget w = new DefaultLabelValueDashboardWidget(provider,
                                myController.getToolbox());
                        myProviderToWidgetMap.put(provider, w);

                        // Add Widget to Panel.
                        topicPanel.addWidget(w);
                    }
                }
            }
        });
    }

    @Override
    public void providerRemoved(final MetricsProvider provider)
    {
        EventQueueUtilities.runOnEDT(new Runnable()
        {
            @Override
            public void run()
            {
                synchronized (myProviderToWidgetMap)
                {
                    if (myProviderToWidgetMap.containsKey(provider))
                    {
                        DashboardWidget w = myProviderToWidgetMap.get(provider);
                        String topic = provider.getTopic();
                        DashboardTopicPanel topicPanel = myTopicToTopicPanelMap.get(topic);
                        if (topicPanel != null)
                        {
                            topicPanel.removeWidget(w);
                            if (topicPanel.numWidgets() <= 0)
                            {
                                getTopicPanel().remove(topicPanel);
                                myTopicToTopicPanelMap.remove(topic);
                            }
                            rebuildTopicComboBox();
                        }
                        myProviderToWidgetMap.remove(provider);
                    }
                }
            }
        });
    }

    @Override
    public void providersSync(final Set<MetricsProvider> providers)
    {
        EventQueueUtilities.runOnEDT(new Runnable()
        {
            @Override
            public void run()
            {
                synchronized (myProviderToWidgetMap)
                {
                    myProviderToWidgetMap.clear();
                    for (Map.Entry<String, DashboardTopicPanel> entry : myTopicToTopicPanelMap.entrySet())
                    {
                        entry.getValue().clear();
                    }
                    myTopicToTopicPanelMap.clear();
                    getTopicPanel().removeAll();

                    for (MetricsProvider provider : providers)
                    {
                        providerAdded(provider);
                    }
                    rebuildTopicComboBox();
                }
            }
        });
    }

    /**
     * Gets the card layout.
     *
     * @return the card layout
     */
    private CardLayout getCardLayout()
    {
        if (myCardLayout == null)
        {
            myCardLayout = new CardLayout();
        }
        return myCardLayout;
    }

    /**
     * Gets the topic combo box.
     *
     * @return the topic combo box
     */
    private JComboBox<String> getTopicComboBox()
    {
        if (myTopicComboBox == null)
        {
            myTopicComboBox = new JComboBox<>();
            myTopicComboBox.setMaximumSize(new Dimension(1000, 24));
            myTopicComboBox.setPreferredSize(new Dimension(1000, 24));
            myTopicComboBox.setBackground(ourComponentBackgroundColor);
            myTopicComboBox.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    Object selItem = getTopicComboBox().getSelectedItem();
                    getCardLayout().show(getTopicPanel(), selItem.toString());
                }
            });
        }
        return myTopicComboBox;
    }

    /**
     * Gets the topic panel.
     *
     * @return the topic panel
     */
    private JPanel getTopicPanel()
    {
        if (myTopicPanel == null)
        {
            myTopicPanel = new JPanel();
            myTopicPanel.setLayout(getCardLayout());
        }
        return myTopicPanel;
    }

    /**
     * Gets the topi selectc panel.
     *
     * @return the topi selectc panel
     */
    private JPanel getTopicSelectcPanel()
    {
        if (myTopicSelectPanel == null)
        {
            myTopicSelectPanel = new JPanel();
            myTopicSelectPanel.setLayout(new BoxLayout(myTopicSelectPanel, BoxLayout.X_AXIS));
            myTopicSelectPanel.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
            myTopicSelectPanel.add(new JLabel("Topic"));
            myTopicSelectPanel.add(Box.createHorizontalStrut(3));
            myTopicSelectPanel.add(getTopicComboBox());
        }
        return myTopicSelectPanel;
    }

    /**
     * Rebuild topic combo box.
     */
    private void rebuildTopicComboBox()
    {
        final List<String> topics = new ArrayList<>(myTopicToTopicPanelMap.keySet());
        Collections.sort(topics);
        final ComboBoxModel<String> model = new ListComboBoxModel<>(topics);
        EventQueueUtilities.runOnEDT(new Runnable()
        {
            @Override
            public void run()
            {
                Object selItem = getTopicComboBox().getSelectedItem();
                getTopicComboBox().setModel(model);
                if (selItem != null)
                {
                    getTopicComboBox().setSelectedItem(selItem);
                }
                else if (!topics.isEmpty())
                {
                    getCardLayout().show(getTopicPanel(), topics.get(0));
                }
            }
        });
    }
}

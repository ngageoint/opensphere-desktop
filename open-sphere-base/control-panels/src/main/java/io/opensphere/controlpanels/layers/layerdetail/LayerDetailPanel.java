package io.opensphere.controlpanels.layers.layerdetail;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

import com.jidesoft.swing.JideTabbedPane;

import io.opensphere.controlpanels.layers.event.TabChangeListener;
import io.opensphere.controlpanels.layers.util.FeatureTypeLabel;
import io.opensphere.core.Toolbox;
import io.opensphere.core.control.ControlContext;
import io.opensphere.core.control.ControlRegistry;
import io.opensphere.core.control.DefaultKeyPressedBinding;
import io.opensphere.core.control.DiscreteEventAdapter;
import io.opensphere.core.event.EventListener;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.util.Colors;
import io.opensphere.core.util.DateTimeFormats;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.collections.StreamUtilities;
import io.opensphere.core.util.image.IconUtil;
import io.opensphere.core.util.image.IconUtil.IconStyle;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.mantle.controller.util.DataGroupInfoUtilities;
import io.opensphere.mantle.data.AbstractActivationListener;
import io.opensphere.mantle.data.ActivationListener;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.MapVisualizationType;
import io.opensphere.mantle.data.event.DataTypeInfoTagsChangeEvent;
import io.opensphere.mantle.data.impl.GroupCategorizationUtilities;
import io.opensphere.mantle.util.TimeSpanUtility;

/**
 * The Class LayerDetailPanel.
 */
@SuppressWarnings({ "serial", "PMD.GodClass" })
public class LayerDetailPanel extends JPanel
{
    /** The Constant DEBUG_TAB. */
    public static final String DEBUG_TAB = "Debug";

    /** The Constant DETAILS_TAB. */
    public static final String DETAILS_TAB = "Details";

    /** The Constant SETTINGS_TAB. */
    public static final String SETTINGS_TAB = "Settings";

    /** The our in debug mode. */
    private static boolean ourInDebugMode = !Boolean.getBoolean("opensphere.productionMode");

    /** Function to remove trailing 's' characters from a string. */
    private static final Function<String, String> SINGULARIZE = input -> StringUtilities.trim(input, 's');

    /** The controls panel. */
    private LayerDetailLowerControlPanel myControlsPanel;

    /** The data group activation listener. */
    private final transient ActivationListener myDataGroupActivationListener = new AbstractActivationListener()
    {
        @Override
        public void handleCommit(boolean active, DataGroupInfo dgi, io.opensphere.core.util.lang.PhasedTaskCanceller canceller)
        {
            updateUIDetailsEDT();
        }
    };

    /** The event listener to show the debug menu. */
    private final transient DiscreteEventAdapter myDebugListener;

    /** The debug panel. */
    private JPanel myDebugPanel;

    /** The details panel. */
    private JPanel myDetailsPanel;

    /** The details text area. */
    private JTextArea myDetailsTextArea;

    /** The data group info. */
    private transient DataGroupInfo myDGI;

    /** The label panel. */
    private JPanel myLabelPanel;

    /** The layer label. */
    private JLabel myLayerLabel;

    /** The layer time label. */
    private JLabel myLayerTimeLabel;

    /** The layer type icon box. */
    private Box myLayerTypeIconBox;

    /** The lock toggle button. */
    private JCheckBox myLockToggleButton;

    /** The owner. */
    private final Component myOwner;

    /** The settings panel. */
    private JPanel mySettingsPanel;

    /** The tab pane. */
    private JTabbedPane myTabPane;

    /** The Tags changed event listener. */
    private final transient EventListener<DataTypeInfoTagsChangeEvent> myTagsEventListener = this::handleTagsChanged;

    /** The time and type box. */
    private Box myTimeAndTypeBox;

    /** The toolbox. */
    private final transient Toolbox myToolbox;

    /**
     * Gets the details text for the given data group.
     *
     * @param dataGroup the data group
     * @return the details text
     */
    public static String getDetailText(DataGroupInfo dataGroup)
    {
        final String provider = dataGroup.getTopParentDisplayName();
        final Collection<String> categories = StreamUtilities
                .map(GroupCategorizationUtilities.getGroupCategories(dataGroup, false), SINGULARIZE);
        final String type = StringUtilities.join(", ", categories);
        final String summary = dataGroup.getSummaryDescription();

        return StringUtilities.concat("Provider: ", provider, "\n", "Type: ", type, "\n\n", summary, "\n");
    }

    /**
     * Instantiates a new layer detail panel.
     *
     * @param tb the {@link Toolbox}
     * @param dgi the {@link DataGroupInfo}
     * @param owner the owner
     */
    public LayerDetailPanel(Toolbox tb, DataGroupInfo dgi, Component owner)
    {
        super(new BorderLayout());
        myOwner = owner;
        myToolbox = tb;
        myDGI = dgi;
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createLineBorder(Color.lightGray));

        add(getLabelPanel(), BorderLayout.NORTH);
        add(getTabPane(), BorderLayout.CENTER);
        add(getControlsPanel(), BorderLayout.SOUTH);

        updateUIDetails();

        // Add the debug listener
        myDebugListener = new DiscreteEventAdapter("Debug", "Show/Hide Debug Menu", "Show or hide the debug menu.")
        {
            @Override
            public void eventOccurred(InputEvent event)
            {
                ourInDebugMode = !ourInDebugMode;
                showHideDebugTab();
            }
        };
        myDebugListener.setReassignable(false);
        final ControlContext context = myToolbox.getControlRegistry().getControlContext(ControlRegistry.GLOBE_CONTROL_CONTEXT);
        context.addListener(myDebugListener, new DefaultKeyPressedBinding(KeyEvent.VK_D,
                InputEvent.ALT_DOWN_MASK | InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));

        myDGI.activationProperty().addListener(myDataGroupActivationListener);
        myToolbox.getEventManager().subscribe(DataTypeInfoTagsChangeEvent.class, myTagsEventListener);
    }

    /**
     * Gets the owner.
     *
     * @return the owner
     */
    public Component getOwner()
    {
        return myOwner;
    }

    /**
     * Checks if is locked.
     *
     * @return true, if is locked
     */
    public boolean isLocked()
    {
        return getLockToggleButton().isSelected();
    }

    /**
     * Sets the data group info.
     *
     * @param dgi the new data group info
     */
    public void setDataGroupInfo(DataGroupInfo dgi)
    {
        myDGI = dgi;
        updateUIDetailsEDT();
    }

    /**
     * Sets the null and hide.
     */
    public void setNullAndHide()
    {
        EventQueueUtilities.runOnEDT(new Runnable()
        {
            @Override
            public void run()
            {
                setDataGroupInfo(null);
                if (myOwner instanceof JDialog)
                {
                    myOwner.setVisible(false);
                }
            }
        });
    }

    /**
     * Show a particular tab.
     *
     * @param tabName the tab name
     */
    public void showTab(final String tabName)
    {
        if (tabName != null)
        {
            EventQueueUtilities.runOnEDT(new Runnable()
            {
                @Override
                public void run()
                {
                    int foundIndex = -1;
                    for (int i = 0; i < getTabPane().getTabCount(); i++)
                    {
                        if (getTabPane().getTitleAt(i).equals(tabName))
                        {
                            foundIndex = i;
                            break;
                        }
                    }
                    if (foundIndex != -1)
                    {
                        getTabPane().setSelectedIndex(foundIndex);
                    }
                }
            });
        }
    }

    /**
     * Gets the controls panel.
     *
     * @return the controls panel
     */
    private LayerDetailLowerControlPanel getControlsPanel()
    {
        if (myControlsPanel == null)
        {
            myControlsPanel = new LayerDetailLowerControlPanel(myToolbox, this);
        }
        return myControlsPanel;
    }

    /**
     * Gets the details panel.
     *
     * @return the details panel
     */
    private JPanel getDebugPanel()
    {
        if (myDebugPanel == null)
        {
            myDebugPanel = new JPanel(new BorderLayout());
        }
        return myDebugPanel;
    }

    /**
     * Gets the details panel.
     *
     * @return the details panel
     */
    private JPanel getDetailsPanel()
    {
        if (myDetailsPanel == null)
        {
            myDetailsPanel = new JPanel(new BorderLayout());
            final JScrollPane jsp = new JScrollPane(getDetailsTextArea());
            myDetailsPanel.add(jsp, BorderLayout.CENTER);
        }
        return myDetailsPanel;
    }

    /**
     * Gets the details text area.
     *
     * @return the details text area
     */
    private JTextArea getDetailsTextArea()
    {
        if (myDetailsTextArea == null)
        {
            myDetailsTextArea = new JTextArea();
            myDetailsTextArea.setEditable(false);
            myDetailsTextArea.setLineWrap(true);
            myDetailsTextArea.setWrapStyleWord(true);
        }
        return myDetailsTextArea;
    }

    /**
     * Gets the label panel.
     *
     * @return the label panel
     */
    private JPanel getLabelPanel()
    {
        if (myLabelPanel == null)
        {
            myLabelPanel = new JPanel();
            myLabelPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
            myLabelPanel.setLayout(new BorderLayout());
            myLabelPanel.add(getLayerTypeIconBox(), BorderLayout.WEST);
            myLabelPanel.add(getLayerLabel(), BorderLayout.CENTER);
            myLabelPanel.add(getLayerTimeAndTypeBox(), BorderLayout.SOUTH);
            myLabelPanel.add(getLockToggleButton(), BorderLayout.EAST);
        }
        return myLabelPanel;
    }

    /**
     * Gets the layer label.
     *
     * @return the layer label
     */
    private JLabel getLayerLabel()
    {
        if (myLayerLabel == null)
        {
            myLayerLabel = new JLabel("Layer Details");
            myLayerLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
            myLayerLabel.setFont(myLayerLabel.getFont().deriveFont(Font.BOLD, myLayerLabel.getFont().getSize() + 2));
            myLayerLabel.setHorizontalAlignment(JLabel.LEFT);
        }
        return myLayerLabel;
    }

    /**
     * Gets the layer time and type box.
     *
     * @return the layer time and type box
     */
    private Box getLayerTimeAndTypeBox()
    {
        if (myTimeAndTypeBox == null)
        {
            myTimeAndTypeBox = Box.createHorizontalBox();
            myTimeAndTypeBox.add(getLayerTimeLabel());
            myTimeAndTypeBox.add(Box.createHorizontalGlue());
        }
        return myTimeAndTypeBox;
    }

    /**
     * Gets the layer label.
     *
     * @return the layer label
     */
    private JLabel getLayerTimeLabel()
    {
        if (myLayerTimeLabel == null)
        {
            myLayerTimeLabel = new JLabel("Layer Time");
            myLayerTimeLabel.setHorizontalAlignment(JLabel.LEFT);
        }
        return myLayerTimeLabel;
    }

    /**
     * Gets the layer type icon box.
     *
     * @return the layer type icon box
     */
    private Box getLayerTypeIconBox()
    {
        if (myLayerTypeIconBox == null)
        {
            myLayerTypeIconBox = Box.createHorizontalBox();
        }
        return myLayerTypeIconBox;
    }

    /**
     * Gets the lock toggle button.
     *
     * @return the lock toggle button
     */
    private JCheckBox getLockToggleButton()
    {
        if (myLockToggleButton == null)
        {
            final String unselectedToolTip = "Pin this panel so it will not be reused for other layers";
            final String selectedToolTip = "Un-pin this panel so it may be reused for other layers";

            myLockToggleButton = new JCheckBox();
            myLockToggleButton.setSelected(false);
            myLockToggleButton.setIcon(
                    IconUtil.getColorizedIcon("/images/pushpin.png", IconStyle.NORMAL, IconUtil.DEFAULT_ICON_FOREGROUND));
            myLockToggleButton.setSelectedIcon(IconUtil.getNormalIcon("/images/pushpin-pushed.png"));
            myLockToggleButton.setMargin(new Insets(2, 2, 2, 2));
            myLockToggleButton.setFocusPainted(false);
            myLockToggleButton.setContentAreaFilled(false);
            myLockToggleButton.setBorder(null);
            myLockToggleButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    myLockToggleButton.setToolTipText(myLockToggleButton.isSelected() ? selectedToolTip : unselectedToolTip);
                }
            });
            myLockToggleButton.setToolTipText(unselectedToolTip);
        }
        return myLockToggleButton;
    }

    /**
     * Gets the details panel.
     *
     * @return the details panel
     */
    private JPanel getSettingsPanel()
    {
        if (mySettingsPanel == null)
        {
            mySettingsPanel = new JPanel(new BorderLayout());
        }
        return mySettingsPanel;
    }

    /**
     * Gets the tab pane.
     *
     * @return the tab pane
     */
    private JTabbedPane getTabPane()
    {
        if (myTabPane == null)
        {
            myTabPane = new JideTabbedPane();

            myTabPane.setOpaque(false);
            myTabPane.setBackground(new Color(0, 0, 0, 0));
            ((JideTabbedPane)myTabPane).setTabShape(JideTabbedPane.SHAPE_ROUNDED_VSNET);
            myTabPane.setTabPlacement(SwingConstants.TOP);

            myTabPane.addTab(DETAILS_TAB, getDetailsPanel());
            myTabPane.setSelectedIndex(0);
            myTabPane.setBackgroundAt(0, Colors.LF_PRIMARY2);
            myTabPane.setForeground(Color.WHITE);

            myTabPane.addChangeListener(new TabChangeListener());

            myTabPane.addTab(SETTINGS_TAB, getSettingsPanel());
            if (!Boolean.getBoolean("opensphere.productionMode"))
            {
                myTabPane.addTab(DEBUG_TAB, getDebugPanel());
            }
        }
        return myTabPane;
    }

    /**
     * Handle tags changed.
     *
     * @param event the event
     */
    private void handleTagsChanged(DataTypeInfoTagsChangeEvent event)
    {
        if (myDGI != null && myDGI.hasMember(event.getDataTypeInfo(), false))
        {
            // Rebuild details text area.
            EventQueueUtilities.runOnEDT(() -> getDetailsTextArea().setText(getDetailText(myDGI)));
        }
    }

    /**
     * Refresh layer type icon box.
     *
     * @param dgi the dgi
     */
    private void refreshLayerTypeIconBox(DataGroupInfo dgi)
    {
        getLayerTypeIconBox().removeAll();
        if (dgi != null)
        {
            final Set<MapVisualizationType> typeSet = dgi.getMemberMapVisualizationTypes(true);
            if (!typeSet.isEmpty())
            {
                final List<MapVisualizationType> typeList = New.list(typeSet);
                Collections.sort(typeList);

                for (final MapVisualizationType mvt : typeList)
                {
                    final FeatureTypeLabel label = new FeatureTypeLabel();
                    label.setIconByType(null, mvt);
                    label.setToolTipText(StringUtilities.concat(label.getType(), " Layer"));
                    getLayerTypeIconBox().add(label);
                }
                getLayerTypeIconBox().add(Box.createHorizontalStrut(4));
            }
        }
    }

    /**
     * Show hide debug tab.
     */
    private void showHideDebugTab()
    {
        int foundIndex = -1;
        for (int i = 0; i < getTabPane().getTabCount(); i++)
        {
            if (getTabPane().getTitleAt(i).equals(DEBUG_TAB))
            {
                foundIndex = i;
                break;
            }
        }
        if (foundIndex != -1)
        {
            if (!ourInDebugMode)
            {
                getTabPane().remove(foundIndex);
            }
        }
        else
        {
            if (ourInDebugMode)
            {
                getTabPane().add(DEBUG_TAB, getDebugPanel());
            }
        }
    }

    /**
     * Update ui details on the EDT.
     */
    void updateUIDetailsEDT()
    {
        EventQueueUtilities.runOnEDT(this::updateUIDetails);
    }

    /**
     * Update ui details.
     */
    private void updateUIDetails()
    { 
        if (!Boolean.getBoolean("opensphere.productionMode"))
        {
            getTabPane().remove(getDebugPanel());
        }
        getControlsPanel().updateUIDetails(myDGI);
        if (myDGI != null)
        {
            getLayerLabel().setText(myDGI.getDisplayName());
            getDetailsTextArea().setText(getDetailText(myDGI));
            getDetailsTextArea().setCaretPosition(0);

            String layerTimeString = "Timeless";
            final TimeSpan timeSpan = DataGroupInfoUtilities.findTimeExtentFromDGICollection(Collections.singleton(myDGI));
            if (!timeSpan.isZero())
            {
                layerTimeString = TimeSpanUtility.formatTimeSpan(new SimpleDateFormat(DateTimeFormats.DATE_TIME_FORMAT),
                        timeSpan);
            }
            getLayerTimeLabel().setText(layerTimeString);
            final Component settingsComponent = myDGI.getAssistant().getSettingsUIComponent(getSettingsPanel().getSize(), myDGI);
            if (settingsComponent != null)
            {
                if (myTabPane.indexOfTab(SETTINGS_TAB) == -1)
                {
                    myTabPane.addTab(SETTINGS_TAB, getSettingsPanel());
                }
                getSettingsPanel().removeAll();
                getSettingsPanel().add(settingsComponent, BorderLayout.CENTER);
                getSettingsPanel().invalidate();
                getSettingsPanel().revalidate();
                getSettingsPanel().repaint();
            }

            if (!Boolean.getBoolean("opensphere.productionMode"))
            {
                final Component debugComponent = myDGI.getAssistant().getDebugUIComponent(getSettingsPanel().getSize(), myDGI);
                if (debugComponent != null)
                {
                    getDebugPanel().add(debugComponent, BorderLayout.CENTER);
                    getDebugPanel().invalidate();
                    getDebugPanel().revalidate();
                    getDebugPanel().repaint();
                }
                getTabPane().addTab(DEBUG_TAB, getDebugPanel());
            }
        }
        else
        {
            getLayerLabel().setText("UNKNOWN");
            getLayerTimeLabel().setText("");
            getDetailsTextArea().setText("");
        }
        refreshLayerTypeIconBox(myDGI);
    }
}

package io.opensphere.mantle.data.geom.style.dialog;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

import io.opensphere.core.Toolbox;
import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.util.Colors;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.core.util.swing.IconButton;
import io.opensphere.core.util.swing.ListComboBoxModel;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.VisualizationSupport;
import io.opensphere.mantle.data.geom.style.FeatureVisualizationControlPanel;
import io.opensphere.mantle.data.geom.style.VisualizationStyle;
import io.opensphere.mantle.data.geom.style.VisualizationStyleController;
import io.opensphere.mantle.data.geom.style.VisualizationStyleDatatypeChangeEvent;
import io.opensphere.mantle.data.geom.style.VisualizationStyleParameter;
import io.opensphere.mantle.data.geom.style.VisualizationStyleParameterChangeEvent;
import io.opensphere.mantle.data.geom.style.VisualizationStyleParameterChangeListener;
import io.opensphere.mantle.data.geom.style.VisualizationStyleRegistry.VisualizationStyleRegistryChangeListener;
import io.opensphere.mantle.data.geom.style.impl.AbstractFeatureVisualizationStyle;
import io.opensphere.mantle.data.geom.style.impl.VisualizationStyleRegistryChangeAdapter;
import io.opensphere.mantle.util.MantleToolboxUtils;

/** A panel on which the style of a specific type may be adjusted. */
public class MiniStyleTypePanel extends JPanel
{
    /** The unique identifier used for serialization operations. */
    private static final long serialVersionUID = -3184666636503734481L;

    /** The Constant FEATURE_TYPE_COLLAPSED_PREFIX. */
    private static final String FEATURE_TYPE_COLLAPSED_PREFIX = "FeatureTypeCollapsed.";

    /** Minus icon. */
    private static final ImageIcon MINUS_ICON = new ImageIcon(MiniStylePanel.class.getResource("/images/minus.gif"));

    /** Plus icon. */
    private static final ImageIcon PLUS_ICON = new ImageIcon(MiniStylePanel.class.getResource("/images/plus.gif"));

    /** The Current fvcp. */
    private transient FeatureVisualizationControlPanel myCurrentFVCP;

    /** A flag used to track the destroyed state of the panel. */
    private boolean myDestroyed;

    /** The currently selected data group. */
    private final transient DataGroupInfo myDataGroup;

    /** The currently selected datatype. */
    private final transient DataTypeInfo myDataType;

    /** The Expand collapse button. */
    private JToggleButton myExpandCollapseButton;

    /** The Feature class managed on this panel. */
    private final transient Class<? extends VisualizationSupport> myFeatureClass;

    /** A flag used to determine if the panel is the first of a group. */
    private final boolean myFirst;

    /** The Mini style panel controller. */
    private final transient MiniStyleTypePanelController myMiniStylePanelController;

    /** The Registry style. */
    private transient VisualizationStyle myRegistryStyle;

    /** The Reset style button. */
    private IconButton myResetStyleButton;

    /** The Style select combo box. */
    private final JComboBox<StyleNodeUserObject> myStyleSelectComboBox;

    /** The Style select cb action listener. */
    private final transient ActionListener myStyleSelectCBActionListener;

    /** The toolbox through which application state is accessed. */
    private final transient Toolbox myToolbox;

    /** The Style parameter change listener. */
    private transient VisualizationStyleParameterChangeListener myVisStyleParameterChangeListener;

    /** The Registry change listener. */
    private transient VisualizationStyleRegistryChangeListener myVisStyleRegistryChangeListener;

    /** The controller used to manage and access visualization styles. */
    private final VisualizationStyleController myVisualizationStyleController;

    /**
     * Instantiates a new mini style type panel.
     *
     * @param toolbox The toolbox through which application state is accessed.
     * @param featureClass the feature class for which the panel is configured.
     * @param dataGroup the selected data group.
     * @param dataType the selected data type.
     * @param first a flag used to determine if the panel is the first of a
     *            group.
     */
    @SuppressWarnings("PMD.ConstructorCallsOverridableMethod")
    public MiniStyleTypePanel(Toolbox toolbox, Class<? extends VisualizationSupport> featureClass, DataGroupInfo dataGroup,
            DataTypeInfo dataType, boolean first)
    {
        super();
        myFirst = first;
        myToolbox = toolbox;
        myVisualizationStyleController = MantleToolboxUtils.getMantleToolbox(toolbox).getVisualizationStyleController();
        MantleToolboxUtils.getMantleToolbox(toolbox).getVisualizationStyleRegistry()
                .addVisualizationStyleRegistryChangeListener(getStyleRegistryChangeListener());
        myFeatureClass = featureClass;
        myDataGroup = dataGroup;
        myDataType = dataType;
        myMiniStylePanelController = new MiniStyleTypePanelController(toolbox);

        Class<? extends VisualizationStyle> baseStyleClass = StyleManagerUtils.getBaseStyleClassesForFeatureClass(myFeatureClass);
        if (baseStyleClass == null)
        {
            baseStyleClass = myVisualizationStyleController.getSelectedVisualizationStyleClass(featureClass, dataGroup, dataType);
        }

        List<StyleNodeUserObject> list = StyleManagerUtils.createStyleNodeList(myToolbox, baseStyleClass, myFeatureClass,
                myDataGroup, myDataType);
        Class<? extends VisualizationStyle> selStyle = myVisualizationStyleController
                .getSelectedVisualizationStyleClass(featureClass, dataGroup, dataType);

        StyleNodeUserObject selectedNode = list.stream().filter(n -> Objects.equals(n.getStyleClass(), selStyle)).findFirst()
                .orElse(null);
        ListComboBoxModel<StyleNodeUserObject> model = new ListComboBoxModel<>(list);
        if (selectedNode != null)
        {
            model.setSelectedElement(selectedNode);
        }
        myStyleSelectComboBox = new JComboBox<>(model);
        myStyleSelectComboBox.setBackground(Colors.LF_SECONDARY3);
        myStyleSelectComboBox.setSize(160, 22);
        myStyleSelectComboBox.setPreferredSize(myStyleSelectComboBox.getSize());
        myStyleSelectComboBox.setMaximumSize(new Dimension(500, 24));
        myStyleSelectCBActionListener = e ->
        {
            VisualizationStyleController vsc1 = MantleToolboxUtils.getMantleToolbox(myToolbox).getVisualizationStyleController();
            StyleNodeUserObject node = (StyleNodeUserObject)myStyleSelectComboBox.getSelectedItem();
            VisualizationStyle vs = vsc1.getStyleForEditorWithConfigValues(node.getStyleClass(), myFeatureClass, myDataGroup,
                    myDataType);
            vsc1.setSelectedStyleClass(vs, myFeatureClass, myDataGroup, myDataType, MiniStyleTypePanel.this);
        };
        myStyleSelectComboBox.addActionListener(myStyleSelectCBActionListener);

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEtchedBorder());
        if (selectedNode != null)
        {
            handleStyleSelectChange(selectedNode.getStyleClass());
        }
    }

    /** Destroy the contents of the panel in preparation for disposal. */
    public void destroy()
    {
        myDestroyed = true;
        if (myCurrentFVCP != null)
        {
            myCurrentFVCP.removeListener(myMiniStylePanelController);
        }
        if (myRegistryStyle != null)
        {
            myRegistryStyle.removeStyleParameterChangeListener(getStyleParameterChangeListener());
        }
    }

    /**
     * Gets the collapsed preference key.
     *
     * @return the collapsed preference key
     */
    public String getCollapsedPreferenceKey()
    {
        StringBuilder sb = new StringBuilder(FEATURE_TYPE_COLLAPSED_PREFIX);
        sb.append(myFeatureClass.getName()).append(':').append(myDataType.getTypeKey());
        return sb.toString();
    }

    /**
     * Gets the collapsed preference.
     *
     * @return the collapsed preference
     */
    public boolean isCollapsedPreference()
    {
        return myToolbox.getPreferencesRegistry().getPreferences(MiniStylePanel.class).getBoolean(getCollapsedPreferenceKey(),
                !myFirst);
    }

    /**
     * Sets the collapsed preference.
     */
    public void saveCollapsedPreference()
    {
        /* We store the preference when the expander is not in its default
         * state, for the first panel its default state is to be open, so we
         * store the preference only when it is closed. If it is open we remove
         * the preference to keep the file clean. For any panel after the first
         * the default state is to be closed, so we store a preference only if
         * it is opened, and remove the preference if it is closed. */
        String key = getCollapsedPreferenceKey();
        Preferences preferences = myToolbox.getPreferencesRegistry().getPreferences(MiniStylePanel.class);
        if (myFirst)
        {
            if (myExpandCollapseButton.isSelected())
            {
                preferences.putBoolean(key, true, this);
            }
            else
            {
                preferences.remove(key, this);
            }
        }
        else
        {
            if (myExpandCollapseButton.isSelected())
            {
                preferences.remove(key, this);
            }
            else
            {
                preferences.putBoolean(key, false, this);
            }
        }
    }

    /**
     * Gets the expand collapse button.
     *
     * @return the expand collapse button
     */
    private JToggleButton getExpandCollapseButton()
    {
        if (myExpandCollapseButton == null)
        {
            myExpandCollapseButton = new JToggleButton(MINUS_ICON);
            myExpandCollapseButton.setSelectedIcon(PLUS_ICON);
            myExpandCollapseButton.setSelected(isCollapsedPreference());
            myExpandCollapseButton.setMargin(new Insets(0, 0, 0, 0));
            myExpandCollapseButton.setBorder(null);
            myExpandCollapseButton.setContentAreaFilled(false);
            myExpandCollapseButton.addActionListener(e ->
            {
                myCurrentFVCP.getPanel().setVisible(!getExpandCollapseButton().isSelected());
                saveCollapsedPreference();
                getResetStyleButton().setVisible(!isCollapsedPreference());
            });
        }
        return myExpandCollapseButton;
    }

    /**
     * Gets the reset style button.
     *
     * @return the reset style button
     */
    private JButton getResetStyleButton()
    {
        if (myResetStyleButton == null)
        {
            myResetStyleButton = new IconButton();
            myResetStyleButton.setIcon("/images/icon-repeat-up.png");
            myResetStyleButton.setRolloverIcon("/images/icon-repeat-over.png");
            myResetStyleButton.setPressedIcon("/images/icon-repeat-down.png");
            myResetStyleButton.setToolTipText("Reset the style to its defaults.");
            myResetStyleButton.addActionListener(evt ->
            {
                if (myCurrentFVCP != null)
                {
                    myCurrentFVCP.revertToDefaultSettigns();
                }
            });
        }
        return myResetStyleButton;
    }

    /**
     * Gets the style parameter change listener.
     *
     * @return the style parameter change listener
     */
    private VisualizationStyleParameterChangeListener getStyleParameterChangeListener()
    {
        if (myVisStyleParameterChangeListener == null)
        {
            myVisStyleParameterChangeListener = evt -> handleVisualizationStyleParameterChange(evt);
        }
        return myVisStyleParameterChangeListener;
    }

    /**
     * Gets the vis style registry change listener.
     *
     * @return the style registry change listener
     */
    private VisualizationStyleRegistryChangeListener getStyleRegistryChangeListener()
    {
        if (myVisStyleRegistryChangeListener == null)
        {
            myVisStyleRegistryChangeListener = new VisualizationStyleRegistryChangeAdapter()
            {
                @Override
                public void visualizationStyleDatatypeChanged(VisualizationStyleDatatypeChangeEvent evt)
                {
                    handleVisualizationStyleDatatypeChangeEvent(evt);
                }
            };
        }
        return myVisStyleRegistryChangeListener;
    }

    /**
     * Handle style select change.
     *
     * @param selectedStyleClass the selected item
     */
    private void handleStyleSelectChange(final Class<? extends VisualizationStyle> selectedStyleClass)
    {
        EventQueueUtilities.runOnEDT(() ->
        {
            VisualizationStyle vs = myVisualizationStyleController.getStyleForEditorWithConfigValues(selectedStyleClass,
                    myFeatureClass, myDataGroup, myDataType);
            VisualizationStyle currentStyle = MantleToolboxUtils.getMantleToolbox(myToolbox).getVisualizationStyleRegistry()
                    .getStyle(myFeatureClass, myDataType.getTypeKey(), false);

            if (myRegistryStyle != null)
            {
                myRegistryStyle.removeStyleParameterChangeListener(getStyleParameterChangeListener());
            }
            myRegistryStyle = currentStyle;
            if (currentStyle != null && !myDestroyed)
            {
                currentStyle.addStyleParameterChangeListener(getStyleParameterChangeListener());
            }
            FeatureVisualizationControlPanel fvsc = vs.getMiniUIPanel();
            myMiniStylePanelController.setItems(null, null, null, null);
            if (myCurrentFVCP != null)
            {
                myCurrentFVCP.removeListener(myMiniStylePanelController);
            }
            myCurrentFVCP = fvsc;
            if (myCurrentFVCP != null && !myDestroyed)
            {
                myMiniStylePanelController.setItems(myCurrentFVCP, myFeatureClass, myDataGroup, myDataType);
                myCurrentFVCP.addListener(myMiniStylePanelController);
                myCurrentFVCP.getPanel().setVisible(!isCollapsedPreference());
            }

            removeAll();
            Box cbBox = Box.createHorizontalBox();
            cbBox.add(Box.createHorizontalStrut(2));
            cbBox.add(getExpandCollapseButton());
            cbBox.add(Box.createHorizontalStrut(4));
            cbBox.add(new JLabel(StyleManagerUtils.getStyleCategoryNameForFeatureClass(myFeatureClass)));
            if (myStyleSelectComboBox.getModel().getSize() > 1)
            {
                cbBox.add(myStyleSelectComboBox);
            }
            cbBox.add(Box.createHorizontalGlue());
            cbBox.add(getResetStyleButton());
            getResetStyleButton().setVisible(!isCollapsedPreference());
            cbBox.add(Box.createHorizontalStrut(3));
            cbBox.setBorder(BorderFactory.createEmptyBorder(1, 0, 0, 0));
            add(cbBox);
            add((Component)fvsc);
            revalidate();
            repaint();
        });
    }

    /**
     * Handle visualization style datatype change event.
     *
     * @param evt the event to process.
     */
    protected void handleVisualizationStyleDatatypeChangeEvent(final VisualizationStyleDatatypeChangeEvent evt)
    {
        if (!myDestroyed && (evt.getDTIKey() == null
                || Objects.equals(myDataType.getTypeKey(), evt.getDTIKey()) && evt.getMGSClass() == myFeatureClass))
        {
            EventQueueUtilities.runOnEDT(() ->
            {
                Class<? extends VisualizationStyle> selStyle = myVisualizationStyleController
                        .getSelectedVisualizationStyleClass(myFeatureClass, myDataGroup, myDataType);

                StyleNodeUserObject selectedNode = null;
                if (myStyleSelectComboBox.getItemCount() > 1)
                {
                    for (int i = 0; i < myStyleSelectComboBox.getItemCount(); i++)
                    {
                        StyleNodeUserObject node = myStyleSelectComboBox.getItemAt(i);
                        if (Objects.equals(node.getStyleClass(), selStyle))
                        {
                            selectedNode = node;
                            break;
                        }
                    }
                }
                if (selectedNode != null && !Utilities.sameInstance(MiniStyleTypePanel.this, evt.getSource()))
                {
                    myStyleSelectComboBox.removeActionListener(myStyleSelectCBActionListener);
                    myStyleSelectComboBox.setSelectedItem(selectedNode);
                    myStyleSelectComboBox.addActionListener(myStyleSelectCBActionListener);
                }
                handleStyleSelectChange(selStyle);
            });
        }
    }

    /**
     * Handle visualization style parameter change.
     *
     * @param evt the event to process.
     */
    private void handleVisualizationStyleParameterChange(VisualizationStyleParameterChangeEvent evt)
    {
        if (!myDestroyed && myCurrentFVCP != null && !Utilities.sameInstance(evt.getSource(), myMiniStylePanelController))
        {
            Map<String, VisualizationStyleParameter> map = evt.getChangedParameterKeyToParameterMap();
            if (map.size() == 1 && map.containsKey(AbstractFeatureVisualizationStyle.COLOR_PROPERTY_KEY))
            {
                // Special handling for color only changes since we don't show
                // the color chooser.
                VisualizationStyleParameter vsp = map.get(AbstractFeatureVisualizationStyle.COLOR_PROPERTY_KEY);
                myCurrentFVCP.getStyle().setParameter(AbstractFeatureVisualizationStyle.COLOR_PROPERTY_KEY, vsp.getValue(),
                        VisualizationStyle.NO_EVENT_SOURCE);
                myCurrentFVCP.getChangedStyle().setParameter(AbstractFeatureVisualizationStyle.COLOR_PROPERTY_KEY, vsp.getValue(),
                        VisualizationStyle.NO_EVENT_SOURCE);
            }
            else
            {
                handleStyleSelectChange(evt.getStyle().getClass());
            }
        }
    }
}

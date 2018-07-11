package io.opensphere.controlpanels.layers.prefs;

import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import io.opensphere.core.Toolbox;
import io.opensphere.core.options.impl.AbstractPreferencesOptionsProvider;
import io.opensphere.core.preferences.PreferenceChangeEvent;
import io.opensphere.core.preferences.PreferenceChangeListener;
import io.opensphere.core.quantify.QuantifyToolboxUtils;
import io.opensphere.core.util.Utilities;
import io.opensphere.mantle.transformer.MapDataElementTransformer;

/**
 * The Class ExternalToolsOptionsProvider.
 */
public class LayerManagerOptionsProvider extends AbstractPreferencesOptionsProvider
{
    /** The active tab layer source label listener. */
    @SuppressWarnings("PMD.SingularField")
    private PreferenceChangeListener myActiveTabLayerSourceLabelListener;

    /** The Active tab layer type icons listener. */
    @SuppressWarnings("PMD.SingularField")
    private PreferenceChangeListener myActiveTabLayerTypeIconsListener;

    /** The Active tab layer type label listener. */
    @SuppressWarnings("PMD.SingularField")
    private PreferenceChangeListener myActiveTabLayerTypeLabelListener;

    /** The Active tab options panel. */
    private JPanel myActiveTabOptionsPanel;

    /** The Active tab show feature count listener. */
    @SuppressWarnings("PMD.SingularField")
    private PreferenceChangeListener myActiveTabShowFeatureCountListener;

    /** The available tab layer source label listener. */
    @SuppressWarnings("PMD.SingularField")
    private PreferenceChangeListener myAvailableTabLayerSourceLabelListener;

    /** The available tab options panel. */
    private JPanel myAvailableTabOptionsPanel;

    /** The Panel. */
    private JPanel myPanel;

    /** The Show active layer source labels check box. */
    private JCheckBox myShowActiveLayerSourceLabelsCheckBox;

    /** The Show layer source labels check box. */
    private JCheckBox myShowAvailableLayerSourceLabelsCheckBox;

    /** The Show feature count check box. */
    private JCheckBox myShowFeatureCountCheckBox;

    /** The Show layer type icons check box. */
    private JCheckBox myShowLayerTypeIconsCheckBox;

    /** The Show layer type labels check box. */
    private JCheckBox myShowLayerTypeLabelsCheckBox;

    private final Toolbox myToolbox;

    /**
     * Instantiates a new wFS plugin options provider.
     *
     * @param prefsRegistry The preferences registry.
     */
    public LayerManagerOptionsProvider(Toolbox toolbox)
    {
        super(toolbox.getPreferencesRegistry(), "Layers");
        myToolbox = toolbox;
    }

    @Override
    public void applyChanges()
    {
        DataDiscoveryPreferences.setShowActiveLayerTypeIcons(getPreferencesRegistry(),
                getActiveLayerShowTypeIconCheckBox().isSelected(), this);
        DataDiscoveryPreferences.setShowActiveLayerTypeLabels(getPreferencesRegistry(),
                getActiveLayerShowTypeLabelsCheckBox().isSelected(), this);
        DataDiscoveryPreferences.setShowActiveLayerFeatureCounts(getPreferencesRegistry(),
                getShowFeatureCountCheckBox().isSelected(), this);
        DataDiscoveryPreferences.setShowActiveSourceTypeLabels(getPreferencesRegistry(),
                getActiveLayerShowSourceLabelsCheckBox().isSelected(), this);
        DataDiscoveryPreferences.setShowAvailableSourceTypeLabels(getPreferencesRegistry(),
                getAvailableLayerShowSourceLabelsCheckBox().isSelected(), this);
    }

    @Override
    public JPanel getOptionsPanel()
    {
        if (myPanel == null)
        {
            myPanel = new JPanel();
            myPanel.setBackground(DEFAULT_BACKGROUND_COLOR);
            myPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 0));
            myPanel.setLayout(new BoxLayout(myPanel, BoxLayout.Y_AXIS));
            myPanel.add(getActiveTabOptionsPanel());
            myPanel.add(getAvailableTabOptionsPanel());
            myPanel.add(Box.createVerticalGlue());
            JPanel emptyPanel = new JPanel();
            emptyPanel.setBackground(TRANSPARENT_COLOR);
            myPanel.add(emptyPanel);
        }
        return myPanel;
    }

    @Override
    public void restoreDefaults()
    {
        DataDiscoveryPreferences.setShowActiveLayerTypeIcons(getPreferencesRegistry(), true, this);
        DataDiscoveryPreferences.setShowActiveLayerTypeLabels(getPreferencesRegistry(), true, this);
        DataDiscoveryPreferences.setShowActiveLayerFeatureCounts(getPreferencesRegistry(), true, this);
        DataDiscoveryPreferences.setShowActiveSourceTypeLabels(getPreferencesRegistry(), false, this);
        DataDiscoveryPreferences.setShowAvailableSourceTypeLabels(getPreferencesRegistry(), false, this);
        getActiveLayerShowTypeLabelsCheckBox().setSelected(true);
        getActiveLayerShowTypeIconCheckBox().setSelected(true);
        getShowFeatureCountCheckBox().setSelected(true);
    }

    @Override
    public boolean usesApply()
    {
        return false;
    }

    @Override
    public boolean usesRestore()
    {
        return true;
    }

    /**
     * Gets the active layer show source labels check box.
     *
     * @return the active layer show source labels check box
     */
    private JCheckBox getActiveLayerShowSourceLabelsCheckBox()
    {
        if (myShowActiveLayerSourceLabelsCheckBox == null)
        {
            myShowActiveLayerSourceLabelsCheckBox = new JCheckBox("Layer Source Labels",
                    isShowActiveLayerSourceLabelsPreference());
            myShowActiveLayerSourceLabelsCheckBox.setFocusable(false);
            myShowActiveLayerSourceLabelsCheckBox.addActionListener(e ->
            {
                QuantifyToolboxUtils.collectMetric(myToolbox,
                        "mist3d.settings.layers.active-layer-tab-options.layer-source-labels-checkbox");
                DataDiscoveryPreferences.setShowActiveSourceTypeLabels(getPreferencesRegistry(),
                        getActiveLayerShowSourceLabelsCheckBox().isSelected(), this);
            });

            myActiveTabLayerSourceLabelListener = new PreferenceChangeListener()
            {
                @Override
                public void preferenceChange(PreferenceChangeEvent evt)
                {
                    if (!Utilities.sameInstance(evt.getSource(), LayerManagerOptionsProvider.this))
                    {
                        myShowActiveLayerSourceLabelsCheckBox.setSelected(evt.getValueAsBoolean(true));
                    }
                }
            };

            getPreferencesRegistry().getPreferences(MapDataElementTransformer.class).addPreferenceChangeListener(
                    DataDiscoveryPreferences.SHOW_ACTIVE_SOURCE_LABELS, myActiveTabLayerSourceLabelListener);
        }
        return myShowActiveLayerSourceLabelsCheckBox;
    }

    /**
     * Gets the default tools to all time check box.
     *
     * @return the default tools to all time check box
     */
    private JCheckBox getActiveLayerShowTypeIconCheckBox()
    {
        if (myShowLayerTypeIconsCheckBox == null)
        {
            myShowLayerTypeIconsCheckBox = new JCheckBox("Layer Type Icons", isActiveLayerShowTypeIconPreferenceValue());
            myShowLayerTypeIconsCheckBox.setFocusable(false);
            myShowLayerTypeIconsCheckBox.addActionListener(e ->
            {
                QuantifyToolboxUtils.collectMetric(myToolbox,
                        "mist3d.settings.layers.active-layer-tab-options.layer-type-icons-checkbox");
                DataDiscoveryPreferences.setShowActiveLayerTypeIcons(getPreferencesRegistry(),
                        getActiveLayerShowTypeIconCheckBox().isSelected(), this);
            });

            myActiveTabLayerTypeIconsListener = new PreferenceChangeListener()
            {
                @Override
                public void preferenceChange(PreferenceChangeEvent evt)
                {
                    if (!Utilities.sameInstance(evt.getSource(), LayerManagerOptionsProvider.this))
                    {
                        myShowLayerTypeIconsCheckBox.setSelected(evt.getValueAsBoolean(true));
                    }
                }
            };

            getPreferencesRegistry().getPreferences(DataDiscoveryPreferences.class).addPreferenceChangeListener(
                    DataDiscoveryPreferences.SHOW_ACTIVE_LAYER_TYPE_ICONS, myActiveTabLayerTypeIconsListener);
        }
        return myShowLayerTypeIconsCheckBox;
    }

    /**
     * Gets the switch types on mouse over check box.
     *
     * @return the switch types on mouse over check box
     */
    private JCheckBox getActiveLayerShowTypeLabelsCheckBox()
    {
        if (myShowLayerTypeLabelsCheckBox == null)
        {
            myShowLayerTypeLabelsCheckBox = new JCheckBox("Layer Type Labels", isShowActiveLayerTypeLabelsPreference());
            myShowLayerTypeLabelsCheckBox.setFocusable(false);
            myShowLayerTypeLabelsCheckBox.addActionListener(e ->
            {
                QuantifyToolboxUtils.collectMetric(myToolbox,
                        "mist3d.settings.layers.active-layer-tab-options.layer-type-labels-checkbox");
                DataDiscoveryPreferences.setShowActiveLayerTypeLabels(getPreferencesRegistry(),
                        getActiveLayerShowTypeLabelsCheckBox().isSelected(), this);
            });

            myActiveTabLayerTypeLabelListener = new PreferenceChangeListener()
            {
                @Override
                public void preferenceChange(PreferenceChangeEvent evt)
                {
                    if (!Utilities.sameInstance(evt.getSource(), LayerManagerOptionsProvider.this))
                    {
                        myShowLayerTypeLabelsCheckBox.setSelected(evt.getValueAsBoolean(true));
                    }
                }
            };

            getPreferencesRegistry().getPreferences(MapDataElementTransformer.class).addPreferenceChangeListener(
                    DataDiscoveryPreferences.SHOW_ACTIVE_LAYER_TYPE_LABELS, myActiveTabLayerTypeLabelListener);
        }
        return myShowLayerTypeLabelsCheckBox;
    }

    /**
     * Gets the switch.
     *
     * @return the switch
     */
    private JPanel getActiveTabOptionsPanel()
    {
        if (myActiveTabOptionsPanel == null)
        {
            myActiveTabOptionsPanel = new JPanel();
            myActiveTabOptionsPanel.setLayout(new BoxLayout(myActiveTabOptionsPanel, BoxLayout.Y_AXIS));
            myActiveTabOptionsPanel.setBorder(BorderFactory.createTitledBorder("Active Layer Tab Options"));
            myActiveTabOptionsPanel.setBackground(TRANSPARENT_COLOR);
            myActiveTabOptionsPanel.add(Box.createHorizontalStrut(20));
            myActiveTabOptionsPanel.add(getActiveLayerShowTypeIconCheckBox());
            myActiveTabOptionsPanel.add(getActiveLayerShowTypeLabelsCheckBox());
            myActiveTabOptionsPanel.add(getShowFeatureCountCheckBox());
            myActiveTabOptionsPanel.add(getActiveLayerShowSourceLabelsCheckBox());
            myActiveTabOptionsPanel.setMaximumSize(new Dimension(3000, 140));
            myActiveTabOptionsPanel.setMinimumSize(new Dimension(100, 140));
            myActiveTabOptionsPanel.setPreferredSize(new Dimension(300, 140));
        }
        return myActiveTabOptionsPanel;
    }

    /**
     * Gets the switch types on mouse over check box.
     *
     * @return the switch types on mouse over check box
     */
    private JCheckBox getAvailableLayerShowSourceLabelsCheckBox()
    {
        if (myShowAvailableLayerSourceLabelsCheckBox == null)
        {
            myShowAvailableLayerSourceLabelsCheckBox = new JCheckBox("Layer Source Labels",
                    isShowAvailableLayerSourceLabelsPreference());
            myShowAvailableLayerSourceLabelsCheckBox.setFocusable(false);
            myShowAvailableLayerSourceLabelsCheckBox.addActionListener(e ->
            {
                QuantifyToolboxUtils.collectMetric(myToolbox,
                        "mist3d.settings.layers.add-data-panel-options.layer-source-labels-checkbox");
                DataDiscoveryPreferences.setShowAvailableSourceTypeLabels(getPreferencesRegistry(),
                        getAvailableLayerShowSourceLabelsCheckBox().isSelected(), this);
            });

            myAvailableTabLayerSourceLabelListener = new PreferenceChangeListener()
            {
                @Override
                public void preferenceChange(PreferenceChangeEvent evt)
                {
                    if (!Utilities.sameInstance(evt.getSource(), LayerManagerOptionsProvider.this))
                    {
                        myShowAvailableLayerSourceLabelsCheckBox.setSelected(evt.getValueAsBoolean(true));
                    }
                }
            };

            getPreferencesRegistry().getPreferences(MapDataElementTransformer.class).addPreferenceChangeListener(
                    DataDiscoveryPreferences.SHOW_AVAILABLE_SOURCE_TYPE_LABELS, myAvailableTabLayerSourceLabelListener);
        }
        return myShowAvailableLayerSourceLabelsCheckBox;
    }

    /**
     * Gets the switch.
     *
     * @return the switch
     */
    private JPanel getAvailableTabOptionsPanel()
    {
        if (myAvailableTabOptionsPanel == null)
        {
            myAvailableTabOptionsPanel = new JPanel();
            myAvailableTabOptionsPanel.setLayout(new BoxLayout(myAvailableTabOptionsPanel, BoxLayout.Y_AXIS));
            myAvailableTabOptionsPanel.setBorder(BorderFactory.createTitledBorder("'Add Data'  Panel Options"));
            myAvailableTabOptionsPanel.setBackground(TRANSPARENT_COLOR);
            myAvailableTabOptionsPanel.add(Box.createHorizontalStrut(20));
            myAvailableTabOptionsPanel.add(getAvailableLayerShowSourceLabelsCheckBox());
            myAvailableTabOptionsPanel.setMaximumSize(new Dimension(3000, 50));
            myAvailableTabOptionsPanel.setMinimumSize(new Dimension(100, 50));
            myAvailableTabOptionsPanel.setPreferredSize(new Dimension(300, 50));
        }
        return myAvailableTabOptionsPanel;
    }

    /**
     * Gets the show feature count check box.
     *
     * @return the show feature count check box
     */
    private JCheckBox getShowFeatureCountCheckBox()
    {
        if (myShowFeatureCountCheckBox == null)
        {
            myShowFeatureCountCheckBox = new JCheckBox("Show Feature Count", isShowFeatureCountPreferenceValue());
            myShowFeatureCountCheckBox.setFocusable(false);
            myShowFeatureCountCheckBox.addActionListener(e ->
            {
                QuantifyToolboxUtils.collectMetric(myToolbox, "mist3d.settings.layers.active-layer-tab-options.show-feature-count-checkbox");
                DataDiscoveryPreferences.setShowActiveLayerFeatureCounts(getPreferencesRegistry(),
                        getShowFeatureCountCheckBox().isSelected(), this);
            });

            myActiveTabShowFeatureCountListener = new PreferenceChangeListener()
            {
                @Override
                public void preferenceChange(PreferenceChangeEvent evt)
                {
                    if (!Utilities.sameInstance(evt.getSource(), LayerManagerOptionsProvider.this))
                    {
                        myShowFeatureCountCheckBox.setSelected(evt.getValueAsBoolean(true));
                    }
                }
            };

            getPreferencesRegistry().getPreferences(DataDiscoveryPreferences.class).addPreferenceChangeListener(
                    DataDiscoveryPreferences.SHOW_ACTIVE_LAYER_FEATURE_COUNTS, myActiveTabShowFeatureCountListener);
        }
        return myShowFeatureCountCheckBox;
    }

    /**
     * Gets the default tools to all time preference value.
     *
     * @return the default tools to all time preference value
     */
    private boolean isActiveLayerShowTypeIconPreferenceValue()
    {
        return DataDiscoveryPreferences.isShowActiveLayerTypeIcons(getPreferencesRegistry());
    }

    /**
     * Checks if is show active layer source labels preference.
     *
     * @return true, if is show active layer source labels preference
     */
    private boolean isShowActiveLayerSourceLabelsPreference()
    {
        return DataDiscoveryPreferences.isShowActiveSourceTypeLabels(getPreferencesRegistry());
    }

    /**
     * Checks if is show active layer type labels preference.
     *
     * @return true, if is show active layer type labels preference
     */
    private boolean isShowActiveLayerTypeLabelsPreference()
    {
        return DataDiscoveryPreferences.isShowActiveLayerTypeLabels(getPreferencesRegistry());
    }

    /**
     * Checks if is show available layer source labels preference.
     *
     * @return true, if is show available layer source labels preference
     */
    private boolean isShowAvailableLayerSourceLabelsPreference()
    {
        return DataDiscoveryPreferences.isShowAvailableSourceTypeLabels(getPreferencesRegistry());
    }

    /**
     * Checks if is show feature count preference value.
     *
     * @return true, if is show feature count preference value
     */
    private boolean isShowFeatureCountPreferenceValue()
    {
        return DataDiscoveryPreferences.isShowActiveLayerTypeIcons(getPreferencesRegistry());
    }
}

package io.opensphere.mantle.data.geom.style.dialog;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

import io.opensphere.core.Toolbox;
import io.opensphere.core.util.Colors;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.concurrent.ProcrastinatingExecutor;
import io.opensphere.core.util.lang.EqualsHelper;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.core.util.swing.IconButton;
import io.opensphere.core.util.swing.ListComboBoxModel;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.VisualizationSupport;
import io.opensphere.mantle.data.geom.style.FeatureVisualizationControlPanel;
import io.opensphere.mantle.data.geom.style.FeatureVisualizationControlPanel.FeatureVisualizationControlPanelListener;
import io.opensphere.mantle.data.geom.style.VisualizationStyle;
import io.opensphere.mantle.data.geom.style.VisualizationStyleController;
import io.opensphere.mantle.data.geom.style.VisualizationStyleDatatypeChangeEvent;
import io.opensphere.mantle.data.geom.style.VisualizationStyleParameter;
import io.opensphere.mantle.data.geom.style.VisualizationStyleParameterChangeEvent;
import io.opensphere.mantle.data.geom.style.VisualizationStyleParameterChangeListener;
import io.opensphere.mantle.data.geom.style.VisualizationStyleRegistry.VisualizationStyleRegistryChangeListener;
import io.opensphere.mantle.data.geom.style.impl.AbstractFeatureVisualizationStyle;
import io.opensphere.mantle.data.geom.style.impl.AbstractVisualizationStyle;
import io.opensphere.mantle.data.geom.style.impl.VisualizationStyleRegistryChangeAdapter;
import io.opensphere.mantle.util.MantleToolboxUtils;

/**
 * The Class MiniStylePanel.
 */
@SuppressWarnings("PMD.GodClass")
public class MiniStylePanel extends JPanel
{
    /** The Constant FEATURE_TYPE_COLLAPSED_PREFIX. */
    private static final String FEATURE_TYPE_COLLAPSED_PREFIX = "FeatureTypeCollapsed.";

    /** Minus icon. */
    private static final ImageIcon MINUS_ICON = new ImageIcon(MiniStylePanel.class.getResource("/images/minus.gif"));

    /** Plus icon. */
    private static final ImageIcon PLUS_ICON = new ImageIcon(MiniStylePanel.class.getResource("/images/plus.gif"));

    /** serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The DGI. */
    private final transient DataGroupInfo myDGI;

    /** The DTI. */
    private final transient DataTypeInfo myDTI;

    /** The Enable custom type check box. */
    private final JCheckBox myEnableCustomTypeCheckBox;

    /** The Registry listener. */
    private final transient VisualizationStyleRegistryChangeListener myRegistryListener = new VisualizationStyleRegistryChangeAdapter()
    {
        @Override
        public void visualizationStyleDatatypeChanged(VisualizationStyleDatatypeChangeEvent evt)
        {
            if ((evt.getDTIKey() == null || EqualsHelper.equals(myDTI.getTypeKey(), evt.getDTIKey()))
                    && myEnableCustomTypeCheckBox.isSelected() == evt.isNewIsDefaultStyle())
            {
                rebuildUI();
            }
        }
    };

    /** The Toolbox. */
    private final transient Toolbox myToolbox;

    /** The Type panels. */
    private final List<MiniStyleTypePanel> myTypePanels;

    /**
     * Instantiates a new mini style panel.
     *
     * @param tb the tb
     * @param dgi the dgi
     * @param dti the dti
     */
    public MiniStylePanel(Toolbox tb, DataGroupInfo dgi, DataTypeInfo dti)
    {
        super();
        myToolbox = tb;
        myTypePanels = New.list();
        myDGI = dgi;
        myDTI = dti;

        myEnableCustomTypeCheckBox = new JCheckBox("Enable Custom Style", false);
        myEnableCustomTypeCheckBox.setBorder(null);
        myEnableCustomTypeCheckBox.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                VisualizationStyleController vsc = MantleToolboxUtils.getMantleToolbox(myToolbox)
                        .getVisualizationStyleController();
                vsc.setUseCustomStyleForDataType(myDGI, myDTI, myEnableCustomTypeCheckBox.isSelected(), MiniStylePanel.this);
                rebuildUI();
            }
        });

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    }

    @Override
    public void addNotify()
    {
        super.addNotify();
        rebuildUI();
        MantleToolboxUtils.getMantleToolbox(myToolbox).getVisualizationStyleRegistry()
                .addVisualizationStyleRegistryChangeListener(myRegistryListener);
    }

    @Override
    public void removeNotify()
    {
        super.removeNotify();
        MantleToolboxUtils.getMantleToolbox(myToolbox).getVisualizationStyleRegistry()
                .removeVisualizationStyleRegistryChangeListener(myRegistryListener);
        destroy();
    }

    /** Destroy. */
    private void destroy()
    {
        removeAll();
        if (myTypePanels != null && !myTypePanels.isEmpty())
        {
            for (MiniStyleTypePanel panel : myTypePanels)
            {
                panel.destroy();
            }
            myTypePanels.clear();
        }
    }

    /** Rebuild ui. */
    private void rebuildUI()
    {
        EventQueueUtilities.runOnEDT(new Runnable()
        {
            @Override
            public void run()
            {
                destroy();

                Box hBox = Box.createHorizontalBox();
                hBox.add(myEnableCustomTypeCheckBox);
                hBox.add(Box.createHorizontalGlue());
                hBox.add(Box.createHorizontalStrut(5));
                add(hBox);

                VisualizationStyleController vsc = MantleToolboxUtils.getMantleToolbox(myToolbox)
                        .getVisualizationStyleController();
                boolean shouldBeSelected = vsc.isTypeUsingCustom(myDGI, myDTI);
                if (shouldBeSelected != myEnableCustomTypeCheckBox.isSelected())
                {
                    myEnableCustomTypeCheckBox.setSelected(shouldBeSelected);
                }

                if (myEnableCustomTypeCheckBox.isSelected())
                {
                    List<Class<? extends VisualizationSupport>> featureClasses = StyleManagerUtils
                            .getDefaultFeatureClassesForType(myDTI);
                    if (CollectionUtilities.hasContent(featureClasses))
                    {
                        boolean isFirst = true;
                        for (Class<? extends VisualizationSupport> fc : featureClasses)
                        {
                            MiniStyleTypePanel mstp = new MiniStyleTypePanel(myToolbox, fc, myDGI, myDTI, isFirst);
                            // GCD: the extra Box has the effect of indenting
                            // by eight pixels--we could re-enable this
                            // Box aBox = Box.createHorizontalBox();
                            // aBox.add(Box.createHorizontalStrut(8));
                            // aBox.add(mstp);
                            myTypePanels.add(mstp);
                            // add(aBox);
                            add(mstp);
                            isFirst = false;
                        }
                    }
                }

                revalidate();
                repaint();
            }
        });
    }

    /**
     * The Class MiniStyleTypePanel.
     */
    private static class MiniStyleTypePanel extends JPanel
    {
        /** serialVersionUID. */
        private static final long serialVersionUID = 1L;

        /** The Current fvcp. */
        private transient FeatureVisualizationControlPanel myCurrentFVCP;

        /** The Destroyed. */
        private boolean myDestroyed;

        /** The DGI. */
        private final transient DataGroupInfo myDGI;

        /** The DTI. */
        private final transient DataTypeInfo myDTI;

        /** The Expand collapse button. */
        private JToggleButton myExpandCollapseButton;

        /** The Feature class. */
        private final transient Class<? extends VisualizationSupport> myFeatureClass;

        /** The Is first. */
        private final boolean myIsFirst;

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

        /** The Toolbox. */
        private final transient Toolbox myToolbox;

        /** The Style parameter change listener. */
        private transient VisualizationStyleParameterChangeListener myVisStyleParameterChangeListener;

        /** The Registry change listener. */
        private transient VisualizationStyleRegistryChangeListener myVisStyleRegistryChangeListener;

        /**
         * Instantiates a new mini style type panel.
         *
         * @param tb the tb
         * @param featureClass the feature class
         * @param dgi the dgi
         * @param dti the dti
         * @param isFirst the is first
         */
        @SuppressWarnings("PMD.ConstructorCallsOverridableMethod")
        public MiniStyleTypePanel(Toolbox tb, Class<? extends VisualizationSupport> featureClass, DataGroupInfo dgi,
                DataTypeInfo dti, boolean isFirst)
        {
            super();
            myIsFirst = isFirst;
            myToolbox = tb;
            VisualizationStyleController vsc = MantleToolboxUtils.getMantleToolbox(tb).getVisualizationStyleController();
            MantleToolboxUtils.getMantleToolbox(tb).getVisualizationStyleRegistry()
                    .addVisualizationStyleRegistryChangeListener(getStyleRegistryChangeListener());
            myFeatureClass = featureClass;
            myDGI = dgi;
            myDTI = dti;
            myMiniStylePanelController = new MiniStyleTypePanelController(tb);

            Class<? extends VisualizationStyle> baseStyleClass = StyleManagerUtils
                    .getBaseStyleClassesForFeatureClass(myFeatureClass);

            if (baseStyleClass == null)
            {
                baseStyleClass = vsc.getSelectedVisualizationStyleClass(featureClass, dgi, dti);
            }

            List<StyleNodeUserObject> list = StyleManagerUtils.createStyleNodeList(myToolbox, baseStyleClass, myFeatureClass,
                    myDGI, myDTI);
            Class<? extends VisualizationStyle> selStyle = vsc.getSelectedVisualizationStyleClass(featureClass, dgi, dti);

            StyleNodeUserObject selectedNode = null;
            for (StyleNodeUserObject node : list)
            {
                if (EqualsHelper.equals(node.getStyleClass(), selStyle))
                {
                    selectedNode = node;
                }
            }
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
            myStyleSelectCBActionListener = new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    VisualizationStyleController vsc = MantleToolboxUtils.getMantleToolbox(myToolbox)
                            .getVisualizationStyleController();
                    StyleNodeUserObject node = (StyleNodeUserObject)myStyleSelectComboBox.getSelectedItem();
                    VisualizationStyle vs = vsc.getStyleForEditorWithConfigValues(node.getStyleClass(), myFeatureClass, myDGI,
                            myDTI);
                    vsc.setSelectedStyleClass(vs, myFeatureClass, myDGI, myDTI, MiniStyleTypePanel.this);
                }
            };
            myStyleSelectComboBox.addActionListener(myStyleSelectCBActionListener);

            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBorder(BorderFactory.createEtchedBorder());
            if (selectedNode != null)
            {
                handleStyleSelectChange(selectedNode.getStyleClass());
            }
        }

        /**
         * Destroy.
         */
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
            StringBuilder sb = new StringBuilder();
            sb.append(FEATURE_TYPE_COLLAPSED_PREFIX);
            sb.append(myFeatureClass.getName()).append(':').append(myDTI.getTypeKey());
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
                    !myIsFirst);
        }

        /**
         * Sets the collapsed preference.
         *
         */
        public void saveCollapsedPreference()
        {
            // We store the preference when the expander is not in its default
            // state, for the first panel its default state is to be
            // open, so we store the preference only when it is closed. If it is
            // open we remove the preference to keep the file clean.
            // For any panel after the first the default state is ot be closed,
            // so we store a preference only if it is opened, and remove
            // the preference if it is closed.
            String key = getCollapsedPreferenceKey();
            if (myIsFirst)
            {
                if (myExpandCollapseButton.isSelected())
                {
                    myToolbox.getPreferencesRegistry().getPreferences(MiniStylePanel.class).putBoolean(key, true, this);
                }
                else
                {
                    myToolbox.getPreferencesRegistry().getPreferences(MiniStylePanel.class).remove(key, this);
                }
            }
            else
            {
                if (myExpandCollapseButton.isSelected())
                {
                    myToolbox.getPreferencesRegistry().getPreferences(MiniStylePanel.class).remove(key, this);
                }
                else
                {
                    myToolbox.getPreferencesRegistry().getPreferences(MiniStylePanel.class).putBoolean(key, false, this);
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
                myExpandCollapseButton.addActionListener(new ActionListener()
                {
                    @Override
                    public void actionPerformed(ActionEvent e)
                    {
                        myCurrentFVCP.getPanel().setVisible(!getExpandCollapseButton().isSelected());
                        saveCollapsedPreference();
                        getResetStyleButton().setVisible(!isCollapsedPreference());
                    }
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
                myResetStyleButton.addActionListener(new ActionListener()
                {
                    @Override
                    public void actionPerformed(ActionEvent evt)
                    {
                        if (myCurrentFVCP != null)
                        {
                            myCurrentFVCP.revertToDefaultSettigns();
                        }
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
                myVisStyleParameterChangeListener = new VisualizationStyleParameterChangeListener()
                {
                    @Override
                    public void styleParametersChanged(VisualizationStyleParameterChangeEvent evt)
                    {
                        handleVisualizationStyleParameterChange(evt);
                    }
                };
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
            EventQueueUtilities.runOnEDT(new Runnable()
            {
                @Override
                public void run()
                {
                    VisualizationStyleController vsc = MantleToolboxUtils.getMantleToolbox(myToolbox)
                            .getVisualizationStyleController();
                    VisualizationStyle vs = vsc.getStyleForEditorWithConfigValues(selectedStyleClass, myFeatureClass, myDGI,
                            myDTI);
                    VisualizationStyle curStyle = MantleToolboxUtils.getMantleToolbox(myToolbox).getVisualizationStyleRegistry()
                            .getStyle(myFeatureClass, myDTI.getTypeKey(), false);

                    if (myRegistryStyle != null)
                    {
                        myRegistryStyle.removeStyleParameterChangeListener(getStyleParameterChangeListener());
                    }
                    myRegistryStyle = curStyle;
                    if (curStyle != null && !myDestroyed)
                    {
                        curStyle.addStyleParameterChangeListener(getStyleParameterChangeListener());
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
                        myMiniStylePanelController.setItems(myCurrentFVCP, myFeatureClass, myDGI, myDTI);
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
                }
            });
        }

        /**
         * Handle visualization style datatype change event.
         *
         * @param evt the evt
         */
        private void handleVisualizationStyleDatatypeChangeEvent(final VisualizationStyleDatatypeChangeEvent evt)
        {
            if (!myDestroyed && (evt.getDTIKey() == null
                    || EqualsHelper.equals(myDTI.getTypeKey(), evt.getDTIKey()) && evt.getMGSClass() == myFeatureClass))
            {
                EventQueueUtilities.runOnEDT(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        VisualizationStyleController vsc = MantleToolboxUtils.getMantleToolbox(myToolbox)
                                .getVisualizationStyleController();
                        Class<? extends VisualizationStyle> selStyle = vsc.getSelectedVisualizationStyleClass(myFeatureClass,
                                myDGI, myDTI);

                        StyleNodeUserObject selectedNode = null;
                        if (myStyleSelectComboBox.getItemCount() > 1)
                        {
                            for (int i = 0; i < myStyleSelectComboBox.getItemCount(); i++)
                            {
                                StyleNodeUserObject node = myStyleSelectComboBox.getItemAt(i);
                                if (EqualsHelper.equals(node.getStyleClass(), selStyle))
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
                    }
                });
            }
        }

        /**
         * Handle visualization style parameter change.
         *
         * @param evt the evt
         */
        private void handleVisualizationStyleParameterChange(VisualizationStyleParameterChangeEvent evt)
        {
            if (!myDestroyed && myCurrentFVCP != null && !Utilities.sameInstance(evt.getSource(), myMiniStylePanelController))
            {
                Map<String, VisualizationStyleParameter> map = evt.getChangedParameterKeyToParameterMap();
                if (map.size() == 1 && map.containsKey(AbstractFeatureVisualizationStyle.COLOR_PROPERTY_KEY))
                {
                    // Special handling for color only changes since we
                    // don't show the color chooser.
                    VisualizationStyleParameter vsp = map.get(AbstractFeatureVisualizationStyle.COLOR_PROPERTY_KEY);
                    myCurrentFVCP.getStyle().setParameter(AbstractFeatureVisualizationStyle.COLOR_PROPERTY_KEY, vsp.getValue(),
                            VisualizationStyle.NO_EVENT_SOURCE);
                    myCurrentFVCP.getChangedStyle().setParameter(AbstractFeatureVisualizationStyle.COLOR_PROPERTY_KEY,
                            vsp.getValue(), VisualizationStyle.NO_EVENT_SOURCE);
                }
                else
                {
                    handleStyleSelectChange(evt.getStyle().getClass());
                }
            }
        }
    }

    /**
     * The Class MiniStylePanelController.
     */
    private static class MiniStyleTypePanelController implements FeatureVisualizationControlPanelListener
    {
        /** The DGI. */
        private transient DataGroupInfo myDGI;

        /** The DTI. */
        private transient DataTypeInfo myDTI;

        /** The Feature class. */
        private transient Class<? extends VisualizationSupport> myFeatureClass;

        /** The FVCP. */
        private transient FeatureVisualizationControlPanel myFVCP;

        /** The Toolbox. */
        private final Toolbox myToolbox;

        /** The Update executor. */
        private final ProcrastinatingExecutor myUpdateExecutor = new ProcrastinatingExecutor("MiniStyleControlPanel:Update", 500);

        /** The VSC. */
        private final VisualizationStyleController myVSC;

        /**
         * Instantiates a new mini style panel controller.
         *
         * @param tb the {@link Toolbox}
         */
        public MiniStyleTypePanelController(Toolbox tb)
        {
            myToolbox = tb;
            myVSC = MantleToolboxUtils.getMantleToolbox(tb).getVisualizationStyleController();
        }

        @Override
        public void performLiveParameterUpdate(String dtiKey, Class<? extends VisualizationSupport> convertedClass,
                Class<? extends VisualizationStyle> vsClass, Set<VisualizationStyleParameter> updateSet)
        {
            VisualizationStyle visStyle = null;
            if (dtiKey == null)
            {
                visStyle = MantleToolboxUtils.getMantleToolbox(myToolbox).getVisualizationStyleRegistry()
                        .getDefaultStyle(convertedClass);
            }
            else
            {
                visStyle = MantleToolboxUtils.getMantleToolbox(myToolbox).getVisualizationStyleRegistry().getStyle(convertedClass,
                        dtiKey, false);
            }
            if (visStyle instanceof AbstractVisualizationStyle && Utilities.sameInstance(visStyle.getClass(), vsClass))
            {
                ((AbstractVisualizationStyle)visStyle).setParameters(updateSet, this);
            }
        }

        /**
         * Sets the items.
         *
         * @param fVCP the f vcp
         * @param featureClass the feature class
         * @param dgi the dgi
         * @param dti the dti
         */
        public void setItems(FeatureVisualizationControlPanel fVCP, Class<? extends VisualizationSupport> featureClass,
                DataGroupInfo dgi, DataTypeInfo dti)
        {
            myFeatureClass = featureClass;
            myFVCP = fVCP;
            myDGI = dgi;
            myDTI = dti;
        }

        @Override
        public void styleChanged(boolean hasChangesFromBase)
        {
            myUpdateExecutor.execute(new Runnable()
            {
                @Override
                public void run()
                {
                    myVSC.updateStyle(myFVCP.getChangedStyle(), myFeatureClass, myDGI, myDTI, MiniStyleTypePanelController.this);
                }
            });
        }

        @Override
        public void styleChangesAccepted()
        {
        }

        @Override
        public void styleChangesCancelled()
        {
        }
    }
}

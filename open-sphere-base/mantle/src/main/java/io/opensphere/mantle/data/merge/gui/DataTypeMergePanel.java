package io.opensphere.mantle.data.merge.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;

import org.apache.commons.lang3.StringUtils;

import io.opensphere.core.Toolbox;
import io.opensphere.core.common.util.Tuple2;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.WeakChangeSupport;
import io.opensphere.core.util.concurrent.EventQueueExecutor;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.mantle.MantleToolbox;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.MetaDataInfo;
import io.opensphere.mantle.data.SpecialKey;
import io.opensphere.mantle.data.impl.DefaultMetaDataInfo;
import io.opensphere.mantle.data.merge.DataTypeMergeAssistant;
import io.opensphere.mantle.data.merge.DataTypeMergeComponent;
import io.opensphere.mantle.data.merge.DataTypeMergeMap;
import io.opensphere.mantle.data.merge.KeySpecification;
import io.opensphere.mantle.data.merge.MergeKeySpecification;
import io.opensphere.mantle.data.merge.MetaDataMergeKeyMapEntry;
import io.opensphere.mantle.data.merge.gui.DataTypeKeyMoveDNDCoordinator.KeyMoveListener;
import io.opensphere.mantle.util.MantleToolboxUtils;

/**
 * The Class DataTypeMergePanel.
 */
@SuppressWarnings({ "serial", "PMD.GodClass" })
public class DataTypeMergePanel extends JPanel implements KeyMoveListener
{
    /** The OUR_EDIT_MESSAGE. */
    private static final String EDIT_MESSAGE = "Please use the lower sets of data type keys to compose a new merged"
            + " type with the lists and keys above by dragging keys from the lower "
            + "set to the upper set.\n\nThe left section of the merged type is for special keys"
            + ", the right side is for all other keys.  Special keys are keys that represent "
            + "a specific type of data such as LATITUDE, LONGITUDE, TIME, etc.";

    /** The OUR_VIEW_MESSAGE. */
    private static final String VIEW_MESSAGE = "The mapping configuration defines how multiple data"
            + " types are mapped together into one merged type.";

    /** The Auto merge button. */
    private JButton myAutoMergeButton;

    /** The Key move listeners. */
    private final transient WeakChangeSupport<MergeMapChangeListener> myChangeSupport;

    /** The Data type key move dnd coordinator. */
    private final transient DataTypeKeyMoveDNDCoordinator myDataTypeKeyMoveDNDCoordinator;

    /** The Data type key to meta data info map. */
    private final transient Map<DTINameKeyPair, MetaDataInfo> myDataTypeKeyToMetaDataInfoMap;

    /** The Data type merge result listener. */
    private final transient DataTypeMergeResultListener myDataTypeMergeResultListener;

    /** The Destination key list panel. */
    private final DestinationKeyListPanel myDestinationKeyListPanel;

    /** The Editable. */
    private boolean myEditable = true;

    /** The Lower source panel. */
    private final JPanel myLowerSourcePanel;

    /** The Mapped type key panels. */
    private final List<MappedTypeKeyPanel> myMappedTypeKeyPanels;

    /** The Normal key target panel. */
    private JPanel myNormalKeyTargetPanel;

    /** The Reset merge button. */
    private JButton myResetMergeButton;

    /** The Source type key panels. */
    private final List<SourceTypeKeyPanel> mySourceTypeKeyPanels;

    /** The Special key target panel. */
    private JPanel mySpecialKeyTargetPanel;

    /** The Special mapped type key panels. */
    private final List<SpecialMappedTypeKeyPanel> mySpecialMappedTypeKeyPanels;

    /** The Top text. */
    private JTextArea myTopText;

    /**
     * Instantiates a new data type merge panel.
     */
    public DataTypeMergePanel()
    {
        this(new HashMap<DTINameKeyPair, MetaDataInfo>());
    }

    /**
     * Instantiates a new data type merge panel.
     *
     * @param dtiKeyToMDIMap the dti key to mdi map
     */
    public DataTypeMergePanel(Map<DTINameKeyPair, MetaDataInfo> dtiKeyToMDIMap)
    {
        this(dtiKeyToMDIMap, null);
    }

    /**
     * Instantiates a new data type merge panel.
     *
     * @param dtiKeyToMDIMap the dti key to mdi map
     * @param resultListener the result listener
     */
    public DataTypeMergePanel(Map<DTINameKeyPair, MetaDataInfo> dtiKeyToMDIMap, DataTypeMergeResultListener resultListener)
    {
        super();
        myChangeSupport = new WeakChangeSupport<>();
        myDataTypeMergeResultListener = resultListener;
        myDataTypeKeyMoveDNDCoordinator = new DataTypeKeyMoveDNDCoordinator();
        myDataTypeKeyMoveDNDCoordinator.addKeyMoveListener(this);
        setLayout(new BorderLayout());
        Utilities.checkNull(dtiKeyToMDIMap, "dtiKeyToMDIMap");

        myMappedTypeKeyPanels = new ArrayList<>();
        mySpecialMappedTypeKeyPanels = new ArrayList<>();
        mySourceTypeKeyPanels = new ArrayList<>();

        myDataTypeKeyToMetaDataInfoMap = new HashMap<>(dtiKeyToMDIMap);

        JPanel upperTargetPanel = new JPanel(new BorderLayout());
        myLowerSourcePanel = new JPanel(new BorderLayout());

        add(createTopTextPanel(), BorderLayout.NORTH);

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));

        JPanel utOuterPanel = new JPanel(new BorderLayout());
        utOuterPanel.setBorder(BorderFactory.createTitledBorder("Merged Type Column Key Mapping"));
        upperTargetPanel.setLayout(new BoxLayout(upperTargetPanel, BoxLayout.X_AXIS));
        JScrollPane upperTargetPanelSP = new JScrollPane(upperTargetPanel);
        utOuterPanel.add(upperTargetPanelSP, BorderLayout.CENTER);

        JPanel lpOuterPanel = new JPanel(new BorderLayout());
        lpOuterPanel.setBorder(BorderFactory.createTitledBorder("Source Data Types and Column Keys"));
        myLowerSourcePanel.setLayout(new BoxLayout(myLowerSourcePanel, BoxLayout.X_AXIS));
        JScrollPane lowerSourcePanelSP = new JScrollPane(myLowerSourcePanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        lowerSourcePanelSP.setViewportView(myLowerSourcePanel);
        lpOuterPanel.add(lowerSourcePanelSP, BorderLayout.CENTER);

        centerPanel.add(utOuterPanel);

        JPanel dropOuterPanel = createDropOuterPanel();

        centerPanel.add(dropOuterPanel);
        centerPanel.add(lpOuterPanel);
        centerPanel.add(Box.createVerticalGlue());

        JPanel skTPOuter = createSpeckalKeyTargetOuterPanel();
        JPanel nkTPOuter = createNormalKeyTargetPanel();

        JPanel sourceKeyPanel = new JPanel();
        sourceKeyPanel.setLayout(new BoxLayout(sourceKeyPanel, BoxLayout.X_AXIS));
        sourceKeyPanel.add(skTPOuter);
        sourceKeyPanel.add(Box.createHorizontalStrut(10));
        sourceKeyPanel.add(nkTPOuter);
        sourceKeyPanel.add(Box.createHorizontalGlue());

        upperTargetPanel.add(sourceKeyPanel);

        add(centerPanel, BorderLayout.CENTER);

        myDestinationKeyListPanel = new DestinationKeyListPanel(myDataTypeKeyMoveDNDCoordinator);
        add(myDestinationKeyListPanel, BorderLayout.WEST);

        if (resultListener != null)
        {
            add(createBottomButtonPanel(), BorderLayout.SOUTH);
        }

        initializeSourcePanels();
    }

    /**
     * Adds the MergeMapChangeListener.
     *
     * @param listener the listener
     */
    public void addMergeMapChangeListenerListener(MergeMapChangeListener listener)
    {
        myChangeSupport.addListener(listener);
    }

    /**
     * Creates the new mapped type from entry.
     *
     * @param data the data
     * @param specialKey if special key
     */
    public void createNewMappedTypeFromEntry(TypeKeyEntry data, boolean specialKey)
    {
        if (specialKey)
        {
            boolean found = false;
            for (SpecialMappedTypeKeyPanel pnl : mySpecialMappedTypeKeyPanels)
            {
                if (pnl.getSpecialKey() == data.getSpecialKeyType())
                {
                    found = true;
                    pnl.addTypeKeyEntry(data);
                    final String specKeyType = pnl.getSpecialKey().getKeyName();
                    EventQueueUtilities.invokeLater(
                        () -> JOptionPane.showMessageDialog(null, "There is already a key for the special type \""
                                + specKeyType + "\". The entry was added to the existing type."));
                }
            }
            if (!found)
            {
                SpecialMappedTypeKeyPanel pnl = new SpecialMappedTypeKeyPanel(myDataTypeKeyMoveDNDCoordinator, this,
                        data.getKeyName(), data.getClassType(), false, data.getSpecialKeyType());
                pnl.addTypeKeyEntry(data);
                pnl.setEditable(myEditable);
                mySpecialMappedTypeKeyPanels.add(pnl);
                rebuildSpecialMappedTypeKeyPanels();
            }
        }
        else
        {
            MappedTypeKeyPanel pnl = new MappedTypeKeyPanel(myDataTypeKeyMoveDNDCoordinator, this, data.getKeyName(),
                    data.getClassType(), false);
            pnl.addTypeKeyEntry(data);
            pnl.setEditable(myEditable);
            myMappedTypeKeyPanels.add(pnl);
            rebuildMappedTypeKeyPanels();
        }
    }

    /**
     * Generate mapping configuration.
     *
     * @return the data type merge map
     */
    public DataTypeMergeMap generateMappingConfiguration()
    {
        List<MergeKeySpecification> mergeKeyNames = new ArrayList<>();
        Map<String, DataTypeMergeComponent> sourceTypeToComponentMap = new HashMap<>();
        DataTypeMergeComponent comp = null;
        List<MappedTypeKeyPanel> pnlList = new ArrayList<>();
        pnlList.addAll(mySpecialMappedTypeKeyPanels);
        pnlList.addAll(myMappedTypeKeyPanels);
        DataTypeMergeMap resultMap = new DataTypeMergeMap();
        Map<String, MetaDataInfo> dtiKeyToMDIMap = new HashMap<>();
        for (Map.Entry<DTINameKeyPair, MetaDataInfo> entry : myDataTypeKeyToMetaDataInfoMap.entrySet())
        {
            dtiKeyToMDIMap.put(entry.getKey().getDataTypeInfoKey(), entry.getValue());
        }

        for (MappedTypeKeyPanel pnl : pnlList)
        {
            Tuple2<MergeKeySpecification.ConversionHint, String> conversionTuple = pnl.getConversionHintAndMergeClassName();
            MergeKeySpecification mks = new MergeKeySpecification(pnl.getKeyName(), conversionTuple.getT2(),
                    conversionTuple.getT1());
            if (pnl instanceof SpecialMappedTypeKeyPanel)
            {
                mks.setSpecialKeyClassName(((SpecialMappedTypeKeyPanel)pnl).getSpecialKey().getClass().getName());
            }
            mergeKeyNames.add(mks);

            for (TypeKeyEntry tke : pnl.getTypeEntryList())
            {
                comp = sourceTypeToComponentMap.get(tke.getDataTypeKey());
                if (comp == null)
                {
                    comp = new DataTypeMergeComponent(tke.getDataTypeKey(), tke.getDataTypeDispName());
                    if (dtiKeyToMDIMap.get(tke.getDataTypeKey()) != null)
                    {
                        comp.getSourceKeyList().clear();
                        comp.getSourceKeyList()
                                .addAll(KeySpecification.createKeySpecification(dtiKeyToMDIMap.get(tke.getDataTypeKey())));
                    }
                    sourceTypeToComponentMap.put(tke.getDataTypeKey(), comp);
                }
                comp.addMetaDataMergeKeyMapEntry(new MetaDataMergeKeyMapEntry(pnl.getKeyName(), tke.getKeyName()));
            }
        }
        resultMap.getMergedKeyNames().addAll(mergeKeyNames);
        for (DataTypeMergeComponent entry : sourceTypeToComponentMap.values())
        {
            resultMap.getMergeComponents().add(entry);
        }

        return resultMap;
    }

    /**
     * Gets the destination key names.
     *
     * @param lowerCase the lower case
     * @return the destination key names
     */
    public Set<String> getDestinationKeyNames(boolean lowerCase)
    {
        Set<String> resultSet = new HashSet<>();
        for (MappedTypeKeyPanel pnl : mySpecialMappedTypeKeyPanels)
        {
            resultSet.add(lowerCase ? pnl.getKeyName().toLowerCase() : pnl.getKeyName());
        }
        for (MappedTypeKeyPanel pnl : myMappedTypeKeyPanels)
        {
            resultSet.add(lowerCase ? pnl.getKeyName().toLowerCase() : pnl.getKeyName());
        }
        return resultSet;
    }

    /**
     * Checks if is editable.
     *
     * @return true, if is editable
     */
    public boolean isEditable()
    {
        return myEditable;
    }

    /**
     * Checks if is merge valid.
     *
     * @return true, if is merge valid
     */
    public boolean isMergeValid()
    {
        boolean hasAtLeastOneMappedKey = false;
        for (SpecialMappedTypeKeyPanel pnl : mySpecialMappedTypeKeyPanels)
        {
            if (pnl.getKeyCount() > 1)
            {
                hasAtLeastOneMappedKey = true;
                break;
            }
        }

        if (!hasAtLeastOneMappedKey)
        {
            for (MappedTypeKeyPanel pnl : myMappedTypeKeyPanels)
            {
                if (pnl.getKeyCount() > 1)
                {
                    hasAtLeastOneMappedKey = true;
                    break;
                }
            }
        }

        return hasAtLeastOneMappedKey;
    }

    @Override
    public void keyMoveCompleted(TypeKeyEntry entry, TypeKeyPanel origPanel)
    {
        fireMergeMapUpdated();
    }

    @Override
    public void keyMoveInitiated(TypeKeyEntry entry, TypeKeyPanel sourcePanel, Object source)
    {
    }

    /**
     * Removes the mapped type.
     *
     * @param mappedTypeKeyPanel the mapped type key panel
     */
    public void removeMappedType(MappedTypeKeyPanel mappedTypeKeyPanel)
    {
        if (mappedTypeKeyPanel instanceof SpecialMappedTypeKeyPanel)
        {
            mySpecialMappedTypeKeyPanels.remove(mappedTypeKeyPanel);
        }
        else
        {
            myMappedTypeKeyPanels.remove(mappedTypeKeyPanel);
        }

        for (TypeKeyEntry entry : mappedTypeKeyPanel.getTypeEntryList())
        {
            for (SourceTypeKeyPanel skp : mySourceTypeKeyPanels)
            {
                if (skp.getTypeKey().equals(entry.getDataTypeKey()))
                {
                    mappedTypeKeyPanel.acceptedTransferOfEntry(entry);
                    skp.addTypeKeyEntry(entry);
                }
            }
        }

        if (mappedTypeKeyPanel instanceof SpecialMappedTypeKeyPanel)
        {
            rebuildSpecialMappedTypeKeyPanels();
        }
        else
        {
            rebuildMappedTypeKeyPanels();
        }
    }

    /**
     * Removes the MergeMapChangeListener.
     *
     * @param listener the listener
     */
    public void removeMergeMapChangeListenerListener(MergeMapChangeListener listener)
    {
        myChangeSupport.removeListener(listener);
    }

    /**
     * Return type entry to source.
     *
     * @param obj the obj
     */
    public void returnTypeEntryToSource(final TypeKeyEntry obj)
    {
        EventQueueUtilities.runOnEDT(new Runnable()
        {
            @Override
            public void run()
            {
                boolean moved = false;
                for (SourceTypeKeyPanel pnl : mySourceTypeKeyPanels)
                {
                    if (pnl.getTypeKey().equals(obj.getDataTypeKey()))
                    {
                        if (obj.getOwner() instanceof TypeKeyPanel)
                        {
                            ((TypeKeyPanel)obj.getOwner()).acceptedTransferOfEntry(obj);
                        }
                        pnl.addTypeKeyEntry(obj);
                        moved = true;
                    }
                }
                if (moved)
                {
                    rebuildMappedTypeKeyPanels();
                    rebuildSpecialMappedTypeKeyPanels();
                }
            }
        });
    }

    /**
     * Sets the editable state of the panel.
     *
     * @param editable the new editable
     */
    public void setEditable(boolean editable)
    {
        myEditable = editable;

        myResetMergeButton.setVisible(editable);
        myAutoMergeButton.setVisible(myEditable);
        myTopText.setText(myEditable ? EDIT_MESSAGE : VIEW_MESSAGE);
        for (MappedTypeKeyPanel pnl : myMappedTypeKeyPanels)
        {
            pnl.setEditable(editable);
        }

        for (SpecialMappedTypeKeyPanel pnl : mySpecialMappedTypeKeyPanels)
        {
            pnl.setEditable(editable);
        }

        for (SourceTypeKeyPanel pnl : mySourceTypeKeyPanels)
        {
            pnl.setEditable(editable);
        }
    }

    /**
     * Sets the from mapping configuration.
     *
     * @param tb the tb
     * @param map the map
     */
    public void setFromMappingConfiguration(final Toolbox tb, final DataTypeMergeMap map)
    {
        EventQueueUtilities.runOnEDT(new Runnable()
        {
            @Override
            public void run()
            {
                MantleToolbox mtb = null;
                if (tb != null)
                {
                    mtb = MantleToolboxUtils.getMantleToolbox(tb);
                }
                myLowerSourcePanel.removeAll();
                myDataTypeKeyToMetaDataInfoMap.clear();
                resetMergeInternal();
                mySourceTypeKeyPanels.clear();

                // Setup and build source panels.
                for (DataTypeMergeComponent comp : map.getMergeComponents())
                {
                    DTINameKeyPair pair = new DTINameKeyPair(comp.getDataTypeDisplayName(), comp.getDataTypeKey());
                    boolean foundInTool = false;
                    if (mtb != null && mtb.getDataTypeController().hasDataTypeInfoForTypeKey(comp.getDataTypeKey()))
                    {
                        DataTypeInfo dti = mtb.getDataTypeController().getDataTypeInfoForType(comp.getDataTypeKey());
                        if (dti.getMetaDataInfo() != null)
                        {
                            myDataTypeKeyToMetaDataInfoMap.put(pair, dti.getMetaDataInfo());
                            foundInTool = true;
                        }
                    }

                    if (!foundInTool)
                    {
                        DefaultMetaDataInfo dmdi = DataTypeMergeAssistant.createMetaDataInfo(comp.getSourceKeyList(), false,
                                null);
                        myDataTypeKeyToMetaDataInfoMap.put(pair, dmdi);
                    }
                }
                Map<String, SourceTypeKeyPanel> dtiToSourceTypeKeyPanelMap = initializeSourcePanels();

                // Setup and build mapped panels.
                Map<String, MappedTypeKeyPanel> keyToPanelMap = new HashMap<>();
                for (MergeKeySpecification mks : map.getMergedKeyNames())
                {
                    if (StringUtils.isNotEmpty(mks.getSpecialKeyClassName()))
                    {
                        SpecialKey sk = KeySpecification.getSpecialKeyForSpecialKeyClassName(mks.getSpecialKeyClassName());
                        SpecialMappedTypeKeyPanel pnl = new SpecialMappedTypeKeyPanel(myDataTypeKeyMoveDNDCoordinator,
                                DataTypeMergePanel.this, mks.getKeyName(), mks.getClassName(), false, sk);
                        pnl.setEditable(myEditable);
                        mySpecialMappedTypeKeyPanels.add(pnl);
                        keyToPanelMap.put(mks.getKeyName(), pnl);
                    }
                    else
                    {
                        MappedTypeKeyPanel pnl = new MappedTypeKeyPanel(myDataTypeKeyMoveDNDCoordinator, DataTypeMergePanel.this,
                                mks.getKeyName(), mks.getClassName(), false);
                        pnl.setEditable(myEditable);
                        myMappedTypeKeyPanels.add(pnl);
                        keyToPanelMap.put(mks.getKeyName(), pnl);
                    }
                }

                // Move the TypeKeyEntry(s) from the source panels to the
                // correct map panels.
                for (DataTypeMergeComponent comp : map.getMergeComponents())
                {
                    SourceTypeKeyPanel panel = dtiToSourceTypeKeyPanelMap.get(comp.getDataTypeKey());
                    Map<String, TypeKeyEntry> keyToEntryMap = panel.getMapOfKeyToTypeKeyEntry();
                    for (MetaDataMergeKeyMapEntry entry : comp.getMetaDataMergeKeyMapEntryList())
                    {
                        String srcKey = entry.getSourceKeyName();
                        String mrgKey = entry.getMergeKeyName();
                        MappedTypeKeyPanel destPanel = keyToPanelMap.get(mrgKey);
                        TypeKeyEntry tke = keyToEntryMap.get(srcKey);
                        if (tke != null)
                        {
                            destPanel.addTypeKeyEntry(tke);
                            panel.acceptedTransferOfEntry(tke);
                        }
                    }
                }

                rebuildSpecialMappedTypeKeyPanels();
                rebuildMappedTypeKeyPanels();
                revalidate();
            }
        });
    }

    /**
     * Target panel renamed.
     *
     * @param renamedPanel the renamed panel
     */
    public void targetPanelRenamed(MappedTypeKeyPanel renamedPanel)
    {
        if (renamedPanel instanceof SpecialMappedTypeKeyPanel)
        {
            rebuildSpecialMappedTypeKeyPanels();
        }
        else
        {
            rebuildMappedTypeKeyPanels();
        }
    }

    /**
     * Auto merge.
     */
    private void autoMerge()
    {
        Object[] options = new Object[2];
        options[1] = "Auto-merge using all available keys from source data types.";
        options[0] = "Auto-merge by pairing only special types and keys with similar names";
        Object result = JOptionPane.showInputDialog(this, "Please select auto merge type:", "Auto Merge Setup",
                JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
        if (result == options[1])
        {
            autoMerge(true);
        }
        else
        {
            autoMerge(false);
        }
    }

    /**
     * Auto merge.
     *
     * @param allUnassignedKeys the all unassigned keys
     */
    private void autoMerge(final boolean allUnassignedKeys)
    {
        EventQueueUtilities.runOnEDT(new Runnable()
        {
            @Override
            public void run()
            {
                Map<String, SpecialMappedTypeKeyPanel> specKeyNameToSpecPanelMap = new HashMap<>();
                Map<SpecialKey, SpecialMappedTypeKeyPanel> specKeyToSpecPanelMap = new HashMap<>();

                for (SpecialMappedTypeKeyPanel pnl : mySpecialMappedTypeKeyPanels)
                {
                    specKeyNameToSpecPanelMap.put(pnl.getKeyName(), pnl);
                    specKeyToSpecPanelMap.put(pnl.getSpecialKey(), pnl);
                }

                Map<String, MappedTypeKeyPanel> regKeyToSpecPanelMap = new HashMap<>();
                for (MappedTypeKeyPanel pnl : myMappedTypeKeyPanels)
                {
                    regKeyToSpecPanelMap.put(pnl.getKeyName().toLowerCase(), pnl);
                }

                List<TypeKeyEntry> unassignedEntryList = new ArrayList<>();
                for (SourceTypeKeyPanel pnl : mySourceTypeKeyPanels)
                {
                    unassignedEntryList.addAll(pnl.getTypeEntryList());
                }

                // Determine new special key panels.
                determineNewSpecialKeyPanelsAsPartOfMerge(specKeyNameToSpecPanelMap, specKeyToSpecPanelMap, unassignedEntryList);

                // Now work the remaining unassigned list
                // for the regular key panels.
                determineNewRegularKeyPanelsAsPartOfMerge(allUnassignedKeys, unassignedEntryList, regKeyToSpecPanelMap);

                // Rebuild the panels.
                rebuildMappedTypeKeyPanels();
                rebuildSpecialMappedTypeKeyPanels();
                fireMergeMapUpdated();
            }
        });
    }

    /**
     * Count column keys for merge.
     *
     * @param unassignedEntryList the unassigned entry list
     * @param regKeyToSpecPanelMap the reg key to spec panel map
     * @return the map
     */
    private Map<String, Integer> countColumnKeysForMerge(List<TypeKeyEntry> unassignedEntryList,
            Map<String, MappedTypeKeyPanel> regKeyToSpecPanelMap)
    {
        Map<String, Integer> keyNameToKeyCount = new HashMap<>();
        for (Map.Entry<String, MappedTypeKeyPanel> entry : regKeyToSpecPanelMap.entrySet())
        {
            String key = entry.getKey().toLowerCase();
            Integer val = keyNameToKeyCount.get(key);
            if (val == null)
            {
                keyNameToKeyCount.put(key, 1);
            }
        }
        for (TypeKeyEntry entry : unassignedEntryList)
        {
            String key = entry.getKeyName().toLowerCase();
            Integer val = keyNameToKeyCount.get(key);
            if (val == null)
            {
                keyNameToKeyCount.put(key, 1);
            }
            else
            {
                keyNameToKeyCount.put(key, val.intValue() + 1);
            }
        }
        return keyNameToKeyCount;
    }

    /**
     * Creates the auto merge button.
     *
     * @return the j button
     */
    private JButton createAutoMergeButton()
    {
        myAutoMergeButton = new JButton("<html>Auto<br>Merge</html>");
        myAutoMergeButton.setFocusable(false);
        myAutoMergeButton.addActionListener(e -> autoMerge());
        return myAutoMergeButton;
    }

    /**
     * Creates the bottom button panel.
     *
     * @return the j panel
     */
    private JPanel createBottomButtonPanel()
    {
        JPanel panel = new JPanel(new GridLayout(1, 2, 40, 0));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 400, 10, 400));
        JButton okButton = new JButton("OK");
        okButton.addActionListener(e -> mergeComplete());
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> mergeCancelled());
        panel.add(okButton);
        panel.add(cancelButton);
        return panel;
    }

    /**
     * Creates the drop outer panel.
     *
     * @return the j panel
     */
    private JPanel createDropOuterPanel()
    {
        JPanel dropPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        dropPanel.setBorder(BorderFactory.createEmptyBorder(3, 10, 3, 10));
        NewKeyDropTargetPanel newSpecialKeyTargetPanel = new NewKeyDropTargetPanel(myDataTypeKeyMoveDNDCoordinator, this,
                new BorderLayout(), "Drop special key here to create\nnew special type", true);
        newSpecialKeyTargetPanel.setMinimumSize(new Dimension(70, 45));
        newSpecialKeyTargetPanel.setPreferredSize(new Dimension(70, 45));

        NewKeyDropTargetPanel newNormalKeyTargetPanel = new NewKeyDropTargetPanel(myDataTypeKeyMoveDNDCoordinator, this,
                new BorderLayout(), "Drop key here to create\nnew regular type", false);
        newNormalKeyTargetPanel.setMinimumSize(new Dimension(70, 40));
        newNormalKeyTargetPanel.setPreferredSize(new Dimension(70, 40));

        dropPanel.add(newSpecialKeyTargetPanel);
        dropPanel.add(newNormalKeyTargetPanel);

        JPanel dropOuterPanel = new JPanel(new BorderLayout());
        dropOuterPanel.setMaximumSize(new Dimension(5000, 50));
        dropOuterPanel.add(dropPanel, BorderLayout.CENTER);
        JPanel autoMergePanel = new JPanel(new GridLayout(1, 2, 5, 0));
        JButton autoMergeButton = createAutoMergeButton();
        JButton resetButton = createResetButton();
        autoMergePanel.add(autoMergeButton);
        autoMergePanel.add(resetButton);
        autoMergePanel.setBorder(BorderFactory.createEmptyBorder(3, 0, 3, 10));
        dropOuterPanel.add(autoMergePanel, BorderLayout.EAST);
        return dropOuterPanel;
    }

    /**
     * Creates the normal key target panel.
     *
     * @return the j panel
     */
    private JPanel createNormalKeyTargetPanel()
    {
        myNormalKeyTargetPanel = new JPanel();
        myNormalKeyTargetPanel.setLayout(new BoxLayout(myNormalKeyTargetPanel, BoxLayout.X_AXIS));
        myNormalKeyTargetPanel.setBorder(BorderFactory.createEtchedBorder());

        JPanel nkTPOuter = new JPanel(new BorderLayout());
        JLabel nkLb = new JLabel("Regular Types");
        nkLb.setHorizontalAlignment(SwingConstants.CENTER);
        nkTPOuter.setMinimumSize(new Dimension(100, 100));
        nkTPOuter.add(nkLb, BorderLayout.NORTH);
        nkTPOuter.add(myNormalKeyTargetPanel, BorderLayout.CENTER);
        return nkTPOuter;
    }

    /**
     * Creates the reset button.
     *
     * @return the j button
     */
    private JButton createResetButton()
    {
        myResetMergeButton = new JButton("<html>Reset<br>Merge</html>");
        myResetMergeButton.setFocusable(false);
        myResetMergeButton.addActionListener(e -> resetMerge());
        return myResetMergeButton;
    }

    /**
     * Creates the speckal key target outer panel.
     *
     * @return the j panel
     */
    private JPanel createSpeckalKeyTargetOuterPanel()
    {
        mySpecialKeyTargetPanel = new JPanel();
        mySpecialKeyTargetPanel.setBorder(BorderFactory.createEtchedBorder());
        mySpecialKeyTargetPanel.setLayout(new BoxLayout(mySpecialKeyTargetPanel, BoxLayout.X_AXIS));

        JPanel skTPOuter = new JPanel(new BorderLayout());
        skTPOuter.setMinimumSize(new Dimension(100, 100));
        skTPOuter.setBackground(new Color(134, 127, 172));
        JLabel skLb = new JLabel("Special Types");
        skLb.setHorizontalAlignment(SwingConstants.CENTER);
        skTPOuter.add(skLb, BorderLayout.NORTH);
        skTPOuter.add(mySpecialKeyTargetPanel, BorderLayout.CENTER);
        return skTPOuter;
    }

    /**
     * Creates the top text panel.
     *
     * @return the j panel
     */
    private JPanel createTopTextPanel()
    {
        JPanel textPanel = new JPanel(new BorderLayout());
        textPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 10));
        myTopText = getTextAreaWithMessage(EDIT_MESSAGE, 4);
        textPanel.add(myTopText, BorderLayout.CENTER);
        return textPanel;
    }

    /**
     * Determine new regular key panels as part of merge.
     *
     * @param allUnassignedKeys the all unassigned keys
     * @param unassignedEntryList the unassigned entry list
     * @param regKeyToSpecPanelMap the reg key to spec panel map
     */
    private void determineNewRegularKeyPanelsAsPartOfMerge(boolean allUnassignedKeys, List<TypeKeyEntry> unassignedEntryList,
            Map<String, MappedTypeKeyPanel> regKeyToSpecPanelMap)
    {
        Set<String> existingKeyNames = getDestinationKeyNames(true);
        Map<String, Integer> keyNameToKeyCount = countColumnKeysForMerge(unassignedEntryList, regKeyToSpecPanelMap);

        Iterator<TypeKeyEntry> tkeItr = unassignedEntryList.iterator();
        TypeKeyEntry tke = null;
        while (tkeItr.hasNext())
        {
            tke = tkeItr.next();
            String key = tke.getKeyName().toLowerCase();
            int count = keyNameToKeyCount.get(key);

            boolean addToPanel = allUnassignedKeys || count > 1;
            if (addToPanel)
            {
                MappedTypeKeyPanel pnl = regKeyToSpecPanelMap.get(key);
                if (pnl == null)
                {
                    // Determine a unique new key name.
                    String newKeyNameBase = tke.getKeyName();
                    String newKeyName = newKeyNameBase;
                    int counter = 0;
                    while (existingKeyNames.contains(newKeyName.toLowerCase()))
                    {
                        counter++;
                        newKeyName = newKeyNameBase + "_" + Integer.toString(counter);
                    }

                    pnl = new MappedTypeKeyPanel(myDataTypeKeyMoveDNDCoordinator, this, newKeyName, tke.getClassType(), false);
                    pnl.setEditable(myEditable);
                    regKeyToSpecPanelMap.put(key, pnl);
                    myMappedTypeKeyPanels.add(pnl);
                    existingKeyNames.add(newKeyName);
                }

                Object owner = tke.getOwner();
                if (owner instanceof TypeKeyPanel)
                {
                    ((TypeKeyPanel)owner).acceptedTransferOfEntry(tke);
                }
                pnl.addTypeKeyEntry(tke);
                tkeItr.remove();
            }
        }
    }

    /**
     * Determine new special key panels as part of merge.
     *
     * @param specKeyNameToSpecPanelMap the spec key name to spec panel map
     * @param specKeyToSpecPanelMap the spec key to spec panel map
     * @param unassignedEntryList the unassigned entry list
     */
    private void determineNewSpecialKeyPanelsAsPartOfMerge(Map<String, SpecialMappedTypeKeyPanel> specKeyNameToSpecPanelMap,
            Map<SpecialKey, SpecialMappedTypeKeyPanel> specKeyToSpecPanelMap, List<TypeKeyEntry> unassignedEntryList)
    {
        // Special keys first.
        Set<String> existingKeyNames = getDestinationKeyNames(true);
        Iterator<TypeKeyEntry> tkeItr = unassignedEntryList.iterator();
        TypeKeyEntry tke = null;
        while (tkeItr.hasNext())
        {
            tke = tkeItr.next();
            if (tke.getSpecialKeyType() != null)
            {
                SpecialMappedTypeKeyPanel pnl = specKeyToSpecPanelMap.get(tke.getSpecialKeyType());
                if (pnl == null)
                {
                    // Determine a unique new key name.
                    String newKeyNameBase = tke.getKeyName();
                    String newKeyName = newKeyNameBase;
                    int counter = 0;
                    while (existingKeyNames.contains(newKeyName.toLowerCase()))
                    {
                        counter++;
                        newKeyName = newKeyNameBase + "_" + Integer.toString(counter);
                    }

                    pnl = new SpecialMappedTypeKeyPanel(myDataTypeKeyMoveDNDCoordinator, this, newKeyName, tke.getClassType(),
                            false, tke.getSpecialKeyType());
                    pnl.setEditable(myEditable);
                    specKeyToSpecPanelMap.put(tke.getSpecialKeyType(), pnl);
                    specKeyNameToSpecPanelMap.put(tke.getKeyName(), pnl);
                    mySpecialMappedTypeKeyPanels.add(pnl);
                    existingKeyNames.add(newKeyName);
                }
                Object owner = tke.getOwner();
                if (owner instanceof TypeKeyPanel)
                {
                    ((TypeKeyPanel)owner).acceptedTransferOfEntry(tke);
                }
                pnl.addTypeKeyEntry(tke);
                tkeItr.remove();
            }
        }
    }

    /**
     * Fire merge map updated.
     */
    private void fireMergeMapUpdated()
    {
        myChangeSupport.notifyListeners(listener -> listener.mergeMapChanged(), new EventQueueExecutor());
    }

    /**
     * Gets the text area with message.
     *
     * @param message the message
     * @param sizeAdjsut the size adjsut
     * @return the text area with message
     */
    private JTextArea getTextAreaWithMessage(String message, int sizeAdjsut)
    {
        JTextArea jta = new JTextArea();
        jta.setLineWrap(true);
        jta.setEditable(false);
        jta.setWrapStyleWord(true);
        jta.setFont(jta.getFont().deriveFont(Font.BOLD, jta.getFont().getSize() + sizeAdjsut));
        jta.setBackground(new JPanel().getBackground());
        jta.setBorder(BorderFactory.createEmptyBorder());
        jta.setText(message);
        return jta;
    }

    /**
     * Initialize panels.
     *
     * @return the map of dti key to {@link SourceTypeKeyPanel}.
     */
    private Map<String, SourceTypeKeyPanel> initializeSourcePanels()
    {
        Map<String, SourceTypeKeyPanel> typeKeyToPanelMap = new HashMap<>();
        List<DTINameKeyPair> typeList = new ArrayList<>(myDataTypeKeyToMetaDataInfoMap.keySet());
        Collections.sort(typeList, new DTINameKeyPair.CompareByDisplayName());

        for (DTINameKeyPair type : typeList)
        {
            SourceTypeKeyPanel tkp = new SourceTypeKeyPanel(type.getDataTypeInfoDispName(), type.getDataTypeInfoKey(),
                    myDataTypeKeyMoveDNDCoordinator);
            tkp.setEditable(myEditable);
            tkp.setTypeEntryList(
                    DataTypeMergeUtils.createTypeKeyEntriesFromMetaDataInfo(type, myDataTypeKeyToMetaDataInfoMap.get(type)));
            mySourceTypeKeyPanels.add(tkp);
            myLowerSourcePanel.add(tkp);
            typeKeyToPanelMap.put(type.getDataTypeInfoKey(), tkp);
        }
        return typeKeyToPanelMap;
    }

    /**
     * Merge cancelled.
     */
    private void mergeCancelled()
    {
        if (myDataTypeMergeResultListener != null)
        {
            myDataTypeMergeResultListener.mergeCancelled();
        }
    }

    /**
     * Merge complete.
     */
    private void mergeComplete()
    {
        if (myDataTypeMergeResultListener != null)
        {
            DataTypeMergeMap map = generateMappingConfiguration();
            myDataTypeMergeResultListener.mergeComplete(map);
        }
    }

    /**
     * Rebuild mapped type key panels.
     */
    private void rebuildMappedTypeKeyPanels()
    {
        EventQueueUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                myNormalKeyTargetPanel.removeAll();

                Collections.sort(myMappedTypeKeyPanels, new Comparator<MappedTypeKeyPanel>()
                {
                    @Override
                    public int compare(MappedTypeKeyPanel o1, MappedTypeKeyPanel o2)
                    {
                        return o1.getKeyName().compareTo(o2.getKeyName());
                    }
                });

                for (MappedTypeKeyPanel pnl : myMappedTypeKeyPanels)
                {
                    myNormalKeyTargetPanel.add(pnl);
                }
                refreshMergedKeynames();
                myNormalKeyTargetPanel.revalidate();
            }
        });
    }

    /**
     * Rebuild special mapped type key panels.
     */
    private void rebuildSpecialMappedTypeKeyPanels()
    {
        EventQueueUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                mySpecialKeyTargetPanel.removeAll();

                Collections.sort(mySpecialMappedTypeKeyPanels, new Comparator<MappedTypeKeyPanel>()
                {
                    @Override
                    public int compare(MappedTypeKeyPanel o1, MappedTypeKeyPanel o2)
                    {
                        return o1.getKeyName().compareTo(o2.getKeyName());
                    }
                });

                for (SpecialMappedTypeKeyPanel pnl : mySpecialMappedTypeKeyPanels)
                {
                    mySpecialKeyTargetPanel.add(pnl);
                }

                refreshMergedKeynames();
                mySpecialKeyTargetPanel.revalidate();
            }
        });
    }

    /**
     * Refresh merged keynames.
     */
    private void refreshMergedKeynames()
    {
        EventQueueUtilities.runOnEDT(new Runnable()
        {
            @Override
            public void run()
            {
                DefaultListModel<MappedTypeKeyPanelProxy> dlm = new DefaultListModel<>();
                for (SpecialMappedTypeKeyPanel pnl : mySpecialMappedTypeKeyPanels)
                {
                    dlm.addElement(new MappedTypeKeyPanelProxy(pnl));
                }

                for (MappedTypeKeyPanel pnl : myMappedTypeKeyPanels)
                {
                    dlm.addElement(new MappedTypeKeyPanelProxy(pnl));
                }
                myDestinationKeyListPanel.setModel(dlm);
            }
        });
    }

    /**
     * Reset merge.
     */
    private void resetMerge()
    {
        EventQueueUtilities.runOnEDT(() -> resetMergeInternal());
    }

    /**
     * Reset merge internal.
     */
    private void resetMergeInternal()
    {
        int opt = JOptionPane.showConfirmDialog(DataTypeMergePanel.this,
                "Are you sure you want to reset the merge configuration?", "Confirm Merge Reset", JOptionPane.OK_CANCEL_OPTION);

        if (opt == JOptionPane.OK_OPTION)
        {
            restoreFromMappedTypePanelListToSourcePanelList(myMappedTypeKeyPanels);
            restoreFromMappedTypePanelListToSourcePanelList(mySpecialMappedTypeKeyPanels);

            myMappedTypeKeyPanels.clear();
            mySpecialMappedTypeKeyPanels.clear();
            rebuildMappedTypeKeyPanels();
            rebuildSpecialMappedTypeKeyPanels();
            fireMergeMapUpdated();
        }
    }

    /**
     * Restore from mapped type panel list to source panel list.
     *
     * @param pnlList the pnl list
     */
    private void restoreFromMappedTypePanelListToSourcePanelList(List<? extends MappedTypeKeyPanel> pnlList)
    {
        Map<String, SourceTypeKeyPanel> typeToTypePanelMap = new HashMap<>();
        for (SourceTypeKeyPanel pnl : mySourceTypeKeyPanels)
        {
            typeToTypePanelMap.put(pnl.getTypeKey(), pnl);
        }

        for (MappedTypeKeyPanel pnl : pnlList)
        {
            if (!pnl.getListModel().isEmpty())
            {
                Object[] objects = pnl.getListModel().toArray();
                pnl.getListModel().clear();

                if (objects != null)
                {
                    for (Object obj : objects)
                    {
                        if (obj instanceof TypeKeyEntry)
                        {
                            TypeKeyEntry entry = (TypeKeyEntry)obj;
                            entry.setOwner(null);
                            SourceTypeKeyPanel sp = typeToTypePanelMap.get(entry.getDataTypeKey());
                            sp.addTypeKeyEntry(entry);
                        }
                    }
                }
            }
        }
    }

    /**
     * Merge map change listener.
     */
    @FunctionalInterface
    public interface MergeMapChangeListener
    {
        /**
         * Merge map changed.
         */
        void mergeMapChanged();
    }
}

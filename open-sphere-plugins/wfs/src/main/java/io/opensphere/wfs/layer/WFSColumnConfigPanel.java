package io.opensphere.wfs.layer;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import io.opensphere.core.common.collapsablepanel.HorizontalSpacerForGridbag;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.SpecialKey;
import io.opensphere.mantle.data.impl.specialkey.EndTimeKey;
import io.opensphere.mantle.data.impl.specialkey.LatitudeKey;
import io.opensphere.mantle.data.impl.specialkey.LongitudeKey;
import io.opensphere.mantle.data.impl.specialkey.TimeKey;

/**
 * The Main WFSConfigDialog class.
 */
@SuppressWarnings("PMD.GodClass")
public final class WFSColumnConfigPanel extends JPanel
{
    /** Default Serial Version UID. */
    private static final long serialVersionUID = 1L;

    /** The collection of check boxes. */
    private final List<ColumnCheckbox> myColumnCheckBoxes = new ArrayList<>();

    /** The layer we are managing columns for. */
    private final transient WFSDataType myDataType;

    /** The Layer detail panel. */
    private JPanel myLayerDetailPanel;

    /** The Select panel. */
    private JPanel mySelectPanel;

    /** The Select all button. */
    private JButton mySelectAllButton;

    /** The Select none button. */
    private JButton mySelectNoneButton;

    /** The Column scroll pane. */
    private JScrollPane myColumnSP;

    /** The Save cancel panel. */
    private JPanel mySaveCancelPanel;

    /** The Save button. */
    private JButton mySaveButton;

    /** The Cancel button. */
    private JButton myCancelButton;

    /** The Auto disable empty check box. */
    private JCheckBox myAutoDisableEmptyCheckBox;

    /** The Select action listener. */
    private transient ActionListener mySelectActionListener;

    /** The Column set. */
    private Set<String> myColumnSet;

    /** This listener will fire an event when the parent dialog is disposed. */
    private ActionListener myCloseListener;

    /** The dialog that will contain this panel. */
    private final JDialog myParent;

    /** The Required keys. */
    private static final List<SpecialKey> ourRequiredKeys;

    static
    {
        List<SpecialKey> tempKeyList = New.list(5);
        tempKeyList.add(LatitudeKey.DEFAULT);
        tempKeyList.add(LongitudeKey.DEFAULT);
        tempKeyList.add(TimeKey.DEFAULT);
        tempKeyList.add(EndTimeKey.DEFAULT);
        ourRequiredKeys = Collections.unmodifiableList(tempKeyList);
    }

    /** The WFS meta data. */
    private final transient WFSMetaDataInfo myWFSMetaData;

    /**
     * Instantiates a new wFS column config dialog.
     *
     * @param layer the layer
     * @param parent the parent
     * @param isLocked the is locked
     */
    public WFSColumnConfigPanel(WFSDataType layer, JDialog parent, boolean isLocked)
    {
        super();
        setLayout(new GridBagLayout());
        myParent = parent;
        myParent.setTitle("Column Filter");
        myDataType = layer;
        myWFSMetaData = (WFSMetaDataInfo)myDataType.getMetaDataInfo();

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(10, 0, 5, 0);
        gbc.weightx = 1.0;
        add(getLayerDetailPanel(), gbc);

        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 5, 0);
        add(getSelectPanel(), gbc);

        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.weighty = 1.0;
        add(getColumnSP(), gbc);

        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridy++;
        gbc.insets = new Insets(5, 0, 10, 0);
        gbc.weightx = 1.0;
        gbc.weighty = 0;
        add(getSaveCancelPanel(), gbc);
    }

    /**
     * Adds the dialog closed listener.
     *
     * @param closeListener the close listener
     */
    public void addDialogClosedListener(ActionListener closeListener)
    {
        myCloseListener = closeListener;
    }

    /**
     * Save the current state into the layer columns configuration.
     */
    public void save()
    {
        Set<String> deselected = new HashSet<>();
        for (ColumnCheckbox cb : myColumnCheckBoxes)
        {
            if (!cb.isSelected())
            {
                deselected.add(cb.getColumnName());
            }
        }

        // Remove this column set. It will either be the all set or
        // we will be adding a new set of columns.
        myWFSMetaData.removeColumnConfig(false);

        WFSLayerConfig layerConfig = new WFSLayerConfig();
        layerConfig.setLayerKey(myDataType.getTypeKey());
        layerConfig.setDeselectedColumns(deselected);
        layerConfig.setAutomaticallyDisableEmptyColumns(getAutoDisableEmptyCheckBox().isSelected());
        myWFSMetaData.saveColumnConfig(layerConfig);

        myColumnSet = myWFSMetaData.getCurrentColumnSet();
        setSaveButtonState(false);
    }

    /**
     * Checks to see if the original set of columns matches the currently
     * selected set of columns and sets the state of the save button
     * accordingly.
     *
     * @param checkAll flag that indicates that we should check to see if all
     *            columns have been selected or not
     */
    private void checkColumnSetState(boolean checkAll)
    {
        myColumnSet = myWFSMetaData.getCurrentColumnSet();
        boolean columnSetChanged = false;
        boolean allChecked = true;
        for (ColumnCheckbox aCheckbox : myColumnCheckBoxes)
        {
            if (!aCheckbox.isSelected())
            {
                allChecked = false;
                break;
            }
        }
        boolean autoDisableChanged = myWFSMetaData.isAutomaticallyDisableEmptyColumns() != getAutoDisableEmptyCheckBox()
                .isSelected();
        if (!myColumnSet.isEmpty())
        {
            for (ColumnCheckbox aCheckbox : myColumnCheckBoxes)
            {
                // If we've selected a checkbox not in the set, enable the save
                // button.
                if (aCheckbox.isSelected() && !myColumnSet.contains(aCheckbox.getColumnName()))
                {
                    columnSetChanged = true;
                    break;
                }
                // If we've unselected a checkbox in the set, enable the save
                // button.
                else if (!aCheckbox.isSelected() && myColumnSet.contains(aCheckbox.getColumnName()))
                {
                    columnSetChanged = true;
                    break;
                }
            }
            setSaveButtonState(columnSetChanged || autoDisableChanged);
        }
        else
        {
            // In this case, there has not been a column filter
            // saved for this data type.
            for (ColumnCheckbox aCheckbox : myColumnCheckBoxes)
            {
                if (!aCheckbox.isSelected())
                {
                    columnSetChanged = false;
                }
            }
            setSaveButtonState(!columnSetChanged || autoDisableChanged);
        }
        if (allChecked && checkAll)
        {
            getAutoDisableEmptyCheckBox().setSelected(false);
        }
    }

    /**
     * Close the config panel.
     */
    private void closeConfigPanel()
    {
        if (myCloseListener != null)
        {
            myCloseListener.actionPerformed(new ActionEvent(this, 0, "CLOSED"));
        }
        myParent.dispose();
    }

    /**
     * Gets the auto disable empty check box.
     *
     * @return the auto disable empty check box
     */
    private JCheckBox getAutoDisableEmptyCheckBox()
    {
        if (myAutoDisableEmptyCheckBox == null)
        {
            myAutoDisableEmptyCheckBox = new JCheckBox("Auto Disable Empty Columns",
                    myWFSMetaData.isAutomaticallyDisableEmptyColumns());
            myAutoDisableEmptyCheckBox.setToolTipText(
                    "If the system determines that a column is always empty it will be turned off automatically.");
            myAutoDisableEmptyCheckBox.setFocusPainted(false);
            myAutoDisableEmptyCheckBox.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent evt)
                {
                    if (evt.getSource() instanceof JCheckBox)
                    {
                        checkColumnSetState(false);
                    }
                }
            });
        }
        return myAutoDisableEmptyCheckBox;
    }

    /**
     * Gets the cancel button.
     *
     * @return the cancel button
     */
    private JButton getCancelButton()
    {
        if (myCancelButton == null)
        {
            myCancelButton = new JButton("Cancel");
            myCancelButton.setSize(75, 20);
            myCancelButton.setPreferredSize(myCancelButton.getSize());
            myCancelButton.setMinimumSize(myCancelButton.getSize());
            myCancelButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    closeConfigPanel();
                }
            });
        }
        return myCancelButton;
    }

    /**
     * Gets the column panel.
     *
     * @return the column panel
     */
    private Component getColumnPanel()
    {
        JPanel columnPanel = new JPanel();
        columnPanel.setLayout(new BoxLayout(columnPanel, BoxLayout.Y_AXIS));
        columnPanel.setBorder(new EmptyBorder(new Insets(5, 15, 10, 20)));
        myColumnSet = myWFSMetaData.getCurrentColumnSet();
        for (String column : myWFSMetaData.getUnfilteredSortedKeyNames())
        {
            ColumnCheckbox columnCB = null;
            SpecialKey key = myWFSMetaData.getSpecialTypeForKey(column);
            if (key == null)
            {
                key = myWFSMetaData.getUnfilteredSpecialTypeForKey(column);
            }
            if (key != null && ourRequiredKeys.contains(key))
            {
                columnCB = new ColumnCheckbox(column, key, true);
            }
            else
            {
                columnCB = new ColumnCheckbox(column, null, true);
            }
            columnCB.setFocusPainted(false);
            columnCB.setMargin(new Insets(0, 10, 0, 10));
            columnCB.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent evt)
                {
                    if (evt.getSource() instanceof JCheckBox)
                    {
                        checkColumnSetState(true);
                    }
                }
            });
            if (!myColumnSet.isEmpty() && !myColumnSet.contains(column))
            {
                columnCB.setSelected(false);
            }
            columnPanel.add(columnCB);
            myColumnCheckBoxes.add(columnCB);
        }

        return columnPanel;
    }

    /**
     * Gets the column scroll pane.
     *
     * @return the column scroll pane.
     */
    private JScrollPane getColumnSP()
    {
        if (myColumnSP == null)
        {
            myColumnSP = new JScrollPane(getColumnPanel());
            myColumnSP.setWheelScrollingEnabled(true);
            myColumnSP.getVerticalScrollBar().setUnitIncrement(10);
        }
        return myColumnSP;
    }

    /**
     * Gets the layer detail panel.
     *
     * @return the layer detail panel
     */
    private JPanel getLayerDetailPanel()
    {
        if (myLayerDetailPanel == null)
        {
            myLayerDetailPanel = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.insets = new Insets(0, 0, 0, 0);
            gbc.weightx = 1.0;
            myLayerDetailPanel.add(new JLabel(myDataType.getSourcePrefix()));
            gbc.gridy++;
            String title = myDataType.getDisplayName();
            JTextField aField = new JTextField();
            aField.setEditable(false);
            DataGroupInfo dgi = myDataType.getParent();
            if (dgi != null)
            {
                title = dgi.getDisplayName();
            }
            aField.setText(title);

            myLayerDetailPanel.add(aField, gbc);
        }
        return myLayerDetailPanel;
    }

    /**
     * Gets the save button.
     *
     * @return the save button
     */
    private JButton getSaveButton()
    {
        if (mySaveButton == null)
        {
            mySaveButton = new JButton("Save");
            mySaveButton.setSize(75, 20);
            mySaveButton.setPreferredSize(mySaveButton.getSize());
            mySaveButton.setMinimumSize(mySaveButton.getSize());
            mySaveButton.setMultiClickThreshhold(500);
            mySaveButton.setEnabled(false);
            mySaveButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    save();
                    closeConfigPanel();
                }
            });
        }
        return mySaveButton;
    }

    /**
     * Gets the button panel.
     *
     * @return the button panel
     */
    private JPanel getSaveCancelPanel()
    {
        if (mySaveCancelPanel == null)
        {
            mySaveCancelPanel = new JPanel(new GridBagLayout());

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(0, 0, 0, 25);
            gbc.gridx = 0;
            gbc.gridy = 0;
            mySaveCancelPanel.add(getSaveButton(), gbc);

            gbc.insets = new Insets(0, 0, 0, 0);
            gbc.gridx = 1;
            mySaveCancelPanel.add(getCancelButton(), gbc);
        }
        return mySaveCancelPanel;
    }

    /**
     * Gets the select all/none action listener.
     *
     * @return the select action listener
     */
    private ActionListener getSelectActionListener()
    {
        if (mySelectActionListener == null)
        {
            mySelectActionListener = new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    if (e.getSource() == mySelectAllButton || e.getSource() == mySelectNoneButton)
                    {
                        boolean allSelected = e.getSource() == mySelectAllButton;
                        for (ColumnCheckbox cb : myColumnCheckBoxes)
                        {
                            // Only set selected if the checkbox does not have a
                            // special key
                            if (cb.getSpecialKey() == null)
                            {
                                cb.setSelected(allSelected);
                            }
                        }
                        checkColumnSetState(true);
                    }
                }
            };
        }
        return mySelectActionListener;
    }

    /**
     * Gets the select all button.
     *
     * @return the select all button
     */
    private JButton getSelectAllButton()
    {
        if (mySelectAllButton == null)
        {
            mySelectAllButton = new JButton("All");
            mySelectAllButton.setMargin(new Insets(2, 12, 2, 12));
            mySelectAllButton.addActionListener(getSelectActionListener());
            mySelectAllButton.setFocusable(false);
        }
        return mySelectAllButton;
    }

    /**
     * Gets the select none button.
     *
     * @return the select none button
     */
    private JButton getSelectNoneButton()
    {
        if (mySelectNoneButton == null)
        {
            mySelectNoneButton = new JButton("None");
            mySelectNoneButton.setMargin(new Insets(2, 6, 2, 6));
            mySelectNoneButton.addActionListener(getSelectActionListener());
            mySelectNoneButton.setFocusable(false);
        }
        return mySelectNoneButton;
    }

    /**
     * Gets the select panel.
     *
     * @return the select panel
     */
    private JPanel getSelectPanel()
    {
        if (mySelectPanel == null)
        {
            mySelectPanel = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.insets = new Insets(0, 3, 0, 0);
            mySelectPanel.add(new JLabel("Columns:"), gbc);

            HorizontalSpacerForGridbag hs = new HorizontalSpacerForGridbag(1, 0);
            mySelectPanel.add(hs, hs.getGBConst());

            gbc.gridx = 2;
            gbc.insets = new Insets(0, 0, 0, 3);
            mySelectPanel.add(getSelectAllButton(), gbc);

            gbc.gridx = 3;
            gbc.insets = new Insets(0, 0, 0, 3);
            mySelectPanel.add(getSelectNoneButton(), gbc);
        }
        return mySelectPanel;
    }

    /**
     * Sets the save button state.
     *
     * @param enabled the new save button state
     */
    private void setSaveButtonState(boolean enabled)
    {
        getSaveButton().setEnabled(enabled);
    }

    /**
     * Setting a SpecialKey for this class will effectively make it a required
     * column for a data type.
     */
    static class ColumnCheckbox extends JCheckBox
    {
        /** Serial. */
        private static final long serialVersionUID = 1L;

        /** The Special key. */
        private final SpecialKey mySpecialKey;

        /** The Column name. */
        private final String myColumnName;

        /**
         * Instantiates a new column checkbox.
         *
         * @param text the text
         * @param key the key
         * @param flat the flat
         */
        public ColumnCheckbox(String text, SpecialKey key, boolean flat)
        {
            super(key == null ? text : text + " (required)", flat);
            if (key != null)
            {
                setSelected(true);
                setEnabled(false);
            }
            myColumnName = text;
            mySpecialKey = key;
        }

        /**
         * Gets the column name.
         *
         * @return the column name
         */
        public String getColumnName()
        {
            return myColumnName;
        }

        /**
         * Gets the special key.
         *
         * @return the special key
         */
        public SpecialKey getSpecialKey()
        {
            return mySpecialKey;
        }
    }
}

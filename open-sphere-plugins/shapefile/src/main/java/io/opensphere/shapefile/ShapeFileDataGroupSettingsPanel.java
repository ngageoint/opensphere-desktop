package io.opensphere.shapefile;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.bric.swing.ColorPicker;

import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.util.swing.ColorCircleIcon;
import io.opensphere.core.util.swing.DateTimePickerPanel;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.TimeExtents;
import io.opensphere.mantle.util.ColumnChooserPanel;
import io.opensphere.mantle.util.MantleToolboxUtils;
import io.opensphere.shapefile.config.v1.ShapeFileSource;

/**
 * The Class CSVDataGroupSettingsPanel.
 */
@SuppressWarnings("serial")
public class ShapeFileDataGroupSettingsPanel extends JPanel implements ActionListener
{
    /** The column filter bt. */
    private final JButton myColumnsFilterBT;

    /** The source. */
    private final ShapeFileSource myShpaeFileSource;

    /** The time filter panel. */
    private JPanel myStartAndEndTimeFilterPanel;

    /** The use date filter check box. */
    private JCheckBox myUseTimeLoadRangeFilterCheckBox;

    /** The date filter panel. */
    private JPanel myTimeLoadRangeFilterPanel;

    /** The color button. */
    private JButton myFeatureColorButton;

    /** The min date time picker. */
    private DateTimePickerPanel myMinimumTimePicker;

    /** The max date time picker. */
    private DateTimePickerPanel myMaximumTimePicker;

    /** The data group info. */
    private final DataGroupInfo myDGI;

    /** The controller. */
    private final ShapeFileDataSourceController mySourceController;

    /**
     * Adds the to disabled if not minus one.
     *
     * @param aSet the a set to investigate
     * @param colNames the list of column names.
     * @param colSetting the column setting (-1 means not included).
     */
    private static void addToDisabledIfNotMinusOne(Set<String> aSet, List<String> colNames, int colSetting)
    {
        if (colSetting != -1)
        {
            aSet.add(colNames.get(colSetting));
        }
    }

    /**
     * Instantiates a new cSV data group settings panel.
     *
     * @param controller the controller
     * @param source the source
     * @param dataGroupInfo the data group info
     */
    @SuppressWarnings("PMD.ConstructorCallsOverridableMethod")
    public ShapeFileDataGroupSettingsPanel(ShapeFileDataSourceController controller, ShapeFileSource source,
            DataGroupInfo dataGroupInfo)
    {
        super();
        myShpaeFileSource = source;
        myDGI = dataGroupInfo;
        mySourceController = controller;
        setMaximumSize(new Dimension(1000, 400));

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(5, 20, 5, 5));

        Box colorBox = Box.createHorizontalBox();
        colorBox.add(new JLabel("Default Feature Color"));
        colorBox.add(Box.createRigidArea(new Dimension(5, 5)));
        colorBox.add(getFeatureColorButton(), BorderLayout.EAST);
        colorBox.add(Box.createHorizontalGlue());

        Box columnFilterBox = Box.createHorizontalBox();
        myColumnsFilterBT = new JButton("Column Control");
        myColumnsFilterBT.addActionListener(createColumnFilterActionListener());
        columnFilterBox.add(myColumnsFilterBT);
        columnFilterBox.add(Box.createHorizontalGlue());

        createTimeFilterPanel();

        add(Box.createVerticalStrut(10));
        add(colorBox);
        add(Box.createVerticalStrut(10));
        add(columnFilterBox);
        if (source.getTimeColumn() != -1 || source.getDateColumn() != -1)
        {
            add(Box.createVerticalStrut(10));
            add(myStartAndEndTimeFilterPanel);
        }
        add(Box.createVerticalGlue());
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        if (e.getSource() == myUseTimeLoadRangeFilterCheckBox)
        {
            myTimeLoadRangeFilterPanel.setVisible(myUseTimeLoadRangeFilterCheckBox.isSelected());
        }
        else if (e.getSource() == myMinimumTimePicker)
        {
            if (myMinimumTimePicker.getCurrentPickerDate().getTime() > myMaximumTimePicker.getCurrentPickerDate().getTime())
            {
                myMaximumTimePicker.removeActionListener(this);
                myMaximumTimePicker.setCurrentPickerDate(myMinimumTimePicker.getCurrentPickerDate());
                myMaximumTimePicker.addActionListener(this);
            }
        }
        else if (e.getSource() == myMaximumTimePicker
                && myMaximumTimePicker.getCurrentPickerDate().getTime() < myMinimumTimePicker.getCurrentPickerDate().getTime())
        {
            myMinimumTimePicker.removeActionListener(this);
            myMinimumTimePicker.setCurrentPickerDate(myMaximumTimePicker.getCurrentPickerDate());
            myMinimumTimePicker.addActionListener(this);
        }

        myShpaeFileSource.setUsesTimeFilter(myUseTimeLoadRangeFilterCheckBox.isSelected());
        myShpaeFileSource
                .setMinDate(myUseTimeLoadRangeFilterCheckBox.isSelected() ? myMinimumTimePicker.getCurrentPickerDate() : null);
        myShpaeFileSource
                .setMaxDate(myUseTimeLoadRangeFilterCheckBox.isSelected() ? myMaximumTimePicker.getCurrentPickerDate() : null);

        confirmReloadWithChanges();
    }

    /**
     * Gets the feature color button.
     *
     * @return the feature color button
     */
    public JButton getFeatureColorButton()
    {
        if (myFeatureColorButton == null)
        {
            myFeatureColorButton = new JButton();
            myFeatureColorButton.setMaximumSize(new Dimension(20, 20));
            myFeatureColorButton.setIcon(new ColorCircleIcon(myShpaeFileSource.getShapeColor()));
            myFeatureColorButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    Color c = ColorPicker.showDialog(SwingUtilities.getWindowAncestor(ShapeFileDataGroupSettingsPanel.this),
                            "Select Color", ((ColorCircleIcon)myFeatureColorButton.getIcon()).getColor(), false);
                    if (c != null)
                    {
                        myFeatureColorButton.setIcon(new ColorCircleIcon(c));

                        // Update the configuration source
                        myShpaeFileSource.setShapeColor(c);
                        mySourceController.updateSource(myShpaeFileSource);

                        // Update the resident data type info.
                        for (DataTypeInfo dti : myDGI.getMembers(false))
                        {
                            dti.getBasicVisualizationInfo().setTypeColor(c, ShapeFileDataGroupSettingsPanel.this);
                        }
                    }
                }
            });
        }
        return myFeatureColorButton;
    }

    /**
     * Confirm reload with changes.
     */
    private void confirmReloadWithChanges()
    {
        boolean reload = false;
        DataTypeInfo dti = myDGI.getMembers(false).iterator().next();
        if (MantleToolboxUtils.getMantleToolbox(mySourceController.getToolbox()).getDataTypeController()
                .hasDataTypeInfoForTypeKey(dti.getTypeKey()))
        {
            int result = JOptionPane.showConfirmDialog(this,
                    "This change requires that the data be reloaded from the file, do you want to reload it now?\n"
                            + "If not the changes will occure the next time the source is activated.",
                    "Reload Data Confirmation", JOptionPane.YES_NO_OPTION);
            if (result == JOptionPane.YES_OPTION)
            {
                reload = true;
            }
        }
        mySourceController.updateSource(myShpaeFileSource);
        if (reload)
        {
            mySourceController.activateSource(myShpaeFileSource);
        }
    }

    /**
     * Creates the column filter action listener.
     *
     * @return the action listener
     */
    private ActionListener createColumnFilterActionListener()
    {
        return new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                ArrayList<String> columnNames = new ArrayList<>(myShpaeFileSource.getColumnNames());
                Set<String> filteredColumns = myShpaeFileSource.getColumnFilter() == null ? new HashSet<String>()
                        : new HashSet<String>(myShpaeFileSource.getColumnFilter());
                HashSet<String> disabledColumns = new HashSet<>();

                addToDisabledIfNotMinusOne(disabledColumns, columnNames, myShpaeFileSource.getLobColumn());
                addToDisabledIfNotMinusOne(disabledColumns, columnNames, myShpaeFileSource.getSmajColumn());
                addToDisabledIfNotMinusOne(disabledColumns, columnNames, myShpaeFileSource.getSminColumn());
                addToDisabledIfNotMinusOne(disabledColumns, columnNames, myShpaeFileSource.getTimeColumn());
                addToDisabledIfNotMinusOne(disabledColumns, columnNames, myShpaeFileSource.getDateColumn());

                ColumnChooserPanel panel = ColumnChooserPanel.showChooser(ShapeFileDataGroupSettingsPanel.this, columnNames,
                        filteredColumns, disabledColumns);
                if (panel != null && panel.changed())
                {
                    filteredColumns = panel.getFilteredNames();
                    myShpaeFileSource.setColumnFilter(filteredColumns);

                    confirmReloadWithChanges();
                }
            }
        };
    }

    /**
     * Creates the time filter panel.
     */
    private void createTimeFilterPanel()
    {
        DataTypeInfo dti = getDataTypeInfo();
        TimeExtents te = dti.getTimeExtents();
        TimeSpan extent = null;
        if (te != null)
        {
            extent = te.getExtent();
        }
        myUseTimeLoadRangeFilterCheckBox = new JCheckBox("Use Time Filter", false);
        myUseTimeLoadRangeFilterCheckBox.addActionListener(this);
        myUseTimeLoadRangeFilterCheckBox.setFocusPainted(false);
        myTimeLoadRangeFilterPanel = new JPanel(new GridLayout(2, 1));
        myTimeLoadRangeFilterPanel.setMaximumSize(new Dimension(1000, 30));
        myTimeLoadRangeFilterPanel.setBorder(BorderFactory.createTitledBorder("Time Filter"));
        myMinimumTimePicker = new DateTimePickerPanel(true);
        if (extent != null)
        {
            myMinimumTimePicker.setCurrentPickerDate(extent.getStartDate());
        }
        myMaximumTimePicker = new DateTimePickerPanel(true);
        if (extent != null)
        {
            myMaximumTimePicker.setCurrentPickerDate(extent.getEndDate());
        }
        myMinimumTimePicker.addActionListener(this);
        myMaximumTimePicker.addActionListener(this);

        JPanel minTimePanel = new JPanel(new BorderLayout());
        JLabel minLabel = new JLabel("Min Time: ");
        minTimePanel.setMinimumSize(new Dimension(80, 25));
        minTimePanel.add(minLabel, BorderLayout.WEST);
        minTimePanel.add(myMinimumTimePicker, BorderLayout.CENTER);

        JPanel maxTimePanel = new JPanel(new BorderLayout());
        JLabel maxLabel = new JLabel("Max Time:");
        maxLabel.setMinimumSize(new Dimension(80, 25));
        maxTimePanel.add(maxLabel, BorderLayout.WEST);
        maxTimePanel.add(myMaximumTimePicker, BorderLayout.CENTER);

        myTimeLoadRangeFilterPanel.add(minTimePanel);
        myTimeLoadRangeFilterPanel.add(maxTimePanel);

        myStartAndEndTimeFilterPanel = new JPanel(new BorderLayout());
        myStartAndEndTimeFilterPanel.add(myUseTimeLoadRangeFilterCheckBox, BorderLayout.NORTH);
        myStartAndEndTimeFilterPanel.add(myTimeLoadRangeFilterPanel, BorderLayout.CENTER);
        myStartAndEndTimeFilterPanel.setMaximumSize(new Dimension(1000, 60));
        myStartAndEndTimeFilterPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        myTimeLoadRangeFilterPanel.setVisible(false);
    }

    /**
     * Gets the data type info.
     *
     * @return the data type info
     */
    private DataTypeInfo getDataTypeInfo()
    {
        return myDGI == null ? null : myDGI.hasMembers(false) ? myDGI.getMembers(false).iterator().next() : null;
    }
}

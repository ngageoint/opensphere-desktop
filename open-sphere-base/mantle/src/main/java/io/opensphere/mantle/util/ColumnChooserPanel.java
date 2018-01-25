package io.opensphere.mantle.util;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
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
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

/**
 * The Class ColumnChooserPanel.
 */
@SuppressWarnings("serial")
public class ColumnChooserPanel extends JPanel implements ActionListener
{
    /** The my check boxes. */
    private final List<JCheckBox> myCheckBoxes;

    /** The my column names. */
    private final List<String> myColumnNames;

    /** The my filtered names. */
    private final Set<String> myFilteredNames;

    /** The my initial filtered names. */
    private final Set<String> myInitialFilteredNames;

    /** The my select all button. */
    private final JButton mySelectAllButton;

    /** The my select none button. */
    private final JButton mySelectNoneButton;

    /**
     * The main method.
     *
     * @param args the arguments
     */
    public static void main(String[] args)
    {
        ArrayList<String> testColumns = new ArrayList<>();
        testColumns.add("A");
        testColumns.add("B");
        testColumns.add("C");
        testColumns.add("D");
        testColumns.add("E");
        testColumns.add("F");
        testColumns.add("G");
        testColumns.add("H");
        testColumns.add("I");
        testColumns.add("J");
        testColumns.add("K");
        testColumns.add("L");
        testColumns.add("M");
        testColumns.add("N");
        testColumns.add("O");
        testColumns.add("P");

        HashSet<String> testFiltered = new HashSet<>();
        testFiltered.add("A");
        testFiltered.add("E");

        HashSet<String> disabled = new HashSet<>();
        disabled.add("B");

        ColumnChooserPanel.showChooser(null, testColumns, testFiltered, disabled);
    }

    /**
     * Simple method to show a column chooser.
     *
     * @param parent - the parent {@link Component}
     * @param columnNames - the complete ordered list of column names
     * @param filteredNames - the column names that are deselected
     * @param disabledNames - the column names that are disabled ( can't be
     *            changed )
     * @return the ColumnChooserPanel if OK was selected by user, null if Cancel
     *         was selected
     */
    public static ColumnChooserPanel showChooser(Component parent, List<String> columnNames, Set<String> filteredNames,
            Set<String> disabledNames)
    {
        ColumnChooserPanel ccp = new ColumnChooserPanel(columnNames, filteredNames, disabledNames);
        ccp.setMinimumSize(new Dimension(100, 200));
        ccp.setPreferredSize(new Dimension(200, 300));
        ccp.setMaximumSize(new Dimension(200, 300));
        int result = JOptionPane.showConfirmDialog(parent, ccp, "Choose Columns", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION)
        {
            return ccp;
        }
        return null;
    }

    /**
     * Creates a column chooser panel.
     *
     * @param columnNames - the master ordered list of all columns
     * @param filteredNames - the set of names that are unchecked
     * @param disabledNames - the set of names that are disabled
     */
    public ColumnChooserPanel(List<String> columnNames, Set<String> filteredNames, Set<String> disabledNames)
    {
        super(new BorderLayout());

        myColumnNames = new ArrayList<>(columnNames == null ? new HashSet<String>() : columnNames);
        Collections.sort(myColumnNames);
        myInitialFilteredNames = new HashSet<>(filteredNames == null ? new HashSet<String>() : filteredNames);
        myFilteredNames = new HashSet<>(filteredNames == null ? new HashSet<String>() : filteredNames);
        myCheckBoxes = new ArrayList<>();
        JPanel checkBoxPanel = new JPanel();
        checkBoxPanel.setLayout(new BoxLayout(checkBoxPanel, BoxLayout.Y_AXIS));
        checkBoxPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
        for (String column : myColumnNames)
        {
            boolean disabled = disabledNames.contains(column);
            StringBuilder label = new StringBuilder();
            label.append(column);
            if (disabled)
            {
                label.append(" ( REQUIRED )");
            }

            JCheckBox cb = new JCheckBox(label.toString(), !myFilteredNames.contains(column));
            cb.setFocusPainted(false);
            cb.setEnabled(!disabled);
            cb.addActionListener(this);
            cb.setActionCommand(column);
            myCheckBoxes.add(cb);
            checkBoxPanel.add(cb);
        }
        checkBoxPanel.add(Box.createVerticalGlue());

        JLabel lb = new JLabel("Select Columns");
        lb.setHorizontalAlignment(SwingConstants.CENTER);

        mySelectAllButton = new JButton("All");
        mySelectAllButton.setMargin(new Insets(2, 12, 2, 12));
        mySelectAllButton.addActionListener(this);
        mySelectAllButton.setFocusable(false);

        mySelectNoneButton = new JButton("None");
        mySelectNoneButton.setMargin(new Insets(2, 6, 2, 6));
        mySelectNoneButton.addActionListener(this);
        mySelectNoneButton.setFocusable(false);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(lb, BorderLayout.NORTH);
        JPanel btPanel = new JPanel();
        btPanel.setLayout(new BoxLayout(btPanel, BoxLayout.X_AXIS));
        btPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 5, 5));
        btPanel.add(Box.createHorizontalGlue());
        btPanel.add(mySelectAllButton);
        btPanel.add(Box.createHorizontalStrut(3));
        btPanel.add(mySelectNoneButton);
        topPanel.add(btPanel, BorderLayout.CENTER);

        add(topPanel, BorderLayout.NORTH);

        JScrollPane jsp = new JScrollPane(checkBoxPanel);
        add(jsp, BorderLayout.CENTER);
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        if (e.getSource() == mySelectAllButton || e.getSource() == mySelectNoneButton)
        {
            boolean select = e.getSource() == mySelectAllButton;
            for (JCheckBox cb : myCheckBoxes)
            {
                if (cb.isEnabled())
                {
                    cb.setSelected(select);

                    if (cb.isSelected())
                    {
                        myFilteredNames.remove(cb.getActionCommand());
                    }
                    else
                    {
                        myFilteredNames.add(cb.getActionCommand());
                    }
                }
            }
        }
        else if (e.getSource() instanceof JCheckBox)
        {
            JCheckBox cb = (JCheckBox)e.getSource();
            if (cb.isSelected())
            {
                myFilteredNames.remove(cb.getActionCommand());
            }
            else
            {
                myFilteredNames.add(cb.getActionCommand());
            }
        }
    }

    /**
     * Returns true if this set has changed from its initial state.
     *
     * @return true if changed, false if not
     */
    public boolean changed()
    {
        return !myInitialFilteredNames.equals(myFilteredNames);
    }

    /**
     * Gets the new set of filtered ( de-selected ) names after the user
     * selection process.
     *
     * @return the Set of filtered column names
     */
    public Set<String> getFilteredNames()
    {
        return new HashSet<>(myFilteredNames);
    }
}

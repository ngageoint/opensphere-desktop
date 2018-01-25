package io.opensphere.mantle.icon.impl.gui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.swing.ListComboBoxModel;
import io.opensphere.mantle.icon.IconRecord;

/**
 * A panel in which a user may select an existing collection or create a new one.
 */
public class IconCollectionNamePanel extends JPanel
{
    /**
     * serialVersionUID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The combo box from which an existing collection name is selected. This is disabled if the user is entering a new
     * collection name.
     */
    private final JComboBox<String> myExistingComboBox;

    /**
     * The radio button with which the user selects the mode of the name panel. This button is in a group with
     * {@link #myNewRB}, and the group is used to put the panel in a mode in which the user may select an existing group, or
     * create a new group.
     */
    private final JRadioButton myExistingRB;

    /**
     * The radio button with which the user selects the mode of the name panel. This button is in a group with
     * {@link #myExistingRB}, and the group is used to put the panel in a mode in which the user may select an existing group, or
     * create a new group.
     */
    private final JRadioButton myNewRB;

    /**
     * The text field into which the user may enter a new collection name. This field is disabled if the user is selecting an
     * existing collection.
     */
    private final JTextField myNewTF;

    /**
     * Instantiates a new collection name panel.
     *
     * @param pCollectionNameSet The set of collection names to display in the panel.
     */
    public IconCollectionNamePanel(Set<String> pCollectionNameSet)
    {
        super();
        setLayout(new GridLayout(3, 1, 0, 6));
        pCollectionNameSet.remove(IconRecord.DEFAULT_COLLECTION);
        pCollectionNameSet.remove(IconRecord.USER_ADDED_COLLECTION);
        pCollectionNameSet.remove(IconRecord.FAVORITES_COLLECTION);
        List<String> colNames = New.list(pCollectionNameSet);
        Collections.sort(colNames);
        colNames.add(0, IconRecord.FAVORITES_COLLECTION);
        colNames.add(0, IconRecord.USER_ADDED_COLLECTION);
        colNames.add(0, IconRecord.DEFAULT_COLLECTION);

        JLabel jl = new JLabel("Select collection name for icons:");
        add(jl);

        myExistingRB = new JRadioButton("Existing", true);
        myExistingRB.addActionListener(e -> updateSelectedCollection(true, false));
        myExistingComboBox = new JComboBox<>(new ListComboBoxModel<>(colNames));
        JPanel existingPnl = new JPanel(new BorderLayout());
        existingPnl.add(myExistingRB, BorderLayout.WEST);
        existingPnl.add(myExistingComboBox, BorderLayout.CENTER);
        add(existingPnl);

        myNewRB = new JRadioButton("New", false);
        myNewRB.addActionListener(e -> updateSelectedCollection(false, true));
        myNewTF = new JTextField();
        myNewTF.setEnabled(false);
        JPanel newPnl = new JPanel(new BorderLayout());
        newPnl.add(myNewRB, BorderLayout.WEST);
        newPnl.add(myNewTF, BorderLayout.CENTER);
        add(newPnl);

        ButtonGroup bg = new ButtonGroup();
        bg.add(myExistingRB);
        bg.add(myNewRB);
    }

    /**
     * Updates the enabled state of the "existing" combo box, and the "new" text field using the supplied parameters.
     *
     * @param pEnableComboBox true if the "existing" combo box should be enabled, false otherwise.
     * @param pEnableTextField true if the "new" text field should be enabled, false otherwise.
     */
    protected void updateSelectedCollection(boolean pEnableComboBox, boolean pEnableTextField)
    {
        myExistingComboBox.setEnabled(pEnableComboBox);
        myNewTF.setEnabled(pEnableTextField);
    }

    /**
     * Gets the collection name.
     *
     * @return the collection name
     */
    public String getCollectionName()
    {
        if (myNewRB.isSelected())
        {
            return myNewTF.getText();
        }
        return (String)myExistingComboBox.getSelectedItem();
    }
}

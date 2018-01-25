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

/**
 * A panel in which a user may select a subcategory, create a new subcategory, or select no subcategories.
 */
public class SubCategoryPanel extends JPanel
{
    /**
     * serialVersionUID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The combo box from which an existing category is selected. This is disabled if the user is entering a new category.
     */
    private final JComboBox<String> myExistingCatComboBox;

    /**
     * The radio button with which the user selects the mode of the category panel to use existing categories. This button is in a
     * group with {@link #myNewCatRB}, {@link #myNoneRB} and optionally {@link #mySubCatsFromDirNamesRB}, and the group is used to
     * put the panel in a mode in which the user may select an existing group, or create a new group.
     */
    private final JRadioButton myExistingCatRB;

    /**
     * The radio button with which the user selects the mode of the name panel to create a new category. This button is in a group
     * with {@link #myExistingCatRB}, {@link #myNoneRB} and optionally {@link #mySubCatsFromDirNamesRB}, and the group is used to
     * put the panel in a mode in which the user may select an existing group, or create a new group.
     */
    private final JRadioButton myNewCatRB;

    /**
     * The radio button with which the user selects the mode of the name panel to not use a category. This button is in a group
     * with {@link #myExistingCatRB}, {@link #myNewCatRB} and optionally {@link #mySubCatsFromDirNamesRB}, and the group is used
     * to put the panel in a mode in which the user may select an existing group, or create a new group.
     */
    private final JRadioButton myNoneRB;

    /**
     * The radio button with which the user selects the mode of the name panel to create subcategories from directory names. This
     * radio button may not be instantiated, and is available if the constructor is called with a value of <code>true</code> for
     * its second argument. This button is in a group with {@link #myExistingCatRB}, {@link #myNewCatRB}, and {@link #myNoneRB},
     * and the group is used to put the panel in a mode in which the user may select an existing group, or create a new group.
     */
    private JRadioButton mySubCatsFromDirNamesRB;

    /**
     * The text field into which the user may enter a new category name. This field is disabled if the user is selecting an
     * existing category.
     */
    private final JTextField myNewCatTF;

    /**
     * Instantiates a new sub-category selection panel.
     *
     * @param categorySet the set of categories with which to populate the selection panel.
     * @param subCatsFromDirNames true if the user should be allowed to create subcategories from directory names, false otherwise
     *            (if <code>true</code>, this causes the instantiation of {@link #mySubCatsFromDirNamesRB}).
     */
    public SubCategoryPanel(Set<String> categorySet, boolean subCatsFromDirNames)
    {
        super();

        int numRows = 3 + (categorySet.isEmpty() ? 0 : 1) + (subCatsFromDirNames ? 1 : 0);
        setLayout(new GridLayout(numRows, 1, 0, 6));
        List<String> names = New.list(categorySet);
        Collections.sort(names);

        add(new JLabel("Do you want to add a sub-category to your icon?:"));

        myNoneRB = new JRadioButton("No Sub-category", true);
        myNoneRB.addActionListener(e -> updateSelectedCategory(false, false));
        add(myNoneRB);

        myExistingCatRB = new JRadioButton("Existing", false);
        myExistingCatRB.addActionListener(e -> updateSelectedCategory(true, false));
        myExistingCatComboBox = new JComboBox<>(new ListComboBoxModel<>(names));
        JPanel existingPnl = new JPanel(new BorderLayout());
        existingPnl.add(myExistingCatRB, BorderLayout.WEST);
        existingPnl.add(myExistingCatComboBox, BorderLayout.CENTER);
        if (!categorySet.isEmpty())
        {
            add(existingPnl);
        }

        myNewCatRB = new JRadioButton("New", false);
        myNewCatRB.addActionListener(e -> updateSelectedCategory(false, true));
        myNewCatTF = new JTextField();
        myNewCatTF.setEnabled(false);

        JPanel newPnl = new JPanel(new BorderLayout());
        newPnl.add(myNewCatRB, BorderLayout.WEST);
        newPnl.add(myNewCatTF, BorderLayout.CENTER);
        add(newPnl);

        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(myNoneRB);
        buttonGroup.add(myExistingCatRB);
        buttonGroup.add(myNewCatRB);

        if (subCatsFromDirNames)
        {
            mySubCatsFromDirNamesRB = new JRadioButton("Create sub-categories from folder names.");
            mySubCatsFromDirNamesRB.setToolTipText("Search for all sub-folders of existing folders"
                    + " and add found icons with the folder name as the sub-category.");
            mySubCatsFromDirNamesRB.addActionListener(e -> updateSelectedCategory(false, false));
            buttonGroup.add(mySubCatsFromDirNamesRB);
            add(mySubCatsFromDirNamesRB);
        }
    }

    /**
     * Updates the enabled state of the "existing" combo box, and the "new" text field using the supplied parameters.
     *
     * @param pEnableComboBox true if the "existing" combo box should be enabled, false otherwise.
     * @param pEnableTextField true if the "new" text field should be enabled, false otherwise.
     */
    protected void updateSelectedCategory(boolean pEnableComboBox, boolean pEnableTextField)
    {
        myExistingCatComboBox.setEnabled(pEnableComboBox);
        myNewCatTF.setEnabled(pEnableTextField);
    }

    /**
     * Gets the category name.
     *
     * @return the category name
     */
    public String getCategory()
    {
        if (myNewCatRB.isSelected())
        {
            return myNewCatTF.getText();
        }
        else if (myExistingCatRB.isSelected())
        {
            return (String)myExistingCatComboBox.getSelectedItem();
        }
        else
        {
            return null;
        }
    }

    /**
     * Checks if is no category.
     *
     * @return true, if is no category
     */
    public boolean isNoCategory()
    {
        return myNoneRB.isSelected();
    }

    /**
     * Checks if is sub cats from dir names.
     *
     * @return true, if is sub cats from dir names
     */
    public boolean isSubCatsFromDirNames()
    {
        return mySubCatsFromDirNamesRB != null && mySubCatsFromDirNamesRB.isSelected();
    }
}

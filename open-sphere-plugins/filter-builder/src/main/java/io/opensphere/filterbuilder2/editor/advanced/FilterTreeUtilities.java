package io.opensphere.filterbuilder2.editor.advanced;

import java.awt.Color;
import java.awt.Component;
import java.util.Collection;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.commons.lang3.StringUtils;

import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.core.util.ValidationStatus;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.core.util.swing.input.DontShowDialog;
import io.opensphere.core.util.swing.input.model.TextModel;
import io.opensphere.filterbuilder2.editor.model.CriterionModel;
import io.opensphere.filterbuilder2.editor.model.GroupModel;

/** Utility methods for managing filter trees. */
public final class FilterTreeUtilities
{
    /**
     * Gets the user object from the given object.
     *
     * @param value the value
     * @return the user object, or null
     */
    public static Object getUserObject(Object value)
    {
        Object userObject = null;
        if (value instanceof DefaultMutableTreeNode)
        {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
            userObject = node.getUserObject();
        }
        return userObject;
    }

    /**
     * Removes the given items.
     *
     * @param items the items (criteria and/or groups)
     * @param prefsRegistry the preferences registry
     * @param parent the parent component
     */
    public static void removeItems(Collection<?> items, PreferencesRegistry prefsRegistry, Component parent)
    {
        int count = 0;
        for (Object item : items)
        {
            if (item instanceof CriterionModel)
            {
                count++;
            }
            else if (item instanceof GroupModel)
            {
                count += ((GroupModel)item).getTotalCount();
            }
        }

        boolean doDelete = true;
        if (count > 1)
        {
            int selection = DontShowDialog.showConfirmDialog(prefsRegistry, parent,
                    StringUtilities.concat("Remove ", String.valueOf(count), " items?"), "Confirm Multiple Removal", false);
            doDelete = selection == JOptionPane.OK_OPTION;
        }

        if (doDelete)
        {
            for (Object item : items)
            {
                if (item instanceof CriterionModel)
                {
                    CriterionModel criterion = (CriterionModel)item;
                    if (criterion.getParent() != null)
                    {
                        criterion.getParent().removeCriterion(criterion);
                    }
                }
                else if (item instanceof GroupModel)
                {
                    GroupModel group = (GroupModel)item;
                    if (group.getParent() != null)
                    {
                        group.getParent().removeGroup(group);
                    }
                }
            }
        }
    }

    /**
     * Sets the text and color of the label from the text model.
     *
     * @param label the label
     * @param textModel the text model
     */
    public static void setText(JLabel label, TextModel textModel)
    {
        label.setText(StringUtils.isEmpty(textModel.get()) ? "<blank>" : textModel.get());
        label.setForeground(textModel.getValidationStatus() == ValidationStatus.VALID ? null : Color.RED);
    }

    /** Disallow instantiation. */
    private FilterTreeUtilities()
    {
    }
}

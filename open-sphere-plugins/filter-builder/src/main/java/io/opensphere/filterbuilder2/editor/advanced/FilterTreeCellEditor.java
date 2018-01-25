package io.opensphere.filterbuilder2.editor.advanced;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;

import javax.swing.AbstractCellEditor;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellEditor;

import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.core.util.swing.tree.ModifiedTreeUI;

/**
 * Advanced filter tree cell editor.
 */
public class FilterTreeCellEditor extends AbstractCellEditor implements TreeCellEditor
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** The editor component. */
    private final FilterTreeCellPanel myPanel;

    /** The user object. */
    private Object myUserObject;

    /**
     * Constructor.
     *
     * @param prefs the system registry for user preferences
     */
    public FilterTreeCellEditor(PreferencesRegistry prefs)
    {
        myPanel = new FilterTreeCellPanel(prefs);
    }

    @Override
    public Object getCellEditorValue()
    {
        return myUserObject;
    }

    @Override
    public Component getTreeCellEditorComponent(JTree tree, Object value, boolean isSelected, boolean expanded, boolean leaf,
            int row)
    {
        myUserObject = myPanel.buildPanel(tree, value, expanded, row, true);
        // Set the preferred size
        if (value instanceof DefaultMutableTreeNode)
        {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
            Rectangle rect = ((ModifiedTreeUI)tree.getUI()).getNodeDimensions(value, row, node.getLevel(), expanded);
            myPanel.setPreferredSize(new Dimension(rect.width, rect.height));
        }
        return myPanel;
    }
}

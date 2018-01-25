package io.opensphere.filterbuilder2.editor.advanced;

import java.awt.Component;

import javax.swing.JTree;
import javax.swing.tree.TreeCellRenderer;

import io.opensphere.core.preferences.PreferencesRegistry;

/**
 * Advanced filter tree cell renderer.
 */
public class FilterTreeCellRenderer implements TreeCellRenderer
{
    /** The renderer component. */
    private final FilterTreeCellPanel myPanel;

    /**
     * Constructor.
     *
     * @param prefs the system registry for user preferences
     */
    public FilterTreeCellRenderer(PreferencesRegistry prefs)
    {
        myPanel = new FilterTreeCellPanel(prefs);
    }

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf,
            int row, boolean hasFocus)
    {
        myPanel.buildPanel(tree, value, expanded, row, false);
        return myPanel;
    }
}

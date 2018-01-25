package io.opensphere.core.util.swing.tree;

import javax.swing.AbstractButton;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;

/**
 * A tree cell renderer that renders a button.
 */
public interface ButtonTreeCellRenderer extends TreeCellRenderer
{
    /**
     * Get the renderer.
     *
     * @return the renderer
     */
    AbstractButton getRenderer();

    /**
     * Checks if is cell editable.
     *
     * @param path the path
     * @return true, if is cell editable
     */
    boolean isCellEditable(TreePath path);
}

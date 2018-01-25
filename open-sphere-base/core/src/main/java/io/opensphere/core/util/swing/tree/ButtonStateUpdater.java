package io.opensphere.core.util.swing.tree;

import io.opensphere.core.util.swing.QuadStateIconButton;

/**
 * The Interface ButtonStateUpdater.
 */
@FunctionalInterface
public interface ButtonStateUpdater
{
    /**
     * Update the button state based on the node.
     *
     * @param button the {@link QuadStateIconButton} to be updated.
     * @param node the {@link TreeTableTreeNode}
     */
    void update(QuadStateIconButton button, TreeTableTreeNode node);
}

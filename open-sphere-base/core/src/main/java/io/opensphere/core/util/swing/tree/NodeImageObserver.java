package io.opensphere.core.util.swing.tree;

import java.awt.EventQueue;
import java.awt.Image;
import java.awt.image.ImageObserver;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JTree;
import javax.swing.tree.TreePath;

import io.opensphere.core.util.swing.EventQueueUtilities;

/**
 * An ImageObserver for an animated images in a tree.
 */
public class NodeImageObserver implements ImageObserver
{
    /** The tree. */
    private final JTree myTree;

    /** The path set. */
    private final Set<TreePath> myPathSet;

    /**
     * Constructor.
     *
     * @param tree The tree
     */
    public NodeImageObserver(JTree tree)
    {
        myTree = tree;
        myPathSet = new HashSet<>();
    }

    /**
     * Adds a tree path to be repainted.
     *
     * @param path The tree path
     */
    public void addPath(TreePath path)
    {
        assert EventQueue.isDispatchThread();
        myPathSet.add(path);
    }

    @Override
    public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height)
    {
        if ((infoflags & (FRAMEBITS | ALLBITS)) != 0)
        {
            EventQueueUtilities.invokeLater(() -> myPathSet.stream().filter(p -> myTree.getPathBounds(p) != null)
                    .map(p -> myTree.getPathBounds(p)).forEach(r -> myTree.repaint(r)));
        }
        return (infoflags & (ALLBITS | ABORT)) == 0;
    }

    /**
     * Removes a tree path to be repainted.
     *
     * @param path The tree path
     */
    public void removePath(TreePath path)
    {
        assert EventQueue.isDispatchThread();
        myPathSet.remove(path);
    }
}

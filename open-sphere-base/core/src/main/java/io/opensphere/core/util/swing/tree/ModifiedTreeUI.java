package io.opensphere.core.util.swing.tree;

import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.JViewport;
import javax.swing.plaf.basic.BasicTreeUI;
import javax.swing.tree.AbstractLayoutCache;
import javax.swing.tree.TreePath;

/**
 * This UI class will calculate the bounds of the tree inside its scroll pane.
 * The bounds can be used to draw a highlight on rows as users mouse over the
 * tree that are the entire width of the tree.
 */
public class ModifiedTreeUI extends BasicTreeUI
{
    /** The Tree. */
    private final JTree myTree;

    /** The Scroll pane. */
    private JScrollPane myScrollPane;

    /**
     * Instantiates a new modified tree ui.
     *
     * @param tree the tree
     */
    public ModifiedTreeUI(JTree tree)
    {
        super();
        myTree = tree;
    }

    /**
     * Gets the node dimensions.
     *
     * @param value the <code>value</code> to be represented
     * @param row row being queried
     * @param depth the depth of the row
     * @param expanded true if row is expanded, false otherwise
     * @return a <code>Rectangle</code> containing the node dimensions, or
     *         <code>null</code> if node has no dimension
     */
    public Rectangle getNodeDimensions(Object value, int row, int depth, boolean expanded)
    {
        return nodeDimensions.getNodeDimensions(value, row, depth, expanded, null);
    }

    @Override
    public void installUI(JComponent c)
    {
        super.installUI(c);
        myTree.addPropertyChangeListener("ancestor", new PropertyChangeListener()
        {
            @Override
            public void propertyChange(PropertyChangeEvent evt)
            {
                parentDidChange();
            }
        });
    }

    @Override
    public boolean startEditing(TreePath path, MouseEvent event)
    {
        return super.startEditing(path, event);
    }

    @Override
    protected AbstractLayoutCache.NodeDimensions createNodeDimensions()
    {
        return new NodeDimensionsHandler()
        {
            @Override
            public Rectangle getNodeDimensions(Object value, int row, int depth, boolean expanded, Rectangle size)
            {
                Rectangle dimensions = super.getNodeDimensions(value, row, depth, expanded, size);
                if (myScrollPane != null)
                {
                    Insets insets = myTree.getInsets();
                    dimensions.width = myScrollPane.getViewport().getWidth() - getRowX(row, depth) - insets.left - insets.right;
                    dimensions.height = Math.max(dimensions.height, 20);
                }
                return dimensions;
            }
        };
    }

    @Override
    protected void paintHorizontalLine(Graphics g, JComponent c, int y, int left, int right)
    {
    }

    @Override
    protected void paintVerticalPartOfLeg(Graphics g, Rectangle clipBounds, Insets insets, TreePath path)
    {
    }

    /**
     * Parent did change.
     */
    private void parentDidChange()
    {
        if (myTree.getParent() instanceof JViewport && myTree.getParent().getParent() instanceof JScrollPane)
        {
            myScrollPane = (JScrollPane)myTree.getParent().getParent();
            myScrollPane.addComponentListener(new ComponentAdapter()
            {
                @Override
                public void componentResized(ComponentEvent e)
                {
                    configureLayoutCache();
                }
            });
        }
    }
}

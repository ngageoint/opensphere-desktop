package io.opensphere.core.util.swing.tree;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

import javax.swing.JTree;
import javax.swing.border.LineBorder;
import javax.swing.tree.TreeCellRenderer;

/**
 * A decorator for TreeTableTreeCellRenderer to support drag-n-drop.
 */
public class DragNDropTreeCellRenderer implements TreeCellRenderer
{
    /** The base renderer. */
    private final TreeTableTreeCellRenderer myRenderer;

    /** The custom line border. */
    private final TopBottomLineBorder myLineBorder = new TopBottomLineBorder(Color.WHITE);

    /**
     * The constructor.
     *
     * @param renderer the base renderer
     */
    public DragNDropTreeCellRenderer(TreeTableTreeCellRenderer renderer)
    {
        super();
        myRenderer = renderer;
    }

    // This method is just adding things to the actual renderer (myRenderer),
    // which is why it returns null.
    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf,
            int row, boolean hasFocus)
    {
        // Draw the top border of the panel during drag and drop
        JTree.DropLocation dropLocation = tree.getDropLocation();
        boolean isDropCell = dropLocation != null && dropLocation.getChildIndex() == -1
                && tree.getRowForPath(dropLocation.getPath()) == row;
        if (isDropCell)
        {
            boolean canImport = false;
            if (tree.getTransferHandler() instanceof DirectionalTransferHandler)
            {
                DirectionalTransferHandler transferHandler = (DirectionalTransferHandler)tree.getTransferHandler();
                canImport = transferHandler.couldImport();
                myLineBorder.setIsTop(transferHandler.isUp());
            }
            myRenderer.getPanel().setBorder(canImport ? myLineBorder : null);
        }
        else
        {
            myRenderer.getPanel().setBorder(null);
        }
        return null;
    }

    /**
     * Line border that paints only the top line.
     */
    private static class TopBottomLineBorder extends LineBorder
    {
        /** The serialVersionUID. */
        private static final long serialVersionUID = 1L;

        /** True to paint the top border, false to paint the bottom border. */
        private boolean myIsTop;

        /**
         * Creates a line border with the specified color and a thickness = 1.
         *
         * @param color the color for the border
         */
        public TopBottomLineBorder(Color color)
        {
            super(color);
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height)
        {
            Color oldColor = g.getColor();
            g.setColor(lineColor);
            for (int i = 0; i < thickness; i++)
            {
                drawLine(g, x + i, y + i, width - i - i - 1, height - i - i - 1);
            }
            g.setColor(oldColor);
        }

        /**
         * Setter for is top.
         *
         * @param isTop True to paint the top border, false to paint the bottom
         *            border
         */
        public void setIsTop(boolean isTop)
        {
            myIsTop = isTop;
        }

        /**
         * Draws a line either on the top or bottom.
         *
         * @param g the paint graphics
         * @param x the <i>x</i> coordinate of the rectangle to be drawn.
         * @param y the <i>y</i> coordinate of the rectangle to be drawn.
         * @param width the width of the rectangle to be drawn.
         * @param height the height of the rectangle to be drawn.
         */
        private void drawLine(Graphics g, int x, int y, int width, int height)
        {
            if (width < 0 || height < 0)
            {
                return;
            }

            if (myIsTop)
            {
                g.drawLine(x, y, x + width - 1, y);
            }
            else
            {
                g.drawLine(x + width, y + height, x + 1, y + height);
            }
        }
    }
}

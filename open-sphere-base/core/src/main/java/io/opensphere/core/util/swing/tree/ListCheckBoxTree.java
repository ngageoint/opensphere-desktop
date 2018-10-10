package io.opensphere.core.util.swing.tree;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;

import javax.swing.AbstractButton;
import javax.swing.DropMode;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.ToolTipManager;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import com.jidesoft.swing.CheckBoxTree;
import com.jidesoft.swing.CheckBoxTreeCellRenderer;

import io.opensphere.core.util.Colors;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.swing.ComponentUtilities;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.core.util.swing.JTreeUtilities;
import io.opensphere.core.util.swing.QuadStateIconButton;
import io.opensphere.core.util.swing.QuadStateIconButton.ButtonState;

/**
 * A check box tree that implements custom selection, hovering, and row
 * painting, in a way that makes it appear like a list.
 */
@SuppressWarnings("PMD.GodClass")
public class ListCheckBoxTree extends CheckBoxTree
{
    /** The hover color. */
    private static final Color ourHoverColor = new Color(.6f, .6f, .6f, .5f);

    /** The selection color. */
    private static final Color ourSelectionColor = Colors.LF_PRIMARY2;

    /** The serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** Whether to ignore selection events to prevent listener loops. */
    private boolean myIgnoreSelectionEvents;

    /** The last mouse moved event. */
    private MouseEvent myLastMouseEvent;

    /**
     * Mouse click count.
     */
    private int myMouseClickCount;

    /** The mouse over component. */
    private Component myMouseOverComponent;

    /** The mouse over path. */
    private TreePath myMouseOverPath;

    /** The mouse over rows. */
    private int[] myMouseOverRows = new int[0];

    /**
     * If the row that the mouse is over should be painted a different color.
     */
    private boolean myPaintHoverRow = true;

    /**
     * Custom selection listener which toggles selection and ignores selection
     * events when the mouse is over the checkbox.
     */
    private final transient CustomTreeSelectionModel mySelectionModel = new CustomTreeSelectionModel();

    /**
     * The minimum number of selections before a warning pop up is displayed
     * asking the user if they are sure they want to select this many.
     */
    private Integer mySelectionWarnThreshold;

    /** The hover listener. */
    private transient BiConsumer<TreePath, Boolean> myHoverListener;

    /** The invisible paths. */
    private final Set<TreePath> myInvisiblePaths = New.set();

    /**
     * Maps a collection of tree nodes to a collection of tree paths.
     *
     * @param nodes The tree nodes
     * @return The tree paths
     */
    public static List<TreePath> mapTreePathsFromNodes(Collection<TreeNode> nodes)
    {
        List<TreePath> paths = New.list(nodes.size());
        for (TreeNode node : nodes)
        {
            if (node instanceof TreeTableTreeNode)
            {
                paths.add(new TreePath(((TreeTableTreeNode)node).getPath()));
            }
        }
        return paths;
    }

    /**
     * Fire checkbox action.
     *
     * @param node the node
     */
    private static void fireCheckboxAction(TreeTableTreeNode node)
    {
        ActionListener[] listeners = node.getPayload().getButton().getActionListeners();
        if (listeners != null && listeners.length > 0)
        {
            ActionEvent event = new ActionEvent(node.getPayload().getButton(), 1001,
                    node.getPayload().getButton().getActionCommand());
            for (ActionListener l : listeners)
            {
                l.actionPerformed(event);
            }
        }
    }

    /**
     * In bounds.
     *
     * @param rect the rect
     * @param bounds the bounds
     * @param mouseX the mouse x
     * @param mouseY the mouse y
     * @return true, if successful
     */
    private static boolean inBounds(Rectangle rect, Rectangle bounds, int mouseX, int mouseY)
    {
        int minX = rect.x + bounds.x;
        int maxX = minX + bounds.width;

        int minY = rect.y + bounds.y;
        int maxY = minY + bounds.height;

        return mouseX >= minX && mouseX <= maxX && mouseY >= minY && mouseY <= maxY;
    }

    /**
     * Maps a collection of tree paths to a collection of tree nodes.
     *
     * @param paths The tree paths
     * @return The tree nodes
     */
    private static List<TreeTableTreeNode> mapTreeNodesFromPaths(TreePath[] paths)
    {
        List<TreeTableTreeNode> nodes = New.list(paths.length);
        for (TreePath path : paths)
        {
            if (path.getLastPathComponent() instanceof TreeTableTreeNode)
            {
                nodes.add((TreeTableTreeNode)path.getLastPathComponent());
            }
        }
        return nodes;
    }

    /**
     * Constructor.
     */
    public ListCheckBoxTree()
    {
        ToolTipManager.sharedInstance().registerComponent(this);
        mySelectionModel.setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
        setSelectionModel(mySelectionModel);
        setLargeModel(true);

        // Add listeners
        addMouseMotionListener(getMouseMotionListener());
        addMouseListener(getMouseListener());
        getCheckBoxTreeSelectionModel().addTreeSelectionListener(getCheckBoxSelectionListener());
    }

    /**
     * Constructor.
     *
     * @param transferHandler the transfer handler
     */
    public ListCheckBoxTree(final DirectionalTransferHandler transferHandler)
    {
        this();

        // Add drag-n-drop functionality
        if (transferHandler != null && transferHandler.getController() != null)
        {
            setDragEnabled(true);
            setDropMode(DropMode.ON);
            setTransferHandler(transferHandler);

            // When selection changes, inform the drag and drop controller
            getSelectionModel().addTreeSelectionListener(e ->
            {
                transferHandler.getController().selectionChanged(e.getPath(), e.isAddedPath());

                // Because we are already on the EDT, scroll to the visible
                // path after the panel has had a chance to update.
                EventQueueUtilities.invokeLater(() -> ListCheckBoxTree.this.scrollPathToVisible(e.getPath()));
            });
        }
    }

    /**
     * Sets the selection to path. If this represents a change, then the
     * TreeSelectionListeners are notified. If {@code path} is null, selection
     * will be cleared.
     *
     * @param path new path to select
     */
    public void forceSelection(TreePath path)
    {
        mySelectionModel.forceSelection(path);
    }

    /**
     * Get the mouseOverPath.
     *
     * @return the mouseOverPath
     */
    public TreePath getMouseOverPath()
    {
        return myMouseOverPath;
    }

    /**
     * Get the mouseOverRow.
     *
     * @return the mouseOverRow
     */
    public int[] getMouseOverRows()
    {
        return myMouseOverRows.clone();
    }

    /**
     * Gets the minimum number of selections before a warning pop up is
     * displayed asking the user if they are sure they want to select this many.
     *
     * @return The warn threshold or null if there isn't one.
     */
    public Integer getSelectionWarnThreshold()
    {
        return mySelectionWarnThreshold;
    }

    /**
     * Sets the hover listener.
     *
     * @param hoverListener The hover listener. The second argument is whether
     *            focus was gained.
     */
    public void setHoverListener(BiConsumer<TreePath, Boolean> hoverListener)
    {
        myHoverListener = hoverListener;
    }

    /**
     * Set the hover paths.
     *
     * @param paths The hover paths.
     */
    public void setHoverPaths(Collection<? extends TreePath> paths)
    {
        myMouseOverRows = new int[paths.size()];
        int index = 0;
        for (TreePath path : paths)
        {
            myMouseOverRows[index++] = getRowForPath(path);
        }
        repaint();
    }

    /**
     * Sets whether to ignore selection events.
     *
     * @param ignoreSelectionEvents whether to ignore selection events
     */
    public void setIgnoreSelectionEvents(boolean ignoreSelectionEvents)
    {
        myIgnoreSelectionEvents = ignoreSelectionEvents;
    }

    /**
     * Set if the hover row should be painted a different color.
     *
     * @param paint If the hover row should be painted a different color.
     */
    public void setPaintHoverRow(boolean paint)
    {
        myPaintHoverRow = paint;
    }

    /**
     * Sets the minimum number of selections before a warning pop up is
     * displayed asking the user if they are sure they want to select this many.
     *
     * @param threshold The warn threshold or null if there shouldn't be a
     *            warning ever.
     */
    public void setSelectionWarnThreshold(Integer threshold)
    {
        mySelectionWarnThreshold = threshold;
    }

    /**
     * Takes the selection state from the application model and applies it to
     * the tree check box model.
     */
    public void updateCheckboxState()
    {
        if (getModel().getRoot() instanceof TreeTableTreeNode)
        {
            List<TreePath> paths;

            // Get the list of selected paths
            paths = mapTreePathsFromNodes(JTreeUtilities.flatten((TreeNode)getModel().getRoot(), node -> node.isLeaf()
                    && node instanceof TreeTableTreeNode && ((TreeTableTreeNode)node).getPayload().getButton().isSelected()));

            // Check the boxes
            if (!paths.isEmpty())
            {
                myIgnoreSelectionEvents = true;
                getCheckBoxTreeSelectionModel().addSelectionPaths(paths.toArray(new TreePath[paths.size()]));
                myIgnoreSelectionEvents = false;
            }

            // Get the list of de-selected paths
            paths = mapTreePathsFromNodes(JTreeUtilities.flatten((TreeNode)getModel().getRoot(), node -> node.isLeaf()
                    && node instanceof TreeTableTreeNode && !((TreeTableTreeNode)node).getPayload().getButton().isSelected()));

            // Un-check the boxes
            if (!paths.isEmpty())
            {
                myIgnoreSelectionEvents = true;
                for (TreePath path : paths)
                {
                    getCheckBoxTreeSelectionModel().removeSelectionPath(path);
                }
                myIgnoreSelectionEvents = false;
            }
        }
    }

    /**
     * Sets the visibility state of the path.
     *
     * @param path the path
     * @param visible whether to make it visible
     */
    public void setCheckBoxVisible(TreePath path, boolean visible)
    {
        if (visible)
        {
            myInvisiblePaths.remove(path);
        }
        else
        {
            myInvisiblePaths.add(path);
        }
    }

    @Override
    public boolean isCheckBoxVisible(TreePath path)
    {
        return myInvisiblePaths == null || !myInvisiblePaths.contains(path);
    }

    @Override
    public void updateUI()
    {
        setUI(new ModifiedTreeUI(this));
    }

    @Override
    protected Handler createHandler()
    {
        return new CustomHandler(this);
    }

    @Override
    protected void paintComponent(Graphics g)
    {
        // Paint hover row
        if (myPaintHoverRow)
        {
            for (int row : myMouseOverRows)
            {
                paintRowBackground(row, ourHoverColor, g);
            }
        }

        // Paint selected rows
        int[] selectedRows = getSelectionRows();
        if (selectedRows != null && selectedRows.length > 0)
        {
            for (int selectedRow : selectedRows)
            {
                boolean alreadyPainted = false;
                if (myPaintHoverRow)
                {
                    for (int row : myMouseOverRows)
                    {
                        if (row == selectedRow)
                        {
                            alreadyPainted = true;
                        }
                    }
                }
                if (!alreadyPainted)
                {
                    paintRowBackground(selectedRow, ourSelectionColor, g);
                }
            }
        }

        super.paintComponent(g);
    }

    /**
     * Gets the checkbox tree selection listener. This listens for click events
     * and updates the application model and listeners.
     *
     * @return the checkbox tree selection listener
     */
    private TreeSelectionListener getCheckBoxSelectionListener()
    {
        return e ->
        {
            if (!myIgnoreSelectionEvents)
            {
                boolean isSelected = e.isAddedPath();

                // Get the list of nodes to update
                List<TreeNode> leafNodes = JTreeUtilities.flatten(mapTreeNodesFromPaths(e.getPaths()), node1 -> node1.isLeaf());

                // Check with the user before large selections
                boolean doIt = true;
                if (isSelected && mySelectionWarnThreshold != null && leafNodes.size() >= mySelectionWarnThreshold.intValue())
                {
                    int option = JOptionPane.showConfirmDialog(ListCheckBoxTree.this.getParent(),
                            "Are you sure you want to select " + leafNodes.size() + " items?", "Confirm Selection",
                            JOptionPane.YES_NO_OPTION);
                    doIt = option == JOptionPane.YES_OPTION;
                }

                // Update the selection state
                if (doIt)
                {
                    int index = 0;
                    for (TreeNode node2 : leafNodes)
                    {
                        if (node2 instanceof TreeTableTreeNode)
                        {
                            TreeTableTreeNode tableNode = (TreeTableTreeNode)node2;
                            if (tableNode.getPayload().getButton().isSelected() != isSelected)
                            {
                                tableNode.getPayload().getButton().setSelected(isSelected);
                                fireCheckboxAction(tableNode);

                                if (tableNode.getPayload().getButton().isSelected() != isSelected)
                                {
                                    getCheckBoxTreeSelectionModel().removeSelectionPath(e.getPaths()[index]);
                                }
                            }
                        }

                        index++;
                    }
                }
                else
                {
                    getCheckBoxTreeSelectionModel().removeSelectionPaths(e.getPaths());
                }
            }
        };
    }

    /**
     * This listener will set myMouseOverPath to null which prevents the row
     * background highlight color from being drawn when the mouse is not over
     * the tree.
     *
     * @return the mouse listener
     */
    private MouseListener getMouseListener()
    {
        return new MouseAdapter()
        {
            @Override
            public void mouseClicked(final MouseEvent e)
            {
                myMouseClickCount = e.getClickCount();
                if (e.getButton() == MouseEvent.BUTTON1 && myMouseOverComponent instanceof JButton)
                {
                    JButton button = (JButton)myMouseOverComponent;
                    button.doClick();

                    // Fix the mouse over component in case the buttons
                    // changed.
                    for (MouseMotionListener mouseListener : getMouseMotionListeners())
                    {
                        mouseListener.mouseMoved(myLastMouseEvent);
                    }
                    e.consume();
                }
            }

            @Override
            public void mouseExited(MouseEvent e)
            {
                if (myHoverListener != null && myMouseOverPath != null)
                {
                    myHoverListener.accept(myMouseOverPath, Boolean.FALSE);
                }
                myMouseOverPath = null;
                myMouseOverComponent = null;
                myMouseOverRows = new int[0];
                myMouseClickCount = 0;
                repaint();
            }

            @Override
            public void mousePressed(MouseEvent e)
            {
                if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 1
                        && myMouseOverComponent instanceof QuadStateIconButton)
                {
                    QuadStateIconButton button = (QuadStateIconButton)myMouseOverComponent;
                    button.setState(ButtonState.PRESSED);
                    repaint();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e)
            {
                if (e.getButton() == MouseEvent.BUTTON1 && myMouseOverComponent instanceof QuadStateIconButton)
                {
                    QuadStateIconButton button = (QuadStateIconButton)myMouseOverComponent;
                    button.setState(ButtonState.ROLLOVER);
                    repaint();
                }
            }
        };
    }

    /**
     * Gets the mouse motion listener.
     *
     * @return the mouse motion listener
     */
    private MouseMotionListener getMouseMotionListener()
    {
        return new MouseMotionAdapter()
        {
            @Override
            public void mouseMoved(MouseEvent e)
            {
                myLastMouseEvent = e;

                int mouseOverRow = getClosestRowForLocation(e.getX(), e.getY());
                TreePath mouseOverPath = getPathForRow(mouseOverRow);
                if (myHoverListener != null && (myMouseOverRows.length != 1 || myMouseOverRows[0] != mouseOverRow))
                {
                    if (myMouseOverPath != null)
                    {
                        myHoverListener.accept(myMouseOverPath, Boolean.FALSE);
                    }
                    if (mouseOverPath != null)
                    {
                        myHoverListener.accept(mouseOverPath, Boolean.TRUE);
                    }
                }
                myMouseOverPath = mouseOverPath;

                myMouseOverComponent = null;
                if (mouseOverRow != -1)
                {
                    myMouseOverRows = new int[] { mouseOverRow };
                    TreeTableTreeNode mouseOverNode = (TreeTableTreeNode)myMouseOverPath.getLastPathComponent();

                    JPanel panel = (JPanel)getCellRenderer().getTreeCellRendererComponent(ListCheckBoxTree.this, mouseOverNode,
                            true, false, mouseOverNode.isLeaf(), mouseOverRow, false);

                    // This row bounds is the tree node's renderable area in the
                    // coordinates of the tree.
                    Rectangle nodeRect = getRowBounds(mouseOverRow);

                    for (Component comp : ComponentUtilities.getAllComponents(panel, comp -> comp instanceof AbstractButton))
                    {
                        // The check box bounds are in the coordinates of the
                        // tree node's renderable area.
                        boolean inBounds = inBounds(nodeRect, comp.getBounds(), e.getX(), e.getY());
                        if (inBounds)
                        {
                            myMouseOverComponent = comp;
                        }

                        if (comp instanceof QuadStateIconButton)
                        {
                            QuadStateIconButton qButton = (QuadStateIconButton)comp;
                            /* Set the tool tips for the rollover icons in the
                             * tree node panel. When rolled off, set the tool
                             * tip to NULL. */
                            if (inBounds && panel instanceof CheckBoxTreeCellRenderer)
                            {
                                CheckBoxTreeCellRenderer cbRenderer = (CheckBoxTreeCellRenderer)panel;
                                if (cbRenderer.getActualTreeRenderer() instanceof TreeTableTreeCellRenderer)
                                {
                                    TreeTableTreeCellRenderer.setToolTipStoredText(qButton.getName());
                                }
                            }
                            qButton.setState(inBounds ? ButtonState.ROLLOVER : ButtonState.DEFAULT);
                        }
                        else
                        {
                            if (panel instanceof CheckBoxTreeCellRenderer)
                            {
                                CheckBoxTreeCellRenderer cbRenderer = (CheckBoxTreeCellRenderer)panel;
                                if (cbRenderer.getActualTreeRenderer() instanceof TreeTableTreeCellRenderer)
                                {
                                    TreeTableTreeCellRenderer.setToolTipStoredText(null);
                                }
                            }
                        }
                    }
                }
                else
                {
                    myMouseOverRows = new int[0];
                }
                repaint();
            }
        };
    }

    /**
     * Paint row background.
     *
     * @param row the row
     * @param backgroundColor the background color
     * @param g the g
     */
    private void paintRowBackground(int row, Color backgroundColor, Graphics g)
    {
        Rectangle bounds = getRowBounds(row);
        if (bounds != null)
        {
            g.setColor(backgroundColor);
            g.fillRect(0, bounds.y, getWidth(), bounds.height);
        }
    }

    /**
     * Custom handler.
     */
    protected static class CustomHandler extends Handler
    {
        /**
         * The constructor.
         *
         * @param tree the CheckBoxTree
         */
        public CustomHandler(CheckBoxTree tree)
        {
            super(tree);
        }

        @Override
        public void mousePressed(MouseEvent e)
        {
            // If over a hover button, do not invoke the super implementation,
            // to avoid toggling the checkbox for the row.
            if (!(((ListCheckBoxTree)_tree).myMouseOverComponent instanceof QuadStateIconButton))
            {
                super.mousePressed(e);
            }
        }

        @Override
        protected boolean clicksInCheckBox(MouseEvent e, TreePath path)
        {
            if (((ListCheckBoxTree)_tree).myMouseOverComponent instanceof QuadStateIconButton)
            {
                return true;
            }
            if (!_tree.isCheckBoxVisible(path))
            {
                return false;
            }
            Rectangle bounds = _tree.getPathBounds(path);
            // Use the actual checkbox width instead of Jide's
            // implementation, which doesn't support wider checkboxes.
            int hotspot = _tree.getCheckBox().getPreferredSize().width;
            if (_tree.getComponentOrientation().isLeftToRight())
            {
                return e.getX() < bounds.x + hotspot;
            }
            return e.getX() > bounds.x + bounds.width - hotspot;
        }
    }

    /**
     * Custom selection model which ignores regular selection in order to allow
     * full control of the row selections. In order for selection to occur,
     * {@code toggleSelection} or {@code forceSelection} should be called.
     */
    private class CustomTreeSelectionModel extends DefaultTreeSelectionModel
    {
        /** serialVersionUID. */
        private static final long serialVersionUID = 1L;

        //        /**
        //         * Timer used to prevent toggling selection when the tree is
        //         * double-clicked.
        //         */
        //        private Timer myTimer;
        //
        //        @Override
        //        public void setSelectionPaths(TreePath[] pPaths)
        //        {
        //            List<TreePath> paths = New.list(pPaths);
        //            for (int index = 0; index < paths.size(); ++index)
        //            {
        //                TreePath treePath = paths.get(index);
        //                List<TreeTableTreeNode> children = ((TreeTableTreeNode)treePath.getLastPathComponent()).getChildren();
        //                if (children != null)
        //                {
        //                    for (TreeTableTreeNode treeTableTreeNode : children)
        //                    {
        //                        paths.add(new TreePath(treeTableTreeNode));
        //                    }
        //                }
        //            }
        //            super.setSelectionPaths(New.array(paths, TreePath.class));
        //        }

        /**
         * Sets the selection path.
         *
         * @param path the new selection path
         */
        @Override
        public void setSelectionPath(final TreePath path)
        {
            // Don't select the row when over a button
            if (myMouseOverComponent == null && myMouseClickCount != 2)
            {
                toggleSelection(path);
            }

            //            System.err.println(myMouseClickCount);
            //            if (((TreeTableTreeNode)path.getLastPathComponent()).getChildCount() > 0)
            //            {
            //                EventQueueUtilities.invokeLater(new Runnable()
            //                {
            //                    @Override
            //                    public void run()
            //                    {
            //                        setSelectionPaths(new TreePath[] { path });
            //                    }
            //                });
            //                return;
            //            }
            //            // Don't select the row when over a button
            //            if (myMouseOverComponent == null && myMouseClickCount != 2)
            //            {
            //                toggleSelection(path);
            //                System.err.println("start timer");
            //                myTimer = new Timer(1000, new ActionListener()
            //                {
            //                    @Override
            //                    public void actionPerformed(ActionEvent e)
            //                    {
            ////                        toggleSelection(path);
            //                        myMouseClickCount = 0;
            //                    }
            //                });
            //                myTimer.setRepeats(false);
            //                myTimer.start();
            //            }
            //            if (myMouseClickCount > 0 && myTimer != null)
            //            {
            //                System.err.println("stop timer");
            //                myTimer.stop();
            //                myTimer = null;
            //            }
        }

        /**
         * Sets the selection to path. If this represents a change, then the
         * TreeSelectionListeners are notified. If {@code path} is null,
         * selection will be cleared.
         *
         * @param path new path to select
         */
        private void forceSelection(TreePath path)
        {
            if (path == null)
            {
                clearSelection();
            }
            else if (!path.equals(getSelectionPath()))
            {
                super.addSelectionPath(path);
            }
        }

        /**
         * If the given path is not selected, selection is changed to the path,
         * if it is selected the selection is removed. If this represents a
         * change, then the TreeSelectionListeners are notified. If {@code path}
         * is null, selection will be cleared.
         *
         * @param path new path to select
         */
        private void toggleSelection(TreePath path)
        {
            if (path == null)
            {
                clearSelection();
            }
            else if (getSelectionPaths().length == 1 && path.equals(getSelectionPath()))
            {
                removeSelectionPath(path);
            }
            else
            {
                super.setSelectionPath(path);
            }
        }
    }
}

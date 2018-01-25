package io.opensphere.filterbuilder2.editor.advanced;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import javax.swing.DropMode;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JTree;
import javax.swing.plaf.basic.BasicTreeUI;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import io.opensphere.core.datafilter.DataFilterOperators.Logical;
import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.collections.StreamUtilities;
import io.opensphere.core.util.swing.JTreeUtilities;
import io.opensphere.core.util.swing.input.model.PropertyChangeEvent;
import io.opensphere.core.util.swing.input.model.PropertyChangeListener;
import io.opensphere.core.util.swing.tree.ModifiedTreeUI;
import io.opensphere.filterbuilder2.common.Constants;
import io.opensphere.filterbuilder2.editor.model.CriterionModel;
import io.opensphere.filterbuilder2.editor.model.FilterModel;
import io.opensphere.filterbuilder2.editor.model.GroupModel;

/**
 * Advanced filter tree.
 */
public class FilterTree extends JTree
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** The UI model. */
    private final FilterModel myModel;

    /** The mouse over row. */
    private int myMouseOverRow = -1;

    /** The system registry for user preferences. */
    private final PreferencesRegistry myPreferencesRegistry;

    /**
     * Creates a tree node from a criterion model.
     *
     * @param criterion the criterion model
     * @return the tree node
     */
    private static DefaultMutableTreeNode createTreeNode(CriterionModel criterion)
    {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(criterion);
        return node;
    }

    /**
     * Creates a tree node from a group model.
     *
     * @param group the group model
     * @return the tree node
     */
    private static DefaultMutableTreeNode createTreeNode(GroupModel group)
    {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(group);
        for (GroupModel aGroup : group.getGroups())
        {
            node.add(createTreeNode(aGroup));
        }
        for (CriterionModel criterion : group.getCriteria())
        {
            node.add(createTreeNode(criterion));
        }
        return node;
    }

    /**
     * Constructor.
     *
     * @param prefs the system registry for user preferences
     * @param model the model
     */
    public FilterTree(PreferencesRegistry prefs, FilterModel model)
    {
        super(createTreeNode(model.getGroup()));
        JTreeUtilities.expandOrCollapseAll(this, true);

        myPreferencesRegistry = prefs;
        myModel = model;

        setOpaque(false);
        setBackground(Constants.TRANSPARENT_COLOR);
        setShowsRootHandles(true);
        setEditable(true);
        setCellEditor(new FilterTreeCellEditor(myPreferencesRegistry));
        setCellRenderer(new FilterTreeCellRenderer(myPreferencesRegistry));
        getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);

        BasicTreeUI treeUI = (BasicTreeUI)getUI();
        ClassLoader classLoader = AdvancedEditorPanel.class.getClassLoader();
        treeUI.setCollapsedIcon(new ImageIcon(classLoader.getResource("images/right.png")));
        treeUI.setExpandedIcon(new ImageIcon(classLoader.getResource("images/down.png")));

        setDragEnabled(true);
        setDropMode(DropMode.ON);
        setTransferHandler(new FilterTreeTransferHandler());

        addListeners();
    }

    /**
     * Adds a new criterion.
     */
    public void addNewCriterion()
    {
        GroupModel group = getGroupForAdd();
        if (group != null && canAddToGroup(group, Constants.EXPRESSION))
        {
            group.addNewCriterion();
        }
    }

    /**
     * Adds a new group.
     */
    public void addNewGroup()
    {
        GroupModel group = getGroupForAdd();
        if (group != null && canAddToGroup(group, "Group"))
        {
            group.addNewGroup();
        }
    }

    /**
     * Gets the mouse over row.
     *
     * @return the mouse over row
     */
    public int getMouseOverRow()
    {
        return myMouseOverRow;
    }

    /**
     * Removes the selected items.
     */
    public void removeSelected()
    {
        FilterTreeUtilities.removeItems(getSelectedItems(), myPreferencesRegistry, this);
    }

    @Override
    public void updateUI()
    {
        setUI(new ModifiedTreeUI(this));
    }

    @Override
    protected void paintComponent(Graphics g)
    {
        // Paint hover row
        if (myMouseOverRow != -1)
        {
            paintRowBackground(myMouseOverRow, Constants.HOVER_COLOR, g);
        }

        // Paint selected rows
        int[] selectionRows = getSelectionRows();
        if (selectionRows != null)
        {
            for (int selectedRow : selectionRows)
            {
                paintRowBackground(selectedRow, Constants.SELECTION_COLOR, g);
            }
        }

        super.paintComponent(g);
    }

    /**
     * Adds listeners.
     */
    private void addListeners()
    {
        // When the model changes, fire a model property change to force the
        // tree UI to update.
        myModel.addPropertyChangeListener(new PropertyChangeListener()
        {
            @Override
            public void stateChanged(PropertyChangeEvent e)
            {
                if (e.getProperty() == PropertyChangeEvent.Property.WRAPPED_VALUE_CHANGED)
                {
                    // A criterion was added/removed, rebuild the tree
                    if (e.getSource() instanceof GroupModel)
                    {
                        int[] selectionRows = getSelectionRows();
                        setModel(new DefaultTreeModel(createTreeNode(myModel.getGroup())));
                        JTreeUtilities.expandOrCollapseAll(FilterTree.this, true);
                        setSelectionRows(selectionRows);
                    }
                    /* A criterion changed, clear the selection to re-render
                     * with new values. */
                    else
                    {
                        clearSelection();
                    }
                }
            }
        });

        addMouseMotionListener(new MouseMotionAdapter()
        {
            @Override
            public void mouseMoved(MouseEvent e)
            {
                myMouseOverRow = getClosestRowForLocation(e.getX(), e.getY());
                repaint();
            }
        });

        addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                if (e.getClickCount() == 2)
                {
                    TreePath path = getPathForLocation(e.getX(), e.getY());
                    if (path != null)
                    {
                        Object userObject = FilterTreeUtilities.getUserObject(path.getLastPathComponent());
                        if (userObject instanceof CriterionModel)
                        {
                            CriterionEditorDialog.showDialog(FilterTree.this, (CriterionModel)userObject);
                        }
                    }
                }
            }

            // Take that BasicTreeUI, you bastard. Thought you could out-smart
            // me? Well, I've got news for you. I'll start editing even when I
            // have drag-n-drop enabled, because that's how I roll.
            @Override
            public void mousePressed(MouseEvent e)
            {
                if (getSelectionCount() == 1)
                {
                    TreePath path = getClosestPathForLocation(e.getX(), e.getY());
                    ((ModifiedTreeUI)getUI()).startEditing(path, e);
                }
            }
        });
    }

    /**
     * Determines whether something can be added to the given group, and
     * displays an error if not.
     *
     * @param group the group
     * @param type the type to be added
     * @return Whether something can be added to the given group
     */
    private boolean canAddToGroup(GroupModel group, String type)
    {
        if (group.getOperator().get() == Logical.NOT && group.getCount() >= 1)
        {
            JOptionPane.showMessageDialog(this, "Only one expression or group can be added to a NOT group.", "Can't Add " + type,
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    /**
     * Gets the group for an add operation.
     *
     * @return the group to which to add
     */
    private GroupModel getGroupForAdd()
    {
        GroupModel group = null;
        List<Object> selectedItems = getSelectedItems();
        if (selectedItems.size() == 1)
        {
            Object selectedItem = selectedItems.get(0);
            if (selectedItem instanceof GroupModel)
            {
                group = (GroupModel)selectedItem;
            }
            else if (selectedItem instanceof CriterionModel)
            {
                CriterionModel criterion = (CriterionModel)selectedItem;
                group = criterion.getParent();
            }
        }
        else if (selectedItems.isEmpty())
        {
            group = myModel.getGroup();
        }
        return group;
    }

    /**
     * Gets the selected items.
     *
     * @return the selected items
     */
    private List<Object> getSelectedItems()
    {
        List<Integer> selectionRows = getSelectionCount() > 0 ? CollectionUtilities.listView(getSelectionRows())
                : Collections.<Integer>emptyList();
        return StreamUtilities.map(selectionRows, new Function<Integer, Object>()
        {
            @Override
            public Object apply(Integer input)
            {
                Object node = getPathForRow(input.intValue()).getLastPathComponent();
                return FilterTreeUtilities.getUserObject(node);
            }
        });
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
            g.fillRect(0, bounds.y, bounds.width + bounds.x, bounds.height);
        }
    }
}

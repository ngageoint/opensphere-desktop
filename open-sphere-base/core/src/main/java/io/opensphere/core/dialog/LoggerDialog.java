package io.opensphere.core.dialog;

import java.awt.Color;
import java.awt.Component;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.border.BevelBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.apache.log4j.Category;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;

import io.opensphere.core.hud.awt.AbstractInternalFrame;
import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.swing.AbstractHUDPanel;
import io.opensphere.core.util.swing.JTreeUtilities;

/**
 * A dialog used for viewing and changing Logger levels.
 */
@SuppressWarnings("PMD.GodClass")
public class LoggerDialog extends AbstractInternalFrame
{
    /** Map of logger levels to colors. */
    private static final Map<Level, Color> LEVEL_COLORS = new LinkedHashMap<>();

    /** The separator used in Logger names. */
    private static final String SEPARATOR = ".";

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /** The node selected by the search. */
    private DefaultMutableTreeNode mySelectedNode;

    /** The tree. */
    private final JTree myTree;

    static
    {
        LEVEL_COLORS.put(Level.OFF, Color.BLACK);
        LEVEL_COLORS.put(Level.FATAL, Color.RED);
        LEVEL_COLORS.put(Level.ERROR, Color.MAGENTA);
        LEVEL_COLORS.put(Level.WARN, Color.ORANGE);
        LEVEL_COLORS.put(Level.INFO, Color.GREEN);
        LEVEL_COLORS.put(Level.DEBUG, Color.BLUE);
        LEVEL_COLORS.put(Level.TRACE, Color.GRAY);
        LEVEL_COLORS.put(Level.ALL, Color.WHITE);
    }

    /**
     * Construct the logger dialog.
     *
     * @param preferencesRegistry The preferences registry.
     * @param aTitle The title for the dialog.
     */
    public LoggerDialog(PreferencesRegistry preferencesRegistry, String aTitle)
    {
        super(aTitle, true, true, true);

        DefaultMutableTreeNode root = new DefaultMutableTreeNode(new LoggerNode(LogManager.getRootLogger()));
        buildTreeFromRoot(root);
        myTree = new JTree(root);
        myTree.setCellRenderer(new Renderer());
        myTree.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mousePressed(MouseEvent e)
            {
                if (e.isPopupTrigger())
                {
                    showMenu(e.getPoint());
                }
            }

            @Override
            public void mouseReleased(MouseEvent e)
            {
                mousePressed(e);
            }
        });
        ToolTipManager.sharedInstance().registerComponent(myTree);

        AbstractHUDPanel panel = new AbstractHUDPanel(preferencesRegistry);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(new JScrollPane(myTree));
        panel.add(createBottomPanel());
        getContentPane().add(panel);

        addComponentListener(new ComponentAdapter()
        {
            @Override
            public void componentShown(ComponentEvent e)
            {
                buildTree();
            }
        });
    }

    /**
     * Expand the tree to show a node.
     *
     * @param node The node to be shown.
     */
    private void expandNode(DefaultMutableTreeNode node)
    {
        myTree.expandPath(new TreePath(node.getPath()));
    }

    /**
     * For a logger that doesn't have its own level set, get a label that
     * indicates the effective level of the logger along with the parent logger
     * that defined the level.
     *
     * @param logger The logger without a level defined.
     * @return A label for the logger's effective level.
     */
    private String getEffectiveLevelLabel(Logger logger)
    {
        Logger parent = getParentWithLevel(logger);
        return getEffectiveLevelLabel(logger, parent);
    }

    /**
     * For a logger that doesn't have its own level set, get a label that
     * indicates the effective level of the logger along with the parent logger
     * that defined the level.
     *
     * @param logger The logger without a level defined.
     * @param parent The ancestor logger that has a level set.
     * @return A label for the logger's effective level.
     */
    private String getEffectiveLevelLabel(Logger logger, Logger parent)
    {
        StringBuilder sb = new StringBuilder("(");
        sb.append(parent.getName()).append(": ").append(parent.getLevel());
        sb.append(')');
        return sb.toString();
    }

    /**
     * Scroll the tree to show a node.
     *
     * @param node The node to be shown.
     */
    private void scrollToVisible(DefaultMutableTreeNode node)
    {
        myTree.scrollPathToVisible(new TreePath(node.getPath()));
    }

    /**
     * Search the tree in row order for a node that contains the given text. If
     * one is found, keep a reference to it so it can be drawn differently, and
     * scroll the tree to show it.
     *
     * @param text The text to search for (case insensitive).
     */
    @SuppressWarnings("unchecked")
    private void search(String text)
    {
        mySelectedNode = null;
        if (!text.isEmpty())
        {
            DefaultMutableTreeNode node = getRootNode();
            for (Enumeration<DefaultMutableTreeNode> nodeEnum = node.preorderEnumeration(); nodeEnum.hasMoreElements();)
            {
                node = nodeEnum.nextElement();
                if (node.toString().toLowerCase().contains(text.toLowerCase()))
                {
                    mySelectedNode = node;
                    scrollToVisible(node);
                    break;
                }
            }
        }

        myTree.repaint();
    }

    /**
     * Set the logger for the given node to the given level. Create the logger
     * if it doesn't exist.
     *
     * @param loggerNode The logger node.
     * @param level The level to set, or <code>null</code> to use the inherited
     *            level.
     */
    private void setLoggerLevel(final LoggerNode loggerNode, final Level level)
    {
        Logger logger = loggerNode.getLogger();
        if (logger == null && level != null)
        {
            logger = Logger.getLogger(loggerNode.toString());
            loggerNode.setLogger(logger);
        }
        if (logger != null)
        {
            logger.setLevel(level);
            myTree.repaint();
        }
    }

    /**
     * Display the popup menu.
     *
     * @param point The mouse point.
     */
    private void showMenu(Point point)
    {
        final TreePath path = myTree.getClosestPathForLocation(point.x, point.y);
        if (path == null || !myTree.getPathBounds(path).contains(point))
        {
            return;
        }

        DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode)path.getLastPathComponent();
        final LoggerNode loggerNode = (LoggerNode)treeNode.getUserObject();

        JPopupMenu popMenu = new JPopupMenu();
        JMenu setLevelMenu = new JMenu("Set Level   ");
        popMenu.add(setLevelMenu);
        for (Entry<Level, Color> entry : LEVEL_COLORS.entrySet())
        {
            final Level level = entry.getKey();
            Color color = entry.getValue();
            JMenuItem menuItem = new JMenuItem(level.toString());
            menuItem.setForeground(color);
            menuItem.addActionListener(e -> setLoggerLevel(loggerNode, level));
            setLevelMenu.add(menuItem);
        }
        JMenuItem addLoggerMenuItem = new JMenuItem("Add Logger...");
        addLoggerMenuItem.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                final String input = JOptionPane.showInputDialog(LoggerDialog.this, "Enter the name of the logger:",
                        loggerNode.toString());
                if (input != null)
                {
                    Logger.getLogger(input);
                    buildTree();
                    TreePath pathToNew = JTreeUtilities.findPathToNode(myTree, new JTreeUtilities.EndCriteria<TreeNode>()
                    {
                        @Override
                        public boolean found(TreeNode value)
                        {
                            return value.toString().equals(input);
                        }
                    });
                    if (pathToNew != null && pathToNew.getParentPath() != null)
                    {
                        myTree.expandPath(pathToNew.getParentPath());
                    }
                }
            }
        });
        popMenu.add(addLoggerMenuItem);
        if (loggerNode.getLogger() != LogManager.getRootLogger())
        {
            DefaultMutableTreeNode parent = (DefaultMutableTreeNode)treeNode.getParent();
            while (((LoggerNode)parent.getUserObject()).getLogger() == null)
            {
                parent = (DefaultMutableTreeNode)parent.getParent();
            }
            Logger logger = ((LoggerNode)parent.getUserObject()).getLogger();
            Logger parentLogger = getParentWithLevel(logger);
            String effectiveLevelLabel = getEffectiveLevelLabel(logger, parentLogger);
            JMenuItem menuItem = new JMenuItem(effectiveLevelLabel);
            Color color = LEVEL_COLORS.get(parentLogger.getLevel());
            if (color != null)
            {
                menuItem.setForeground(color);
            }
            menuItem.addActionListener(e -> setLoggerLevel(loggerNode, null));
            setLevelMenu.add(menuItem);
        }
        popMenu.show(myTree, point.x, point.y);
    }

    /**
     * Build the tree using the current logger configuration.
     */
    private void buildTree()
    {
        Enumeration<TreePath> expanded = myTree.getExpandedDescendants(new TreePath(getRootNode().getPath()));
        DefaultMutableTreeNode root = getRootNode();
        buildTreeFromRoot(root);

        // Expand the single-child nodes.
        DefaultMutableTreeNode node = root;
        while (node.getChildCount() == 1)
        {
            node = (DefaultMutableTreeNode)node.getFirstChild();
        }
        expandNode(node);

        if (expanded != null)
        {
            for (TreePath path : Collections.list(expanded))
            {
                myTree.expandPath(path);
            }
        }
    }

    /**
     * Build the tree using the current logger configuration.
     *
     * @param root The root node of the tree.
     */
    private void buildTreeFromRoot(DefaultMutableTreeNode root)
    {
        boolean changed = false;

        Enumeration<Logger> loggerEnum = getCurrentLoggers();
        while (loggerEnum.hasMoreElements())
        {
            Logger logger = loggerEnum.nextElement();
            String name = logger.getName();
            String[] split = name.split("\\" + SEPARATOR);
            DefaultMutableTreeNode node = root;
            for (int i = 0; i < split.length; i++)
            {
                String nodeName = i > 0 ? node.toString() + SEPARATOR + split[i] : split[i];
                DefaultMutableTreeNode child = getNodeChild(node, nodeName);
                if (child == null)
                {
                    child = new DefaultMutableTreeNode(new LoggerNode(nodeName));
                    node.add(child);
                    changed = true;
                }

                node = child;
            }

            ((LoggerNode)node.getUserObject()).setLogger(logger);
        }

        if (changed && myTree != null)
        {
            ((DefaultTreeModel)myTree.getModel()).reload();
        }
    }

    /**
     * Initialize the controls at the bottom of the dialog.
     *
     * @return The bottom panel.
     */
    private JPanel createBottomPanel()
    {
        JLabel searchButton = new JLabel("Search: ");
        final int columns = 20;
        final JTextField searchField = new JTextField(columns);
        searchField.addKeyListener(new KeyAdapter()
        {
            @Override
            public void keyReleased(KeyEvent e)
            {
                String text = ((JTextField)e.getComponent()).getText();
                search(text);
            }

            @Override
            public void keyTyped(KeyEvent e)
            {
                if (e.getKeyChar() == KeyEvent.VK_ESCAPE)
                {
                    setVisible(false);
                    e.consume();
                }
            }
        });
        searchField.addActionListener(e ->
        {
            if (mySelectedNode != null)
            {
                Logger logger = ((LoggerNode)mySelectedNode.getUserObject()).getLogger();
                if (logger != null)
                {
                    Level level = logger.getEffectiveLevel();
                    if (level.toInt() < Priority.OFF_INT)
                    {
                        logger.setLevel(Level.OFF);
                    }
                    else
                    {
                        logger.setLevel(Level.ALL);
                    }
                }
            }
        });
        addComponentListener(new ComponentAdapter()
        {
            @Override
            public void componentShown(ComponentEvent e)
            {
                searchField.requestFocusInWindow();
            }
        });

        JPanel searchPanel = new JPanel();
        searchPanel.add(searchButton);
        searchPanel.add(searchField);

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> buildTree());

        JButton offButton = new JButton("All Off");
        offButton.addActionListener(e ->
        {
            Logger.getRootLogger().setLevel(Level.OFF);
            Enumeration<Logger> loggerEnum = getCurrentLoggers();
            while (loggerEnum.hasMoreElements())
            {
                Logger logger = loggerEnum.nextElement();
                if (logger.getParent() != null)
                {
                    logger.setLevel(null);
                }
            }
            myTree.repaint();
        });

        JPanel bottomPanel = new JPanel();
        bottomPanel.add(searchPanel);
        bottomPanel.add(refreshButton);
        bottomPanel.add(offButton);
        return bottomPanel;
    }

    /**
     * Wrapper to get the current loggers, suppressing the unchecked cast
     * warning.
     *
     * @return An enumeration of the current loggers.
     */
    @SuppressWarnings("unchecked")
    private Enumeration<Logger> getCurrentLoggers()
    {
        return LogManager.getCurrentLoggers();
    }

    /**
     * Find a child of a node.
     *
     * @param node The parent node.
     * @param name The name of the child node.
     * @return The child, or <code>null</code> if one was not found.
     */
    private DefaultMutableTreeNode getNodeChild(DefaultMutableTreeNode node, String name)
    {
        Enumeration<?> children = node.children();
        Object child;
        while (children.hasMoreElements())
        {
            child = children.nextElement();
            if (child.toString().equals(name))
            {
                return (DefaultMutableTreeNode)child;
            }
        }

        return null;
    }

    /**
     * Find an ancestor of a logger that has a logging level set.
     *
     * @param logger The logger.
     * @return An ancestor of the logger.
     */
    private Logger getParentWithLevel(Logger logger)
    {
        Category cat = logger;
        while (cat.getLevel() == null)
        {
            cat = cat.getParent();
        }
        return (Logger)cat;
    }

    /**
     * Helper method to get the root node of the tree.
     *
     * @return The root node.
     */
    private DefaultMutableTreeNode getRootNode()
    {
        return (DefaultMutableTreeNode)myTree.getModel().getRoot();
    }

    /**
     * Class used as the user object in the JTree.
     */
    private static class LoggerNode
    {
        /** The logger. Type is {@code Category} to avoid style warnings. */
        private Category myLogger;

        /** The display name. */
        private final String myName;

        /**
         * Construct a node with logger, using the logger's name for the display
         * name.
         *
         * @param logger The logger.
         */
        public LoggerNode(Logger logger)
        {
            myLogger = logger;
            myName = logger.getName();
        }

        /**
         * Construct a node with a name but <code>null</code> logger.
         *
         * @param name The display name.
         */
        public LoggerNode(String name)
        {
            myLogger = null;
            myName = name;
        }

        /**
         * Mutator for the logger.
         *
         * @param logger the logger
         */
        public void setLogger(Logger logger)
        {
            myLogger = logger;
        }

        @Override
        public String toString()
        {
            return myName;
        }

        /**
         * Accessor for the logger.
         *
         * @return the logger
         */
        protected Logger getLogger()
        {
            return (Logger)myLogger;
        }
    }

    /**
     * The renderer for the JTree.
     */
    private final class Renderer extends DefaultTreeCellRenderer
    {
        /**
         * Serial version UID.
         */
        private static final long serialVersionUID = 1L;

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf,
                int row, boolean hasFocus1)
        {
            DefaultTreeCellRenderer comp = (DefaultTreeCellRenderer)super.getTreeCellRendererComponent(tree, value, sel, expanded,
                    leaf, row, hasFocus1);
            DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode)value;
            if (Utilities.sameInstance(mySelectedNode, treeNode))
            {
                comp.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
                comp.validate();
            }
            else
            {
                comp.setBorder(BorderFactory.createEmptyBorder());
            }
            Object userObject = treeNode.getUserObject();
            if (userObject instanceof LoggerNode)
            {
                Logger logger = ((LoggerNode)userObject).getLogger();
                Level level = null;
                if (logger == null)
                {
                    while ((treeNode = (DefaultMutableTreeNode)treeNode.getParent()) != null)
                    {
                        logger = ((LoggerNode)treeNode.getUserObject()).getLogger();
                        if (logger != null)
                        {
                            level = logger.getEffectiveLevel();
                            myTree.setToolTipText(getEffectiveLevelLabel(logger));
                            break;
                        }
                    }
                }
                else
                {
                    level = logger.getLevel();
                    if (level == null)
                    {
                        level = logger.getEffectiveLevel();
                        myTree.setToolTipText(getEffectiveLevelLabel(logger));
                    }
                    else
                    {
                        myTree.setToolTipText(level.toString());
                    }
                }
                if (level != null)
                {
                    Color color = LEVEL_COLORS.get(level);
                    if (color != null)
                    {
                        setForeground(color);
                    }
                }
            }
            return comp;
        }
    }
}

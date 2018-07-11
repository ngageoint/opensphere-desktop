package io.opensphere.controlpanels.options;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Observable;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.apache.commons.lang3.StringUtils;

import io.opensphere.core.Toolbox;
import io.opensphere.core.control.ui.impl.TopicOrSubTopicMatchesRegexOptionsProviderFilter;
import io.opensphere.core.options.OptionsProvider;
import io.opensphere.core.options.OptionsProviderUserObject;
import io.opensphere.core.quantify.QuantifyToolboxUtils;
import io.opensphere.core.util.NonSuckingObservable;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.swing.AbstractHUDPanel;
import io.opensphere.core.util.swing.ButtonPanel;
import io.opensphere.core.util.swing.GhostTextField;
import io.opensphere.core.util.swing.JTreeUtilities;
import io.opensphere.core.util.swing.JTreeUtilities.EndCriteria;

/**
 * The Class OptionsPanel.
 */
@SuppressWarnings("PMD.GodClass")
class OptionsPanel extends AbstractHUDPanel
{
    /** The Constant TREE_RIGHT_ICON. */
    private static final ImageIcon TREE_COLLAPSED_ICON = new ImageIcon(
            OptionsPanel.class.getClassLoader().getResource("images/right.png"));

    /** The Constant TREE_DOWN_ICON. */
    private static final ImageIcon TREE_EXPANDED_ICON = new ImageIcon(
            OptionsPanel.class.getClassLoader().getResource("images/down.png"));

    /** Serial. */
    private static final long serialVersionUID = 1L;

    /** The Apply button. */
    private JButton myApplyButton;

    /** The Apply restore panel. */
    private JPanel myApplyRestorePanel;

    /** The Close button. */
    private JButton myCloseButton;

    /** The Current option editor panel. */
    private JScrollPane myCurrentOptionEditorJSP;

    /** The Current option editor panel. */
    private JPanel myCurrentOptionEditorPanel;

    /** The Current option header panel. */
    private JPanel myCurrentOptionHeaderPanel;

    /** The Current provider. */
    private transient OptionsProvider myCurrentProvider;

    /** The Editor title label. */
    private JLabel myEditorTitleLabel;

    /** The Editor title panel. */
    private JPanel myEditorTitlePanel;

    /** The Lower button panel. */
    private JPanel myLowerButtonPanel;

    /** The Option editor panel. */
    private JPanel myOptionEditorPanel;

    /** The Restore defaults button. */
    private JButton myRestoreDefaultsButton;

    /** The Toolbox. */
    private final transient Toolbox myToolbox;

    /** The Topic filter tf. */
    private JTextField myTopicFilterTF;

    /** The Topic tree. */
    private JTree myTopicTree;

    /** The Topic tree panel. */
    private JPanel myTopicTreePanel;

    /** The closed observable. */
    private final transient Observable myClosed = new NonSuckingObservable();

    /**
     * Instantiates a new layer manager panel.
     *
     * @param toolbox the toolbox
     */
    public OptionsPanel(Toolbox toolbox)
    {
        super(toolbox.getPreferencesRegistry());
        myToolbox = toolbox;
        this.setSize(getTopLevelPanelDim());
        setMinimumSize(getSize());
        setPreferredSize(getSize());
        setLayout(new BorderLayout());
        setBackground(getBackgroundColor());
        // add(getTabbedPane(), BorderLayout.CENTER);

        add(getTopicTreePanel(), BorderLayout.WEST);
        add(getOptionEditorPanel(), BorderLayout.CENTER);
        add(getLowerButtonPanel(), BorderLayout.SOUTH);
        getOptionEditorPanel().setVisible(false);
        refreshTopicTree();
    }

    /**
     * Show provider.
     *
     * @param provider the provider
     */
    public void showProvider(final OptionsProvider provider)
    {
        TreePath pathToRequested = JTreeUtilities.findPathToNode(getTopicTree(), new EndCriteria<TreeNode>()
        {
            @Override
            public boolean found(TreeNode value)
            {
                boolean found = false;
                if (value instanceof DefaultMutableTreeNode)
                {
                    Object uo = ((DefaultMutableTreeNode)value).getUserObject();
                    if (uo instanceof OptionsProviderUserObject)
                    {
                        found = Utilities.sameInstance(((OptionsProviderUserObject)uo).getOptionsProvider(), provider);
                    }
                }
                return found;
            }
        });
        if (pathToRequested != null)
        {
            getTopicTree().addSelectionPath(pathToRequested);
        }
    }

    /**
     * Refresh topic tree.
     */
    public final void refreshTopicTree()
    {
        TreePath selPath = getTopicTree().getSelectionPath();
        Object lastPathComp = selPath == null ? null : selPath.getLastPathComponent();
        OptionsProvider op = null;
        if (lastPathComp instanceof DefaultMutableTreeNode)
        {
            Object userObject = ((DefaultMutableTreeNode)lastPathComp).getUserObject();
            if (userObject instanceof OptionsProviderUserObject)
            {
                op = ((OptionsProviderUserObject)userObject).getOptionsProvider();
            }
        }
        String filterText = getTopicFilterTF().getText();
        MutableTreeNode node = myToolbox.getUIRegistry().getOptionsRegistry()
                .getOptionProviderTree(StringUtils.isEmpty(filterText) ? null
                        : new TopicOrSubTopicMatchesRegexOptionsProviderFilter(genRegex(filterText)));
        DefaultTreeModel model = new DefaultTreeModel(node, false);
        getTopicTree().setModel(model);
        JTreeUtilities.expandOrCollapseAll(getTopicTree(), true);

        /* If we previously had a selected node in the tree, find its new
         * location int he new tree if possible and select it. */
        if (op != null)
        {
            final OptionsProvider lastOp = op;
            TreePath pathToLastSelected = JTreeUtilities.findPathToNode(getTopicTree(), new EndCriteria<TreeNode>()
            {
                @Override
                public boolean found(TreeNode value)
                {
                    boolean found = false;
                    if (value instanceof DefaultMutableTreeNode)
                    {
                        Object uo = ((DefaultMutableTreeNode)value).getUserObject();
                        if (uo instanceof OptionsProviderUserObject)
                        {
                            found = Utilities.sameInstance(lastOp, ((OptionsProviderUserObject)uo).getOptionsProvider());
                        }
                    }
                    return found;
                }
            });
            if (pathToLastSelected != null)
            {
                getTopicTree().addSelectionPath(pathToLastSelected);
            }
        }
    }

    /**
     * Gets the closed observable.
     *
     * @return the closed observable
     */
    public Observable getClosed()
    {
        return myClosed;
    }

    /**
     * Gets the apply button.
     *
     * @return the apply button
     */
    private JButton getApplyButton()
    {
        if (myApplyButton == null)
        {
            myApplyButton = new JButton("Apply");
            myApplyButton.setMargin(ButtonPanel.INSETS_MEDIUM);
            myApplyButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    if (myCurrentProvider != null)
                    {
                        myCurrentProvider.applyChanges();
                    }
                }
            });
        }
        return myApplyButton;
    }

    /**
     * Gets the apply restore panel.
     *
     * @return the apply restore panel
     */
    private JPanel getApplyRestorePanel()
    {
        if (myApplyRestorePanel == null)
        {
            myApplyRestorePanel = new JPanel();
            myApplyRestorePanel.setLayout(new BoxLayout(myApplyRestorePanel, BoxLayout.X_AXIS));
            myApplyRestorePanel.setMaximumSize(new Dimension(3000, 30));
            myApplyRestorePanel.setMinimumSize(new Dimension(100, 30));
            myApplyRestorePanel.setPreferredSize(new Dimension(370, 30));
            JPanel empty = new JPanel();
            empty.setBackground(getBackgroundColor());
            myApplyRestorePanel.add(empty);
            myApplyRestorePanel.add(Box.createHorizontalGlue());
            myApplyRestorePanel.add(getApplyButton());
            myApplyRestorePanel.add(Box.createHorizontalStrut(6));
            myApplyRestorePanel.add(getRestoreDefaultsButton());
            myApplyRestorePanel.add(Box.createHorizontalStrut(10));
            myApplyRestorePanel.setBackground(getBackgroundColor());
        }

        return myApplyRestorePanel;
    }

    /**
     * Gets the close button.
     *
     * @return the close button
     */
    private JButton getCloseButton()
    {
        if (myCloseButton == null)
        {
            myCloseButton = new JButton("Close");
            myCloseButton.setMargin(ButtonPanel.INSETS_MEDIUM);
            myCloseButton.addActionListener(e -> myClosed.notifyObservers());
        }
        return myCloseButton;
    }

    /**
     * Gets the current option editor header panel.
     *
     * @return the current option editor header panel
     */
    private JPanel getCurrentOptionEditorHeaderPanel()
    {
        if (myCurrentOptionHeaderPanel == null)
        {
            myCurrentOptionHeaderPanel = new JPanel(new BorderLayout());
            myCurrentOptionHeaderPanel.setBackground(new Color(0, 0, 0, 0));
        }
        return myCurrentOptionHeaderPanel;
    }

    /**
     * Gets the current option editor panel.
     *
     * @return the current option editor panel
     */
    private JScrollPane getCurrentOptionEditorJSP()
    {
        if (myCurrentOptionEditorJSP == null)
        {
            myCurrentOptionEditorJSP = new JScrollPane(getCurrentOptionEditorPanel(), JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                    JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            myCurrentOptionEditorJSP.setBackground(new Color(0, 0, 0, 0));
        }
        return myCurrentOptionEditorJSP;
    }

    /**
     * Gets the current option editor panel.
     *
     * @return the current option editor panel
     */
    private JPanel getCurrentOptionEditorPanel()
    {
        if (myCurrentOptionEditorPanel == null)
        {
            myCurrentOptionEditorPanel = new JPanel(new BorderLayout());
            myCurrentOptionEditorPanel.setBackground(new Color(0, 0, 0, 0));
        }
        return myCurrentOptionEditorPanel;
    }

    /**
     * Gets the editor title label.
     *
     * @return the editor title label
     */
    private JLabel getEditorTitleLabel()
    {
        if (myEditorTitleLabel == null)
        {
            myEditorTitleLabel = new JLabel("Settings");
            myEditorTitleLabel
                    .setFont(myEditorTitleLabel.getFont().deriveFont(Font.BOLD, myEditorTitleLabel.getFont().getSize() + 4));
        }
        return myEditorTitleLabel;
    }

    /**
     * Gets the editor title panel.
     *
     * @return the editor title panel
     */
    private JPanel getEditorTitlePanel()
    {
        if (myEditorTitlePanel == null)
        {
            myEditorTitlePanel = new JPanel();
            myEditorTitlePanel.setLayout(new BoxLayout(myEditorTitlePanel, BoxLayout.X_AXIS));
            myEditorTitlePanel.setMaximumSize(new Dimension(3000, 30));
            myEditorTitlePanel.setMinimumSize(new Dimension(100, 30));
            myEditorTitlePanel.setPreferredSize(new Dimension(370, 30));
            myEditorTitlePanel.setBackground(getBackgroundColor());
            myEditorTitlePanel.add(Box.createHorizontalStrut(6));
            myEditorTitlePanel.add(getEditorTitleLabel());
            myEditorTitlePanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
            JPanel empty = new JPanel();
            empty.setBackground(getBackgroundColor());
            myEditorTitlePanel.add(empty);
        }
        return myEditorTitlePanel;
    }

    /**
     * Gets the lower button panel.
     *
     * @return the lower button panel
     */
    private JPanel getLowerButtonPanel()
    {
        if (myLowerButtonPanel == null)
        {
            myLowerButtonPanel = new JPanel();
            myLowerButtonPanel.setLayout(new BoxLayout(myLowerButtonPanel, BoxLayout.X_AXIS));
            myLowerButtonPanel.setMaximumSize(new Dimension(3000, 35));
            myLowerButtonPanel.setMinimumSize(new Dimension(100, 35));
            myLowerButtonPanel.setPreferredSize(new Dimension(370, 35));
            myLowerButtonPanel.setBackground(getBackgroundColor());
            JPanel empty = new JPanel();
            empty.setBackground(getBackgroundColor());
            myLowerButtonPanel.add(empty);
            myLowerButtonPanel.add(Box.createHorizontalGlue());
            myLowerButtonPanel.add(getCloseButton());
            myLowerButtonPanel.add(Box.createHorizontalGlue());
            JPanel empty2 = new JPanel();
            empty2.setBackground(getBackgroundColor());
            myLowerButtonPanel.add(empty2);
        }
        return myLowerButtonPanel;
    }

    /**
     * Gets the option editor panel.
     *
     * @return the option editor panel
     */
    private JPanel getOptionEditorPanel()
    {
        if (myOptionEditorPanel == null)
        {
            myOptionEditorPanel = new JPanel();
            myOptionEditorPanel.setBackground(getBackgroundColor());
            myOptionEditorPanel.setBorder(BorderFactory.createLineBorder(Color.lightGray));
            myOptionEditorPanel.setLayout(new BoxLayout(myOptionEditorPanel, BoxLayout.Y_AXIS));
            myOptionEditorPanel.add(getEditorTitlePanel());
            myOptionEditorPanel.add(getCurrentOptionEditorHeaderPanel());
            myOptionEditorPanel.add(getCurrentOptionEditorJSP());
            myOptionEditorPanel.add(getApplyRestorePanel());
        }
        return myOptionEditorPanel;
    }

    /**
     * Gets the restore defaults button.
     *
     * @return the restore defaults button
     */
    private JButton getRestoreDefaultsButton()
    {
        if (myRestoreDefaultsButton == null)
        {
            myRestoreDefaultsButton = new JButton("Restore Defaults");
            myRestoreDefaultsButton.setMargin(ButtonPanel.INSETS_MEDIUM);
            myRestoreDefaultsButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    if (myCurrentProvider != null)
                    {
                        myCurrentProvider.restoreDefaults();
                    }
                }
            });
        }
        return myRestoreDefaultsButton;
    }

    /**
     * Gets the topic filter tf.
     *
     * @return the topic filter tf
     */
    private JTextField getTopicFilterTF()
    {
        if (myTopicFilterTF == null)
        {
            myTopicFilterTF = new GhostTextField("Type Filter Text");
            myTopicFilterTF.getDocument().addDocumentListener(new DocumentListener()
            {
                @Override
                public void changedUpdate(DocumentEvent e)
                {
                    refreshTopicTree();
                }

                @Override
                public void insertUpdate(DocumentEvent e)
                {
                    refreshTopicTree();
                }

                @Override
                public void removeUpdate(DocumentEvent e)
                {
                    refreshTopicTree();
                }
            });
        }
        return myTopicFilterTF;
    }

    /**
     * Gets the topic tree.
     *
     * @return the topic tree
     */
    private JTree getTopicTree()
    {
        if (myTopicTree == null)
        {
            DefaultTreeModel emptyModel = new DefaultTreeModel(new DefaultMutableTreeNode());
            myTopicTree = new JTree(emptyModel);
            DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
            renderer.setBackgroundNonSelectionColor(new Color(0, 0, 0, 0));
            renderer.setOpenIcon(TREE_EXPANDED_ICON);
            renderer.setClosedIcon(TREE_COLLAPSED_ICON);

            Color c = renderer.getBackgroundSelectionColor();
            renderer.setBackgroundSelectionColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 50));
            myTopicTree.setCellRenderer(renderer);
            myTopicTree.setRootVisible(false);
            DefaultTreeSelectionModel selMode = new DefaultTreeSelectionModel();
            selMode.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
            myTopicTree.setSelectionModel(selMode);
            myTopicTree.setBackground(new Color(0, 0, 0, 0));
            myTopicTree.addTreeSelectionListener(new TreeSelectionListener()
            {
                @Override
                public void valueChanged(TreeSelectionEvent e)
                {
                    TreePath path = e.getNewLeadSelectionPath();
                    if (path == null)
                    {
                        setCurrentOptionsProvider(null);
                    }
                    else
                    {
                        Object lastPathComp = path.getLastPathComponent();
                        if (lastPathComp instanceof DefaultMutableTreeNode)
                        {
                            DefaultMutableTreeNode node = (DefaultMutableTreeNode)lastPathComp;
                            Object userObj = node.getUserObject();
                            if (userObj instanceof OptionsProviderUserObject)
                            {
                                OptionsProviderUserObject o = (OptionsProviderUserObject)userObj;
                                if (node.getParent() != null)
                                {
                                    Object parent = ((DefaultMutableTreeNode)node.getParent()).getUserObject();
                                    if (parent instanceof OptionsProviderUserObject)
                                        QuantifyToolboxUtils.collectMetric(myToolbox,
                                            "mist3d.settings.left-menu-options."
                                            + ((OptionsProviderUserObject)parent).getNodeName().toLowerCase().replaceAll(" ", "-")
                                            + "." + o.getNodeName().toLowerCase().replaceAll(" ", "-"));
                                }
                                else
                                {
                                    QuantifyToolboxUtils.collectMetric(myToolbox, "mist3d.settings.left-menu-options."
                                        + o.getNodeName().toLowerCase().replaceAll(" ", "-"));
                                }
                                setCurrentOptionsProvider(o.getOptionsProvider());
                            }
                        }
                    }
                }
            });
        }
        return myTopicTree;
    }

    /**
     * Gets the topic tree panel.
     *
     * @return the topic tree panel
     */
    private JPanel getTopicTreePanel()
    {
        if (myTopicTreePanel == null)
        {
            myTopicTreePanel = new JPanel(new BorderLayout());
            myTopicTreePanel.setBackground(getBackgroundColor());
            myTopicTreePanel.setMaximumSize(new Dimension(180, 3000));
            myTopicTreePanel.setMinimumSize(new Dimension(180, 400));
            myTopicTreePanel.setPreferredSize(new Dimension(180, 400));

            JPanel searchPanel = new JPanel(new BorderLayout());
            searchPanel.setBackground(getBackgroundColor());
            searchPanel.setBorder(BorderFactory.createEmptyBorder(3, 0, 3, 0));
            searchPanel.add(getTopicFilterTF());

            myTopicTreePanel.add(searchPanel, BorderLayout.NORTH);
            JScrollPane jsp = new JScrollPane(getTopicTree(), JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                    JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            jsp.setBackground(getBackgroundColor());
            myTopicTreePanel.add(jsp, BorderLayout.CENTER);
        }
        return myTopicTreePanel;
    }

    /**
     * Sets the current options provider.
     *
     * @param optionsProvider the new current options provider
     */
    private void setCurrentOptionsProvider(final OptionsProvider optionsProvider)
    {
        assert EventQueue.isDispatchThread();

        myCurrentProvider = optionsProvider;

        getCurrentOptionEditorHeaderPanel().removeAll();
        getCurrentOptionEditorHeaderPanel().setVisible(false);
        getCurrentOptionEditorPanel().removeAll();
        if (optionsProvider != null)
        {
            final JComponent optPanel = optionsProvider.getOptionsPanel();
            getEditorTitleLabel().setText(optionsProvider.getTopic());
            getApplyRestorePanel().setVisible(optionsProvider.usesApply() || optionsProvider.usesRestore());
            getApplyButton().setVisible(optionsProvider.usesApply());
            getRestoreDefaultsButton().setVisible(optionsProvider.usesRestore());
            myOptionEditorPanel.setVisible(true);
            final JPanel headerPanel = optionsProvider.getOptionsHeaderPanel();
            if (headerPanel != null)
            {
                getCurrentOptionEditorHeaderPanel().setVisible(true);
                getCurrentOptionEditorHeaderPanel().add(headerPanel, BorderLayout.CENTER);
                if (headerPanel.getSize() != null)
                {
                    getCurrentOptionEditorHeaderPanel().setSize(headerPanel.getSize());
                    getCurrentOptionEditorHeaderPanel().setPreferredSize(headerPanel.getSize());
                    getCurrentOptionEditorHeaderPanel().setMinimumSize(headerPanel.getSize());
                    getCurrentOptionEditorHeaderPanel().setMaximumSize(headerPanel.getSize());
                }
            }
            getCurrentOptionEditorPanel().add(optPanel, BorderLayout.CENTER);
        }
        else
        {
            myOptionEditorPanel.setVisible(false);
        }
        getCurrentOptionEditorHeaderPanel().revalidate();
        getCurrentOptionEditorHeaderPanel().repaint();

        getCurrentOptionEditorPanel().revalidate();
        getCurrentOptionEditorPanel().repaint();
    }

    /**
     * Generates a regex, man.
     *
     * @param filterText the filter text
     * @return the regex
     */
    private static String genRegex(String filterText)
    {
        StringBuilder sb = new StringBuilder(32);
        sb.append("(?i)(" + "(.*?)(").append(Pattern.quote(filterText)).append(")(.*?)" + ")");
        return sb.toString();
    }
}

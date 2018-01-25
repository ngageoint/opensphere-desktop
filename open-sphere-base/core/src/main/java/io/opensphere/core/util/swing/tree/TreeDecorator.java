package io.opensphere.core.util.swing.tree;

import java.awt.Dimension;

import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.plaf.basic.BasicTreeUI;

import com.jidesoft.swing.CheckBoxTree;

/**
 * Decorates a JTree.
 */
public final class TreeDecorator
{
    /**
     * Decorates the given tree.
     *
     * @param tree the tree
     * @param <T> the type of the tree
     * @return the decorated tree
     */
    public static <T extends JTree> T decorate(T tree)
    {
        return decorate(tree, false, true);
    }

    /**
     * Decorates the given tree and forces check boxes to be displayed.
     *
     * @param <T> the generic type
     * @param tree the tree
     * @param useOnOffIcons the use on off icons
     * @return the t
     */
    public static <T extends JTree> T decorate(T tree, boolean useOnOffIcons)
    {
        return decorate(tree, useOnOffIcons, true);
    }

    /**
     * Decorates the given tree.
     *
     * @param <T> the type of the tree
     * @param tree the tree
     * @param useOnOffIcons whether to use on/off icons
     * @param showCheckbox show the check box or not
     * @return the decorated tree
     */
    public static <T extends JTree> T decorate(T tree, boolean useOnOffIcons, boolean showCheckbox)
    {
        // Set up check box tree look & feel
        if (tree instanceof CheckBoxTree)
        {
            CheckBoxTree checkBoxTree = (CheckBoxTree)tree;
            TristateCheckBoxWithButtonModeSupport checkBox = new TristateCheckBoxWithButtonModeSupport();

            if (!showCheckbox)
            {
                checkBox.setPreferredSize(new Dimension(1, 1));
            }
            else
            {
                checkBox.setPreferredSize(new Dimension(useOnOffIcons ? 35 : checkBox.getPreferredSize().width, 0));
            }
            checkBoxTree.setCheckBox(checkBox);
            checkBoxTree.getCheckBoxTreeSelectionModel().setSingleEventMode(true);
            checkBoxTree.setSelectPartialOnToggling(false);
        }

        // Set up normal tree look & feel
        tree.setRootVisible(false);
        tree.setShowsRootHandles(true);
        tree.putClientProperty("JTree.lineStyle", "None");
        BasicTreeUI treeUI = (BasicTreeUI)tree.getUI();
        treeUI.setCollapsedIcon(new ImageIcon(TreeDecorator.class.getClassLoader().getResource("images/right.png")));
        treeUI.setExpandedIcon(new ImageIcon(TreeDecorator.class.getClassLoader().getResource("images/down.png")));
        treeUI.setRightChildIndent(9);
        treeUI.setLeftChildIndent(5);
        ToolTipManager.sharedInstance().registerComponent(tree);

        return tree;
    }

    /**
     * Private constructor.
     */
    private TreeDecorator()
    {
    }
}

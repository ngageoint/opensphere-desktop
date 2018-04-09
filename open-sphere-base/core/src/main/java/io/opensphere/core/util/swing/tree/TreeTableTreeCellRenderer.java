package io.opensphere.core.util.swing.tree;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;

import com.jidesoft.swing.CheckBoxTree;
import com.jidesoft.swing.TristateCheckBox;

import io.opensphere.core.util.swing.GridBagPanel;

/** Tree cell renderer for the tree table. */
public class TreeTableTreeCellRenderer extends GridBagPanel implements TreeCellRenderer
{
    /** The tool tip text which is current for this renderer type. */
    private static String ourToolTipText;

    /** Serial. */
    private static final long serialVersionUID = 1L;

    /** The Bold font. */
    private final Font myBoldFont;

    /** The busy label. */
    private final JLabel myBusyLabel;

    /** The Component widths. */
    private int myComponentWidths;

    /** The image observer. */
    private NodeImageObserver myImageObserver;

    /** The Plain font. */
    private final Font myPlainFont;

    /** The text label for the node. */
    private final JLabel myText = new JLabel();

    /** The Use on off icons. */
    private final boolean myUseOnOffIcons;

    /**
     * Set the text to use for tool tips.
     *
     * @param text to use for tool tips.
     */
    public static void setToolTipStoredText(String text)
    {
        ourToolTipText = text;
    }

    /**
     * Constructor.
     *
     * @param useOnOffIcons the use on off icons
     */
    public TreeTableTreeCellRenderer(boolean useOnOffIcons)
    {
        myUseOnOffIcons = useOnOffIcons;

        myBoldFont = myText.getFont().deriveFont(Font.BOLD);
        myPlainFont = myText.getFont();

        anchorWest();
        setOpaque(false);
        setBackground(new Color(0, 0, 0, 0));
        setBorder(null);

        myBusyLabel = new JLabel(new ImageIcon(TreeTableTreeCellRenderer.class.getResource("/images/busy.gif")));

        myText.setOpaque(false);
        myText.setBorder(null);
    }

    /**
     * Adds the prefix icons.
     *
     * @param tree The tree.
     * @param panel the panel
     * @param node the tree node
     */
    public void addPrefixIcons(JTree tree, JPanel panel, TreeTableTreeNode node)
    {
        /* intentionally blank, provided for subclass implementations to
         * override */
    }

    /**
     * Format text.
     *
     * @param payload the payload
     * @param label the label
     */
    public void formatText(ButtonModelPayload payload, JLabel label)
    {
        /* intentionally blank, provided for subclass implementations to
         * override */
    }

    /**
     * Sets the state of the current node within the tree.
     *
     * @param tree the tree
     * @param pNode the node for which to set the state.
     * @param pTristateCheckBox the check box into which to set the state.
     */
    public void setState(JTree tree, TreeTableTreeNode pNode, TristateCheckBox pTristateCheckBox)
    {
        /* intentionally blank, provided for subclass implementations to
         * override */
    }

    /**
     * Gets the added component width.
     *
     * @return the added component width
     */
    public int getAddedComponentWidth()
    {
        return myComponentWidths;
    }

    /**
     * Gets the label.
     *
     * @return the label
     */
    public JLabel getLabel()
    {
        return myText;
    }

    /**
     * Gets the panel.
     *
     * @return the panel
     */
    public JPanel getPanel()
    {
        return this;
    }

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean isSelected, boolean expanded, boolean leaf,
            int row, boolean hasFocus)
    {
        if (value instanceof TreeTableTreeNode)
        {
            myComponentWidths = 0;
            removeAll();
            TreeTableTreeNode node = (TreeTableTreeNode)value;
            myText.setText(node.getPayload().getButton().getText());
            myText.setFont(leaf ? myPlainFont : myBoldFont);

            formatText(node.getPayload(), myText);

            manageBusyLabel(tree, node);
            if (tree instanceof CheckBoxTree)
            {
                myComponentWidths += ((CheckBoxTree)tree).getCheckBox().getWidth();
            }
            addPrefixIcons(tree, this, node);
            setState(tree, node, ((CheckBoxTree)tree).getCheckBox());
            fillHorizontal();
            add(myText);
            fillNone();

            setToolTipText(ourToolTipText);
        }

        return this;
    }

    /**
     * Checks if is busy label visible.
     *
     * @param buttonModelPayload the button model payload
     * @return true, if is busy label visible
     */
    public boolean isBusyLabelVisible(ButtonModelPayload buttonModelPayload)
    {
        return false;
    }

    /**
     * Checks if is show check box.
     *
     * @param payload the payload
     * @return true, if is show check box
     */
    public boolean isShowCheckBox(ButtonModelPayload payload)
    {
        return true;
    }

    /**
     * Returns whether to use on/off icons for the check boxes.
     *
     * @return whether to use on/off icons for the check boxes
     */
    public boolean isUseOnOffIcons()
    {
        return myUseOnOffIcons;
    }

    /**
     * Reset component widths.
     */
    public void resetComponentWidths()
    {
        myComponentWidths = 0;
    }

    /**
     * Adds the component's width.
     *
     * @param comp the component
     */
    public void addComponentWidth(Component comp)
    {
        myComponentWidths += comp.getPreferredSize().width;
    }

    /**
     * Adds the busy label if necessary, and takes care of managing its
     * painting.
     *
     * @param tree the tree
     * @param node the node
     */
    private void manageBusyLabel(JTree tree, TreeTableTreeNode node)
    {
        if (myImageObserver == null)
        {
            myImageObserver = new NodeImageObserver(tree);
            ((ImageIcon)myBusyLabel.getIcon()).setImageObserver(myImageObserver);
        }

        TreePath path = new TreePath(node.getPath());
        if (isBusyLabelVisible(node.getPayload()))
        {
            add(myBusyLabel);
            myComponentWidths += myBusyLabel.getWidth();

            myImageObserver.addPath(path);
        }
        else
        {
            myImageObserver.removePath(path);
        }
    }
}

package io.opensphere.core.util.swing.tree;

import java.awt.Color;
import java.awt.Component;
import java.awt.FontMetrics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractButton;
import javax.swing.JTree;
import javax.swing.tree.TreeCellRenderer;

import io.opensphere.core.util.collections.New;

/**
 * A decorator for TreeTableTreeCellRenderer to support drag-n-drop.
 */
public class HoverButtonTreeCellRenderer implements TreeCellRenderer
{
    /** The base renderer. */
    private final TreeTableTreeCellRenderer myRenderer;

    /** The buttons built from the button builders. */
    private final List<ForcePaintedButton> myButtons = New.list();

    /** The button to action listener map. */
    private final Map<ForcePaintedButton, ActionListener> myButtonToActionListenerMap = New.map();

    /**
     * The node which the mouse is over. This is used for handling events and
     * rendering which are specific to mouse position.
     */
    private TreeTableTreeNode myMouseOverNode;

    /**
     * Whether to return the last calculated renderer. Used to prevent a stack
     * overflow when calling tree.getRowBounds(row) in
     * getTreeCellRendererComponent().
     */
    private boolean myUsedCachedRenderer;

    /**
     * The constructor.
     *
     * @param renderer the base renderer
     */
    public HoverButtonTreeCellRenderer(TreeTableTreeCellRenderer renderer)
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
        if (myUsedCachedRenderer)
        {
            return myRenderer.getPanel();
        }

        if (tree instanceof ListCheckBoxTree && value instanceof TreeTableTreeNode)
        {
            int[] selRow = ((ListCheckBoxTree)tree).getMouseOverRows();
            if (selRow.length == 1 && selRow[0] == row)
            {
                myMouseOverNode = (TreeTableTreeNode)value;

                // Set button visibility
                for (ForcePaintedButton button : myButtons)
                {
                    if (button.getStateUpdater() != null)
                    {
                        button.getStateUpdater().update(button, myMouseOverNode);
                    }
                }

                // This row bounds is the tree node's renderable area in the
                // coordinates of the tree.
                myUsedCachedRenderer = true;
                Rectangle rowBounds = tree.getRowBounds(row);
                myUsedCachedRenderer = false;

                // Add the buttons to the panel
                int firstButtonOffset = addButtons(rowBounds);

                // Chop off the text to fit the buttons
                if (firstButtonOffset > 0 && rowBounds != null)
                {
                    String labelText = abbreviateText(myMouseOverNode.getPayload().getButton(), firstButtonOffset, rowBounds);
                    if (labelText != null)
                    {
                        myRenderer.getLabel().setText(labelText);
                    }
                }
            }
        }
        return null;
    }

    /**
     * Sets the button builders.
     *
     * @param buttonBuilders the button builders
     */
    public void setButtonBuilders(Collection<CustomTreeTableModelButtonBuilder> buttonBuilders)
    {
        myButtons.clear();
        for (CustomTreeTableModelButtonBuilder builder : buttonBuilders)
        {
            ForcePaintedButton button = new ForcePaintedButton(builder.getDefaultIcon(), builder.getRolloverIcon(),
                    builder.getPressedIcon(), builder.getSelectedIcon(), builder.getUpdater());
            button.setActionCommand(builder.getActionCommand());
            button.setName(builder.getButtonName());
            button.setOpaque(false);
            button.setBackground(new Color(0, 0, 0, 0));
            button.setBorder(null);
            button.addActionListener(e ->
            {
                ActionListener listener = myButtonToActionListenerMap.get(e.getSource());
                if (listener != null)
                {
                    ForcePaintedButton button1 = (ForcePaintedButton)e.getSource();
                    listener.actionPerformed(new ActionEvent(myMouseOverNode, e.getID(), button1.getActionCommand(),
                            e.getWhen(), e.getModifiers()));
                }
            });
            myButtons.add(button);
            myButtonToActionListenerMap.put(button, builder.getActionListener());
        }
    }

    /**
     * Check text bounds and truncate the text string if its width is greater
     * than the tree width.
     *
     * @param button the button
     * @param hoverButtonsWidth the hover buttons width
     * @param rowBounds the row bounds
     * @return the string
     */
    private String abbreviateText(AbstractButton button, int hoverButtonsWidth, Rectangle rowBounds)
    {
        String abbrieviatedTextString = null;

        FontMetrics fm = myRenderer.getLabel().getFontMetrics(myRenderer.getLabel().getFont());
        int availableTextWidth = rowBounds.width - myRenderer.getAddedComponentWidth() - hoverButtonsWidth;

        String text = button.getText();
        int textWidth = fm.stringWidth(text);
        int textLength = text.length() - 1;
        while (textWidth > availableTextWidth && textLength >= 4)
        {
            text = org.apache.commons.lang3.StringUtils.abbreviate(text, textLength);
            textWidth = fm.stringWidth(text);
            textLength--;
            abbrieviatedTextString = text;
        }

        return abbrieviatedTextString;
    }

    /**
     * Adds buttons to the panel.
     *
     * @param rowBounds the row bounds
     * @return the total buttons width
     */
    private int addButtons(Rectangle rowBounds)
    {
        int firstButtonOffset = 0;
        for (int i = 0; i < myButtons.size(); i++)
        {
            ForcePaintedButton button = myButtons.get(i);

            if (!button.isHidden())
            {
                // Add it to the panel
                myRenderer.getPanel().add(button);

                int offsetFromRight = 0;
                for (ForcePaintedButton nextButton : myButtons.subList(i, myButtons.size()))
                {
                    if (!nextButton.isHidden())
                    {
                        offsetFromRight += nextButton.getWidth();
                    }
                }

                // Set the button location
                if (rowBounds != null)
                {
                    int x = rowBounds.width - offsetFromRight;
                    button.setLocation(x, 0);
                }

                // Get the offset of the first button
                if (firstButtonOffset == 0)
                {
                    firstButtonOffset = offsetFromRight;
                }
            }
        }
        return firstButtonOffset;
    }
}

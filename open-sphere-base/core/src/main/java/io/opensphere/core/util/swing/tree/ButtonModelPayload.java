package io.opensphere.core.util.swing.tree;

import javax.swing.AbstractButton;

/** A cell payload backed by a button for maintaining a boolean state. */
public class ButtonModelPayload
{
    /** The button which represents the boolean stated of this payload. */
    private final AbstractButton myButton;

    /** The data for the cell. */
    private final Object myPayloadData;

    /** Tool tip to show when the mouse is over this node. */
    private String myTooltip;

    /** The show check box. */
    private final boolean myShowCheckBox;

    /**
     * Constructor.
     *
     * @param payloadData The data for the cell.
     * @param label The text for the button.
     * @param tooltip Tool tip to show when the mouse is over this node.
     */
    public ButtonModelPayload(Object payloadData, String label, String tooltip)
    {
        this(payloadData, label, tooltip, true);
    }

    /**
     * Constructor.
     *
     * @param payloadData The data for the cell.
     * @param label The text for the button.
     * @param tooltip Tool tip to show when the mouse is over this node.
     * @param showCheckBox the show check box
     */
    public ButtonModelPayload(Object payloadData, String label, String tooltip, boolean showCheckBox)
    {
        myPayloadData = payloadData;
        myButton = new ButtonModelPayloadJCheckBox(label, payloadData);
        myButton.setSelected(false);
        myTooltip = tooltip;
        myShowCheckBox = showCheckBox;
    }

    /**
     * Get the button.
     *
     * @return the button
     */
    public AbstractButton getButton()
    {
        return myButton;
    }

    /**
     * Get the payloadData.
     *
     * @return the payloadData
     */
    public Object getPayloadData()
    {
        return myPayloadData;
    }

    /**
     * Get the tooltip.
     *
     * @return the tooltip
     */
    public String getTooltip()
    {
        return myTooltip;
    }

    /**
     * Checks if is show check box.
     *
     * @return true, if is show check box
     */
    public boolean isShowCheckBox()
    {
        return myShowCheckBox;
    }

    /**
     * Sets the label.
     *
     * @param label the new label
     */
    public void setLabel(String label)
    {
        myButton.setText(label);
    }

    /**
     * Sets the tootip.
     *
     * @param tooltip the new tootip
     */
    public void setTootip(String tooltip)
    {
        myTooltip = tooltip;
    }
}

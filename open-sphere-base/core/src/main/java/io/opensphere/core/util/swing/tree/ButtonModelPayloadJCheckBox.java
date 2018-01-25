package io.opensphere.core.util.swing.tree;

import javax.swing.JCheckBox;

/**
 * The Class ButtonModelPayloadJCheckBox.
 */
@SuppressWarnings("serial")
public class ButtonModelPayloadJCheckBox extends JCheckBox
{
    /** The payload data. */
    private final transient Object myPayloadData;

    /**
     * Instantiates a new button model payload j check box.
     *
     * @param label the label
     * @param payloadData the payload data
     */
    public ButtonModelPayloadJCheckBox(String label, Object payloadData)
    {
        super(label);
        myPayloadData = payloadData;
    }

    /**
     * Gets the payload data.
     *
     * @return the payload data
     */
    public Object getPayloadData()
    {
        return myPayloadData;
    }
}

package io.opensphere.core.util.swing;

import javax.swing.JLabel;
import javax.swing.JSeparator;

/**
 * A named separator component.
 */
public class NamedSeparator extends GridBagPanel
{
    /** The serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The label. */
    private final JLabel myLabel;

    /**
     * Constructor.
     *
     * @param text the text
     */
    public NamedSeparator(String text)
    {
        super();
        myLabel = new JLabel(text);
        add(myLabel);
        fillHorizontal().setInsets(0, 4, 0, 0).add(new JSeparator());
    }

    /**
     * Gets the label.
     *
     * @return the label
     */
    public JLabel getLabel()
    {
        return myLabel;
    }
}

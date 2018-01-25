package io.opensphere.laf.dark;

import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;

/**
 * OpenSphere Dark Look and Feel for formatted text fields.
 */
public class OSDarkLAFFormattedTextFieldUI extends OSDarkLAFTextFieldUI
{
    /**
     * Utility method used to create the ComponentUI.
     *
     * @param pComponent the component for which the UI will be created.
     * @return a new ComponentUI created for the supplied component.
     */
    public static ComponentUI createUI(JComponent pComponent)
    {
        return new OSDarkLAFFormattedTextFieldUI(pComponent);
    }

    /**
     * Creates a new UI for the supplied component.
     *
     * @param pComponent the component for which the UI will be created.
     */
    public OSDarkLAFFormattedTextFieldUI(JComponent pComponent)
    {
        super(pComponent);
    }
}

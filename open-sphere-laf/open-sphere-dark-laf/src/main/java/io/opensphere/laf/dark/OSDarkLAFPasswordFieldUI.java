package io.opensphere.laf.dark;

import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;
import javax.swing.text.Element;
import javax.swing.text.View;

public class OSDarkLAFPasswordFieldUI extends OSDarkLAFTextFieldUI
{
    /**
     * Utility method used to create the ComponentUI.
     *
     * @param pComponent the component for which the UI will be created.
     * @return a new ComponentUI created for the supplied component.
     */
    public static ComponentUI createUI(JComponent pComponent)
    {
        return new OSDarkLAFPasswordFieldUI(pComponent);
    }

    public OSDarkLAFPasswordFieldUI(JComponent pComponent)
    {
        super(pComponent);
    }

    @Override
    public View create(Element el)
    {
        return new OSDarkLAFPasswordView(el);
    }

    @Override
    protected String getPropertyPrefix()
    {
        return "PasswordField";
    }
}

package io.opensphere.core.util.javafx.input.view;

import javafx.scene.AccessibleAttribute;
import javafx.scene.control.TextField;

/**
 * A field used to fake-out the focus mechanism.
 */
public class FakeFocusTextField extends TextField
{
    /**
     * {@inheritDoc}
     *
     * @see javafx.scene.Node#requestFocus()
     */
    @Override
    public void requestFocus()
    {
        if (getParent() != null)
        {
            getParent().requestFocus();
        }
    }

    /**
     * Sets focus for the field.
     *
     * @param b the focus state of the field
     */
    public void setFakeFocus(boolean b)
    {
        setFocused(b);
    }

    /**
     * {@inheritDoc}
     *
     * @see javafx.scene.control.TextInputControl#queryAccessibleAttribute(javafx.scene.AccessibleAttribute,
     *      java.lang.Object[])
     */
    @Override
    public Object queryAccessibleAttribute(AccessibleAttribute attribute, Object... parameters)
    {
        switch (attribute)
        {
            case FOCUS_ITEM:
                /* Internally comboBox reassign its focus the text field. For
                 * the accessibility perspective it is more meaningful if the
                 * focus stays with the comboBox control. */
                return getParent();
            default:
                return super.queryAccessibleAttribute(attribute, parameters);
        }
    }
}

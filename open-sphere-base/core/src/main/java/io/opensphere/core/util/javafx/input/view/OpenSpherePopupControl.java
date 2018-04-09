package io.opensphere.core.util.javafx.input.view;

import javafx.css.Styleable;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.PopupControl;

/**
 * A popup control extended to accept the root of the popup, and the content of
 * the popup.
 *
 * @param <T> the content type of the control from which the popup is launched.
 */
public class OpenSpherePopupControl<T extends Control> extends PopupControl
{
    /**
     * The control from which the popup is launched.
     */
    private final T mySkinnable;

    /**
     * Creates the popup control.
     *
     * @param skinnable the control from which the popup is launched.
     * @param content the content to be shown as part of the popup.
     */
    public OpenSpherePopupControl(T skinnable, Node content)
    {
        mySkinnable = skinnable;
        setSkin(new PopupSkin<>(skinnable, content));
    }

    /**
     * {@inheritDoc}
     *
     * @see javafx.scene.control.PopupControl#getStyleableParent()
     */
    @Override
    public Styleable getStyleableParent()
    {
        return mySkinnable;
    }
}

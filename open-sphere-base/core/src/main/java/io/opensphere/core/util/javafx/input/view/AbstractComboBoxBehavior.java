package io.opensphere.core.util.javafx.input.view;

import static javafx.scene.input.KeyCode.ALT;
import static javafx.scene.input.KeyCode.DOWN;
import static javafx.scene.input.KeyCode.ESCAPE;
import static javafx.scene.input.KeyCode.F10;
import static javafx.scene.input.KeyCode.F4;
import static javafx.scene.input.KeyCode.UP;

import io.opensphere.core.util.javafx.input.view.behavior.AbstractBehavior;
import javafx.event.EventTarget;
import javafx.scene.Node;
import javafx.scene.control.ComboBoxBase;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

/**
 * An extension of the {@link AbstractBehavior} class, in which combo-box specific behaviors are added.
 *
 * @param <T> The data type contained within the combo box to which the behavior is bound.
 */
public class AbstractComboBoxBehavior<T> extends AbstractBehavior<ComboBoxBase<T>>
{
    /**
     * A flag used to keep track of when the mouse cursor is over the button.
     */
    private boolean myMouseInsideButton;

    /**
     * A flag used to determine if the popup should be shown on mouse release (it should not be shown in some cases).
     */
    private boolean myShowPopupOnMouseRelease = true;

    /**
     * Creates a new behavior bound to the supplied combo box.
     *
     * @param pComboBox the combo box to which to bind the behavior.
     */
    public AbstractComboBoxBehavior(final ComboBoxBase<T> pComboBox)
    {
        super(pComboBox);

        bind(F4, "togglePopup");
        bind(UP, "togglePopup", ALT);
        bind(DOWN, "togglePopup", ALT);

        bind(ESCAPE, "Cancel");
        bind(F10, "ToParent");
    }

    /**
     * Arms the ComboBox based on the supplied event. The event must be valid for the combo box to arm (a valid event is defined
     * as the primary mouse button is the only button that is clicked, and no modifier keys were pressed when the click event
     * occurred). An armed ComboBox will show a popup list on the next expected UI gesture.
     *
     * @param pEvent the event triggered by the mouse click.
     */
    protected void arm(MouseEvent pEvent)
    {
        boolean valid = (pEvent.getButton() == MouseButton.PRIMARY
                && !(pEvent.isMiddleButtonDown() || pEvent.isSecondaryButtonDown() || pEvent.isShiftDown()
                        || pEvent.isControlDown() || pEvent.isAltDown() || pEvent.isMetaDown()));

        if (!getControl().isArmed() && valid)
        {
            getControl().arm();
        }
    }

    /**
     * Arms the ComboBox. An armed ComboBox will show a popup list on the next expected UI gesture.
     */
    public void arm()
    {
        if (getControl().isPressed())
        {
            getControl().arm();
        }
    }

    /**
     * Disarms the ComboBox. See {@link #arm()}.
     */
    public void disarm()
    {
        if (getControl().isArmed())
        {
            getControl().disarm();
        }
    }

    /**
     * Shows the bound combo box.
     */
    public void show()
    {
        if (!getControl().isShowing())
        {
            getControl().requestFocus();
            getControl().show();
        }
    }

    /**
     * Hides the bound combo box.
     */
    public void hide()
    {
        if (getControl().isShowing())
        {
            getControl().hide();
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.util.javafx.input.view.behavior.AbstractBehavior#mouseEntered(javafx.scene.input.MouseEvent)
     */
    @Override
    public void mouseEntered(MouseEvent pEvent)
    {
        super.mouseEntered(pEvent);

        if (!getControl().isEditable())
        {
            myMouseInsideButton = true;
        }
        else
        {
            // This is strongly tied to ComboBoxBaseSkin
            final EventTarget target = pEvent.getTarget();
            myMouseInsideButton = (target instanceof Node && "arrow-button".equals(((Node)target).getId()));
        }
        arm();
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.util.javafx.input.view.behavior.AbstractBehavior#mouseExited(javafx.scene.input.MouseEvent)
     */
    @Override
    public void mouseExited(MouseEvent pEvent)
    {
        super.mouseExited(pEvent);
        myMouseInsideButton = false;
        disarm();
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.util.javafx.input.view.behavior.AbstractBehavior#mousePressed(javafx.scene.input.MouseEvent)
     */
    @Override
    public void mousePressed(MouseEvent pEvent)
    {
        super.mousePressed(pEvent);
        arm(pEvent);
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.util.javafx.input.view.behavior.AbstractBehavior#actionPerformed(java.lang.String)
     */
    @Override
    public void actionPerformed(String pAction)
    {
        switch (pAction)
        {
            case "togglePopup":
                if (getControl().isShowing())
                {
                    hide();
                }
                else
                {
                    show();
                }
                break;
            default:
                super.actionPerformed(pAction);
                break;
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.util.javafx.input.view.behavior.AbstractBehavior#mouseReleased(javafx.scene.input.MouseEvent)
     */
    @Override
    public void mouseReleased(MouseEvent pEvent)
    {
        super.mouseReleased(pEvent);

        disarm();

        if (myShowPopupOnMouseRelease)
        {
            show();
        }
        else
        {
            myShowPopupOnMouseRelease = true;
            hide();
        }
    }

    /**
     * An event handler method used to respond to an auto-hide event.
     */
    public void onAutoHide()
    {
        hide();
        if (myMouseInsideButton)
        {
            myShowPopupOnMouseRelease = !myShowPopupOnMouseRelease;
        }
        else
        {
            myShowPopupOnMouseRelease = true;
        }
    }
}

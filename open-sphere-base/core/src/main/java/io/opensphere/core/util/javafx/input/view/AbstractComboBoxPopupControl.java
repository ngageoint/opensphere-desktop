package io.opensphere.core.util.javafx.input.view;

import javafx.beans.Observable;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.AccessibleAttribute;
import javafx.scene.Node;
import javafx.scene.control.ComboBoxBase;
import javafx.scene.control.PopupControl;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.stage.WindowEvent;
import javafx.util.StringConverter;

/**
 * An abstract base class used to provide control to popups for combo boxes.
 * Instances are bound to control types.
 *
 * @param <T> the control type to which the implementation is bound.
 */
@SuppressWarnings("PMD.GodClass")
public abstract class AbstractComboBoxPopupControl<T> extends AbstractComboBoxSkin<T>
{
    /**
     * The control in which the popup is managed.
     */
    private PopupControl myPopup;

    /**
     * The combo box to which the control is bound.
     */
    private final ComboBoxBase<T> myParentComboBox;

    /**
     * The editor displayed to the user when the popup is not shown.
     */
    private TextField myTextField;

    /**
     * The initial value from the text field.
     */
    private String myInitialTextFieldValue;

    /**
     * Creates a new control in which a skinned popup is managed.
     *
     * @param pParentComboBox The combo box to which the control is bound.
     * @param pBehavior the behavior support to add to the skin.
     */
    @SuppressWarnings("PMD.ConstructorCallsOverridableMethod")
    public AbstractComboBoxPopupControl(ComboBoxBase<T> pParentComboBox, final AbstractComboBoxBehavior<T> pBehavior)
    {
        super(pParentComboBox, pBehavior);
        this.myParentComboBox = pParentComboBox;

        // editable input node
        this.myTextField = getEditor() != null ? getEditableInputNode() : null;

        // Fix for RT-29565. Without this the textField does not have a correct
        // preferred width at startup, as it is not part of
        // the scene graph (and therefore has no preferred width until after the
        // first measurements have been taken).
        if (this.myTextField != null)
        {
            getChildren().add(myTextField);
        }

        myParentComboBox.addEventFilter(KeyEvent.ANY, ke -> filterKeyEvent(ke));
    }

    /**
     * Filters the supplied key event, narrowing the set of processed keys to
     * only those that the combo box is interested in.
     *
     * @param pKeyEvent the event to process.
     */
    protected void filterKeyEvent(KeyEvent pKeyEvent)
    {
        if (myTextField == null || getEditor() == null)
        {
            handleKeyEvent(pKeyEvent, false);
        }
        else if (!pKeyEvent.getTarget().equals(myTextField))
        {
            // This prevents a stack overflow from our re-broadcasting of the
            // event to the text field that occurs in the final
            // else statement of the conditions below.
            switch (pKeyEvent.getCode())
            {
                case ESCAPE:
                case F10:
                    // Allow to bubble up.
                    break;
                case ENTER:
                    handleKeyEvent(pKeyEvent, true);
                    break;
                default:
                    // This forwards the event down into the TextField when the
                    // key event is actually received by the ComboBox.
                    myTextField.fireEvent(pKeyEvent.copyFor(myTextField, myTextField));
                    pKeyEvent.consume();
                    break;
            }
        }
    }

    /**
     * An event handler method in which key events are processed and distributed
     * to their target locations.
     *
     * @param pKeyEvent the event to handle.
     * @param pConsumeEvent when true, the event is consumed after processing.
     */
    protected void handleKeyEvent(KeyEvent pKeyEvent, boolean pConsumeEvent)
    {
        // When the user hits the enter or F4 keys, respond before ever giving
        // the event to the TextField.
        if (pKeyEvent.getCode() == KeyCode.ENTER)
        {
            setTextFromTextFieldIntoComboBoxValue();

            if (pConsumeEvent && myParentComboBox.getOnAction() != null)
            {
                pKeyEvent.consume();
            }
            else
            {
                forwardToParent(pKeyEvent);
            }
        }
        else if (pKeyEvent.getCode() == KeyCode.F4)
        {
            if (pKeyEvent.getEventType() == KeyEvent.KEY_RELEASED)
            {
                if (myParentComboBox.isShowing())
                {
                    myParentComboBox.hide();
                }
                else
                {
                    myParentComboBox.show();
                }
            }
            pKeyEvent.consume();
        }
    }

    /**
     * Sets the text value from the editable text field into parent combo box.
     */
    protected void setTextFromTextFieldIntoComboBoxValue()
    {
        if (getEditor() != null)
        {
            StringConverter<T> c = getConverter();
            if (c != null)
            {
                T oldValue = myParentComboBox.getValue();
                T value = oldValue;
                String text = myTextField.getText();

                // conditional check here added due to RT-28245
                if (oldValue == null && (text == null || text.isEmpty()))
                {
                    value = null;
                }
                else
                {
                    value = c.fromString(text);
                }

                if ((value != null || oldValue != null) && (value == null || !value.equals(oldValue)))
                {
                    // no point updating values needlessly if they are the same
                    myParentComboBox.setValue(value);
                }

                updateDisplayNode();
            }
        }
    }

    /**
     * Update the combo box's display node with the selected content.
     */
    protected void updateDisplayNode()
    {
        if (myTextField != null && getEditor() != null)
        {
            T value = myParentComboBox.getValue();
            StringConverter<T> c = getConverter();

            if (myInitialTextFieldValue != null && !myInitialTextFieldValue.isEmpty())
            {
                myTextField.setText(myInitialTextFieldValue);
                myInitialTextFieldValue = null;
            }
            else
            {
                String stringValue = c.toString(value);
                if (value == null || stringValue == null)
                {
                    myTextField.setText("");
                }
                else if (!stringValue.equals(myTextField.getText()))
                {
                    myTextField.setText(stringValue);
                }
            }
        }
    }

    /**
     * Sends the supplied key event to the combo-box's parent for further
     * processing.
     *
     * @param pEvent the event to delegate to the parent for further processing.
     */
    protected void forwardToParent(KeyEvent pEvent)
    {
        if (myParentComboBox.getParent() != null)
        {
            myParentComboBox.getParent().fireEvent(pEvent);
        }
    }

    /**
     * This method should return the Node that will be displayed when the user
     * clicks on the ComboBox 'button' area.
     *
     * @return the node that will be displayed when the user clicks on the combo
     *         box button area.
     */
    protected abstract Node getPopupContent();

    /**
     * Gets the value of the {@link #myPopup} field.
     *
     * @return the value stored in the {@link #myPopup} field.
     */
    public PopupControl getPopup()
    {
        if (myPopup == null)
        {
            Node content = getPopupContent();
            if (content == null)
            {
                throw new IllegalStateException("Popup node is null");
            }

            myPopup = new OpenSpherePopupControl<>(myParentComboBox, content);
            myPopup.setConsumeAutoHidingEvents(false);
            myPopup.setAutoHide(true);
            myPopup.setAutoFix(true);
            myPopup.setHideOnEscape(true);

            myPopup.setOnAutoHide(e -> getBehavior().onAutoHide());
            // RT-18529: We listen to mouse input that is received by the popup
            // but that is not consumed, and assume that this is
            // due to the mouse clicking outside of the node, but in areas such
            // as the drop shadow.
            myPopup.addEventHandler(MouseEvent.MOUSE_CLICKED, t -> getBehavior().onAutoHide());
            // Make sure the accessibility focus returns to the combo box after
            // the window closes (multi-line shennanigans are in
            // place to keep check style happy):
            AccessibleAttribute focusNode = AccessibleAttribute.FOCUS_NODE;
            EventHandler<? super WindowEvent> eventHandler = t -> getSkinnable().notifyAccessibleAttributeChanged(focusNode);
            myPopup.addEventHandler(WindowEvent.WINDOW_HIDDEN, eventHandler);

            // RT-36966 - if skinnable's scene becomes null, ensure popup is
            // closed
            getSkinnable().sceneProperty().addListener(o -> handleSceneInvalidation(o));
        }
        return myPopup;
    }

    /**
     * An event handler used to react to a scene becoming invalid.
     *
     * @param pObservable the observable fired by the scene invalidation
     *            operation.
     */
    protected void handleSceneInvalidation(Observable pObservable)
    {
        if (((ObservableValue<?>)pObservable).getValue() == null)
        {
            hide();
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.util.javafx.input.view.AbstractComboBoxSkin#show()
     */
    @Override
    public void show()
    {
        if (getSkinnable() == null)
        {
            throw new IllegalStateException("ComboBox is null");
        }

        Node content = getPopupContent();
        if (content == null)
        {
            throw new IllegalStateException("Popup node is null");
        }

        if (!getPopup().isShowing())
        {
            positionAndShowPopup();
        }
    }

    /**
     * Calculate the target location of the popup, and display it at that
     * location.
     */
    protected void positionAndShowPopup()
    {
        final PopupControl popup = getPopup();
        popup.getScene().setNodeOrientation(getSkinnable().getEffectiveNodeOrientation());

        final Node popupContent = getPopupContent();
        sizePopup();

        Point2D p = PositioningUtil.pointRelativeTo(getSkinnable(), getPopupContent().getLayoutBounds().getWidth(),
                getPopupContent().getLayoutBounds().getHeight());

        final ComboBoxBase<T> comboBoxBase = getSkinnable();
        popup.show(comboBoxBase.getScene().getWindow(), snapPosition(p.getX()), snapPosition(p.getY()));

        popupContent.requestFocus();

        sizePopup();
    }

    /**
     * Calculate the desired size of the popup, and apply that size to the
     * popup's content area.
     */
    protected void sizePopup()
    {
        final Node popupContent = getPopupContent();

        if (popupContent instanceof Region)
        {
            // snap to pixel
            final Region r = (Region)popupContent;

            // 0 is used here for the width due to RT-46097
            double prefHeight = snapSize(r.prefHeight(0));
            double minHeight = snapSize(r.minHeight(0));
            double maxHeight = snapSize(r.maxHeight(0));
            double h = snapSize(Math.min(Math.max(prefHeight, minHeight), Math.max(minHeight, maxHeight)));

            double prefWidth = snapSize(r.prefWidth(h));
            double minWidth = snapSize(r.minWidth(h));
            double maxWidth = snapSize(r.maxWidth(h));
            double w = snapSize(Math.min(Math.max(prefWidth, minWidth), Math.max(minWidth, maxWidth)));

            popupContent.resize(w, h);
        }
        else
        {
            popupContent.autosize();
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.util.javafx.input.view.AbstractComboBoxSkin#hide()
     */
    @Override
    public void hide()
    {
        if (myPopup != null && myPopup.isShowing())
        {
            myPopup.hide();
        }
    }

    /**
     * Subclasses are responsible for getting the editor.
     *
     * Note: ComboBoxListViewSkin should return null if editable is false, even
     * if the ComboBox does have an editor set.
     *
     * @return the component used as an editor.
     */
    protected abstract TextField getEditor();

    /**
     * Subclasses are responsible for getting the converter.
     *
     * @return the converter instance in which the type 'T' is converted to and
     *         from {@link String} instances.
     */
    protected abstract StringConverter<T> getConverter();

    /**
     * Gets the editable input node for the combo box.
     *
     * @return the editable input node for the combo box.
     */
    protected TextField getEditableInputNode()
    {
        if (myTextField == null && getEditor() != null)
        {
            myTextField = getEditor();
            myTextField.setFocusTraversable(false);
            myTextField.promptTextProperty().bind(myParentComboBox.promptTextProperty());
            myTextField.tooltipProperty().bind(myParentComboBox.tooltipProperty());

            myInitialTextFieldValue = myTextField.getText();
        }

        return myTextField;
    }
}

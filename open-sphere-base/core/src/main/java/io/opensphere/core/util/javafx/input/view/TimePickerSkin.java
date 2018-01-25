package io.opensphere.core.util.javafx.input.view;

import java.time.LocalTime;

import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;

import org.apache.commons.lang.StringUtils;

/**
 * A skin used for the time picker combo box.
 */
public class TimePickerSkin extends AbstractComboBoxPopupControl<LocalTime>
{
    /**
     * The picker to which this skin is bound.
     */
    private final TimePicker myPicker;

    /**
     * The text field in which the date / time is displayed.
     */
    private TextField myDisplayNode;

    /**
     * The content panel used as a popup.
     */
    private TimePickerContent myPickerContent;

    /**
     * Creates a new time picker skin, bound to the supplied picker.
     *
     * @param pPicker the time picker to which the skin is bound.
     */
    public TimePickerSkin(final TimePicker pPicker)
    {
        super(pPicker, new TimePickerBehavior(pPicker));
        myPicker = pPicker;

        registerChangeListener(myPicker.converterProperty(), "CONVERTER");
        registerChangeListener(myPicker.valueProperty(), "VALUE");
        registerChangeListener(myPicker.valueProperty(), "EDITOR");
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.util.javafx.input.view.AbstractBehaviorSkin#handleControlPropertyChanged(java.lang.String)
     */
    @Override
    protected void handleControlPropertyChanged(String pPropertyName)
    {
        if ("CONVERTER".equals(pPropertyName))
        {
            updateDisplayNode();
        }
        else if (StringUtils.equals(pPropertyName, "SHOWING"))
        {
            if (myPicker.isShowing())
            {
                if (myPickerContent != null)
                {
                    LocalTime time = myPicker.getValue();
                    myPickerContent.populateValue(time);
                }
                show();
            }
            else
            {
                hide();
            }
        }
        else if (StringUtils.equals(pPropertyName, "VALUE"))
        {
            updateDisplayNode();
            myPicker.fireEvent(new ActionEvent());
        }
        else
        {
            super.handleControlPropertyChanged(pPropertyName);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.util.javafx.input.view.AbstractComboBoxPopupControl#getPopupContent()
     */
    @Override
    protected Node getPopupContent()
    {
        if (myPickerContent == null)
        {
            myPickerContent = new TimePickerContent(myPicker);
        }

        return myPickerContent;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.util.javafx.input.view.AbstractComboBoxPopupControl#show()
     */
    @Override
    public void show()
    {
        super.show();
        myPickerContent.clearFocus();
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.util.javafx.input.view.AbstractComboBoxPopupControl#getEditor()
     */
    @Override
    protected TextField getEditor()
    {
        // Use getSkinnable() here because this method is called from the super constructor before datePicker is initialized.
        return ((TimePicker)getSkinnable()).getEditor();
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.util.javafx.input.view.AbstractComboBoxPopupControl#getConverter()
     */
    @Override
    protected StringConverter<LocalTime> getConverter()
    {
        return ((TimePicker)getSkinnable()).getConverter();
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.util.javafx.input.view.AbstractComboBoxSkin#getDisplayNode()
     */
    @Override
    public Node getDisplayNode()
    {
        if (myDisplayNode == null)
        {
            myDisplayNode = getEditableInputNode();
            myDisplayNode.getStyleClass().add("time-picker-display-node");
            updateDisplayNode();
        }
        myDisplayNode.setEditable(myPicker.isEditable());

        return myDisplayNode;
    }

    // TODO this may not be needed:
    /**
     * Synchronizes the hidden state of the picker with the popup's state.
     */
    public void syncWithAutoUpdate()
    {
        if (!getPopup().isShowing() && myPicker.isShowing())
        {
            // Popup was dismissed. Maybe user clicked outside or typed ESCAPE. Make sure DateTimePicker button is in sync.
            myPicker.hide();
        }
    }
}

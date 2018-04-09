package io.opensphere.core.util.javafx.input.view;

import java.time.LocalDate;
import java.time.YearMonth;

import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;

/**
 * An implementation of the {@link AbstractComboBoxPopupControl} class for use
 * with the OpenSphere Date Picker.
 */
public class DatePickerSkin extends AbstractComboBoxPopupControl<LocalDate>
{
    /**
     * The picker to which this skin is bound.
     */
    private final DatePicker myDatePicker;

    /**
     * The text field in which the date / time is displayed.
     */
    private TextField myDisplayNode;

    /**
     * The content panel used as a popup.
     */
    private DatePickerContent myDatePickerContent;

    /**
     * Creates a new picker skin, bound to the supplied date / time picker.
     *
     * @param pDatePicker the date / time picker to which the skin is bound.
     */
    public DatePickerSkin(final DatePicker pDatePicker)
    {
        super(pDatePicker, new DatePickerBehavior(pDatePicker));

        this.myDatePicker = pDatePicker;

        // The "arrow" is actually a rectangular SVG icon resembling a calendar.
        // Round the size of the icon to whole integers to
        // get sharp edges.
        getIcon().paddingProperty().addListener(new PaddedInvalidationListener(getIcon()));

        registerChangeListener(pDatePicker.converterProperty(), "CONVERTER");
        registerChangeListener(pDatePicker.valueProperty(), "VALUE");
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.util.javafx.input.view.AbstractComboBoxPopupControl#getPopupContent()
     */
    @Override
    public Node getPopupContent()
    {
        if (myDatePickerContent == null)
        {
            myDatePickerContent = new DatePickerContent(myDatePicker);
        }

        return myDatePickerContent;
    }

    /**
     * {@inheritDoc}
     *
     * @see javafx.scene.control.SkinBase#computeMinWidth(double, double,
     *      double, double, double)
     */
    @Override
    protected double computeMinWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset)
    {
        return 50;
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
        myDatePickerContent.clearFocus();
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.util.javafx.input.view.AbstractBehaviorSkin#handleControlPropertyChanged(java.lang.String)
     */
    @Override
    protected void handleControlPropertyChanged(String p)
    {
        if ("CONVERTER".equals(p))
        {
            updateDisplayNode();
        }
        else if ("EDITOR".equals(p))
        {
            getEditableInputNode();
        }
        else if ("SHOWING".equals(p))
        {
            if (myDatePicker.isShowing())
            {
                if (myDatePickerContent != null)
                {
                    LocalDate date = myDatePicker.getValue();
                    myDatePickerContent.displayedYearMonthProperty().set((date != null) ? YearMonth.from(date) : YearMonth.now());
                    myDatePickerContent.updateValues();
                }
                show();
            }
            else
            {
                hide();
            }
        }
        else if ("VALUE".equals(p))
        {
            updateDisplayNode();
            if (myDatePickerContent != null)
            {
                LocalDate date = myDatePicker.getValue();
                myDatePickerContent.displayedYearMonthProperty().set((date != null) ? YearMonth.from(date) : YearMonth.now());
                myDatePickerContent.updateValues();
            }
            myDatePicker.fireEvent(new ActionEvent());
        }
        else
        {
            super.handleControlPropertyChanged(p);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.util.javafx.input.view.AbstractComboBoxPopupControl#getEditor()
     */
    @Override
    protected TextField getEditor()
    {
        // Use getSkinnable() here because this method is called from the super
        // constructor before datePicker is initialized.
        return ((DatePicker)getSkinnable()).getEditor();
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.util.javafx.input.view.AbstractComboBoxPopupControl#getConverter()
     */
    @Override
    protected StringConverter<LocalDate> getConverter()
    {
        return ((DatePicker)getSkinnable()).getConverter();
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
            myDisplayNode.getStyleClass().add("date-picker-display-node");
            updateDisplayNode();
        }
        myDisplayNode.setEditable(myDatePicker.isEditable());

        return myDisplayNode;
    }

    /**
     * Synchronizes the hidden state of the picker with the popup's state.
     */
    public void syncWithAutoUpdate()
    {
        if (!getPopup().isShowing() && myDatePicker.isShowing())
        {
            // Popup was dismissed. Maybe user clicked outside or typed ESCAPE.
            // Make sure DateTimePicker button is in sync.
            myDatePicker.hide();
        }
    }
}

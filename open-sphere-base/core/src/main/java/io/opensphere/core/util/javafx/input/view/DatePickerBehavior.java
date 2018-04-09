package io.opensphere.core.util.javafx.input.view;

import java.time.LocalDate;

/**
 * A behavior class used for the date time picker.
 */
public class DatePickerBehavior extends AbstractComboBoxBehavior<LocalDate>
{
    /**
     * Creates a new behavior for the supplied date / time picker.
     *
     * @param dateTimePicker the picker to which the behavior is bound.
     */
    public DatePickerBehavior(final DatePicker dateTimePicker)
    {
        super(dateTimePicker);
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.util.javafx.input.view.AbstractComboBoxBehavior#onAutoHide()
     */
    @Override
    public void onAutoHide()
    {
        // when we click on some non-interactive part of the calendar - we do
        // not want to hide.
        DatePicker datePicker = (DatePicker)getControl();
        DatePickerSkin cpSkin = (DatePickerSkin)datePicker.getSkin();
        cpSkin.syncWithAutoUpdate();
        // if the DateTimePicker is no longer showing, then invoke the super
        // method to keep its show/hide state in sync.
        if (!datePicker.isShowing())
        {
            super.onAutoHide();
        }
    }
}

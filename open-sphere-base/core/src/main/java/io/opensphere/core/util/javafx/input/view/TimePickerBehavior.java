package io.opensphere.core.util.javafx.input.view;

import java.time.LocalTime;

import javafx.scene.control.ComboBoxBase;

/**
 * A behavior implementation bound to a combo box that supports selection of {@link LocalTime} values.
 */
public class TimePickerBehavior extends AbstractComboBoxBehavior<LocalTime>
{
    /**
     * Creates a new behavior, bound to the supplied combo box.
     *
     * @param pComboBox the combo box to which the behavior is bound.
     */
    public TimePickerBehavior(ComboBoxBase<LocalTime> pComboBox)
    {
        super(pComboBox);
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.util.javafx.input.view.AbstractComboBoxBehavior#onAutoHide()
     */
    @Override
    public void onAutoHide()
    {
        // when we click on some non-interactive part of the calendar - we do not want to hide.
        TimePicker picker = (TimePicker)getControl();
        TimePickerSkin cpSkin = (TimePickerSkin)picker.getSkin();
        cpSkin.syncWithAutoUpdate();
        // if the TimePicker is no longer showing, then invoke the super method to keep its show/hide state in sync.
        if (!picker.isShowing())
        {
            super.onAutoHide();
        }
    }
}

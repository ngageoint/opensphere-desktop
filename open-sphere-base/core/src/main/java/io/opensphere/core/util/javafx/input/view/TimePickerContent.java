package io.opensphere.core.util.javafx.input.view;

import java.time.LocalTime;

import io.opensphere.core.util.AwesomeIcon;
import io.opensphere.core.util.fx.FxIcons;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * The content section of the time picker, in which the controls are rendered.
 */
public class TimePickerContent extends VBox
{
    /**
     * The button used to roll back a month.
     */
    private final BoundNumericSpinner myHourSpinner;

    /**
     * The button used to roll back a month.
     */
    private final BoundNumericSpinner myMinuteSpinner;

    /**
     * The button used to roll back a month.
     */
    private final BoundNumericSpinner mySecondSpinner;

    /**
     * The button used to accept the time entered in the picker, also hides the
     * popup.
     */
    private final Button myOkayButton;

    /**
     * The button used to set the picker's value to the current local time.
     */
    private final Button myNowButton;

    /**
     * The button used to reject the time entered in the picker, also hides the
     * popup.
     */
    private final Button myCancelButton;

    /**
     * The picker to which the content section is bound.
     */
    private final TimePicker myTimePicker;

    /**
     * Creates a new content area, bound to the supplied time picker.
     *
     * @param pTimePicker the picker to which the content area is bound.
     */
    @SuppressWarnings("PMD.ConstructorCallsOverridableMethod")
    public TimePickerContent(TimePicker pTimePicker)
    {
        myTimePicker = pTimePicker;
        getStyleClass().add("time-picker-popup");

        HBox hbox = new HBox();
        hbox.setAlignment(Pos.CENTER);
        myHourSpinner = new BoundNumericSpinner(24);
        myMinuteSpinner = new BoundNumericSpinner(60);
        mySecondSpinner = new BoundNumericSpinner(60);

        hbox.getChildren().add(myHourSpinner);
        hbox.getChildren().add(new Label(":"));
        hbox.getChildren().add(myMinuteSpinner);
        hbox.getChildren().add(new Label(":"));
        hbox.getChildren().add(mySecondSpinner);

        HBox bar = new HBox();
        bar.setAlignment(Pos.CENTER);
        myOkayButton = FxIcons.createIconButton(AwesomeIcon.ICON_CHECK, "", 12, "ok-button");
        myOkayButton.setTooltip(new Tooltip("Okay"));

        myNowButton = FxIcons.createIconButton(AwesomeIcon.ICON_FLASH, "", 12, "now-button");
        myNowButton.setTooltip(new Tooltip("Set to current time"));

        myCancelButton = FxIcons.createIconButton(AwesomeIcon.ICON_TIMES, "", 12, "cancel-button");
        myCancelButton.setTooltip(new Tooltip("Cancel"));

        Label spacer1 = new Label();
        Label spacer2 = new Label();

        HBox.setHgrow(spacer1, Priority.ALWAYS);
        HBox.setHgrow(spacer2, Priority.ALWAYS);

        bar.getChildren().addAll(myCancelButton, spacer1, myNowButton, spacer2, myOkayButton);

        myOkayButton.setOnAction(pEvent -> calculateValue());
        myNowButton.setOnAction(pEvent -> populateValue(LocalTime.now()));
        myCancelButton.setOnAction(pEvent -> myTimePicker.hide());

        getChildren().add(hbox);
        getChildren().add(bar);
    }

    /**
     * Calculates a time and populates the picker with the values extracted from
     * the components.
     */
    protected void calculateValue()
    {
        int hours = myHourSpinner.value().get();
        int minutes = myMinuteSpinner.value().get();
        int seconds = mySecondSpinner.value().get();

        String value = String.format("%1$02d:%2$02d:%3$02d", hours, minutes, seconds);
        LocalTime time = myTimePicker.getConverter().fromString(value);
        myTimePicker.setValue(time);
        myTimePicker.hide();
    }

    /**
     * Sets the value of the picker's components to the supplied time.
     *
     * @param pTime the time to which to set the picker's component values.
     */
    protected void populateValue(LocalTime pTime)
    {
        String value;
        if (pTime != null)
        {
            value = myTimePicker.getConverter().toString(pTime);
        }
        else
        {
            value = "00:00:00";
        }

        String[] tokens = value.split(":");

        if (tokens[2].contains("."))
        {
            String[] secondFieldTokens = tokens[2].split("\\.");
            tokens[2] = secondFieldTokens[0];
        }

        int hours = Integer.parseInt(tokens[0]);
        int minutes = Integer.parseInt(tokens[1]);
        int seconds = Integer.parseInt(tokens[2]);

        myHourSpinner.set(hours);
        myMinuteSpinner.set(minutes);
        mySecondSpinner.set(seconds);
    }

    /**
     * Clears the focus of the picker.
     */
    public void clearFocus()
    {
        // intentionally blank
    }
}

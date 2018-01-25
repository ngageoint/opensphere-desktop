package io.opensphere.core.util.javafx.input;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;

import javafx.collections.FXCollections;
import javafx.geometry.HPos;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import io.opensphere.core.Toolbox;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.util.Visitor;
import io.opensphere.core.util.javafx.input.view.CombinedDateTimePicker;

/**
 * A specialized {@link CompoundTitledControl} in which a preset combo box, and paired start and end date time pickers are
 * contained.
 */
public class DateTimeRangeInput extends CompoundTitledControl
{
    /**
     * The preset used when a user manually enters a date / time in either of the pickers.
     */
    private static final String CUSTOM_PRESET = "-- Custom --";

    /**
     * The control in which a preset may be selected.
     */
    private final IdentifiedControl<ComboBox<String>> myPresetControl;

    /**
     * The named control in which the start date / time is entered.
     */
    private final IdentifiedControl<CombinedDateTimePicker> myStartInput;

    /**
     * The named control in which the end date / time is entered.
     */
    private final IdentifiedControl<CombinedDateTimePicker> myEndInput;

    /**
     * The toolbox with which the time manager is referenced.
     */
    private final Toolbox myToolbox;

    /**
     * A flag used to determine if the preset selector caused the change, and if so, allows change handlers to not react to value
     * changes within the picker.
     */
    private boolean myPresetCausedChanged;

//    /**
//     * Creates a new {@link DateTimeRangeInput} using the supplied parameters.
//     *
//     * @param pTitle the display title of the component to be displayed on the form.
//     * @param pStartVariable the variable name to apply to the start date / time input field.
//     * @param pEndVariable the variable name to apply to the end date / time input field.
//     * @param pStartTitle the title to apply to the start date / time input field.
//     * @param pEndTitle the title to apply to the end date / time input field.
//     * @param pToolbox the toolbox through which the time manager is referenced.
//     */
//    @SuppressWarnings("PMD.ConstructorCallsOverridableMethod")
//    public DateTimeRangeInput(String pTitle, String pStartVariable, String pEndVariable, String pStartTitle, String pEndTitle,
//            Toolbox pToolbox)
//    {
//        super(pTitle, Orientation.VERTICAL, true);
//
//        myToolbox = pToolbox;
//
//        CombinedDateTimePicker start = new CombinedDateTimePicker();
//        CombinedDateTimePicker end = new CombinedDateTimePicker();
//        ComboBox<String> presetControl = new ComboBox<>(FXCollections.observableArrayList(CUSTOM_PRESET, "Timeline",
//                "Last 24 Hours", "Last 48 Hours", "Last 72 Hours", "Last 168 Hours (1 Week)", "Today", "Yesterday", "This Week",
//                "Last Week", "This Month", "Last 30 Days", "Last 60 Days", "Last 90 Days", "This Year"));
//        presetControl.valueProperty().addListener((pSource, pOld, pNew) -> presetSelected(pNew));
//
//        start.valueProperty().addListener((pSource, pOld, pNew) -> valueChanged());
//        end.valueProperty().addListener((pSource, pOld, pNew) -> valueChanged());
//
//        myStartInput = new IdentifiedControl<>(pStartVariable, pStartTitle, start);
//        myStartInput.setResultAccessorFunction(() -> DateTimeUtilities.generateISO8601DateString(start.valueProperty().get()));
//        myEndInput = new IdentifiedControl<>(pEndVariable, pEndTitle, end);
//        myEndInput.setResultAccessorFunction(() -> DateTimeUtilities.generateISO8601DateString(end.valueProperty().get()));
//        myPresetControl = new IdentifiedControl<>(null, "Time From", presetControl);
//
//        setControls(Orientation.VERTICAL, true, myPresetControl, myStartInput, myEndInput);
//
//        setNodeStyle("-fx-padding: 5;");
//        setStyle("-fx-border-width: 1; -fx-border-style: solid; -fx-border-radius: 3; -fx-border-color: -fx-outer-border;");
//
//        presetControl.setValue("Timeline");
//    }

    /**
     * Creates a new {@link DateTimeRangeInput} using the supplied parameters.
     *
     * @param pTitle the display title of the component to be displayed on the form.
     * @param pStartControl the identified control to use for the start input field.
     * @param pEndControl the identified control to use for the end input field.
     * @param pToolbox the toolbox through which the time manager is referenced.
     */
    @SuppressWarnings("PMD.ConstructorCallsOverridableMethod")
    public DateTimeRangeInput(String pTitle, IdentifiedControl<CombinedDateTimePicker> pStartControl,
            IdentifiedControl<CombinedDateTimePicker> pEndControl, Toolbox pToolbox)
    {
        super(pTitle, Orientation.VERTICAL, true);

        myToolbox = pToolbox;

        ComboBox<String> presetControl = new ComboBox<>(FXCollections.observableArrayList(CUSTOM_PRESET, "Timeline",
                "Last 24 Hours", "Last 48 Hours", "Last 72 Hours", "Last 168 Hours (1 Week)", "Today", "Yesterday", "This Week",
                "Last Week", "This Month", "Last 30 Days", "Last 60 Days", "Last 90 Days", "This Year"));
        presetControl.valueProperty().addListener((pSource, pOld, pNew) -> presetSelected(pNew));

        pStartControl.getControl().valueProperty().addListener((pSource, pOld, pNew) -> valueChanged());
        pEndControl.getControl().valueProperty().addListener((pSource, pOld, pNew) -> valueChanged());

        myStartInput = pStartControl;
        myEndInput = pEndControl;
        myPresetControl = new IdentifiedControl<>(null, "Time From", presetControl);

        setControls(Orientation.VERTICAL, true, myPresetControl, myStartInput, myEndInput);

        setNodeStyle("-fx-padding: 5;");
        setStyle("-fx-border-width: 1; -fx-border-style: solid; -fx-border-radius: 3; -fx-border-color: -fx-outer-border;");

        presetControl.setValue("Timeline");
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.util.javafx.input.CompoundTitledControl#createVerticalBox(boolean,
     *      io.opensphere.core.util.javafx.input.IdentifiedControl[])
     */
    @Override
    protected Node createVerticalBox(boolean pShowSubtitles, IdentifiedControl<?>... pControls)
    {
        if (pShowSubtitles)
        {
            int rowNumber = 0;
            GridPane grid = new GridPane();
            grid.setVgap(4);
            grid.setHgap(4);
            for (IdentifiedControl<? extends Control> wpsControl : pControls)
            {
                Label label = new Label(wpsControl.getTitle() + ":");
                grid.addRow(rowNumber, label, wpsControl);
                GridPane.setHalignment(label, HPos.RIGHT);
                if (rowNumber > 0)
                {
                    GridPane.setHalignment(wpsControl, HPos.RIGHT);
                    GridPane.setFillWidth(wpsControl, true);
                }
                rowNumber++;
            }
            return grid;
        }
        return new VBox(pControls);
    }

    /**
     * An event handler method used to react to a value change in either of the date / time pickers. This allows for the preset
     * {@link ComboBox}'s selected item to be changed to {@link #CUSTOM_PRESET}.
     */
    protected void valueChanged()
    {
        if (!myPresetCausedChanged)
        {
            myPresetControl.getControl().valueProperty().set(CUSTOM_PRESET);
        }
    }

    /**
     * An event handler method used to react to the selection of a choice within the preset selector.
     *
     * @param pPresetType The preset value selected by the user.
     */
    protected void presetSelected(String pPresetType)
    {
        myPresetCausedChanged = true;
        switch (pPresetType)
        {
            case "Timeline":
                adjustPickers(myToolbox.getTimeManager().getPrimaryActiveTimeSpans().get(0));
                break;
            case "Last 24 Hours":
                adjustPickers(-24, ChronoUnit.HOURS);
                break;
            case "Last 48 Hours":
                adjustPickers(-48, ChronoUnit.HOURS);
                break;
            case "Last 72 Hours":
                adjustPickers(-72, ChronoUnit.HOURS);
                break;
            case "Last 168 Hours (1 Week)":
                adjustPickers(-1, ChronoUnit.WEEKS);
                break;
            case "Today":
                adjustPickers(LocalDate.now().atStartOfDay(), 1, ChronoUnit.DAYS);
                break;
            case "Yesterday":
                adjustPickers(LocalDate.now().atStartOfDay(), -1, ChronoUnit.DAYS);
                break;
            case "This Week":
                adjustPickers(getStartOfCurrentWeek(), 1, ChronoUnit.WEEKS);
                break;
            case "Last Week":
                adjustPickers(getStartOfCurrentWeek(), -1, ChronoUnit.WEEKS);
                break;
            case "This Month":
                adjustPickers(LocalDate.now().atStartOfDay().withDayOfMonth(1), 1, ChronoUnit.MONTHS);
                break;
            case "Last 30 Days":
                adjustPickers(-30, ChronoUnit.DAYS);
                break;
            case "Last 60 Days":
                adjustPickers(-60, ChronoUnit.DAYS);
                break;
            case "Last 90 Days":
                adjustPickers(-90, ChronoUnit.DAYS);
                break;
            case "This Year":
                adjustPickers(LocalDate.now().atStartOfDay().withDayOfYear(1), 1, ChronoUnit.YEARS);
                break;
            default:
                break;
        }
        myPresetCausedChanged = false;
    }

    /**
     * Adjusts the selected time to the active range displayed on the time line.
     *
     * @param pActiveTimeSpan the span displayed on the time line.
     */
    private void adjustPickers(TimeSpan pActiveTimeSpan)
    {
        myStartInput.getControl().setValue(LocalDateTime.ofInstant(pActiveTimeSpan.getStartDate().toInstant(), ZoneOffset.UTC));
        myEndInput.getControl().setValue(LocalDateTime.ofInstant(pActiveTimeSpan.getEndDate().toInstant(), ZoneOffset.UTC));
    }

    /**
     * Convenience method used to get the start of the current week.
     *
     * @return a {@link LocalDateTime} instance representing the preceding Sunday of the current week.
     */
    protected LocalDateTime getStartOfCurrentWeek()
    {
        LocalDateTime startDateTime = LocalDate.now().with(ChronoField.DAY_OF_WEEK, 7).atStartOfDay();
        if (startDateTime.isAfter(LocalDateTime.now()))
        {
            startDateTime = startDateTime.minus(1, ChronoUnit.WEEKS);
        }
        return startDateTime;
    }

    /**
     * Adjusts the date / time pickers by the supplied offset. The offset direction is determined by the sign of the supplied
     * offset. For example to adjust backwards by 24 hours, supply the offset parameter with -24, and the type parameter with
     * {@link ChronoUnit#HOURS}. Likewise, to adjust forward by 24 hours, supply the offset parameter with 24 and the type
     * parameter with {@link ChronoUnit#HOURS}. If the offset is negative, the method assumes the current date / time for the end
     * time, and adjusts the start time by the offset. If the offset is positive, the method assumes the current date / time for
     * the start time, and adjusts the end time by the offset.
     *
     * @param offset the amount by which to offset the picker.
     * @param type the units by which to adjust.
     * @see #adjustPickers(LocalDateTime, int, TemporalUnit)
     */
    protected void adjustPickers(int offset, TemporalUnit type)
    {
        adjustPickers(LocalDateTime.now(), offset, type);
    }

    /**
     * Adjusts the date / time pickers by the supplied offset. The offset direction is determined by the sign of the supplied
     * offset. For example to adjust backwards by 24 hours, supply the offset parameter with -24, and the type parameter with
     * {@link ChronoUnit#HOURS}. Likewise, to adjust forward by 24 hours, supply the offset parameter with 24 and the type
     * parameter with {@link ChronoUnit#HOURS}. If the offset is negative, the method uses the supplied base date / time for the
     * end time, and adjusts the start time by the offset. If the offset is positive, the method uses the supplied base date /
     * time for the start time, and adjusts the end time by the offset.
     *
     * @param pBaseTime the time from which adjustments are made.
     * @param offset the amount by which to offset the picker.
     * @param type the units by which to adjust.
     */
    protected void adjustPickers(LocalDateTime pBaseTime, int offset, TemporalUnit type)
    {
        LocalDateTime endDateTime;
        LocalDateTime startDateTime;
        if (offset < 0)
        {
            endDateTime = pBaseTime;
            startDateTime = pBaseTime.minus(Math.abs(offset), type);
        }
        else
        {
            startDateTime = pBaseTime;
            endDateTime = pBaseTime.plus(offset, type);
        }

        myStartInput.getControl().setValue(startDateTime);
        myEndInput.getControl().setValue(endDateTime);
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.util.javafx.input.CompoundTitledControl#visit(io.opensphere.core.util.Visitor)
     */
    @Override
    public void visit(Visitor<?> pVisitor)
    {
        myStartInput.visit(pVisitor);
        myEndInput.visit(pVisitor);
    }
}

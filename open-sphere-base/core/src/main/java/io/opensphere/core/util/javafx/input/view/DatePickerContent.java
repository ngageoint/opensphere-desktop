package io.opensphere.core.util.javafx.input.view;

import static java.time.temporal.ChronoField.DAY_OF_WEEK;
import static java.time.temporal.ChronoField.MONTH_OF_YEAR;
import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.MONTHS;
import static java.time.temporal.ChronoUnit.YEARS;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.chrono.ChronoLocalDate;
import java.time.chrono.IsoChronology;
import java.time.format.DateTimeFormatter;
import java.time.format.DecimalStyle;
import java.time.temporal.ChronoUnit;
import java.time.temporal.ValueRange;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.WeakChangeListener;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.DateCell;
import javafx.scene.control.Label;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * The full content for the DatePicker popup. This class could probably be used
 * more or less as-is with an embeddable type of date picker that doesn't use a
 * popup.
 */
@SuppressWarnings("PMD.GodClass")
public class DatePickerContent extends VBox
{
    /**
     * The <code>Log</code> instance used for logging.
     */
    private static final Logger LOG = Logger.getLogger(DatePickerContent.class);

    /**
     * The formatter used to format the name of the month.
     */
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("MMMM");

    /**
     * The formatter used to format the name of the month in standalone mode.
     */
    private static final DateTimeFormatter STANDALONE_MONTH_FORMATTER = DateTimeFormatter.ofPattern("LLLL");

    /**
     * The formatter used to format year values.
     */
    private static final DateTimeFormatter YEAR_FORMATTER = DateTimeFormatter.ofPattern("y");

    /**
     * The formatter used to format year values with era information.
     */
    private static final DateTimeFormatter YEAR_WITH_ERA_FORMATTER = DateTimeFormatter.ofPattern("GGGGy");

    /**
     * The formatter used to format the names of each weekday.
     */
    private static final DateTimeFormatter WEEK_DAY_NAME_FORMATTER = DateTimeFormatter.ofPattern("ccc");

    /**
     * The formatter used to format the day cell's numeric values.
     */
    private static final DateTimeFormatter DAY_CELL_FORMATTER = DateTimeFormatter.ofPattern("d");

    /**
     * The date time picker to which the content is bound.
     */
    private final DatePicker myDateTimePicker;

    /**
     * The button used to roll back a month.
     */
    private Button myBackMonthButton;

    /**
     * The button used to roll forward a month.
     */
    private Button myForwardMonthButton;

    /**
     * The button used to roll back a year.
     */
    private Button myBackYearButton;

    /**
     * The button used to roll forward a year.
     */
    private Button myForwardYearButton;

    /**
     * The button used to select the current day.
     */
    private Button myNowButton;

    /**
     * The label used to display the month.
     */
    private Label myMonthLabel;

    /**
     * The label used to display the year.
     */
    private Label myYearLabel;

    /**
     * The pane on which the calendar is rendered.
     */
    private final GridPane myGridPane;

    /**
     * The number of days per week to display in the calendar.
     */
    private final int myDaysPerWeek;

    /**
     * The cells in which the names of the days are displayed.
     */
    private final List<DateCell> myDayNameCells = new ArrayList<>();

    /**
     * The cells in which the days are displayed.
     */
    private final List<DateCell> myDayCells = new ArrayList<>();

    /**
     * An array of the dates assigned to each day cell.
     */
    private LocalDate[] myDayCellDates;

    /**
     * The last cell selected by the user.
     */
    private DateCell myLastFocusedDayCell;

    /**
     * The property in which the displayed year and month are stored.
     */
    private final ObjectProperty<YearMonth> myDisplayedYearMonth = new SimpleObjectProperty<>(this, "displayedYearMonth");

    /**
     * Creates a new calendar panel.
     *
     * @param pDateTimePicker the picker to which the content will be bound.
     */
    @SuppressWarnings("PMD.ConstructorCallsOverridableMethod")
    public DatePickerContent(DatePicker pDateTimePicker)
    {
        myDateTimePicker = pDateTimePicker;

        getStyleClass().add("date-time-picker-popup");

        myDaysPerWeek = getDaysPerWeek();
        LocalDate date = myDateTimePicker.getValue();
        myDisplayedYearMonth.set(date != null ? YearMonth.from(date) : YearMonth.now());
        myDisplayedYearMonth.addListener((observable, oldValue, newValue) -> updateValues());
        getChildren().add(createHeaderPane());

        myGridPane = new FixedColumnGridPane(myDaysPerWeek);
        myGridPane.setFocusTraversable(true);
        myGridPane.getStyleClass().add("calendar-grid");
        myGridPane.setVgap(-1);
        myGridPane.setHgap(-1);

        // Add a focus owner listener to Scene when it becomes available.
        final ChangeListener<Node> listener = (ov2, oldOwner, newOwner) -> handleFocusChange(oldOwner, newOwner);
        final WeakChangeListener<Node> weakListener = new WeakChangeListener<>(listener);

        myGridPane.sceneProperty().addListener(
                new WeakChangeListener<Scene>((ov, oldScene, newScene) -> adjustFocus(weakListener, oldScene, newScene)));
        if (myGridPane.getScene() != null)
        {
            myGridPane.getScene().focusOwnerProperty().addListener(weakListener);
        }

        // get the weekday labels starting with the weekday that is the
        // first-day-of-the-week according to the locale in the
        // displayed LocalDate
        for (int i = 0; i < myDaysPerWeek; i++)
        {
            DateCell cell = new DateCell();
            cell.getStyleClass().add("day-name-cell");
            myDayNameCells.add(cell);
        }

        createDayCells();
        updateGrid();
        getChildren().add(myGridPane);

        getChildren().add(createFooterPane());

        refresh();

        // RT-30511: This prevents key events from reaching the popup's owner.
        addEventHandler(KeyEvent.ANY, e -> handleKeyEvent(e));
    }

    /**
     * An event handler method that detects if focus has been given to the grid
     * pane. If not, propagates the focus loss throughout the subcomponents.
     *
     * @param oldFocusOwner the component that had focus before the event took
     *            place.
     * @param newFocusOwner the component that gained focus in the event.
     */
    protected void handleFocusChange(Node oldFocusOwner, Node newFocusOwner)
    {
        if (newFocusOwner.equals(myGridPane) && !(oldFocusOwner instanceof DateCell))
        {
            // Forwards traversal, pass focus to day cell.
            if (myLastFocusedDayCell != null)
            {
                Platform.runLater(() -> myLastFocusedDayCell.requestFocus());
            }
            else
            {
                clearFocus();
            }
        }
    }

    /**
     * An event handler method used to adjust how focus events are propagated
     * across the scene. If the old scene is not null, the supplied listener is
     * removed from the old scene. If the new scene is not null, the supplied
     * listener is added to the new scene.
     *
     * @param pListener the listener to add or remove from the scene for focus
     *            event receipt.
     * @param oldScene the scene from which to unregister the supplied listener,
     *            if the scene is not null.
     * @param newScene the scene to which to register the supplied listener, if
     *            the scene is not null.
     */
    protected void adjustFocus(final WeakChangeListener<Node> pListener, Scene oldScene, Scene newScene)
    {
        if (oldScene != null)
        {
            oldScene.focusOwnerProperty().removeListener(pListener);
        }
        if (newScene != null)
        {
            newScene.focusOwnerProperty().addListener(pListener);
        }
    }

    /**
     * Handles any key events fired when the popup is displayed.
     *
     * @param pEvent the key event to process.
     */
    protected void handleKeyEvent(KeyEvent pEvent)
    {
        Node node = getScene().getFocusOwner();
        if (node instanceof DateCell)
        {
            myLastFocusedDayCell = (DateCell)node;
        }

        if (pEvent.getEventType() == KeyEvent.KEY_PRESSED)
        {
            switch (pEvent.getCode())
            {
                case HOME:
                    goToDate(LocalDate.now(), true);
                    pEvent.consume();
                    break;

                case PAGE_UP:
                    if (pEvent.isControlDown())
                    {
                        if (!myBackYearButton.isDisabled())
                        {
                            forward(-1, YEARS, true);
                        }
                    }
                    else
                    {
                        if (!myBackMonthButton.isDisabled())
                        {
                            forward(-1, MONTHS, true);
                        }
                    }
                    pEvent.consume();
                    break;

                case PAGE_DOWN:
                    if (pEvent.isControlDown())
                    {
                        if (!myForwardYearButton.isDisabled())
                        {
                            forward(1, YEARS, true);
                        }
                    }
                    else
                    {
                        if (!myForwardMonthButton.isDisabled())
                        {
                            forward(1, MONTHS, true);
                        }
                    }
                    pEvent.consume();
                    break;
                default:
                    pEvent.consume();
                    break;
            }

            node = getScene().getFocusOwner();
            if (node instanceof DateCell)
            {
                myLastFocusedDayCell = (DateCell)node;
            }
        }

        // Consume all key events except those that control showing the popup
        // and traversal.
        switch (pEvent.getCode())
        {
            case F4:
            case F10:
            case UP:
            case DOWN:
            case LEFT:
            case RIGHT:
            case TAB:
                break;

            case ESCAPE:
                pEvent.consume();
                break;

            default:
                pEvent.consume();
                break;
        }
    }

    /**
     * Gets the value of the {@link #myDisplayedYearMonth} field.
     *
     * @return the value stored in the {@link #myDisplayedYearMonth} field.
     */
    public ObjectProperty<YearMonth> getDisplayedYearMonthProperty()
    {
        return myDisplayedYearMonth;
    }

    /**
     * Creates the panel on which the bottom controls are displayed.
     *
     * @return the panel on which the bottom controls are displayed.
     */
    protected BorderPane createFooterPane()
    {
        BorderPane footerPane = new BorderPane();
        footerPane.getStyleClass().add("footer-pane");

        myNowButton = new Button("Today");
        myNowButton.setOnAction((event) -> goToDate(LocalDate.now(), true));
        myNowButton.getStyleClass().add("today-button");

        footerPane.setCenter(myNowButton);

        return footerPane;
    }

    /**
     * Creates the panel on which the month / year picker is displayed.
     *
     * @return a panel on which the month / year picker is displayed.
     */
    protected BorderPane createHeaderPane()
    {
        BorderPane monthYearPane = new BorderPane();
        monthYearPane.getStyleClass().add("month-year-pane");

        // Month spinner

        HBox monthSpinner = new HBox();
        monthSpinner.getStyleClass().add("spinner");

        myBackMonthButton = new Button();
        myBackMonthButton.getStyleClass().add("left-button");

        myForwardMonthButton = new Button();
        myForwardMonthButton.getStyleClass().add("right-button");

        StackPane leftMonthArrow = new StackPane();
        leftMonthArrow.getStyleClass().add("left-arrow");
        leftMonthArrow.setMaxSize(USE_PREF_SIZE, USE_PREF_SIZE);
        myBackMonthButton.setGraphic(leftMonthArrow);

        StackPane rightMonthArrow = new StackPane();
        rightMonthArrow.getStyleClass().add("right-arrow");
        rightMonthArrow.setMaxSize(USE_PREF_SIZE, USE_PREF_SIZE);
        myForwardMonthButton.setGraphic(rightMonthArrow);

        myBackMonthButton.setOnAction(t -> forward(-1, MONTHS, false));

        myMonthLabel = new Label();
        myMonthLabel.getStyleClass().add("spinner-label");

        myForwardMonthButton.setOnAction(t -> forward(1, MONTHS, false));

        monthSpinner.getChildren().addAll(myBackMonthButton, myMonthLabel, myForwardMonthButton);
        monthYearPane.setLeft(monthSpinner);

        // Year spinner

        HBox yearSpinner = new HBox();
        yearSpinner.getStyleClass().add("spinner");

        myBackYearButton = new Button();
        myBackYearButton.getStyleClass().add("left-button");

        myForwardYearButton = new Button();
        myForwardYearButton.getStyleClass().add("right-button");

        StackPane leftYearArrow = new StackPane();
        leftYearArrow.getStyleClass().add("left-arrow");
        leftYearArrow.setMaxSize(USE_PREF_SIZE, USE_PREF_SIZE);
        myBackYearButton.setGraphic(leftYearArrow);

        StackPane rightYearArrow = new StackPane();
        rightYearArrow.getStyleClass().add("right-arrow");
        rightYearArrow.setMaxSize(USE_PREF_SIZE, USE_PREF_SIZE);
        myForwardYearButton.setGraphic(rightYearArrow);

        myBackYearButton.setOnAction(t -> forward(-1, YEARS, false));

        myYearLabel = new Label();
        myYearLabel.getStyleClass().add("spinner-label");

        myForwardYearButton.setOnAction(t -> forward(1, YEARS, false));

        yearSpinner.getChildren().addAll(myBackYearButton, myYearLabel, myForwardYearButton);
        yearSpinner.setFillHeight(false);
        monthYearPane.setRight(yearSpinner);

        return monthYearPane;
    }

    /**
     * Update the view's layout and values.
     */
    protected void refresh()
    {
        updateMonthLabelWidth();
        updateDayNameCells();
        updateValues();
    }

    /**
     * Update the values within the view.
     */
    protected void updateValues()
    {
        updateDayCells();
        updateMonthYearPane();
    }

    /**
     * Update the grid's display area.
     */
    protected void updateGrid()
    {
        myGridPane.getColumnConstraints().clear();
        myGridPane.getChildren().clear();

        int nCols = myDaysPerWeek;

        ColumnConstraints columnConstraints = new ColumnConstraints();
        columnConstraints.setPercentWidth(100);
        for (int i = 0; i < nCols; i++)
        {
            myGridPane.getColumnConstraints().add(columnConstraints);
        }

        for (int i = 0; i < myDaysPerWeek; i++)
        {
            // col, row
            myGridPane.add(myDayNameCells.get(i), i + nCols - myDaysPerWeek, 1);
        }

        // setup: 6 rows of daysPerWeek (which is the maximum number of cells
        // required in the worst case layout)
        for (int row = 0; row < 6; row++)
        {
            for (int col = 0; col < myDaysPerWeek; col++)
            {
                myGridPane.add(myDayCells.get(row * myDaysPerWeek + col), col + nCols - myDaysPerWeek, row + 2);
            }
        }
    }

    /**
     * Update the cells that display the name of the day of the week.
     */
    protected void updateDayNameCells()
    {
        // first day of week, 1 = monday, 7 = sunday
        int firstDayOfWeek = WeekFields.of(getLocale()).getFirstDayOfWeek().getValue();

        // july 13th 2009 is a Monday, so a firstDayOfWeek=1 must come out of
        // the 13th
        LocalDate date = LocalDate.of(2009, 7, 12 + firstDayOfWeek);
        for (int i = 0; i < myDaysPerWeek; i++)
        {
            String name = WEEK_DAY_NAME_FORMATTER.withLocale(getLocale()).format(date.plus(i, DAYS));
            myDayNameCells.get(i).setText(titleCaseWord(name));
        }
    }

    /**
     * Update the cells that display the number of the day.
     */
    protected void updateDayCells()
    {
        Locale locale = getLocale();
        int firstOfMonthIdx = determineFirstOfMonthDayOfWeek();
        YearMonth curMonth = myDisplayedYearMonth.get();

        // RT-31075: The following are now set in the try-catch block.
        YearMonth prevMonth = null;
        YearMonth nextMonth = null;
        int daysInCurMonth = -1;
        int daysInPrevMonth = -1;
        for (int i = 0; i < 6 * myDaysPerWeek; i++)
        {
            DateCell dayCell = myDayCells.get(i);
            dayCell.getStyleClass().setAll("cell", "date-cell", "day-cell");
            dayCell.setDisable(false);
            dayCell.setStyle(null);
            dayCell.setGraphic(null);
            dayCell.setTooltip(null);

            try
            {
                if (daysInCurMonth == -1)
                {
                    daysInCurMonth = curMonth.lengthOfMonth();
                }
                YearMonth month = curMonth;
                int day = i - firstOfMonthIdx + 1;
                // int index = firstOfMonthIdx + i - 1;
                if (i < firstOfMonthIdx)
                {
                    if (prevMonth == null)
                    {
                        prevMonth = curMonth.minusMonths(1);
                        daysInPrevMonth = prevMonth.lengthOfMonth();
                    }
                    month = prevMonth;
                    day = i + daysInPrevMonth - firstOfMonthIdx + 1;
                    dayCell.getStyleClass().add("previous-month");
                }
                else if (i >= firstOfMonthIdx + daysInCurMonth)
                {
                    if (nextMonth == null)
                    {
                        nextMonth = curMonth.plusMonths(1);
                    }
                    month = nextMonth;
                    day = i - daysInCurMonth - firstOfMonthIdx + 1;
                    dayCell.getStyleClass().add("next-month");
                }
                LocalDate date = month.atDay(day);
                myDayCellDates[i] = date;
                ChronoLocalDate cDate = IsoChronology.INSTANCE.date(date);

                dayCell.setDisable(false);

                if (isToday(date))
                {
                    dayCell.getStyleClass().add("today");
                }

                if (date.equals(myDateTimePicker.getValue()))
                {
                    dayCell.getStyleClass().add("selected");
                }

                String cellText = DAY_CELL_FORMATTER.withLocale(locale).withChronology(IsoChronology.INSTANCE)
                        .withDecimalStyle(DecimalStyle.of(locale)).format(cDate);
                dayCell.setText(cellText);

                dayCell.updateItem(date, false);
            }
            catch (DateTimeException ex)
            {
                // Date is out of range.
                LOG.error("Selected date " + dayCellDate(dayCell) + " is out of range.", ex);
                dayCell.setText(" ");
                dayCell.setDisable(true);
            }
        }
    }

    /**
     * Gets the number of days in a given week.
     *
     * @return the number of days in a given week.
     */
    protected int getDaysPerWeek()
    {
        ValueRange range = IsoChronology.INSTANCE.range(DAY_OF_WEEK);
        return (int)(range.getMaximum() - range.getMinimum() + 1);
    }

    /**
     * Gets the number of months in a given year.
     *
     * @return the number of months in a given year.
     */
    protected int getMonthsPerYear()
    {
        ValueRange range = IsoChronology.INSTANCE.range(MONTH_OF_YEAR);
        return (int)(range.getMaximum() - range.getMinimum() + 1);
    }

    /**
     * Updates the width of the month label upon change.
     */
    protected void updateMonthLabelWidth()
    {
        if (myMonthLabel != null)
        {
            int monthsPerYear = getMonthsPerYear();
            double width = 0;
            for (int i = 0; i < monthsPerYear; i++)
            {
                YearMonth yearMonth = myDisplayedYearMonth.get().withMonth(i + 1);
                String name = STANDALONE_MONTH_FORMATTER.withLocale(getLocale()).format(yearMonth);
                if (Character.isDigit(name.charAt(0)))
                {
                    // Fallback. The standalone format returned a number, so use
                    // standard format instead.
                    name = MONTH_FORMATTER.withLocale(getLocale()).format(yearMonth);
                }

                width = Math.max(width, myMonthLabel.getFont().getSize() * name.length());
            }
            myMonthLabel.setMinWidth(width);
        }
    }

    /**
     * Updates the month / year pane when the value of either the month or the
     * year field changes.
     */
    protected void updateMonthYearPane()
    {
        YearMonth yearMonth = myDisplayedYearMonth.get();
        String str = formatMonth(yearMonth);
        myMonthLabel.setText(str);

        str = formatYear(yearMonth);
        myYearLabel.setText(str);

        double width = myYearLabel.getFont().getSize() * str.length();

        if (width > myYearLabel.getMinWidth())
        {
            myYearLabel.setMinWidth(width);
        }

        LocalDate firstDayOfMonth = yearMonth.atDay(1);
        myBackMonthButton.setDisable(!isValidDate(firstDayOfMonth, -1, DAYS));
        myForwardMonthButton.setDisable(!isValidDate(firstDayOfMonth, +1, MONTHS));
        myBackYearButton.setDisable(!isValidDate(firstDayOfMonth, -1, YEARS));
        myForwardYearButton.setDisable(!isValidDate(firstDayOfMonth, +1, YEARS));
    }

    /**
     * Formats the month component of the supplied year / month value as a word
     * instead of a number.
     *
     * @param yearMonth the year / month value from which to extract the month
     *            component.
     * @return a String in which the month is contained as a word instead of a
     *         number.
     */
    protected String formatMonth(YearMonth yearMonth)
    {
        try
        {
            ChronoLocalDate cDate = IsoChronology.INSTANCE.date(yearMonth.atDay(1));

            String str = STANDALONE_MONTH_FORMATTER.withLocale(getLocale()).withChronology(IsoChronology.INSTANCE).format(cDate);
            if (Character.isDigit(str.charAt(0)))
            {
                // Fallback. The standalone format returned a number, so use
                // standard format instead.
                str = MONTH_FORMATTER.withLocale(getLocale()).withChronology(IsoChronology.INSTANCE).format(cDate);
            }
            return titleCaseWord(str);
        }
        catch (DateTimeException ex)
        {
            // Date is out of range.
            LOG.debug("Date is out of range.", ex);
            return "";
        }
    }

    /**
     * Formats the year component of the supplied year / month value as a
     * four-digit year.
     *
     * @param yearMonth the year / month value from which to extract the year
     *            component.
     * @return a String in which the year is contained as a four-digit year.
     */
    protected String formatYear(YearMonth yearMonth)
    {
        try
        {
            DateTimeFormatter formatter = YEAR_FORMATTER;
            ChronoLocalDate cDate = IsoChronology.INSTANCE.date(yearMonth.atDay(1));
            int era = cDate.getEra().getValue();
            int nEras = IsoChronology.INSTANCE.eras().size();

            if (nEras == 2 && era == 0 || nEras > 2)
            {
                formatter = YEAR_WITH_ERA_FORMATTER;
            }

            String str = formatter.withLocale(getLocale()).withChronology(IsoChronology.INSTANCE)
                    .withDecimalStyle(DecimalStyle.of(getLocale())).format(cDate);

            return str;
        }
        catch (DateTimeException ex)
        {
            // Date is out of range.
            LOG.debug("Date is out of range.", ex);
            return "";
        }
    }

    /**
     * Ensures that month and day names are title-cased (capitalized).
     *
     * @param str the text on which to operate.
     * @return the adjusted text.
     */
    protected String titleCaseWord(String str)
    {
        String workingString = str;
        if (workingString.length() > 0)
        {
            int firstChar = workingString.codePointAt(0);
            if (!Character.isTitleCase(firstChar))
            {
                workingString = new String(new int[] { Character.toTitleCase(firstChar) }, 0, 1)
                        + workingString.substring(Character.offsetByCodePoints(workingString, 0, 1));
            }
        }
        return workingString;
    }

    /**
     * determine on which day of week index the first of the months is.
     *
     * @return the first day on which to start the month.
     */
    protected int determineFirstOfMonthDayOfWeek()
    {
        // determine with which cell to start
        int firstDayOfWeek = WeekFields.of(getLocale()).getFirstDayOfWeek().getValue();
        int firstOfMonthIdx = myDisplayedYearMonth.get().atDay(1).getDayOfWeek().getValue() - firstDayOfWeek;
        if (firstOfMonthIdx < 0)
        {
            firstOfMonthIdx += myDaysPerWeek;
        }
        return firstOfMonthIdx;
    }

    /**
     * Tests to determine if the supplied date represents today's date.
     *
     * @param localDate the date to test.
     * @return true if the supplied date represents today, false otherwise.
     */
    protected boolean isToday(LocalDate localDate)
    {
        return localDate.equals(LocalDate.now());
    }

    /**
     * Gets the date corresponding to the supplied cell.
     *
     * @param dateCell the date corresponding to the supplied cell.
     * @return the date corresponding to the supplied cell.
     */
    protected LocalDate dayCellDate(DateCell dateCell)
    {
        assert myDayCellDates != null;
        return myDayCellDates[myDayCells.indexOf(dateCell)];
    }

    /**
     * Shifts the content's selected cell to the supplied {@link DateCell}.
     *
     * @param dateCell the cell to which to move the selection.
     * @param offset the offset from the supplied cell by which to move the
     *            selection.
     * @param unit the units in which the offset is expressed.
     * @param focusDayCell a flag used to force the supplied dateCell to request
     *            focus after movement.
     */
    public void goToDayCell(DateCell dateCell, int offset, ChronoUnit unit, boolean focusDayCell)
    {
        goToDate(dayCellDate(dateCell).plus(offset, unit), focusDayCell);
    }

    /**
     * Moves the selected date forward from the currently selected date by the
     * supplied offset.
     *
     * @param offset the offset from the currently selected cell by which to
     *            move the selection.
     * @param unit the units in which the offset is expressed.
     * @param focusDayCell a flag used to force the supplied dateCell to request
     *            focus after movement.
     */
    protected void forward(int offset, ChronoUnit unit, boolean focusDayCell)
    {
        YearMonth yearMonth = myDisplayedYearMonth.get();
        DateCell dateCell = myLastFocusedDayCell;
        if (dateCell == null || !dayCellDate(dateCell).getMonth().equals(yearMonth.getMonth()))
        {
            dateCell = findDayCellForDate(yearMonth.atDay(1));
        }
        goToDayCell(dateCell, offset, unit, focusDayCell);
    }

    /**
     * Adjusts the calendar view to the supplied date, and optionally grabs
     * focus.
     *
     * @param date the date to which to adjust the view.
     * @param focusDayCell a flag used to force the calendar view to grab focus.
     */
    public void goToDate(LocalDate date, boolean focusDayCell)
    {
        if (isValidDate(date))
        {
            myDisplayedYearMonth.set(YearMonth.from(date));
            if (focusDayCell)
            {
                myLastFocusedDayCell = findDayCellForDate(date);
                myLastFocusedDayCell.requestFocus();
                refresh();
                updateValues();
            }
        }
    }

    /**
     * Selects the supplied date cell.
     *
     * @param dateCell the cell to select.
     */
    public void selectDayCell(DateCell dateCell)
    {
        myDateTimePicker.setValue(dayCellDate(dateCell));
        myDateTimePicker.hide();
    }

    /**
     * Finds the cell corresponding to the supplied date.
     *
     * @param date the date for which to search.
     * @return a {@link DateCell} corresponding to the supplied date.
     */
    protected DateCell findDayCellForDate(LocalDate date)
    {
        for (int i = 0; i < myDayCellDates.length; i++)
        {
            if (date.equals(myDayCellDates[i]))
            {
                return myDayCells.get(i);
            }
        }
        return myDayCells.get(myDayCells.size() / 2 + 1);
    }

    /**
     * Clears all focus from the panel.
     */
    protected void clearFocus()
    {
        LocalDate focusDate = myDateTimePicker.getValue();
        if (focusDate == null)
        {
            focusDate = LocalDate.now();
        }
        if (YearMonth.from(focusDate).equals(myDisplayedYearMonth.get()))
        {
            // focus date
            goToDate(focusDate, true);
        }
        else
        {
            // focus month spinner (should not happen)
            myBackMonthButton.requestFocus();
        }

        // RT-31857
        if (myBackMonthButton.getWidth() == 0)
        {
            myBackMonthButton.requestLayout();
            myForwardMonthButton.requestLayout();
            myBackYearButton.requestLayout();
            myForwardYearButton.requestLayout();
        }
    }

    /**
     * Creates the cells to display for each day.
     */
    protected void createDayCells()
    {
        final EventHandler<MouseEvent> dayCellActionHandler = ev -> selectCell(ev);

        for (int row = 0; row < 6; row++)
        {
            for (int col = 0; col < myDaysPerWeek; col++)
            {
                DateCell dayCell = new DateCell();
                dayCell.addEventHandler(MouseEvent.MOUSE_CLICKED, dayCellActionHandler);
                myDayCells.add(dayCell);
            }
        }

        myDayCellDates = new LocalDate[6 * myDaysPerWeek];
    }

    /**
     * An event handler method used to react to a mouse event. If the primary
     * button is used to trigger the event, the target day cell is selected.
     *
     * @param pEvent the event to process.
     */
    protected void selectCell(MouseEvent pEvent)
    {
        if (pEvent.getButton() != MouseButton.PRIMARY)
        {
            return;
        }

        DateCell dayCell = (DateCell)pEvent.getSource();
        selectDayCell(dayCell);
        myLastFocusedDayCell = dayCell;
    }

    /**
     * Gets the current locale for internationalization.
     *
     * @return the current locale for internationalization.
     */
    protected Locale getLocale()
    {
        return Locale.getDefault(Locale.Category.FORMAT);
    }

    /**
     * Tests to determine the supplied date is valid.
     *
     * @param date the date to validate.
     * @param offset the offset by which to adjust the date.
     * @param unit the units by which to offset the date.
     * @return true if the supplied value is valid.
     */
    protected boolean isValidDate(LocalDate date, int offset, ChronoUnit unit)
    {
        if (date != null)
        {
            try
            {
                return isValidDate(date.plus(offset, unit));
            }
            catch (DateTimeException ex)
            {
                LOG.debug("Date is out of range.", ex);
            }
        }
        return false;
    }

    /**
     * Tests to determine if the supplied date is valid.
     *
     * @param date the date to validate.
     * @return true if the supplied value is valid.
     */
    protected boolean isValidDate(LocalDate date)
    {
        try
        {
            if (date != null)
            {
                IsoChronology.INSTANCE.date(date);
            }
            return true;
        }
        catch (DateTimeException ex)
        {
            LOG.debug("Date is out of range.", ex);
            return false;
        }
    }

    /**
     * Gets the property to which the displayed year / month field is bound.
     *
     * @return the property to which the displayed year / month field is bound.
     */
    public ObjectProperty<YearMonth> displayedYearMonthProperty()
    {
        return myDisplayedYearMonth;
    }
}

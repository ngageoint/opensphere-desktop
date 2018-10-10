package io.opensphere.core.util.swing.input.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.event.MouseEvent;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.function.Function;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.ToolTipManager;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.jdesktop.swingx.JXMonthView;

import io.opensphere.core.model.time.TimeInstant;
import io.opensphere.core.units.duration.Days;
import io.opensphere.core.util.ValidationStatus;
import io.opensphere.core.util.function.ShortCircuitFunction;
import io.opensphere.core.util.lang.ThreadUtilities;
import io.opensphere.core.util.swing.ButtonPanel;
import io.opensphere.core.util.swing.GridBagPanel;
import io.opensphere.core.util.swing.IconButton;
import io.opensphere.core.util.swing.input.model.TimeInstantModel;
import io.opensphere.core.util.time.TimelineUtilities;

/**
 * A date picker panel. This is currently intended to be used with
 * TimeInstantTextFieldController but could be made more generic.
 */
public class DatePickerPanel extends GridBagPanel
{
    /** The serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The model. */
    private final TimeInstantModel myModel;

    /** The converter from picker date to model time instant. */
    private final Function<Date, TimeInstant> myFromPickerConverter;

    /** The converter from model date to picker date. */
    private final Function<Date, Date> myToPickerConverter;

    /** The format. */
    private DateTextFieldFormat myFormat;

    /** The text field. */
    private final JTextField myTextField;

    /** The calendar button. */
    private final IconButton myCalendarButton;

    /** The calendar picker popup menu. */
    private JPopupMenu myMonthPopup;

    /** The calendar picker. */
    private JXMonthView myMonthView;

    /** The button for selecting now. */
    private final JButton myNowButton;

    /**
     * Optional message to show the user when choosing a value from the calendar
     * picker.
     */
    private String myUserMessage;

    /** The user message count. */
    private int myUserMessageCount;

    /**
     * Constructor.
     *
     * @param model the model
     */
    public DatePickerPanel(TimeInstantModel model)
    {
        this(model, TimeInstant::get, new ShortCircuitFunction<Date>());
    }

    /**
     * Constructor.
     *
     * @param model the model
     * @param fromPickerConverter the converter from picker date to model time
     *            instant
     * @param toPickerConverter the converter from model date to picker date
     */
    public DatePickerPanel(TimeInstantModel model, Function<Date, TimeInstant> fromPickerConverter,
            Function<Date, Date> toPickerConverter)
    {
        myModel = model;
        myFromPickerConverter = fromPickerConverter;
        myToPickerConverter = toPickerConverter;

        myTextField = new JTextField();
        myCalendarButton = new IconButton();
        myCalendarButton.setIcon("/images/defaultCalendar.png");
        myCalendarButton.setRolloverIcon("/images/rolloverCalendar.png");
        myCalendarButton.setFocusPainted(false);
        myCalendarButton.setContentAreaFilled(false);
        myCalendarButton.setBorder(null);
        myNowButton = new JButton();
        createMonthView();

        myCalendarButton.addActionListener(e -> getMonthPopup().show(myTextField, myTextField.getWidth(), 0));

        add(myTextField);
        add(myCalendarButton);
    }

    /**
     * Sets the format.
     *
     * @param format the format
     */
    public void setFormat(DateTextFieldFormat format)
    {
        myFormat = format;
    }

    /**
     * Sets the date.
     *
     * @param date the date
     */
    public void setDate(Date date)
    {
        myTextField.setText(myFormat.getFormat().format(date));
        Date pickerDate = myToPickerConverter.apply(date);
        myMonthView.setFirstDisplayedDay(pickerDate);
        myMonthView.setSelectionDate(pickerDate);
    }

    /**
     * Sets the selection unit.
     *
     * @param selectionUnit the selection unit
     */
    public void setSelectionUnit(ChronoUnit selectionUnit)
    {
        String text;
        if (selectionUnit == ChronoUnit.WEEKS)
        {
            text = "This Week";
        }
        else if (selectionUnit == ChronoUnit.MONTHS)
        {
            text = "This Month";
        }
        else
        {
            text = "Today";
        }
        myNowButton.setText(text);
    }

    /**
     * Sets the userMessage.
     *
     * @param userMessage the userMessage
     */
    public void setUserMessage(String userMessage)
    {
        myUserMessage = userMessage;
    }

    /**
     * Gets the text field.
     *
     * @return the text field
     */
    public JTextField getTextField()
    {
        return myTextField;
    }

    @Override
    public void setEnabled(boolean enabled)
    {
        myTextField.setEditable(enabled);
        myCalendarButton.setEnabled(enabled);
        myMonthView.setEnabled(enabled);
        myNowButton.setEnabled(enabled);
    }

    /**
     * Gets the month popup, creating it if necessary.
     *
     * @return the month popup
     */
    private JPopupMenu getMonthPopup()
    {
        if (myMonthPopup == null)
        {
            myMonthPopup = new JPopupMenu();
            myMonthPopup.setLayout(new BorderLayout());
            myMonthPopup.add(myMonthView, BorderLayout.CENTER);
            myMonthPopup.add(createLowerPanel(), BorderLayout.SOUTH);
            myMonthPopup.pack();

            myMonthPopup.addPopupMenuListener(new PopupMenuListener()
            {
                @Override
                public void popupMenuCanceled(PopupMenuEvent e)
                {
                    if (myModel.getValidationStatus() == ValidationStatus.ERROR)
                    {
                        updateModel(myMonthView.getSelectionDate());
                    }
                }

                @Override
                public void popupMenuWillBecomeInvisible(PopupMenuEvent e)
                {
                }

                @Override
                public void popupMenuWillBecomeVisible(PopupMenuEvent e)
                {
                }
            });
        }
        return myMonthPopup;
    }

    /**
     * Creates the month view.
     *
     * @return the month view
     */
    private JXMonthView createMonthView()
    {
        myMonthView = new JXMonthView();
        JPanel bitchPanel = new JPanel();
        Color background = bitchPanel.getBackground();
        Color foreground = bitchPanel.getForeground();
        myMonthView.setTraversable(true);
        myMonthView.setBackground(background);
        myMonthView.setForeground(foreground);
        myMonthView.setDaysOfTheWeekForeground(Color.GRAY);
        myMonthView.setFlaggedDayForeground(Color.MAGENTA);
        myMonthView.setMonthStringBackground(background);
        myMonthView.setMonthStringForeground(foreground);
        myMonthView.setSelectionBackground(Color.LIGHT_GRAY);
        myMonthView.setSelectionForeground(Color.BLACK);
        myMonthView.setTodayBackground(background);
        myMonthView.addActionListener(e ->
        {
            getMonthPopup().setVisible(false);
            updateModel(myMonthView.getSelectionDate());
        });
        return myMonthView;
    }

    /**
     * Creates the lower panel of the calendar picker.
     *
     * @return the panel
     */
    private JPanel createLowerPanel()
    {
        JPanel panel = new JPanel();
        myNowButton.setMargin(ButtonPanel.INSETS_JOPTIONPANE);
        panel.add(myNowButton);
        myNowButton.addActionListener(e ->
        {
            getMonthPopup().setVisible(false);
            updateModel(TimelineUtilities.roundDown(new Date(), Days.ONE).getTime());
        });
        return panel;
    }

    /**
     * Updates the model value with the view value.
     *
     * @param viewValue the view value
     */
    private void updateModel(Date viewValue)
    {
        boolean changed = myModel.set(myFromPickerConverter.apply(viewValue));
        if (changed)
        {
            showUserMessage();
        }
    }

    /**
     * Shows the user message tool tip if there is one.
     */
    private void showUserMessage()
    {
        if (myUserMessage != null && myUserMessageCount++ < 2)
        {
            myTextField.setToolTipText(myUserMessage);
            MouseEvent phantom = new MouseEvent(myTextField, MouseEvent.MOUSE_MOVED, System.currentTimeMillis(), 0, 2, 10, 0,
                    false);
            ToolTipManager.sharedInstance().mouseMoved(phantom);
            ThreadUtilities.runBackground(() ->
            {
                ThreadUtilities.sleep(1000);
                EventQueue.invokeLater(() -> myTextField.setToolTipText(null));
            });
        }
    }
}

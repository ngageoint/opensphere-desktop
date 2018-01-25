package io.opensphere.core.util.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFormattedTextField.AbstractFormatter;
import javax.swing.JFormattedTextField.AbstractFormatterFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.text.MaskFormatter;

import org.apache.log4j.Logger;
import org.jdesktop.swingx.JXMonthView;

import io.opensphere.core.util.DateTimeFormats;
import io.opensphere.core.util.DateTimeUtilities;
import io.opensphere.core.util.collections.New;

/**
 * The Class DateTimePickerPanel.
 */
@SuppressWarnings("PMD.GodClass")
public final class DateTimePickerPanel extends JPanel
{
    /** The Constant COMMIT_KEY. */
    public static final String COMMIT_KEY = "datePickerCommit";

    /** The Current change type. */
    private DateTimeChangeType myCurrentChangeType;

    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(DateTimePickerPanel.class);

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The action listeners. */
    private final List<ActionListener> myActionListeners = New.list();

    /** The my calendar. */
    private Calendar myCalendar;

    /** The curr date caret pos. */
    private int myCurrDateCaretPos = -1;

    /** The curr time caret pos. */
    private int myCurrTimeCaretPos = -1;

    /** The date caret pos. */
    private int myDateCaretPos = -1;

    /** The date picker ftf. */
    private CustomFormattedTextField myDatePickerFTF;

    /** The Calendar button. */
    private JButton myCalendarButton;

    /** The date format. */
    private final SimpleDateFormat myDateFormat = new SimpleDateFormat(DateTimeFormats.DATE_FORMAT);

    /** The date time format. */
    private final SimpleDateFormat myDateTimeFormat = new SimpleDateFormat(DateTimeFormats.DATE_TIME_FORMAT);

    /** The Current format. */
    private SimpleDateFormat myCurrentFormat;

    /** Flag that indicates whether to fire text field events or not. */
    private final AtomicBoolean myUpdating = new AtomicBoolean(false);

    /** The format used by the time spinner. */
    private final SimpleDateFormat myTimeSpinnerFormat = new SimpleDateFormat("HH:mm:ss");

    /** The Date time factory. */
    private final transient AbstractFormatterFactory myDateTimeFactory = new AbstractFormatterFactory()
    {
        @Override
        public AbstractFormatter getFormatter(JFormattedTextField tf)
        {
            MaskFormatter format = null;
            try
            {
                format = new MaskFormatter("####-##-## ##:##:##");
            }
            catch (ParseException e)
            {
                LOGGER.error("Error retrieving date/time format.", e);
            }
            return format;
        }
    };

    /** The Date factory. */
    private final transient AbstractFormatterFactory myDateFactory = new AbstractFormatterFactory()
    {
        @Override
        public AbstractFormatter getFormatter(JFormattedTextField tf)
        {
            MaskFormatter format = null;
            try
            {
                format = new MaskFormatter("####-##-##");
            }
            catch (ParseException e)
            {
                LOGGER.error("Error retrieving date format.", e);
            }
            return format;
        }
    };

    /** The Month view. */
    private JXMonthView myMonthView;

    /** The Month popup. */
    private JPopupMenu myMonthPopup;

    /** The height. */
    private int myHeight = 20;

    /** The hour button panel. */
    private JPanel myHourButtonPanel;

    /** The is editable. */
    private boolean myIsEditable = true;

    /** The is in date time mode. */
    private boolean myIsInDateTimeMode;

    /** The is using time selector. */
    private boolean myIsUsingTimeSelector;

    /** The last time. */
    private long myLastTime = -1;

    /** The time caret pos. */
    private int myTimeCaretPos = -1;

    /** The time selector panel. */
    private JPanel myTimeSelectorPanel;

    /** The time spinner ftf. */
    private CustomFormattedTextField myTimeSpinnerFTF;

    /** The use time checkbox. */
    private JCheckBox myUseTimeCheckbox;

    /** The width. */
    private int myWidth = 159;

    /** The Constant ourFont. */
    private static final Font FONT;

    static
    {
        if (System.getProperty("os.name").contains("Windows"))
        {
            FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
        }
        else
        {
            FONT = new Font(Font.SERIF, Font.PLAIN, 12);
        }
    }

    /**
     * Instantiates a new date time picker.
     *
     * @param pIsUsingTime the is using time
     */
    public DateTimePickerPanel(boolean pIsUsingTime)
    {
        super();
        myIsUsingTimeSelector = pIsUsingTime;
        initialize();
    }

    /**
     * This is the default constructor.
     *
     * @param pWidth the width
     * @param pHeight the height
     * @param pIsUsingTime the is using time
     */
    public DateTimePickerPanel(int pWidth, int pHeight, boolean pIsUsingTime)
    {
        super();
        myWidth = pWidth;
        myHeight = pHeight;
        myIsUsingTimeSelector = pIsUsingTime;
        initialize();
    }

    /**
     * The main method.
     *
     * @param args the arguments
     */
    public static void main(String[] args)
    {
        JFrame aFrame = new JFrame();

        DateTimePickerPanel dtm = new DateTimePickerPanel(false);
        aFrame.add(dtm);

        aFrame.pack();
        aFrame.setVisible(true);
    }

    /**
     * Adds the action listener.
     *
     * @param listener the listener
     */
    public void addActionListener(ActionListener listener)
    {
        synchronized (myActionListeners)
        {
            if (!myActionListeners.contains(listener))
            {
                myActionListeners.add(listener);
            }
        }
    }

    /**
     * Disable and show text.
     */
    public void disableAndShowText()
    {
        super.setEnabled(false);
        getDatePickerFTF().disableAndShowText();
        getTimeSpinner().disableAndShowText();
        getCalendarButton().setEnabled(false);
        myIsEditable = false;
    }

    /**
     * Gets the current change type.
     *
     * @return the current change type
     */
    public DateTimeChangeType getCurrentChangeType()
    {
        return myCurrentChangeType;
    }

    /**
     * Gets the date.
     *
     * @return the date
     */
    public Date getCurrentPickerDate()
    {
        return getCalendar().getTime();
    }

    /**
     * Gets the month popup.
     *
     * @return the month popup
     */
    public JPopupMenu getMonthPopup()
    {
        if (myMonthPopup == null)
        {
            myMonthPopup = new JPopupMenu();
            myMonthPopup.setLayout(new BorderLayout());
            myMonthPopup.setPopupSize(240, 210);
            myMonthPopup.add(getMonthView(), BorderLayout.CENTER);
            myMonthPopup.add(getTimeSelectorPanel(), BorderLayout.SOUTH);
            myMonthPopup.addPopupMenuListener(new PopupMenuListener()
            {
                @Override
                public void popupMenuCanceled(PopupMenuEvent e)
                {
                    // Do nothing
                }

                @Override
                public void popupMenuWillBecomeInvisible(PopupMenuEvent e)
                {
                    pushToTimeModel();
                }

                @Override
                public void popupMenuWillBecomeVisible(PopupMenuEvent e)
                {
                    // Do nothing
                }
            });
        }
        return myMonthPopup;
    }

    /**
     * Checks if is in date time mode.
     *
     * @return true, if is in date time mode
     */
    public boolean isInDateTimeMode()
    {
        return myIsInDateTimeMode;
    }

    /**
     * Checks if is using time selector.
     *
     * @return true, if is using time selector
     */
    public boolean isUsingTimeSelector()
    {
        return myIsUsingTimeSelector;
    }

    /**
     * Removes the action listener.
     *
     * @param listener the listener
     */
    public void removeActionListener(ActionListener listener)
    {
        synchronized (myActionListeners)
        {
            myActionListeners.remove(listener);
        }
    }

    /**
     * Sets the current change type.
     *
     * @param type the new current change type
     */
    public void setCurrentChangeType(DateTimeChangeType type)
    {
        myCurrentChangeType = type;
    }

    /**
     * Sets the date.
     *
     * @param aDate the new date
     */
    public void setCurrentPickerDate(Date aDate)
    {
        if (aDate != null && aDate.getTime() != getCalendar().getTimeInMillis())
        {
            getCalendar().setTimeInMillis(aDate.getTime());
            updateDatePickerDate(myCurrentChangeType.toString());
            getTimeSpinner().setValue(aDate);
        }
    }

    /**
     * Sets the current picker date and provides a mechanism to filter
     * subsequent events by using the calling object.
     *
     * @param obj the caller
     * @param aDate the new date to set the calendar/spinner to
     */
    public void setCurrentPickerDate(Object obj, Date aDate)
    {
        if (aDate != null && aDate.getTime() != getCalendar().getTimeInMillis())
        {
            getCalendar().setTimeInMillis(aDate.getTime());
            updateDatePickerDate(obj);
            getTimeSpinner().setValue(aDate);
        }
    }

    /**
     * Sets opacity of the date picker and the text field.
     *
     * @param pColor the new color
     */
    public void setDatePickerFTFBackground(Color pColor)
    {
        setOpaque(false);
        getDatePickerFTF().setOpaque(false);
        getDatePickerFTF().setBackground(pColor);
        getMonthView().setOpaque(false);
        getMonthView().setBackground(pColor);
        getDatePickerFTF().setOpaque(false);
        getDatePickerFTF().setBackground(pColor);
    }

    /**
     * Sets the display format.
     *
     * @param isUsingDateTime the new display format
     */
    public void setDisplayFormat(boolean isUsingDateTime)
    {
        getUseTimeCheckbox().setSelected(isUsingDateTime);
        if (isUsingDateTime)
        {
            myCurrentFormat = myDateTimeFormat;
            getDatePickerFTF().setFormatterFactory(myDateTimeFactory);
        }
        else
        {
            myCurrentFormat = myDateFormat;
            getDatePickerFTF().setFormatterFactory(myDateFactory);
        }
    }

    /**
     * Sets the editable.
     *
     * @param editable the new editable
     */
    public void setEditable(boolean editable)
    {
        myIsEditable = editable;
        getUseTimeCheckbox().setEnabled(editable);
        getTimeSpinner().setEditable(false);
        getHourButton().setVisible(editable);
    }

    @Override
    public void setEnabled(boolean enabled)
    {
        super.setEnabled(enabled);
        getDatePickerFTF().setEnabled(enabled);
        getTimeSpinner().setEnabled(enabled);
        getCalendarButton().setEnabled(enabled);
        myIsEditable = enabled;
    }

    /**
     * Sets the first displayed day.
     *
     * @param date the new first displayed day
     */
    public void setFirstDisplayedDay(Date date)
    {
        getMonthView().setFirstDisplayedDay(date);
        getMonthView().setSelectionDate(date);

        // Make sure the time is also set in the time spinner panel.
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        int hour = cal.get(Calendar.HOUR_OF_DAY);
        StringBuilder hourStr = new StringBuilder();
        if (hour < 10)
        {
            hourStr.append(0);
        }
        hourStr.append(Integer.toString(cal.get(Calendar.HOUR_OF_DAY)));

        int min = cal.get(Calendar.MINUTE);
        StringBuilder minStr = new StringBuilder();
        if (min < 10)
        {
            minStr.append(0);
        }
        minStr.append(Integer.toString(cal.get(Calendar.MINUTE)));

        int sec = cal.get(Calendar.SECOND);
        StringBuilder secStr = new StringBuilder();
        if (sec < 10)
        {
            secStr.append(0);
        }
        secStr.append(Integer.toString(cal.get(Calendar.SECOND)));
        getTimeSpinner().setText(hourStr + ":" + minStr + ":" + secStr);
    }

    /**
     * Sets the using time selector.
     *
     * @param isUsingTimeSelector the new using time selector
     */
    public void setUsingTimeSelector(boolean isUsingTimeSelector)
    {
        myIsUsingTimeSelector = isUsingTimeSelector;
        getTimeSelectorPanel().setVisible(myIsUsingTimeSelector);
        getUseTimeCheckbox().setSelected(myIsUsingTimeSelector);
        if (!myIsUsingTimeSelector)
        {
            getTimeSpinner().setValue(getCalendar().getTime());
            getCalendar().set(getCalendar().get(Calendar.YEAR), getCalendar().get(Calendar.MONTH),
                    getCalendar().get(Calendar.DAY_OF_MONTH), 0, 0, 0);
            myCurrentChangeType = DateTimeChangeType.DATE_TIME_CHANGE_ACTION;
            updateDatePickerDate(myCurrentChangeType.toString());
        }
    }

    /**
     * Adds the listeners.
     */
    protected void addListeners()
    {
        getDatePickerFTF().addCaretListener(e -> myDateCaretPos = e.getDot());
        getDatePickerFTF().addMouseWheelListener(e -> mouseWheelMoved(e));
        getDatePickerFTF().getDocument().addDocumentListener(new DocumentListener()
        {
            @Override
            public void changedUpdate(DocumentEvent e)
            {
                if (myUpdating.get())
                {
                    validateEnteredDate();
                }
            }

            @Override
            public void insertUpdate(DocumentEvent e)
            {
                if (myUpdating.get())
                {
                    validateEnteredDate();
                }
            }

            @Override
            public void removeUpdate(DocumentEvent e)
            {
                if (myUpdating.get())
                {
                    validateEnteredDate();
                }
            }
        });
        getDatePickerFTF().addKeyListener(new KeyListener()
        {
            @Override
            public void keyPressed(KeyEvent e)
            {
                if (e.getKeyCode() == KeyEvent.VK_UP)
                {
                    adjustDate(-1);
                }
                else if (e.getKeyCode() == KeyEvent.VK_DOWN)
                {
                    adjustDate(1);
                }
            }

            @Override
            public void keyReleased(KeyEvent e)
            {
            }

            @Override
            public void keyTyped(KeyEvent e)
            {
            }
        });
    }

    /**
     * An event handler method, used to process a mouse-wheel movement event.
     * Used as a lambda to adjust the date when the panel is editable.
     *
     * @param e the event fired by the mouse wheel.
     */
    protected void mouseWheelMoved(MouseWheelEvent e)
    {
        if (myIsEditable)
        {
            adjustDate(e.getWheelRotation());
        }
    }

    /**
     * Adjust date.
     *
     * @param dateRotation the date rotation
     */
    protected void adjustDate(int dateRotation)
    {
        if (!myIsEditable)
        {
            return;
        }

        myCurrDateCaretPos = myDateCaretPos;

        // Month
        if (myDateCaretPos >= 5 && myDateCaretPos <= 7)
        {
            // Add/subtract a Month
            getCalendar().add(Calendar.MONTH, -dateRotation);
        }
        // Day
        else if (myDateCaretPos >= 8 && myDateCaretPos <= 10)
        {
            // Add/subtract a day
            getCalendar().add(Calendar.DAY_OF_YEAR, -dateRotation);
        }
        // Year
        else if (myDateCaretPos >= 0 && myDateCaretPos <= 4)
        {
            // Add/subtract a day
            getCalendar().add(Calendar.YEAR, -dateRotation);
        }

        // Hour
        else if (myDateCaretPos >= 11 && myDateCaretPos < 14)
        {
            // Add/subtract an hour
            getCalendar().add(Calendar.HOUR, -dateRotation);
        }

        // Minute
        else if (myDateCaretPos >= 14 && myDateCaretPos < 17)
        {
            // Add/subtract a minute
            getCalendar().add(Calendar.MINUTE, -dateRotation);
        }

        // Second
        else if (myDateCaretPos >= 17 && myDateCaretPos < 20)
        {
            // Add/subtract a second
            getCalendar().add(Calendar.SECOND, -dateRotation);
        }

        getTimeSpinner().setValue(getCalendar().getTime());
        myCurrentChangeType = DateTimeChangeType.DATE_TIME_CHANGE_ACTION;
        updateDatePickerDate(myCurrentChangeType.toString());
        getDatePickerFTF().setCaretPosition(myCurrDateCaretPos);
    }

    /**
     * Creates the fixed time action listener.
     *
     * @param hourOfDay the hour of day
     * @return the action listener
     */
    protected ActionListener createFixedTimeActionListener(final int hourOfDay)
    {
        return (e) -> shiftToHour(hourOfDay);
    }

    /**
     * Shifts the display to the supplied hour.
     *
     * @param hourOfDay the hour to which to shift the display.
     */
    protected void shiftToHour(final int hourOfDay)
    {
        getCalendar().set(Calendar.HOUR_OF_DAY, hourOfDay);
        getCalendar().set(Calendar.MINUTE, 0);
        getCalendar().set(Calendar.SECOND, 0);
        getCalendar().set(Calendar.MILLISECOND, 0);
        getTimeSpinner().setValue(getCalendar().getTime());
        myCurrentChangeType = DateTimeChangeType.DATE_TIME_CHANGE_ACTION;
        updateDatePickerDate(myCurrentChangeType.toString());
    }

    /**
     * Digit match. Matches 'length' digits.
     *
     * @param str the str
     * @param length the length
     * @return true, if successful
     */
    protected boolean digitMatch(String str, int length)
    {
        String toTest = str;
        boolean valid = false;
        toTest = toTest.trim();

        String matchStr = "[0-9]{" + Integer.toString(length) + "}";
        if (toTest.matches(matchStr))
        {
            valid = true;
        }
        return valid;
    }

    /**
     * Fire action event.
     *
     * @param e the e
     */
    protected void fireActionEvent(ActionEvent e)
    {
        if (myLastTime != getCalendar().getTimeInMillis())
        {
            myLastTime = getCalendar().getTimeInMillis();
            synchronized (myActionListeners)
            {
                for (ActionListener lstr : myActionListeners)
                {
                    lstr.actionPerformed(e);
                }
            }
        }
    }

    /**
     * Gets the calendar.
     *
     * @return the calendar
     */
    protected Calendar getCalendar()
    {
        if (myCalendar == null)
        {
            myCalendar = Calendar.getInstance();
        }
        return myCalendar;
    }

    /**
     * Gets the calendar button.
     *
     * @return the calendar button
     */
    protected JButton getCalendarButton()
    {
        if (myCalendarButton == null)
        {
            myCalendarButton = new JButton();
            try
            {
                myCalendarButton.setIcon(
                        new ImageIcon(ImageIO.read(DateTimePickerPanel.class.getResource("/images/defaultCalendar.png"))));
                myCalendarButton.setRolloverIcon(
                        new ImageIcon(ImageIO.read(DateTimePickerPanel.class.getResource("/images/rolloverCalendar.png"))));
            }
            catch (IOException e)
            {
                LOGGER.error("IOException reading images.", e);
            }

            myCalendarButton.setFocusPainted(false);
            myCalendarButton.setContentAreaFilled(false);
            myCalendarButton.setBorder(null);
            myCalendarButton.setSize(new Dimension(17, 15));
            myCalendarButton.setPreferredSize(myCalendarButton.getSize());
            myCalendarButton.setMinimumSize(myCalendarButton.getSize());
            myCalendarButton.addMouseListener(new MouseAdapter()
            {
                @Override
                public void mouseClicked(MouseEvent e)
                {
                    setFirstDisplayedDay(getCalendar().getTime());
                    getMonthPopup().show(getDatePickerFTF(), getDatePickerFTF().getLocation().x, getDatePickerFTF().getHeight());
                }
            });
        }
        return myCalendarButton;
    }

    /**
     * Gets the date picker ftf.
     *
     * @return the date picker ftf
     */
    protected CustomFormattedTextField getDatePickerFTF()
    {
        if (myDatePickerFTF == null)
        {
            myDatePickerFTF = new CustomFormattedTextField();
            myDatePickerFTF.setFont(FONT);
            myDatePickerFTF.setFormatterFactory(myDateFactory);
        }
        return myDatePickerFTF;
    }

    /**
     * Gets the hour button button.
     *
     * @return the hour button button
     */
    protected JPanel getHourButton()
    {
        if (myHourButtonPanel == null)
        {
            int leftInset = 3;
            int rightInset = 3;
            myHourButtonPanel = new JPanel(new GridLayout(1, 4));
            JButton zeroTimeButton = new JButton("00");
            zeroTimeButton.setMargin(new Insets(3, leftInset, 3, rightInset));
            myHourButtonPanel.add(zeroTimeButton);
            zeroTimeButton.addActionListener(createFixedTimeActionListener(0));

            JButton sixTimeButton = new JButton("06");
            sixTimeButton.setMargin(new Insets(3, leftInset, 3, rightInset));
            myHourButtonPanel.add(sixTimeButton);
            sixTimeButton.addActionListener(createFixedTimeActionListener(6));

            JButton twelveTimeButton = new JButton("12");
            twelveTimeButton.setMargin(new Insets(3, leftInset, 3, rightInset));
            myHourButtonPanel.add(twelveTimeButton);
            twelveTimeButton.addActionListener(createFixedTimeActionListener(12));

            JButton eighteenTimeButton = new JButton("18");
            eighteenTimeButton.setMargin(new Insets(3, leftInset, 3, rightInset));
            myHourButtonPanel.add(eighteenTimeButton);
            eighteenTimeButton.addActionListener(createFixedTimeActionListener(18));
        }
        if (!myIsEditable)
        {
            myHourButtonPanel.setVisible(false);
        }

        return myHourButtonPanel;
    }

    /**
     * Gets the month view.
     *
     * @return the month view
     */
    protected JXMonthView getMonthView()
    {
        if (myMonthView == null)
        {
            myMonthView = new JXMonthView();
            myMonthView.setTraversable(true);
            myMonthView.setBackground(getBackground());
            myMonthView.setForeground(getForeground());
            myMonthView.setDaysOfTheWeekForeground(Color.gray);
            myMonthView.setFlaggedDayForeground(Color.magenta);
            myMonthView.setMonthStringBackground(getBackground());
            myMonthView.setMonthStringForeground(getForeground());
            myMonthView.setSelectionBackground(Color.lightGray);
            myMonthView.setSelectionForeground(Color.BLACK);
            myMonthView.setTodayBackground(getBackground());
            myMonthView.addActionListener(e -> updateDateTime());
        }
        return myMonthView;
    }

    /**
     * Updates the date / time selection based on the user's selections. Used as
     * a lambda for the {@link #myMonthView}'s action listener.
     */
    protected void updateDateTime()
    {
        Calendar cal = Calendar.getInstance();
        cal.setTime(getMonthView().getFirstSelectionDate());

        if (isUsingTimeSelector())
        {
            String[] timeTok = getTimeSpinner().getText().split(":");
            cal.add(Calendar.HOUR, Integer.parseInt(timeTok[0]));
            cal.add(Calendar.MINUTE, Integer.parseInt(timeTok[1]));
            cal.add(Calendar.SECOND, Integer.parseInt(timeTok[2]));
        }
        getCalendar().setTime(cal.getTime());
        myCurrentChangeType = DateTimeChangeType.CALENDAR_DATE_CHANGE_ACTION;
        updateDatePickerDate(myCurrentChangeType.toString());
        getMonthPopup().setVisible(false);
    }

    /**
     * Gets the time selector panel.
     *
     * @return the time selector panel
     */
    protected JPanel getTimeSelectorPanel()
    {
        if (myTimeSelectorPanel == null)
        {
            myTimeSelectorPanel = new JPanel();
            myTimeSelectorPanel.setSize(200, 20);
            myTimeSelectorPanel.setLayout(new BoxLayout(myTimeSelectorPanel, BoxLayout.X_AXIS));
            myTimeSelectorPanel.add(getUseTimeCheckbox());
            myTimeSelectorPanel.setVisible(myIsUsingTimeSelector);
        }
        return myTimeSelectorPanel;
    }

    /**
     * This method initializes timeSpinner.
     *
     * @return javax.swing.JSpinner
     */
    protected CustomFormattedTextField getTimeSpinner()
    {
        if (myTimeSpinnerFTF == null)
        {
            myTimeSpinnerFTF = new CustomFormattedTextField(myTimeSpinnerFormat);
            myTimeSpinnerFTF.setValue(new Date());

            myTimeSpinnerFTF.addCaretListener(e ->
            {
                if (myIsEditable && e.getSource().equals(myTimeSpinnerFTF))
                {
                    myTimeCaretPos = e.getDot();
                }
            });

            myTimeSpinnerFTF.addMouseWheelListener(e ->
            {
                if (myIsEditable)
                {
                    updateSpinner(e.getWheelRotation());
                    getTimeSpinner().setValue(getCalendar().getTime());
                    getTimeSpinner().setCaretPosition(myCurrTimeCaretPos);
                    e.consume();
                }
            });
            myTimeSpinnerFTF.addPropertyChangeListener(evt ->
            {
                if ("value".equals(evt.getPropertyName()))
                {
                    pushToTimeModel();
                }
            });
            myTimeSpinnerFTF.addKeyListener(new KeyListener()
            {
                @Override
                public void keyPressed(KeyEvent e)
                {
                    if (myIsEditable && e.getSource().equals(myTimeSpinnerFTF))
                    {
                        if (e.getKeyCode() == KeyEvent.VK_UP)
                        {
                            updateSpinner(-1);
                            myTimeSpinnerFTF.setValue(getCalendar().getTime());
                            myTimeSpinnerFTF.setCaretPosition(myCurrTimeCaretPos);
                        }
                        else if (e.getKeyCode() == KeyEvent.VK_DOWN)
                        {
                            updateSpinner(1);
                            myTimeSpinnerFTF.setValue(getCalendar().getTime());
                            myTimeSpinnerFTF.setCaretPosition(myCurrTimeCaretPos);
                        }
                        else if (e.getKeyCode() == KeyEvent.VK_LEFT)
                        {
                            if (myTimeCaretPos - 1 >= 0)
                            {
                                myTimeSpinnerFTF.setCaretPosition(myTimeCaretPos - 1);
                            }
                        }
                        else if (e.getKeyCode() == KeyEvent.VK_RIGHT && myTimeCaretPos + 1 <= myTimeSpinnerFTF.getText().length())
                        {
                            myTimeSpinnerFTF.setCaretPosition(myTimeCaretPos + 1);
                        }
                        e.consume();
                    }
                }

                @Override
                public void keyReleased(KeyEvent e)
                {
                }

                @Override
                public void keyTyped(KeyEvent e)
                {
                }
            });
        }
        return myTimeSpinnerFTF;
    }

    /**
     * This method initializes useTimeCheckbox.
     *
     * @return javax.swing.JCheckBox
     */
    protected JCheckBox getUseTimeCheckbox()
    {
        if (myUseTimeCheckbox == null)
        {
            myUseTimeCheckbox = new JCheckBox();
            myUseTimeCheckbox.setText("Time");
            myUseTimeCheckbox.setFocusPainted(false);
            myUseTimeCheckbox.addItemListener(evt -> useTimeCheckboxEvent(evt));
        }
        return myUseTimeCheckbox;
    }

    /**
     * This method initializes this.
     */
    protected void initialize()
    {
        this.setSize(myHeight, myWidth);
        setPreferredSize(new Dimension(myWidth, myHeight));
        setMinimumSize(new Dimension(myWidth, myHeight));
        setLayout(new BorderLayout());

        add(getDatePickerFTF(), BorderLayout.CENTER);
        add(getCalendarButton(), BorderLayout.EAST);
        setDisplayFormat(myIsUsingTimeSelector);
        getDatePickerFTF().setText(myCurrentFormat.format(getCalendar().getTime()));
        myCurrentChangeType = DateTimeChangeType.DATE_TIME_CHANGE_ACTION;
        updateDatePickerDate(myCurrentChangeType.toString());

        // Make sure all date initialization is complete before adding the text
        // field listeners
        addListeners();
    }

    /**
     * Takes the value in the time spinner text and puts it in the Calendar
     * model.
     */
    protected void pushToTimeModel()
    {
        String timeText = myTimeSpinnerFTF.getText();
        if (timeText != null)
        {
            try
            {
                Date time = myTimeSpinnerFormat.parse(timeText);
                Calendar timeCalendar = Calendar.getInstance();
                timeCalendar.setTime(time);
                getCalendar().set(Calendar.HOUR_OF_DAY, timeCalendar.get(Calendar.HOUR_OF_DAY));
                getCalendar().set(Calendar.MINUTE, timeCalendar.get(Calendar.MINUTE));
                getCalendar().set(Calendar.SECOND, timeCalendar.get(Calendar.SECOND));
                getCalendar().set(Calendar.MILLISECOND, timeCalendar.get(Calendar.MILLISECOND));
                myCurrentChangeType = DateTimeChangeType.DATE_TIME_CHANGE_ACTION;
                updateDatePickerDate(myCurrentChangeType.toString());
            }
            catch (ParseException e)
            {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

    /**
     * Update date picker date and provide a mechanism to filter subsequent
     * events by using the calling object.
     *
     * @param obj the caller
     */
    protected void updateDatePickerDate(final Object obj)
    {
        myUpdating.set(false);
        getDatePickerFTF().setText(myCurrentFormat.format(getCalendar().getTime()));
        getMonthView().setSelectionDate(getCalendar().getTime());
        myUpdating.set(true);
        if (obj == null)
        {
            fireActionEvent(new ActionEvent(this, 0, myCurrentChangeType.toString()));
        }
        else
        {
            fireActionEvent(new ActionEvent(obj, 0, myCurrentChangeType.toString()));
        }
    }

    /**
     * Update date picker date. Make sure to disable document event processing
     * temporarily so that we do not attempt to mutate in notification.
     *
     * @param eventType the event type
     */
    protected void updateDatePickerDate(String eventType)
    {
        myUpdating.set(false);
        getDatePickerFTF().setText(myCurrentFormat.format(getCalendar().getTime()));
        getMonthView().setSelectionDate(getCalendar().getTime());
        myUpdating.set(true);
        fireActionEvent(new ActionEvent(this, 0, myCurrentChangeType.toString()));
    }

    /**
     * Update spinner.
     *
     * @param pDir the dir
     */
    protected void updateSpinner(int pDir)
    {
        myCurrTimeCaretPos = myTimeCaretPos;
        if (LOGGER.isDebugEnabled())
        {
            LOGGER.debug("in updateSpinner,  caret pos = " + myTimeCaretPos);
        }

        // Hour
        if (myTimeCaretPos >= 0 && myTimeCaretPos < 3)
        {
            // Add/subtract a Month
            getCalendar().add(Calendar.HOUR, -pDir);
        }
        else if (myTimeCaretPos >= 3 && myTimeCaretPos < 6)
        {
            // Add/subtract a day
            getCalendar().add(Calendar.MINUTE, -pDir);
        }
        else if (myTimeCaretPos >= 6 && myTimeCaretPos < 9)
        {
            // Add/subtract a day
            getCalendar().add(Calendar.SECOND, -pDir);
        }

        myCurrentChangeType = DateTimeChangeType.DATE_TIME_CHANGE_ACTION;
        updateDatePickerDate(myCurrentChangeType.toString());
    }

    /**
     * Use time checkbox event.
     *
     * @param evt the evt
     */
    protected void useTimeCheckboxEvent(ItemEvent evt)
    {
        if (evt.getSource().equals(getUseTimeCheckbox()))
        {
            if (evt.getStateChange() == ItemEvent.SELECTED)
            {
                // Change the format to the date/time format
                myCurrentFormat = myDateTimeFormat;
                getDatePickerFTF().setFormatterFactory(myDateTimeFactory);

                // If the current date is today, use todays time, else quadZ
                Calendar now = Calendar.getInstance();
                if (getCalendar().get(Calendar.YEAR) == now.get(Calendar.YEAR)
                        && getCalendar().get(Calendar.MONTH) == now.get(Calendar.MONTH)
                        && getCalendar().get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR))
                {
                    getCalendar().setTime(now.getTime());
                    myCurrentChangeType = DateTimeChangeType.DATE_TIME_CHANGE_ACTION;
                    updateDatePickerDate(myCurrentChangeType.toString());
                    getTimeSpinner().setValue(getCalendar().getTime());
                }
                else
                {
                    getCalendar().set(getCalendar().get(Calendar.YEAR), getCalendar().get(Calendar.MONTH),
                            getCalendar().get(Calendar.DAY_OF_MONTH), 0, 0, 0);
                    myCurrentChangeType = DateTimeChangeType.DATE_TIME_CHANGE_ACTION;
                    updateDatePickerDate(myCurrentChangeType.toString());
                    getTimeSpinner().setValue(getCalendar().getTime());
                }

                // Add the timeSpinner
                getTimeSelectorPanel().add(getTimeSpinner());
                getTimeSelectorPanel().add(getHourButton());
                getTimeSelectorPanel().validate();
                getTimeSelectorPanel().repaint();
                myIsInDateTimeMode = true;
            }
            else if (evt.getStateChange() == ItemEvent.DESELECTED)
            {
                // Change the format to the date format
                myCurrentFormat = myDateFormat;
                getDatePickerFTF().setFormatterFactory(myDateFactory);

                getCalendar().set(Calendar.HOUR_OF_DAY, 0);
                getCalendar().set(Calendar.MINUTE, 0);
                getCalendar().set(Calendar.SECOND, 0);
                getCalendar().set(Calendar.MILLISECOND, 0);
                myCurrentChangeType = DateTimeChangeType.DATE_TIME_CHANGE_ACTION;
                updateDatePickerDate(myCurrentChangeType.toString());

                // Remove the time spinner
                getTimeSelectorPanel().remove(getTimeSpinner());
                getTimeSelectorPanel().remove(getHourButton());
                getTimeSelectorPanel().validate();
                getTimeSelectorPanel().repaint();

                myIsInDateTimeMode = false;
            }
        }
    }

    /**
     * Validate entered date. Depending on date or date/time usage, parse the
     * user entered string and set the date accordingly.
     */
    protected void validateEnteredDate()
    {
        boolean validString = false;
        String[] tok = null;
        String validDateString = getDatePickerFTF().getText();

        // Substitute spaces for zeros so that the date formatter doesn't
        // complain
        validDateString = validDateString.trim();

        // For date tokens below: year = tok[0], month = tok[1], day = tok[2]

        if (validDateString.length() > 0)
        {
            if (isUsingTimeSelector())
            {
                tok = validDateString.split(" ");
                if (tok.length == 2)
                {
                    String[] dateTok = tok[0].split("-");
                    if (dateTok.length == 3 && digitMatch(dateTok[0], 4) && digitMatch(dateTok[1], 2)
                            && digitMatch(dateTok[2], 2))
                    {
                        String[] timeTok = tok[1].split(":");
                        if (timeTok.length == 3 && digitMatch(timeTok[0], 2) && digitMatch(timeTok[1], 2)
                                && digitMatch(timeTok[2], 2))
                        {
                            validString = true;
                        }
                    }
                }
            }
            else
            {
                tok = validDateString.split("-");
                if (tok.length == 3 && digitMatch(tok[0], 4) && digitMatch(tok[1], 2) && digitMatch(tok[2], 2))
                {
                    validString = true;
                }
            }

            if (validString)
            {
                try
                {
                    getCalendar().setTime(DateTimeUtilities.parse(myCurrentFormat, validDateString));
                    fireActionEvent(new ActionEvent(this, 0, myCurrentChangeType.toString()));
                }
                catch (ParseException e)
                {
                    LOGGER.error("Error parsing date.", e);
                }
            }
        }
    }
}

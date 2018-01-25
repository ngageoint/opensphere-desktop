package io.opensphere.core.util.swing;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JFrame;

import io.opensphere.core.util.lang.EqualsHelper;

/**
 * The Class SlavedDateTimePicker. This class will prevent a begin date from
 * being later than a start date and vice versa.
 */
public class SlavedDateTimePicker implements ActionListener
{
    /** The our slaved date time picker listeners. */
    private final Set<SlavedDateTimeListener> mySlavedDateTimePickerListeners = new HashSet<>();

    /** The Begin date time picker. */
    private final DateTimePickerPanel myBeginDateTimePicker;

    /** The End date time picker. */
    private final DateTimePickerPanel myEndDateTimePicker;

    /**
     * The main method.
     *
     * @param args the arguments
     */
    public static void main(String[] args)
    {
        JFrame aFrame = new JFrame();

        SlavedDateTimePicker dtm = new SlavedDateTimePicker(true);
        aFrame.getContentPane().setLayout(new BorderLayout());
        aFrame.getContentPane().add(dtm.getBeginDateTimePicker(), BorderLayout.NORTH);
        aFrame.getContentPane().add(dtm.getEndDateTimePicker(), BorderLayout.SOUTH);

        aFrame.pack();
        aFrame.setVisible(true);
    }

    /**
     * Instantiates a new slaved date time picker.
     *
     * @param isUsingTime the is using time
     */
    public SlavedDateTimePicker(boolean isUsingTime)
    {
        myBeginDateTimePicker = new DateTimePickerPanel(isUsingTime);
        myBeginDateTimePicker.addActionListener(this);
        myEndDateTimePicker = new DateTimePickerPanel(isUsingTime);
        myEndDateTimePicker.addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        if (EqualsHelper.equals(e.getSource(), myBeginDateTimePicker))
        {
            if (myBeginDateTimePicker.getCurrentPickerDate().after(myEndDateTimePicker.getCurrentPickerDate()))
            {
                myEndDateTimePicker.setCurrentPickerDate(new Date(myBeginDateTimePicker.getCurrentPickerDate().getTime()));
            }
        }
        else if (EqualsHelper.equals(e.getSource(), myEndDateTimePicker)
                && myEndDateTimePicker.getCurrentPickerDate().before(myBeginDateTimePicker.getCurrentPickerDate()))
        {
            myBeginDateTimePicker.setCurrentPickerDate(new Date(myEndDateTimePicker.getCurrentPickerDate().getTime()));
        }
        fireDateChange(new SlavedDateTimeEvent(this, myBeginDateTimePicker.getCurrentPickerDate(),
                myEndDateTimePicker.getCurrentPickerDate()));
    }

    /**
     * Adds the slaved date time picker listener.
     *
     * @param lstr the listener to add
     */
    public void addSlavedDateTimePickerListener(SlavedDateTimeListener lstr)
    {
        if (lstr != null)
        {
            synchronized (mySlavedDateTimePickerListeners)
            {
                mySlavedDateTimePickerListeners.add(lstr);
            }
        }
    }

    /**
     * Fires a {@link SlavedDateTimeEvent} to listeners.
     *
     * @param e the e
     */
    public void fireDateChange(final SlavedDateTimeEvent e)
    {
        synchronized (mySlavedDateTimePickerListeners)
        {
            List<SlavedDateTimeListener> list = new ArrayList<SlavedDateTimeListener>(mySlavedDateTimePickerListeners);

            for (SlavedDateTimeListener al : list)
            {
                al.slavedDateChanged(e);
            }
        }
    }

    /**
     * Gets the begin date time picker.
     *
     * @return the begin date time picker
     */
    public DateTimePickerPanel getBeginDateTimePicker()
    {
        return myBeginDateTimePicker;
    }

    /**
     * Gets the end date time picker.
     *
     * @return the end date time picker
     */
    public DateTimePickerPanel getEndDateTimePicker()
    {
        return myEndDateTimePicker;
    }

    /**
     * Removes a listener.
     *
     * @param lstr the listener to remove
     */
    public void removeSlavedDateTimePickerListener(SlavedDateTimeListener lstr)
    {
        if (lstr != null)
        {
            synchronized (mySlavedDateTimePickerListeners)
            {
                mySlavedDateTimePickerListeners.remove(lstr);
            }
        }
    }
}

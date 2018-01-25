package io.opensphere.core.util.swing.input.controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.ParseException;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import io.opensphere.core.util.swing.DateTimePickerPanel;
import io.opensphere.core.util.swing.input.model.DateModel;

/**
 * DateController used to control edits of dates.
 */
public class DateController extends AbstractController<String, DateModel, DateTimePickerPanel>
{
    /**
     * Used to log messages.
     */
    private static final Logger LOGGER = Logger.getLogger(DateController.class);

    /** The document listener. */
    private ActionListener myActionListener;

    /**
     * Constructs a new date controller.
     *
     * @param model The model that contains the date string to modify.
     */
    public DateController(DateModel model)
    {
        super(model, new DateTimePickerPanel(true));
    }

    @Override
    public void close()
    {
        super.close();
        getView().removeActionListener(myActionListener);
    }

    @Override
    public void open()
    {
        super.open();
        myActionListener = new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                updateModel();
            }
        };

        getView().addActionListener(myActionListener);
    }

    @Override
    protected void updateModel()
    {
        Date pickedDate = getView().getCurrentPickerDate();
        String dateString = getModel().getFormat().format(pickedDate);

        getModel().set(dateString);
    }

    @Override
    protected void updateViewValue()
    {
        Date date = null;
        try
        {
            String value = getModel().get();

            if (StringUtils.isNotEmpty(value))
            {
                date = getModel().getFormat().parse(getModel().get());
            }
        }
        catch (ParseException e1)
        {
            LOGGER.error(e1.getMessage(), e1);
        }

        getView().setCurrentPickerDate(date);
    }
}

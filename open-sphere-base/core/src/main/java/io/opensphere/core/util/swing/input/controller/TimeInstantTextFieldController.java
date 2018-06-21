package io.opensphere.core.util.swing.input.controller;

import java.awt.Color;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.text.ParseException;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Objects;
import java.util.function.Function;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.apache.log4j.Logger;

import io.opensphere.core.model.time.TimeInstant;
import io.opensphere.core.units.duration.Days;
import io.opensphere.core.units.duration.Duration;
import io.opensphere.core.units.duration.Hours;
import io.opensphere.core.units.duration.Minutes;
import io.opensphere.core.units.duration.Months;
import io.opensphere.core.units.duration.Seconds;
import io.opensphere.core.units.duration.Years;
import io.opensphere.core.util.MathUtil;
import io.opensphere.core.util.ValidationStatus;
import io.opensphere.core.util.awt.AWTUtilities;
import io.opensphere.core.util.swing.ComponentUtilities;
import io.opensphere.core.util.swing.DocumentListenerAdapter;
import io.opensphere.core.util.swing.input.model.TimeInstantModel;
import io.opensphere.core.util.swing.input.view.DatePickerPanel;
import io.opensphere.core.util.swing.input.view.DateTextFieldFormat;

/**
 * Controller for a time instant with a text field.
 */
public class TimeInstantTextFieldController extends AbstractController<TimeInstant, TimeInstantModel, DatePickerPanel>
{
    /** The logger. */
    private static final Logger LOGGER = Logger.getLogger(TimeInstantTextFieldController.class);

    /** The format. */
    private DateTextFieldFormat myFormat;

    /** The selection unit. */
    private ChronoUnit mySelectionUnit;

    /** The document listener. */
    private DocumentListener myDocumentListener;

    /** The mouse wheel listener. */
    private MouseWheelListener myMouseWheelListener;

    /**
     * Constructor.
     *
     * @param model the model
     */
    public TimeInstantTextFieldController(TimeInstantModel model)
    {
        super(model, new DatePickerPanel(model));
    }

    /**
     * Constructor.
     *
     * @param model the model
     * @param fromPickerConverter the converter from picker date to model time
     *            instant
     * @param toPickerConverter the converter from model date to picker date
     */
    public TimeInstantTextFieldController(TimeInstantModel model, Function<Date, TimeInstant> fromPickerConverter,
            Function<Date, Date> toPickerConverter)
    {
        super(model, new DatePickerPanel(model, fromPickerConverter, toPickerConverter));
    }

    @Override
    public void close()
    {
        super.close();
        getView().getTextField().getDocument().removeDocumentListener(myDocumentListener);
        getView().getTextField().removeMouseWheelListener(myMouseWheelListener);
    }

    @Override
    public void open()
    {
        setFormat(DateTextFieldFormat.DATE);
        setSelectionUnit(ChronoUnit.DAYS);
        Color background = getView().getTextField().getBackground();
        super.open();
        setDefaultBackground(background);
        getView().getTextField().setBackground(background);

        myDocumentListener = new DocumentListenerAdapter()
        {
            @Override
            protected void updateAction(DocumentEvent event)
            {
                boolean isValid = myFormat.isValid(getView().getTextField().getText());
                if (isValid)
                {
                    handleViewChange();
                }
                getModel().setValid(isValid, TimeInstantTextFieldController.this);
            }
        };
        getView().getTextField().getDocument().addDocumentListener(myDocumentListener);

        myMouseWheelListener = new MouseWheelListener()
        {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e)
            {
                if (getView().getTextField().isEditable())
                {
                    handleMouseWheelEvent(e);
                }
            }
        };
        getView().getTextField().addMouseWheelListener(myMouseWheelListener);
    }

    /**
     * Sets the format.
     *
     * @param format the format
     */
    public void setFormat(DateTextFieldFormat format)
    {
        if (!Objects.equals(myFormat, format))
        {
            myFormat = format;
            getView().setFormat(format);
            updateViewValue();
            ComponentUtilities.setPreferredWidth(getView().getTextField(), myFormat.getWidth());
            if (getView().getTextField().getParent() != null)
            {
                getView().getTextField().getParent().revalidate();
            }
        }
    }

    /**
     * Sets the selection unit.
     *
     * @param selectionUnit the selection unit
     */
    public void setSelectionUnit(ChronoUnit selectionUnit)
    {
        mySelectionUnit = selectionUnit;
        getView().setSelectionUnit(selectionUnit);
    }

    @Override
    protected void updateModel()
    {
        try
        {
            Date date = myFormat.parse(getView().getTextField().getText());
            getModel().set(TimeInstant.get(date));
        }
        catch (ParseException e)
        {
            LOGGER.error(e.getMessage());
        }
    }

    @Override
    protected void updateViewValue()
    {
        getView().setDate(getModel().get().toDate());
    }

    @Override
    protected void updateViewLookAndFeel()
    {
        if (getModel().getValidationStatus() != ValidationStatus.VALID)
        {
            getView().getTextField().setToolTipText(getModel().getErrorMessage());
            getView().getTextField().setBackground(getErrorBackground());
        }
        else
        {
            getView().getTextField().setToolTipText(getModel().getDescription());
            getView().getTextField().setBackground(getDefaultBackground());
        }
    }

    /**
     * Handles a MouseWheelEvent.
     *
     * @param e the event
     */
    private void handleMouseWheelEvent(MouseWheelEvent e)
    {
        // Calculate the scroll duration
        Duration duration;
        {
            String text = getView().getTextField().getText();
            double textWidth = AWTUtilities.getTextWidth(text, getView().getTextField().getGraphics());
            int adjustedMouseX = e.getX() - getView().getTextField().getInsets().left;
            double percent = adjustedMouseX / textWidth;
            int charIndex = MathUtil.clamp((int)(text.length() * percent), 0, text.length() - 1);
            char formatChar = myFormat.getFormat().toPattern().charAt(charIndex);
            int magnitude = -e.getWheelRotation();
            switch (formatChar)
            {
                case 'y':
                    duration = new Years(magnitude);
                    break;
                case 'M':
                    duration = new Months(magnitude);
                    break;
                case 'd':
                    // Handle scrolling the day field by a week
                    if (mySelectionUnit == ChronoUnit.WEEKS)
                    {
                        magnitude *= 7;
                    }
                    duration = new Days(magnitude);
                    break;
                case 'H':
                    duration = new Hours(magnitude);
                    break;
                case 'm':
                    duration = new Minutes(magnitude);
                    break;
                case 's':
                    duration = new Seconds(magnitude);
                    break;
                default:
                    duration = null;
                    break;
            }
        }

        // Adjust the model
        if (duration != null)
        {
            getModel().plus(duration);
        }
    }
}

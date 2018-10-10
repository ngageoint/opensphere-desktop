package io.opensphere.core.util.swing.input.controller;

import java.awt.event.MouseWheelListener;

import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeListener;
import javax.swing.text.DefaultFormatter;

import io.opensphere.core.util.swing.input.model.IntegerModel;
import io.opensphere.core.util.swing.input.model.PropertyChangeEvent;
import io.opensphere.core.util.swing.input.model.PropertyChangeListener;

/**
 * A controller using an Integer model and JSpinner view.
 */
public class IntegerSpinnerController extends AbstractController<Integer, IntegerModel, JSpinner>
{
    /** The change listener. */
    private ChangeListener myChangeListener;

    /** The property change listener. */
    private PropertyChangeListener myPropertyChangeListener;

    /** The mouse wheel listener. */
    private MouseWheelListener myMouseWheelListener;

    /**
     * Gets the spinner model from the model.
     *
     * @param model the model
     * @return the spinner model
     */
    private static SpinnerNumberModel getSpinnerModel(IntegerModel model)
    {
        return new SpinnerNumberModel(model.get().intValue(), model.getMin(), model.getMax(), 1);
    }

    /**
     * Constructor.
     *
     * @param model The model
     */
    public IntegerSpinnerController(IntegerModel model)
    {
        super(model, new JSpinner(getSpinnerModel(model)));
    }

    @Override
    public void close()
    {
        super.close();
        getModel().removePropertyChangeListener(myPropertyChangeListener);
        getView().removeChangeListener(myChangeListener);
        getView().removeMouseWheelListener(myMouseWheelListener);
    }

    @Override
    public void open()
    {
        super.open();

        // Make typing in the text field actually work
        ((DefaultFormatter)((JSpinner.DefaultEditor)getView().getEditor()).getTextField().getFormatter())
        .setCommitsOnValidEdit(true);

        myPropertyChangeListener = e ->
        {
            if (e.getProperty() == PropertyChangeEvent.Property.VALIDATION_CRITERIA)
            {
                getView().setModel(getSpinnerModel(getModel()));
            }
        };
        getModel().addPropertyChangeListener(myPropertyChangeListener);

        myChangeListener = e -> handleViewChange();
        getView().addChangeListener(myChangeListener);

        myMouseWheelListener = new IntegerModelMouseWheelListener(getModel());
        getView().addMouseWheelListener(myMouseWheelListener);
    }

    @Override
    protected void updateModel()
    {
        getModel().set((Integer)getView().getValue());
    }

    @Override
    protected void updateViewValue()
    {
        getView().setValue(getModel().get());
    }
}

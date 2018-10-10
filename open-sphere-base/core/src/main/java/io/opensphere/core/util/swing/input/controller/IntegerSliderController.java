package io.opensphere.core.util.swing.input.controller;

import java.awt.event.MouseWheelListener;

import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JSlider;
import javax.swing.event.ChangeListener;

import io.opensphere.core.util.swing.input.model.IntegerModel;
import io.opensphere.core.util.swing.input.model.PropertyChangeEvent;
import io.opensphere.core.util.swing.input.model.PropertyChangeListener;

/**
 * A controller using an Integer model and JSlider view.
 */
public class IntegerSliderController extends AbstractController<Integer, IntegerModel, JSlider>
{
    /** The change listener. */
    private ChangeListener myChangeListener;

    /** The property change listener. */
    private PropertyChangeListener myPropertyChangeListener;

    /** The mouse wheel listener. */
    private MouseWheelListener myMouseWheelListener;

    /**
     * Gets the slider model from the model.
     *
     * @param model the model
     * @return the slider model
     */
    private static DefaultBoundedRangeModel getSliderModel(IntegerModel model)
    {
        return new DefaultBoundedRangeModel(model.get().intValue(), 0, model.getMin(), model.getMax());
    }

    /**
     * Constructor.
     *
     * @param model The model
     */
    public IntegerSliderController(IntegerModel model)
    {
        super(model, new JSlider(getSliderModel(model)));
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

        myPropertyChangeListener = e ->
        {
            if (e.getProperty() == PropertyChangeEvent.Property.VALIDATION_CRITERIA)
            {
                getView().setModel(getSliderModel(getModel()));
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
        getModel().set(Integer.valueOf(getView().getValue()));
    }

    @Override
    protected void updateViewValue()
    {
        getView().setValue(getModel().get().intValue());
    }
}

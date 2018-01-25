package io.opensphere.core.util.swing.input.controller;

import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import io.opensphere.core.util.swing.input.model.IntegerModel;

/**
 * MouseWheelListener for IntegerModel.
 */
public class IntegerModelMouseWheelListener implements MouseWheelListener
{
    /** The model. */
    private final IntegerModel myModel;

    /**
     * Constructor.
     *
     * @param model the model
     */
    public IntegerModelMouseWheelListener(IntegerModel model)
    {
        super();
        myModel = model;
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent evt)
    {
        int wheelRotation = -1 * evt.getWheelRotation();
        if (wheelRotation > 0)
        {
            int value = myModel.get().intValue() + 1;
            if (value <= myModel.getMax())
            {
                myModel.set(Integer.valueOf(value));
            }
        }
        else
        {
            int value = myModel.get().intValue() - 1;
            if (value >= myModel.getMin())
            {
                myModel.set(Integer.valueOf(value));
            }
        }
    }
}

package io.opensphere.core.util.swing.input.controller;

import java.awt.Color;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.SwingUtilities;

import com.bric.swing.ColorPicker;

import io.opensphere.core.util.swing.ColorCircleIcon;
import io.opensphere.core.util.swing.input.model.ColorModel;

/**
 * A controller using an Color model and JButton view.
 */
public class ColorButtonController extends AbstractController<Color, ColorModel, JButton>
{
    /** The action listener. */
    private ActionListener myActionListener;

    /**
     * Constructor.
     *
     * @param model The model
     */
    public ColorButtonController(ColorModel model)
    {
        super(model, new JButton());
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
        getView().setIcon(new ColorCircleIcon(getModel().get()));
        myActionListener = e ->
        {
            Color color = ColorPicker.showDialog(SwingUtilities.getWindowAncestor(getView()), "Select Color", getModel().get(),
                    true);
            if (color != null)
            {
                getModel().set(color);
            }
        };
        getView().addActionListener(myActionListener);
    }

    @Override
    protected void updateModel()
    {
    }

    @Override
    protected void updateViewValue()
    {
        getView().setIcon(new ColorCircleIcon(getModel().get()));
    }
}

package io.opensphere.core.util.swing.input.controller;

import io.opensphere.core.util.swing.input.model.IntegerModel;

/**
 * A controller using an Integer model and JTextField view.
 */
public class IntegerTextFieldController extends AbstractTextFieldController<Integer, IntegerModel>
{
    /**
     * Constructor.
     *
     * @param model The model
     */
    public IntegerTextFieldController(IntegerModel model)
    {
        super(model);
    }

    @Override
    protected Integer convertViewValueToModel(String viewValue)
    {
        return Integer.valueOf(viewValue);
    }

    @Override
    protected int getViewColumns()
    {
        return String.valueOf(getModel().getMax()).length();
    }
}

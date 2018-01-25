package io.opensphere.core.util.swing.input.controller;

import io.opensphere.core.util.swing.input.model.DoubleModel;

/**
 * A controller using an Integer model and JTextField view.
 */
public class DoubleTextFieldController extends AbstractTextFieldController<Double, DoubleModel>
{
    /**
     * Constructor.
     *
     * @param model The model
     */
    public DoubleTextFieldController(DoubleModel model)
    {
        super(model);
    }

    @Override
    protected Double convertViewValueToModel(String viewValue)
    {
        return Double.valueOf(viewValue);
    }

    @Override
    protected int getViewColumns()
    {
        return String.valueOf(getModel().getMax()).length();
    }

    @Override
    protected void invalidView()
    {
        getModel().set(Double.valueOf(Double.NaN));
    }
}

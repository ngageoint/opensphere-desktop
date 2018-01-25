package io.opensphere.core.util.swing.input.controller;

import javax.swing.JCheckBox;

import io.opensphere.core.util.swing.input.model.BooleanModel;

/**
 * A controller using an Boolean model and JCheckBox view.
 */
public class BooleanCheckBoxController extends BooleanAbstractButtonController
{
    /**
     * Constructor.
     *
     * @param model The model
     */
    public BooleanCheckBoxController(BooleanModel model)
    {
        super(model, new JCheckBox());
    }
}

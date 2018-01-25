package io.opensphere.core.util.swing.input.controller;

import io.opensphere.core.util.swing.IconButton;
import io.opensphere.core.util.swing.input.model.BooleanModel;

/**
 * A controller using an Boolean model and IconButton view.
 */
public class IconButtonController extends BooleanAbstractButtonController
{
    /**
     * Constructor.
     *
     * @param model The model.
     */
    public IconButtonController(BooleanModel model)
    {
        super(model, new IconButton());
    }

    @Override
    public void open()
    {
        super.open();

        ((IconButton)getView()).setHoldDelay(200);
    }

    @Override
    protected void updateModel()
    {
        getModel().set(Boolean.TRUE);
        getModel().set(Boolean.FALSE);
    }
}

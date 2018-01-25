package io.opensphere.core.util.swing.input.controller;

import io.opensphere.core.util.swing.GhostTextField;
import io.opensphere.core.util.swing.input.model.GhostTextModel;

/**
 * A controller using a ghost text model and {@link GhostTextField} view.
 */
public class GhostTextController extends TextController
{
    /**
     * Create the view.
     *
     * @param model The model.
     * @return The view.
     */
    private static GhostTextField getView(GhostTextModel model)
    {
        GhostTextField view = new GhostTextField(model.getGhostText());
        view.setColumns(model.getColumns());
        return view;
    }

    /**
     * Constructor.
     *
     * @param model The model.
     */
    public GhostTextController(GhostTextModel model)
    {
        super(model, getView(model));
    }

    @Override
    public GhostTextField getView()
    {
        return (GhostTextField)super.getView();
    }

    @Override
    protected void updateViewParameters()
    {
        super.updateViewParameters();
        getView().setGhostText(((GhostTextModel)getModel()).getGhostText());
    }
}

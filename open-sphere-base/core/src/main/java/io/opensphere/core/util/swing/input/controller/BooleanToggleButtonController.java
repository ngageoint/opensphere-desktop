package io.opensphere.core.util.swing.input.controller;

import io.opensphere.core.util.image.IconUtil;
import io.opensphere.core.util.swing.IconToggleButton;
import io.opensphere.core.util.swing.input.model.BooleanModel;

/**
 * A controller using an Boolean model and JToggleButton view.
 */
public class BooleanToggleButtonController extends BooleanAbstractButtonController
{
    /**
     * Constructor.
     *
     * @param model The model
     */
    public BooleanToggleButtonController(BooleanModel model)
    {
        super(model, new IconToggleButton());
    }

    @Override
    protected void updateViewLookAndFeel()
    {
        super.updateViewLookAndFeel();

        ViewSettings<Boolean> viewSettings = getViewSettings();
        if (viewSettings == null || viewSettings.getIconProvider() == null)
        {
            getView().setForeground(
                    getView().isSelected() ? IconUtil.ICON_SELECTION_FOREGROUND : IconUtil.DEFAULT_ICON_FOREGROUND);
        }
    }
}

package io.opensphere.core.util.swing.input.controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.function.Function;

import javax.swing.AbstractButton;

import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.swing.RadioButtonPanel;
import io.opensphere.core.util.swing.input.model.ChoiceModel;

/**
 * A controller using an choice model and JRadioButton view.
 *
 * @param <T> The type of the options
 */
public class ChoiceRadioButtonController<T> extends AbstractController<T, ChoiceModel<T>, RadioButtonPanel<T>>
{
    /** The action listener. */
    private ActionListener myActionListener;

    /**
     * Gets the options.
     *
     * @param <T> The type of the options
     * @param model The model
     * @param viewSettings The view settings
     * @return the options
     */
    private static <T> List<T> getOptions(ChoiceModel<T> model, ViewSettings<T> viewSettings)
    {
        T[] viewOptions = viewSettings != null ? viewSettings.getOptions() : null;
        return viewOptions != null ? New.list(viewOptions) : model.getOptions();
    }

    /**
     * Constructor.
     *
     * @param model The model
     * @param viewSettings The view settings
     */
    public ChoiceRadioButtonController(ChoiceModel<T> model, ViewSettings<T> viewSettings)
    {
        this(model, viewSettings, null);
    }

    /**
     * Constructor.
     *
     * @param model The model
     * @param viewSettings The view settings
     * @param buttonProvider The button provider
     */
    public ChoiceRadioButtonController(ChoiceModel<T> model, ViewSettings<T> viewSettings,
            Function<T, AbstractButton> buttonProvider)
    {
        super(model, new RadioButtonPanel<>(getOptions(model, viewSettings), model.get(), buttonProvider));
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
        myActionListener = new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                handleViewChange();
            }
        };
        getView().addActionListener(myActionListener);
    }

    @Override
    protected void updateModel()
    {
        getModel().set(getView().getSelection());
    }

    @Override
    protected void updateViewOptions()
    {
        // Not currently supported
    }

    @Override
    protected void updateViewValue()
    {
        getView().setSelection(getModel().get());
    }
}

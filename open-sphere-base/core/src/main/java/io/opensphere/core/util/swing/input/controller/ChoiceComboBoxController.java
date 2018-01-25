package io.opensphere.core.util.swing.input.controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JComboBox;

import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.swing.ListComboBoxModel;
import io.opensphere.core.util.swing.input.model.ChoiceModel;

/**
 * A controller using an choice model and JComboBox view.
 *
 * @param <T> The type of the options
 */
public class ChoiceComboBoxController<T> extends AbstractController<T, ChoiceModel<T>, JComboBox<T>>
{
    /** The view settings. */
    private final ViewSettings<T> myViewSettings;

    /** The action listener. */
    private ActionListener myActionListener;

    /**
     * Gets the combo box model for the given choice model.
     *
     * @param <T> The type of the options
     * @param model the choice model
     * @param viewSettings The view settings
     * @return the combo box model
     */
    private static <T> ListComboBoxModel<T> getComboBoxModel(ChoiceModel<T> model, ViewSettings<T> viewSettings)
    {
        List<T> options = getOptions(model, viewSettings);
        return options != null ? new ListComboBoxModel<>(options) : new ListComboBoxModel<T>();
    }

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
    public ChoiceComboBoxController(ChoiceModel<T> model, ViewSettings<T> viewSettings)
    {
        super(model, new JComboBox<>(getComboBoxModel(model, viewSettings)));
        myViewSettings = viewSettings;
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

    @SuppressWarnings("unchecked")
    @Override
    protected void updateModel()
    {
        getModel().set((T)getView().getSelectedItem());
    }

    @Override
    protected void updateViewOptions()
    {
        getView().setModel(getComboBoxModel(getModel(), myViewSettings));
    }

    @Override
    protected void updateViewValue()
    {
        getView().setSelectedItem(getModel().get());
    }
}

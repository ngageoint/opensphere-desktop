package io.opensphere.core.util.swing.input.controller;

import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import io.opensphere.core.util.swing.DocumentListenerAdapter;
import io.opensphere.core.util.swing.input.model.AbstractViewModel;

/**
 * Abstract controller that uses a JTextField view.
 *
 * @param <E> The type of value in the model.
 * @param <M> The type of the model.
 */
public abstract class AbstractTextFieldController<E, M extends AbstractViewModel<E>> extends AbstractController<E, M, JTextField>
{
    /** The document listener. */
    private DocumentListener myDocumentListener;

    /**
     * Constructor.
     *
     * @param model The model
     */
    public AbstractTextFieldController(M model)
    {
        super(model, new JTextField());
    }

    @Override
    public void close()
    {
        super.close();
        getView().getDocument().removeDocumentListener(myDocumentListener);
    }

    @Override
    public void open()
    {
        super.open();
        getView().setColumns(getViewColumns());
        myDocumentListener = new DocumentListenerAdapter()
        {
            @Override
            protected void updateAction(DocumentEvent e)
            {
                handleViewChange();
            }
        };
        getView().getDocument().addDocumentListener(myDocumentListener);
    }

    /**
     * Hook method to convert the view value to the model value.
     *
     * @param viewValue The view value.
     * @return The model value.
     */
    protected abstract E convertViewValueToModel(String viewValue);

    /**
     * Get the number of columns for the text field.
     *
     * @return The number of columns.
     */
    protected abstract int getViewColumns();

    /**
     * Handle the case where the view value is invalid and cannot be set in the
     * model.
     */
    protected void invalidView()
    {
        getModel().set(null);
        getModel().setValid(false, getView());
    }

    @Override
    protected void updateModel()
    {
        String viewValue = getView().getText();
        if (viewValue == null)
        {
            getModel().set(null);
        }
        else
        {
            try
            {
                getModel().set(convertViewValueToModel(viewValue));
            }
            catch (NumberFormatException e)
            {
                invalidView();
            }
        }
    }

    @Override
    protected void updateViewValue()
    {
        getView().setText(getModel().get() == null ? null : getModel().get().toString());
    }
}

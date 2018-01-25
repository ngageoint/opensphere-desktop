package io.opensphere.core.util.swing.input.controller;

import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;

import io.opensphere.core.util.swing.DocumentListenerAdapter;
import io.opensphere.core.util.swing.input.model.TextModel;

/**
 * A controller using an text model and JTextField view.
 */
public class TextController extends AbstractController<String, TextModel, JTextField>
{
    /** The document listener. */
    private DocumentListener myDocumentListener;

    /**
     * Constructor.
     *
     * @param model The model
     */
    public TextController(TextModel model)
    {
        super(model, new JTextField(model.getColumns()));
    }

    /**
     * Constructor.
     *
     * @param model The model
     * @param view The view
     */
    protected TextController(TextModel model, JTextField view)
    {
        super(model, view);
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
        myDocumentListener = new DocumentListenerAdapter()
        {
            @Override
            protected void updateAction(DocumentEvent e)
            {
                handleViewChange();
            }
        };
        getView().getDocument().addDocumentListener(myDocumentListener);

        if (getModel().getDocumentFilter() != null)
        {
            ((AbstractDocument)getView().getDocument()).setDocumentFilter(getModel().getDocumentFilter());
        }
    }

    @Override
    protected void updateModel()
    {
        getModel().set(getView().getText());
    }

    @Override
    protected void updateViewParameters()
    {
        getView().setColumns(getModel().getColumns());
    }

    @Override
    protected void updateViewValue()
    {
        getView().setText(getModel().get());
    }
}

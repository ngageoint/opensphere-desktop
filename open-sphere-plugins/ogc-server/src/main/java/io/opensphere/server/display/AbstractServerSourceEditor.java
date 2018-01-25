package io.opensphere.server.display;

import java.awt.Component;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JTextArea;

import io.opensphere.core.util.ChangeListener;
import io.opensphere.core.util.DefaultValidatorSupport;
import io.opensphere.core.util.ObservableValue;
import io.opensphere.core.util.ValidationStatus;
import io.opensphere.core.util.ValidatorSupport;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.swing.GridBagPanel;
import io.opensphere.core.util.swing.input.controller.ControllerFactory;
import io.opensphere.core.util.swing.input.model.ViewModel;
import io.opensphere.core.util.swing.input.model.WrappedModel;
import io.opensphere.mantle.datasources.IDataSource;

/**
 * Abstract implementation of common ServerSourceEditor functionality.
 *
 * @param <T> The type of server source.
 */
public abstract class AbstractServerSourceEditor<T extends IDataSource> extends GridBagPanel implements ServerSourceEditor
{
    /** The serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The GUI model. */
    private final WrappedModel<T> myModel;

    /** The UI validator. */
    private final transient DefaultValidatorSupport myUIValidator = new DefaultValidatorSupport(this);

    /** The full service validator. */
    private final transient ServiceValidator<? super T> myServiceValidator;

    /** Whether the source is new. */
    private boolean myIsNew;

    /** The other sources. */
    private transient List<IDataSource> myOtherSources = Collections.emptyList();

    /**
     * Creates a new JTextArea that looks like a wrapping JLabel.
     *
     * @param text the text
     * @return the new JTextArea
     */
    protected static JTextArea createTextArea(String text)
    {
        JTextArea textArea = new JTextArea(text);
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setBackground(null);
        textArea.setBorder(null);
        return textArea;
    }

    /**
     * Constructor.
     *
     * @param model the GUI model
     * @param serviceValidator the service validator
     */
    public AbstractServerSourceEditor(WrappedModel<T> model, ServiceValidator<? super T> serviceValidator)
    {
        super();
        myModel = model;
        myServiceValidator = serviceValidator;

        // Hook in the UI validator to the model
        myModel.addListener(new ChangeListener<IDataSource>()
        {
            @Override
            public void changed(ObservableValue<? extends IDataSource> observable, IDataSource oldValue, IDataSource newValue)
            {
                myUIValidator.setValidationResult(myModel.getValidationStatus(), myModel.getErrorMessage());
            }
        });

        // Set up the panel and styles
        init0();
        style("heading").setInsets(6, 0, 6, 0).fillHorizontal().setGridwidth(3);
        style("label").setInsets(0, 0, 6, 6).anchorWest();
        style("text").setInsets(0, 0, 6, 6).fillHorizontal();
        style("button").setInsets(0, 0, 6, 0);
    }

    @Override
    public boolean accept()
    {
        myModel.setValidating(true);
        myUIValidator.setValidationResult(myModel.getValidationStatus(), myModel.getErrorMessage());
        return myUIValidator.getValidationStatus() == ValidationStatus.VALID;
    }

    @Override
    public IDataSource getChangedSource()
    {
        myModel.applyChanges();
        return myModel.get();
    }

    @Override
    public Component getEditor()
    {
        return this;
    }

    @Override
    public ValidatorSupport getValidatorSupport()
    {
        return myUIValidator;
    }

    @Override
    public boolean isNewSource()
    {
        return myIsNew;
    }

    @Override
    public void openSource(IDataSource source, boolean isNew, List<IDataSource> otherSources)
    {
        myIsNew = isNew;
        myOtherSources = otherSources;
    }

    /**
     * Adds a row to the GUI for the given model.
     *
     * @param model the model
     */
    protected void addModelRow(ViewModel<?> model)
    {
        style("label", "text").addRow(createLabel(model), ControllerFactory.createComponent(model));
    }

    /**
     * Creates a label for the given model.
     *
     * @param model the model
     * @return the label
     */
    protected JLabel createLabel(ViewModel<?> model)
    {
        StringBuilder text = new StringBuilder(model.getName());
        text.append(':');
        if (model.isRequired())
        {
            text.append(" *");
        }
        return new JLabel(text.toString());
    }

    /**
     * Gets the model.
     *
     * @return the model
     */
    protected WrappedModel<T> getModel()
    {
        return myModel;
    }

    /**
     * Gets the other data sources' names.
     *
     * @return the other names
     */
    protected Collection<String> getOtherNames()
    {
        Collection<String> otherNames = New.list(myOtherSources.size());
        for (IDataSource otherSource : myOtherSources)
        {
            otherNames.add(otherSource.getName());
        }
        return otherNames;
    }

    /**
     * Gets the other data sources.
     *
     * @return the other data sources
     */
    protected List<IDataSource> getOtherSources()
    {
        return myOtherSources;
    }

    /**
     * Gets the ServiceValidator.
     *
     * @return the ServiceValidator
     */
    protected ServiceValidator<? super T> getServiceValidator()
    {
        return myServiceValidator;
    }
}

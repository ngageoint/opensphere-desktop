package io.opensphere.server.display;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import io.opensphere.core.util.ValidationStatus;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.swing.input.controller.ControllerFactory;
import io.opensphere.core.util.swing.input.model.ViewModel;
import io.opensphere.mantle.datasources.IDataSource;
import io.opensphere.mantle.datasources.impl.UrlDataSource;
import io.opensphere.server.display.model.UrlSourceModel;

/**
 * The server editor panel for a UrlDataSource.
 */
public final class UrlSourceEditor extends AbstractServerSourceEditor<UrlDataSource>
{
    /** The serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(UrlSourceEditor.class);

    /** The URL status widget. */
    private final UrlStatusWidget myUrlStatusWidget;

    /**
     * Constructor.
     *
     * @param model the GUI model
     * @param validator the service validator
     */
    public UrlSourceEditor(UrlSourceModel model, ServiceValidator<? super UrlDataSource> validator)
    {
        super(model, validator);

        // Support for full validation
        myUrlStatusWidget = new UrlStatusWidget();
        myUrlStatusWidget.addActionListener(e -> performFullValidation());
        model.getURL().addListener((obs, o, n) -> myUrlStatusWidget.setState(UrlStatusWidget.UNVALIDATED));

        buildGUI();
    }

    @Override
    public void openSource(IDataSource source, boolean isNew, List<IDataSource> otherSources)
    {
        super.openSource(source, isNew, otherSources);
        if (source instanceof UrlDataSource)
        {
            UrlDataSource serverSource = (UrlDataSource)source;
            getModel().getServerName().setDisallowedNames(getOtherNames());
            getModel().getURL().setDisallowedUrls(getOtherUrls());
            getModel().setValidating(false);
            getModel().set(serverSource);
            getModel().setEnabled(isNew || !serverSource.isActive());
        }
    }

    @Override
    protected UrlSourceModel getModel()
    {
        return (UrlSourceModel)super.getModel();
    }

    /**
     * Adds a row to the GUI for the given model.
     *
     * @param model the model
     */
    private void addUrlRow(ViewModel<?> model)
    {
        style("label", "text", "button").addRow(createLabel(model), ControllerFactory.createComponent(model), myUrlStatusWidget);
    }

    /**
     * Builds the GUI.
     */
    private void buildGUI()
    {
        // Add components (styles are defined in the base class)
        style("heading").addRow(createTextArea(getModel().getDescription()));
        addModelRow(getModel().getServerName());
        addUrlRow(getModel().getURL());
        style("label", "text").addRow(null, new JLabel(getModel().getUrlExample()));
        fillVerticalSpace();
    }

    /**
     * Gets the other data sources' URLs.
     *
     * @return the other URLs
     */
    private Collection<String> getOtherUrls()
    {
        Collection<String> otherUrls = New.list(getOtherSources().size());
        for (IDataSource otherSource : getOtherSources())
        {
            if (otherSource instanceof UrlDataSource)
            {
                otherUrls.add(((UrlDataSource)otherSource).getURL());
            }
        }
        return otherUrls;
    }

    /**
     * Performs full validation of the server URLs.
     */
    private void performFullValidation()
    {
        myUrlStatusWidget.setState(UrlStatusWidget.WAITING);

        final UrlDataSource editingSource = getModel().getEditingSource();

        new SwingWorker<Boolean, Void>()
        {
            @Override
            protected Boolean doInBackground()
            {
                boolean isValid = false;

                getServiceValidator().setParent(UrlSourceEditor.this);
                getServiceValidator().setSource(editingSource);
                String url = editingSource.getURL();
                if (StringUtils.isNotEmpty(url))
                {
                    isValid = getServiceValidator().getValidationStatus() == ValidationStatus.VALID;
                }

                return Boolean.valueOf(isValid);
            }

            @Override
            protected void done()
            {
                try
                {
                    boolean isValid = get().booleanValue();
                    myUrlStatusWidget.setState(isValid ? UrlStatusWidget.VALID : UrlStatusWidget.INVALID);
                    if (!isValid)
                    {
                        JOptionPane.showMessageDialog(UrlSourceEditor.this, getServiceValidator().getValidationMessage(),
                                "Validation Failed", JOptionPane.ERROR_MESSAGE);
                    }
                }
                catch (InterruptedException | ExecutionException e)
                {
                    LOGGER.error(e, e);
                }
            }
        }.execute();
    }
}

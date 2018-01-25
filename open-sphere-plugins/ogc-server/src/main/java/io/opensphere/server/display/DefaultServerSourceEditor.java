package io.opensphere.server.display;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingWorker;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import io.opensphere.core.util.ChangeListener;
import io.opensphere.core.util.ObservableValue;
import io.opensphere.core.util.ValidationStatus;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.swing.GridBagPanel;
import io.opensphere.core.util.swing.input.controller.ControllerFactory;
import io.opensphere.core.util.swing.input.model.ViewModel;
import io.opensphere.mantle.datasources.IDataSource;
import io.opensphere.server.display.model.DefaultServerSourceModel;
import io.opensphere.server.source.OGCServerSource;
import io.opensphere.server.source.ServerSource;

/**
 * The default server source editor panel.
 *
 * @param <T> The type of server source.
 */
public final class DefaultServerSourceEditor<T extends ServerSource> extends AbstractServerSourceEditor<T>
{
    /** The serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(DefaultServerSourceEditor.class);

    /** The URL status widget map. */
    private final Map<String, UrlStatusWidget> myUrlStatusWidgets;

    /**
     * Creates a horizontal separator with a label.
     *
     * @param text The label text
     * @return The separator panel
     */
    private static JPanel createSeparator(String text)
    {
        GridBagPanel panel = new GridBagPanel();
        panel.setInsets(0, 0, 0, 0).fillHorizontal().add(new JSeparator());
        panel.setInsets(0, 3, 0, 3).fillNone().add(new JLabel(text));
        panel.setInsets(0, 0, 0, 0).fillHorizontal().add(new JSeparator());
        return panel;
    }

    /**
     * Constructor.
     *
     * @param model the GUI model
     * @param validator the service validator
     */
    public DefaultServerSourceEditor(DefaultServerSourceModel<T> model, ServiceValidator<T> validator)
    {
        super(model, validator);

        // Support for full validation
        myUrlStatusWidgets = New.map();
        for (final String service : model.getBasicServices())
        {
            UrlStatusWidget widget = new UrlStatusWidget();
            widget.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    performFullValidation(service);
                }
            });
            myUrlStatusWidgets.put(service, widget);

            model.getUrl(service).addListener(newUrlModelChangeListener(service));
        }

        buildGUI();
    }

    /**
     * Open source.
     *
     * @param source The source.
     * @param isNew The is new.
     * @param otherSources The other sources.
     */
    @SuppressWarnings("unchecked")
    @Override
    public void openSource(IDataSource source, boolean isNew, List<IDataSource> otherSources)
    {
        super.openSource(source, isNew, otherSources);
        if (source instanceof ServerSource)
        {
            ServerSource serverSource = (ServerSource)source;
            getModel().getServerName().setDisallowedNames(getOtherNames());
            for (String service : getModel().getBasicServices())
            {
                getModel().getUrl(service).setDisallowedUrls(getOtherUrls(service));
            }
            getModel().setValidating(false);
            getModel().set((T)serverSource);
            getModel().setEnabled(isNew || !serverSource.isActive());
        }
    }

    /**
     * Get the model.
     *
     * @return The model.
     */
    @Override
    protected DefaultServerSourceModel<T> getModel()
    {
        return (DefaultServerSourceModel<T>)super.getModel();
    }

    /**
     * Adds a row to the GUI for the given model.
     *
     * @param model the model
     * @param service the service of the status widget
     */
    private void addUrlRow(ViewModel<?> model, String service)
    {
        style("label", "text", "button").addRow(createLabel(model), ControllerFactory.createComponent(model),
                myUrlStatusWidgets.get(service));
    }

    /**
     * Builds the GUI.
     */
    private void buildGUI()
    {
        // Add components (styles are defined in the base class)
        style("heading").addRow(createTextArea(getModel().getDescription()));
        style("heading").addRow(createSeparator("Basic Settings"));
        addModelRow(getModel().getServerName());
        for (String service : getModel().getBasicServices())
        {
            addUrlRow(getModel().getUrl(service), service);
        }
        if (!getModel().getAdvancedServices().isEmpty())
        {
            style("heading").addRow(createSeparator("Advanced Settings"));
            for (String service : getModel().getAdvancedServices())
            {
                addUrlRow(getModel().getUrl(service), service);
            }
        }
        fillVerticalSpace();
    }

    /**
     * Gets the other data sources' URLs for the given service.
     *
     * @param service the service
     * @return the other URLs
     */
    private Collection<String> getOtherUrls(String service)
    {
        Collection<String> otherUrls = New.list(getOtherSources().size());
        for (IDataSource otherSource : getOtherSources())
        {
            if (otherSource instanceof OGCServerSource)
            {
                String url = ((OGCServerSource)otherSource).getURL(service);
                if (!StringUtils.isBlank(url))
                {
                    otherUrls.add(url);
                }
            }
        }
        return otherUrls;
    }

    /**
     * Creates a new ModelChangeListener for a URL of the given service.
     *
     * @param service the service
     * @return the new ModelChangeListener
     */
    private ChangeListener<String> newUrlModelChangeListener(final String service)
    {
        return new ChangeListener<String>()
        {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue)
            {
                myUrlStatusWidgets.get(service).setState(UrlStatusWidget.UNVALIDATED);
            }
        };
    }

    /**
     * Performs full validation of the server URLs.
     *
     * @param service the service to validate
     */
    private void performFullValidation(final String service)
    {
        myUrlStatusWidgets.get(service).setState(UrlStatusWidget.WAITING);

        final T editingSource = getModel().getEditingSource();

        new SwingWorker<Boolean, Void>()
        {
            @Override
            protected Boolean doInBackground()
            {
                getServiceValidator().setParent(DefaultServerSourceEditor.this);
                getServiceValidator().setSource(editingSource);
                getServiceValidator().setService(service);
                return Boolean.valueOf(getServiceValidator().getValidationStatus() == ValidationStatus.VALID);
            }

            @Override
            protected void done()
            {
                try
                {
                    boolean isValid = get().booleanValue();
                    myUrlStatusWidgets.get(service).setState(isValid ? UrlStatusWidget.VALID : UrlStatusWidget.INVALID);
                    if (!isValid)
                    {
                        JOptionPane.showMessageDialog(DefaultServerSourceEditor.this,
                                getServiceValidator().getValidationMessage(), "Validation Failed", JOptionPane.ERROR_MESSAGE);
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

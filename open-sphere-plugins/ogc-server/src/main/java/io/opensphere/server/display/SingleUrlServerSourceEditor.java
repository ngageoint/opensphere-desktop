package io.opensphere.server.display;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import io.opensphere.core.util.ChangeListener;
import io.opensphere.core.util.ObservableValue;
import io.opensphere.core.util.ValidationStatus;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.swing.input.controller.ControllerFactory;
import io.opensphere.core.util.swing.input.model.ViewModel;
import io.opensphere.mantle.datasources.IDataSource;
import io.opensphere.server.display.model.SingleUrlServerSourceModel;
import io.opensphere.server.source.OGCServerSource;

/**
 * The single URL OGC server editor panel.
 */
public final class SingleUrlServerSourceEditor extends AbstractServerSourceEditor<OGCServerSource>
{
    /** The serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(SingleUrlServerSourceEditor.class);

    /** The URL status widget. */
    private final UrlStatusWidget myUrlStatusWidget;

    /**
     * Constructor.
     *
     * @param model the GUI model
     * @param validator the service validator
     */
    public SingleUrlServerSourceEditor(SingleUrlServerSourceModel model, ServiceValidator<? super OGCServerSource> validator)
    {
        super(model, validator);

        // Support for full validation
        myUrlStatusWidget = new UrlStatusWidget();
        myUrlStatusWidget.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                performFullValidation();
            }
        });
        model.getURL().addListener(newUrlModelChangeListener());

        buildGUI();
    }

    @Override
    public void openSource(IDataSource source, boolean isNew, List<IDataSource> otherSources)
    {
        super.openSource(source, isNew, otherSources);
        if (source instanceof OGCServerSource)
        {
            OGCServerSource serverSource = (OGCServerSource)source;
            getModel().getServerName().setDisallowedNames(getOtherNames());
            getModel().getURL().setDisallowedUrls(getOtherUrls());
            getModel().setValidating(false);
            getModel().set(serverSource);
            getModel().setEnabled(isNew || !serverSource.isActive());
        }
    }

    @Override
    protected SingleUrlServerSourceModel getModel()
    {
        return (SingleUrlServerSourceModel)super.getModel();
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
            if (otherSource instanceof OGCServerSource)
            {
                otherUrls.add(getModel().getSingleUrl((OGCServerSource)otherSource));
            }
        }
        return otherUrls;
    }

    /**
     * Creates a new ModelChangeListener for a URL of the given service.
     *
     * @return the new ModelChangeListener
     */
    private ChangeListener<String> newUrlModelChangeListener()
    {
        return new ChangeListener<String>()
        {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue)
            {
                myUrlStatusWidget.setState(UrlStatusWidget.UNVALIDATED);
            }
        };
    }

    /**
     * Performs full validation of the server URLs.
     */
    private void performFullValidation()
    {
        myUrlStatusWidget.setState(UrlStatusWidget.WAITING);

        final OGCServerSource editingSource = getModel().getEditingSource();

        new SwingWorker<Boolean, Void>()
        {
            @Override
            protected Boolean doInBackground()
            {
                boolean allValid = true;
                boolean anyValid = false;
                getServiceValidator().setParent(SingleUrlServerSourceEditor.this);
                getServiceValidator().setSource(editingSource);
                for (String service : new String[] { OGCServerSource.WMS_SERVICE, OGCServerSource.WFS_SERVICE,
                    OGCServerSource.WPS_SERVICE })
                {
                    if (allValid)
                    {
                        String url = editingSource.getURL(service);
                        if (StringUtils.isNotEmpty(url))
                        {
                            getServiceValidator().setService(service);
                            boolean isValid = getServiceValidator().getValidationStatus() == ValidationStatus.VALID;
                            allValid &= isValid;
                            anyValid |= isValid;
                        }
                    }
                }
                // They all have to be valid and there must be at least one
                return Boolean.valueOf(allValid && anyValid);
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
                        JOptionPane.showMessageDialog(SingleUrlServerSourceEditor.this,
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

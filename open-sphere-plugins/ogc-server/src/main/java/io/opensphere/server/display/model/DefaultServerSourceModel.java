package io.opensphere.server.display.model;

import java.util.Collection;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.ValidationStatus;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.core.util.swing.input.model.NameModel;
import io.opensphere.core.util.swing.input.model.UrlModel;
import io.opensphere.core.util.swing.input.model.WrappedModel;
import io.opensphere.server.source.ServerSource;

/**
 * The default GUI model for server source.
 *
 * @param <T> The actual type of the model.
 */
public abstract class DefaultServerSourceModel<T extends ServerSource> extends WrappedModel<T>
{
    /** The required services supported by this model. */
    private final Collection<? extends String> myBasicServices;

    /** The optional services supported by this model. */
    private final Collection<? extends String> myAdvancedServices;

    /** The server name. */
    private final NameModel myName = new NameModel();

    /** The URL map. */
    private final Map<String, UrlModel> myUrlMap = New.map();

    /** The error message. */
    private String myError;

    /**
     * Constructor.
     *
     * @param basicServices The basic services.
     * @param advancedServices The advanced services.
     */
    protected DefaultServerSourceModel(Collection<? extends String> basicServices, Collection<? extends String> advancedServices)
    {
        myBasicServices = New.unmodifiableCollection(Utilities.checkNull(basicServices, "basicServices"));
        myAdvancedServices = New.unmodifiableCollection(Utilities.checkNull(advancedServices, "advancedServices"));
        myName.setName("Title");
        addModel(myName);
        for (String service : CollectionUtilities.concat(myBasicServices, myAdvancedServices))
        {
            UrlModel urlModel = new UrlModel();
            urlModel.setName(service + " URL");
            urlModel.setRequired(false);
            myUrlMap.put(service, urlModel);
            addModel(urlModel);
        }
    }

    /**
     * Gets a collection of the basic services.
     *
     * @return the basic services
     */
    public Collection<? extends String> getBasicServices()
    {
        return myBasicServices;
    }

    /**
     * Gets the GUI model in its current state.
     *
     * @return the editing source
     */
    @SuppressWarnings("unchecked")
    public T getEditingSource()
    {
        T editingSource = null;
        if (get() != null)
        {
            editingSource = (T)get().createExportDataSource();
            updateDomainModel(editingSource);
        }
        return editingSource;
    }

    @Override
    public synchronized String getErrorMessage()
    {
        return myError != null ? myError : super.getErrorMessage();
    }

    /**
     * Gets the server name.
     *
     * @return the server name
     */
    public NameModel getServerName()
    {
        return myName;
    }

    /**
     * Gets the advanced services.
     *
     * @return advanced services
     */
    public Collection<? extends String> getAdvancedServices()
    {
        return myAdvancedServices;
    }

    /**
     * Gets the URL for the given service.
     *
     * @param service the service
     * @return the URL
     */
    public UrlModel getUrl(String service)
    {
        return myUrlMap.get(service);
    }

    @Override
    public ValidationStatus getValidationStatus()
    {
        myError = null;
        if (super.getValidationStatus() == ValidationStatus.VALID)
        {
            if (!isValidating())
            {
                return ValidationStatus.VALID;
            }

            boolean haveAUrl = false;
            Collection<? extends String> basicServices = getBasicServices();
            for (String service : basicServices)
            {
                haveAUrl |= StringUtils.isNotEmpty(getUrl(service).get());
            }
            if (!haveAUrl)
            {
                StringBuilder error = new StringBuilder("Please enter at least one URL (");
                StringUtilities.join(error, ", ", basicServices);
                error.append(')');
                myError = error.toString();
                return ValidationStatus.ERROR;
            }

            return ValidationStatus.VALID;
        }
        return ValidationStatus.ERROR;
    }

    @Override
    protected void updateDomainModel(ServerSource domainModel)
    {
        domainModel.setName(myName.get());
        for (String service : CollectionUtilities.concat(myBasicServices, myAdvancedServices))
        {
            domainModel.setURL(service, getUrl(service).get());
        }
    }

    @Override
    protected void updateViewModel(ServerSource domainModel)
    {
        myName.set(domainModel.getName());
        for (String service : CollectionUtilities.concat(myBasicServices, myAdvancedServices))
        {
            getUrl(service).set(domainModel.getURL(service));
        }
    }
}

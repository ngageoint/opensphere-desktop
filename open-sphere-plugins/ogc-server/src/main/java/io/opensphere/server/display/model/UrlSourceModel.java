package io.opensphere.server.display.model;

import io.opensphere.core.util.swing.input.model.NameModel;
import io.opensphere.core.util.swing.input.model.UrlModel;
import io.opensphere.core.util.swing.input.model.WrappedModel;
import io.opensphere.mantle.datasources.impl.UrlDataSource;

/**
 * The model used by the Add Server Editor.
 */
public class UrlSourceModel extends WrappedModel<UrlDataSource>
{
    /** The server name. */
    private final NameModel myName = new NameModel();

    /** The server URL. */
    private final UrlModel myURL = new UrlModel();

    /** The example URL. */
    private final String myExampleUrl;

    /**
     * Constructor.
     *
     * @param exampleUrl The example URL
     */
    public UrlSourceModel(String exampleUrl)
    {
        myExampleUrl = exampleUrl;
        myName.setName("Title");
        myURL.setName("URL");
        addModel(myName);
        addModel(myURL);
    }

    /**
     * Gets the GUI model in its current state as an UrlDataSource.
     *
     * @return the editing source
     */
    public UrlDataSource getEditingSource()
    {
        UrlDataSource editingSource = null;
        UrlDataSource thisSource = get();
        if (thisSource != null)
        {
            editingSource = thisSource.createExportDataSource();
            updateDomainModel(editingSource);
        }
        return editingSource;
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
     * Gets the server URL.
     *
     * @return the server URL
     */
    public UrlModel getURL()
    {
        return myURL;
    }

    /**
     * Gets the URL example text.
     *
     * @return the URL example text
     */
    public String getUrlExample()
    {
        return myExampleUrl;
    }

    @Override
    protected void updateDomainModel(UrlDataSource domainModel)
    {
        domainModel.setName(getServerName().get());
        domainModel.setBaseUrl(getURL().get());
    }

    @Override
    protected void updateViewModel(UrlDataSource domainModel)
    {
        getServerName().set(domainModel.getName());
        getURL().set(domainModel.getBaseUrl());
    }
}

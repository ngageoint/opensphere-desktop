package io.opensphere.server.display.model;

import org.apache.commons.lang3.StringUtils;

import io.opensphere.core.util.swing.input.model.NameModel;
import io.opensphere.core.util.swing.input.model.UrlModel;
import io.opensphere.core.util.swing.input.model.WrappedModel;
import io.opensphere.server.source.OGCServerSource;

/**
 * An abstract single server GUI model for an OGCServerSource.
 */
public abstract class SingleUrlServerSourceModel extends WrappedModel<OGCServerSource>
{
    /** The server name. */
    private final NameModel myName = new NameModel();

    /** The server URL. */
    private final UrlModel myURL = new UrlModel();

    /**
     * Gets the first non-empty W*S URL in the server source.
     *
     * @param source the server source
     * @return the first non-empty W*S URL
     */
    protected static String getFirstFullUrl(OGCServerSource source)
    {
        String urlToUse = "";
        String[] urls = new String[] { source.getWMSServerURL(), source.getWFSServerURL(), source.getWPSServerURL() };
        for (String url : urls)
        {
            if (StringUtils.isNotEmpty(url))
            {
                urlToUse = url;
                break;
            }
        }
        return urlToUse;
    }

    /**
     * Constructor.
     */
    public SingleUrlServerSourceModel()
    {
        myName.setName("Title");
        myURL.setName("URL");
        addModel(myName);
        addModel(myURL);
    }

    /**
     * Gets the GUI model in its current state as an OGCServerSource.
     *
     * @return the editing source
     */
    public OGCServerSource getEditingSource()
    {
        OGCServerSource editingSource = null;
        if (get() != null)
        {
            editingSource = get().createExportDataSource();
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
     * Gets the single URL in the server source.
     *
     * @param source the server source
     * @return the single URL
     */
    public abstract String getSingleUrl(OGCServerSource source);

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
    public abstract String getUrlExample();
}

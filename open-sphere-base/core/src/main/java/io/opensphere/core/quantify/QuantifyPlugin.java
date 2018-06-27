package io.opensphere.core.quantify;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.bitsys.common.http.util.UrlUtils;

import io.opensphere.core.PluginLoaderData;
import io.opensphere.core.Toolbox;
import io.opensphere.core.api.adapter.PluginAdapter;
import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.quantify.impl.DefaultQuantifyService;
import io.opensphere.core.quantify.impl.HttpQuantifySender;
import io.opensphere.core.quantify.impl.LoggingQuantifySender;
import io.opensphere.core.quantify.impl.QuantifyToolboxImpl;

/** A plugin used to collect metrics and send them to a remote endpoint. */
public class QuantifyPlugin extends PluginAdapter
{
    /** Logger reference. */
    private static final Logger LOG = Logger.getLogger(QuantifyPlugin.class);

    /** The service used to collect metrics. */
    private QuantifyService myService;

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.api.adapter.PluginAdapter#initialize(io.opensphere.core.PluginLoaderData,
     *      io.opensphere.core.Toolbox)
     */
    @Override
    public void initialize(PluginLoaderData plugindata, Toolbox toolbox)
    {
        Preferences preferences = toolbox.getPreferencesRegistry().getPreferences(QuantifyPlugin.class);
        String url = preferences.getString("quantify.url", null);

        QuantifySender sender;
        if (StringUtils.isNotBlank(url))
        {
            sender = new HttpQuantifySender(toolbox, UrlUtils.toUrl(url));
        }
        else
        {
            LOG.info("Unable to find preference 'quantify.url'. Writing metrics to log.");
            sender = new LoggingQuantifySender();
        }
        myService = new DefaultQuantifyService(sender);
        QuantifyToolbox quantifyToolbox = new QuantifyToolboxImpl(myService);
        toolbox.getPluginToolboxRegistry().registerPluginToolbox(quantifyToolbox);
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.api.adapter.PluginAdapter#close()
     */
    @Override
    public void close()
    {
        myService.close();
        super.close();
    }
}

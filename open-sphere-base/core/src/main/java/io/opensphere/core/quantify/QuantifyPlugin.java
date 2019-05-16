package io.opensphere.core.quantify;

import java.util.Set;
import java.util.stream.Collectors;

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
import io.opensphere.core.quantify.settings.QuantifyOptionsProvider;
import io.opensphere.core.quantify.settings.QuantifySettingsModel;
import io.opensphere.core.util.collections.New;

/** A plugin used to collect metrics and send them to a remote endpoint. */
public class QuantifyPlugin extends PluginAdapter
{
    /** Logger reference. */
    private static final Logger LOG = Logger.getLogger(QuantifyPlugin.class);

    /** The service used to collect metrics. */
    private QuantifyService myService;

    /** The object with which preferences are read. */
    private Preferences myPreferences;

    /** The options provider used to configure the quantify plugin. */
    private QuantifyOptionsProvider myOptionsProvider;

    /** The toolbox through which application state is accessed. */
    private Toolbox myToolbox;

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.api.adapter.PluginAdapter#initialize(io.opensphere.core.PluginLoaderData,
     *      io.opensphere.core.Toolbox)
     */
    @Override
    public void initialize(PluginLoaderData plugindata, Toolbox toolbox)
    {
        LOG.info("Initializing Quantify Plugin");
        myToolbox = toolbox;
        myPreferences = toolbox.getPreferencesRegistry().getPreferences(QuantifyPlugin.class);

        QuantifySettingsModel settingsModel = new QuantifySettingsModel(myPreferences);
        myOptionsProvider = new QuantifyOptionsProvider(settingsModel);

        myToolbox.getUIRegistry().getOptionsRegistry().addOptionsProvider(myOptionsProvider);

        String url = settingsModel.urlProperty().get();

        Set<QuantifySender> senders = New.set();
        if (StringUtils.isNotBlank(url))
        {
            senders.add(new HttpQuantifySender(toolbox, UrlUtils.toUrl(url)));
        }
        else
        {
            LOG.info(
                    "Unable to find preference 'quantify.url'. Configuring for logged output, and disabling metrics collection.");
            settingsModel.enabledProperty().set(false);
            settingsModel.captureToLogProperty().set(true);
        }

        if (settingsModel.captureToLogProperty().get())
        {
            senders.add(new LoggingQuantifySender());
        }

        // disable the service until the entire plugin's initialization routine
        // is completed:
        boolean enableUponCompletedInitialization = settingsModel.enabledProperty().get();
        settingsModel.enabledProperty().set(false);

        myService = new DefaultQuantifyService(senders, settingsModel.enabledProperty());

        settingsModel.captureToLogProperty()
                .addListener((obs, ov, nv) -> updateCaptureToLog(ov.booleanValue(), nv.booleanValue()));

        QuantifyToolbox quantifyToolbox = new QuantifyToolboxImpl(settingsModel, myService);
        toolbox.getPluginToolboxRegistry().registerPluginToolbox(quantifyToolbox);
        LOG.info("Quantify Plugin Initialization Complete, restoring enabled state.");
        settingsModel.enabledProperty().set(enableUponCompletedInitialization);
    }

    /**
     * @param originalValue the previous value of the capture-to-log setting.
     * @param newValue the new value of the capture-to-log setting.
     */
    private void updateCaptureToLog(boolean originalValue, boolean newValue)
    {
        if (originalValue != newValue)
        {
            synchronized (myService)
            {
                if (!newValue)
                {
                    myService.getSenders().removeAll(myService.getSenders().stream()
                            .filter(s -> s instanceof LoggingQuantifySender).collect(Collectors.toSet()));
                }
                else
                {
                    myService.getSenders().add(new LoggingQuantifySender());
                }
            }
        }
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
        myToolbox.getUIRegistry().getOptionsRegistry().removeOptionsProvider(myOptionsProvider);

        super.close();
    }
}

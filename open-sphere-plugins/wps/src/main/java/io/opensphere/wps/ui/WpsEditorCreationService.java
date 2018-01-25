package io.opensphere.wps.ui;

import javafx.concurrent.Service;
import javafx.concurrent.Task;

import io.opensphere.core.Toolbox;
import io.opensphere.wps.config.v2.ProcessConfig;
import io.opensphere.wps.config.v2.ProcessSetting;
import io.opensphere.wps.ui.detail.WpsProcessEditorFactory;
import io.opensphere.wps.ui.detail.WpsProcessForm;

/**
 * A service implementation in which a new WPS form is created in an asynchronous manner.
 */
public class WpsEditorCreationService extends Service<WpsProcessForm>
{
    /**
     * the toolbox through which application interactions occur.
     */
    private final Toolbox myToolbox;

    /**
     * the identifier of the server against which the query will be performed.
     */
    private final String myServerId;

    /**
     * the identifier of the process for which the form will be generated.
     */
    private final String myProcessId;

    /**
     * The factory that will create the process editor form.
     */
    private final WpsProcessEditorFactory myFactory;

    /** The process configuration. */
    private final ProcessConfig myConfig;

    /** The user process settings. */
    private final ProcessSetting myProcessSetting;

    /**
     * Creates a new WPS Form Creation Service.
     *
     * @param pToolbox the toolbox through which application interactions occur.
     * @param pFactory The factory that will create the process editor form.
     * @param pServerId the identifier of the server against which the query will be performed.
     * @param pProcessId the identifier of the process for which the form will be generated.
     * @param config The process configuration
     * @param processSetting The user process settings
     */
    public WpsEditorCreationService(Toolbox pToolbox, WpsProcessEditorFactory pFactory, String pServerId, String pProcessId,
            ProcessConfig config, ProcessSetting processSetting)
    {
        myToolbox = pToolbox;
        myFactory = pFactory;
        myServerId = pServerId;
        myProcessId = pProcessId;
        myConfig = config;
        myProcessSetting = processSetting;
    }

    /**
     * {@inheritDoc}
     *
     * @see javafx.concurrent.Service#createTask()
     */
    @Override
    protected Task<WpsProcessForm> createTask()
    {
        return new CreateWpsFormTask(myToolbox, myFactory, myServerId, myProcessId, myConfig, myProcessSetting);
    }
}

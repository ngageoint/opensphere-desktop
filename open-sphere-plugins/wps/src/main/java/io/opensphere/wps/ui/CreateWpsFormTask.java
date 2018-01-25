package io.opensphere.wps.ui;

import java.util.Collections;
import java.util.List;

import javafx.concurrent.Task;

import org.apache.log4j.Logger;

import io.opensphere.core.Toolbox;
import io.opensphere.core.cache.matcher.GeneralPropertyMatcher;
import io.opensphere.core.cache.matcher.PropertyMatcher;
import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.core.data.util.DefaultPropertyValueReceiver;
import io.opensphere.core.data.util.DefaultQuery;
import io.opensphere.core.data.util.QueryTracker;
import io.opensphere.wps.config.v2.ProcessConfig;
import io.opensphere.wps.config.v2.ProcessSetting;
import io.opensphere.wps.envoy.WpsPropertyDescriptors;
import io.opensphere.wps.envoy.WpsRequestType;
import io.opensphere.wps.layer.WpsDataTypeInfo;
import io.opensphere.wps.ui.detail.WpsProcessEditorFactory;
import io.opensphere.wps.ui.detail.WpsProcessForm;
import net.opengis.wps._100.ProcessDescriptionType;

/**
 * A task in which a new WPS form is created. The form is created based on a WPS Process Description, which may be either
 * internally locally cached, or requested from a remote server.
 */
public class CreateWpsFormTask extends Task<WpsProcessForm>
{
    /**
     * The <code>Log</code> instance used for logging.
     */
    private static final Logger LOG = Logger.getLogger(CreateWpsFormTask.class);

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
     * Creates a new WPS Form Task.
     *
     * @param pToolbox the toolbox through which application interactions occur.
     * @param pFactory The factory that will create the process editor form.
     * @param pServerId the identifier of the server against which the query will be performed.
     * @param pProcessId the identifier of the process for which the form will be generated.
     * @param config The process configuration
     * @param processSetting The user process settings
     */
    public CreateWpsFormTask(Toolbox pToolbox, WpsProcessEditorFactory pFactory, String pServerId, String pProcessId,
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
     * @see javafx.concurrent.Task#call()
     */
    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    @Override
    protected WpsProcessForm call() throws Exception
    {
        DataModelCategory category = new DataModelCategory(myServerId, WpsDataTypeInfo.SOURCE_PREFIX,
                WpsRequestType.DESCRIBE_PROCESS_TYPE.getValue());
        final DefaultPropertyValueReceiver<ProcessDescriptionType> valueReceiver = new DefaultPropertyValueReceiver<>(
                WpsPropertyDescriptors.WPS_DESCRIBE_PROCESS);

        GeneralPropertyMatcher<String> propertyMatcher = new GeneralPropertyMatcher<>(
                WpsPropertyDescriptors.PROCESS_ID_DESCRIPTOR, myProcessId);
        List<PropertyMatcher<String>> propertyMatchers = Collections.singletonList(propertyMatcher);
        DefaultQuery query = new DefaultQuery(category, Collections.singleton(valueReceiver), propertyMatchers, null);

        QueryTracker tracker = myToolbox.getDataRegistry().performQuery(query);
        tracker.awaitCompletion();

        List<ProcessDescriptionType> results = valueReceiver.getValues();
        if (results.size() > 1)
        {
            LOG.warn("The query produced more than one result, which will make a non-deterministic form.");
        }

        WpsProcessForm returnValue = null;
        for (ProcessDescriptionType result : results)
        {
            returnValue = myFactory.createForm(myServerId, result, myConfig, myProcessSetting);
        }

        return returnValue;
    }
}

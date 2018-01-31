package io.opensphere.osh.mantle;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.JMenuItem;

import org.apache.log4j.Logger;

import io.opensphere.core.Notify;
import io.opensphere.core.TimeManager.PrimaryTimeSpanChangeListener;
import io.opensphere.core.Toolbox;
import io.opensphere.core.animationhelper.TimeSpanGovernor;
import io.opensphere.core.animationhelper.TimeSpanGovernorManager;
import io.opensphere.core.control.action.ContextActionManager;
import io.opensphere.core.control.action.ContextMenuProvider;
import io.opensphere.core.data.QueryException;
import io.opensphere.core.event.DynamicService;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.model.time.TimeSpanList;
import io.opensphere.core.util.Service;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.io.CancellableInputStream;
import io.opensphere.core.util.lang.ExceptionUtilities;
import io.opensphere.core.util.lang.PhasedTaskCanceller;
import io.opensphere.core.util.swing.SwingUtilities;
import io.opensphere.mantle.GroupService;
import io.opensphere.mantle.MantleToolbox;
import io.opensphere.mantle.TypeService;
import io.opensphere.mantle.data.AbstractActivationListener;
import io.opensphere.mantle.data.ActivationListener;
import io.opensphere.mantle.data.ActivationState;
import io.opensphere.mantle.data.DataGroupActivationProperty;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataGroupInfo.DataGroupContextKey;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.impl.DefaultDataGroupInfo;
import io.opensphere.osh.aerialimagery.results.AerialPlatformResultHandler;
import io.opensphere.osh.model.OSHDataGroupInfo;
import io.opensphere.osh.model.OSHDataGroupInfoAssistant;
import io.opensphere.osh.model.OSHDataTypeInfo;
import io.opensphere.osh.model.Offering;
import io.opensphere.osh.model.Output;
import io.opensphere.osh.results.ResultHandler;
import io.opensphere.osh.results.features.FeatureResultHandler;
import io.opensphere.osh.results.video.VideoResultHandler;
import io.opensphere.osh.util.OSHQuerier;

/** The OpenSensorHub mantle controller. */
public class OSHMantleController extends DynamicService<String, Service>
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(OSHMantleController.class);

    /** The provider type. */
    private static final String PROVIDER = "OpenSensorHub";

    /** The toolbox. */
    private final Toolbox myToolbox;

    /** The mantle toolbox. */
    private final MantleToolbox myMantleToolbox;

    /** The querier. */
    private final OSHQuerier myQuerier;

    /** The time span governor manager. */
    private final TimeSpanGovernorManager<OSHDataGroupInfo> myGovernorManager = new TimeSpanGovernorManager<>(
            OSHTimeSpanGovernor::new);

    /** The group activation listener. */
    private final ActivationListener myActivationListener = new AbstractActivationListener()
    {
        @Override
        public void commit(DataGroupActivationProperty property, ActivationState state, PhasedTaskCanceller canceller)
        {
            handleGroupActivation(property, state);
        }
    };

    /** The data group assistant. */
    private final OSHDataGroupInfoAssistant myGroupAssistant;

    /** The results handlers. */
    private final List<ResultHandler> myHandlers = New.list(3);

    /**
     * Constructor.
     *
     * @param toolbox the toolbox
     */
    public OSHMantleController(Toolbox toolbox)
    {
        super(null);
        myToolbox = toolbox;
        myMantleToolbox = toolbox.getPluginToolboxRegistry().getPluginToolbox(MantleToolbox.class);
        myQuerier = new OSHQuerier(toolbox.getDataRegistry());
        myGroupAssistant = new OSHDataGroupInfoAssistant(toolbox);
        myHandlers.add(new FeatureResultHandler(toolbox, myMantleToolbox.getDataTypeController(), myQuerier));
        myHandlers.add(new VideoResultHandler(toolbox, myQuerier));
        myHandlers.add(new AerialPlatformResultHandler(toolbox, myQuerier));

        addService(createRootGroupService());
        addService(toolbox.getTimeManager().getPrimaryTimeSpanListenerService(new OSHPrimaryTimeSpanChangeListener()));
        addService(createContextMenuService());
    }

    /**
     * Adds the server.
     *
     * @param serverName the server name
     * @param baseUrl the server url
     * @return whether it was successful
     */
    public boolean addServer(String serverName, String baseUrl)
    {
        boolean success = false;
        try
        {
            List<Offering> offerings = myQuerier.getCapabilities(serverName, baseUrl);
            if (!offerings.isEmpty())
            {
                addDynamicService(serverName, createServerGroupService(serverName, baseUrl, offerings));
                success = true;
            }
        }
        catch (QueryException e)
        {
            LOGGER.error(e.getCause().getMessage());
            Notify.error("Failed to query OpenSensorHub server: " + e.getCause().getMessage());
        }
        return success;
    }

    /**
     * Removes the server.
     *
     * @param name the server name
     */
    public void removeServer(String name)
    {
        removeDynamicService(name);
    }

    /**
     * Creates the root group service.
     *
     * @return the service
     */
    private Service createRootGroupService()
    {
        return new Service()
        {
            /** The root group. */
            private volatile DataGroupInfo myRootGroup;

            @Override
            public void open()
            {
                myRootGroup = new DefaultDataGroupInfo(true, myToolbox, PROVIDER, PROVIDER);
                myMantleToolbox.getDataGroupController().addRootDataGroupInfo(myRootGroup, this);
            }

            @Override
            public void close()
            {
                myMantleToolbox.getDataGroupController().removeDataGroupInfo(myRootGroup, this);
            }
        };
    }

    /** Time change listener. */
    private class OSHPrimaryTimeSpanChangeListener implements PrimaryTimeSpanChangeListener
    {
        @Override
        public void primaryTimeSpansChanged(TimeSpanList spans)
        {
            requestDataForActiveLayers(spans);
        }

        @Override
        public void primaryTimeSpansCleared()
        {
        }
    }

    /**
     * Creates the context menu service.
     *
     * @return the service
     */
    private Service createContextMenuService()
    {
        return new Service()
        {
            /** The ContextMenuProvider. */
            private final ContextMenuProvider<DataGroupContextKey> myContextMenuProvider = new ContextMenuProvider<DataGroupContextKey>()
            {
                @Override
                public List<JMenuItem> getMenuItems(String contextId, DataGroupContextKey key)
                {
                    if (key.getDataType() instanceof OSHDataTypeInfo)
                    {
                        OSHDataTypeInfo dataType = (OSHDataTypeInfo)key.getDataType();
                        return Collections.singletonList(
                                SwingUtilities.newMenuItem("Show Data", e -> myGroupAssistant.showData(dataType.getParent())));
                    }
                    return null;
                }

                @Override
                public int getPriority()
                {
                    return 0;
                }
            };

            @Override
            public void open()
            {
                ContextActionManager manager = myToolbox.getUIRegistry().getContextActionManager();
                manager.registerContextMenuItemProvider(DataGroupInfo.ACTIVE_DATA_CONTEXT, DataGroupContextKey.class,
                        myContextMenuProvider);
            }

            @Override
            public void close()
            {
                ContextActionManager manager = myToolbox.getUIRegistry().getContextActionManager();
                manager.deregisterContextMenuItemProvider(DataGroupInfo.ACTIVE_DATA_CONTEXT, DataGroupContextKey.class,
                        myContextMenuProvider);
            }
        };
    }

    /**
     * Creates a service that manages the server data group and all its children.
     *
     * @param serverName the server name
     * @param url the server URL
     * @param offerings the offerings
     * @return the service
     */
    private GroupService createServerGroupService(String serverName, String url, Collection<? extends Offering> offerings)
    {
        DataGroupInfo rootGroup = myMantleToolbox.getDataGroupController().getDataGroupInfo(PROVIDER);
        DataGroupInfo serverGroup = new DefaultDataGroupInfo(false, myToolbox, PROVIDER, url, serverName);

        GroupService serverGroupService = new GroupService(rootGroup, serverGroup);
        for (Offering offering : offerings)
        {
            List<DataTypeInfo> dataTypes = New.list();
            try
            {
                List<Output> outputs = myQuerier.describeSensor(url, offering);

                Map<String, OSHDataTypeInfo> outputsDataTypes = New.map();
                for (ResultHandler handler : myHandlers)
                {
                    List<Output> handlerOutputs = handler.canHandle(outputs);

                    OSHDataTypeInfo dataType = null;
                    for (Output handlerOutput : handlerOutputs)
                    {
                        dataType = outputsDataTypes.get(handlerOutput.getName());
                        if (dataType != null)
                        {
                            break;
                        }
                    }

                    if (dataType == null && !handlerOutputs.isEmpty())
                    {
                        dataType = new OSHDataTypeInfo(myToolbox, PROVIDER, url, offering, handlerOutputs);
                        handler.initializeType(dataType);
                        dataTypes.add(dataType);

                        for (Output handlerOutput : handlerOutputs)
                        {
                            outputsDataTypes.put(handlerOutput.getName(), dataType);
                        }
                    }
                    else if (dataType != null)
                    {
                        dataType.getOutputs().removeAll(handlerOutputs);
                        dataType.getOutputs().addAll(handlerOutputs);
                        handler.initializeType(dataType);
                    }
                }
            }
            catch (QueryException e)
            {
                LOGGER.error(e);
                Notify.error("Failed to query OpenSensorHub server: " + e.getCause().getMessage());
            }

            if (!dataTypes.isEmpty())
            {
                DefaultDataGroupInfo layerGroup = new OSHDataGroupInfo(myToolbox, PROVIDER, url, offering);
                layerGroup.setAssistant(myGroupAssistant);
                layerGroup.activationProperty().addListener(myActivationListener);

                GroupService layerGroupService = new GroupService(serverGroup, layerGroup);
                serverGroupService.addService(layerGroupService);

                for (DataTypeInfo layerType : dataTypes)
                {
                    layerGroupService.addService(new TypeService(myToolbox, layerGroup, layerType));
                }
            }
        }
        return serverGroupService;
    }

    /**
     * Handles group de/activation.
     *
     * @param activationProperty the activation property
     * @param state the activation state
     */
    private void handleGroupActivation(DataGroupActivationProperty activationProperty, ActivationState state)
    {
        OSHDataGroupInfo dataGroup = (OSHDataGroupInfo)activationProperty.getDataGroup();
        Collection<OSHDataTypeInfo> dataTypes = dataGroup.getDataTypes();

        if (state == ActivationState.ACTIVE)
        {
            requestResultTemplates(dataTypes);
            requestInitialData(dataGroup);
        }

        // Let the handlers know about the activation
        for (OSHDataTypeInfo dataType : dataTypes)
        {
            Iterator<ResultHandler> iterator = getHandlers(dataType.getOutputs());

            while (iterator.hasNext())
            {
                ResultHandler handler = iterator.next();
                handler.handleGroupActivation(dataType, state);
            }
        }
    }

    /**
     * Gets the handlers for the outputs.
     *
     * @param outputs the outputs
     * @return the handler
     */
    private Iterator<ResultHandler> getHandlers(List<Output> outputs)
    {
        return myHandlers.stream().filter(h -> !h.canHandle(outputs).isEmpty()).iterator();
    }

    /**
     * Requests result templates for each data type.
     *
     * @param dataTypes the data types
     */
    private void requestResultTemplates(Collection<OSHDataTypeInfo> dataTypes)
    {
        for (OSHDataTypeInfo dataType : dataTypes)
        {
            for (ResultHandler handler : myHandlers)
            {
                List<Output> handlerOutputs = handler.canHandle(dataType.getOutputs());
                for (Output handlerOutput : handlerOutputs)
                {
                    if (dataType.getResultTemplate(handlerOutput) == null)
                    {
                        String property = handler.getQueryProperty(dataType.getOffering(), handlerOutput);
                        try
                        {
                            Output resultTemplate = myQuerier.getResultTemplate(dataType.getUrl(), dataType.getOffering(),
                                    property);
                            dataType.setResultTemplate(handlerOutput, resultTemplate);
                        }
                        catch (QueryException e)
                        {
                            LOGGER.error(e, e);
                        }
                    }
                }
            }
        }
    }

    /**
     * Requests initial data for the group. Currently it requests all data.
     *
     * @param dataGroup the data group
     */
    private void requestInitialData(OSHDataGroupInfo dataGroup)
    {
        TimeSpanGovernor governor = myGovernorManager.getGovernor(dataGroup);

        // Request all the data
        DataTypeInfo firstDataType = dataGroup.getMembers(false).iterator().next();
        TimeSpan requestSpan = firstDataType.getTimeExtents().getExtent();
        governor.requestData(requestSpan);
    }

    /**
     * Requests the time spans for all active layers.
     *
     * @param spans the time spans to request
     */
    private void requestDataForActiveLayers(Collection<? extends TimeSpan> spans)
    {
        List<TimeSpanGovernor> activeGovernors = myGovernorManager.findGovernors(g -> g.activationProperty().isActive());
        for (TimeSpanGovernor governor : activeGovernors)
        {
            for (TimeSpan span : spans)
            {
                governor.requestData(span);
            }
        }
    }

    /** OSH time span governor. */
    private class OSHTimeSpanGovernor extends TimeSpanGovernor
    {
        /** The data group. */
        private final OSHDataGroupInfo myDataGroup;

        /**
         * Constructor.
         *
         * @param dataGroup the data group
         */
        public OSHTimeSpanGovernor(OSHDataGroupInfo dataGroup)
        {
            super(dataGroup.getOffering().getSpan());
            myDataGroup = dataGroup;
        }

        @Override
        protected boolean performRequest(List<? extends TimeSpan> spans)
        {
            boolean requestComplete = true;
            for (TimeSpan span : spans)
            {
                requestComplete &= queryResults(myDataGroup, span);
            }
            return requestComplete;
        }

        /**
         * Queries results for the data group and time span.
         *
         * @param dataGroup the data group
         * @param span the time span to request
         * @return whether the request was completed
         */
        private boolean queryResults(OSHDataGroupInfo dataGroup, TimeSpan span)
        {
            boolean requestComplete = true;
            for (OSHDataTypeInfo dataType : dataGroup.getDataTypes())
            {
                if (!dataType.isNrtStreaming())
                {
                    for (ResultHandler handler : myHandlers)
                    {
                        List<Output> handlerOutputs = handler.canHandle(dataType.getOutputs());
                        List<CancellableInputStream> streams = New.list();
                        for (Output handlerOutput : handlerOutputs)
                        {
                            String property = handler.getQueryProperty(dataGroup.getOffering(), handlerOutput);
                            try
                            {
                                CancellableInputStream stream = myQuerier.getResults(dataGroup.getUrl(), dataGroup.getOffering(),
                                        property, span);
                                boolean success = !stream.isCancelled();
                                requestComplete &= success;
                                if (success)
                                {
                                    streams.add(stream);
                                }
                            }
                            catch (QueryException e)
                            {
                                LOGGER.error(e);
                                Notify.error("Failed to query OpenSensorHub server: "
                                        + ExceptionUtilities.getRootCause(e).getMessage());
                            }
                        }

                        try
                        {
                            handler.handleResults(dataType, handlerOutputs, streams);
                        }
                        catch (IOException e)
                        {
                            LOGGER.error(e);
                            Notify.error("Failed to read from OpenSensorHub server: "
                                    + ExceptionUtilities.getRootCause(e).getMessage());
                        }
                    }
                }
            }
            return requestComplete;
        }
    }
}

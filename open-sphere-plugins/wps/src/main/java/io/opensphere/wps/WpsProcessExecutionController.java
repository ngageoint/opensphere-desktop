package io.opensphere.wps;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import io.opensphere.core.Toolbox;
import io.opensphere.core.event.EventListenerService;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.ThreadUtilities;
import io.opensphere.mantle.MantleToolbox;
import io.opensphere.mantle.controller.event.impl.ActiveDataGroupsChangedEvent;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.util.MantleToolboxUtils;
import io.opensphere.server.services.ServerConfigEvent;
import io.opensphere.server.source.OGCServerSource;
import io.opensphere.wps.layer.WpsDataTypeInfo;
import io.opensphere.wps.request.WpsProcessConfiguration;

/**
 * A controller class responsible for maintaining individual executors, and
 * receiving notification of both process and server activation events. Upon
 * activation of a new server, the a new {@link WpsProcessExecutor} is created
 * and saved internally for future use. Upon de-activation of a server, the WPS
 * process executor for that server is removed from the controller. Upon
 * activation of a process, the process is executed against a given server using
 * a process executor.
 */
public class WpsProcessExecutionController extends EventListenerService
{
    /**
     * Contains the layer ids of the wps layers currently executing their
     * algorithms.
     */
    private final Set<String> myExecuting = Collections.synchronizedSet(New.set());

    /**
     * The dictionary of process executors, associated with the unique
     * identifier of the server for which they are configured.
     */
    private final Map<String, WpsProcessExecutor> myExecutors = Collections.synchronizedMap(New.map());

    /**
     * The toolbox through which application interaction occurs.
     */
    private final Toolbox myToolbox;

    /** The geometry controller. */
    private final WpsGeometryController myGeometryController;

    /**
     * Creates a new execution controller, responsible for maintaining
     * individual executors, and receiving notification of both process and
     * server activation events.
     *
     * @param pToolbox the toolbox through which application interaction occurs.
     */
    public WpsProcessExecutionController(Toolbox pToolbox)
    {
        super(pToolbox.getEventManager());
        myToolbox = pToolbox;
        myGeometryController = new WpsGeometryController(pToolbox);
        addService(myGeometryController);
        bindEvent(ServerConfigEvent.class, this::processServerConfigEvent);
        bindEvent(ActiveDataGroupsChangedEvent.class, this::processActiveDataGroupsChangedEvent);
    }

    /**
     * Executes the supplied process, and associates the results with the
     * supplied result type. This method delegates to the executor defined for
     * the configuration's specified server.
     *
     * @param pConfiguration the configuration of the process to execute.
     */
    public void execute(WpsProcessConfiguration pConfiguration)
    {
        WpsProcessExecutor executor = myExecutors.get(pConfiguration.getServerId());
        if (executor != null)
        {
            ThreadUtilities.runBackground(() ->
            {
                if (myExecuting.add(pConfiguration.getResultType().getTypeKey()))
                {
                    executor.execute(pConfiguration);
                    myExecuting.remove(pConfiguration.getResultType().getTypeKey());
                }
            });
        }
    }

    /**
     * Handles server activations / de-activations. Upon server activation, the
     * server's connection information is added to the connection parameter
     * dictionary, and upon de-activation, the server's connection information
     * is removed from the connection parameter dictionary.
     *
     * @param pEvent the event to process.
     */
    private void processServerConfigEvent(ServerConfigEvent pEvent)
    {
        String serverId = pEvent.getServer() == null ? null : pEvent.getServer().getServerId(OGCServerSource.WPS_SERVICE);
        if (StringUtils.isNotEmpty(serverId))
        {
            ThreadUtilities.runCpu(() ->
            {
                switch (pEvent.getEventAction())
                {
                    case ACTIVATE:
                        myExecutors.put(serverId, new WpsProcessExecutor(myToolbox, pEvent.getServer()));
                        break;
                    case DEACTIVATE:
                        myExecutors.remove(serverId);
                        break;
                    default:
                        break;
                }
            });
        }
    }

    /**
     * Handles layer activation/deactivation.
     *
     * @param pEvent the event
     */
    private void processActiveDataGroupsChangedEvent(ActiveDataGroupsChangedEvent pEvent)
    {
        ThreadUtilities.runCpu(() ->
        {
            executeAnyWpsLayers(pEvent.getActivatedGroups());

            myGeometryController.handleActiveDataGroupsChangedEvent(pEvent);
        });
    }

    /**
     * Executes any wps layers that may be in the collection of layers.
     *
     * @param layers The layer to check for wps layers, and execute them if any.
     */
    private void executeAnyWpsLayers(Collection<DataGroupInfo> layers)
    {
        for (DataGroupInfo dataGroupInfo : layers)
        {
            if (!dataGroupInfo.isRootNode() && dataGroupInfo.activationProperty().isActiveOrActivating())
            {
                for (DataTypeInfo dataTypeInfo : dataGroupInfo.getMembers(true))
                {
                    if (dataTypeInfo instanceof WpsDataTypeInfo
                            && dataTypeInfo.getParent().activationProperty().isActiveOrActivating())
                    {
                        // Check the data element count to prevent double
                        // requesting data when first creating the process
                        MantleToolbox mantleToolbox = MantleToolboxUtils.getMantleToolbox(myToolbox);
                        int elementCount = mantleToolbox.getDataElementCache().getElementCountForType(dataTypeInfo);
                        if (elementCount == 0)
                        {
                            WpsDataTypeInfo data = (WpsDataTypeInfo)dataTypeInfo;
                            execute(data.getProcessConfiguration());
                        }
                    }
                }
            }
        }
    }
}

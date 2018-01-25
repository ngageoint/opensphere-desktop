package io.opensphere.arcgis2.mantle;

import java.util.Collection;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import io.opensphere.arcgis2.esri.Response;
import io.opensphere.arcgis2.model.ArcGISDataGroupInfo;
import io.opensphere.core.Notify;
import io.opensphere.core.Notify.Method;
import io.opensphere.core.Toolbox;
import io.opensphere.core.data.QueryException;
import io.opensphere.core.event.DynamicService;
import io.opensphere.core.util.Service;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.MantleToolbox;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.element.MapDataElement;

/** The ArcGIS mantle controller. */
public class ArcGISMantleController extends DynamicService<String, Service> implements MantleController
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(ArcGISMantleController.class);

    /** The mantle toolbox. */
    private final MantleToolbox myMantleToolbox;

    /** The querier. */
    private final ArcGISLayerProvider myLayerProvider;

    /**
     * Constructor.
     *
     * @param toolbox the toolbox
     */
    public ArcGISMantleController(Toolbox toolbox)
    {
        super(null);
        myMantleToolbox = toolbox.getPluginToolboxRegistry().getPluginToolbox(MantleToolbox.class);
        myLayerProvider = new ArcGISLayerProvider(toolbox);
    }

    @Override
    public void addServerGroup(String serverName, String baseUrl, DataGroupInfo dataGroup)
    {
        addDynamicService(serverName, createServerGroupService(serverName, baseUrl, dataGroup));
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
            if (myLayerProvider.serverAdded(serverName, baseUrl, this) != null)
            {
                success = true;
            }
        }
        catch (QueryException e)
        {
            LOGGER.error(e, e);
            Throwable t = Utilities.getValue(e.getCause(), e);
            Notify.error("Failed to query ArcGIS server: " + t.getMessage(), Method.TOAST);
        }
        return success;
    }

    /**
     * Removes the server.
     *
     * @param name the server name
     * @param serverUrl The url of the server that was removed.
     */
    public void removeServer(String name, String serverUrl)
    {
        removeDynamicService(name);
        myLayerProvider.serverRemoved(serverUrl);
    }

    /**
     * Gets the current list of active layers.
     *
     * @param filter the data type filter
     * @return the active layers
     */
    public Collection<ArcGISDataGroupInfo> getActiveLayers(Predicate<DataTypeInfo> filter)
    {
        Predicate<DataTypeInfo> actualFilter = filter.and(t -> t.isVisible());
        Collection<DataGroupInfo> groups = New.list();
        myMantleToolbox.getDataGroupController().findDataGroupInfo(
            g -> g instanceof ArcGISDataGroupInfo && g.activationProperty().isActive() && g.hasMember(actualFilter, false),
            groups, false);
        return CollectionUtilities.filterDowncast(groups, ArcGISDataGroupInfo.class);
    }

    /**
     * Adds data elements for the response.
     *
     * @param dataType the data type
     * @param response the response
     * @return the element IDs
     */
    public long[] addDataElements(DataTypeInfo dataType, Response response)
    {
        long[] ids = {};
        Collection<MapDataElement> dataElements = response.getFeatures().stream()
                .map(f -> ArcGISDataElementHelper.createDataElement(dataType, f)).filter(e -> e != null)
                .collect(Collectors.toList());
        if (!dataElements.isEmpty())
        {
            if (dataType.getMetaDataInfo().getKeyCount() > 0)
            {
                myMantleToolbox.getDataTypeController().addDataType("Baba", "Ganoush", dataType, this);
            }

            ids = myMantleToolbox.getDataTypeController().addMapDataElements(dataType, null, null, dataElements, this);
        }
        return ids;
    }

    /**
     * Removes data elements for the IDs.
     *
     * @param dataType the data type
     * @param ids the element IDs
     */
    public void removeDataElements(DataTypeInfo dataType, long[] ids)
    {
        myMantleToolbox.getDataTypeController().removeDataElements(dataType, ids);
    }

    /**
     * Creates a service that manages the server data group and all its children.
     *
     * @param serverName the server name
     * @param url the server URL
     * @param serverGroup The server group.
     * @return the service
     */
    private Service createServerGroupService(String serverName, String url, DataGroupInfo serverGroup)
    {
        return new Service()
        {
            @Override
            public void open()
            {
                myMantleToolbox.getDataGroupController().addRootDataGroupInfo(serverGroup, this);
            }

            @Override
            public void close()
            {
                serverGroup.getMembers(true).forEach(t -> myMantleToolbox.getDataTypeController().removeDataType(t, this));
                myMantleToolbox.getDataGroupController().removeDataGroupInfo(serverGroup, this);
            }
        };
    }
}

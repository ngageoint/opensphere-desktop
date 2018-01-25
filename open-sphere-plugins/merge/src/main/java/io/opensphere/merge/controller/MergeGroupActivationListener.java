package io.opensphere.merge.controller;

import java.util.Iterator;

import io.opensphere.core.data.DataRegistry;
import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.core.data.util.SimpleQuery;
import io.opensphere.core.util.lang.PhasedTaskCanceller;
import io.opensphere.mantle.controller.DataTypeController;
import io.opensphere.mantle.data.AbstractActivationListener;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.merge.model.MergedDataRow;

/**
 * Listens for when a Merged layer is activated. Once activated it will query
 * the data registry to get the merged data, and add them to the data group.
 */
public class MergeGroupActivationListener extends AbstractActivationListener
{
    /**
     * The {@link DataRegistry} that contains the merged data.
     */
    private final DataRegistry myRegistry;

    /**
     * Used to add data elements.
     */
    private final DataTypeController myTypeController;

    /**
     * Constructs a new activation listener.
     *
     * @param registry The {@link DataRegistry} that contains the merged data.
     * @param typeController Used to add data elements.
     */
    public MergeGroupActivationListener(DataRegistry registry, DataTypeController typeController)
    {
        myRegistry = registry;
        myTypeController = typeController;
    }

    @Override
    public void handleCommit(boolean active, DataGroupInfo dgi, PhasedTaskCanceller canceller)
    {
        super.handleCommit(active, dgi, canceller);
        if (active)
        {
            Iterator<DataTypeInfo> mergedLayers = dgi.getMembers(false).iterator();
            if (mergedLayers.hasNext())
            {
                DataTypeInfo mergedLayer = mergedLayers.next();
                DataModelCategory category = DataRegistryUtils.getInstance().getMergeDataCategory(mergedLayer.getTypeKey());
                SimpleQuery<MergedDataRow> simpleQuery = new SimpleQuery<>(category, DataRegistryUtils.MERGED_PROP_DESCRIPTOR);
                myRegistry.performLocalQuery(simpleQuery);
                if (simpleQuery.getResults() != null && !simpleQuery.getResults().isEmpty())
                {
                    myTypeController.addDataElements(new MergeDataElementProvider(mergedLayer, simpleQuery.getResults()), null,
                            null, this);
                }
            }
        }
    }
}

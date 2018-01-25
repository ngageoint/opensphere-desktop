package io.opensphere.merge.controller;

import java.util.List;
import java.util.function.Consumer;

import org.apache.commons.lang3.StringUtils;

import io.opensphere.core.Notify;
import io.opensphere.core.Notify.Method;
import io.opensphere.core.Toolbox;
import io.opensphere.core.cache.SimpleSessionOnlyCacheDeposit;
import io.opensphere.core.data.DataRegistry;
import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.core.datafilter.columns.ColumnMappingController;
import io.opensphere.core.order.impl.DefaultOrderCategory;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.Pair;
import io.opensphere.core.util.lang.PhasedTaskCanceller;
import io.opensphere.core.util.lang.ThreadUtilities;
import io.opensphere.core.util.taskactivity.CancellableTaskActivity;
import io.opensphere.mantle.MantleToolbox;
import io.opensphere.mantle.controller.DataGroupController;
import io.opensphere.mantle.controller.DataTypeController;
import io.opensphere.mantle.crust.AbstractMantleController;
import io.opensphere.mantle.data.ActivationState;
import io.opensphere.mantle.data.DataGroupActivationProperty;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.MetaDataInfo;
import io.opensphere.mantle.data.impl.DefaultDataTypeInfo;
import io.opensphere.mantle.data.util.DataElementLookupUtils;
import io.opensphere.merge.algorithm.MergeData;
import io.opensphere.merge.model.MergeModel;
import io.opensphere.merge.model.MergedDataRow;

/**
 * The merge controller, actually performs the merge.
 */
public class MergeController extends AbstractMantleController implements Consumer<DataGroupInfo>
{
    /**
     * The root group name for the merge layers.
     */
    private static final String ourRootGroupName = "Merged";

    /**
     * Adds merged data elements to the layer when it is activated.
     */
    private final MergeGroupActivationListener myActivationListener;

    /**
     * Used to store the merged data.
     */
    private final DataRegistry myDataRegistry;

    /**
     * Gets the data elements for layers.
     */
    private final DataElementLookupUtils myElementProvider;

    /**
     * The mantle group controller.
     */
    private final DataGroupController myGroupController;

    /**
     * Prevents executing checkAssociations if we are the ones that changed the
     * model.
     */
    private boolean myIsCheckingAssociations;

    /**
     * Contains the column mapping configurations.
     */
    private final ColumnMappingController myMapper;

    /**
     * Provides metadata info for the merged layers.
     */
    private final MetaDataInfoProvider myMetaDataInfoProvider;

    /**
     * The merge model.
     */
    private MergeModel myModel;

    /**
     * The system toolbox.
     */
    private final Toolbox myToolbox;

    /**
     * The type controller.
     */
    private final DataTypeController myTypeController;

    /**
     * Constructs a new column mapping support.
     *
     * @param toolbox The system toolbox.
     */
    public MergeController(Toolbox toolbox)
    {
        super(toolbox, ourRootGroupName);

        myToolbox = toolbox;
        MantleToolbox mantle = myToolbox.getPluginToolboxRegistry().getPluginToolbox(MantleToolbox.class);
        myElementProvider = mantle.getDataElementLookupUtils();
        myTypeController = mantle.getDataTypeController();
        myGroupController = mantle.getDataGroupController();
        myMapper = myToolbox.getDataFilterRegistry().getColumnMappingController();
        myDataRegistry = myToolbox.getDataRegistry();
        myMetaDataInfoProvider = new MetaDataInfoProvider(myMapper);
        myActivationListener = new MergeGroupActivationListener(myDataRegistry, myTypeController);

        open();
    }

    /**
     * Gets the model.
     *
     * @return The model.
     */
    public synchronized MergeModel getModel()
    {
        return myModel;
    }

    /**
     * Performs the merge as described in the model.
     */
    public void performMerge()
    {
        ThreadUtilities.runCpu(() ->
        {
            performMergeBackground();
        });
    }

    @Override
    public void accept(DataGroupInfo group)
    {
        // No need to do anything on delete.
    }

    /**
     * Sets the current model to user to perform a merge.
     *
     * @param model The model to use to perform the merge.
     */
    public synchronized void setModel(MergeModel model)
    {
        myModel = model;
        fillDefaultName();
        checkAssociations();
        myModel.getUserMessage().addListener((prop, old, newValue) ->
        {
            checkAssociations();
        });
    }

    @Override
    protected void handleGroupActivation(DataGroupActivationProperty activationProperty, ActivationState state,
            PhasedTaskCanceller canceller)
    {
    }

    /**
     * Checks to see if the layers wanting to be merged have column associations
     * with eachother. If not this will populate a user message within the
     * model.
     */
    private void checkAssociations()
    {
        if (!myIsCheckingAssociations)
        {
            myIsCheckingAssociations = true;
            try
            {
                List<Pair<String, List<String>>> layers = New.list();
                for (DataTypeInfo layer : myModel.getLayers())
                {
                    layers.add(new Pair<>(layer.getTypeKey(), layer.getMetaDataInfo().getKeyNames()));
                }

                if (myMapper.getDefinedColumns(layers).isEmpty())
                {
                    myModel.getUserMessage().set("These layers have no associated columns.");
                }
                else
                {
                    myModel.getUserMessage().set("");
                }
            }
            finally
            {
                myIsCheckingAssociations = false;
            }
        }
    }

    /**
     * Create the layer, activates it and adds the data to it.
     *
     * @param data The data to belong to the layer.
     */
    private void createLayerAndActivate(List<MergedDataRow> data)
    {
        MetaDataInfo metadataInfo = myMetaDataInfoProvider.createMetaDataInfo(myModel.getLayers(), data);

        DataGroupInfo dataGroup = add1stLevelLayer(myModel.getNewLayerName().get(), myModel.getNewLayerName().get(),
                myModel.getNewLayerName().get(), DefaultOrderCategory.FEATURE_CATEGORY, metadataInfo, this);
        DefaultDataTypeInfo dataType = (DefaultDataTypeInfo)dataGroup.getMembers(false).iterator().next();
        dataType.setFilterable(false);
        dataType.setVisible(true, this);
        dataGroup.activationProperty().addListener(myActivationListener);
        depositMergeData(dataType.getTypeKey(), data);
        // We must deactivate then activate this data group in order for the
        // activation listener to be executed
        // if this data groups activation state has been saved to active
        // already.
        dataGroup.activationProperty().setActive(false);
        dataGroup.activationProperty().setActive(true);
    }

    /**
     * Deposits the merged data into the data registry.
     *
     * @param layerId The id of the merged layer.
     * @param data The data to deposit.
     */
    private void depositMergeData(String layerId, List<MergedDataRow> data)
    {
        DataModelCategory category = DataRegistryUtils.getInstance().getMergeDataCategory(layerId);
        SimpleSessionOnlyCacheDeposit<MergedDataRow> deposit = new SimpleSessionOnlyCacheDeposit<>(category,
                DataRegistryUtils.MERGED_PROP_DESCRIPTOR, data);
        myDataRegistry.addModels(deposit);
    }

    /**
     * Puts in a default name for the merged layer.
     */
    private void fillDefaultName()
    {
        if (StringUtils.isEmpty(myModel.getNewLayerName().get()))
        {
            StringBuilder defaultName = new StringBuilder();
            int index = 0;
            for (DataTypeInfo dataType : myModel.getLayers())
            {
                defaultName.append(dataType.getDisplayName());
                index++;
                if (index < myModel.getLayers().size())
                {
                    defaultName.append(' ');
                }
            }

            String defaultNameString = defaultName.toString();
            DataGroupInfo existingGroup = myGroupController.getDataGroupInfo(defaultNameString);
            int nameAppend = 1;
            String theName = defaultNameString;
            while (existingGroup != null)
            {
                StringBuilder builder = new StringBuilder(defaultNameString);
                builder.append(' ');
                builder.append(nameAppend);
                theName = builder.toString();
                nameAppend++;
                existingGroup = myGroupController.getDataGroupInfo(builder.toString());
            }

            myModel.getNewLayerName().set(theName);
        }
    }

    /**
     * Performs the merge expects to be called on background thread.
     */
    private synchronized void performMergeBackground()
    {
        try (CancellableTaskActivity ta = CancellableTaskActivity
                .createActive("Merging selected layers to " + myModel.getNewLayerName().get()))
        {
            myToolbox.getUIRegistry().getMenuBarRegistry().addTaskActivity(ta);
            MergeData merger = new MergeData();

            ColumnMappingSupport support = new ColumnMappingSupport(myElementProvider, myMapper);
            merger.setSupp(support);
            merger.getSrc().addAll(myModel.getLayers());
            merger.merge();

            // in case of error, punt!
            String errorMsg = merger.getErrorMessage();
            if (errorMsg != null)
            {
                Notify.info(errorMsg, Method.POPUP);
                return;
            }

            List<MergedDataRow> data = merger.getAllData();
            if (!ta.isCancelled() && !data.isEmpty())
            {
                createLayerAndActivate(data);
            }
        }
    }
}

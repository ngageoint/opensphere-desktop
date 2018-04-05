package io.opensphere.analysis.base.controller;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import io.opensphere.analysis.base.model.BinType;
import io.opensphere.analysis.base.model.DataType;
import io.opensphere.analysis.base.model.SettingsModel;
import io.opensphere.analysis.base.model.SortMethod;
import io.opensphere.analysis.base.model.ToolModels;
import io.opensphere.analysis.base.model.UIBin;
import io.opensphere.analysis.binning.algorithm.DataElementBinner;
import io.opensphere.analysis.binning.bins.Bin;
import io.opensphere.analysis.binning.criteria.BinCriteriaElement;
import io.opensphere.analysis.binning.criteria.CriteriaType;
import io.opensphere.analysis.binning.criteria.RangeCriteria;
import io.opensphere.analysis.binning.criteria.TimeBinType;
import io.opensphere.analysis.binning.criteria.TimeCriteria;
import io.opensphere.analysis.binning.criteria.UniqueCriteria;
import io.opensphere.core.TimeManager.PrimaryTimeSpanChangeListener;
import io.opensphere.core.Toolbox;
import io.opensphere.core.event.EventListenerService;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.util.ListDataEvent;
import io.opensphere.core.util.ListDataListener;
import io.opensphere.core.util.ThreadConfined;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.collections.TroveUtilities;
import io.opensphere.core.util.fx.FXUtilities;
import io.opensphere.core.util.lang.NamedThreadFactory;
import io.opensphere.core.util.lang.ThreadUtilities;
import io.opensphere.mantle.MantleToolbox;
import io.opensphere.mantle.controller.event.AbstractDataTypeControllerEvent;
import io.opensphere.mantle.controller.event.impl.DataElementsAddedEvent;
import io.opensphere.mantle.controller.event.impl.DataElementsRemovedEvent;
import io.opensphere.mantle.data.AbstractDataTypeInfoChangeEvent;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.element.DataElement;
import io.opensphere.mantle.data.element.event.consolidated.AbstractConsolidatedDataElementChangeEvent;
import io.opensphere.mantle.data.element.event.consolidated.ConsolidatedDataElementVisibilityChangeEvent;
import io.opensphere.mantle.data.event.DataTypeInfoColorChangeEvent;
import io.opensphere.mantle.data.event.DataTypeInfoMetaDataKeyAddedChangeEvent;
import io.opensphere.mantle.data.event.DataTypeInfoMetaDataKeyRemovedChangeEvent;
import io.opensphere.mantle.data.util.DataElementLookupUtils;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.paint.Color;

/** Abstract tool controller. */
public abstract class AbstractToolController extends EventListenerService
{
    /** The executor for querying mantle data and binning. */
    private static final ExecutorService DATA_EXECUTOR = Executors
            .newSingleThreadExecutor(new NamedThreadFactory("AbstractToolController"));

    /** The toolbox. */
    private final Toolbox myToolbox;

    /** The mantle toolbox. */
    private final MantleToolbox myMantleToolbox;

    /** The model. */
    private final ToolModels myModel;

    /** The binner. */
    @ThreadConfined("AbstractToolController")
    private DataElementBinner myBinner;

    /** The binner listener, converts bin results to the UI model. */
    private final ListDataListener<Bin<DataElement>> myBinnerListener = new ListDataListener<Bin<DataElement>>()
    {
        @Override
        public void elementsAdded(ListDataEvent<Bin<DataElement>> e)
        {
            Comparator<Bin<DataElement>> comparator = getSettingsModel().sortMethodProperty().get().getComparator();
            List<UIBin> added = CollectionUtilities
                    .getList(CollectionUtilities.filterDowncast(e.getChangedElements(), UIBin.class));
            Collections.sort(added, comparator);
            FXUtilities.runOnFXThreadAndWait(
                    () -> CollectionUtilities.addSorted(getModel().getDataModel().getBins(), added, comparator));
        }

        @Override
        public void elementsChanged(ListDataEvent<Bin<DataElement>> e)
        {
            // Don't need to handle change events because the UIBin handles
            // updating the count itself
        }

        @Override
        public void elementsRemoved(ListDataEvent<Bin<DataElement>> e)
        {
            Set<Bin<DataElement>> removed = New.set(e.getChangedElements());
            FXUtilities.runOnFXThreadAndWait(() -> getModel().getDataModel().getBins().removeAll(removed));
        }
    };

    /**
     * Constructor.
     *
     * @param toolbox The toolbox
     * @param model The model
     * @param toolName The tool name
     */
    public AbstractToolController(Toolbox toolbox, ToolModels model, String toolName)
    {
        super(toolbox.getEventManager());
        myToolbox = toolbox;
        myMantleToolbox = toolbox.getPluginToolboxRegistry().getPluginToolbox(MantleToolbox.class);
        myModel = model;
        bindModelFX(model.getSettingsModel().lockedProperty(), (obs, o, n) -> handleLockedChange(n));
        bindModelFX(model.getSettingsModel().currentLayerProperty(), (obs, o, n) -> handleLayerChange(n));
        bindModelFX(model.getSettingsModel().selectedColumnProperty(), (obs, o, n) -> handleSelectedColumnChange(n));
        bindModelFX(model.getSettingsModel().dataTypeProperty(), (obs, o, n) -> handleDataTypeChange(n));
        bindModelFX(model.getSettingsModel().numericBinTypeProperty(), (obs, o, n) -> handleNumericBinTypeChange(n));
        bindModelFX(model.getSettingsModel().timeBinTypeProperty(), (obs, o, n) -> handleTimeBinTypeChange(n));
        bindModelFX(model.getSettingsModel().allTimeProperty(), (obs, o, n) -> reloadData());
        bindModelFX(model.getSettingsModel().binWidthProperty(), (obs, o, n) -> handleBinWidthChange(n));
        bindModelFX(model.getSettingsModel().showEmptyBinsProperty(), (obs, o, n) -> handleShowEmptyChange(n));
        bindModelFX(model.getSettingsModel().showNABinProperty(), (obs, o, n) -> handleShowNAChange(n));
        bindModelFX(model.getSettingsModel().sortMethodProperty(), (obs, o, n) -> handleSortMethodChange(n));
        bindEvent(AbstractDataTypeInfoChangeEvent.class, this::handleDataTypeEvent);
        bindEvent(AbstractDataTypeControllerEvent.class, this::handleDataTypeControllerEvent);
        bindEvent(AbstractConsolidatedDataElementChangeEvent.class, this::handleDataElementEvent);
        addService(toolbox.getTimeManager()
                .getPrimaryTimeSpanListenerService(PrimaryTimeSpanChangeListener.newChangedListener(spans ->
                {
                    if (!getModel().getSettingsModel().allTimeProperty().get())
                    {
                        reloadData();
                    }
                })));
        addService(new SettingsModelPersister(model.getSettingsModel(), toolbox.getPreferencesRegistry(), toolName));
        model.getActionModel().setBinSelectionListener(this::handleBinSelected);
    }

    @Override
    public void open()
    {
        super.open();

        // Set some initial state after listeners have been added
        FXUtilities.runOnFXThreadAndWait(() ->
        {
            handleLockedChange(Boolean.valueOf(getSettingsModel().lockedProperty().get()));
            String selectedColumn = getInitialColumn();
            if (selectedColumn != null)
            {
                getSettingsModel().selectedColumnProperty().setValue(selectedColumn);
            }
        });
    }

    /**
     * Gets the initial selected column.
     *
     * @return the column name or null
     */
    private String getInitialColumn()
    {
        DataTypeInfo currentLayer = getSettingsModel().currentLayerProperty().get();
        return currentLayer != null ? currentLayer.getMetaDataInfo().getTimeKey() : null;
    }

    /**
     * Handles a change in the locked property.
     *
     * @param locked whether the layer is locked
     */
    protected void handleLockedChange(Boolean locked)
    {
        // Attach/detach the tool's layer property to the common layer property
        ObjectProperty<DataTypeInfo> toolLayer = getSettingsModel().currentLayerProperty();
        ObjectProperty<DataTypeInfo> commonLayer = getSettingsModel().getCommonSettings().currentLayerProperty();
        if (locked.booleanValue())
        {
            toolLayer.bindBidirectional(commonLayer);
            toolLayer.set(commonLayer.get());
        }
        else
        {
            toolLayer.unbindBidirectional(commonLayer);
        }
    }

    /**
     * Handles a change in the current layer.
     *
     * @param layer the new layer
     */
    protected void handleLayerChange(DataTypeInfo layer)
    {
        if (layer != null)
        {
            List<String> columns = CollectionUtilities.sort(layer.getMetaDataInfo().getKeyNames());
            getSettingsModel().availableColumnsProperty().setAll(columns);
            StringProperty selectedColumnProp = getSettingsModel().selectedColumnProperty();
            String selectedColumn = selectedColumnProp.get();
            // Enable an event to be triggered even if the column doesn't change
            selectedColumnProp.set(null);
            selectedColumnProp.set(CollectionUtilities.getItemOrFirst(columns, selectedColumn));
        }
        else
        {
            getSettingsModel().availableColumnsProperty().clear();
            getSettingsModel().selectedColumnProperty().set(null);
            getModel().getDataModel().getBins().clear();
        }
    }

    /**
     * Handles a change in the selected column.
     *
     * @param column the new column
     */
    protected void handleSelectedColumnChange(String column)
    {
        if (column != null)
        {
            DataType dataType = DataType.toDataType(column, getCurrentLayer());
            getSettingsModel().dataTypeProperty().set(dataType);
            completeReload();
//            doBinnerStuff(() ->
//            {
//                myBinner.getCriteriaElement().setField(column);
//                myBinner.rebin();
//            });
        }
    }

    /**
     * Handles a change in the data type.
     *
     * @param dataType the data type
     */
    protected void handleDataTypeChange(DataType dataType)
    {
        getSettingsModel().binTypeProperty().set(getBinType());
    }

    /**
     * Handles a change in the numeric bin type.
     *
     * @param binType the bin type
     */
    protected void handleNumericBinTypeChange(BinType binType)
    {
        if (getSettingsModel().dataTypeProperty().get() == DataType.NUMBER)
        {
            getSettingsModel().binTypeProperty().set(getBinType());
            completeReload();
        }
    }

    /**
     * Handles a change in the time bin type.
     *
     * @param timeBinType the time bin type
     */
    protected void handleTimeBinTypeChange(TimeBinType timeBinType)
    {
        if (getSettingsModel().dataTypeProperty().get() == DataType.DATE)
        {
            getSettingsModel().binTypeProperty().set(getBinType());
            doBinnerStuff(() ->
            {
                CriteriaType criteriaType = myBinner.getCriteriaElement().getCriteriaType();
                if (criteriaType instanceof TimeCriteria)
                {
                    TimeCriteria timeCriteria = (TimeCriteria)criteriaType;
                    timeCriteria.setBinType(timeBinType);
                    myBinner.rebin();
                }
            });
        }
    }

    /**
     * Handles a change in the bin width.
     *
     * @param binWidth the bin width
     */
    protected void handleBinWidthChange(Number binWidth)
    {
        double doubleValue = binWidth.doubleValue();
        if (doubleValue > 0)
        {
            doBinnerStuff(() ->
            {
                CriteriaType criteriaType = myBinner.getCriteriaElement().getCriteriaType();
                if (criteriaType instanceof RangeCriteria)
                {
                    RangeCriteria rangeCriteria = (RangeCriteria)criteriaType;
                    rangeCriteria.setBinWidth(doubleValue);
                    myBinner.rebin();
                }
            });
        }
    }

    /**
     * Handles a change in showing empty bins.
     *
     * @param showEmpty whether to show empty bins
     */
    protected void handleShowEmptyChange(Boolean showEmpty)
    {
        doBinnerStuff(() ->
        {
            myBinner.setCreateEmptyBins(showEmpty.booleanValue());
            myBinner.rebin();
        });
    }

    /**
     * Handles a change in showing the N/A bin.
     *
     * @param showNA whether to show the N/A bin
     */
    protected void handleShowNAChange(Boolean showNA)
    {
        doBinnerStuff(() ->
        {
            myBinner.setCreateNABin(showNA.booleanValue());
            reloadDataNow();
        });
    }

    /**
     * Handles a change in sort method.
     *
     * @param sortMethod the sort method
     */
    protected void handleSortMethodChange(SortMethod sortMethod)
    {
        List<UIBin> oldBins = New.list(getModel().getDataModel().getBins());
        Collections.sort(oldBins, sortMethod.getComparator());

        getModel().getDataModel().getBins().clear();
        // Immediately setting the bins here doesn't cause the UI to update.
        // Seems like a bug in JavaFX.
        ThreadUtilities.runBackground(() ->
        {
            ThreadUtilities.sleep(100);
            FXUtilities.runOnFXThreadAndWait(() -> getModel().getDataModel().getBins().addAll(oldBins));
        });
    }

    /**
     * Handles AbstractDataTypeInfoChangeEvent.
     *
     * @param event the event
     */
    protected void handleDataTypeEvent(AbstractDataTypeInfoChangeEvent event)
    {
        if (event instanceof DataTypeInfoColorChangeEvent)
        {
            if (isCurrentLayer(event.getDataTypeInfo()))
            {
                DataTypeInfoColorChangeEvent colorEvent = (DataTypeInfoColorChangeEvent)event;
                Color color = FXUtilities.fromAwtColor(colorEvent.getColor());
                FXUtilities.runOnFXThreadAndWait(() -> myModel.getDataModel().layerColorProperty().set(color));
            }
        }
        else if (event instanceof DataTypeInfoMetaDataKeyAddedChangeEvent)
        {
            if (isCurrentLayer(event.getDataTypeInfo()))
            {
                DataTypeInfoMetaDataKeyAddedChangeEvent keyEvent = (DataTypeInfoMetaDataKeyAddedChangeEvent)event;
                FXUtilities.runOnFXThreadAndWait(() -> getSettingsModel().availableColumnsProperty().add(keyEvent.getKey()));
            }
        }
        else if (event instanceof DataTypeInfoMetaDataKeyRemovedChangeEvent)
        {
            if (isCurrentLayer(event.getDataTypeInfo()))
            {
                DataTypeInfoMetaDataKeyRemovedChangeEvent keyEvent = (DataTypeInfoMetaDataKeyRemovedChangeEvent)event;
                FXUtilities.runOnFXThreadAndWait(() -> getSettingsModel().availableColumnsProperty().remove(keyEvent.getKey()));
            }
        }
    }

    /**
     * Handles AbstractDataTypeControllerEvent.
     *
     * @param event the event
     */
    protected void handleDataTypeControllerEvent(AbstractDataTypeControllerEvent event)
    {
        if (event instanceof DataElementsAddedEvent)
        {
            DataElementsAddedEvent addedEvent = (DataElementsAddedEvent)event;
            if (isCurrentLayer(addedEvent.getType()))
            {
                addOrRemoveIDs(addedEvent.getAddedDataElementIds(), true);
            }
        }
        else if (event instanceof DataElementsRemovedEvent)
        {
            DataElementsRemovedEvent removedEvent = (DataElementsRemovedEvent)event;
            if (isCurrentLayer(removedEvent.getType()))
            {
                addOrRemoveIDs(removedEvent.getRemovedDataElementIds(), false);
            }
        }
    }

    /**
     * Handles AbstractConsolidatedDataElementChangeEvent.
     *
     * @param event the event
     */
    protected void handleDataElementEvent(AbstractConsolidatedDataElementChangeEvent event)
    {
        if (event instanceof ConsolidatedDataElementVisibilityChangeEvent)
        {
            assert event.getDataTypeKeys().size() == 1;
            String layerKey = event.getDataTypeKeys().iterator().next();
            if (isCurrentLayer(layerKey))
            {
                ConsolidatedDataElementVisibilityChangeEvent visEvent = (ConsolidatedDataElementVisibilityChangeEvent)event;
                boolean isAdd = !visEvent.getVisibleIdSet().isEmpty();
                addOrRemoveIDs(visEvent.getRegistryIds(), isAdd);
            }
        }
    }

    /**
     * Handles a bin being selected.
     *
     * @param bin the bin
     */
    protected void handleBinSelected(UIBin bin)
    {
        String typeKey = !bin.getData().isEmpty() ? bin.getData().get(0).getDataTypeInfo().getTypeKey() : null;
        Set<Long> idsToSelect = TroveUtilities.toLongSet(bin.getElementCacheIds());
        List<Long> allIds = myMantleToolbox.getDataElementLookupUtils().getDataElementCacheIds(typeKey);
        myMantleToolbox.getDataElementUpdateUtils().setDataElementsSelectionState(idsToSelect, allIds, typeKey, this);
    }

    /**
     * Gets the model.
     *
     * @return the model
     */
    public final ToolModels getModel()
    {
        return myModel;
    }

    /**
     * Gets the model.
     *
     * @return the model
     */
    protected final SettingsModel getSettingsModel()
    {
        return myModel.getSettingsModel();
    }

    /**
     * Creates a new binner and reloads the data.
     */
    private void completeReload()
    {
        DATA_EXECUTOR.execute(() ->
        {
            myBinner = newBinner();
            reloadDataNow();
        });
    }

    /**
     * Reloads data for the current layer.
     */
    protected void reloadData()
    {
        DATA_EXECUTOR.execute(this::reloadDataNow);
    }

    /**
     * Reloads data for the current layer.
     */
    private void reloadDataNow()
    {
        if (myBinner != null)
        {
            myBinner.setListener(null);
            TimeSpan span = getSettingsModel().allTimeProperty().get() ? null
                    : myToolbox.getTimeManager().getPrimaryActiveTimeSpans().get(0);
            myBinner.clear();
            myBinner.addAllElements(span);
//            myBinner.autoBin();
            myBinner.setListener(myBinnerListener);
            List<UIBin> bins = CollectionUtilities.getList(CollectionUtilities.filterDowncast(myBinner.getBins(), UIBin.class));
            Collections.sort(bins, getSettingsModel().sortMethodProperty().get().getComparator());
            FXUtilities.runOnFXThreadAndWait(() -> myModel.getDataModel().getBins().setAll(bins));
        }
    }

    /**
     * Returns whether the given layer is the current layer.
     *
     * @param layer the layer
     * @return whether it's the current layer
     */
    private boolean isCurrentLayer(DataTypeInfo layer)
    {
        return layer.equals(getCurrentLayer());
    }

    /**
     * Returns whether the given layer is the current layer.
     *
     * @param layerKey the layer key
     * @return whether it's the current layer
     */
    private boolean isCurrentLayer(String layerKey)
    {
        DataTypeInfo currentLayer = getCurrentLayer();
        return currentLayer != null && currentLayer.getTypeKey().equals(layerKey);
    }

    /**
     * Gets the current layer used in the tool.
     *
     * @return the current layer
     */
    private DataTypeInfo getCurrentLayer()
    {
        return getSettingsModel().currentLayerProperty().get();
    }

    /**
     * Adds/removes element IDs to the binner/model.
     *
     * @param ids the IDs
     * @param isAdd true for add, false for remove
     */
    private void addOrRemoveIDs(Collection<Long> ids, boolean isAdd)
    {
        doBinnerStuff(() ->
        {
            if (isAdd)
            {
//                boolean wasEmpty = myBinner.getBins().isEmpty();
                List<Long> filteredIds = filterIds(ids);
                if (!filteredIds.isEmpty())
                {
                    myBinner.addIds(filteredIds);
                }
//                if (wasEmpty || ids.size() > 100)
//                {
//                    myBinner.autoBin();
//                }
            }
            else
            {
                myBinner.removeIds(CollectionUtilities.getSet(ids));
            }
        });
    }

    /**
     * Filters the ids based on allowable times.
     *
     * @param ids the initial IDs
     * @return the filtered IDs
     */
    private List<Long> filterIds(Collection<Long> ids)
    {
        List<Long> filteredIds;
        if (getSettingsModel().allTimeProperty().get())
        {
            filteredIds = CollectionUtilities.getList(ids);
        }
        else
        {
            TimeSpan activeSpan = myToolbox.getTimeManager().getPrimaryActiveTimeSpans().get(0);
            DataElementLookupUtils lookupUtil = myMantleToolbox.getDataElementLookupUtils();
            filteredIds = ids.stream().filter(id -> activeSpan.overlaps(lookupUtil.getTimespan(id.longValue())))
                    .collect(Collectors.toList());
        }
        return filteredIds;
    }

    /**
     * Runs the runnable that operates on the binner.
     *
     * @param r the runnable
     */
    private void doBinnerStuff(Runnable r)
    {
        DATA_EXECUTOR.execute(() ->
        {
            if (myBinner != null)
            {
                r.run();
            }
        });
    }

    /**
     * Creates a new binner.
     *
     * @return the binner
     */
    private DataElementBinner newBinner()
    {
        BinCriteriaElement criteriaElement = new BinCriteriaElement();
        criteriaElement.setField(getSettingsModel().selectedColumnProperty().get());

        CriteriaType criteriaType;
        DataType dataType = getSettingsModel().dataTypeProperty().get();
        if (dataType == DataType.DATE)
        {
            TimeCriteria timeCriteria = new TimeCriteria();
            timeCriteria.setBinType(getSettingsModel().timeBinTypeProperty().get());
            criteriaType = timeCriteria;
        }
        else if (dataType == DataType.NUMBER)
        {
            if (getSettingsModel().numericBinTypeProperty().get() == BinType.UNIQUE)
            {
                criteriaType = new UniqueCriteria();
            }
            else
            {
                RangeCriteria rangeCriteria = new RangeCriteria();
                rangeCriteria.setBinWidth(getSettingsModel().binWidthProperty().get());
                criteriaType = rangeCriteria;
            }
        }
        else
        {
            criteriaType = new UniqueCriteria();
        }
        criteriaElement.setCriteriaType(criteriaType);

        DataElementBinner binner = new UIDataElementBinner(myMantleToolbox, criteriaElement, getCurrentLayer());
        binner.setCreateEmptyBins(getSettingsModel().showEmptyBinsProperty().get());
        binner.setCreateNABin(getSettingsModel().showNABinProperty().get());
        return binner;
    }

    /**
     * Gets the current bin type based on various settings.
     *
     * @return the bin type
     */
    private BinType getBinType()
    {
        BinType binType;
        switch (getSettingsModel().dataTypeProperty().get())
        {
            case NUMBER:
                binType = getSettingsModel().numericBinTypeProperty().get();
                break;
            case DATE:
                binType = BinType.toBinType(getSettingsModel().timeBinTypeProperty().get());
                break;
            case STRING:
            default:
                binType = BinType.UNIQUE;
        }
        return binType;
    }
}

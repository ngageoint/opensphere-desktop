package io.opensphere.mantle.crust;

import java.awt.Color;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import io.opensphere.core.Toolbox;
import io.opensphere.core.data.DataRegistry;
import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.core.model.Altitude.ReferenceLevel;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.order.OrderManager;
import io.opensphere.core.order.OrderManagerRegistry;
import io.opensphere.core.order.impl.DefaultOrderCategory;
import io.opensphere.core.order.impl.DefaultOrderParticipantKey;
import io.opensphere.core.util.Constants;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.MantleToolbox;
import io.opensphere.mantle.controller.DataTypeController;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.LoadsTo;
import io.opensphere.mantle.data.MapVisualizationType;
import io.opensphere.mantle.data.MetaDataInfo;
import io.opensphere.mantle.data.element.DataElement;
import io.opensphere.mantle.data.element.impl.SimpleDataElementProvider;
import io.opensphere.mantle.data.impl.DefaultBasicVisualizationInfo;
import io.opensphere.mantle.data.impl.DefaultDataGroupInfo;
import io.opensphere.mantle.data.impl.DefaultDataGroupInfoAssistant;
import io.opensphere.mantle.data.impl.DefaultDataTypeInfo;
import io.opensphere.mantle.data.impl.DefaultMapFeatureVisualizationInfo;

/**
 * Bundle together all of the things that comprise a loaded data set, and
 * provide methods for managing the data. The containing class maintains a map
 * of these, indexed by name.
 */
public class GenericDataSet
{
    /** Layer name. */
    private String name;

    /** Key for accessing the DataRegistry. */
    private DataModelCategory myDataModelCategory;

    /** Containing data group (one per DataTypeInfo). */
    private DefaultDataGroupInfo group;

    /** The data type (one per request). */
    private DataTypeInfo myType;

    /** IDs in Mantle. We need these to delete records from Mantle. */
    private long[] mantleIds;

    /** Name of the latitude field. */
    private String myLatitudeFieldName;

    /** Name of the longitude field. */
    private String myLongitudeFieldName;

    /** The controller through which data types are accessed. */
    private final DataTypeController myDataTypeController;

    /** The registry through which data is accessed. */
    private final DataRegistry myDataRegistry;

    /** The supplier used to get the root name of the data set. */
    private final Supplier<String> myRootNameSupplier;

    /** The toolbox through which application state is accessed. */
    private final Toolbox myToolbox;

    /** The registry used to manage the z-order of the data layer. */
    private final OrderManagerRegistry myOrderManagerRegistry;

    /** The manager instance used to maintain named data sets. */
    private final NamedDataSetManager myNamedDataSetManager;

    /**
     * Pass in the basic components.
     *
     * @param namedDataSetManager The manager instance used to maintain named
     *            data sets.
     * @param toolbox the toolbox through which application state is accessed.
     * @param dataModelCategory the category to which the data set will be
     *            assigned.
     * @param typeId name of the layer
     * @param rootNameSupplier the supplier used to get the root name of the
     *            data set.
     */
    public GenericDataSet(NamedDataSetManager namedDataSetManager, Toolbox toolbox, DataModelCategory dataModelCategory,
            String typeId, Supplier<String> rootNameSupplier)
    {
        myNamedDataSetManager = namedDataSetManager;
        myToolbox = toolbox;
        myDataRegistry = toolbox.getDataRegistry();
        myOrderManagerRegistry = myToolbox.getOrderManagerRegistry();
        MantleToolbox mantleTools = toolbox.getPluginToolboxRegistry().getPluginToolbox(MantleToolbox.class);
        myDataTypeController = mantleTools.getDataTypeController();
        mantleTools.getDataGroupController();
        myDataModelCategory = dataModelCategory;
        name = typeId;
        myRootNameSupplier = rootNameSupplier;
    }

    /**
     * Gets the value of the {@link #name} field.
     *
     * @return the value stored in the {@link #name} field.
     */
    public String getName()
    {
        return name;
    }

    /**
     * Specify the names of latitude and longitude fields.
     *
     * @param latitudeFieldName the name of the latitude field.
     * @param longitudeFieldName the name of the longitude field.
     */
    public void setMapSupport(String latitudeFieldName, String longitudeFieldName)
    {
        myLatitudeFieldName = latitudeFieldName;
        myLongitudeFieldName = longitudeFieldName;
    }

    /**
     * Construct and register Mantle support, including a data group and a data
     * type.
     *
     * @param parent the parent group
     * @param meta the object in which metadata structures are defined.
     */
    public void create(DataGroupInfo parent, MetaDataInfo meta)
    {
        create(parent, name, name, meta);
    }

    /**
     * Alternate create, allowing group and type names to differ.
     *
     * @param parent the parent group
     * @param groupId the group name
     * @param typeId the layer name
     * @param meta the object in which metadata structures are defined.
     */
    protected void create(DataGroupInfo parent, String groupId, String typeId, MetaDataInfo meta)
    {
        // Order of the operations in the implementation of this method is
        // deceptively important. Moving things around here can cause things
        // to stop working. Here are highlights (in no particular order):
        // - DataGroupInfo and DataTypeInfo are created
        // - the type is added to the group, which is added to its own parent
        // - the DataTypeController is notified of the new DataTypeInfo
        group = createDataGroup(parent, groupId);
        group.setAssistant(new DefaultAssistant(myNamedDataSetManager));
        myType = generateDataType(typeId, groupId, myRootNameSupplier.get(), meta);
        group.addMember(myType, this);
        parent.addChild(group, this);
        group.activationProperty().setActive(true);
        group.setGroupVisible(null, true, false, this);
        // GBDXMantleController listens for deactivation and removes data
        // group.activationProperty().addListener(myActivationListener);
        myDataTypeController.addDataType("Your", "Mom", myType, this);
    }

    /**
     * Creates a new data type, storing it in the order manager registry, and
     * registering it with the supplied types.
     *
     * @param name layer name
     * @param parent parent group name
     * @param rootName I don't know what this is
     * @param meta the object in which metadata structures are defined.
     * @return the constructed DataTypeInfo instance
     */
    protected DataTypeInfo generateDataType(String name, String parent, String rootName, MetaDataInfo meta)
    {
        // construct the type as a DefaultDataTypeInfo and endow it with stuff
        DefaultDataTypeInfo type = new DefaultDataTypeInfo(myToolbox, rootName, parent, name, name, true, meta);
        type.setBasicVisualizationInfo(new DefaultBasicVisualizationInfo(LoadsTo.STATIC,
                DefaultBasicVisualizationInfo.LOADS_TO_STATIC_ONLY, Color.ORANGE, true));
        type.setMapVisualizationInfo(new DefaultMapFeatureVisualizationInfo(MapVisualizationType.POINT_ELEMENTS, true));
        type.setOrderKey(new DefaultOrderParticipantKey(DefaultOrderCategory.DEFAULT_FEATURE_LAYER_FAMILY,
                DefaultOrderCategory.FEATURE_CATEGORY, type.getTypeKey()));

        // do the z-order thing
        OrderManager manager = myOrderManagerRegistry.getOrderManager(type.getOrderKey());
        int zorder = manager.activateParticipant(type.getOrderKey());
        type.getMapVisualizationInfo().setZOrder(zorder, null);

        return type;
    }

    /**
     * Create a group, with the new instance registered as a child of the
     * supplied parent.
     *
     * @param parent parent group
     * @param groupId its ID
     * @return the group
     */
    protected DefaultDataGroupInfo createDataGroup(DataGroupInfo parent, String groupId)
    {
        return new DataGroup(parent, groupId, myToolbox);
    }

    /**
     * Raw data are converted for participation in the Mantle. To wit:
     * <ul>
     * <li>Wrap each basic record as a DataElement with reference to the
     * resident DataTypeInfo</li>
     * <li>Create a DataElementProvider, which is a glorified Iterator, to
     * contain/introduce the DataElements</li>
     * <li>Pass the DataElementProvider to the DataTypeController, causing the
     * DataElements to be inserted and assigned unique IDs; those IDs are
     * returned and can be kept for future management</li>
     * </ul>
     *
     * @param recs the List of data records
     * @param cols the set of field (column) names
     */
    public void populate(List<Map<String, Object>> recs, Set<String> cols)
    {
        List<DataElement> elements = New.list();
        Map<String, Object> previousData = null;
        for (Map<String, Object> record : recs)
        {
            DataElement element = createElement(record, cols, previousData);
            elements.add(element);
            previousData = record;
        }

        List<Long> idList = myDataTypeController.addDataElements(new SimpleDataElementProvider(myType, elements.iterator()),
                null, null, this);
        if (idList == null)
        {
            return;
        }
        mantleIds = new long[idList.size()];
        int i = 0;
        for (Long id : idList)
        {
            mantleIds[i++] = id;
        }
    }

    /**
     * Create a {@link DataElement} from the specified data.
     *
     * @param data map of field name to field value
     * @param cols all field names (including those with null value)
     * @param previousData the previous element we created in order to make a
     *            polyline, or null if data is the first point.
     * @return a DataElement generated from the specified data.
     */
    public DataElement createElement(Map<String, Object> data, Set<String> cols, Map<String, Object> previousData)
    {
        if (myLatitudeFieldName == null || myLongitudeFieldName == null)
        {
            return DataUtil.createDataElement(data, cols);
        }
        Object latObj = data.get(myLatitudeFieldName);
        Object lonObj = data.get(myLongitudeFieldName);
        if (!(latObj instanceof Double && lonObj instanceof Double))
        {
            return DataUtil.createDataElement(data, cols);
        }

        LatLonAlt firstPoint = LatLonAlt.createFromDegreesMeters((Double)latObj, (Double)lonObj,
                Constants.METERS_PER_FEET * ((Double)data.get("Altitude")).doubleValue(), ReferenceLevel.ELLIPSOID);
        LatLonAlt secondPoint = null;
        if (previousData != null)
        {
            secondPoint = LatLonAlt.createFromDegreesMeters((Double)previousData.get(myLatitudeFieldName),
                    (Double)previousData.get(myLongitudeFieldName),
                    Constants.METERS_PER_FEET * ((Double)data.get("Altitude")).doubleValue(), ReferenceLevel.ELLIPSOID);
        }
        return DataUtil.createMapDataElement(new SimpleMetaDataProvider(data, cols), myType, firstPoint, secondPoint);
    }

    /**
     * Listener for an event indicating that the user has requested to delete
     * this dataset.
     */
    private class DefaultAssistant extends DefaultDataGroupInfoAssistant
    {
        /**
         * The manager instance used to handle named data sets.
         */
        private final NamedDataSetManager namedDataSetManager;

        /**
         * Creates a new default assistant, populated with the supplied manager.
         *
         * @param pNamedDataSetManager The manager instance used to handle named
         *            data sets.
         */
        public DefaultAssistant(NamedDataSetManager pNamedDataSetManager)
        {
            namedDataSetManager = pNamedDataSetManager;
        }

        @Override
        public boolean canDeleteGroup(DataGroupInfo dgi)
        {
            return true;
        }

        /**
         * {@inheritDoc}
         *
         * @see DefaultDataGroupInfoAssistant#deleteGroup(io.opensphere.mantle.data.DataGroupInfo,
         *      java.lang.Object)
         */
        @Override
        public void deleteGroup(DataGroupInfo dgi, Object source)
        {
            namedDataSetManager.removeDataSet(name);
            group.activationProperty().setActive(false);
            group.getParent().removeChild(group, null);
            myDataRegistry.removeModels(myDataModelCategory, false);
            if (mantleIds != null)
            {
                myDataTypeController.removeDataElements(myType, mantleIds);
            }
            myDataTypeController.removeDataType(myType, null);
        }
    }
}

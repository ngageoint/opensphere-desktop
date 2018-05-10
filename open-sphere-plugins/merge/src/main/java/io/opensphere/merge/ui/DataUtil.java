package io.opensphere.merge.ui;

import java.awt.Color;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import io.opensphere.core.Toolbox;
import io.opensphere.core.order.OrderManager;
import io.opensphere.core.order.OrderManagerRegistry;
import io.opensphere.core.order.impl.DefaultOrderCategory;
import io.opensphere.core.order.impl.DefaultOrderParticipantKey;
import io.opensphere.mantle.MantleToolbox;
import io.opensphere.mantle.controller.DataGroupController;
import io.opensphere.mantle.controller.DataTypeController;
import io.opensphere.mantle.data.ActivationListener;
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
 * The purpose of this class is to reduce the effort required to deal with the
 * annoying and stupid APIs in Mantle (and, to some extent, Core). This class
 * takes care of the procedures for inserting layers and data elements into
 * Mantle and handling requests to delete the same.
 */
public class DataUtil
{
    /** We need this to create a DefaultDataTypeInfo. */
    private Toolbox myToolbox;

    /** Whatever this is. */
    private OrderManagerRegistry myOrderRegistry;

    /** For manipulating data types. */
    private DataTypeController myTypeController;

    /** For manipulating data groups. */
    private DataGroupController myGroupController;

    /** Filler? */
    private String myRootName;

    /** All layers created by this DataUtil are subordinate to this root. */
    private DataGroupInfo myRootGroup;

    /** Map of data sets by their unique layer names. */
    private final Map<String, GenericDataSet> myNamedDataSets = new TreeMap<>();

    /**
     * Grant access to the Toolbox and all of its many, many subcomponents.
     *
     * @param toolbox the toolbox through which application interaction is
     *            performed.
     */
    public void setTools(Toolbox toolbox)
    {
        myToolbox = toolbox;
        myOrderRegistry = myToolbox.getOrderManagerRegistry();
        MantleToolbox mtb = myToolbox.getPluginToolboxRegistry().getPluginToolbox(MantleToolbox.class);
        myTypeController = mtb.getDataTypeController();
        myGroupController = mtb.getDataGroupController();
    }

    /**
     * Create a root DataGroupInfo with a specified name; all layers created by
     * this DataUtil instance will be subordinate to the root. Note: can only be
     * called after setTools (q.v.).
     *
     * @param name the name to assign to the generated root
     *            {@link DataGroupInfo}.
     */
    public void setupRoot(String name)
    {
        myRootName = name;
        myRootGroup = new DefaultDataGroupInfo(true, myToolbox, myRootName, myRootName);
        myGroupController.addRootDataGroupInfo(myRootGroup, this);
    }

    /**
     * Creates a DataTypeInfo and supporting Mantle structures, with references
     * to all relevant objects cached herein. Inclusion of data elements is
     * deferred (called from {@link #populateType(String, List)}), as some
     * DataElement generation routines (such as may be performed by the caller
     * or some agent thereof) require prior existence of the DataTypeInfo.
     *
     * @param typeId the name of the data type
     * @param meta MetaDataInfo belonging to the type
     * @return the DataTypeInfo created and registered
     */
    public DataTypeInfo registerType(String typeId, MetaDataInfo meta)
    {
        GenericDataSet data = getDataSet(typeId);
        if (data != null)
        {
            return data.getType();
        }
        data = new GenericDataSet(typeId);
        data.create(myRootGroup, meta);
        addDataSet(data);
        return data.getType();
    }

    /**
     * Introduce DataElement instances into a previously registered layer
     * (called from {@link #registerType(String, MetaDataInfo)}), navigating the
     * byzantine API of Mantle. Though it is not the expected use-case for this
     * method, multiple calls for the same layer should be handled correctly.
     *
     * @param typeId the layer's typeId
     * @param records the elements to be inserted
     */
    public void populateType(String typeId, List<DataElement> records)
    {
        GenericDataSet data = getDataSet(typeId);
        if (data == null)
        {
            return;
        }
        data.addElements(records);
    }

    /**
     * Introduce DataElement instances into a previously registered layer (cf.
     * registerType), navigating the byzantine API of Mantle. Any previously
     * stored DataElements are removed and replaced with the new ones.
     *
     * @param typeId the layer's typeId
     * @param records the elements to be inserted
     */
    public void repopulateType(String typeId, List<DataElement> records)
    {
        GenericDataSet data = getDataSet(typeId);
        if (data == null)
        {
            return;
        }
        data.clearElements();
        data.addElements(records);
    }

    /**
     * Remove all DataElements associated with the identified layer.
     *
     * @param typeId the layer's typeId
     */
    public void clearType(String typeId)
    {
        GenericDataSet data = getDataSet(typeId);
        if (data == null)
        {
            return;
        }
        data.clearElements();
    }

    /**
     * Attach a listener for events relating to the activation state of the
     * parent group of identified layer.
     *
     * @param typeId the layer's typeId
     * @param listener the ActivationListener
     */
    public void setActivationListener(String typeId, ActivationListener listener)
    {
        GenericDataSet data = getDataSet(typeId);
        if (data != null)
        {
            data.setStateListener(listener);
        }
    }

    /**
     * Attach a listener to be informed when the identified layer is removed
     * from the system.
     *
     * @param typeId the layer's typeId
     * @param listener the listener
     */
    public void setDeleteListener(String typeId, Runnable listener)
    {
        GenericDataSet data = getDataSet(typeId);
        if (data != null)
        {
            data.setDeleteListener(listener);
        }
    }

    /**
     * Retrieve the collection of stuff associated with the given <i>id</i>.
     *
     * @param id the type id
     * @return a GenericDataSet
     */
    private GenericDataSet getDataSet(String id)
    {
        synchronized (myNamedDataSets)
        {
            return myNamedDataSets.get(id);
        }
    }

    /**
     * Index a new data set by its name within the resident Map (thread-safe).
     *
     * @param data the data set to index.
     */
    private void addDataSet(GenericDataSet data)
    {
        synchronized (myNamedDataSets)
        {
            myNamedDataSets.put(data.name, data);
        }
    }

    /**
     * Remove a data set from the resident Map (thread-safe).
     *
     * @param name name of the removed data
     */
    private void delDataSet(String name)
    {
        synchronized (myNamedDataSets)
        {
            myNamedDataSets.remove(name);
        }
    }

    /**
     * Bundle together all of the things that comprise a loaded data set, and
     * provide methods for managing the data. The containing class maintains a
     * map of these, indexed by name.
     */
    private class GenericDataSet
    {
        /** Layer name. */
        private final String name;

        /** Containing data group (one per DataTypeInfo). */
        private DefaultDataGroupInfo group;

        /** The data type (one per request). */
        private DataTypeInfo type;

        /** IDs in Mantle. We need these to delete records from Mantle. */
        private long[] mantleIds = new long[0];

        /**
         * Reference to the activation listener to prevent garbage collection
         * (preserving weak references).
         */
        private ActivationListener stateListener;

        /** Callback for when the layer is deleted. */
        private Runnable deleteListeners;

        /**
         * Pass in the basic components.
         *
         * @param typeId name of the layer
         */
        public GenericDataSet(String typeId)
        {
            name = typeId;
        }

        /**
         * Construct and register Mantle support, including a data group and a
         * data type.
         *
         * @param parentGroup the parent group
         * @param meta the metadata to use to create the dataset.
         */
        public void create(DataGroupInfo parentGroup, MetaDataInfo meta)
        {
            create(parentGroup, name, name, meta);
        }

        /**
         * Alternate create, allowing group and type names to differ.
         *
         * @param parentGroup the parent group
         * @param groupId the group name
         * @param typeId the layer name
         * @param meta the metadata with which to create the data set.
         */
        private void create(DataGroupInfo parentGroup, String groupId, String typeId, MetaDataInfo meta)
        {
            // Order of the operations in the implementation of this method is
            // deceptively important. Moving things around here can cause things
            // to stop working. Here are highlights (in no particular order):
            // - DataGroupInfo and DataTypeInfo are created
            // - the type is added to the group, which is added to its own
            // parent
            // - the DataTypeController is notified of the new DataTypeInfo
            group = generateGroup(parentGroup, groupId);
            group.setAssistant(new ListenerAssistant());
            type = generateType(typeId, groupId, myRootName, meta);
            group.addMember(type, this);
            parentGroup.addChild(group, this);
            group.activationProperty().setActive(true);
            group.setGroupVisible(null, true, false, this);
            myTypeController.addDataType("Your", "Mom", type, this);
        }

        /**
         * Get a reference to the local DataTypeInfo.
         *
         * @return the DataTypeInfo
         */
        public DataTypeInfo getType()
        {
            return type;
        }

        /**
         * Install a listener to be informed of when the activation state of the
         * managed DataGroupInfo changes.
         *
         * @param listener the ActivationListener
         */
        public void setStateListener(ActivationListener listener)
        {
            if (stateListener != null)
            {
                group.activationProperty().removeListener(stateListener);
            }
            stateListener = listener;
            if (stateListener != null)
            {
                group.activationProperty().addListener(stateListener);
            }
        }

        /**
         * Install a listener to be informed of when the managed DataTypeInfo is
         * deleted from the system.
         *
         * @param listener listener
         */
        public void setDeleteListener(Runnable listener)
        {
            deleteListeners = listener;
        }

        /**
         * Introduce new DataElement instances to the managed DataTypeInfo.
         *
         * @param elements List o' DataElement
         */
        public void addElements(List<DataElement> elements)
        {
            List<Long> idList = myTypeController.addDataElements(new SimpleDataElementProvider(type, elements.iterator()), null,
                    null, this);
            if (idList == null || idList.isEmpty())
            {
                return;
            }
            // add new IDs to the old ones
            long[] newIds;
            int j = 0;
            // create a new array and move the old ones over
            newIds = new long[mantleIds.length + idList.size()];
            for (int i = 0; i < mantleIds.length; i++)
            {
                newIds[j++] = mantleIds[i];
            }
            // include the new ones
            for (Long id : idList)
            {
                newIds[j++] = id.longValue();
            }
            mantleIds = newIds;
        }

        /** Clear from Mantle the elements associated with this data set. */
        public void clearElements()
        {
            myTypeController.removeDataElements(type, mantleIds);
            mantleIds = new long[0];
        }

        /** Remove the layer and group from Mantle. */
        private void removeLayer()
        {
            delDataSet(name);
            group.activationProperty().setActive(false);
            group.getParent().removeChild(group, null);
            clearElements();
            myTypeController.removeDataType(type, null);
        }

        /**
         * Listener for an event indicating that the user has requested to
         * delete this dataset from the "Add Data" dialog.
         */
        private class ListenerAssistant extends DefaultDataGroupInfoAssistant
        {
            /**
             * {@inheritDoc}
             *
             * @see io.opensphere.mantle.data.impl.DefaultDataGroupInfoAssistant#canDeleteGroup(io.opensphere.mantle.data.DataGroupInfo)
             */
            @Override
            public boolean canDeleteGroup(DataGroupInfo dgi)
            {
                return true;
            }

            /**
             * {@inheritDoc}
             *
             * @see io.opensphere.mantle.data.impl.DefaultDataGroupInfoAssistant#deleteGroup(io.opensphere.mantle.data.DataGroupInfo,
             *      java.lang.Object)
             */
            @Override
            public void deleteGroup(DataGroupInfo dgi, Object source)
            {
                removeLayer();
                if (deleteListeners != null)
                {
                    deleteListeners.run();
                }
            }
        }
    }

    /**
     * Create a group.
     *
     * @param parentGroup parent group
     * @param groupId its ID
     * @return the group
     */
    private DefaultDataGroupInfo generateGroup(DataGroupInfo parentGroup, String groupId)
    {
        return new DefaultDataGroupInfo(false, myToolbox, parentGroup.getId(), groupId, groupId);
    }

    /**
     * Deal with the annoying Mantle API.
     *
     * @param name layer name
     * @param parentGroupName parent group name
     * @param rootName I don't know what this is
     * @param meta the metadata describing the type to be created.
     * @return the constructed DataTypeInfo instance
     */
    private DataTypeInfo generateType(String name, String parentGroupName, String rootName, MetaDataInfo meta)
    {
        // construct the type as a DefaultDataTypeInfo and endow it with stuff
        DefaultDataTypeInfo type = new DefaultDataTypeInfo(myToolbox, rootName, parentGroupName, name, name, true, meta);
        type.setBasicVisualizationInfo(new DefaultBasicVisualizationInfo(LoadsTo.STATIC,
                DefaultBasicVisualizationInfo.LOADS_TO_STATIC_ONLY, Color.ORANGE, true));
        type.setMapVisualizationInfo(new DefaultMapFeatureVisualizationInfo(MapVisualizationType.POINT_ELEMENTS, true));
        type.setOrderKey(new DefaultOrderParticipantKey(DefaultOrderCategory.DEFAULT_FEATURE_LAYER_FAMILY,
                DefaultOrderCategory.FEATURE_CATEGORY, type.getTypeKey()));

        // do the z-order thing
        OrderManager manager = myOrderRegistry.getOrderManager(type.getOrderKey());
        int zorder = manager.activateParticipant(type.getOrderKey());
        type.getMapVisualizationInfo().setZOrder(zorder, null);

        return type;
    }
}

package io.opensphere.mantle.data;

import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.event.EventHandler;

import javax.naming.OperationNotSupportedException;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

import io.opensphere.core.cache.util.PropertyDescriptor;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.Quadrilateral;
import io.opensphere.core.util.ObservableValue;
import io.opensphere.core.util.WeakChangeSupport;
import io.opensphere.core.util.collections.New;

/**
 * A grouping of associated DataTypeInfo, that can possibly form a node in a tree of associated groups of data.
 *
 * This is intended to be a pure model with no visualization or other properties associated with nodes or children. It only
 * details how data types and other groups are associated. The only exception at present is the DisplayName property.
 */
@SuppressWarnings("PMD.GodClass")
public interface DataGroupInfo
{
    /** The context id for actions against active data groups. */
    String ACTIVE_DATA_CONTEXT = "ACTIVE_DATA_GROUP_CONTEXT";

    /** The context id for actions against areas. */
    String AREA_CONTEXT = "AREA_CONTEXT";

    /**
     * Comparator that orders {@link DataGroupInfo}s by their display names (case insensitive).
     */
    Comparator<DataGroupInfo> CASE_INSENSITIVE_DISPLAY_NAME_COMPARATOR = (o1, o2) ->
    {
        int result = 0;
        if (o1 != null && o1.getDisplayName() != null && o2 != null && o2.getDisplayName() != null)
        {
            result = o1.getDisplayName().toLowerCase().compareTo(o2.getDisplayName().toLowerCase());
        }
        return result;
    };

    /** Comparator that orders {@link DataGroupInfo}s by their display names. */
    Comparator<DataGroupInfo> DISPLAY_NAME_COMPARATOR = (o1, o2) ->
    {
        int result = 0;
        if (o1 != null && o1.getDisplayName() != null && o2 != null && o2.getDisplayName() != null)
        {
            result = o1.getDisplayName().compareTo(o2.getDisplayName());
        }
        return result;
    };

    /** A function that returns the display name of the data group info. */
    Function<? super DataGroupInfo, ? extends String> DISPLAY_NAME_FUNCTION = input -> input.getDisplayName();

    /**
     * Comparator that orders {@link DataGroupInfo}s by their long display names.
     */
    Comparator<DataGroupInfo> LONG_DISPLAY_NAME_COMPARATOR = (o1, o2) ->
    {
        int result = 0;
        if (o1 != null && o1.getLongDisplayName() != null && o2 != null && o2.getLongDisplayName() != null)
        {
            result = o1.getLongDisplayName().compareTo(o2.getLongDisplayName());
        }
        return result;
    };

    /** The context id for actions against active data groups. */
    String MANAGE_DATA_CONTEXT = "MANAGE_DATA_GROUP_CONTEXT";

    /**
     * The Constant NO_EVENT_SOURCE. If provided as the source for one of the change events, it will cause the event not be be
     * sent.
     */
    Object NO_EVENT_SOURCE = new Object();

    /**
     * Property descriptor for {@link DataGroupInfo}s used in the data registry.
     */
    PropertyDescriptor<DataGroupInfo> PROPERTY_DESCRIPTOR = new PropertyDescriptor<>(DataGroupInfo.class.getName(),
            DataGroupInfo.class);

    /**
     * Sets the activationSupported flag to true, allowing the application to respond to activation events. If activation is not
     * supported, then the {@link #activationProperty()} may be null.
     *
     * @param pActivationSupported the state to set the activation supported flag. True enables activation support, false disables
     *            it.
     */
    void setActivationSupported(boolean pActivationSupported);

    /**
     * Tests to determine if activation is supported in the data group. Defaults to true.
     *
     * @return true if activation is supported for the data group, false otherwise.
     * @see #setActivationSupported(boolean)
     * @see #activationProperty()
     */
    boolean isActivationSupported();

    /**
     * The property in which activation support is tracked. This property will emit events when activation support changed (note
     * the distinction: this property is used to track the <i>support</i> of activation, not the activation itself).
     *
     * @return The property in which activation support is tracked.
     */
    BooleanProperty activationSupportProperty();

    /**
     * Enables or disables the support of triggering within the data group. Typically (but not always), a data group supports
     * either triggering or activation, but not both at the same time. When a user interacts with a data group that supports
     * activation, the state can be set to activated or de-activated. When a user interacts with a data group that doesn't support
     * activation, but does support triggering, the data group may be triggered one or more times, depending on the user's
     * interaction.
     *
     * @param pTriggeringSupported true if triggering is supported for the data group, false otherwise.
     */
    void setTriggeringSupported(boolean pTriggeringSupported);

    /**
     * Tests to determine if triggering is supported for the data group. Typically (but not always), a data group supports either
     * triggering or activation, but not both at the same time. When a user interacts with a data group that supports activation,
     * the state can be set to activated or de-activated. When a user interacts with a data group that doesn't support activation,
     * but does support triggering, the data group may be triggered one or more times, depending on the user's interaction.
     *
     * @return true if triggering is supported for the data group, false otherwise.
     */
    boolean isTriggeringSupported();

    /**
     * The property in which triggering support is tracked. This property will emit events when triggering support changed (note
     * the distinction: this property is used to track the <i>support</i> of triggering, not if the data group was actually
     * triggered).
     *
     * @return The property in which triggering support is tracked.
     */
    BooleanProperty triggerSupportProperty();

    /**
     * The property through which data group triggering is propagated. When the data group is triggered, the event is sent through
     * this property.
     *
     * @return the property through which triggered events are handled.
     */
    ObjectProperty<EventHandler<DataGroupEvent>> triggeredProperty();

    /**
     * Sets the event handler to react to triggering events. When the data group is triggered, the event is sent to this handler.
     *
     * @param pHandler the handler used to process trigger events.
     */
    void setTriggerHandler(EventHandler<DataGroupEvent> pHandler);

    /**
     * Executes the trigger logic on the data group. If no listeners are registered, nothing happens.
     *
     * @param pEvent the event to propagate out to registered listeners of the trigger property.
     */
    void trigger(DataGroupEvent pEvent);

    /**
     * Get the activation property, which can be used to listen for activation events.
     *
     * @return The activation property.
     */
    DataGroupActivationProperty activationProperty();

    /**
     * Adds the child data group info.
     *
     * @param dgi the dgi
     * @param source the source of the change
     * @throws IllegalStateException if the dgi being added would become its own parent ancestor.
     */
    void addChild(DataGroupInfo dgi, Object source);

    /**
     * Adds the data type to the node list.
     *
     * @param dti the {@link DataTypeInfo} to add.
     * @param source the source of the change
     */
    void addMember(DataTypeInfo dti, Object source);

    /**
     * Clear all data types.
     *
     * @param recursive - true to clear for all children nodes.
     * @param source the source of the change
     */
    void clearMembers(boolean recursive, Object source);

    /**
     * Creates the group set.
     *
     * @param nodeFilter the node filter
     * @return the sets the
     */
    Set<DataGroupInfo> createGroupSet(Predicate<? super DataGroupInfo> nodeFilter);

    /**
     * Creates a TreeNode that represents this DataGroupInfo and its children. Children are sorted in "natural" order. No filter
     * is performed.
     *
     * @return the {@link TreeNode}
     */
    MutableTreeNode createTreeNode();

    /**
     * Creates a tree node that represents this DataGroupInfo and its children. child nodes will be sorted using the comparator or
     * in natural order if no comparator is provided ( null ), nodes can be filtered using the supplied filter. Child nodes that
     * do not pass the filter criteria will not be added to the tree ( including their children ). The filter will not apply to
     * the node on which this function is called only on children.
     *
     * @param comparator the comparator to use to ordert he child nodes ( recursive )
     * @param nodeFilter a filter to filter down the children nodes ( and their children )
     * @return the tree node the TreeNode.
     */
    MutableTreeNode createTreeNode(Comparator<? super DataGroupInfo> comparator, Predicate<? super DataGroupInfo> nodeFilter);

    /**
     * Searches for members that matches the filter, optionally recursively searches the children to see if any child members
     * match the filter.
     *
     * @param dtiFilter the {@link DataTypeInfo} filter to use to select members.
     * @param recursive if true recurses into children to check
     * @param stopOnFirstFound if true stops search on the first member to match the filter criteria, if false exhaustively
     *            searches member set.
     *
     * @return the set of Members that matched the filter criteria or an empty set if none found.
     */
    Set<DataTypeInfo> findMembers(Predicate<? super DataTypeInfo> dtiFilter, boolean recursive, boolean stopOnFirstFound);

    /**
     * Fires off a {@link AbstractDataGroupInfoChangeEvent} to any listeners.
     *
     * @param e - the event to fire.
     */
    void fireGroupInfoChangeEvent(AbstractDataGroupInfoChangeEvent e);

    /**
     * Gets the {@link DataGroupInfoAssistant} for this group.
     *
     * @return the assistant or null if no assistant is available.
     */
    DataGroupInfoAssistant getAssistant();

    /**
     * Gets the child added change support.
     *
     * @return the child added change support
     */
    WeakChangeSupport<Consumer<? super DataGroupInfo>> getChildAddedChangeSupport();

    /**
     * Gets the {@link Set} of {@link DataGroupInfo} that represent the children of this node.
     *
     * @return the immutable list of children
     */
    List<DataGroupInfo> getChildren();

    /**
     * Get all the descendants of this node.
     *
     * @param descendants Return collection of descendants.
     */
    void getDescendants(Collection<DataGroupInfo> descendants);

    /**
     * Gets the display name for the type.
     *
     * @return the display name for the type.
     */
    String getDisplayName();

    /**
     * Gets the display name with postfix top parent name.
     *
     * @return the display name with postfix top parent name
     */
    String getDisplayNameWithPostfixTopParentName();

    /**
     * Gets whether the group should be expanded by default.
     *
     * @return TRUE: expand by default, FALSE: collapse by default, null: caller's discretion
     */
    Boolean getExpandedByDefault();

    /**
     * Recursively searches the node hierarchy to find and return the {@link DataGroupInfo} with the given id.
     *
     * @param id - the id to search for
     * @return the DataGroupInfo or null if not found
     */
    DataGroupInfo getGroupById(String id);

    /**
     * Gets the description for the data group info.
     *
     * @return the description
     */
    String getGroupDescription();

    /**
     * Gets the id.
     *
     * @return the id for the group
     */
    String getId();

    /**
     * Gets the long display name which is the display name for the top parent prepended to the display name for the group.
     *
     * @return the long display name
     */
    String getLongDisplayName();

    /**
     * Gets the member by id.
     *
     * @param id the id
     * @param recursive - true to search the entire tree from this node down to remove the type.
     * @return the member by id
     */
    DataTypeInfo getMemberById(String id, boolean recursive);

    /**
     * Gets the member visualization types.
     *
     * @param recursive the recursive
     * @return the member visualization types
     */
    Set<MapVisualizationType> getMemberMapVisualizationTypes(boolean recursive);

    /**
     * Gets the data types for this node (and optionally all sub-nodes).
     *
     * @param recurseChildren - recurse into children nodes.
     * @return the data types
     */
    Set<DataTypeInfo> getMembers(boolean recurseChildren);

    /**
     * Gets the parent for this node, or null if this is a top level node.
     *
     * @return the parent {@link DataGroupInfo} or null
     */
    DataGroupInfo getParent();

    /**
     * Gets the preview image.
     *
     * @param observableImage The receiver for the preview image.
     * @return {@code true} if this data group supports preview images.
     */
    boolean getPreviewImage(ObservableValue<? super BufferedImage> observableImage);

    /**
     * Gets the provider type.
     *
     * @return the provider type
     */
    String getProviderType();

    /**
     * Gets the region the data is contained in.
     *
     * @param observableRegion Observable to contain the region the data is contained in.
     * @return {@code true} if this data group supports regions.
     */
    boolean getRegion(ObservableValue<? super Quadrilateral<GeographicPosition>> observableRegion);

    /**
     * Gets a description for this group and its members if applicable.
     *
     * @return the description
     */
    String getSummaryDescription();

    /**
     * Working up the chain through parents, searches until it finds the top most parent ( i.e. a DataGroupInfo without a parent).
     *
     * @return the top parent or this if this DataGroupInfo has no parent.
     */
    DataGroupInfo getTopParent();

    /**
     * Gets the top parent display name.
     *
     * @return the top parent display name
     */
    String getTopParentDisplayName();

    /**
     * Get a stream containing this data group as well as its children and its children's children, and so on.
     *
     * @return The stream.
     */
    Stream<DataGroupInfo> groupStream();

    /**
     * Returns true if there are children groups.
     *
     * @return true if there are children, false if not
     */
    boolean hasChildren();

    /**
     * Get if this group has settings that can be configured on a settings panel.
     *
     * @return If this group should have a settings panel.
     */
    boolean hasDetails();

    /**
     * Checks to see if this group ( or optionally all child group recursive ) contains DataTypeInfo with a MapVisualizationType
     * for feature data.
     *
     * @param recursive , if true recurses into children to check.
     * @return true, if found member or child member of feature type.
     */
    boolean hasFeatureTypes(boolean recursive);

    /**
     * Checks to see if this group ( or optionally all child group recursive ) contains DataTypeInfo with a MapVisualizationType
     * for image tile data.
     *
     * @param recursive , if true recurses into children to check.
     * @return true, if found member or child member of image tile type.
     */
    boolean hasImageTileTypes(boolean recursive);

    /**
     * Checks to see if this group ( or optionally all child group recursive ) contain the specified {@link DataTypeInfo}.
     *
     * @param dti the {@link DataTypeInfo} to check for.
     * @param recursive , if true recurses into children to check.
     * @return true, if found as a member
     */
    boolean hasMember(DataTypeInfo dti, boolean recursive);

    /**
     * Checks to see if this node has any DataTypeInfo specified. But does not check children nodes.
     *
     * @param recursive , if true recurses into children to check.
     * @return true, if there are members
     */
    boolean hasMembers(boolean recursive);

    /**
     * Checks for timeline member.
     *
     * @param recursive the recursive
     * @return true, if successful
     */
    boolean hasTimelineMember(boolean recursive);

    /**
     * Checks to see if this group ( or optionally all child group recursive ) contains DataTypeInfo with visualization styles for
     * feature data.
     *
     * @param recursive , if true recurses into children to check.
     * @return true, if found member or child member of feature type.
     */
    boolean hasVisualizationStyles(boolean recursive);

    /**
     * Indicates if this data group supports drag and drop of its types.
     *
     * @return True if it can support drag and drop, false otherwise.
     */
    boolean isDragAndDrop();

    /**
     * Indicates if this data group can be flattened or if its heirarchical information must be intact.
     *
     * @return True if it can be flattened false otherwise.
     */
    boolean isFlattenable();

    /**
     * Whether this group should be hidden in the tree.
     *
     * @return whether this group should be hidden in the tree
     */
    boolean isHidden();

    /**
     * Checks if the DataGroupInfo specified a parent ancestor working up the tree.
     *
     * @param dgi the dgi to check.
     * @return true, if is above in hierarchy
     */
    boolean isParentAncestor(DataGroupInfo dgi);

    /**
     * Returns true if this {@link DataGroupInfo} is a root node ( i.e. it cannot have a parent.) If this returns true any attempt
     * to set a parent on a root {@link DataGroupInfo} will throw {@link OperationNotSupportedException}.
     *
     * @return true if root node, false if not
     */
    boolean isRootNode();

    /**
     * Get if this group can have tags applied to it.
     *
     * @return If this group can have tags.
     */
    boolean isTaggable();

    /**
     * Notify listeners on my ancestors and me that a child was added to me or one of my descendants.
     *
     * @param child The child.
     */
    void notifyChildAddedListeners(DataGroupInfo child);

    /**
     * Returns the number of children groups or zero if none.
     *
     * @return the number of children groups.
     */
    int numChildren();

    /**
     * Returns the number of members in this DataTypeInfo. Count is only for this DataGroupInfo unless recursive, then counts all
     * members for all children as well and includes them in total.
     *
     * @param recursive , if true recurses into children to check.
     * @return the number of members
     */
    int numMembers(boolean recursive);

    /**
     * Removes the child group.
     *
     * @param dgi the group to remove
     * @param source the source of the change
     * @return true if removed, false if not.
     */
    boolean removeChild(DataGroupInfo dgi, Object source);

    /**
     * Removes the child group.
     *
     * @param dgi the group to remove
     * @param source the source of the change
     * @return true if removed, false if not.
     */
    boolean removeChildKeepActive(DataGroupInfo dgi, Object source);

    /**
     * Removes the data type from this node.
     *
     * @param dti the dti to remove.
     * @param recursive - true to search the entire tree from this node down to remove the type.
     * @param source the source of the change
     * @return true, if successful, false if not or it was not contained within this node.
     */
    boolean removeMember(DataTypeInfo dti, boolean recursive, Object source);

    /**
     * Sets the display name for the group.
     *
     * @param name the new display name
     * @param source the source of the change
     */
    void setDisplayName(String name, Object source);

    /**
     * Sets whether the group should be expanded by default.
     *
     * @param expandedByDefault whether the group should be expanded by default
     */
    void setExpandedByDefault(Boolean expandedByDefault);

    /**
     * A macro function that sets the visibility state of all the members ( {@link DataTypeInfo}) that are part of this group, and
     * if recursive, all the members of children of this group and their children etc.
     *
     * @param dtiFilter - the filter to select the DataTypeInfo members to adjust ( may be null if no filter is desired )
     * @param visible - true to set to visible, false to set to invisible
     * @param recursive - true to recurse into children etc.
     * @param source the source making the change
     */
    void setGroupVisible(Predicate<? super DataTypeInfo> dtiFilter, boolean visible, boolean recursive, Object source);

    /**
     * Sets whether this group should be hidden in the tree.
     *
     * @param hidden whether this group should be hidden in the tree
     */
    void setHidden(boolean hidden);

    /**
     * Sets the id for the group.
     *
     * @param id - the id
     * @param source the source of the change
     */
    void setId(String id, Object source);

    /**
     * Sets the parent for this node. Null indicates no parent.
     *
     * @param parent - the parent DataGroupInfo
     */
    void setParent(DataGroupInfo parent);

    /**
     * True if the user is allowed to activate/de-activate this group through UI interfaces. False if it is programmatic
     * activation only.
     *
     * @return true, user controlled, false if machine controlled.
     */
    boolean userActivationStateControl();

    /**
     * True if the user is allowed to remove this group through UI interfaces. False if it is programmatic deletion only.
     *
     * @return true, user controlled, false if machine controlled.
     */
    boolean userDeleteControl();

    /**
     * Checks to see if this group (or optionally all child groups recursive ) contains data type info with a MapVisualizaton that
     * uses styles.
     *
     * @param recursive , if true recurses into children to check.
     * @return true if found member or child member that uses styles.
     */
    boolean usesStyles(boolean recursive);

    /**
     * Gets whether to preserve child order.
     *
     * @return whether to preserve child order
     */
    boolean isPreserveChildOrder();

    /**
     * Sets whether to preserve child order.
     *
     * @param preserveChildOrder whether to preserve child order
     */
    void setPreserveChildOrder(boolean preserveChildOrder);

    /**
     * Gets the categories to which the data group belongs. Note that the
     * returned value is a live list, and can be modified by the caller.
     *
     * @return a set of data categories, which may be empty, but never null.
     */
    Set<String> getDataCategories();

    /**
     * Determines whether the group has a member that matches the filter.
     *
     * @param dtiFilter the data type filter
     * @param recursive whether to search recursively
     * @return whether any member matches
     */
    default boolean hasMember(Predicate<? super DataTypeInfo> dtiFilter, boolean recursive)
    {
        return !findMembers(dtiFilter, recursive, true).isEmpty();
    }

    /**
     * A marker interface to provide a common base for
     * {@link DataGroupContextKey} and {@link MultiDataGroupContextKey} to
     * implement.
     */
    public interface ContextKey
    {
        /**
         * Get the dataGroups.
         *
         * @return the dataGroups
         */
        Collection<DataGroupInfo> getDataGroups();

        /**
         * Get the dataTypes.
         *
         * @return the dataTypes
         */
        Collection<DataTypeInfo> getDataTypes();
    }

    /** The context key for actions associated with a particular data group. */
    class DataGroupContextKey implements ContextKey
    {
        /** The data group this context acts against. */
        private final DataGroupInfo myDataGroup;

        /** The data type this context acts against. */
        private final DataTypeInfo myDataType;

        /**
         * Constructor.
         *
         * @param dgi The data group this context acts against.
         * @param dti The data type this context acts against.
         */
        public DataGroupContextKey(DataGroupInfo dgi, DataTypeInfo dti)
        {
            myDataGroup = dgi;
            myDataType = dti;
        }

        /**
         * Get the dataGroup.
         *
         * @return the dataGroup
         */
        public DataGroupInfo getDataGroup()
        {
            return myDataGroup;
        }

        /**
         * Get the dataType.
         *
         * @return the dataType
         */
        public DataTypeInfo getDataType()
        {
            return myDataType;
        }

        /**
         * {@inheritDoc}
         *
         * @see io.opensphere.mantle.data.DataGroupInfo.ContextKey#getDataGroups()
         */
        @Override
        public Collection<DataGroupInfo> getDataGroups()
        {
            return Collections.singleton(myDataGroup);
        }

        /**
         * {@inheritDoc}
         *
         * @see io.opensphere.mantle.data.DataGroupInfo.ContextKey#getDataTypes()
         */
        @Override
        public Collection<DataTypeInfo> getDataTypes()
        {
            return Collections.singleton(myDataType);
        }
    }

    /**
     * The context key for actions associated with a multiple data groups/types.
     */
    class MultiDataGroupContextKey implements ContextKey
    {
        /** The actual data groups this context acts against. */
        private final Collection<DataGroupInfo> myActualDataGroups;

        /** The actual data types this context acts against. */
        private final Collection<DataTypeInfo> myActualDataTypes;

        /** The data groups this context acts against. */
        private final Collection<DataGroupInfo> myDataGroups;

        /**
         * Constructor.
         *
         * @param dgis The data groups this context acts against.
         * @param actualGroups The actual data groups this context acts against.
         * @param actualTypes The actual data types this context acts against.
         */
        public MultiDataGroupContextKey(Collection<DataGroupInfo> dgis, Collection<DataGroupInfo> actualGroups,
                Collection<DataTypeInfo> actualTypes)
        {
            myDataGroups = New.collection(dgis);
            myActualDataGroups = New.collection(actualGroups);
            myActualDataTypes = New.collection(actualTypes);
        }

        /**
         * Get the dataGroups.
         *
         * @return the dataGroups
         */
        public Collection<DataGroupInfo> getActualDataGroups()
        {
            return myActualDataGroups;
        }

        /**
         * Get the dataTypes.
         *
         * @return the dataTypes
         */
        public Collection<DataTypeInfo> getActualDataTypes()
        {
            return myActualDataTypes;
        }

        /**
         * Get the dataGroups.
         *
         * @return the dataGroups
         */
        @Override
        public Collection<DataGroupInfo> getDataGroups()
        {
            return myDataGroups;
        }

        /**
         * {@inheritDoc}
         *
         * @see io.opensphere.mantle.data.DataGroupInfo.ContextKey#getDataTypes()
         */
        @Override
        public Collection<DataTypeInfo> getDataTypes()
        {
            return myActualDataTypes;
        }
    }
}

package io.opensphere.mantle.data;

import java.util.Collection;
import java.util.Comparator;
import java.util.Set;
import java.util.function.Predicate;

import io.opensphere.core.Toolbox;
import io.opensphere.core.api.Model;
import io.opensphere.core.cache.util.PropertyDescriptor;
import io.opensphere.core.datafilter.DataFilterRegistry;
import io.opensphere.core.event.EventManager;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.order.OrderParticipantKey;

/**
 * The interface that defines a data type ( or layer ) for the application.
 */
@SuppressWarnings("PMD.GodClass")
public interface DataTypeInfo extends Model, Comparable<DataTypeInfo>
{
    /**
     * Comparator that orders {@link DataTypeInfo}s by their display names (Case
     * insensitive).
     */
    Comparator<DataTypeInfo> CASE_INSENSITIVE_DISPLAY_NAME_COMPARATOR = new Comparator<>()
    {
        @Override
        public int compare(DataTypeInfo o1, DataTypeInfo o2)
        {
            int result = 0;
            if (o1 != null && o1.getDisplayName() != null && o2 != null && o2.getDisplayName() != null)
            {
                result = o1.getDisplayName().toLowerCase().compareTo(o2.getDisplayName().toLowerCase());
            }
            return result;
        }
    };

    /** Comparator that orders {@link DataTypeInfo}s by their display names. */
    Comparator<DataTypeInfo> DISPLAY_NAME_COMPARATOR = new Comparator<>()
    {
        @Override
        public int compare(DataTypeInfo o1, DataTypeInfo o2)
        {
            int result = 0;
            if (o1 != null && o1.getDisplayName() != null && o2 != null && o2.getDisplayName() != null)
            {
                result = o1.getDisplayName().compareTo(o2.getDisplayName());
            }
            return result;
        }
    };

    /** Predicate that selects data types that have meta data. */
    Predicate<DataTypeInfo> HAS_METADATA_PREDICATE = input -> input.getMetaDataInfo() != null;

    /** The Constant NO_EVENT_SOURCE. */
    Object NO_EVENT_SOURCE = new Object();

    /**
     * Property descriptor for {@link DataTypeInfo}s used in the data registry.
     */
    PropertyDescriptor<DataTypeInfo> PROPERTY_DESCRIPTOR = new PropertyDescriptor<>(DataTypeInfo.class.getName(),
            DataTypeInfo.class);

    /**
     * Converts the data type to a display string, or null.
     *
     * @param dataType the data type
     * @return the display string, or null
     */
    static String toDisplayString(DataTypeInfo dataType)
    {
        return dataType != null ? dataType.getDisplayName() : null;
    }

    /**
     * Adds a tag to this data type.
     *
     * @param tag the tag to add ( null not allowed ).
     * @param source the source of the change.
     */
    void addTag(String tag, Object source);

    /**
     * Asks the user the choose time column(s).
     */
    void changeTimeColumns();

    /**
     * Clears all tags for this data source.
     *
     * @param source the source of the change.
     */
    void clearTags(Object source);

    /**
     * Fires a {@link AbstractDataTypeInfoChangeEvent} to subscribers via the
     * {@link EventManager}.
     *
     * @param evt - the change event.
     */
    void fireChangeEvent(AbstractDataTypeInfoChangeEvent evt);

    /**
     * The associated view assigned to the data type.
     *
     * @return The associated view.
     */
    String getAssociatedView();

    /**
     * Gets the BasicVisualizationInfo.
     *
     * @return the BasicVisualziationInfo
     */
    BasicVisualizationInfo getBasicVisualizationInfo();

    /**
     * Gets the description of the data type.
     *
     * @return the description
     */
    String getDescription();

    /**
     * Gets the display name for the type.
     *
     * @return the display name for the type.
     */
    String getDisplayName();

    /**
     * Gets the bounding box.
     *
     * @return the bounding box
     */
    GeographicBoundingBox getBoundingBox();

    /**
     * Gets the MapVisualizationInfo. May be null if this DataTypeInfo does not
     * support display on the map.
     *
     * @return the MapVisualizationInfo
     */
    MapVisualizationInfo getMapVisualizationInfo();

    /**
     * Gets the {@link MetaDataInfo} for this type if any ( may be null if not
     * supplied ).
     *
     * @return the {@link MetaDataInfo} or null.
     */
    MetaDataInfo getMetaDataInfo();

    /**
     * Get the key for participation in order management.
     *
     * @return the order key.
     */
    OrderParticipantKey getOrderKey();

    /**
     * Gets the parent for this data type.
     *
     * @return The parent containing this data type.
     */
    DataGroupInfo getParent();

    /**
     * Gets the name of the source of this data. If it comes from a server it
     * would be the server name, if from CSV file it would be "CSV". etc.
     *
     * @return the source prefix
     */
    String getSourcePrefix();

    /**
     * Returns a string that composites the source prefix ( if not null ) with
     * the display name in the following format: "sourcePrefix/displayName".
     *
     * @return the combo.
     */
    String getSourcePrefixAndDisplayNameCombo();

    /**
     * Gets the streaming support for this data type.
     *
     * @return the streaming support for this data type
     */
    StreamingSupport getStreamingSupport();

    /**
     * Gets the complete set of tags for this data type.
     *
     * @return an immutable set of tags for this data type.
     */
    Set<String> getTags();

    /**
     * Gets the {@link TimeExtents} for this type, if available will be null if
     * not available.
     *
     * @return the {@link TimeExtents} or null if not available.
     */
    TimeExtents getTimeExtents();

    /**
     * Gets the fully qualified unique key for this type.
     *
     * @return the type key
     */
    String getTypeKey();

    /**
     * Gets the type name ( not fully qualified ).
     *
     * @return the type name
     */
    String getTypeName();

    /**
     * Gets the URL for this data type if available.
     *
     * @return the URL, or null
     */
    String getUrl();

    /**
     * Get if this data type has details to be displayed.
     *
     * @return true if this data type has display details
     */
    boolean hasDetails();

    /**
     * Checks for a particular tag on this data type.
     *
     * @param tag the tag to check for
     * @return true if this data type has the specified tag.
     */
    boolean hasTag(String tag);

    /**
     * Checks if this data type info is editable.
     *
     * @return true , if it is editable.
     */
    boolean isEditable();

    /**
     * Checks if this data type info is a filterable type.
     *
     * @return true, if is filterable
     */
    boolean isFilterable();

    /**
     * Returns true if there are registered users of this {@link DataTypeInfo}
     * that would prefer changes not be made to the DataTypeInfo if possible.
     *
     * @return true if registered users, false if not
     */
    boolean isInUse();

    /**
     * Checks if is in use by a particular registrant.
     *
     * @param registrant the registrant
     * @return true, if is in use by this registrant.
     */
    boolean isInUseBy(Object registrant);

    /**
     * Get if this data type can be queried.
     *
     * @return If this type can be queried.
     */
    boolean isQueryable();

    /**
     * Returns true if the time column(s) are changeable by the user.
     *
     * @return true if the timem column(s) are changeable, false otherwise
     */
    boolean isTimeColumnChangeable();

    /**
     * Gets the visible state ( visible to user ).
     *
     * Note: This flag is what would be set/checked by the old "data" tab when a
     * type is active in a timeline but the feature or tile data type is being
     * temporarily toggled on and off by the user.
     *
     * @return the visible
     */
    boolean isVisible();

    /**
     * Launches the editor for this type.
     *
     * @param dataGroup The data group containing the type to edit.
     * @param dataTypes The data types to edit.
     */
    void launchEditor(DataGroupInfo dataGroup, Collection<? extends DataTypeInfo> dataTypes);

    /**
     * Returns true if the provider of this DataType is capable of and takes
     * responsibility for filtering metadata using the
     * {@link DataFilterRegistry} in the core {@link Toolbox}. If false the
     * Mantle layer will provide the filtering for data at insert time.
     *
     * @return true, if provider will filter.
     */
    boolean providerFiltersMetaData();

    /**
     * Registers an object that is using this DataTypeInfo and would prefer if
     * no changes were made to the type description. Note: Held as a weak
     * reference, caller should make sure a strong reference exists to the
     * registrant to ensure it is not garbage collected.
     *
     * If the number of registered goes from 0 to any value an event will be
     * dispatched notifying listeners that this DataTypeInfo is now in use.
     *
     * @param registrant - the object making the registration.
     * @param fireEvent True if the event should be fired false otherwise.
     */
    void registerInUse(Object registrant, boolean fireEvent);

    /**
     * Removes a tag from this data type.
     *
     * @param tag the tag to remove
     * @param source the source of the change.
     * @return true if removed false if not in set.
     */
    boolean removeTag(String tag, Object source);

    /**
     * Set the order key for participation in order management.
     *
     * @param key the order key for participation in order management.
     */
    void setOrderKey(OrderParticipantKey key);

    /**
     * Sets the parent of the data type.
     *
     * @param parent The parent.
     */
    void setParent(DataGroupInfo parent);

    /**
     * Set if this data type can be queried.
     *
     * @param queryable If this type can be queried.
     */
    void setQueryable(boolean queryable);

    /**
     * Sets the type name.
     *
     * @param typeName the new type name
     */
    void setTypeName(String typeName);

    /**
     * Sets the visible state ( visible to user ).
     *
     * @param visible - true for visible
     * @param source the source object making the change.
     */
    void setVisible(boolean visible, Object source);

    /**
     * Unregisters a using object from the data type.
     *
     * Note that if the number of registered users drops to zero an event will
     * be dispatched notifying listeners that that this data type is no longer
     * "in-use".
     *
     * @param registrant - the object that is unregistering
     */
    void unregisterInUse(Object registrant);

    /**
     * Gets whether the data type is in an alert state.
     *
     * @return the alert state
     */
    boolean isAlert();

    /**
     * Sets whether the data type is in an alert state.
     *
     * @param isAlert the alert state
     */
    void setAlert(boolean isAlert);

    /**
     * Gets the assistant.
     *
     * @return the assistant
     */
    DataTypeInfoAssistant getAssistant();

    /**
     * Sets the assistant.
     *
     * @param assistant the assistant
     */
    void setAssistant(DataTypeInfoAssistant assistant);
}

package io.opensphere.mantle.icon;

import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import gnu.trove.list.TIntList;
import io.opensphere.mantle.iconproject.model.IconManagerPrefs;
import io.opensphere.mantle.iconproject.model.PanelModel;

/**
 * The Interface IconRegistry.
 */
public interface IconRegistry
{
    /** The Constant DEFAULT_ICON_URL. */
    URL DEFAULT_ICON_URL = IconRegistry.class.getClassLoader().getResource("images/Location_Pin_32.png");

    /**
     * Adds the icon.
     *
     * @param provider the {@link IconProvider}
     * @param source the source of the add
     * @return the {@link IconRecord} for the added icon.
     */
    IconRecord addIcon(IconProvider provider, Object source);

    /**
     * Adds the icons to the registry. A list of icon ids in the same order of
     * the provider list is returned.
     *
     * @param providerList the provider list
     * @param source the source of the add
     * @return the {@link List} of {@link IconRecord} for the inserted
     *         providers.
     */
    List<IconRecord> addIcons(List<? extends IconProvider> providerList, Object source);

    /**
     * Adds the icons to the registry. A list of icon ids in the same order of
     * the provider list is returned.
     *
     * @param providerList the provider list
     * @param ids the ids to use for the providers
     * @param source the source of the add
     * @return the {@link List} of {@link IconRecord} for the inserted
     *         providers.
     */
    List<IconRecord> addIcons(List<? extends IconProvider> providerList, List<? extends Integer> ids, Object source);

    /**
     * Adds the listener to the registry.
     *
     * NOTE: Listener is held as a weak reference.
     *
     * @param listener the listener
     */
    void addListener(IconRegistryListener listener);

    /**
     * Clear icon for element.
     *
     * @param elementId the element id
     * @param source the source initiating the change
     */
    void clearIconForElement(long elementId, Object source);

    /**
     * Clear icons for all the elements in the list.
     *
     * @param elementIds the element ids to clear icons.
     * @param source the source initiating the change
     */
    void clearIconsForElements(Collection<Long> elementIds, Object source);

    /**
     * Gets the all assigned element ids.
     *
     * @return the all assigned element ids
     */
    List<Long> getAllAssignedElementIds();

    /**
     * Gets the set of all collection names in use in the registry.
     *
     * @return the collection names
     */
    Set<String> getCollectionNames();

    /**
     * Gets the element ids for icon id.
     *
     * @param iconId the icon id
     * @return the element ids for icon id
     */
    List<Long> getElementIdsForIconId(int iconId);

    /**
     * Gets the icon cache.
     *
     * @return the icon cache
     */
    IconCache getIconCache();

    /**
     * Gets the first icon id that has the specified url.
     *
     * @param iconURL the icon url
     * @return the icon id or -1 if not found.
     */
    int getIconId(URL iconURL);

    /**
     * Gets the icon id for element id.
     *
     * @param elementId the element id
     * @return the icon id for element or -1 if not found.
     */
    int getIconIdForElement(long elementId);

    /**
     * Gets the icon ids for all the icons in the registry.
     *
     * @return the icon ids
     */
    TIntList getIconIds();

    /**
     * Gets the icon ids for the {@link IconRecord}s that match the
     * {@link Predicate}.
     *
     * @param filter the {@link Predicate} of {@link IconRecord}, accepts all if
     *            filter is null.
     * @return the icon ids that match the filter criteria.
     */
    TIntList getIconIds(Predicate<IconRecord> filter);

    /**
     * Gets the first icon in the registry with the specified URL or returns
     * null if not found.
     *
     * @param iconURL the icon url
     * @return the icon record or null if not found
     */
    IconRecord getIconRecord(URL iconURL);

    /**
     * Gets the icon record by icon id.
     *
     * @param iconId the icon id
     * @return the {@link IconRecord} by icon id
     */
    IconRecord getIconRecordByIconId(int iconId);

    /**
     * Gets the icon record for element.
     *
     * @param elementId the element id
     * @return the {@link IconRecord} for element
     */
    IconRecord getIconRecordForElement(long elementId);

    /**
     * Gets the {@link IconRecord}s for the specified ids.
     *
     * May not match 1 to 1 with the ids if the ids in the list are not in the
     * registry.
     *
     * @param iconIds the icon ids
     * @return the icon records
     */
    List<IconRecord> getIconRecords(TIntList iconIds);

    /**
     * Gets the {@link IconRecord}s from the registry that match the
     * {@link Predicate} criteria.
     *
     * @param filter the {@link Predicate} of {@link IconRecord}, accepts all if
     *            filter is null.
     * @return the icon records that match the filter.
     */
    List<IconRecord> getIconRecords(Predicate<IconRecord> filter);

    /**
     * Gets the loaded icon pool.
     *
     * @return the loaded icon pool
     */
    LoadedIconPool getLoadedIconPool();

    /**
     * Gets the sub categories for a collection name.
     *
     * @param collection the collection name
     * @return the set of sub categories for a collection.
     */
    Set<String> getSubCategoiresForCollection(String collection);

    /**
     * Removes the icon.
     *
     * @param record the {@link IconRecord}
     * @param source the source of the remove
     * @return true, if removed
     */
    boolean removeIcon(IconRecord record, Object source);

    /**
     * Removes the icon.
     *
     * @param iconId the icon id
     * @param source the source of the remove
     * @return true, if successful
     */
    boolean removeIcon(int iconId, Object source);

    /**
     * Removes the icons.
     *
     * @param iconIdsToRemove the icon ids to remove
     * @param source the source of the remove
     * @return true, if the registry changed as a result of this call.
     */
    boolean removeIcons(TIntList iconIdsToRemove, Object source);

    /**
     * Deletes the icon file from the machine.
     *
     * @param iconToDelete the IconRecord to delete.
     * @param panelModel used for registry.
     */
    void deleteIcon(IconRecord iconToDelete, PanelModel panelModel);

    /**
     * Removes the listener.
     *
     * NOTE: Listener is held as a weak reference.
     *
     * @param listener the listener
     */
    void removeListener(IconRegistryListener listener);

    /**
     * Sets the icon for element.
     *
     * Note: Does not check to see if the {@link IconRecord} is part of the
     * registry.
     *
     * @param elementId the element id
     * @param record the {@link IconRecord} to link with the data element id, or
     *            data element assign if null.
     * @param source the source initiating the change
     */
    void setIconForElement(long elementId, IconRecord record, Object source);

    /**
     * Sets the icon for element.
     *
     * Note: Does not check to see if the iconId is valid for the registry.
     *
     * @param elementId the element id
     * @param iconId the icon id
     * @param source the source initiating the change
     */
    void setIconForElement(long elementId, int iconId, Object source);

    /**
     * Sets the icon for elements by id.
     *
     * @param elementIds the element ids
     * @param record the {@link IconRecord}
     * @param source the source
     */
    void setIconForElements(List<Long> elementIds, IconRecord record, Object source);

    /**
     * Sets the icon for elements.
     *
     * Note: Does not check to see if the iconId is valid for the registry.
     *
     * @param elementIds the element ids
     * @param iconId the icon id
     * @param source the source initiating the change
     */
    void setIconForElements(List<Long> elementIds, int iconId, Object source);

    /**
     * Sets up the preferences to be used when using the program. Are reset on
     * the start up of the program to their default values.
     *
     * @return IconManagerPrefs the startup valeus for the tree selection, tile
     *         width, and view selection.
     */
    IconManagerPrefs getManagerPrefs();
}

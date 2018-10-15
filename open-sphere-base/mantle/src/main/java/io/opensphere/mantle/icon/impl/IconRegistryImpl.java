package io.opensphere.mantle.icon.impl;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Predicate;

import gnu.trove.iterator.TIntIterator;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TLongIntHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import io.opensphere.core.Toolbox;
import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.WeakChangeSupport;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.concurrent.ProcrastinatingExecutor;
import io.opensphere.core.util.concurrent.SuppressableRejectedExecutionHandler;
import io.opensphere.core.util.lang.NamedThreadFactory;
import io.opensphere.core.util.lang.Pair;
import io.opensphere.mantle.icon.IconCache;
import io.opensphere.mantle.icon.IconProvider;
import io.opensphere.mantle.icon.IconRecord;
import io.opensphere.mantle.icon.IconRegistry;
import io.opensphere.mantle.icon.IconRegistryListener;
import io.opensphere.mantle.icon.LoadedIconPool;
import io.opensphere.mantle.icon.config.v1.IconRecordConfig;
import io.opensphere.mantle.icon.config.v1.IconRegistryConfig;
import io.opensphere.mantle.iconproject.model.IconManagerPrefs;
import io.opensphere.mantle.iconproject.model.PanelModel;

/**
 * The Class IconRegistryImpl.
 */
public class IconRegistryImpl implements IconRegistry
{
    /** The key used to store the preferences. */
    private static final String PREFERENCE_KEY = "iconConfig";

    /** The Constant ourExecutor. */
    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(1,
            new NamedThreadFactory("IconRegistryImpl::Dispatch", 3, 4));

    /** The Change support. */
    private final WeakChangeSupport<IconRegistryListener> myChangeSupport;

    /** The Data element id lock. */
    private final ReentrantLock myDataElementIdLock;

    /** The Data element id to icon id map. */
    private final TLongIntHashMap myDataElementIdToIconIdMap;

    /** The Icon cache. */
    private final IconCache myIconCache;

    /** The Icon id counter. */
    private final AtomicInteger myIconIdCounter = new AtomicInteger(0);

    /** The Icon id to icon record map. */
    private final TIntObjectHashMap<IconRecord> myIconIdToIconRecordMap;

    /** The Icon record to icon id map. */
    private final TObjectIntHashMap<IconRecord> myIconRecordToIconIdMap;

    /** The Icon registry lock. */
    private final ReentrantLock myIconRegistryLock;

    /** The Loaded icon pool. */
    private final LoadedIconPool myLoadedIconPool;

    /** The preferences. */
    private final Preferences myPrefs;

    /** The executor used for launching updates. */
    private final Executor mySaveExecutor = new ProcrastinatingExecutor(new ScheduledThreadPoolExecutor(2,
            new NamedThreadFactory("IconRegistry::IO"), SuppressableRejectedExecutionHandler.getInstance()), 1000);

    /** The model containing the preferences for the Icon Manager start up. */
    private IconManagerPrefs myIconManagerPrefs = new IconManagerPrefs();

    /**
     * Instantiates a new icon registry impl.
     *
     * @param toolbox the toolbox
     * @param iconCacheLocation the icon cache location
     */
    public IconRegistryImpl(Toolbox toolbox, File iconCacheLocation)
    {
        myPrefs = toolbox.getPreferencesRegistry().getPreferences(IconRegistry.class);
        myDataElementIdLock = new ReentrantLock();
        myDataElementIdToIconIdMap = new TLongIntHashMap();
        myIconRegistryLock = new ReentrantLock();
        myIconIdToIconRecordMap = new TIntObjectHashMap<>();
        myIconRecordToIconIdMap = new TObjectIntHashMap<>();
        myLoadedIconPool = new LoadedIconPoolImpl(toolbox);
        myIconCache = new IconCacheImpl(iconCacheLocation);
        myChangeSupport = new WeakChangeSupport<>();
        load();
    }

    @Override
    public IconRecord addIcon(IconProvider rec, final Object source)
    {
        return addIcon(rec, source, true);
    }

    @Override
    public List<IconRecord> addIcons(List<? extends IconProvider> providerList, final Object source)
    {
        return addIcons(providerList, null, source, true);
    }

    @Override
    public List<IconRecord> addIcons(List<? extends IconProvider> providerList, List<? extends Integer> ids, Object source)
    {
        return addIcons(providerList, ids, source, true);
    }

    @Override
    public void addListener(IconRegistryListener listener)
    {
        myChangeSupport.addListener(listener);
    }

    @Override
    public void clearIconForElement(final long deId, final Object source)
    {
        myDataElementIdLock.lock();
        try
        {
            myDataElementIdToIconIdMap.remove(deId);
        }
        finally
        {
            myDataElementIdLock.unlock();
        }
        myChangeSupport.notifyListeners(
            listener -> listener.iconsUnassigned(Collections.singletonList(Long.valueOf(deId)), source), EXECUTOR);
    }

    @Override
    public void clearIconsForElements(final Collection<Long> deIds, final Object source)
    {
        myDataElementIdLock.lock();
        try
        {
            for (Long deId : deIds)
            {
                if (deId != null)
                {
                    myDataElementIdToIconIdMap.remove(deId.longValue());
                }
            }
        }
        finally
        {
            myDataElementIdLock.unlock();
        }
        final List<Long> deIdList = Collections.unmodifiableList(New.list(deIds));
        myChangeSupport.notifyListeners(listener -> listener.iconsUnassigned(deIdList, source), EXECUTOR);
    }

    @Override
    public List<Long> getAllAssignedElementIds()
    {
        List<Long> idList = null;
        myDataElementIdLock.lock();
        try
        {
            long[] ids = myDataElementIdToIconIdMap.keys();
            if (ids != null && ids.length > 0)
            {
                idList = CollectionUtilities.listView(ids);
            }
        }
        finally
        {
            myDataElementIdLock.unlock();
        }
        return idList == null ? Collections.<Long>emptyList() : idList;
    }

    @Override
    public Set<String> getCollectionNames()
    {
        final Set<String> nameSet = New.set();
        nameSet.add(IconRecord.DEFAULT_COLLECTION);
        nameSet.add(IconRecord.USER_ADDED_COLLECTION);
        nameSet.add(IconRecord.FAVORITES_COLLECTION);
        myIconRegistryLock.lock();
        try
        {
            myIconIdToIconRecordMap.forEachEntry((iconId, rec) ->
            {
                nameSet.add(rec.getCollectionName());
                return true;
            });
        }
        finally
        {
            myIconRegistryLock.unlock();
        }
        return nameSet;
    }

    @Override
    public List<Long> getElementIdsForIconId(final int iconId)
    {
        final List<Long> resultList = New.linkedList();
        myDataElementIdLock.lock();
        try
        {
            myDataElementIdToIconIdMap.forEachEntry((deId, icId) ->
            {
                if (iconId == icId)
                {
                    resultList.add(Long.valueOf(deId));
                }
                return true;
            });
        }
        finally
        {
            myDataElementIdLock.unlock();
        }
        return resultList.isEmpty() ? Collections.<Long>emptyList() : New.list(resultList);
    }

    @Override
    public IconCache getIconCache()
    {
        return myIconCache;
    }

    @Override
    public int getIconId(URL iconURL)
    {
        Utilities.checkNull(iconURL, "iconURL");
        IconRecord rec = getIconRecord(iconURL);
        return rec == null ? -1 : rec.getId();
    }

    @Override
    public int getIconIdForElement(long deId)
    {
        int resultIconId = -1;
        myDataElementIdLock.lock();
        try
        {
            resultIconId = myDataElementIdToIconIdMap.get(deId);
        }
        finally
        {
            myDataElementIdLock.unlock();
        }
        return resultIconId == 0 ? -1 : resultIconId;
    }

    @Override
    public TIntList getIconIds()
    {
        int[] result = null;
        myIconRegistryLock.lock();
        try
        {
            result = myIconIdToIconRecordMap.keys();
        }
        finally
        {
            myIconRegistryLock.unlock();
        }
        return new TIntArrayList(result);
    }

    @Override
    public TIntList getIconIds(final Predicate<IconRecord> filter)
    {
        final TIntList resultList = new TIntArrayList();
        myIconRegistryLock.lock();
        try
        {
            myIconIdToIconRecordMap.forEachEntry((iconId, record) ->
            {
                if (filter == null || filter.test(record))
                {
                    resultList.add(iconId);
                }
                return true;
            });
        }
        finally
        {
            myIconRegistryLock.unlock();
        }
        return resultList;
    }

    @Override
    public IconRecord getIconRecord(URL iconURL)
    {
        Utilities.checkNull(iconURL, "iconURL");
        final List<IconRecord> recList = New.list(1);
        final String urlStr = iconURL.toString();
        myIconRegistryLock.lock();
        try
        {
            myIconIdToIconRecordMap.forEachEntry((iconId, record) ->
            {
                if (Objects.equals(urlStr, record.getImageURL().toString()))
                {
                    recList.add(record);
                    return false;
                }
                return true;
            });
        }
        finally
        {
            myIconRegistryLock.unlock();
        }
        return recList.isEmpty() ? null : recList.get(0);
    }

    @Override
    public IconRecord getIconRecordByIconId(int iconId)
    {
        IconRecord result = null;
        myIconRegistryLock.lock();
        try
        {
            result = myIconIdToIconRecordMap.get(iconId);
        }
        finally
        {
            myIconRegistryLock.unlock();
        }
        return result;
    }

    @Override
    public IconRecord getIconRecordForElement(long deId)
    {
        int iconId = getIconIdForElement(deId);
        IconRecord result = null;
        if (iconId != -1)
        {
            result = getIconRecordByIconId(iconId);
        }
        return result;
    }

    @Override
    public List<IconRecord> getIconRecords(TIntList iconIds)
    {
        final List<IconRecord> resultList = New.linkedList();
        if (iconIds != null && !iconIds.isEmpty())
        {
            myIconRegistryLock.lock();
            try
            {
                for (TIntIterator iter = iconIds.iterator(); iter.hasNext();)
                {
                    IconRecord rec = myIconIdToIconRecordMap.get(iter.next());
                    if (rec != null)
                    {
                        resultList.add(rec);
                    }
                }
            }
            finally
            {
                myIconRegistryLock.unlock();
            }
        }
        return resultList.isEmpty() ? Collections.<IconRecord>emptyList() : New.list(resultList);
    }

    @Override
    public List<IconRecord> getIconRecords(final Predicate<IconRecord> filter)
    {
        final List<IconRecord> resultList = New.linkedList();
        myIconRegistryLock.lock();
        try
        {
            myIconIdToIconRecordMap.forEachEntry((iconId, record) ->
            {
                if (filter == null || filter.test(record))
                {
                    resultList.add(record);
                }
                return true;
            });
        }
        finally
        {
            myIconRegistryLock.unlock();
        }
        return resultList.isEmpty() ? Collections.<IconRecord>emptyList() : New.list(resultList);
    }

    @Override
    public LoadedIconPool getLoadedIconPool()
    {
        return myLoadedIconPool;
    }

    @Override
    public Set<String> getSubCategoiresForCollection(final String collection)
    {
        final Set<String> subCatSet = New.set();
        myIconRegistryLock.lock();
        try
        {
            myIconIdToIconRecordMap.forEachEntry((iconId, record) ->
            {
                if (record.getSubCategory() != null && Objects.equals(collection, record.getCollectionName()))
                {
                    subCatSet.add(record.getSubCategory());
                }
                return true;
            });
        }
        finally
        {
            myIconRegistryLock.unlock();
        }
        return subCatSet;
    }

    @Override
    public boolean removeIcon(IconRecord rec, Object source)
    {
        return rec != null && removeIcon(rec.getId(), source);
    }

    @Override
    public boolean removeIcon(final int iconId, final Object source)
    {
        IconRecord removedRecord = null;
        myIconRegistryLock.lock();
        try
        {
            removedRecord = myIconIdToIconRecordMap.remove(iconId);
            myIconRecordToIconIdMap.remove(removedRecord);
        }
        finally
        {
            myIconRegistryLock.unlock();
        }
        if (removedRecord != null)
        {
            // Clear any data element ids associated with this icon id.
            final List<Long> foundDeIdList = getElementIdsForIconId(iconId);
            if (!foundDeIdList.isEmpty())
            {
                clearIconsForElements(foundDeIdList, source);
            }

            final IconRecord fRemoved = removedRecord;
            myChangeSupport.notifyListeners(listener -> listener.iconsRemoved(Collections.singletonList(fRemoved), source),
                    EXECUTOR);
            saveLater();
        }
        return removedRecord != null;
    }

    @Override
    public boolean removeIcons(TIntList iconIdsToRemove, final Object source)
    {
        Utilities.checkNull(iconIdsToRemove, "iconIdsToRemove");
        boolean changed = false;

        if (!iconIdsToRemove.isEmpty())
        {
            // Remove all the records.
            IconRecord removedRecord = null;
            final List<IconRecord> removedRecords = New.linkedList();
            myIconRegistryLock.lock();
            try
            {
                for (TIntIterator iter = iconIdsToRemove.iterator(); iter.hasNext();)
                {
                    removedRecord = myIconIdToIconRecordMap.remove(iter.next());
                    if (removedRecord != null)
                    {
                        myIconRecordToIconIdMap.remove(removedRecord);
                        removedRecords.add(removedRecord);
                    }
                }
            }
            finally
            {
                myIconRegistryLock.unlock();
            }

            if (!removedRecords.isEmpty())
            {
                changed = true;
                final List<IconRecord> fRemoved = Collections.unmodifiableList(New.list(removedRecords));

                // Clear the assigned icon ids for the removed records.
                List<Long> elementIdList = New.list();
                for (IconRecord rec : fRemoved)
                {
                    List<Long> elIds = getElementIdsForIconId(rec.getId());
                    if (elIds != null && elIds.isEmpty())
                    {
                        elementIdList.addAll(elIds);
                    }
                }
                clearIconsForElements(elementIdList, source);

                // Now notify listeners of removal.
                myChangeSupport.notifyListeners(listener -> listener.iconsRemoved(fRemoved, source), EXECUTOR);
                saveLater();
            }
        }

        return changed;
    }

    @Override
    public void removeListener(IconRegistryListener listener)
    {
        myChangeSupport.removeListener(listener);
    }

    @Override
    public void setIconForElement(long deId, IconRecord rec, Object source)
    {
        if (rec != null)
        {
            setIconForElement(deId, rec.getId(), source);
        }
        else
        {
            clearIconForElement(deId, source);
        }
    }

    @Override
    public void setIconForElement(final long deId, final int iconId, final Object source)
    {
        myDataElementIdLock.lock();
        try
        {
            myDataElementIdToIconIdMap.put(deId, iconId);
        }
        finally
        {
            myDataElementIdLock.unlock();
        }
        myChangeSupport.notifyListeners(
            listener -> listener.iconAssigned(iconId, Collections.singletonList(Long.valueOf(deId)), source), EXECUTOR);
    }

    @Override
    public void setIconForElements(List<Long> deIds, IconRecord rec, Object source)
    {
        if (rec != null)
        {
            setIconForElements(deIds, rec.getId(), source);
        }
        else
        {
            clearIconsForElements(deIds, source);
        }
    }

    @Override
    public void setIconForElements(final List<Long> deIds, final int iconId, final Object source)
    {
        Utilities.checkNull(deIds, "deIds");
        myDataElementIdLock.lock();
        try
        {
            for (Long deId : deIds)
            {
                myDataElementIdToIconIdMap.put(deId.intValue(), iconId);
            }
        }
        finally
        {
            myDataElementIdLock.unlock();
        }

        myChangeSupport.notifyListeners(listener -> listener.iconAssigned(iconId, Collections.unmodifiableList(deIds), source),
                EXECUTOR);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(64);

        myIconRegistryLock.lock();
        try
        {
            sb.append("IconRegistryImpl:\n  IconRecords: ").append(myIconIdToIconRecordMap.size()).append('\n');
            if (!myIconIdToIconRecordMap.isEmpty())
            {
                int[] keys = myIconIdToIconRecordMap.keys();
                Arrays.sort(keys);
                for (int key : keys)
                {
                    sb.append("    ").append(myIconIdToIconRecordMap.get(key)).append('\n');
                }
            }
        }
        finally
        {
            myIconRegistryLock.unlock();
        }

        myDataElementIdLock.lock();
        try
        {
            sb.append("Assigned Data Elements :").append(myDataElementIdToIconIdMap.size());
            if (!myDataElementIdToIconIdMap.isEmpty())
            {
                sb.append(String.format("    %-20s %-20s%n", "EL_ID", "ICON_ID"));
                long[] keys = myDataElementIdToIconIdMap.keys();
                Arrays.sort(keys);
                for (long key : keys)
                {
                    sb.append(String.format("    %-20d %-20d%n", Long.valueOf(key),
                            Integer.valueOf(myDataElementIdToIconIdMap.get(key))));
                }
            }
        }
        finally
        {
            myDataElementIdLock.unlock();
        }
        return sb.toString();
    }

    /**
     * Adds the icon.
     *
     * @param rec the rec
     * @param source the source
     * @param saveToConfig the save to config
     * @return the added {@link IconRecord}
     */
    private IconRecord addIcon(IconProvider rec, final Object source, boolean saveToConfig)
    {
        Utilities.checkNull(rec, "rec");
        if (rec.getIconURL() == null)
        {
            throw new IllegalArgumentException("Icon provider must have a valid image URL.");
        }
        myIconRegistryLock.lock();
        IconRecord record = null;
        boolean wasAdded = false;
        try
        {
            Pair<IconRecord, Boolean> pair = createRecord(rec, null);
            record = pair.getFirstObject();
            wasAdded = pair.getSecondObject().booleanValue();
        }
        finally
        {
            myIconRegistryLock.unlock();
        }
        if (wasAdded)
        {
            final IconRecord fAddedRec = record;
            myChangeSupport.notifyListeners(listener -> listener.iconsAdded(Collections.singletonList(fAddedRec), source),
                    EXECUTOR);
            if (saveToConfig)
            {
                saveLater();
            }
        }
        return record;
    }

    /**
     * Adds the icons.
     *
     * @param providerList the provider list
     * @param ids the IDs of the providers, or null
     * @param source the source
     * @param saveToConfig the save to config
     * @return the list
     */
    private List<IconRecord> addIcons(List<? extends IconProvider> providerList, List<? extends Integer> ids, final Object source,
            boolean saveToConfig)
    {
        Utilities.checkNull(providerList, "providerList");
        final List<IconRecord> addedList = New.linkedList();
        if (!providerList.isEmpty())
        {
            myIconRegistryLock.lock();
            try
            {
                for (int i = 0; i < providerList.size(); i++)
                {
                    IconProvider provider = providerList.get(i);
                    Integer overrideId = ids != null ? ids.get(i) : null;
                    IconRecord record = createRecord(provider, overrideId).getFirstObject();
                    addedList.add(record);
                }
            }
            finally
            {
                myIconRegistryLock.unlock();
            }
        }
        if (addedList.isEmpty())
        {
            return Collections.<IconRecord>emptyList();
        }
        final List<IconRecord> fAdded = Collections.unmodifiableList(New.list(addedList));
        myChangeSupport.notifyListeners(listener -> listener.iconsAdded(fAdded, source), EXECUTOR);
        if (saveToConfig)
        {
            saveLater();
        }
        return New.list(fAdded);
    }

    /**
     * Creates an IconRecord from an IconProvider.
     *
     * @param provider the icon provider
     * @param overrideId the ID to use, or null
     * @return the IconRecord and whether it was created anew
     */
    private Pair<IconRecord, Boolean> createRecord(IconProvider provider, Integer overrideId)
    {
        IconRecord record = new DefaultIconRecord(0, provider);
        boolean wasAdded = false;
        int id = myIconRecordToIconIdMap.get(record);
        if (id == 0)
        {
            record = new DefaultIconRecord(getId(provider, overrideId), provider);
            myIconIdToIconRecordMap.put(record.getId(), record);
            myIconRecordToIconIdMap.put(record, record.getId());
            wasAdded = true;
        }
        else
        {
            record = myIconIdToIconRecordMap.get(id);
        }
        return new Pair<>(record, Boolean.valueOf(wasAdded));
    }

    /**
     * Gets the icon ID for the provider.
     *
     * @param provider the provider
     * @param overrideId the ID to use, or null
     * @return the icon ID
     */
    private int getId(IconProvider provider, Integer overrideId)
    {
        if (overrideId != null)
        {
            return overrideId.intValue();
        }

        /* If the value came from the config, use it. If it's a legacy config
         * the version won't be there so we need to use the counter to calculate
         * the correct ID. If it's being added in code (not config) then just
         * one-up from the highest value */
        int id;
        if (provider instanceof IconRecordConfig)
        {
            id = ((IconRecordConfig)provider).getId();
            if (id == 0)
            {
                id = myIconIdCounter.incrementAndGet();
            }
        }
        else
        {
            id = Arrays.stream(myIconIdToIconRecordMap.keys()).max().orElse(0) + 1;
        }
        return id;
    }

    /** Loads the icons. */
    private void load()
    {
        IconRegistryConfig config = myPrefs.getJAXBObject(IconRegistryConfig.class, PREFERENCE_KEY, new IconRegistryConfig());
        if (config != null && config.getIconRecords() != null && !config.getIconRecords().isEmpty())
        {
            addIcons(New.list(config.getIconRecords()), null, this, false);
        }

        addIcon(new DefaultIconProvider(DEFAULT_ICON_URL, IconRecord.DEFAULT_COLLECTION, null, null), null, false);

        // Clean up duplicates of the default icon
        TIntList iconIds = getIconIds(r -> IconRecord.DEFAULT_COLLECTION.equals(r.getCollectionName()) && r.getSourceKey() == null
                && !DEFAULT_ICON_URL.equals(r.getImageURL()));
        if (!iconIds.isEmpty())
        {
            removeIcons(iconIds, this);
        }
    }

    /** Saves the icons on the save executor. */
    private void saveLater()
    {
        mySaveExecutor.execute(this::save);
    }

    /** Saves the icons. */
    private void save()
    {
        IconRegistryConfig config = new IconRegistryConfig();

        myIconRegistryLock.lock();
        try
        {
            List<IconRecordConfig> ircList = New.list(myIconIdToIconRecordMap.size());
            for (IconRecord ir : myIconIdToIconRecordMap.valueCollection())
            {
                ircList.add(new IconRecordConfig(ir));
            }
            config.getIconRecords().addAll(ircList);
        }
        finally
        {
            myIconRegistryLock.unlock();
        }
        myPrefs.putJAXBObject(PREFERENCE_KEY, config, false, this);
    }

    /**
     * Physically removes Icons from your machine and simultaneously deletes
     * duplicates which may appear in the registry.
     *
     * @param iconToDelete the icon selected for deletion.
     * @param thePanelModel the model to use for registry.
     */
    @Override
    public void deleteIcon(IconRecord iconToDelete, PanelModel thePanelModel)
    {
        String filename = iconToDelete.getImageURL().toString();
        // This loop logic is to make sure it is removed from all the
        // directories. The null check ensures it works on all machines since
        // icon # may change on computer to computer.
        for (int idx = 0; idx <= thePanelModel.getIconRegistry().getIconIds().max(); idx++)
        {
            IconRecord iconRecord = thePanelModel.getIconRegistry().getIconRecordByIconId(idx);
            if (iconRecord != null)
            {
                if (iconRecord.getImageURL().toString().equals(filename))
                {
                    thePanelModel.getIconRegistry().removeIcon(this.getIconRecordByIconId(idx), this);
                }
            }
        }
        filename = filename.replace("file:", "");
        filename = filename.replace("%20", " ");
        File iconActual = new File(filename);
        iconActual.delete();
    }

    @Override
    public IconManagerPrefs getManagerPrefs()
    {
        return myIconManagerPrefs;
    }
}

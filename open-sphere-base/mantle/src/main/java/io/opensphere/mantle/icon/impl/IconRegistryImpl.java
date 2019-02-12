package io.opensphere.mantle.icon.impl;

import java.io.File;
import java.io.IOException;
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
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Predicate;

import org.apache.log4j.Logger;

import gnu.trove.iterator.TIntIterator;
import gnu.trove.iterator.TLongIterator;
import gnu.trove.list.TIntList;
import gnu.trove.list.TLongList;
import gnu.trove.list.array.TLongArrayList;
import gnu.trove.map.hash.TLongLongHashMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import gnu.trove.map.hash.TObjectLongHashMap;
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
import io.opensphere.mantle.icon.chooser.model.IconManagerPrefs;
import io.opensphere.mantle.icon.chooser.model.IconModel;
import io.opensphere.mantle.icon.config.v1.IconRecordConfig;
import io.opensphere.mantle.icon.config.v1.IconRegistryConfig;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * The Class IconRegistryImpl.
 */
public class IconRegistryImpl implements IconRegistry
{
    /** The logger used to capture output from instances of this class. */
    private static final Logger LOG = Logger.getLogger(IconRegistryImpl.class);

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
    private final TLongLongHashMap myDataElementIdToIconIdMap;

    /** The Icon cache. */
    private final IconCache myIconCache;

    /** The Icon id counter. */
    private final AtomicLong myIconIdCounter = new AtomicLong(0);

    /** An observable list of icon records. */
    private final ObservableList<IconRecord> myIconRecords;

    /** The Icon id to icon record map. */
    private final TLongObjectHashMap<IconRecord> myIconIdToIconRecordMap;

    /** The Icon record to icon id map. */
    private final TObjectLongHashMap<IconRecord> myIconRecordToIconIdMap;

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
    private final IconManagerPrefs myIconManagerPrefs = new IconManagerPrefs();

    /** An observable list of collection names. */
    private final ObservableList<String> myCollectionNames = FXCollections.observableArrayList();

    /**
     * Instantiates a new icon registry impl.
     *
     * @param toolbox the toolbox
     * @param iconCacheLocation the icon cache location
     */
    public IconRegistryImpl(final Toolbox toolbox, final File iconCacheLocation)
    {
        myPrefs = toolbox.getPreferencesRegistry().getPreferences(IconRegistry.class);
        myDataElementIdLock = new ReentrantLock();
        myDataElementIdToIconIdMap = new TLongLongHashMap();
        myIconRegistryLock = new ReentrantLock();
        myIconIdToIconRecordMap = new TLongObjectHashMap<>();
        myIconRecordToIconIdMap = new TObjectLongHashMap<>();
        myLoadedIconPool = new LoadedIconPoolImpl(toolbox);
        myIconCache = new IconCacheImpl(iconCacheLocation);
        myChangeSupport = new WeakChangeSupport<>();
        myIconRecords = FXCollections.observableArrayList(param -> new Observable[] { param.collectionNameProperty() });
        load();
    }

    @Override
    public IconRecord addIcon(final IconProvider rec, final Object source)
    {
        return addIcon(rec, source, true);
    }

    @Override
    public List<IconRecord> addIcons(final List<? extends IconProvider> providerList, final Object source)
    {
        return addIcons(providerList, null, source, true);
    }

    @Override
    public List<IconRecord> addIcons(final List<? extends IconProvider> providerList, final List<? extends Long> ids,
            final Object source)
    {
        return addIcons(providerList, ids, source, true);
    }

    @Override
    public void addListener(final IconRegistryListener listener)
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
            for (final Long deId : deIds)
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
            final long[] ids = myDataElementIdToIconIdMap.keys();
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
    public ObservableList<String> getCollectionNameSet()
    {
        return myCollectionNames;
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
                nameSet.add(rec.collectionNameProperty().get());
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
    public List<Long> getElementIdsForIconId(final long iconId)
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
    public long getIconId(final URL iconURL)
    {
        Utilities.checkNull(iconURL, "iconURL");
        final IconRecord rec = getIconRecord(iconURL);
        return rec == null ? -1 : rec.idProperty().get();
    }

    @Override
    public long getIconIdForElement(final long deId)
    {
        long resultIconId = -1;
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
    public TLongList getIconIds()
    {
        long[] result = null;
        myIconRegistryLock.lock();
        try
        {
            result = myIconIdToIconRecordMap.keys();
        }
        finally
        {
            myIconRegistryLock.unlock();
        }
        return new TLongArrayList(result);
    }

    @Override
    public TLongList getIconIds(final Predicate<IconRecord> filter)
    {
        final TLongList resultList = new TLongArrayList();
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
    public IconRecord getIconRecord(final URL iconURL)
    {
        Utilities.checkNull(iconURL, "iconURL");
        final List<IconRecord> recList = New.list(1);
        final String urlStr = iconURL.toString();
        myIconRegistryLock.lock();
        try
        {
            myIconIdToIconRecordMap.forEachEntry((iconId, record) ->
            {
                if (Objects.equals(urlStr, record.imageURLProperty().get().toString()))
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
    public IconRecord getIconRecordByIconId(final long iconId)
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
    public IconRecord getIconRecordForElement(final long deId)
    {
        final long iconId = getIconIdForElement(deId);
        IconRecord result = null;
        if (iconId != -1)
        {
            result = getIconRecordByIconId(iconId);
        }
        return result;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.icon.IconRegistry#getIconRecords()
     */
    @Override
    public Collection<IconRecord> getIconRecords()
    {
        return myIconIdToIconRecordMap.valueCollection();
    }

    @Override
    public List<IconRecord> getIconRecords(final TIntList iconIds)
    {
        final List<IconRecord> resultList = New.linkedList();
        if (iconIds != null && !iconIds.isEmpty())
        {
            myIconRegistryLock.lock();
            try
            {
                for (final TIntIterator iter = iconIds.iterator(); iter.hasNext();)
                {
                    final IconRecord rec = myIconIdToIconRecordMap.get(iter.next());
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
    public boolean removeIcon(final IconRecord rec, final Object source)
    {
        return rec != null && removeIcon(rec.idProperty().get(), source);
    }

    @Override
    public boolean removeIcon(final long iconId, final Object source)
    {
        IconRecord removedRecord = null;
        myIconRegistryLock.lock();
        try
        {
            removedRecord = myIconIdToIconRecordMap.remove(iconId);
            myIconRecords.remove(removedRecord);
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
    public boolean removeIcons(final TLongList iconIdsToRemove, final Object source)
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
                for (final TLongIterator iter = iconIdsToRemove.iterator(); iter.hasNext();)
                {
                    removedRecord = myIconIdToIconRecordMap.remove(iter.next());
                    if (removedRecord != null)
                    {
                        myIconRecordToIconIdMap.remove(removedRecord);
                        myIconRecords.remove(removedRecord);
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
                final List<Long> elementIdList = New.list();
                for (final IconRecord rec : fRemoved)
                {
                    final List<Long> elIds = getElementIdsForIconId(rec.idProperty().get());
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
    public void removeListener(final IconRegistryListener listener)
    {
        myChangeSupport.removeListener(listener);
    }

    @Override
    public void setIconForElement(final long deId, final IconRecord rec, final Object source)
    {
        if (rec != null)
        {
            setIconForElement(deId, rec.idProperty().get(), source);
        }
        else
        {
            clearIconForElement(deId, source);
        }
    }

    @Override
    public void setIconForElement(final long deId, final long iconId, final Object source)
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
    public void setIconForElements(final List<Long> deIds, final IconRecord rec, final Object source)
    {
        if (rec != null)
        {
            setIconForElements(deIds, rec.idProperty().get(), source);
        }
        else
        {
            clearIconsForElements(deIds, source);
        }
    }

    @Override
    public void setIconForElements(final List<Long> deIds, final long iconId, final Object source)
    {
        Utilities.checkNull(deIds, "deIds");
        myDataElementIdLock.lock();
        try
        {
            for (final Long deId : deIds)
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
        final StringBuilder sb = new StringBuilder(64);

        myIconRegistryLock.lock();
        try
        {
            sb.append("IconRegistryImpl:\n  IconRecords: ").append(myIconIdToIconRecordMap.size()).append('\n');
            if (!myIconIdToIconRecordMap.isEmpty())
            {
                final long[] keys = myIconIdToIconRecordMap.keys();
                Arrays.sort(keys);
                for (final long key : keys)
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
                final long[] keys = myDataElementIdToIconIdMap.keys();
                Arrays.sort(keys);
                for (final long key : keys)
                {
                    sb.append(String.format("    %-20d %-20d%n", Long.valueOf(key),
                            Long.valueOf(myDataElementIdToIconIdMap.get(key))));
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
    private IconRecord addIcon(final IconProvider rec, final Object source, final boolean saveToConfig)
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
        catch (IOException e)
        {
            LOG.error("Unable to read image for icon from URL '" + rec.getIconURL() + "'");
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
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.icon.IconRegistry#iconStateChanged()
     */
    @Override
    public void iconStateChanged()
    {
        saveLater();
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
    private List<IconRecord> addIcons(final List<? extends IconProvider> providerList, final List<? extends Long> ids,
            final Object source, final boolean saveToConfig)
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
                    final IconProvider provider = providerList.get(i);
                    final Long overrideId = ids != null ? ids.get(i) : null;
                    try
                    {
                        final IconRecord record = createRecord(provider, overrideId).getFirstObject();
                        addedList.add(record);
                    }
                    catch (IOException e)
                    {
                        LOG.error("Unable to read image for icon from URL '" + provider.getIconURL() + "'");
                    }
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
     * @throws IOException if the icon image could not be read
     */
    private Pair<IconRecord, Boolean> createRecord(final IconProvider provider, final Long overrideId) throws IOException
    {
        IconRecord record = new DefaultIconRecord(0, provider);
        boolean wasAdded = false;
        final long id = myIconRecordToIconIdMap.get(record);
        if (id == 0)
        {
            record = new DefaultIconRecord(getId(provider, overrideId), provider);
            record.favoriteProperty().set(provider.isFavorite());
            myIconIdToIconRecordMap.put(record.idProperty().get(), record);
            myIconRecordToIconIdMap.put(record, record.idProperty().get());
            myIconRecords.add(record);
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
    private long getId(final IconProvider provider, final Long overrideId)
    {
        if (overrideId != null)
        {
            return overrideId.longValue();
        }

        /* If the value came from the config, use it. If it's a legacy config
         * the version won't be there so we need to use the counter to calculate
         * the correct ID. If it's being added in code (not config) then just
         * one-up from the highest value */
        long id;
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
        final IconRegistryConfig config = myPrefs.getJAXBObject(IconRegistryConfig.class, PREFERENCE_KEY,
                new IconRegistryConfig());
        if (config != null && config.getIconRecords() != null && !config.getIconRecords().isEmpty())
        {
            addIcons(New.list(config.getIconRecords()), null, this, false);
        }

        addIcon(new DefaultIconProvider(DEFAULT_ICON_URL, IconRecord.DEFAULT_COLLECTION, null), null, false);

        // Clean up duplicates of the default icon
        final TLongList iconIds = getIconIds(r -> IconRecord.DEFAULT_COLLECTION.equals(r.collectionNameProperty().get())
                && r.sourceKeyProperty() == null && !DEFAULT_ICON_URL.equals(r.imageURLProperty().get()));
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
        final IconRegistryConfig config = new IconRegistryConfig();

        myIconRegistryLock.lock();
        try
        {
            final List<IconRecordConfig> ircList = New.list(myIconIdToIconRecordMap.size());
            for (final IconRecord ir : myIconIdToIconRecordMap.valueCollection())
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
    public void deleteIcon(final IconRecord iconToDelete, final IconModel thePanelModel)
    {
        String filename = iconToDelete.imageURLProperty().get().toString();
        // This loop logic is to make sure it is removed from all the
        // directories. The null check ensures it works on all machines since
        // icon # may change on computer to computer.
        for (int idx = 0; idx <= thePanelModel.getIconRegistry().getIconIds().max(); idx++)
        {
            final IconRecord iconRecord = thePanelModel.getIconRegistry().getIconRecordByIconId(idx);
            if (iconRecord != null)
            {
                if (iconRecord.imageURLProperty().get().toString().equals(filename))
                {
                    thePanelModel.getIconRegistry().removeIcon(getIconRecordByIconId(idx), this);
                }
            }
        }
        filename = filename.replace("file:", "");
        filename = filename.replace("%20", " ");
        final File iconActual = new File(filename);
        iconActual.delete();
    }

    @Override
    public IconManagerPrefs getManagerPrefs()
    {
        return myIconManagerPrefs;
    }
}

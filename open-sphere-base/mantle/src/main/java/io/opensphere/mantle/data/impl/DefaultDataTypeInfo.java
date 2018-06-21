package io.opensphere.mantle.data.impl;

import java.awt.Color;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

import net.jcip.annotations.GuardedBy;

import io.opensphere.core.Toolbox;
import io.opensphere.core.datafilter.DataFilterRegistry;
import io.opensphere.core.model.Altitude;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.order.OrderParticipantKey;
import io.opensphere.core.util.ChangeListener;
import io.opensphere.core.util.ObservableValue;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.ToStringHelper;
import io.opensphere.core.util.ref.WeakReference;
import io.opensphere.mantle.data.AbstractDataTypeInfoChangeEvent;
import io.opensphere.mantle.data.BasicVisualizationInfo;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.DataTypeInfoAssistant;
import io.opensphere.mantle.data.DataTypeInfoPreferenceAssistant;
import io.opensphere.mantle.data.LoadsTo;
import io.opensphere.mantle.data.MapVisualizationInfo;
import io.opensphere.mantle.data.MetaDataInfo;
import io.opensphere.mantle.data.StreamingSupport;
import io.opensphere.mantle.data.TimeExtents;
import io.opensphere.mantle.data.event.DataTypeInfoColorChangeEvent;
import io.opensphere.mantle.data.event.DataTypeInfoInUseChangeEvent;
import io.opensphere.mantle.data.event.DataTypeInfoLoadsToChangeEvent;
import io.opensphere.mantle.data.event.DataTypeInfoTagsChangeEvent;
import io.opensphere.mantle.data.event.DataTypeTimeExtentsChangeEvent;
import io.opensphere.mantle.data.event.DataTypeVisibilityChangeEvent;
import io.opensphere.mantle.util.MantleToolboxUtils;

/**
 * Default implementation of a {@link DataTypeInfo} that provides all necessary
 * mantle level events.
 */
@SuppressWarnings("PMD.GodClass")
public class DefaultDataTypeInfo implements DataTypeInfo
{
    /** The Constant TAG_SET_PREFERENCE_PREFIX. */
    private static final String TAG_SET_PREFERENCE_PREFIX = "DTI_TAG_SET:";

    /** The toolbox. */
    private final Toolbox myToolbox;

    /** The source prefix. */
    private final String mySourcePrefix;

    /** The type key. */
    private final String myTypeKey;

    /** The type name. */
    private volatile String myTypeName;

    /** The type display name. */
    private volatile String myDisplayName;

    /** Whether the provider filters meta data. */
    private final boolean myProviderFiltersMetaData;

    /** Optional {@link MetaDataInfo} for this data type. */
    @GuardedBy("this")
    private MetaDataInfo myMetaDataInfo;

    /** The description. */
    private volatile String myDescription;

    /** Basic visualization information. */
    @GuardedBy("this")
    private BasicVisualizationInfo myBasicVisualizationInfo;

    /** Map visualization information. */
    @GuardedBy("this")
    private MapVisualizationInfo myMapVisualizationInfo;

    /** Optional {@link TimeExtents} for this data type. */
    @GuardedBy("this")
    private TimeExtents myTimeExtents;

    /** The bounding box. */
    @GuardedBy("this")
    private GeographicBoundingBox myBoundingBox;

    /** The key for participation in order management. */
    private volatile OrderParticipantKey myOrderKey;

    /** The associated view for the data type. */
    private volatile String myAssociatedView;

    /**
     * Registry for who is using this type such that it should not allow
     * changes.
     */
    @GuardedBy("this")
    private final Set<WeakReference<Object>> myInUseRegistry = new HashSet<>();

    /** The data type's parent. */
    private volatile DataGroupInfo myParent;

    /** Indicates if this type can be queried for data. */
    private volatile boolean myQueryable;

    /** The Tag set. */
    @GuardedBy("myTagSetLock")
    private Set<String> myTagSet;

    /** The Tag set lock. */
    private final ReentrantLock myTagSetLock = new ReentrantLock();

    /** The url. */
    private volatile String myUrl;

    /** The visible flag. */
    private final AtomicBoolean myVisible = new AtomicBoolean(true);

    /** The streaming support. */
    private final StreamingSupportImpl myStreamingSupport;

    /** Change listener for streaming support. */
    private final ChangeListener<Boolean> myStreamingChangeListener;

    /** Whether the data type is in an alert state. */
    private volatile boolean myIsAlert;

    /** The optional assistant. */
    private volatile DataTypeInfoAssistant myAssistant;

    /** Value returned by interface method isFilterable. Default is true. */
    protected boolean myFilterable = true;

    /**
     * Constructor with key, name, display name, and configurable default
     * visibility.
     *
     * @param tb - the tool box
     * @param sourcePrefix the source prefix
     * @param typeKey - the type key
     * @param typeName - the type name
     * @param displayName - the display name.
     * @param providerFiltersMetaData - true if the provider of this DataType is
     *            capable of and takes responsibility for filtering metadata
     *            using the {@link DataFilterRegistry} in the core
     *            {@link Toolbox}. If false the Mantle layer will provide the
     *            filtering for data at insert time.
     */
    public DefaultDataTypeInfo(Toolbox tb, String sourcePrefix, String typeKey, String typeName, String displayName,
            boolean providerFiltersMetaData)
    {
        this(tb, sourcePrefix, typeKey, typeName, displayName, providerFiltersMetaData, null);
    }

    /**
     * Constructor with key, name, and display name.
     *
     * @param tb - the tool box
     * @param sourcePrefix the source prefix
     * @param typeKey - the type key
     * @param typeName - the type name
     * @param displayName - the display name.
     * @param metaData - the {@link MetaDataInfo} for the type
     * @param providerFiltersMetaData - true if the provider of this DataType is
     *            capable of and takes responsibility for filtering metadata
     *            using the {@link DataFilterRegistry} in the core
     *            {@link Toolbox}. If false the Mantle layer will provide the
     *            filtering for data at insert time.
     */
    public DefaultDataTypeInfo(Toolbox tb, String sourcePrefix, String typeKey, String typeName, String displayName,
            boolean providerFiltersMetaData, MetaDataInfo metaData)
    {
        myToolbox = tb;
        mySourcePrefix = sourcePrefix;
        myTypeKey = Utilities.checkNull(typeKey, "typeKey").intern();
        myTypeName = typeName;
        myDisplayName = displayName;
        myProviderFiltersMetaData = providerFiltersMetaData;
        myMetaDataInfo = metaData;
        if (myMetaDataInfo != null)
        {
            myMetaDataInfo.setDataTypeInfo(this);
        }

        if (tb != null)
        {
            DataTypeInfoPreferenceAssistant dataTypeInfoPreferenceAssistant = MantleToolboxUtils.getMantleToolbox(tb)
                    .getDataTypeInfoPreferenceAssistant();
            myStreamingSupport = new StreamingSupportImpl(dataTypeInfoPreferenceAssistant, myTypeKey);
            myVisible.getAndSet(dataTypeInfoPreferenceAssistant.isVisiblePreference(myTypeKey));
        }
        else
        {
            myStreamingSupport = new StreamingSupportImpl(null, myTypeKey);
        }

        // Set to default value
        setBasicVisualizationInfo(null);

        myStreamingChangeListener = new ChangeListener<Boolean>()
        {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue)
            {
                if (newValue.booleanValue())
                {
                    addTag("streaming", DefaultDataTypeInfo.this);
                }
            }
        };
        myStreamingSupport.streamingEnabledProperty().addListener(myStreamingChangeListener);
    }

    @Override
    public final void addTag(String tag, Object source)
    {
        Set<String> changedSet = null;
        myTagSetLock.lock();
        try
        {
            createAndRetrieveTagSetFromPreferencesIfNull();
            if (myTagSet != null && myTagSet.add(tag))
            {
                changedSet = Collections.unmodifiableSet(New.set(myTagSet));
            }
        }
        finally
        {
            myTagSetLock.unlock();
        }
        if (changedSet != null && myToolbox != null)
        {
            myToolbox.getPreferencesRegistry().getPreferences(DefaultDataTypeInfo.class)
                    .putStringSet(TAG_SET_PREFERENCE_PREFIX + myTypeKey, changedSet, this);
            fireChangeEvent(new DataTypeInfoTagsChangeEvent(this, changedSet, source));
        }
    }

    @Override
    public void changeTimeColumns()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public final void clearTags(Object source)
    {
        boolean changed = false;
        myTagSetLock.lock();
        try
        {
            createAndRetrieveTagSetFromPreferencesIfNull();
            changed = !myTagSet.isEmpty();
            myTagSet.clear();
        }
        finally
        {
            myTagSetLock.unlock();
        }
        if (changed)
        {
            myToolbox.getPreferencesRegistry().getPreferences(DefaultDataTypeInfo.class)
                    .remove(TAG_SET_PREFERENCE_PREFIX + myTypeKey, this);
            fireChangeEvent(new DataTypeInfoTagsChangeEvent(this, Collections.<String>emptySet(), source));
        }
    }

    @Override
    public int compareTo(DataTypeInfo o)
    {
        return myDisplayName.compareTo(o.getDisplayName());
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null || getClass() != obj.getClass())
        {
            return false;
        }
        DefaultDataTypeInfo other = (DefaultDataTypeInfo)obj;
        return Objects.equals(myTypeKey, other.myTypeKey);
    }

    @Override
    public void fireChangeEvent(AbstractDataTypeInfoChangeEvent evt)
    {
        if (evt != null && myToolbox != null && !Utilities.sameInstance(evt.getSource(), NO_EVENT_SOURCE))
        {
            myToolbox.getEventManager().publishEvent(evt);
        }
    }

    @Override
    public String getAssociatedView()
    {
        return myAssociatedView;
    }

    @Override
    public synchronized BasicVisualizationInfo getBasicVisualizationInfo()
    {
        return myBasicVisualizationInfo;
    }

    @Override
    public String getDescription()
    {
        return myDescription;
    }

    @Override
    public String getDisplayName()
    {
        return myDisplayName;
    }

    @Override
    public synchronized MapVisualizationInfo getMapVisualizationInfo()
    {
        return myMapVisualizationInfo;
    }

    @Override
    public synchronized MetaDataInfo getMetaDataInfo()
    {
        return myMetaDataInfo;
    }

    @Override
    public OrderParticipantKey getOrderKey()
    {
        return myOrderKey;
    }

    @Override
    public DataGroupInfo getParent()
    {
        return myParent;
    }

    @Override
    public String getSourcePrefix()
    {
        return mySourcePrefix;
    }

    @Override
    public String getSourcePrefixAndDisplayNameCombo()
    {
        return (mySourcePrefix == null ? "" : mySourcePrefix + "/") + myDisplayName;
    }

    @Override
    public StreamingSupport getStreamingSupport()
    {
        return myStreamingSupport;
    }

    @Override
    public final Set<String> getTags()
    {
        Set<String> result = null;
        myTagSetLock.lock();
        try
        {
            createAndRetrieveTagSetFromPreferencesIfNull();
            result = myTagSet != null ? Collections.unmodifiableSet(New.set(myTagSet)) : Collections.emptySet();
        }
        finally
        {
            myTagSetLock.unlock();
        }
        return result;
    }

    @Override
    public synchronized TimeExtents getTimeExtents()
    {
        return myTimeExtents;
    }

    /**
     * Gets the Toolbox assigned to this {@link DefaultDataTypeInfo}.
     *
     * @return the {@link Toolbox}
     */
    public Toolbox getToolbox()
    {
        return myToolbox;
    }

    @Override
    public String getTypeKey()
    {
        return myTypeKey;
    }

    @Override
    public String getTypeName()
    {
        return myTypeName;
    }

    @Override
    public String getUrl()
    {
        return myUrl;
    }

    @Override
    public boolean hasDetails()
    {
        return true;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + (myTypeKey == null ? 0 : myTypeKey.hashCode());
        return result;
    }

    @Override
    public final boolean hasTag(String tag)
    {
        boolean hasTag = false;
        myTagSetLock.lock();
        try
        {
            createAndRetrieveTagSetFromPreferencesIfNull();
            hasTag = myTagSet.contains(tag);
        }
        finally
        {
            myTagSetLock.unlock();
        }
        return hasTag;
    }

    @Override
    public boolean isDisplayable()
    {
        return false;
    }

    @Override
    public boolean isEditable()
    {
        return false;
    }

    @Override
    public boolean isFilterable()
    {
        return myFilterable;
    }

    /**
     * Sets if this layer can be filtered.
     *
     * @param isFilterable True if this layer can be filtered, false otherwise.
     */
    public void setFilterable(boolean isFilterable)
    {
        myFilterable = isFilterable;
    }

    @Override
    public boolean isInUse()
    {
        boolean inUse = false;
        synchronized (myInUseRegistry)
        {
            inUse = !myInUseRegistry.isEmpty();
        }
        return inUse;
    }

    @Override
    public boolean isInUseBy(Object registrant)
    {
        boolean inUse = false;
        if (registrant != null)
        {
            synchronized (myInUseRegistry)
            {
                if (!myInUseRegistry.isEmpty())
                {
                    Iterator<WeakReference<Object>> regItr = myInUseRegistry.iterator();
                    WeakReference<Object> ref = null;

                    // Go through the registry see if we have this registrant.
                    // Also
                    // clean out any registrant that have been garbage
                    // collected.
                    while (regItr.hasNext())
                    {
                        ref = regItr.next();
                        if (ref.get() == null)
                        {
                            regItr.remove();
                        }
                        else if (Utilities.sameInstance(ref.get(), registrant))
                        {
                            inUse = true;
                            break;
                        }
                    }
                }
            }
        }
        return inUse;
    }

    @Override
    public boolean isQueryable()
    {
        return myQueryable;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.data.DataTypeInfo#isTimeColumnChangeable()
     */
    @Override
    public boolean isTimeColumnChangeable()
    {
        return false;
    }

    @Override
    public boolean isVisible()
    {
        return myVisible.get();
    }

    @Override
    public void launchEditor(DataGroupInfo dataGroup, Collection<? extends DataTypeInfo> dataTypes)
    {
    }

    @Override
    public boolean providerFiltersMetaData()
    {
        return myProviderFiltersMetaData;
    }

    @Override
    public void registerInUse(Object registrant, boolean fireEvent)
    {
        synchronized (myInUseRegistry)
        {
            int sizeBefore = myInUseRegistry.size();
            boolean isInRegistry = false;
            if (!myInUseRegistry.isEmpty())
            {
                Iterator<WeakReference<Object>> regItr = myInUseRegistry.iterator();
                WeakReference<Object> ref = null;

                // Go through the registry and make sure that we don't
                // already have this registrant in the set. Also
                // clean out any registrants that have been garbage collected.
                while (regItr.hasNext())
                {
                    ref = regItr.next();
                    if (ref.get() == null)
                    {
                        regItr.remove();
                    }
                    else
                    {
                        if (ref.get() == registrant)
                        {
                            isInRegistry = true;
                        }
                    }
                }
            }
            if (!isInRegistry)
            {
                myInUseRegistry.add(new WeakReference<>(registrant));
            }
            int sizeAfter = myInUseRegistry.size();
            if (sizeBefore == 0 && sizeAfter > 0 && fireEvent)
            {
                fireChangeEvent(new DataTypeInfoInUseChangeEvent(this, true, registrant));
            }
        }
    }

    @Override
    public final boolean removeTag(String tag, Object source)
    {
        Set<String> changedSet = null;
        myTagSetLock.lock();
        try
        {
            createAndRetrieveTagSetFromPreferencesIfNull();
            if (myTagSet.remove(tag))
            {
                changedSet = Collections.unmodifiableSet(New.set(myTagSet));
            }
        }
        finally
        {
            myTagSetLock.unlock();
        }
        if (changedSet != null)
        {
            myToolbox.getPreferencesRegistry().getPreferences(DefaultDataTypeInfo.class)
                    .putStringSet(TAG_SET_PREFERENCE_PREFIX + myTypeKey, changedSet, this);
            fireChangeEvent(new DataTypeInfoTagsChangeEvent(this, changedSet, source));
        }
        return changedSet != null;
    }

    /**
     * Sets the associated view for the data type.
     *
     * @param associatedView The associated view.
     */
    public void setAssociatedView(String associatedView)
    {
        myAssociatedView = associatedView;
    }

    /**
     * Sets the BasicVisualizationInfo for the type.
     *
     * @param bvi - the BasicVisualizationInfo
     */
    @SuppressWarnings("PMD.SimplifiedTernary")
    public final synchronized void setBasicVisualizationInfo(BasicVisualizationInfo bvi)
    {
        if (bvi != null)
        {
            myBasicVisualizationInfo = bvi;
        }
        else
        {
            myBasicVisualizationInfo = new DefaultBasicVisualizationInfo(LoadsTo.BASE, Color.WHITE, false);
            applyColorPreferences();
        }

        myBasicVisualizationInfo.loadsToProperty().addListener((obs, oldValue, newValue) -> fireChangeEvent(
                new DataTypeInfoLoadsToChangeEvent(this, newValue, oldValue, this)));
        myBasicVisualizationInfo.typeColorProperty().addListener((obs, oldValue, newValue) ->
        {
            boolean opacityChangeOnly = oldValue != null && newValue != null && oldValue.getRed() == newValue.getRed()
                    && oldValue.getGreen() == newValue.getGreen() && oldValue.getBlue() == newValue.getBlue()
                    && oldValue.getAlpha() != newValue.getAlpha();
            fireChangeEvent(new DataTypeInfoColorChangeEvent(this, newValue, opacityChangeOnly, this));
        });
    }

    /**
     * Applies color/opacity preferences to the visualization info, if
     * available.
     */
    public synchronized void applyColorPreferences()
    {
        if (myToolbox != null)
        {
            Color typeColor = myBasicVisualizationInfo.getTypeColor();

            DataTypeInfoPreferenceAssistant dataTypeInfoPreferenceAssistant = MantleToolboxUtils.getMantleToolbox(myToolbox)
                    .getDataTypeInfoPreferenceAssistant();

            int prefColor = dataTypeInfoPreferenceAssistant.getColorPreference(myTypeKey, typeColor.getRGB());
            if (prefColor != myBasicVisualizationInfo.getDefaultTypeColor().getRGB())
            {
                typeColor = new Color(prefColor);
            }

            int prefAlpha = dataTypeInfoPreferenceAssistant.getOpacityPreference(myTypeKey, typeColor.getAlpha());
            if (prefAlpha != typeColor.getAlpha())
            {
                typeColor = new Color(typeColor.getRed(), typeColor.getGreen(), typeColor.getBlue(), prefAlpha);
            }

            myBasicVisualizationInfo.setTypeColor(typeColor, this);
        }
    }

    /**
     * Sets the description.
     *
     * @param description the new description
     */
    public void setDescription(String description)
    {
        myDescription = description;
    }

    @Override
    public void setDisplayable(boolean displayable, Object source)
    {
    }

    /**
     * Sets the user friendly name.
     *
     * @param displayName The user friendly name.
     */
    public void setDisplayName(String displayName)
    {
        myDisplayName = displayName;
    }

    /**
     * Adds locations.
     *
     * @param locations the locations
     */
    public void addLocations(Collection<? extends LatLonAlt> locations)
    {
        addBoundingBox(GeographicBoundingBox.getMinimumBoundingBoxWithAlt(locations));
    }

    /**
     * Adds a bounding box.
     *
     * @param bbox the bounding box
     */
    public synchronized void addBoundingBox(GeographicBoundingBox bbox)
    {
        if (myBoundingBox == null)
        {
            myBoundingBox = bbox;
        }
        else
        {
            myBoundingBox = GeographicBoundingBox.merge(myBoundingBox, bbox, Altitude.ReferenceLevel.ELLIPSOID);
        }
    }

    /**
     * Sets the bounding box.
     *
     * @param bbox the bounding box
     */
    public synchronized void setBoundingBox(GeographicBoundingBox bbox)
    {
        myBoundingBox = bbox;
    }

    @Override
    public synchronized GeographicBoundingBox getBoundingBox()
    {
        return myBoundingBox;
    }

    /**
     * Sets the MapVisualizationInfo for the type.
     *
     * @param mvi - the MapVisualizationInfo
     */
    public synchronized void setMapVisualizationInfo(MapVisualizationInfo mvi)
    {
        if (myMapVisualizationInfo != null)
        {
            myMapVisualizationInfo.setDataTypeInfo(null);
        }
        myMapVisualizationInfo = mvi;
        if (myMapVisualizationInfo != null)
        {
            myMapVisualizationInfo.setDataTypeInfo(this);
        }
    }

    /**
     * Gets the {@link MetaDataInfo} for this type, or null if not available or
     * unknown.
     *
     * @param metaDataInfo - the {@link MetaDataInfo} or null.
     */
    public synchronized void setMetaDataInfo(MetaDataInfo metaDataInfo)
    {
        if (myMetaDataInfo != null)
        {
            myMetaDataInfo.setDataTypeInfo(null);
        }
        myMetaDataInfo = metaDataInfo;
        if (myMetaDataInfo != null)
        {
            myMetaDataInfo.setDataTypeInfo(this);
        }
    }

    @Override
    public void setOrderKey(OrderParticipantKey key)
    {
        myOrderKey = key;
    }

    @Override
    public void setParent(DataGroupInfo parent)
    {
        myParent = parent;
    }

    @Override
    public void setQueryable(boolean queryable)
    {
        myQueryable = queryable;
    }

    /**
     * Sets the TimeExtents for this type. Null is valid to indicate TimeExtents
     * are unknown.
     *
     * @param timeExtents - the {@link TimeExtents}
     * @param source the source of the change.
     */
    public void setTimeExtents(TimeExtents timeExtents, Object source)
    {
        TimeExtents oldExtents;
        synchronized (this)
        {
            oldExtents = myTimeExtents;
            myTimeExtents = timeExtents;
        }
        fireChangeEvent(new DataTypeTimeExtentsChangeEvent(this, timeExtents, oldExtents, source));
    }

    @Override
    public void setTypeName(String typeName)
    {
        myTypeName = typeName;
    }

    /**
     * Sets the url.
     *
     * @param url the url
     */
    public void setUrl(String url)
    {
        myUrl = url;
    }

    @Override
    public void setVisible(boolean visible, Object source)
    {
        if (myVisible.getAndSet(visible) != visible)
        {
            fireChangeEvent(new DataTypeVisibilityChangeEvent(this, visible, isPreserveVisibility(), source));
        }
    }

    @Override
    public String toString()
    {
        ToStringHelper helper = new ToStringHelper(DefaultDataTypeInfo.class, 1536);
        helper.add("Type Key", myTypeKey);
        helper.add("Type Name", myTypeName);
        helper.add("Tags", getTags());
        helper.add("Display Name", myDisplayName);
        helper.add("Source Prefix", mySourcePrefix);
        helper.add("Visible", myVisible.get());
        helper.add("Description", myDescription);
        synchronized (this)
        {
            helper.add("Time Extents", myTimeExtents);
            helper.add("Bounding Box", myBoundingBox);
            helper.add("MetaDataInfo", myMetaDataInfo);
            helper.add("BasicVisualizationInfo", myBasicVisualizationInfo);
            helper.add("MapVisualizationInfo", myMapVisualizationInfo);
        }
        return helper.toStringMultiLine();
    }

    @Override
    public void unregisterInUse(Object registrant)
    {
        synchronized (myInUseRegistry)
        {
            int sizeBefore = myInUseRegistry.size();
            if (!myInUseRegistry.isEmpty())
            {
                Iterator<WeakReference<Object>> regItr = myInUseRegistry.iterator();
                WeakReference<Object> ref = null;

                // Go through the registry and make sure that we don't
                // already have this registrant in the set. Also
                // clean out any registrants that have been garbage collected.
                while (regItr.hasNext())
                {
                    ref = regItr.next();
                    if (ref.get() == null || ref.get() == registrant)
                    {
                        regItr.remove();
                    }
                }
            }
            int sizeAfter = myInUseRegistry.size();
            if (sizeBefore > 0 && sizeAfter == 0)
            {
                fireChangeEvent(new DataTypeInfoInUseChangeEvent(this, false, registrant));
            }
        }
    }

    @Override
    public boolean isAlert()
    {
        return myIsAlert;
    }

    @Override
    public void setAlert(boolean isAlert)
    {
        myIsAlert = isAlert;
    }

    @Override
    public DataTypeInfoAssistant getAssistant()
    {
        return myAssistant;
    }

    @Override
    public void setAssistant(DataTypeInfoAssistant assistant)
    {
        myAssistant = assistant;
    }

    /**
     * Overridable method that allows certain DataTypeInfos to opt out of
     * preserving their visibility state between sessions.
     *
     * @return the visibility preservation flag
     */
    protected boolean isPreserveVisibility()
    {
        return true;
    }

    /**
     * Lazily creates the and retrieve tag set from preferences. Note: Make sure
     * to call from within the tag set lock.
     */
    private void createAndRetrieveTagSetFromPreferencesIfNull()
    {
        if (myTagSet == null)
        {
            Set<String> defaultTagSet = New.set();
            myTagSet = myToolbox == null ? defaultTagSet
                    : New.set(myToolbox.getPreferencesRegistry().getPreferences(DefaultDataTypeInfo.class)
                            .getStringSet(TAG_SET_PREFERENCE_PREFIX + myTypeKey, defaultTagSet));
        }
    }
}

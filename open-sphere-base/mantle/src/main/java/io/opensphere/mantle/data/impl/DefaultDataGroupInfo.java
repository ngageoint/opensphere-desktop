package io.opensphere.mantle.data.impl;

import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;

import org.apache.commons.lang3.StringUtils;

import io.opensphere.core.Toolbox;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.Quadrilateral;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.util.ObservableValue;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.WeakChangeSupport;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.javafx.ConcurrentBooleanProperty;
import io.opensphere.core.util.javafx.ConcurrentObjectProperty;
import io.opensphere.core.util.lang.BitArrays;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.mantle.data.AbstractDataGroupInfoChangeEvent;
import io.opensphere.mantle.data.DataGroupActivationProperty;
import io.opensphere.mantle.data.DataGroupEvent;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataGroupInfoAssistant;
import io.opensphere.mantle.data.DataGroupInfoLookup;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.LoadsTo;
import io.opensphere.mantle.data.MapVisualizationType;
import io.opensphere.mantle.data.event.DataGroupInfoChildAddedEvent;
import io.opensphere.mantle.data.event.DataGroupInfoChildRemovedEvent;
import io.opensphere.mantle.data.event.DataGroupInfoDisplayNameChangedEvent;
import io.opensphere.mantle.data.event.DataGroupInfoIdChangedEvent;
import io.opensphere.mantle.data.event.DataGroupInfoMemberAddedEvent;
import io.opensphere.mantle.data.event.DataGroupInfoMemberRemovedEvent;
import io.opensphere.mantle.data.event.DataGroupInfoMembersClearedEvent;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.event.EventHandler;
import net.jcip.annotations.GuardedBy;

/**
 * Default implementation of the {@link DataGroupInfo}.
 */
@SuppressWarnings("PMD.GodClass")
public class DefaultDataGroupInfo implements DataGroupInfo
{
    /** Mask for the 'is flattenable' flag. */
    private static final byte IS_FLATTENABLE_MASK = 1;

    /** Mask for the 'preserve child order' flag. */
    private static final byte PRESERVE_CHILD_ORDER_MASK = 2;

    /** Mask for the 'hidden' flag. */
    private static final byte HIDDEN_MASK = 4;

    /** The key map. */
    private static DataGroupInfoKeyMap ourKeyMap = new DataGroupInfoKeyMap();

    /** The activation property. */
    private final DataGroupActivationProperty myActivationProperty = new DataGroupActivationProperty(this);

    /** The child added change support. */
    private volatile WeakChangeSupport<Consumer<? super DataGroupInfo>> myChildAddedChangeSupport;

    /** The children node set. */
    private final List<DataGroupInfo> myChildren = New.list();

    /** The categories to which the group belongs. */
    private final Set<String> myDataCategories = New.set();

    /** The data group info assistant. */
    private DataGroupInfoAssistant myDataGroupInfoAssistant = new DefaultDataGroupInfoAssistant();

    /** The display name. */
    private String myDisplayName;

    /** Whether the group should be expanded by default in the UI. */
    private Boolean myExpandedByDefault;

    /** The Description. */
    private String myGroupDescription;

    /** The id. */
    private String myId;

    /** Up to 8 boolean flags. */
    @GuardedBy("this")
    private byte myFlags;

    /**
     * Flag that indicates that this is a root node.
     */
    private final boolean myIsRootNode;

    /** The member set. */
    private final Set<DataTypeInfo> myMemberSet = New.set();

    /** The modification lock. */
    private final ReadWriteLock myModificationLock = new ReentrantReadWriteLock();

    /** The parent node. */
    private DataGroupInfo myParent;

    /** The provider type. */
    private String myProviderType;

    /** The toolbox. */
    private final Toolbox myToolbox;

    /** The property through which activation support is tracked. */
    private final BooleanProperty myActivationSupportProperty;

    /** The property through which triggering support is tracked. */
    private final BooleanProperty myTriggeringSupportProperty;

    /** The property in which the triggered event handler is tracked. */
    private final ObjectProperty<EventHandler<DataGroupEvent>> myTriggeredProperty;

    /**
     * Gets the key map.
     *
     * @return the key map
     */
    public static DataGroupInfoLookup getKeyMap()
    {
        return ourKeyMap;
    }

    /**
     * Adds a title and items to the given builder.
     *
     * @param builder the string builder
     * @param title the title
     * @param items the items
     * @param separator the item separator
     * @param emptyText the text to display if there ain't no items
     */
    private static void addItems(StringBuilder builder, String title, Collection<String> items, String separator,
            String emptyText)
    {
        if (builder.length() != 0)
        {
            builder.append("\n\n");
        }
        if (items.isEmpty())
        {
            if (emptyText != null)
            {
                builder.append(emptyText);
            }
        }
        else
        {
            if (title != null)
            {
                builder.append(title);
            }
            StringUtilities.join(builder, separator, items);
        }
    }

    /**
     * CTOR for group info with id for the group. Note: Display name will be set
     * to id initially.
     *
     * @param rootNode - true if this is a root level node ( root nodes cannot
     *            have parents set )
     * @param aToolbox the toolbox
     * @param providerType the provider type
     * @param id - the id for the group
     */
    public DefaultDataGroupInfo(boolean rootNode, Toolbox aToolbox, String providerType, String id)
    {
        this(rootNode, aToolbox, providerType, id, id);
    }

    /**
     * Instantiates a new default data group info.
     *
     * @param rootNode - true if this is a root level node ( root nodes cannot
     *            have parents set )
     * @param aToolbox the toolbox
     * @param providerType the provider type
     * @param id the id for the group.
     * @param displayName the display name
     */
    public DefaultDataGroupInfo(boolean rootNode, Toolbox aToolbox, String providerType, String id, String displayName)
    {
        if (id == null)
        {
            throw new IllegalArgumentException("Id cannot be null");
        }
        if (displayName == null)
        {
            throw new IllegalArgumentException("Display name cannot be null");
        }

        myIsRootNode = rootNode;
        myToolbox = aToolbox;
        myProviderType = providerType;
        myId = id;
        myDisplayName = displayName;
        myActivationSupportProperty = new ConcurrentBooleanProperty(true);
        myTriggeringSupportProperty = new ConcurrentBooleanProperty(false);
        myTriggeredProperty = new ConcurrentObjectProperty<>();
        myFlags = BitArrays.setFlag(IS_FLATTENABLE_MASK, true, myFlags);
        ourKeyMap.setKeyToGroupMapEntry(myId, this);
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.data.DataGroupInfo#isActivationSupported()
     */
    @Override
    public boolean isActivationSupported()
    {
        return myActivationSupportProperty.get();
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.data.DataGroupInfo#setActivationSupported(boolean)
     */
    @Override
    public void setActivationSupported(boolean pActivationSupported)
    {
        myActivationSupportProperty.set(pActivationSupported);
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.data.DataGroupInfo#activationSupportProperty()
     */
    @Override
    public BooleanProperty activationSupportProperty()
    {
        return myActivationSupportProperty;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.data.DataGroupInfo#activationProperty()
     */
    @Override
    public DataGroupActivationProperty activationProperty()
    {
        return myActivationProperty;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.data.DataGroupInfo#isTriggeringSupported()
     */
    @Override
    public boolean isTriggeringSupported()
    {
        return myTriggeringSupportProperty.get();
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.data.DataGroupInfo#setTriggeringSupported(boolean)
     */
    @Override
    public void setTriggeringSupported(boolean pTriggeringSupported)
    {
        myTriggeringSupportProperty.set(pTriggeringSupported);
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.data.DataGroupInfo#triggerSupportProperty()
     */
    @Override
    public BooleanProperty triggerSupportProperty()
    {
        return myTriggeringSupportProperty;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.data.DataGroupInfo#setTriggerHandler(javafx.event.EventHandler)
     */
    @Override
    public void setTriggerHandler(EventHandler<DataGroupEvent> pHandler)
    {
        myTriggeredProperty.set(pHandler);
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.data.DataGroupInfo#trigger(DataGroupEvent)
     */
    @Override
    public void trigger(DataGroupEvent pEvent)
    {
        myTriggeredProperty.get().handle(pEvent);
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.data.DataGroupInfo#triggeredProperty()
     */
    @Override
    public ObjectProperty<EventHandler<DataGroupEvent>> triggeredProperty()
    {
        return myTriggeredProperty;
    }

    @Override
    public WeakChangeSupport<Consumer<? super DataGroupInfo>> getChildAddedChangeSupport()
    {
        if (myChildAddedChangeSupport == null)
        {
            AtomicReferenceFieldUpdater
                    .newUpdater(DefaultDataGroupInfo.class, WeakChangeSupport.class, "myChildAddedChangeSupport")
                    .compareAndSet(this, null, new WeakChangeSupport<>());
        }
        return myChildAddedChangeSupport;
    }

    @Override
    public void addChild(DataGroupInfo dgi, Object source)
    {
        boolean added = false;
        myModificationLock.writeLock().lock();
        try
        {
            if (isParentAncestor(dgi))
            {
                throw new IllegalStateException("The DataGroupInfo " + dgi.getId() + " Cannot be a descendent of itself.");
            }

            dgi.setParent(this);
            added = myChildren.add(dgi);
        }
        finally
        {
            myModificationLock.writeLock().unlock();
        }
        if (added)
        {
            fireGroupInfoChangeEvent(new DataGroupInfoChildAddedEvent(this, dgi, source));

            notifyChildAddedListeners(dgi);
        }

        assertHasMembers();
    }

    @Override
    public void addMember(DataTypeInfo dti, Object source)
    {
        myModificationLock.writeLock().lock();
        try
        {
            myMemberSet.add(dti);
            dti.setParent(this);
        }
        finally
        {
            myModificationLock.writeLock().unlock();
        }
        fireGroupInfoChangeEvent(new DataGroupInfoMemberAddedEvent(this, dti, source));
        assertHasChildren();
    }

    @Override
    public void clearMembers(boolean recursive, Object source)
    {
        myModificationLock.writeLock().lock();
        try
        {
            myMemberSet.clear();

            if (recursive)
            {
                myChildren.forEach(d -> d.clearMembers(recursive, this));
                myChildren.clear();
            }
        }
        finally
        {
            myModificationLock.writeLock().unlock();
        }
        if (!Utilities.sameInstance(source, myParent))
        {
            fireGroupInfoChangeEvent(new DataGroupInfoMembersClearedEvent(this, source));
        }
    }

    @Override
    public Set<DataGroupInfo> createGroupSet(Predicate<? super DataGroupInfo> nodeFilter)
    {
        Set<DataGroupInfo> groupSet = New.set();
        myModificationLock.readLock().lock();

        try
        {
            if (nodeFilter == null || nodeFilter.test(this))
            {
                groupSet.add(this);
            }

            if (!myChildren.isEmpty())
            {
                myChildren.stream().map(dgi -> dgi.createGroupSet(nodeFilter)).forEach(groupSet::addAll);
            }
        }
        finally
        {
            myModificationLock.readLock().unlock();
        }

        return groupSet;
    }

    @Override
    public MutableTreeNode createTreeNode()
    {
        return createTreeNode(null, null);
    }

    @Override
    public MutableTreeNode createTreeNode(Comparator<? super DataGroupInfo> comparator,
            Predicate<? super DataGroupInfo> nodeFilter)
    {
        DefaultMutableTreeNode node = null;
        myModificationLock.readLock().lock();
        try
        {
            DefaultGroupInfoTreeNodeData nodeData = new DefaultGroupInfoTreeNodeData(myId, myDisplayName,
                    new HashSet<>(myMemberSet), this);
            node = new DefaultMutableTreeNode(nodeData, false);

            if (!myChildren.isEmpty())
            {
                List<DataGroupInfo> childList = CollectionUtilities.sort(myChildren,
                        comparator == null ? DataGroupInfo.DISPLAY_NAME_COMPARATOR : comparator);

                for (DataGroupInfo dgi : childList)
                {
                    if (nodeFilter == null || nodeFilter.test(dgi))
                    {
                        MutableTreeNode childNode = dgi.createTreeNode(comparator, nodeFilter);
                        if (!node.getAllowsChildren())
                        {
                            node.setAllowsChildren(true);
                        }
                        node.add(childNode);
                    }
                }
            }
        }
        finally
        {
            myModificationLock.readLock().unlock();
        }
        return node;
    }

    @Override
    public Set<DataGroupInfo> findChildren(Predicate<? super DataGroupInfo> dgiFilter, boolean recursive,
            boolean stopOnFirstFound)
    {
        Utilities.checkNull(dgiFilter, "dgiFilter");
        Set<DataGroupInfo> resultSet = new HashSet<>();
        myModificationLock.readLock().lock();
        try
        {
            for (DataGroupInfo dataGroupInfo : myChildren)
            {
                if (dgiFilter.test(dataGroupInfo))
                {
                    resultSet.add(dataGroupInfo);
                    if (stopOnFirstFound)
                    {
                        break;
                    }
                }
            }
            if (recursive && (!stopOnFirstFound || resultSet.isEmpty()))
            {
                for (DataGroupInfo dgi : myChildren)
                {
                    resultSet.addAll(dgi.findChildren(dgiFilter, recursive, stopOnFirstFound));
                    if (stopOnFirstFound && !resultSet.isEmpty())
                    {
                        break;
                    }
                }
            }
        }
        finally
        {
            myModificationLock.readLock().unlock();
        }
        return resultSet.isEmpty() ? Collections.<DataGroupInfo>emptySet() : resultSet;
    }

    @Override
    public Set<DataTypeInfo> findMembers(Predicate<? super DataTypeInfo> dtiFilter, boolean recursive, boolean stopOnFirstFound)
    {
        Utilities.checkNull(dtiFilter, "dtiFilter");
        Set<DataTypeInfo> resultSet = new HashSet<>();
        myModificationLock.readLock().lock();
        try
        {
            for (DataTypeInfo dti : myMemberSet)
            {
                if (dtiFilter.test(dti))
                {
                    resultSet.add(dti);
                    if (stopOnFirstFound)
                    {
                        break;
                    }
                }
            }
            if (recursive && (!stopOnFirstFound || resultSet.isEmpty()))
            {
                for (DataGroupInfo dgi : myChildren)
                {
                    resultSet.addAll(dgi.findMembers(dtiFilter, recursive, stopOnFirstFound));
                    if (stopOnFirstFound && !resultSet.isEmpty())
                    {
                        break;
                    }
                }
            }
        }
        finally
        {
            myModificationLock.readLock().unlock();
        }
        return resultSet.isEmpty() ? Collections.<DataTypeInfo>emptySet() : resultSet;
    }

    @Override
    public void fireGroupInfoChangeEvent(AbstractDataGroupInfoChangeEvent e)
    {
        if (myToolbox != null && e != null && !Utilities.sameInstance(e.getSource(), NO_EVENT_SOURCE))
        {
            myToolbox.getEventManager().publishEvent(e);
        }
    }

    @Override
    public DataGroupInfoAssistant getAssistant()
    {
        return myDataGroupInfoAssistant;
    }

    @Override
    public List<DataGroupInfo> getChildren()
    {
        List<DataGroupInfo> returnSet;
        myModificationLock.readLock().lock();
        try
        {
            returnSet = Collections.unmodifiableList(New.list(myChildren));
        }
        finally
        {
            myModificationLock.readLock().unlock();
        }
        return returnSet;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.data.DataGroupInfo#getDataCategories()
     */
    @Override
    public Set<String> getDataCategories()
    {
        return myDataCategories;
    }

    @Override
    public void getDescendants(Collection<DataGroupInfo> descendants)
    {
        Collection<DataGroupInfo> children = getChildren();
        descendants.addAll(children);
        children.forEach(c -> c.getDescendants(descendants));
    }

    @Override
    public String getDisplayName()
    {
        return myDisplayName;
    }

    @Override
    public String getDisplayNameWithPostfixTopParentName()
    {
        String longName = getDisplayName();
        if (getTopParent() != null && !getTopParent().getDisplayName().equals(getDisplayName()))
        {
            longName = getDisplayName() + " (" + getTopParent().getDisplayName() + ")";
        }
        return longName;
    }

    @Override
    public Boolean getExpandedByDefault()
    {
        return myExpandedByDefault;
    }

    @Override
    public DataGroupInfo getGroupById(String id)
    {
        DataGroupInfo returnValue = null;
        if (id.equals(myId))
        {
            returnValue = this;
        }
        else
        {
            myModificationLock.readLock().lock();
            try
            {
                for (DataGroupInfo dgi : myChildren)
                {
                    returnValue = dgi.getGroupById(id);
                    if (returnValue != null)
                    {
                        break;
                    }
                }
            }
            finally
            {
                myModificationLock.readLock().unlock();
            }
        }

        return returnValue;
    }

    @Override
    public String getGroupDescription()
    {
        return myGroupDescription;
    }

    @Override
    public final String getId()
    {
        return myId;
    }

    @Override
    public String getLongDisplayName()
    {
        String longName = getDisplayName();
        if (getTopParent() != null && !getTopParent().getDisplayName().equals(getDisplayName()))
        {
            longName = getTopParent().getDisplayName() + "/" + getDisplayName();
        }
        return longName;
    }

    @Override
    public DataTypeInfo getMemberById(String id, boolean recursive)
    {
        DataTypeInfo returnValue = null;
        if (hasMembers(false))
        {
            for (DataTypeInfo info : getMembers(false))
            {
                if (info.getTypeKey().equals(id))
                {
                    returnValue = info;
                    break;
                }
            }
        }

        // If we have not yet found the child with the member that
        // has the the id we are interested in, and we are recursive
        // and have children, then search the child list.
        if (returnValue == null && recursive && hasChildren())
        {
            for (DataGroupInfo child : myChildren)
            {
                returnValue = child.getMemberById(id, recursive);
                if (returnValue != null)
                {
                    break;
                }
            }
        }
        return returnValue;
    }

    @Override
    public Set<MapVisualizationType> getMemberMapVisualizationTypes(boolean recursive)
    {
        final Set<MapVisualizationType> resultSet = New.set();
        findMembers(value ->
        {
            if (value.getMapVisualizationInfo() != null)
            {
                resultSet.add(value.getMapVisualizationInfo().getVisualizationType());
            }
            return false;
        }, recursive, false);
        return resultSet;
    }

    @Override
    public Set<DataTypeInfo> getMembers(boolean recurseChildren)
    {
        Set<DataTypeInfo> returnSet;
        myModificationLock.readLock().lock();
        try
        {
            returnSet = New.set(myMemberSet);

            if (recurseChildren)
            {
                myChildren.stream().map(dgi -> dgi.getMembers(recurseChildren)).forEach(returnSet::addAll);
            }
        }
        finally
        {
            myModificationLock.readLock().unlock();
        }
        return Collections.unmodifiableSet(returnSet);
    }

    @Override
    public DataGroupInfo getParent()
    {
        return myParent;
    }

    @Override
    public boolean getPreviewImage(ObservableValue<? super BufferedImage> value)
    {
        return false;
    }

    @Override
    public String getProviderType()
    {
        return myProviderType;
    }

    @Override
    public boolean getRegion(ObservableValue<? super Quadrilateral<GeographicPosition>> observableRegion)
    {
        return false;
    }

    @Override
    public String getSummaryDescription()
    {
        // Combine the items of each data type
        Collection<String> tags = New.set();
        Collection<String> descriptions = New.insertionOrderSet();
        Collection<String> urls = New.set();
        Collection<String> times = New.set();
        //Collection<>
        myModificationLock.readLock().lock();
        try
        {
            if (!myMemberSet.isEmpty())
            {
                List<DataTypeInfo> memberList = CollectionUtilities.sort(myMemberSet, DataTypeInfo.DISPLAY_NAME_COMPARATOR);
                for (DataTypeInfo member : memberList)
                {
                    tags.addAll(member.getTags());
                    if (!StringUtils.isBlank(member.getDescription()))
                    {
                        descriptions.add(member.getDescription());
                    }
                    if (member.getUrl() != null)
                    {
                        urls.add(member.getUrl());
                    }
                    if (member.getTimeExtents() != null)
                    {
                    	TimeSpan ts = member.getTimeExtents().getExtent();
                    	if (ts != null)
                    	{
                    		times.add(ts.toDisplayString());
                    	}
                    }
                }
            }
        }
        finally
        {
            myModificationLock.readLock().unlock();
        }

        // Sort the tags
        if (tags.size() > 1)
        {
            tags = CollectionUtilities.sort(tags);
        }

        // Create the description string
        StringBuilder sb = new StringBuilder();
        String groupDesc = getGroupDescription();
        if (StringUtils.isNotEmpty(groupDesc))
        {
            sb.append(groupDesc);
        }
        addItems(sb, null, descriptions, "\n\n", "");
        addItems(sb, "Tags: ", tags, ", ", "Tags: (none)");
        addItems(sb, urls.size() == 1 ? "URL:\n" : "URLs:\n", urls, "\n", null);
        addItems(sb, times.size() == 1 ? "Timespan:\n" : "Timespans:\n", times, "\n", null);

        return sb.toString();
    }

    /**
     * Gets the toolbox.
     *
     * @return the toolbox
     */
    public Toolbox getToolbox()
    {
        return myToolbox;
    }

    @Override
    public DataGroupInfo getTopParent()
    {
        return myParent == null ? this : myParent.getTopParent();
    }

    @Override
    public String getTopParentDisplayName()
    {
        return myParent != null ? getTopParent().getDisplayName() : "";
    }

    @Override
    public Stream<DataGroupInfo> groupStream()
    {
        return Stream.concat(Stream.of(this), getChildren().stream().flatMap(g -> ((DefaultDataGroupInfo)g).groupStream()));
    }

    @Override
    public boolean hasChildren()
    {
        return !myChildren.isEmpty();
    }

    @Override
    public boolean hasDetails()
    {
        return true;
    }

    @Override
    public boolean hasFeatureTypes(boolean recursive)
    {
        return hasMember(t -> t.getMapVisualizationInfo() != null && t.getMapVisualizationInfo().usesMapDataElements(),
                recursive);
    }

    @Override
    public boolean hasImageTileTypes(boolean recursive)
    {
        return hasMember(t -> t.getMapVisualizationInfo() != null && t.getMapVisualizationInfo().isImageTileType(), recursive);
    }

    @Override
    public boolean hasMember(final DataTypeInfo dti, boolean recursive)
    {
        Utilities.checkNull(dti, "dti");
        return hasMember(t -> Utilities.sameInstance(dti, t), recursive);
    }

    @Override
    public boolean hasMembers(boolean recursive)
    {
        boolean found;
        myModificationLock.readLock().lock();
        try
        {
            found = !myMemberSet.isEmpty();
            if (!found && recursive)
            {
                for (DataGroupInfo dgi : myChildren)
                {
                    found = dgi.hasMembers(recursive);
                    if (found)
                    {
                        break;
                    }
                }
            }
        }
        finally
        {
            myModificationLock.readLock().unlock();
        }
        return found;
    }

    @Override
    public boolean hasTimelineMember(boolean recursive)
    {
        return hasMember(t -> t.getBasicVisualizationInfo().supportsLoadsTo(LoadsTo.TIMELINE), recursive);
    }

    @Override
    public boolean hasVisualizationStyles(boolean recursive)
    {
        return hasMember(t -> t.getMapVisualizationInfo() != null && t.getMapVisualizationInfo().usesVisualizationStyles(),
                recursive);
    }

    @Override
    public boolean isDragAndDrop()
    {
        return false;
    }

    @Override
    public synchronized boolean isFlattenable()
    {
        return BitArrays.isFlagSet(IS_FLATTENABLE_MASK, myFlags);
    }

    @Override
    public boolean isParentAncestor(DataGroupInfo dgi)
    {
        boolean found = false;
        if (getParent() != null)
        {
            if (dgi == getParent())
            {
                found = true;
            }
            else
            {
                found = getParent().isParentAncestor(dgi);
            }
        }
        return found;
    }

    @Override
    public boolean isRootNode()
    {
        return myIsRootNode;
    }

    @Override
    public boolean isTaggable()
    {
        return true;
    }

    @Override
    public void notifyChildAddedListeners(DataGroupInfo dgi)
    {
        if (myChildAddedChangeSupport != null)
        {
            myChildAddedChangeSupport.notifyListeners(l -> l.accept(dgi));
        }
        if (myParent != null)
        {
            myParent.notifyChildAddedListeners(dgi);
        }
    }

    @Override
    public int numChildren()
    {
        return myChildren.size();
    }

    @Override
    public int numMembers(boolean recursive)
    {
        int count;
        myModificationLock.readLock().lock();
        try
        {
            count = myMemberSet.size();
            if (recursive)
            {
                for (DataGroupInfo dgi : myChildren)
                {
                    count = dgi.numMembers(recursive);
                }
            }
        }
        finally
        {
            myModificationLock.readLock().unlock();
        }
        return count;
    }

    @Override
    public boolean removeChild(DataGroupInfo dgi, Object source)
    {
        return removeChild(dgi, source, false);
    }

    @Override
    public boolean removeChildKeepActive(DataGroupInfo dgi, Object source)
    {
        return removeChild(dgi, source, true);
    }

    @Override
    public boolean removeMember(DataTypeInfo dti, boolean recursive, Object source)
    {
        boolean removed = false;
        boolean generateEvent = false;
        myModificationLock.writeLock().lock();
        try
        {
            removed = myMemberSet.remove(dti);
            dti.setParent(null);
            generateEvent = removed;

            if (!removed && recursive)
            {
                for (DataGroupInfo dgi : myChildren)
                {
                    removed = dgi.removeMember(dti, recursive, source);
                    if (removed)
                    {
                        if (dgi.getMembers(false).isEmpty() && dgi.getChildren().isEmpty())
                        {
                            // remove the group too:
                            removeChild(dgi, source);
                        }
                        break;
                    }
                }
            }
        }
        finally
        {
            myModificationLock.writeLock().unlock();
        }
        if (generateEvent)
        {
            fireGroupInfoChangeEvent(new DataGroupInfoMemberRemovedEvent(this, dti, source));
        }
        return removed;
    }

    /**
     * Sets the assistant.
     *
     * @param assistant the new assistant
     */
    public void setAssistant(DataGroupInfoAssistant assistant)
    {
        myDataGroupInfoAssistant = assistant == null ? new DefaultDataGroupInfoAssistant() : assistant;
    }

    @Override
    public void setDisplayName(String name, Object source)
    {
        if (name == null)
        {
            throw new IllegalArgumentException("Display name cannot be null");
        }

        String oldName = myDisplayName;
        myDisplayName = name;
        fireGroupInfoChangeEvent(new DataGroupInfoDisplayNameChangedEvent(this, oldName, name, source));
    }

    @Override
    public void setExpandedByDefault(Boolean expandedByDefault)
    {
        myExpandedByDefault = expandedByDefault;
    }

    /**
     * Sets the group description.
     *
     * @param description the new description
     */
    public void setGroupDescription(String description)
    {
        myGroupDescription = description;
    }

    @Override
    public void setGroupVisible(Predicate<? super DataTypeInfo> dtiFilter, boolean visible, boolean recursive, Object source)
    {
        getMembers(recursive).stream().filter(d -> dtiFilter == null || dtiFilter.test(d))
                .forEach(d -> d.setVisible(visible, source));
    }

    @Override
    public final void setId(String id, Object source)
    {
        if (id == null)
        {
            throw new IllegalArgumentException("Id cannot be null");
        }

        String oldId = myId;
        myId = id;
        ourKeyMap.rekey(oldId, id);
        fireGroupInfoChangeEvent(new DataGroupInfoIdChangedEvent(this, oldId, id, source));
    }

    /**
     * Sets if this group is flattenable or not.
     *
     * @param isFlattenable True if flattenable false otherwise.
     */
    public synchronized void setIsFlattenable(boolean isFlattenable)
    {
        myFlags = BitArrays.setFlag(IS_FLATTENABLE_MASK, isFlattenable, myFlags);
    }

    @Override
    public synchronized boolean isPreserveChildOrder()
    {
        return BitArrays.isFlagSet(PRESERVE_CHILD_ORDER_MASK, myFlags);
    }

    @Override
    public synchronized void setPreserveChildOrder(boolean preserveChildOrder)
    {
        myFlags = BitArrays.setFlag(PRESERVE_CHILD_ORDER_MASK, preserveChildOrder, myFlags);
    }

    @Override
    public synchronized boolean isHidden()
    {
        return BitArrays.isFlagSet(HIDDEN_MASK, myFlags);
    }

    @Override
    public synchronized void setHidden(boolean hidden)
    {
        myFlags = BitArrays.setFlag(HIDDEN_MASK, hidden, myFlags);
    }

    @Override
    public void setParent(DataGroupInfo parent)
    {
        if (myIsRootNode)
        {
            throw new UnsupportedOperationException("A parent cannot be set for a Root node.");
        }
        myParent = parent;
    }

    /**
     * Sets the provider type.
     *
     * @param providerType the new provider type
     */
    public void setProviderType(String providerType)
    {
        myProviderType = providerType;
    }

    @Override
    public String toString()
    {
        return toIndentedString("");
    }

    @Override
    public boolean userActivationStateControl()
    {
        return true;
    }

    @Override
    public boolean userDeleteControl()
    {
        return false;
    }

    @Override
    public boolean usesStyles(boolean recursive)
    {
        return hasMember(t -> t.getMapVisualizationInfo() != null && t.getMapVisualizationInfo().usesVisualizationStyles(),
                recursive);
    }

    /**
     * Asserts if there are children.
     */
    protected void assertHasChildren()
    {
        assert !hasChildren();
    }

    /**
     * Asserts if there are members.
     */
    protected void assertHasMembers()
    {
        assert !hasMembers(false);
    }

    /**
     * Removes the child.
     *
     * @param dgi The child to remove.
     * @param source The source making the change.
     * @param keepActive True if the state of the child should not change,
     *            otherwise the child will be set to inactive.
     * @return True if the remove was successful.
     */
    private boolean removeChild(DataGroupInfo dgi, Object source, boolean keepActive)
    {
        boolean removed = false;
        myModificationLock.writeLock().lock();
        try
        {
            if (myChildren.contains(dgi))
            {
                dgi.setParent(null);
                removed = myChildren.remove(dgi);
            }
        }
        finally
        {
            myModificationLock.writeLock().unlock();
        }
        if (removed)
        {
            fireGroupInfoChangeEvent(new DataGroupInfoChildRemovedEvent(this, dgi, source, keepActive));
        }

        return removed;
    }

    /**
     * To indented string.
     *
     * @param pIndent the indent
     * @return the string
     */
    private String toIndentedString(String pIndent)
    {
        String indent = pIndent == null ? "" : pIndent;
        StringBuilder sb = new StringBuilder(64);
        sb.append(indent).append("Group: ").append(getLongDisplayName()).append('\n');
        sb.append(indent).append("  ID: ").append(myId).append('\n');
        sb.append(indent).append("  Description: ").append(myGroupDescription == null ? "" : myGroupDescription).append('\n');

        sb.append(indent).append("  Members: Count: ").append(myMemberSet.size()).append('\n');
        if (!myMemberSet.isEmpty())
        {
            List<DataTypeInfo> dtiList = CollectionUtilities.sort(myMemberSet, DataTypeInfo.DISPLAY_NAME_COMPARATOR);
            for (DataTypeInfo dti : dtiList)
            {
                sb.append(indent).append("       DTIName[").append(dti.getDisplayName()).append("] DTIId[")
                        .append(dti.getTypeKey()).append("]\n");
            }
        }
        if (myChildren != null)
        {
            sb.append(indent).append("  Children: Count: ").append(myChildren.size()).append('\n');
            if (!myChildren.isEmpty())
            {
                sb.append(indent).append("  {\n");
                List<DataGroupInfo> dgiList = CollectionUtilities.sort(myChildren, DataGroupInfo.LONG_DISPLAY_NAME_COMPARATOR);
                for (DataGroupInfo dgi : dgiList)
                {
                    if (dgi instanceof DefaultDataGroupInfo)
                    {
                        sb.append(((DefaultDataGroupInfo)dgi).toIndentedString(indent + "       "));
                    }
                    else
                    {
                        sb.append(indent).append("     ").append(dgi.getClass().getSimpleName()).append("  Name[")
                                .append(dgi.getLongDisplayName()).append("] Id[").append(dgi.getId());
                    }
                }
                sb.append(indent).append("  }\n");
            }
        }
        return sb.toString();
    }
}

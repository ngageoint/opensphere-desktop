package io.opensphere.mantle.mp.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Predicate;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;

import io.opensphere.core.Toolbox;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.mp.AbstractMapAnnotationPointGroupChangeEvent;
import io.opensphere.mantle.mp.MapAnnotationPoint;
import io.opensphere.mantle.mp.MapAnnotationPointGroup;
import io.opensphere.mantle.mp.MutableMapAnnotationPoint;
import io.opensphere.mantle.mp.MutableMapAnnotationPointGroup;
import io.opensphere.mantle.mp.event.impl.MapAnnotationPointGroupChildAddedEvent;
import io.opensphere.mantle.mp.event.impl.MapAnnotationPointGroupChildRemovedEvent;
import io.opensphere.mantle.mp.event.impl.MapAnnotationPointGroupChildrenClearedEvent;
import io.opensphere.mantle.mp.event.impl.MapAnnotationPointGroupClearedEvent;
import io.opensphere.mantle.mp.event.impl.MapAnnotationPointGroupMemberAddedEvent;
import io.opensphere.mantle.mp.event.impl.MapAnnotationPointGroupMemberRemovedEvent;
import io.opensphere.mantle.mp.event.impl.MapAnnotationPointGroupMembersClearedEvent;
import io.opensphere.mantle.mp.event.impl.MapAnnotationPointGroupNameChangedEvent;

/**
 * The Class DefaultMapAnnotationPointGroup.
 */
@SuppressWarnings("PMD.GodClass")
public class DefaultMapAnnotationPointGroup implements MutableMapAnnotationPointGroup
{
    /** The children node set. */
    private final Set<MutableMapAnnotationPointGroup> myChildren;

    /** The member set. */
    private final Set<MutableMapAnnotationPoint> myMemberSet;

    /** The modification lock. */
    private final ReadWriteLock myModificationLock = new ReentrantReadWriteLock();

    /** The name. */
    private String myName;

    /** The parent node. */
    private MutableMapAnnotationPointGroup myParent;

    /**
     * Preferred order number.
     */
    private int myPreferredOrder;

    /** The toolbox. */
    private final Toolbox myToolbox;

    /**
     * Instantiates a new default map annotation point group.
     *
     * @param aToolbox the a toolbox
     * @param other the other
     */
    public DefaultMapAnnotationPointGroup(Toolbox aToolbox, MapAnnotationPointGroup other)
    {
        this(aToolbox);
        myName = other.getName();
        myPreferredOrder = other.getPreferredOrder();
        List<MapAnnotationPoint> ptSet = other.getPoints(false);
        if (ptSet != null && !ptSet.isEmpty())
        {
            for (MapAnnotationPoint pt : ptSet)
            {
                DefaultMapAnnotationPoint copy = new DefaultMapAnnotationPoint(pt);
                copy.setGroup(this);
                myMemberSet.add(copy);
            }
        }
        List<MapAnnotationPointGroup> groups = other.getChildren();
        if (groups != null && !groups.isEmpty())
        {
            for (MapAnnotationPointGroup group : groups)
            {
                DefaultMapAnnotationPointGroup copy = new DefaultMapAnnotationPointGroup(aToolbox, group);
                copy.setParent(this);
                myChildren.add(copy);
            }
        }
    }

    /**
     * CTOR for group info with id for the group. Note: Display name will be set
     * to id initially.
     *
     * @param aToolbox the toolbox
     * @param name the name
     */
    public DefaultMapAnnotationPointGroup(Toolbox aToolbox, String name)
    {
        this(aToolbox);

        if (name == null)
        {
            throw new IllegalArgumentException("name cannot be null");
        }

        myName = name;
    }

    /**
     * Instantiates a new default data group info.
     *
     * @param aToolbox the toolbox
     */
    private DefaultMapAnnotationPointGroup(Toolbox aToolbox)
    {
        myChildren = new HashSet<>();
        myMemberSet = new HashSet<>();
        myToolbox = aToolbox;
    }

    @Override
    public void addChild(MutableMapAnnotationPointGroup group, Object source)
    {
        boolean added = false;
        myModificationLock.writeLock().lock();
        try
        {
            if (isParentAncestor(group))
            {
                throw new IllegalStateException(
                        "The MapAnnotationPointGroup " + group.getName() + " Cannot be a descendent of itself.");
            }

            group.setParent(this);
            added = myChildren.add(group);
        }
        finally
        {
            myModificationLock.writeLock().unlock();
        }
        if (added)
        {
            fireGroupInfoChangeEvent(new MapAnnotationPointGroupChildAddedEvent(this, group, source));
        }
    }

    @Override
    public void addPoint(MutableMapAnnotationPoint pt, Object source)
    {
        myModificationLock.writeLock().lock();
        try
        {
            pt.setGroup(this);
            myMemberSet.add(pt);
        }
        finally
        {
            myModificationLock.writeLock().unlock();
        }
        fireGroupInfoChangeEvent(new MapAnnotationPointGroupMemberAddedEvent(this, pt, source));
    }

    @Override
    public void clearAll(Object source)
    {
        myModificationLock.writeLock().lock();
        try
        {
            myMemberSet.clear();

            for (MutableMapAnnotationPointGroup group : myChildren)
            {
                group.setParent(null);
                group.clearAll(this);
            }
            myChildren.clear();
        }
        finally
        {
            myModificationLock.writeLock().unlock();
        }
        if (!Utilities.sameInstance(source, myParent))
        {
            fireGroupInfoChangeEvent(new MapAnnotationPointGroupClearedEvent(this, source));
        }
    }

    @Override
    public void clearChildren(Object source)
    {
        myModificationLock.writeLock().lock();
        try
        {
            for (MutableMapAnnotationPointGroup group : myChildren)
            {
                group.setParent(null);
                group.clearChildren(this);
            }
            myChildren.clear();
        }
        finally
        {
            myModificationLock.writeLock().unlock();
        }
        if (!Utilities.sameInstance(source, myParent))
        {
            fireGroupInfoChangeEvent(new MapAnnotationPointGroupChildrenClearedEvent(this, source));
        }
    }

    @Override
    public void clearPoints(boolean recursive, Object source)
    {
        myModificationLock.writeLock().lock();
        try
        {
            myMemberSet.clear();

            if (recursive)
            {
                for (MutableMapAnnotationPointGroup group : myChildren)
                {
                    group.clearPoints(recursive, this);
                }
                myChildren.clear();
            }
        }
        finally
        {
            myModificationLock.writeLock().unlock();
        }
        if (!Utilities.sameInstance(source, myParent))
        {
            fireGroupInfoChangeEvent(new MapAnnotationPointGroupMembersClearedEvent(this, source));
        }
    }

    @Override
    public MutableMapAnnotationPointGroup createFilteredCopy(Predicate<? super MutableMapAnnotationPoint> pointFilter,
            Predicate<? super MutableMapAnnotationPointGroup> groupFilter)
    {
        DefaultMapAnnotationPointGroup copy = new DefaultMapAnnotationPointGroup(myToolbox);
        myModificationLock.readLock().lock();
        try
        {
            copy.setName(myName, null);
            for (MutableMapAnnotationPoint pt : myMemberSet)
            {
                if (pointFilter == null || pointFilter.test(pt))
                {
                    copy.addPoint(new DefaultMapAnnotationPoint(pt), null);
                }
            }

            for (MutableMapAnnotationPointGroup child : myChildren)
            {
                if (groupFilter == null || groupFilter.test(child))
                {
                    copy.addChild(child.createFilteredCopy(pointFilter, groupFilter), null);
                }
            }
        }
        finally
        {
            myModificationLock.readLock().unlock();
        }
        return copy;
    }

    @Override
    public Set<MutableMapAnnotationPointGroup> createGroupSet(Predicate<? super MutableMapAnnotationPointGroup> nodeFilter)
    {
        Set<MutableMapAnnotationPointGroup> groupSet = New.set();
        myModificationLock.readLock().lock();

        try
        {
            if (nodeFilter == null || nodeFilter.test(this))
            {
                groupSet.add(this);
            }

            if (!myChildren.isEmpty())
            {
                for (MutableMapAnnotationPointGroup group : myChildren)
                {
                    groupSet.addAll(group.createGroupSet(nodeFilter));
                }
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
    public MutableTreeNode createTreeNode(Comparator<? super MutableMapAnnotationPointGroup> comparator,
            Predicate<? super MutableMapAnnotationPointGroup> nodeFilter)
    {
        DefaultMutableTreeNode node = null;
        myModificationLock.readLock().lock();
        try
        {
            DefaultMapAnnotationPointGroupTreeNodeData nodeData = new DefaultMapAnnotationPointGroupTreeNodeData(this);
            node = new DefaultMutableTreeNode(nodeData, false);

            if (!myMemberSet.isEmpty())
            {
                List<MutableMapAnnotationPoint> memberList = new ArrayList<>(myMemberSet);
                Collections.sort(memberList, MutableMapAnnotationPoint.TITLE_COMPARATOR);
                for (MutableMapAnnotationPoint pt : memberList)
                {
                    DefaultMutableTreeNode memberNode = new DefaultMutableTreeNode(pt);
                    if (!node.getAllowsChildren())
                    {
                        node.setAllowsChildren(true);
                    }
                    node.add(memberNode);
                }
            }

            if (!myChildren.isEmpty())
            {
                List<MutableMapAnnotationPointGroup> childList = new ArrayList<>(myChildren);
                if (comparator == null)
                {
                    Collections.sort(childList, MapAnnotationPointGroup.NAME_COMPARATOR);
                }
                else
                {
                    Collections.sort(childList, comparator);
                }

                for (MutableMapAnnotationPointGroup group : childList)
                {
                    if (nodeFilter == null || nodeFilter.test(group))
                    {
                        MutableTreeNode childNode = group.createTreeNode(comparator, nodeFilter);
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
    public MutableTreeNode createTreeNodeWithChildrenInPrefferedOrder()
    {
        return createTreeNodeWithChildrenInPrefferedOrder(null);
    }

    @Override
    public MutableTreeNode createTreeNodeWithChildrenInPrefferedOrder(
            Predicate<? super MutableMapAnnotationPointGroup> nodeFilter)
    {
        return createTreeNode((o1, o2) -> Integer.compare(o1.getPreferredOrder(), o2.getPreferredOrder()), nodeFilter);
    }

    @Override
    public Set<MutableMapAnnotationPoint> findPoints(Predicate<? super MutableMapAnnotationPoint> filter, boolean recursive,
            boolean stopOnFirstFound)
    {
        Utilities.checkNull(filter, "dtiFilter");
        Set<MutableMapAnnotationPoint> resultSet = new HashSet<>();
        myModificationLock.readLock().lock();
        try
        {
            for (MutableMapAnnotationPoint dti : myMemberSet)
            {
                if (filter.test(dti))
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
                for (MutableMapAnnotationPointGroup group : myChildren)
                {
                    resultSet.addAll(group.findPoints(filter, recursive, stopOnFirstFound));
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
        return resultSet.isEmpty() ? Collections.<MutableMapAnnotationPoint>emptySet() : resultSet;
    }

    @Override
    public void fireGroupInfoChangeEvent(AbstractMapAnnotationPointGroupChangeEvent e)
    {
        if (myToolbox != null)
        {
            myToolbox.getEventManager().publishEvent(e);
        }
    }

    @Override
    public List<MapAnnotationPointGroup> getChildren()
    {
        List<MapAnnotationPointGroup> returnSet = null;
        myModificationLock.readLock().lock();
        try
        {
            returnSet = Collections.unmodifiableList(new ArrayList<MapAnnotationPointGroup>(myChildren));
        }
        finally
        {
            myModificationLock.readLock().unlock();
        }
        return returnSet;
    }

    @Override
    public String getName()
    {
        return myName;
    }

    @Override
    public MutableMapAnnotationPointGroup getParent()
    {
        return myParent;
    }

    @Override
    public List<MapAnnotationPoint> getPoints(boolean recurseChildren)
    {
        List<MapAnnotationPoint> returnSet = null;
        myModificationLock.readLock().lock();
        try
        {
            returnSet = new ArrayList<>(myMemberSet);

            if (recurseChildren)
            {
                for (MutableMapAnnotationPointGroup group : myChildren)
                {
                    returnSet.addAll(group.getPoints(recurseChildren));
                }
            }
        }
        finally
        {
            myModificationLock.readLock().unlock();
        }
        return Collections.unmodifiableList(returnSet);
    }

    @Override
    public int getPreferredOrder()
    {
        return myPreferredOrder;
    }

    @Override
    public MutableMapAnnotationPointGroup getTopParent()
    {
        if (myParent == null)
        {
            return this;
        }
        else
        {
            return myParent.getTopParent();
        }
    }

    @Override
    public boolean hasChildren()
    {
        return !myChildren.isEmpty();
    }

    @Override
    public boolean hasPoint(final MapAnnotationPoint pt, boolean recursive)
    {
        Utilities.checkNull(pt, "dti");
        return !findPoints(value -> Utilities.sameInstance(pt, value), recursive, true).isEmpty();
    }

    @Override
    public boolean hasPoints()
    {
        return !myMemberSet.isEmpty();
    }

    @Override
    public boolean isParentAncestor(MutableMapAnnotationPointGroup group)
    {
        boolean found = false;
        if (getParent() != null)
        {
            if (group == getParent())
            {
                found = true;
            }
            else
            {
                found = getParent().isParentAncestor(group);
            }
        }
        return found;
    }

    @Override
    public boolean isRootNode()
    {
        return myParent == null;
    }

    @Override
    public boolean removeChild(MutableMapAnnotationPointGroup group, Object source)
    {
        boolean removed = false;
        myModificationLock.writeLock().lock();
        try
        {
            if (myChildren.contains(group))
            {
                group.setParent(null);
                removed = myChildren.remove(group);
            }
        }
        finally
        {
            myModificationLock.writeLock().unlock();
        }
        if (removed)
        {
            fireGroupInfoChangeEvent(new MapAnnotationPointGroupChildRemovedEvent(this, group, source));
        }

        return removed;
    }

    @Override
    public boolean removePoint(MutableMapAnnotationPoint pt, boolean recursive, Object source)
    {
        boolean removed = false;
        boolean generateEvent = false;
        myModificationLock.writeLock().lock();
        try
        {
            removed = myMemberSet.remove(pt);
            generateEvent = removed;

            if (!removed && recursive)
            {
                for (MutableMapAnnotationPointGroup group : myChildren)
                {
                    removed = group.removePoint(pt, recursive, source);
                    if (removed)
                    {
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
            fireGroupInfoChangeEvent(new MapAnnotationPointGroupMemberRemovedEvent(this, pt, source));
        }
        return removed;
    }

    @Override
    public void setName(String name, Object source)
    {
        if (name == null)
        {
            throw new IllegalArgumentException("Name cannot be null");
        }

        String oldName = myName;
        myName = name;
        fireGroupInfoChangeEvent(new MapAnnotationPointGroupNameChangedEvent(this, oldName, name, source));
    }

    @Override
    public void setParent(MutableMapAnnotationPointGroup parent)
    {
        myParent = parent;
    }

    @Override
    public void setPreferredOrder(int order)
    {
        myPreferredOrder = order;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(128);
        sb.append("DefaultMapAnnotationPointGroup: Name[").append(myName).append("] Parent[");
        sb.append(myParent == null ? "NULL" : myParent.getName()).append("] PrefOrder[").append(myPreferredOrder).append("]\n");
        if (myMemberSet != null && !myMemberSet.isEmpty())
        {
            sb.append("  Points: ").append(myMemberSet.size()).append('\n');
            for (MutableMapAnnotationPoint pt : myMemberSet)
            {
                sb.append("    ").append(pt.toString()).append('\n');
            }
        }
        else
        {
            sb.append("  Points: 0\n");
        }
        if (myChildren != null && !myChildren.isEmpty())
        {
            sb.append("Children: ").append(myChildren.size()).append('\n');
            for (MutableMapAnnotationPointGroup child : myChildren)
            {
                sb.append('\n');
                sb.append(child.toString());
                sb.append('\n');
            }
        }
        else
        {
            sb.append("Children: 0\n");
        }

        return sb.toString();
    }
}

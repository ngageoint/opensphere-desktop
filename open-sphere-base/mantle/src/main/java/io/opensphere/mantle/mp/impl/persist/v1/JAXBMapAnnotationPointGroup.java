package io.opensphere.mantle.mp.impl.persist.v1;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.mp.MapAnnotationPoint;
import io.opensphere.mantle.mp.MapAnnotationPointGroup;

/**
 * The Class JAXBMapAnnotationPointGroup.
 */
@XmlRootElement(name = "MapAnnotationPointGroup")
@XmlAccessorType(XmlAccessType.FIELD)
public class JAXBMapAnnotationPointGroup implements MapAnnotationPointGroup
{
    /** The children node set. */
    @XmlElement(name = "MapAnnotationPointGroup")
    private final List<JAXBMapAnnotationPointGroup> myChildren;

    /** The member set. */
    @XmlElement(name = "MapAnnotationPoint")
    private final List<JAXBMapAnnotationPoint> myMemberSet;

    /** The name. */
    @XmlAttribute(name = "name")
    private String myName;

    /** The my preferred order. */
    @XmlAttribute(name = "preferredOrder")
    private int myPreferredOrder;

    /**
     * CTOR for group info with id for the group. Note: Display name will be set
     * to id initially.
     *
     * @param group the group
     */
    public JAXBMapAnnotationPointGroup(MapAnnotationPointGroup group)
    {
        this();
        myName = group.getName();
        myPreferredOrder = group.getPreferredOrder();
        List<MapAnnotationPointGroup> childSet = group.getChildren();
        if (childSet != null && !childSet.isEmpty())
        {
            for (MapAnnotationPointGroup child : childSet)
            {
                myChildren.add(new JAXBMapAnnotationPointGroup(child));
            }
        }
        List<MapAnnotationPoint> ptSet = group.getPoints(false);
        if (ptSet != null && !ptSet.isEmpty())
        {
            for (MapAnnotationPoint pt : ptSet)
            {
                myMemberSet.add(new JAXBMapAnnotationPoint(pt));
            }
        }
    }

    /**
     * Instantiates a new jAXB map annotation point group.
     *
     * @param name the name
     */
    public JAXBMapAnnotationPointGroup(String name)
    {
        this();
        myName = name;
    }

    /**
     * Instantiates a new default data group info.
     *
     */
    private JAXBMapAnnotationPointGroup()
    {
        myChildren = New.list();
        myMemberSet = New.list();
    }

    /**
     * Adds the child.
     *
     * @param child the child
     */
    public void addChild(JAXBMapAnnotationPointGroup child)
    {
        myChildren.add(child);
    }

    /**
     * Adds the point.
     *
     * @param point the point
     */
    public void addPoint(JAXBMapAnnotationPoint point)
    {
        myMemberSet.add(point);
    }

    @Override
    public List<MapAnnotationPointGroup> getChildren()
    {
        return new ArrayList<MapAnnotationPointGroup>(myChildren);
    }

    @Override
    public String getName()
    {
        return myName;
    }

    @Override
    public List<MapAnnotationPoint> getPoints(boolean recurseChildren)
    {
        List<MapAnnotationPoint> returnSet = new ArrayList<MapAnnotationPoint>(myMemberSet);
        if (recurseChildren)
        {
            for (JAXBMapAnnotationPointGroup group : myChildren)
            {
                returnSet.addAll(group.getPoints(recurseChildren));
            }
        }
        return Collections.unmodifiableList(returnSet);
    }

    @Override
    public int getPreferredOrder()
    {
        return myPreferredOrder;
    }

    @Override
    public boolean hasChildren()
    {
        return !myChildren.isEmpty();
    }

    @Override
    public boolean hasPoint(MapAnnotationPoint pt, boolean recursive)
    {
        boolean hasPoint = false;
        for (MapAnnotationPoint point : myMemberSet)
        {
            if (Utilities.sameInstance(point, pt))
            {
                hasPoint = true;
                break;
            }
        }
        if (recursive && !hasPoint)
        {
            for (JAXBMapAnnotationPointGroup group : myChildren)
            {
                hasPoint = group.hasPoint(pt, recursive);
                if (hasPoint)
                {
                    break;
                }
            }
        }
        return hasPoint;
    }

    @Override
    public boolean hasPoints()
    {
        return myMemberSet != null && !myMemberSet.isEmpty();
    }
}

package io.opensphere.mantle.data.impl.dgset.v1;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.EqualsHelper;
import io.opensphere.mantle.data.ActiveGroupEntry;
import io.opensphere.mantle.data.DataGroupInfoActiveSet;

/**
 * The Class JAXBDataGroupInfoActiveSet.
 */
@XmlRootElement(name = "DataGroupInfoActiveSet")
@XmlAccessorType(XmlAccessType.FIELD)
public class JAXBDataGroupInfoActiveSet implements DataGroupInfoActiveSet
{
    /** The group ids. */
    @XmlElement(name = "group")
    private final List<JAXBActiveGroupEntry> myGroupEntries;

    /** The name. */
    @XmlAttribute(name = "name")
    private String myName;

    /**
     * Instantiates a new jAXB data group info active set.
     */
    public JAXBDataGroupInfoActiveSet()
    {
        myGroupEntries = New.list();
    }

    /**
     * Instantiates a new jAXB data group info active set.
     *
     * @param other the other
     */
    public JAXBDataGroupInfoActiveSet(DataGroupInfoActiveSet other)
    {
        myName = other.getName();
        myGroupEntries = New.list();
        if (other.getGroupEntries() != null)
        {
            Set<JAXBActiveGroupEntry> entrySet = New.set();
            for (ActiveGroupEntry entry : other.getGroupEntries())
            {
                entrySet.add(new JAXBActiveGroupEntry(entry));
            }
            myGroupEntries.addAll(entrySet);
        }
    }

    /**
     * Instantiates a new jAXB data group info active set.
     *
     * @param name the name
     */
    public JAXBDataGroupInfoActiveSet(String name)
    {
        this();
        myName = name;
    }

    /**
     * Instantiates a new jAXB data group info active set.
     *
     * @param name the name
     * @param groups the group ids
     */
    public JAXBDataGroupInfoActiveSet(String name, Collection<? extends ActiveGroupEntry> groups)
    {
        myName = name;
        myGroupEntries = New.list();
        Set<JAXBActiveGroupEntry> entrySet = New.set();
        for (ActiveGroupEntry entry : groups)
        {
            entrySet.add(new JAXBActiveGroupEntry(entry));
        }
        myGroupEntries.addAll(entrySet);
    }

    /**
     * Adds the active group entry.
     *
     * @param entry the entry
     */
    public void addActiveGroupEntry(ActiveGroupEntry entry)
    {
        myGroupEntries.add(new JAXBActiveGroupEntry(entry));
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
        JAXBDataGroupInfoActiveSet other = (JAXBDataGroupInfoActiveSet)obj;
        return EqualsHelper.equals(myName, other.myName, myGroupEntries, other.myGroupEntries);
    }

    @Override
    public List<JAXBActiveGroupEntry> getGroupEntries()
    {
        return myGroupEntries;
    }

    @Override
    public List<String> getGroupIds()
    {
        List<String> groupIds = New.list();
        for (JAXBActiveGroupEntry entry : myGroupEntries)
        {
            groupIds.add(entry.getId());
        }
        return groupIds;
    }

    @Override
    public String getName()
    {
        return myName;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + (myGroupEntries == null ? 0 : myGroupEntries.hashCode());
        result = prime * result + (myName == null ? 0 : myName.hashCode());
        return result;
    }

    @Override
    public void setName(String name)
    {
        myName = name;
    }
}

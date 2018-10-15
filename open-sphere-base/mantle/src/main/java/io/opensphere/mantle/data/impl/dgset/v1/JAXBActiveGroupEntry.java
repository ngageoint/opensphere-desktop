package io.opensphere.mantle.data.impl.dgset.v1;

import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import io.opensphere.mantle.data.ActiveGroupEntry;

/**
 * The Class JAXBActiveGroupEntry.
 */
@XmlRootElement(name = "ActiveGroupEntry")
@XmlAccessorType(XmlAccessType.FIELD)
public class JAXBActiveGroupEntry implements ActiveGroupEntry
{
    /** The name. */
    @XmlAttribute(name = "id")
    private String myGroupId;

    /** The name. */
    @XmlAttribute(name = "name", required = false)
    private String myGroupName;

    /**
     * Default CTOR.
     */
    public JAXBActiveGroupEntry()
    {
    }

    /**
     * Instantiates a new jAXB active group entry.
     *
     * @param other the other
     */
    public JAXBActiveGroupEntry(ActiveGroupEntry other)
    {
        myGroupName = other.getName();
        myGroupId = other.getId();
    }

    /**
     * Instantiates a new jAXB active group entry.
     *
     * @param name the name
     * @param id the id
     */
    public JAXBActiveGroupEntry(String name, String id)
    {
        myGroupName = name;
        myGroupId = id;
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
        JAXBActiveGroupEntry other = (JAXBActiveGroupEntry)obj;
        return Objects.equals(myGroupId, other.myGroupId);
    }

    @Override
    public String getId()
    {
        return myGroupId;
    }

    @Override
    public String getName()
    {
        return myGroupName;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + (myGroupId == null ? 0 : myGroupId.hashCode());
        return result;
    }

    /**
     * Sets the id.
     *
     * @param id the new id
     */
    public void setId(String id)
    {
        myGroupId = id;
    }

    /**
     * Sets the name.
     *
     * @param name the new name
     */
    public void setName(String name)
    {
        myGroupName = name;
    }

    @Override
    public String toString()
    {
        return myGroupName + " [" + myGroupId + "]";
    }
}

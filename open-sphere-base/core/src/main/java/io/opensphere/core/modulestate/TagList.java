package io.opensphere.core.modulestate;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import io.opensphere.core.util.collections.New;

/** List of tags for a state file. */
@XmlRootElement(name = "tags", namespace = ModuleStateController.STATE_NAMESPACE)
@XmlAccessorType(XmlAccessType.NONE)
public class TagList implements Iterable<String>
{
    /** The list of tags. */
    @XmlElement(name = "tag", namespace = ModuleStateController.STATE_NAMESPACE)
    private final List<String> myTags = New.list();

    /** Default constructor. */
    public TagList()
    {
    }

    /**
     * Constructor with tags.
     *
     * @param tags The tags.
     */
    public TagList(Collection<? extends String> tags)
    {
        myTags.addAll(tags);
    }

    /**
     * Get the tags.
     *
     * @return The tags.
     */
    public List<String> getTags()
    {
        return myTags;
    }

    @Override
    public Iterator<String> iterator()
    {
        return myTags.iterator();
    }

    /**
     * Set the tags.
     *
     * @param tags The tags.
     */
    public void setTags(Collection<? extends String> tags)
    {
        myTags.clear();
        myTags.addAll(tags);
    }
}

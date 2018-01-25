package io.opensphere.core.help.data;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

/**
 * This class holds information pertaining to a mapping entry for a java help
 * mapping file.
 */
@XmlAccessorType(XmlAccessType.NONE)
public class HelpIndexEntry extends AbstractHelpEntry
{
    /** The list of sub entries. */
    @XmlElement(name = "indexitem", required = false)
    private List<HelpIndexEntry> myIndexEntries;

    /**
     * Default constructor (for JAXB).
     */
    public HelpIndexEntry()
    {
        myIndexEntries = new ArrayList<>();
    }

    /**
     * Constructor.
     *
     * @param target The target value.
     * @param title The title value.
     */
    public HelpIndexEntry(String target, String title)
    {
        setTarget(target);
        setTitle(title);
        myIndexEntries = new ArrayList<>();
    }

    /**
     * Add a table of contents entry to my list of sub entries.
     *
     * @param item The index entry to add.
     */
    public void addIndexItem(HelpIndexEntry item)
    {
        if (myIndexEntries == null)
        {
            myIndexEntries = new ArrayList<>();
        }
        myIndexEntries.add(item);
    }

    /**
     * Accessor for my list of sub entries.
     *
     * @return The list of index sub entries.
     */
    public List<HelpIndexEntry> getIndexItems()
    {
        return myIndexEntries;
    }

    /**
     * Mutator for my list of sub entries.
     *
     * @param indexItems The list of index sub entries.
     */
    public void setIndexItems(List<HelpIndexEntry> indexItems)
    {
        myIndexEntries = indexItems;
    }
}

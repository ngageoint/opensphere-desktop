package io.opensphere.core.help.data;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * This class holds information pertaining to a table of contents entry for a
 * java help file.
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "tocitem")
public class HelpTOCEntry extends AbstractHelpEntry
{
    /** The list of sub entries. */
    @XmlElement(name = "tocitem", required = false)
    private List<HelpTOCEntry> myTOCEntries;

    /**
     * Default Constructor (for JAXB).
     */
    public HelpTOCEntry()
    {
        myTOCEntries = new ArrayList<>();
    }

    /**
     * Constructor.
     *
     * @param title The title attribute value.
     * @param targetID The target attribute value.
     */
    public HelpTOCEntry(String title, String targetID)
    {
        setTitle(title);
        setTarget(targetID);
        myTOCEntries = new ArrayList<>();
    }

    /**
     * Add a table of contents entry to my list of sub entries.
     *
     * @param item The table of contents entry to add.
     */
    public void addTOCItem(HelpTOCEntry item)
    {
        if (myTOCEntries == null)
        {
            myTOCEntries = new ArrayList<>();
        }
        myTOCEntries.add(item);
    }

    /**
     * Get table of contents sub entry at specific index.
     *
     * @param index The index.
     * @return The sub entry.
     */
    public HelpTOCEntry getEntry(int index)
    {
        return myTOCEntries.get(index);
    }

    /**
     * Accessor for the last table of contents sub entry.
     *
     * @return The last sub entry.
     */
    public HelpTOCEntry getLastEntry()
    {
        return myTOCEntries.get(myTOCEntries.size() - 1);
    }

    /**
     * Accessor for my list of sub entries.
     *
     * @return The list of table of contents sub entries.
     */
    public List<HelpTOCEntry> getTocItems()
    {
        return myTOCEntries;
    }

    /**
     * Mutator for my list of sub entries.
     *
     * @param tocItems The list of table of contents sub entries.
     */
    public void setTocItems(List<HelpTOCEntry> tocItems)
    {
        myTOCEntries = tocItems;
    }
}

package io.opensphere.core.help.data;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.log4j.Logger;

import io.opensphere.core.util.JAXBContextHelper;

/**
 * This will be the top level of the help table of contents.
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = "toc")
public class HelpTOC
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(HelpTOC.class);

    /** The list of table of contents entries. */
    @XmlElement(name = "tocitem")
    private List<HelpTOCEntry> myEntries;

    /** The target attribute of the entry. */
    @XmlAttribute(name = "target", required = true)
    private String myTarget;

    /** The text attribute of the entry. */
    @XmlAttribute(name = "text", required = true)
    private String myTitle;

    /**
     * Default constructor.
     */
    public HelpTOC()
    {
        myEntries = new ArrayList<>();
    }

    /**
     * Constructor.
     *
     * @param title The title attribute value.
     * @param targetID The target attribute value.
     */
    public HelpTOC(String title, String targetID)
    {
        myTitle = title;
        myTarget = targetID;
    }

    /**
     * Add the table of content entries from the given list into my own.
     *
     * @param tocList The other top level table of contents.
     * @return True if successful, false otherwise.
     */
    public boolean add(HelpTOC tocList)
    {
        if (tocList == null || tocList.getTOCEntries().isEmpty())
        {
            return false;
        }
        boolean result = true;
        for (HelpTOCEntry entry : tocList.getTOCEntries())
        {
            result &= myEntries.add(entry);
        }
        return result;
    }

    /**
     * Add a table of contents entry to my existing entries.
     *
     * @param entry The entry to add.
     * @return True if successful, false otherwise.
     */
    public boolean add(HelpTOCEntry entry)
    {
        return myEntries.add(entry);
    }

    /**
     * Add a table of contents entry to my existing entries at the specified
     * index.
     *
     * @param index The index.
     * @param entry The entry to add.
     */
    public void add(int index, HelpTOCEntry entry)
    {
        myEntries.add(index, entry);
    }

    /**
     * Accessor for the last entry in my list of table of content entries.
     *
     * @return The last entry.
     */
    public HelpTOCEntry getLastEntry()
    {
        return myEntries.get(myEntries.size() - 1);
    }

    /**
     * Accessor for the target attribute.
     *
     * @return The target attribute.
     */
    public String getTarget()
    {
        return myTarget;
    }

    /**
     * Accessor for the title.
     *
     * @return The title.
     */
    public String getTitle()
    {
        return myTitle;
    }

    /**
     * Accessor for the list of my table of content entries.
     *
     * @return The list of my table of content entries.
     */
    public List<HelpTOCEntry> getTOCEntries()
    {
        return myEntries;
    }

    /**
     * Remove the table of content entries associated with the given list.
     *
     * @param tocList The other top level table of contents.
     */
    public void remove(HelpTOC tocList)
    {
        for (HelpTOCEntry entry : tocList.getTOCEntries())
        {
            myEntries.remove(entry);
        }
    }

    /**
     * Mutator for the target attribute.
     *
     * @param target The target attribute.
     */
    public void setTarget(String target)
    {
        myTarget = target;
    }

    /**
     * Mutator for the title attribute.
     *
     * @param text The title attribute.
     */
    public void setTitle(String text)
    {
        myTitle = text;
    }

    /**
     * Convenience method to put the XML of this class into a string.
     *
     * @return String representation of the XML of this class.
     */
    public String toXml()
    {
        JAXBContext c = null;
        Marshaller m = null;
        StringWriter sw = new StringWriter();
        try
        {
            c = JAXBContextHelper.getCachedContext(HelpTOC.class);
            m = c.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            m.marshal(this, sw);
        }
        catch (JAXBException e)
        {
            LOGGER.error("Unable to create help mapping XML string: " + e.getMessage());
        }
        return sw.toString();
    }
}

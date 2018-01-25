package io.opensphere.core.help.data;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.log4j.Logger;

import io.opensphere.core.util.JAXBContextHelper;

/**
 * This will be the top level of the help indexing contents.
 */
@XmlRootElement(name = "index")
@XmlAccessorType(XmlAccessType.NONE)
public class HelpIndex
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(HelpIndex.class);

    /** The list of index entries. */
    @XmlElement(name = "indexitem")
    private final List<HelpIndexEntry> myIndexEntries;

    /**
     * Default constructor.
     */
    public HelpIndex()
    {
        myIndexEntries = new ArrayList<>();
    }

    /**
     * Add Index entries from another top level list to my list of indices.
     *
     * @param indexList The other index list to copy to my own.
     * @return True if successful, false otherwise.
     */
    public boolean add(HelpIndex indexList)
    {
        if (indexList == null || indexList.getIndexEntries().isEmpty())
        {
            return false;
        }
        boolean result = true;
        for (HelpIndexEntry entry : indexList.getIndexEntries())
        {
            result &= myIndexEntries.add(entry);
        }
        return result;
    }

    /**
     * Add a index entry to my list of entries.
     *
     * @param entry The index entry to add.
     * @return True if successful, false otherwise.
     */
    public boolean add(HelpIndexEntry entry)
    {
        return myIndexEntries.add(entry);
    }

    /**
     * Accessor for the list of index entries.
     *
     * @return The list of index entries.
     */
    public List<HelpIndexEntry> getIndexEntries()
    {
        return myIndexEntries;
    }

    /**
     * Remove from my index entries at the specified index.
     *
     * @param index The index to remove.
     * @return True if successful, false otherwise.
     */
    public HelpIndexEntry remove(int index)
    {
        return myIndexEntries.remove(index);
    }

    /**
     * Remove the given index entry from my indices list.
     *
     * @param entry The index entry to remove
     * @return True if successful, false otherwise.
     */
    public boolean remove(Object entry)
    {
        return myIndexEntries.remove(entry);
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
            c = JAXBContextHelper.getCachedContext(HelpIndex.class);
            m = c.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            m.marshal(this, sw);
        }
        catch (JAXBException e)
        {
            LOGGER.error("Unable to create help indexing XML string: " + e.getMessage());
        }
        return sw.toString();
    }
}

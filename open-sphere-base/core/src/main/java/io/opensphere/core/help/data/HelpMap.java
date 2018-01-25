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
 * This will be the top level of the help mapping contents.
 */
@XmlRootElement(name = "map")
@XmlAccessorType(XmlAccessType.NONE)
public class HelpMap
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(HelpMap.class);

    /** The list of mapping entries. */
    @XmlElement(name = "mapID")
    private final List<HelpMapEntry> myMappingEntries;

    /**
     * Default constructor.
     */
    public HelpMap()
    {
        myMappingEntries = new ArrayList<>();
    }

    /**
     * Add mapping entries from another top level list to my list of mappings.
     *
     * @param mapList The other mappings list to copy to my own.
     * @return True if successful, false otherwise.
     */
    public boolean add(HelpMap mapList)
    {
        if (mapList == null || mapList.getMapEntries().isEmpty())
        {
            return false;
        }
        boolean result = true;
        for (HelpMapEntry entry : mapList.getMapEntries())
        {
            result &= myMappingEntries.add(entry);
        }
        return result;
    }

    /**
     * Add a mapping entry to my list of entries.
     *
     * @param entry The mapping entry to add.
     * @return True if successful, false otherwise.
     */
    public boolean add(HelpMapEntry entry)
    {
        return myMappingEntries.add(entry);
    }

    /**
     * Accessor for the list of mapping entries.
     *
     * @return The list of mapping entries.
     */
    public List<HelpMapEntry> getMapEntries()
    {
        return myMappingEntries;
    }

    /**
     * Remove from my mappings entries at the specified index.
     *
     * @param index The index to remove.
     * @return True if successful, false otherwise.
     */
    public HelpMapEntry remove(int index)
    {
        return myMappingEntries.remove(index);
    }

    /**
     * Remove the given mapping entry from my mappings list.
     *
     * @param entry The mapping entry to remove
     * @return True if successful, false otherwise.
     */
    public boolean remove(Object entry)
    {
        return myMappingEntries.remove(entry);
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
            c = JAXBContextHelper.getCachedContext(HelpMap.class);
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

package io.opensphere.core.help;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;

import io.opensphere.core.help.data.HelpMap;
import io.opensphere.core.util.XMLUtilities;

/**
 * This is a helper class to handle reading and writing mapping entries to the
 * help mappings file.
 */
public class MapFileHelper
{
    /** Logger used. */
    private static final Logger LOGGER = Logger.getLogger(MapFileHelper.class);

    /** The mappings file name. */
    private static final String ourFilename = "helpMap.xml";

    /** The help map file entries. */
    private final HelpMap myMapEntries;

    /** A mapping of name (plugin name) to map file entries. */
    private final Map<String, HelpMap> myMapEntriesMap;

    /** The help map file. */
    private final File myMapFile;

    /**
     * Constructor.
     *
     * @param baseDir The base directory where all the help files will reside.
     */
    public MapFileHelper(String baseDir)
    {
        myMapFile = new File(baseDir + File.separator + ourFilename);
        if (!myMapFile.exists())
        {
            LOGGER.error("Unable to find help mapping file: " + myMapFile.getAbsolutePath());
        }
        myMapEntries = new HelpMap();
        myMapEntriesMap = new HashMap<>();
    }

    /**
     * Add entries to the help mappings.
     *
     * @param name The name of who is adding entries (most likely the plugin
     *            name).
     * @param mappings The entries to add to the help mappings.
     * @return True if successful, false otherwise.
     */
    public boolean addItem(String name, HelpMap mappings)
    {
        boolean result = true;
        synchronized (myMapEntriesMap)
        {
            myMapEntriesMap.put(name, mappings);
        }

        synchronized (myMapEntries)
        {
            result &= myMapEntries.add(mappings);
            result &= writeFile();
        }

        return result;
    }

    /**
     * Standard accessor for the help mappings.
     *
     * @return The help mappings.
     */
    public HelpMap getHelpMappings()
    {
        return myMapEntries;
    }

    /**
     * Remove the mappings file.
     *
     * @return True if successful, false otherwise.
     */
    public boolean removeFile()
    {
        return myMapFile.delete();
    }

    /**
     * Remove entries from the help mappings associated with the given name.
     *
     * @param name The name for the mappings entries to remove.
     * @return True if successful, false otherwise.
     */
    public boolean removeItem(String name)
    {
        synchronized (myMapEntriesMap)
        {
            if (myMapEntriesMap.containsKey(name))
            {
                synchronized (myMapEntries)
                {
                    myMapEntries.remove(myMapEntriesMap.get(name));

                    myMapEntriesMap.remove(name);
                    return writeFile();
                }
            }
        }
        return false;
    }

    /**
     * Write my current entries to the mappings file.
     *
     * @return True if successful, false otherwise.
     */
    private boolean writeFile()
    {
        boolean result = false;
        try
        {
            XMLUtilities.writeXMLObject(myMapEntries, myMapFile);
            result = true;
        }
        catch (JAXBException e)
        {
            LOGGER.error("Unable to write help mappings file: " + e.getMessage());
        }
        return result;
    }
}

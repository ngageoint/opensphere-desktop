package io.opensphere.core.help;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;

import io.opensphere.core.help.data.HelpIndex;
import io.opensphere.core.util.XMLUtilities;

/**
 * This is a helper class to handle reading and writing index entries to the
 * help indexing file.
 */
public class IndexFileHelper
{
    /** Logger used. */
    private static final Logger LOGGER = Logger.getLogger(IndexFileHelper.class);

    /** The index file name. */
    private static final String ourFilename = "helpIndex.xml";

    /** The help index file entries. */
    private final HelpIndex myIndexEntries;

    /** A mapping of name (plugin name) to index file entries. */
    private final Map<String, HelpIndex> myIndexEntriesMap;

    /** The help index file. */
    private final File myIndexFile;

    /**
     * Constructor.
     *
     * @param baseDir The base directory where all the help files will reside.
     */
    public IndexFileHelper(String baseDir)
    {
        myIndexFile = new File(baseDir + File.separator + ourFilename);
        if (!myIndexFile.exists())
        {
            LOGGER.error("Unable to find index mapping file: " + myIndexFile.getAbsolutePath());
        }
        myIndexEntries = new HelpIndex();
        myIndexEntriesMap = new HashMap<>();
    }

    /**
     * Add entries to the help index.
     *
     * @param name The name of who is adding entries (most likely the plugin
     *            name).
     * @param indexes The entries to add to the help indexing.
     * @return True if successful, false otherwise.
     */
    public boolean addItem(String name, HelpIndex indexes)
    {
        boolean result = true;
        synchronized (myIndexEntriesMap)
        {
            myIndexEntriesMap.put(name, indexes);
        }

        synchronized (myIndexEntries)
        {
            result &= myIndexEntries.add(indexes);
            result &= writeFile();
        }

        return result;
    }

    /**
     * Standard accessor for the help indices.
     *
     * @return The help index entries.
     */
    public HelpIndex getHelpIndexEntries()
    {
        return myIndexEntries;
    }

    /**
     * Remove the index file.
     *
     * @return True if successful, false otherwise.
     */
    public boolean removeFile()
    {
        return myIndexFile.delete();
    }

    /**
     * Remove entries from the help indices associated with the given name.
     *
     * @param name The name for the index entries to remove.
     * @return True if successful, false otherwise.
     */
    public boolean removeItem(String name)
    {
        synchronized (myIndexEntriesMap)
        {
            if (myIndexEntriesMap.containsKey(name))
            {
                synchronized (myIndexEntries)
                {
                    myIndexEntries.remove(myIndexEntriesMap.get(name));
                    myIndexEntriesMap.remove(name);
                    return writeFile();
                }
            }
        }
        return false;
    }

    /**
     * Write my current entries to the indices file.
     *
     * @return True if successful, false otherwise.
     */
    private boolean writeFile()
    {
        boolean result = false;

        try
        {
            XMLUtilities.writeXMLObject(myIndexEntries, myIndexFile);
            result = true;
        }
        catch (JAXBException e)
        {
            LOGGER.error("Unable to write help index file: " + e.getMessage());
        }
        return result;
    }
}

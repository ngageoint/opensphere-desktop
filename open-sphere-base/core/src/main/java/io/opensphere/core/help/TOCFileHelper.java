package io.opensphere.core.help;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;

import io.opensphere.core.help.data.HelpTOC;
import io.opensphere.core.util.XMLUtilities;

/**
 * This is a helper class to handle reading and writing table of content entries
 * to the help table of contents file.
 */
public class TOCFileHelper
{
    /** Logger used. */
    private static final Logger LOGGER = Logger.getLogger(TOCFileHelper.class);

    /** The table of contents file name. */
    private static final String ourFilename = "helpToc.xml";

    /** The help table of contents. */
    private final HelpTOC myTOC;

    /** The table of contents file. */
    private final File myTOCFile;

    /** A mapping of name (plugin name) to table of contents entries. */
    private final Map<String, HelpTOC> myTOCLists;

    /**
     * Constructor.
     *
     * @param baseDir The base directory where all the help files will reside.
     */
    public TOCFileHelper(String baseDir)
    {
        myTOCFile = new File(baseDir + File.separator + ourFilename);
        if (!myTOCFile.exists())
        {
            LOGGER.error("Unable to find help table of contents file: " + myTOCFile.getAbsolutePath());
        }
        myTOC = new HelpTOC();
        myTOCLists = new HashMap<>();
    }

    /**
     * Add entries to the main help table of contents.
     *
     * @param name The name of who is adding entries (most likely the plugin
     *            name).
     * @param tocItem The entries to add to the table of contents.
     * @return True if successful, false otherwise.
     */
    public boolean addItem(String name, HelpTOC tocItem)
    {
        boolean result = true;
        synchronized (myTOCLists)
        {
            myTOCLists.put(name, tocItem);
        }
        synchronized (myTOC)
        {
            result &= myTOC.add(tocItem);
            result &= writeFile();
        }

        return result;
    }

    /**
     * Standard accessor for the help table of contents.
     *
     * @return The help table of contents.
     */
    public HelpTOC getHelpTOC()
    {
        return myTOC;
    }

    /**
     * Remove the table of contents file.
     *
     * @return True if successful, false otherwise.
     */
    public boolean removeFile()
    {
        return myTOCFile.delete();
    }

    /**
     * Remove entries from the help table of contents associated with the given
     * name.
     *
     * @param name The name for the table of contents entries to remove.
     * @return True if successful, false otherwise.
     */
    public boolean removeItem(String name)
    {
        synchronized (myTOCLists)
        {
            if (myTOCLists.containsKey(name))
            {
                synchronized (myTOC)
                {
                    myTOC.remove(myTOCLists.get(name));

                    myTOCLists.remove(name);
                    return writeFile();
                }
            }
        }
        return false;
    }

    /**
     * Write my current entries to the table of content file.
     *
     * @return True if successful, false otherwise.
     */
    private boolean writeFile()
    {
        boolean result = false;

        try
        {
            XMLUtilities.writeXMLObject(myTOC, myTOCFile);
            result = true;
        }
        catch (JAXBException e)
        {
            LOGGER.error("Unable to write help table of contents file: " + e.getMessage());
        }
        return result;
    }
}

package io.opensphere.core.help;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import org.apache.log4j.Logger;

import io.opensphere.core.help.data.HelpIndex;
import io.opensphere.core.help.data.HelpIndexEntry;
import io.opensphere.core.help.data.HelpMap;
import io.opensphere.core.help.data.HelpMapEntry;
import io.opensphere.core.help.data.HelpTOC;
import io.opensphere.core.help.data.HelpTOCEntry;
import io.opensphere.core.util.lang.StringUtilities;

/**
 * Class holds utility methods that are used by the help system.
 */
public final class HelpUtilities
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(HelpUtilities.class);

    /**
     * This helper method uses the information from the help mapping values to
     * create a search configuration file. The search configuration file is used
     * in indexing and creating the needed files for javaHelp searching.
     *
     * The search configuration file has the syntax of the following
     *
     * IndexRemove /home/user/opensphere/vortex/help/content File
     * /home/user/opensphere/vortex/help/content/files/helpFile1.html File
     * /home/user/opensphere/vortex/help/content/files/helpFile2.html
     *
     * The first line tells which directory to start in (don't search parent
     * directories). The following lines describe which files to search and
     * index.
     *
     * @param cfgFile The search config file to write to.
     * @param fileLocation The name of the top level directory that contains
     *            HTML files.
     * @param helpMappings The help file mapping information.
     * @throws IOException The thrown exception.
     */
    public static void createDBConfig(File cfgFile, String fileLocation, HelpMap helpMappings) throws IOException
    {
        // The search configuration file has the syntax of the following
        //
        // IndexRemove /home/user/opensphere/vortex/help/content
        // File /home/user/opensphere/vortex/help/content/files/helpFile1.html
        // File /home/user/opensphere/vortex/help/content/files/helpFile2.html
        //
        // The first line tells which directory to start in (remove this
        // parent directory). The following lines describe which files
        // to search and index.

        if (cfgFile == null || helpMappings == null)
        {
            throw new IllegalArgumentException();
        }

        if (!cfgFile.exists())
        {
            LOGGER.warn("Help system search config file doesn't exist" + cfgFile.getAbsolutePath());
            if (!cfgFile.createNewFile())
            {
                LOGGER.warn("Unable to create search config file for the help system");
                return;
            }
        }

        OutputStreamWriter outputWriter = null;
        try
        {
            outputWriter = new OutputStreamWriter(new FileOutputStream(cfgFile), StringUtilities.DEFAULT_CHARSET);

            final StringBuilder builder = new StringBuilder();
            builder.append("IndexRemove ").append(fileLocation).append('\n');
            outputWriter.write(builder.toString());

            for (final HelpMapEntry entry : helpMappings.getMapEntries())
            {
                final StringBuilder strBuilder = new StringBuilder();
                strBuilder.append("File ").append(entry.getUrl()).append('\n');
                outputWriter.write(strBuilder.toString());
            }
        }
        catch (final FileNotFoundException e)
        {
            if (outputWriter != null)
            {
                outputWriter.close();
            }
            LOGGER.error("Unable to write to help search config file: " + e.getMessage());
        }
        finally
        {
            if (outputWriter != null)
            {
                outputWriter.close();
            }
        }
    }

    /**
     * Helper method that creates index information from the table of contents
     * information. The same structure is retained but with the index specific
     * tags.
     *
     * @param helpTOC The help table of contents information.
     * @return The help indexing information.
     */
    public static HelpIndex createIndex(HelpTOC helpTOC)
    {
        final HelpIndex index = new HelpIndex();

        index.add(new HelpIndex());
        for (final HelpTOCEntry entry : helpTOC.getTOCEntries())
        {
            LOGGER.warn(" entry target" + entry.getTarget() + " title = " + entry.getTitle());
            index.add(createIndexEntry(entry));
        }

        return index;
    }

    /**
     * Helper method that recursively goes through help table of contents
     * entries and creates indexing entries.
     *
     * @param tocEntry The table of contents entries.
     * @return Indexing entries that retain the same structure of the given
     *         table of content entries.
     */
    public static HelpIndexEntry createIndexEntry(HelpTOCEntry tocEntry)
    {
        final HelpIndexEntry indexEntry = new HelpIndexEntry(tocEntry.getTarget(), tocEntry.getTitle());
        for (final HelpTOCEntry entry : tocEntry.getTocItems())
        {
            indexEntry.addIndexItem(createIndexEntry(entry));
        }

        return indexEntry;
    }

    /**
     * Private default constructor to disallow instantiation.
     */
    private HelpUtilities()
    {
    }
}

package io.opensphere.core.matchers;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Comparator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * An input stream comparator that compares two input streams by reading them
 * into a string and comparing the strings.
 */
public class InputStreamComparator implements Comparator<InputStream>
{
    /**
     * Used to log messages.
     */
    private static final Logger LOGGER = LogManager.getLogger(InputStreamComparator.class);

    @Override
    public int compare(InputStream o1, InputStream o2)
    {
        int compare = 1;
        try
        {
            String string1 = readStream(o1);
            String string2 = readStream(o2);

            compare = string1.compareTo(string2);
        }
        catch (IOException e)
        {
            LOGGER.error(e.getMessage(), e);
        }

        return compare;
    }

    /**
     * Reads the stream and puts it into a string.
     *
     * @param stream The stream to read.
     * @return The string representation of the string.
     * @throws IOException Stream read error.
     */
    private String readStream(InputStream stream) throws IOException
    {
        InputStreamReader reader = new InputStreamReader(stream);
        StringBuilder builder = new StringBuilder();
        while (reader.ready())
        {
            builder.append((char)reader.read());
        }

        return builder.toString();
    }
}

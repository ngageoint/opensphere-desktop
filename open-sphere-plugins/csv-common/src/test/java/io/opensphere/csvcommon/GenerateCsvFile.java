package io.opensphere.csvcommon;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.StringUtilities;

/**
 * Generates a CSV file with an arbitrary number of points.
 */
public final class GenerateCsvFile
{
    /** The logger. */
    private static final Logger LOGGER = Logger.getLogger(GenerateCsvFile.class);

    /**
     * Main.
     *
     * @param args the arguments
     */
    public static void main(String[] args)
    {
        if (args.length >= 2)
        {
            int numPoints = Integer.parseInt(args[0]);
            Path path = Paths.get(args[1]);
            generate(numPoints, path);
        }
        else
        {
            LOGGER.info("Required arguments: <number-of-points> <output-file>");
        }
    }

    /**
     * Generates the CSV file.
     *
     * @param numPoints the number of points
     * @param path the file path
     */
    private static void generate(int numPoints, Path path)
    {
        long timeInterval = 60000;
        long time = System.currentTimeMillis() - numPoints * timeInterval;
        float lat = 0;
        float lon = 0;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

        List<String> lines = New.list(numPoints + 1);
        lines.add("index,time,lat,lon");
        for (int i = 0; i < numPoints; ++i)
        {
            lon = (float)(Math.random() * 360. - 180.);
            lat = (float)(Math.random() * 180. - 90.);

            String line = StringUtilities.join(",", String.valueOf(i + 1), format.format(new Date(time)), String.valueOf(lat),
                    String.valueOf(lon));
            lines.add(line);

            time += timeInterval + (long)(Math.random() * timeInterval - (timeInterval >> 1));
        }

        try
        {
            Files.write(path, lines, Charset.defaultCharset());
        }
        catch (IOException e)
        {
            LOGGER.error(e, e);
        }
    }

    /** Private constructor. */
    private GenerateCsvFile()
    {
    }
}

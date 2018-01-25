package io.opensphere.csvcommon.detect.datetime.util;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import io.opensphere.core.common.configuration.date.DateFormat.Type;
import io.opensphere.core.util.collections.New;
import io.opensphere.csvcommon.detect.datetime.model.PotentialColumn;
import io.opensphere.csvcommon.detect.datetime.model.SuccessfulFormat;

/**
 * Contains utility methods for a PotentialColumn.
 *
 */
public final class PotentialColumnUtils
{
    /**
     * Gets the most successful date format for the given potential date column.
     *
     * @param potential The potential date column.
     * @return The most successful date format.
     */
    public static SuccessfulFormat getMostSuccessfulFormat(PotentialColumn potential)
    {
        int max = -1;
        SuccessfulFormat mostSuccessful = null;

        Collection<SuccessfulFormat> formats = potential.getFormats().values();

        for (SuccessfulFormat format : formats)
        {
            int numberOfSuccesses = format.getNumberOfSuccesses();
            if (numberOfSuccesses > max)
            {
                max = numberOfSuccesses;
                mostSuccessful = format;
            }
        }

        return mostSuccessful;
    }

    /**
     * Gets the most successful date format for the given format type.
     *
     * @param potential Contains the successful formats for a given column.
     * @param suggestedType The format type Date, Date/Time, or Time.
     * @return The most success format for the given format type. If the format
     *         type was not found it then returns null.
     */
    public static SuccessfulFormat getMostSuccessfulFormat(PotentialColumn potential, Type suggestedType)
    {
        int max = -1;
        SuccessfulFormat mostSuccessful = null;

        Collection<SuccessfulFormat> formats = potential.getFormats().values();

        for (SuccessfulFormat format : formats)
        {
            if (format.getFormat().getType() == suggestedType)
            {
                int numberOfSuccesses = format.getNumberOfSuccesses();
                if (numberOfSuccesses > max)
                {
                    max = numberOfSuccesses;
                    mostSuccessful = format;
                }
            }
        }

        return mostSuccessful;
    }

    /**
     * Gets the most successful date formats for the given format type.
     *
     * @param potential Contains the successful formats for a given column.
     * @param suggestedType The format type Date, Date/Time, or Time.
     * @return The most success format for the given format type. If the format
     *         type was not found it then returns null.
     */
    public static List<SuccessfulFormat> getMostSuccessfulFormats(PotentialColumn potential, Type suggestedType)
    {
        List<SuccessfulFormat> formats = New.list();

        for (SuccessfulFormat format : potential.getFormats().values())
        {
            if (format.getFormat().getType() == suggestedType)
            {
                formats.add(format);
            }
        }

        Collections.sort(formats, new Comparator<SuccessfulFormat>()
        {
            @Override
            public int compare(SuccessfulFormat o1, SuccessfulFormat o2)
            {
                int compare = 0;

                if (o1.getNumberOfSuccesses() > o2.getNumberOfSuccesses())
                {
                    compare = -1;
                }
                else if (o1.getNumberOfSuccesses() < o2.getNumberOfSuccesses())
                {
                    compare = 1;
                }

                return compare;
            }
        });

        if (!formats.isEmpty() && StringUtils.isNotEmpty(formats.get(0).getFormat().getRegex()))
        {
            formats = New.list(formats.get(0));
        }

        return formats;
    }

    /**
     * Not constructible.
     */
    private PotentialColumnUtils()
    {
    }
}

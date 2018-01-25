package io.opensphere.shapefile;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import io.opensphere.core.common.configuration.date.DateFormat;
import io.opensphere.shapefile.config.v1.ShapeFileSource;

/**
 * The Class MetadataTimeExtractor.
 */
public class MetadataTimeExtractor
{
    /** The date column. */
    private int myDateColumn;

    /** The date format. */
    private SimpleDateFormat myDateFormat;

    /** The time column. */
    private int myTimeColumn;

    /** The time format. */
    private SimpleDateFormat myTimeFormat;

    /** The using timestamp. */
    private boolean myUsingTimestamp;

    /**
     * Instantiates a new metadata time extractor.
     *
     * @param source the source
     */
    public MetadataTimeExtractor(ShapeFileSource source)
    {
        if (source != null)
        {
            DateFormat dateColumnFormat = null;
            DateFormat timeColumnFormat = null;

            ShapeFileSource sfs = source;
            myUsingTimestamp = sfs.usesTimestamp();
            myTimeColumn = sfs.getTimeColumn();
            myDateColumn = sfs.getDateColumn();

            dateColumnFormat = sfs.getDateFormat();
            timeColumnFormat = sfs.getTimeFormat();

            if (myTimeColumn != -1 || myDateColumn != -1)
            {
                myTimeFormat = null;
                if (timeColumnFormat != null)
                {
                    myTimeFormat = timeColumnFormat.getFormat();
                }

                myDateFormat = null;
                if (dateColumnFormat != null)
                {
                    myDateFormat = dateColumnFormat.getFormat();
                }

                // If we're not using a timestamp format append the date and
                // time formats together as we will do the same with the
                // values. We check the dateColIndex because if we're not
                // using a timestamp and we have a dateColIndex == -1 then
                // we have no time based columns at all.
                if (!myUsingTimestamp && myDateColumn != -1)
                {
                    myTimeFormat = new SimpleDateFormat(myDateFormat.toPattern() + " " + myTimeFormat.toPattern());
                }
            }
        }
    }

    /**
     * Extract date.
     *
     * @param metadata the metadata
     * @return the date
     */
    public Date extractDate(Object[] metadata)
    {
        Date resultDate;
        if (metadata != null && myTimeFormat != null && (myTimeColumn != -1 || myDateColumn != -1))
        {
            try
            {
                Object timeValue = myTimeColumn == -1 ? "" : metadata[myTimeColumn];
                Object dateValue = myDateColumn == -1 ? "" : metadata[myDateColumn];

                if (myUsingTimestamp)
                {
                    resultDate = myTimeFormat.parse(timeValue.toString());
                }
                else
                {
                    resultDate = myTimeFormat.parse(dateValue.toString() + " " + timeValue.toString());
                }
            }
            catch (ParseException e)
            {
                resultDate = null;
            }
        }
        else
        {
            resultDate = null;
        }
        return resultDate;
    }
}

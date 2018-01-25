package io.opensphere.csvcommon.detect.datetime.algorithm;

import java.util.List;
import java.util.Map;

import io.opensphere.csvcommon.common.CellSampler;
import io.opensphere.csvcommon.common.datetime.ConfigurationProvider;
import io.opensphere.csvcommon.common.datetime.DateColumnResults;
import io.opensphere.csvcommon.detect.ValueWithConfidence;
import io.opensphere.csvcommon.detect.datetime.model.PotentialColumn;

/**
 * Finds the potential date time columns within the csv data.
 */
public class DateTimeFinder
{
    /**
     * Gets the known date time formats.
     */
    private final ConfigurationProvider myConfigurationProvider;

    /**
     * Finds potential date columns.
     */
    private final MatchMaker myMatchMaker;

    /**
     * Rates the potentials and picks the best columns for dates.
     */
    private final DateRater myRater;

    /**
     * True if the detection is only used to detect formats.
     */
    private boolean myIsJustFormat;

    /**
     * Constructs a date time finder.
     *
     * @param configurationProvider Gets the known date time formats.
     */
    public DateTimeFinder(ConfigurationProvider configurationProvider)
    {
        myConfigurationProvider = configurationProvider;
        myMatchMaker = new MatchMaker();
        myRater = new DateRater();
    }

    /**
     * Finds potential dates.
     *
     * @param sampler The sample data from the csv file.
     *
     * @return The results.
     */
    public ValueWithConfidence<DateColumnResults> findDates(CellSampler sampler)
    {
        List<? extends List<? extends String>> sampleData = sampler.getBeginningSampleCells();

        Map<Integer, PotentialColumn> potentials = myMatchMaker.findPotentialDates(sampleData, myConfigurationProvider);

        if (!myIsJustFormat)
        {
            filterOutByColumnName(potentials, sampler);
        }

        return myRater.rateAndPick(potentials, sampleData);
    }

    /**
     * Removes columns that we know not to consider based on column name.
     *
     * @param potentials The map of columns to remove based on columns name.
     * @param sampler Gets the list of columns names if applicable.
     */
    private void filterOutByColumnName(Map<Integer, PotentialColumn> potentials, CellSampler sampler)
    {
        List<String> excludeColumns = myConfigurationProvider.getExcludeColumns();

        List<? extends String> headerCells = sampler.getHeaderCells();

        if (headerCells != null)
        {
            int columnIndex = 0;
            for (String headerCell : headerCells)
            {
                for (String excludeColumn : excludeColumns)
                {
                    if (headerCell.toUpperCase().contains(excludeColumn.toUpperCase()))
                    {
                        potentials.remove(columnIndex);
                    }
                }

                columnIndex++;
            }
        }
    }

    /**
     * Sets whether or not the date rater should just go through the steps to
     * detect formats, or if it should actually pick date columns.
     *
     * @param isJustFormats True if it should ignore some of the detection rules
     *            in order to find formats.
     */
    public void setIsJustDetectFormats(boolean isJustFormats)
    {
        myIsJustFormat = isJustFormats;
        myRater.setIsJustDetectFormats(isJustFormats);
        myMatchMaker.setIsJustForFormats(isJustFormats);
    }
}

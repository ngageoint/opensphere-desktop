package io.opensphere.csvcommon.detect.datetime.algorithm;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import io.opensphere.core.common.configuration.date.DateFormat;
import io.opensphere.core.common.configuration.date.DateFormat.Type;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.Pair;
import io.opensphere.csvcommon.common.datetime.ConfigurationProvider;
import io.opensphere.csvcommon.detect.datetime.model.PotentialColumn;
import io.opensphere.csvcommon.detect.datetime.model.SuccessfulFormat;
import io.opensphere.csvcommon.detect.datetime.util.Constants;

/**
 * This class looks at a double array of Strings (rows to columns) and figures
 * out which columns are potential date and/or time columns. It gathers the
 * results in a list of PotentialColumns and returns those results.
 */
public class MatchMaker
{
    /**
     * Indicates if the match maker should just be concerned of formats or
     * actually trying to find the date columns within a csv.
     */
    private boolean myIsJustForFormats;

    /**
     * Finds potential date columns and returns the formats that were successful
     * for those columns.
     *
     * @param tableData The sample table data to inspect.
     * @param provider Provides the configured list of date formats.
     * @return The potential date columns and the formats that succeeded.
     */
    public Map<Integer, PotentialColumn> findPotentialDates(List<? extends List<? extends String>> tableData,
            ConfigurationProvider provider)
    {
        Map<Integer, PotentialColumn> potentialDates = New.map();
        List<Pair<DateFormat, Pattern>> formatsAndPattern = compilePatterns(provider.getDateFormats().getFormats());
        int numberOfRows = tableData.size();

        Map<Integer, Integer> numberOfNonBlankCells = New.map();
        boolean hasCellsBeenCounted = false;

        for (Pair<DateFormat, Pattern> pair : formatsAndPattern)
        {
            DateFormat format = pair.getFirstObject();
            Pattern pattern = pair.getSecondObject();

            boolean isEmptyRegex = StringUtils.isEmpty(format.getRegex());

            for (List<? extends String> row : tableData)
            {
                for (int columnIndex = 0; columnIndex < row.size(); ++columnIndex)
                {
                    String cell = row.get(columnIndex);
                    if (StringUtils.isNotEmpty(cell))
                    {
                        if (!hasCellsBeenCounted)
                        {
                            if (!numberOfNonBlankCells.containsKey(columnIndex))
                            {
                                numberOfNonBlankCells.put(columnIndex, 0);
                            }

                            numberOfNonBlankCells.put(columnIndex, numberOfNonBlankCells.get(columnIndex) + 1);
                        }

                        boolean matches = isEmptyRegex;

                        if (!matches)
                        {
                            matches = pattern.matcher(cell).matches();
                        }

                        if (matches)
                        {
                            if (!potentialDates.containsKey(columnIndex))
                            {
                                PotentialColumn potential = new PotentialColumn();
                                potential.setColumnIndex(columnIndex);

                                potentialDates.put(columnIndex, potential);
                            }

                            PotentialColumn potential = potentialDates.get(columnIndex);

                            String formatKey = format.getSdf();

                            if (!potential.getFormats().containsKey(format.getSdf()))
                            {
                                SuccessfulFormat successfulFormat = new SuccessfulFormat();
                                successfulFormat.setNumberOfSuccesses(0);
                                successfulFormat.setFormat(format);

                                potential.getFormats().put(formatKey, successfulFormat);
                            }

                            SuccessfulFormat successfulFormat = potential.getFormats().get(formatKey);

                            if (!isEmptyRegex)
                            {
                                successfulFormat.setNumberOfSuccesses(successfulFormat.getNumberOfSuccesses() + 1);
                            }
                            else
                            {
                                successfulFormat.setNumberOfSuccesses(
                                        (int)(Constants.THRESHOLD_SCORE / Constants.PERCENT * numberOfRows + 1));
                            }
                        }
                    }
                }

                if (isEmptyRegex)
                {
                    break;
                }
            }

            hasCellsBeenCounted = true;
        }

        recalculateScores(numberOfNonBlankCells, potentialDates);

        return potentialDates;
    }

    /**
     * Recalculates the scores based on how many non blank cells there are.
     *
     * @param nonBlankCells The map of column index to the number of non blank
     *            values.
     * @param potentials The potential date columns.
     */
    private void recalculateScores(Map<Integer, Integer> nonBlankCells, Map<Integer, PotentialColumn> potentials)
    {
        for (Entry<Integer, PotentialColumn> entry : potentials.entrySet())
        {
            float numberOfNonBlankCells = 0f;
            if (nonBlankCells.containsKey(entry.getKey()))
            {
                numberOfNonBlankCells = nonBlankCells.get(entry.getKey());
            }

            PotentialColumn column = entry.getValue();

            for (Entry<String, SuccessfulFormat> formatEntry : column.getFormats().entrySet())
            {
                SuccessfulFormat successfulFormat = formatEntry.getValue();

                if (numberOfNonBlankCells != 0)
                {
                    successfulFormat.setNumberOfSuccesses(
                            (int)(successfulFormat.getNumberOfSuccesses() / numberOfNonBlankCells * Constants.PERCENT));
                }
                else
                {
                    successfulFormat.setNumberOfSuccesses(0);
                }
            }
        }
    }

    /**
     * Sets if the match maker should just be concerned of formats or actually
     * trying to find the date columns within a csv.
     *
     * @param isJustFormats True if the match maker will just look at formats,
     *            false otherwise.
     */
    public void setIsJustForFormats(boolean isJustFormats)
    {
        myIsJustForFormats = isJustFormats;
    }

    /**
     * Compiles regex patterns from the regex expression stored in the
     * configurations.
     *
     * @param formats The formats to compile the expressions for.
     * @return The list of format and their respective compiled regex pattern.
     */
    private List<Pair<DateFormat, Pattern>> compilePatterns(List<DateFormat> formats)
    {
        List<Pair<DateFormat, Pattern>> formatsAndPattern = New.list();

        for (DateFormat format : formats)
        {
            // Check if the format specifies day before month, if so we will not
            // auto detect that uncommon ambiguous format.
            boolean canAutoDetect = isFormatNotAutoDetect(format);

            if (canAutoDetect)
            {
                if (StringUtils.isNotEmpty(format.getRegex()))
                {
                    Pair<DateFormat, Pattern> pair = new Pair<>(format, Pattern.compile(format.getRegex()));

                    formatsAndPattern.add(pair);
                }
                else
                {
                    // Can't match with pattern so we will have to analyze the
                    // format string later so make the pattern a wildcard
                    // so that this format is selected by the matchmaker.
                    Pair<DateFormat, Pattern> pair = new Pair<>(format, Pattern.compile(".*"));

                    formatsAndPattern.add(pair);
                }
            }
        }

        return formatsAndPattern;
    }

    /**
     * Checks to see if the given format is unambiguous and can be auto
     * detected.
     *
     * @param format The format to inspect.
     * @return True if the format can be autodetected false otherwise.
     */
    private boolean isFormatNotAutoDetect(DateFormat format)
    {
        boolean canAutodetect = true;

        if (!myIsJustForFormats && (format.getType() == Type.DATE || format.getType() == Type.TIMESTAMP))
        {
            String sdf = format.getSdf();
            int monthIndex = sdf.indexOf('M');
            int dayIndex = sdf.lastIndexOf('d');

            if (dayIndex < monthIndex && !sdf.contains("MMM"))
            {
                canAutodetect = false;
            }
        }

        return canAutodetect;
    }
}

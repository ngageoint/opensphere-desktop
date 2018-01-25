package io.opensphere.csvcommon.detect.columnformat;

import io.opensphere.csvcommon.common.LineSampler;
import io.opensphere.csvcommon.detect.LineDetector;
import io.opensphere.csvcommon.detect.ValuesWithConfidence;
import io.opensphere.csvcommon.detect.columnformat.ColumnFormatParameters;

/**
 * Module that identifies column format.
 */
public class ColumnFormatDetector implements LineDetector<ColumnFormatParameters>
{
    @Override
    public ValuesWithConfidence<? extends ColumnFormatParameters> detect(LineSampler sampler)
    {
        // Determine delimiter
        ColumnDelimiterDetector delimiterDetector = new ColumnDelimiterDetector();
        ValuesWithConfidence<DelimitedColumnFormatParameters> delimitedFormat = delimiterDetector.detect(sampler);

        // Determine fixed column widths
        ColumnWidthDetector widthDetector = new ColumnWidthDetector();
        ValuesWithConfidence<FixedWidthColumnFormatParameters> fixedWidthFormat = widthDetector.detect(sampler);

        // Figure out the winner
        ValuesWithConfidence<? extends ColumnFormatParameters> format = determineWinner(delimitedFormat, fixedWidthFormat);

        return format;
    }

    /**
     * Determines the winning format.
     *
     * @param delimitedFormat the column count from the delimiter detector
     * @param fixedWidthFormat the column count from the fixed width detector
     * @return the winning format
     */
    private ValuesWithConfidence<? extends ColumnFormatParameters> determineWinner(
            ValuesWithConfidence<DelimitedColumnFormatParameters> delimitedFormat,
            ValuesWithConfidence<FixedWidthColumnFormatParameters> fixedWidthFormat)
    {
        int fixedWidthCount = fixedWidthFormat.getBestValue().getColumnDivisions().length + 1;

        ValuesWithConfidence<? extends ColumnFormatParameters> result;
        if (delimitedFormat.getBestValue().getTokenDelimiter().charValue() == ' ')
        {
            result = fixedWidthCount > 1 ? fixedWidthFormat : delimitedFormat;
        }
        else
        {
            result = fixedWidthCount > delimitedFormat.getBestValue().getColumnCount() ? fixedWidthFormat : delimitedFormat;
        }

        return result;
    }
}

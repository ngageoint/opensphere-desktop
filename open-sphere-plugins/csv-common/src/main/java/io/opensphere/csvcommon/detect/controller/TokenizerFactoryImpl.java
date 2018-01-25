package io.opensphere.csvcommon.detect.controller;

import io.opensphere.core.util.lang.PatternStringTokenizer;
import io.opensphere.core.util.lang.StringTokenizer;
import io.opensphere.core.util.lang.TextDelimitedStringTokenizer;
import io.opensphere.csvcommon.config.v2.CSVColumnFormat;
import io.opensphere.csvcommon.config.v2.CSVDelimitedColumnFormat;
import io.opensphere.csvcommon.config.v2.CSVFixedWidthColumnFormat;
import io.opensphere.csvcommon.detect.columnformat.ColumnFormatParameters;
import io.opensphere.csvcommon.detect.columnformat.DelimitedColumnFormatParameters;
import io.opensphere.csvcommon.detect.columnformat.FixedWidthColumnFormatParameters;

/**
 * Factory that creates tokenizers based on the known and guessed column
 * parameters.
 */
public class TokenizerFactoryImpl implements TokenizerFactory
{
    /**
     * Get a tokenizer to be used to split lines into cells. Preferably use the
     * truth format to generate the tokenizer, or use the detected format if
     * available.
     *
     * @param truthFormat The truth parameters.
     * @param detectedFormat The detected parameters.
     * @return The tokenizer.
     */
    @Override
    public StringTokenizer getTokenizer(CSVColumnFormat truthFormat, ColumnFormatParameters detectedFormat)
    {
        StringTokenizer tokenizer;
        if (truthFormat instanceof CSVDelimitedColumnFormat)
        {
            CSVDelimitedColumnFormat format = (CSVDelimitedColumnFormat)truthFormat;
            tokenizer = new TextDelimitedStringTokenizer(format.getTokenDelimiter(), format.getTextDelimiter());
        }
        else if (truthFormat instanceof CSVFixedWidthColumnFormat)
        {
            int[] columnDivisions = ((CSVFixedWidthColumnFormat)truthFormat).getColumnDivisions();
            tokenizer = PatternStringTokenizer.createFromDivisions(columnDivisions);
        }
        else if (detectedFormat instanceof DelimitedColumnFormatParameters)
        {
            DelimitedColumnFormatParameters parameters = (DelimitedColumnFormatParameters)detectedFormat;
            Character textDelimiter = parameters.getTextDelimiter();

            tokenizer = new TextDelimitedStringTokenizer(parameters.getTokenDelimiter().toString(),
                    textDelimiter == null ? null : textDelimiter.toString());
        }
        else if (detectedFormat instanceof FixedWidthColumnFormatParameters)
        {
            int[] columnDivisions = ((FixedWidthColumnFormatParameters)detectedFormat).getColumnDivisions();
            tokenizer = PatternStringTokenizer.createFromDivisions(columnDivisions);
        }
        else
        {
            return null;
        }
        return tokenizer;
    }
}

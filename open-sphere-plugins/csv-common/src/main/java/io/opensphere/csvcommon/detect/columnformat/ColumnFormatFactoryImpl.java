package io.opensphere.csvcommon.detect.columnformat;

import io.opensphere.csvcommon.config.v2.CSVDelimitedColumnFormat;
import io.opensphere.csvcommon.config.v2.CSVFixedWidthColumnFormat;
import io.opensphere.csvcommon.config.v2.CSVParseParameters;
import io.opensphere.csvcommon.detect.ValueWithConfidence;
import io.opensphere.csvcommon.detect.ValuesWithConfidence;
import io.opensphere.csvcommon.detect.columnformat.ColumnFormatParameters;

/**
 * The Class ColumnFormatFactoryImpl creates a column format based on known
 * parameters.
 */
public class ColumnFormatFactoryImpl implements ColumnFormatFactory
{
    @Override
    public ValuesWithConfidence<? extends ColumnFormatParameters> createColumnFormat(CSVParseParameters params)
    {
        ValuesWithConfidence<ColumnFormatParameters> columnFormat = null;
        if (params.getColumnFormat() instanceof CSVDelimitedColumnFormat)
        {
            CSVDelimitedColumnFormat f = (CSVDelimitedColumnFormat)params.getColumnFormat();
            ValueWithConfidence<DelimitedColumnFormatParameters> result = new ValueWithConfidence<>();
            Character tokenChar = f.getTokenDelimiter() == null ? null : Character.valueOf(f.getTokenDelimiter().charAt(0));
            Character textChar = f.getTextDelimiter() == null ? null : Character.valueOf(f.getTextDelimiter().charAt(0));
            result.setValue(new DelimitedColumnFormatParameters(tokenChar, textChar, f.getColumnCount()));
            result.setConfidence(1.0f);
            columnFormat = new ValuesWithConfidence<ColumnFormatParameters>(result);
        }
        else if (params.getColumnFormat() instanceof CSVFixedWidthColumnFormat)
        {
            CSVFixedWidthColumnFormat fixed = (CSVFixedWidthColumnFormat)params.getColumnFormat();
            ValueWithConfidence<FixedWidthColumnFormatParameters> result = new ValueWithConfidence<>();
            result.setValue(new FixedWidthColumnFormatParameters(fixed.getColumnDivisions()));
            columnFormat = new ValuesWithConfidence<ColumnFormatParameters>(result);
        }

        return columnFormat;
    }
}

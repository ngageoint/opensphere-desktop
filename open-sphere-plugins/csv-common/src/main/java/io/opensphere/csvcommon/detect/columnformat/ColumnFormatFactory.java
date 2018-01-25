package io.opensphere.csvcommon.detect.columnformat;

import io.opensphere.csvcommon.config.v2.CSVParseParameters;
import io.opensphere.csvcommon.detect.ValuesWithConfidence;
import io.opensphere.csvcommon.detect.columnformat.ColumnFormatParameters;

/**
 * A factory for creating ColumnFormat objects.
 */
@FunctionalInterface
public interface ColumnFormatFactory
{
    /**
     * Creates a new ColumnFormat object using known values instead of having to
     * run a column format detector.
     *
     * @param params the known parameters
     * @return the new column format
     */
    ValuesWithConfidence<? extends ColumnFormatParameters> createColumnFormat(CSVParseParameters params);
}

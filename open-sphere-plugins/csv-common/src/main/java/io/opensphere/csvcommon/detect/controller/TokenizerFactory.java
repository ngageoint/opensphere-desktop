package io.opensphere.csvcommon.detect.controller;

import io.opensphere.core.util.lang.StringTokenizer;
import io.opensphere.csvcommon.config.v2.CSVColumnFormat;
import io.opensphere.csvcommon.detect.columnformat.ColumnFormatParameters;

/**
 * Factory that creates tokenizers based on the known and guessed column
 * parameters.
 */
@FunctionalInterface
public interface TokenizerFactory
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
    StringTokenizer getTokenizer(CSVColumnFormat truthFormat, ColumnFormatParameters detectedFormat);
}

package io.opensphere.mantle.data.analysis;

import io.opensphere.mantle.util.columnanalyzer.ColumnAnalyzerData;

/**
 * The Interface ColumnAnalysis.
 */
public interface ColumnAnalysis
{
    /**
     * Gets a copy of the column analyzer data used to reach the current
     * Determination.
     *
     * @return the column analyzer data
     */
    ColumnAnalyzerData getColumnAnalyzerData();

    /**
     * Gets the determination.
     *
     * @return the determination
     */
    Determination getDetermination();

    /**
     * Gets the determined class.
     *
     * @return the determined class
     */
    Class<?> getDeterminedClass();

    /**
     * Gets the unique value count.
     *
     * @return the unique value count
     */
    int getUniqueValueCount();

    /**
     * Checks if is enum candidate.
     *
     * @return true, if is enum candidate
     */
    boolean isEnumCandidate();

    /**
     * Checks if is string.
     *
     * This will return true if the {@link Determination} is INSUFFICENT_DATA or
     * it has been analyzed and determined to be a string value.
     *
     * @return true, if is string
     */
    boolean isString();

    /**
     * The Enum Determination.
     */
    enum Determination
    {
        /** The DETERMINED. */
        DETERMINED,

        /** The INSUFFICENT_DATA. */
        INSUFFICENT_DATA,
    }
}

package io.opensphere.mantle.data.analysis;

/**
 * The Interface DataAnalysisReporter.
 */
public interface DataAnalysisReporter
{
    /**
     * Clear all column analysis data so that all must be re-determined.
     */
    void clearAllColumnAnalysisData();

    /**
     * Clear column analysis for a data type so that it must be re-evaluated.
     *
     * @param dtiKey the dti key
     */
    void clearColumnAnalysis(String dtiKey);

    /**
     * Clear column analysis for a specific type or key so that it must be
     * re-evaluated.
     *
     * @param dtiKey the dti key
     * @param columnKey the column key
     */
    void clearColumnAnalysis(String dtiKey, String columnKey);

    /**
     * Gets the column analysis for a given data type info key and column key.
     * Will only return non-null analysis for types that have (1) Been analyzed
     * (2) Are string types.
     *
     * @param dtiKey the DataTypeInfo key
     * @param columnKey the column key
     * @return the column analysis or null if no analysis performed on that
     *         type/key combination.
     */
    ColumnAnalysis getColumnAnalysis(String dtiKey, String columnKey);

    /**
     * Checks if is column data analysis enabled.
     *
     * @return true, if is column data analysis enabled
     */
    boolean isColumnDataAnalysisEnabled();

    /**
     * Sets the analyze strings only.
     *
     * @param dtiKey the dtiKey
     * @param analyzeStringsOnly the true to only analyze strings (default),
     *            false to analyze everything.
     */
    void setAnalyzeStringsOnly(String dtiKey, boolean analyzeStringsOnly);

    /**
     * Sets the do not track flag for a type.
     *
     * @param dtiKey the new do not track for type
     * @param doNotTrack the do not track
     */
    void setDoNotTrackForType(String dtiKey, boolean doNotTrack);

    /**
     * Sets the finalized for type.
     *
     * @param dtiKey the dti key
     * @param finalized the finalized
     */
    void setFinalizedForType(String dtiKey, boolean finalized);
}

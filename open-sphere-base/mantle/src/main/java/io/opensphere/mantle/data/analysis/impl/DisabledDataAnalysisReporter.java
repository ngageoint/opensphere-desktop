package io.opensphere.mantle.data.analysis.impl;

import io.opensphere.mantle.data.analysis.ColumnAnalysis;
import io.opensphere.mantle.data.analysis.DataAnalysisReporter;

/**
 * The Class DisabledDataAnalysisReporter.
 */
public class DisabledDataAnalysisReporter implements DataAnalysisReporter
{
    @Override
    public void clearAllColumnAnalysisData()
    {
    }

    @Override
    public void clearColumnAnalysis(String dtiKey)
    {
    }

    @Override
    public void clearColumnAnalysis(String dtiKey, String columnKey)
    {
    }

    @Override
    public ColumnAnalysis getColumnAnalysis(String dtiKey, String columnKey)
    {
        return null;
    }

    @Override
    public boolean isColumnDataAnalysisEnabled()
    {
        return false;
    }

    @Override
    public void setAnalyzeStringsOnly(String dtiKey, boolean b)
    {
    }

    @Override
    public void setDoNotTrackForType(String dtiKey, boolean doNotTrack)
    {
    }

    @Override
    public void setFinalizedForType(String dtiKey, boolean finalized)
    {
    }
}

package io.opensphere.mantle.data.analysis.impl;

import io.opensphere.mantle.data.analysis.ColumnAnalysis;
import io.opensphere.mantle.util.columnanalyzer.ColumnAnalyzerData;
import io.opensphere.mantle.util.columnanalyzer.ColumnAnalyzerData.ColumnClass;
import io.opensphere.mantle.util.columnanalyzer.ColumnDataAnalyzer;

/**
 * The Class ColumnAnalysisImpl.
 */
public class ColumnAnalysisImpl implements ColumnAnalysis
{
    /** The Constant DEFAULT_UNDETERMINED. */
    public static final ColumnAnalysisImpl DEFAULT_UNDETERMINED = new ColumnAnalysisImpl(Determination.INSUFFICENT_DATA, true,
            String.class);

    /** The Data. */
    private ColumnAnalyzerData myData;

    /** The Determination. */
    private Determination myDetermination;

    /** The Determined class. */
    private Class<?> myDeterminedClass;

    /** The Is enum candidate. */
    private boolean myIsEnumCandidate;

    /** The Is string. */
    private final boolean myIsString;

    /**
     * Instantiates a new column analysis impl.
     *
     * @param data the data
     */
    public ColumnAnalysisImpl(ColumnAnalyzerData data)
    {
        myData = data;
        ColumnDataAnalyzer analyzer = new ColumnDataAnalyzer(data);
        analyzer.determineColumnClassFromData();
        ColumnClass cc = myData.getColumnClass();
        myDetermination = myData.mustBeString() ? Determination.DETERMINED : Determination.INSUFFICENT_DATA;
        myDeterminedClass = String.class;

        if (myData.getNumValuesConsidered() > ColumnAnalyzerData.DETERMINATION_THRESHOLD)
        {
            myIsEnumCandidate = analyzer.hasLessThanMaxUniqueValues();
            myDeterminedClass = myData.mustBeString() ? String.class : cc.getRepresentativeClass();
            myDetermination = Determination.DETERMINED;
        }
        myIsString = myDeterminedClass == String.class;
    }

    /**
     * Instantiates a new column analysis impl.
     *
     * @param det the
     *            {@link io.opensphere.mantle.data.analysis.ColumnAnalysis.Determination}
     * @param isString the is string
     * @param detClass the determined class
     */
    private ColumnAnalysisImpl(Determination det, boolean isString, Class<?> detClass)
    {
        myDetermination = det;
        myIsString = isString;
        myDeterminedClass = detClass;
        myIsEnumCandidate = false;
    }

    @Override
    public ColumnAnalyzerData getColumnAnalyzerData()
    {
        return myData;
    }

    @Override
    public Determination getDetermination()
    {
        return myDetermination;
    }

    @Override
    public Class<?> getDeterminedClass()
    {
        return myDeterminedClass;
    }

    @Override
    public int getUniqueValueCount()
    {
        return myData == null ? 0 : myData.getUniqueValueCount();
    }

    @Override
    public boolean isEnumCandidate()
    {
        return myIsEnumCandidate;
    }

    @Override
    public boolean isString()
    {
        return myIsString;
    }
}

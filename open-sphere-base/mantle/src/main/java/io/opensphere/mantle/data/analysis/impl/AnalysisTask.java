package io.opensphere.mantle.data.analysis.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import gnu.trove.map.hash.TObjectIntHashMap;
import io.opensphere.core.Toolbox;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.mantle.controller.event.impl.DataElementsAddedEvent;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.MetaDataInfo;
import io.opensphere.mantle.data.SpecialKey;
import io.opensphere.mantle.data.cache.CacheEntryView;
import io.opensphere.mantle.data.cache.CacheIdQuery;
import io.opensphere.mantle.data.cache.CacheQueryException;
import io.opensphere.mantle.data.cache.QueryAccessConstraint;
import io.opensphere.mantle.data.impl.specialkey.TimeKey;
import io.opensphere.mantle.util.MantleToolboxUtils;
import io.opensphere.mantle.util.columnanalyzer.ColumnAnalyzerData;
import io.opensphere.mantle.util.columnanalyzer.ColumnDataAnalyzer;
import io.opensphere.mantle.util.columnanalyzer.DataTypeColumnAnalyzerDataSet;

/**
 * The Class AnalysisTask.
 */
public class AnalysisTask implements Runnable
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(AnalysisTask.class);

    /** The Added event. */
    private final DataElementsAddedEvent myAddedEvent;

    /** The Columns to analyze. */
    private final Set<String> myColumnsToAnalyze;

    /** The Data set. */
    private DataTypeColumnAnalyzerDataSet myDataSet;

    /** The DTI. */
    private final DataTypeInfo myDTI;

    /** The Failed. */
    private boolean myFailed;

    /** The Reporter. */
    private final DataAnalysisReporterImpl myReporter;

    /** The Toolbox. */
    private final Toolbox myToolbox;

    /**
     * Instantiates a new analysis task.
     *
     * @param tb the tb
     * @param reporter the reporter
     * @param event the event
     */
    public AnalysisTask(Toolbox tb, DataAnalysisReporterImpl reporter, DataElementsAddedEvent event)
    {
        myToolbox = tb;
        myDTI = event.getType();
        myAddedEvent = event;
        myReporter = reporter;
        myColumnsToAnalyze = New.set();
    }

    @Override
    public void run()
    {
        long start = System.nanoTime();
        prepareForAnalysis();
        executeAnalysis();

        if (!myFailed)
        {
            reportResults();
        }
        if (LOGGER.isDebugEnabled())
        {
            LOGGER.debug(StringUtilities.formatTimingMessage("Performed analysis task for type [" + myDTI.getTypeKey()
                    + "] Examined " + myAddedEvent.getAddedDataElementIds().size() + " Elements in ", System.nanoTime() - start));
        }
    }

    /**
     * Execute analysis.
     */
    private void executeAnalysis()
    {
        try
        {
            if (!myColumnsToAnalyze.isEmpty())
            {
                // Set up the analysis by creating analyzers for each column to
                // be analyzed.
                // Also cache the column name to column index in a map for
                // faster lookup.
                final Map<String, ColumnDataAnalyzer> columnNameToDataAnalyzerMap = New.map();
                final TObjectIntHashMap<String> colunNameToColumnIndexMap = new TObjectIntHashMap<>(myColumnsToAnalyze.size());
                final MetaDataInfo mdi = myDTI.getMetaDataInfo();
                for (String column : myColumnsToAnalyze)
                {
                    ColumnAnalyzerData cad = myDataSet.getAnalyzerDataForColumnKey(column);
                    // If we don't have a analyzer data record, go create one.
                    if (cad == null)
                    {
                        cad = new ColumnAnalyzerData(myDTI.getTypeKey(), column);
                        myDataSet.setAnalyzerDataForColumnKey(cad);
                    }
                    columnNameToDataAnalyzerMap.put(column, new ColumnDataAnalyzer(cad));
                    int colIndex = mdi.getKeyIndex(column);
                    colunNameToColumnIndexMap.put(column, colIndex);
                }

                CacheIdQuery query = new CacheIdQuery(
                        CollectionUtilities.listView(myAddedEvent.getAddedDataElementIds().getValues()),
                        new QueryAccessConstraint(false, false, false, true, false))
                {
                    @Override
                    public void finalizeQuery()
                    {
                        myColumnsToAnalyze.stream().map(c -> columnNameToDataAnalyzerMap.get(c))
                                .forEach(a -> a.determineColumnClassFromData());
                    }

                    @Override
                    public void process(Long id, CacheEntryView entry) throws CacheQueryException
                    {
                        if (entry.getLoadedElementData() != null && entry.getLoadedElementData().getMetaData() != null)
                        {
                            List<Object> metaData = entry.getLoadedElementData().getMetaData();

                            for (String column : myColumnsToAnalyze)
                            {
                                ColumnDataAnalyzer analyzer = columnNameToDataAnalyzerMap.get(column);
                                int index = colunNameToColumnIndexMap.get(column);
                                if (index < metaData.size())
                                {
                                    Object mdValue = metaData.get(index);
                                    analyzer.considerValue(mdValue);
                                }
                            }
                        }
                    }
                };
                MantleToolboxUtils.getMantleToolbox(myToolbox).getDataElementCache().query(query);
            }
        }
        catch (RuntimeException e)
        {
            myFailed = true;
            LOGGER.error("Failed during analysis task of type " + myDTI.getSourcePrefixAndDisplayNameCombo() + "["
                    + myDTI.getTypeKey() + "]", e);
        }
    }

    /**
     * Prepare for analysis.
     */
    private void prepareForAnalysis()
    {
        myDataSet = myReporter.getAnalyzerDataSet(myDTI.getTypeKey());
        if (!myDataSet.isIsFinalized() && !myDataSet.isDoNotTrack())
        {
            // First figure out which columns are string columns
            Set<String> stringColumns = New.set();
            if (myDTI.getMetaDataInfo() != null)
            {
                for (String key : myDTI.getMetaDataInfo().getKeyNames())
                {
                    SpecialKey sk = myDTI.getMetaDataInfo().getSpecialTypeForKey(key);
                    if (!(sk instanceof TimeKey))
                    {
                        if (myDataSet.isAnalyzeStringsOnly())
                        {
                            Class<?> keyClass = myDTI.getMetaDataInfo().getKeyClassType(key);

                            if (keyClass == String.class)
                            {
                                stringColumns.add(key);
                            }
                        }
                        else
                        {
                            stringColumns.add(key);
                        }
                    }
                }
            }

            if (!stringColumns.isEmpty())
            {
                stringColumns.forEach(myColumnsToAnalyze::add);
            }
        }
    }

    /**
     * Report results.
     */
    private void reportResults()
    {
        if (!myFailed)
        {
            myDataSet.setLastUpdateTime(new Date());
            myReporter.updateAnalyzerDataSet(myDataSet);
        }
    }
}

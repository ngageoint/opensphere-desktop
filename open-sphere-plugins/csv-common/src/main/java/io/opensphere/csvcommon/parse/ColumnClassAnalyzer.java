package io.opensphere.csvcommon.parse;

import java.util.ArrayList;
import java.util.List;

import io.opensphere.core.util.collections.New;
import io.opensphere.csvcommon.config.v1.CSVColumnInfo;
import io.opensphere.csvcommon.config.v2.CSVParseParameters;
import io.opensphere.csvcommon.parse.ColumnClassData;

/** The Class ColumnClassAnalyzer. */
@SuppressWarnings("PMD.GodClass")
public class ColumnClassAnalyzer
{
    /** The file parameters source. */
    private final CSVParseParameters params;

    /** The Column count. */
    private final int myColumnCount;

    /** The Column class data. */
    private final List<ColumnClassData> myColumnClassData;

    /**
     * Instantiates a new column class analyzer.
     *
     * @param cpp the parse parameters
     */
    public ColumnClassAnalyzer(CSVParseParameters cpp)
    {
        params = cpp;
        myColumnCount = params.getColumnNames().size();
        myColumnClassData = new ArrayList<>();
        for (String columnName : params.getColumnNames())
        {
            myColumnClassData.add(new ColumnClassData(columnName));
        }
    }

    /**
     * Consider values.
     *
     * @param values the values
     */
    public void considerValues(String[] values)
    {
        if (values != null && values.length == myColumnCount)
        {
            for (int i = 0; i < values.length; i++)
            {
                myColumnClassData.get(i).considerValue(values[i]);
            }
        }
    }

    /** Imprint results. */
    public void imprintResults()
    {
        List<CSVColumnInfo> columnInfoList = New.list();
        for (ColumnClassData ccd : myColumnClassData)
        {
            CSVColumnInfo info = new CSVColumnInfo(ccd.getColumnClass().getRepresentativeClass().getName());
            info.setNumSamplesConsidered(ccd.getNumValuesConsidered());
            if (ccd.hasLessThanMaxUniqueValues())
            {
                info.setUniqueValueCount(ccd.getUniqueValueCount());
                info.setIsEnumCandidate(ccd.hasLessThanMaxUniqueValues());
            }
            columnInfoList.add(info);
        }
        params.setColumnClasses(columnInfoList);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        for (ColumnClassData ccd : myColumnClassData)
        {
            sb.append(ccd.toString());
        }
        return sb.toString();
    }
}

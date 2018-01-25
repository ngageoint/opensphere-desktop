package io.opensphere.mantle.util.columnanalyzer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.New;

/**
 * The Class DataAnalyzerRepository.
 */
@XmlRootElement(name = "DataAnalyzerRepository")
@XmlAccessorType(XmlAccessType.FIELD)
public class DataAnalyzerRepository
{
    /** The Data type to analysis map. */
    @XmlElement(name = "DataTypeAnalysisMap")
    @XmlJavaTypeAdapter(DataTypeAnalysisDataXmlAdapter.class)
    private Map<String, DataTypeColumnAnalyzerDataSet> myDataTypeToAnalysisMap;

    /** The Map lock. */
    private final transient ReentrantLock myMapLock;

    /**
     * Instantiates a new data analyzer repository.
     */
    public DataAnalyzerRepository()
    {
        myMapLock = new ReentrantLock();
        myDataTypeToAnalysisMap = new HashMap<>();
    }

    /**
     * Instantiates a new data analyzer repository.
     *
     * @param other the other
     */
    public DataAnalyzerRepository(DataAnalyzerRepository other)
    {
        this();
        Map<String, DataTypeColumnAnalyzerDataSet> map = New.map();
        other.myMapLock.lock();
        try
        {
            if (other.myDataTypeToAnalysisMap != null)
            {
                for (Map.Entry<String, DataTypeColumnAnalyzerDataSet> entry : other.myDataTypeToAnalysisMap.entrySet())
                {
                    map.put(entry.getKey(), new DataTypeColumnAnalyzerDataSet(entry.getValue()));
                }
            }
        }
        finally
        {
            other.myMapLock.unlock();
        }
        myMapLock.lock();
        try
        {
            myDataTypeToAnalysisMap = map;
        }
        finally
        {
            myMapLock.unlock();
        }
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null || getClass() != obj.getClass())
        {
            return false;
        }
        DataAnalyzerRepository other = (DataAnalyzerRepository)obj;
        return Objects.equals(myDataTypeToAnalysisMap, other.myDataTypeToAnalysisMap);
    }

    /**
     * Gets the analyzer data.
     *
     * @param typeKey the type key
     * @return the analyzer data
     */
    public DataTypeColumnAnalyzerDataSet getAnalyzerData(String typeKey)
    {
        DataTypeColumnAnalyzerDataSet result = null;
        myMapLock.lock();
        try
        {
            result = myDataTypeToAnalysisMap.get(typeKey);
        }
        finally
        {
            myMapLock.unlock();
        }
        return result;
    }

    /**
     * Returns a copy of the internal {@link ColumnAnalyzerData} for a data type
     * and column name.
     *
     * @param dataType the data type
     * @param columnName the column name
     * @return the data
     */
    public ColumnAnalyzerData getColumnAnalyzerData(String dataType, String columnName)
    {
        ColumnAnalyzerData result = null;
        myMapLock.lock();
        try
        {
            DataTypeColumnAnalyzerDataSet typeSet = myDataTypeToAnalysisMap.get(dataType);
            if (typeSet != null)
            {
                ColumnAnalyzerData data = typeSet.getAnalyzerDataForColumnKey(columnName);
                if (data != null)
                {
                    result = new ColumnAnalyzerData(data);
                }
            }
        }
        finally
        {
            myMapLock.unlock();
        }
        return result;
    }

    /**
     * Returns a new DataTypeColumnAnalyzerDataSet for a data type, mapped by
     * column name, or an empty set for the type if none yet exists.
     *
     * @param dataType the data type
     * @return the column analyzer data for type
     */
    public DataTypeColumnAnalyzerDataSet getColumnAnalyzerDataForType(String dataType)
    {
        DataTypeColumnAnalyzerDataSet result = null;
        myMapLock.lock();
        try
        {
            DataTypeColumnAnalyzerDataSet typeSet = myDataTypeToAnalysisMap.get(dataType);
            if (typeSet != null)
            {
                result = new DataTypeColumnAnalyzerDataSet(typeSet);
            }
        }
        finally
        {
            myMapLock.unlock();
        }
        return result == null ? new DataTypeColumnAnalyzerDataSet(dataType) : result;
    }

    /**
     * Gets the type keys.
     *
     * @return the type keys
     */
    public Set<String> getTypeKeys()
    {
        Set<String> result = null;
        myMapLock.lock();
        try
        {
            result = new HashSet<>(myDataTypeToAnalysisMap.keySet());
        }
        finally
        {
            myMapLock.unlock();
        }
        return result;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + (myDataTypeToAnalysisMap == null ? 0 : myDataTypeToAnalysisMap.hashCode());
        return result;
    }

    /**
     * Removes the analyzer data.
     *
     * @param typeKey the type key
     * @return the data type column analyzer data set
     */
    public DataTypeColumnAnalyzerDataSet removeAnalyzerData(String typeKey)
    {
        DataTypeColumnAnalyzerDataSet removed = null;
        myMapLock.lock();
        try
        {
            removed = myDataTypeToAnalysisMap.remove(typeKey);
        }
        finally
        {
            myMapLock.unlock();
        }
        return removed;
    }

    /**
     * Sets the analyzer data.
     *
     * @param dataSet the new analyzer data
     */
    public void setAnalyzerData(DataTypeColumnAnalyzerDataSet dataSet)
    {
        Utilities.checkNull(dataSet, "dataSet");
        myMapLock.lock();
        try
        {
            myDataTypeToAnalysisMap.put(dataSet.getTypeName(), dataSet);
        }
        finally
        {
            myMapLock.unlock();
        }
    }

    /**
     * Sets the equal to.
     *
     * @param other the new equal to
     */
    public void setEqualTo(DataAnalyzerRepository other)
    {
        Map<String, DataTypeColumnAnalyzerDataSet> mapCopy = new HashMap<>();
        other.myMapLock.lock();
        try
        {
            if (other.myDataTypeToAnalysisMap != null)
            {
                for (Map.Entry<String, DataTypeColumnAnalyzerDataSet> entry : other.myDataTypeToAnalysisMap.entrySet())
                {
                    mapCopy.put(entry.getKey(), new DataTypeColumnAnalyzerDataSet(entry.getValue()));
                }
            }
        }
        finally
        {
            other.myMapLock.unlock();
        }
        myMapLock.lock();
        try
        {
            myDataTypeToAnalysisMap = mapCopy;
        }
        finally
        {
            myMapLock.unlock();
        }
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(256);
        sb.append("DataAnalyzerRepository: Type: Total Types:").append(myDataTypeToAnalysisMap.size())
                .append("\n" + "Data Types Count: ");
        List<String> keyList = new ArrayList<>(myDataTypeToAnalysisMap.keySet());
        Collections.sort(keyList);
        sb.append(keyList.size()).append("\n:");
        for (String key : keyList)
        {
            sb.append(
                    "=============================================== T Y P E ===================================================\n");
            sb.append(myDataTypeToAnalysisMap.get(key));
            sb.append('\n');
        }
        return sb.toString();
    }

    /**
     * Gets the data type to analysis map.
     *
     * @return the data type to analysis map
     */
    @SuppressWarnings("unused")
    private Map<String, DataTypeColumnAnalyzerDataSet> getDataTypeToAnalysisMap()
    {
        return myDataTypeToAnalysisMap;
    }

    /**
     * Sets the data type to analysis map.
     *
     * @param map the map
     */
    @SuppressWarnings("unused")
    private void setDataTypeToAnalysisMap(Map<String, DataTypeColumnAnalyzerDataSet> map)
    {
        myDataTypeToAnalysisMap = map;
    }
}

package io.opensphere.mantle.util.columnanalyzer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.EqualsHelper;

/**
 * The Class DataTypeColumnAnalyzerDataSet.
 */
@XmlRootElement(name = "DataTypeColumnAnalyzerDataSet")
@XmlAccessorType(XmlAccessType.FIELD)
public class DataTypeColumnAnalyzerDataSet
{
    /** The is analyzeStringsOnly. */
    @XmlAttribute(name = "analyzeStringsOnly")
    private boolean myAnalyzeStringsOnly = true;

    /** The Column key to analyzer data map. */
    @XmlElement(name = "columnAnalyzerDataMap")
    @XmlJavaTypeAdapter(ColumnAnalyzerDataXmlAdapter.class)
    private Map<String, ColumnAnalyzerData> myColumnKeyToAnalyzerDataMap;

    /** The Do not track. */
    @XmlAttribute(name = "doNotTrack")
    private boolean myDoNotTrack;

    /** The is finalized. */
    @XmlAttribute(name = "isFinalized")
    private boolean myIsFinalized;

    /** The Last update time. */
    @XmlAttribute(name = "lastUpdateTime")
    private Date myLastUpdateTime = new Date();

    /** The Map lock. */
    private final transient ReentrantLock myMapLock = new ReentrantLock();

    /** The Type name. */
    @XmlAttribute(name = "typeName")
    private String myTypeName;

    /**
     * Instantiates a new data type column analyzer data set.
     */
    public DataTypeColumnAnalyzerDataSet()
    {
        myColumnKeyToAnalyzerDataMap = new HashMap<>();
    }

    /**
     * Copy constructor.
     *
     * @param other the other
     */
    public DataTypeColumnAnalyzerDataSet(DataTypeColumnAnalyzerDataSet other)
    {
        this();
        setEqualTo(other);
    }

    /**
     * Instantiates a new data type column analyzer data set.
     *
     * @param typeName the type name
     */
    public DataTypeColumnAnalyzerDataSet(String typeName)
    {
        this();
        myTypeName = typeName;
    }

    /**
     * Clear analyzer data.
     */
    public void clearAnalyzerData()
    {
        myMapLock.lock();
        try
        {
            myColumnKeyToAnalyzerDataMap.clear();
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
        DataTypeColumnAnalyzerDataSet other = (DataTypeColumnAnalyzerDataSet)obj;
        return myIsFinalized == other.myIsFinalized && myDoNotTrack == other.myDoNotTrack
                && myAnalyzeStringsOnly == other.myAnalyzeStringsOnly
                && EqualsHelper.equals(myTypeName, other.myTypeName, myLastUpdateTime, other.myLastUpdateTime,
                        myColumnKeyToAnalyzerDataMap, other.myColumnKeyToAnalyzerDataMap);
    }

    /**
     * Gets the analyzer data for column key.
     *
     * @param columnKey the column key
     * @return the analyzer data for column key
     */
    public ColumnAnalyzerData getAnalyzerDataForColumnKey(String columnKey)
    {
        ColumnAnalyzerData data = null;
        myMapLock.lock();
        try
        {
            data = myColumnKeyToAnalyzerDataMap.get(columnKey);
        }
        finally
        {
            myMapLock.unlock();
        }
        return data;
    }

    /**
     * Gets the column keys.
     *
     * @return the column keys
     */
    public Set<String> getColumnKeys()
    {
        Set<String> keySet = null;
        myMapLock.lock();
        try
        {
            keySet = New.set(myColumnKeyToAnalyzerDataMap.keySet());
        }
        finally
        {
            myMapLock.unlock();
        }
        return keySet;
    }

    /**
     * Gets the last update time.
     *
     * @return the last update time
     */
    public final Date getLastUpdateTime()
    {
        return new Date(myLastUpdateTime.getTime());
    }

    /**
     * Gets the type name.
     *
     * @return the type name
     */
    public final String getTypeName()
    {
        return myTypeName;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + (myColumnKeyToAnalyzerDataMap == null ? 0 : myColumnKeyToAnalyzerDataMap.hashCode());
        result = prime * result + (myDoNotTrack ? 1231 : 1237);
        result = prime * result + (myIsFinalized ? 1231 : 1237);
        result = prime * result + (myAnalyzeStringsOnly ? 1231 : 1237);
        result = prime * result + (myLastUpdateTime == null ? 0 : myLastUpdateTime.hashCode());
        result = prime * result + (myTypeName == null ? 0 : myTypeName.hashCode());
        return result;
    }

    /**
     * Checks if is analyze strings only.
     *
     * @return true, if is analyze strings only
     */
    public final boolean isAnalyzeStringsOnly()
    {
        return myAnalyzeStringsOnly;
    }

    /**
     * Checks if is do not track.
     *
     * @return true, if is do not track
     */
    public final boolean isDoNotTrack()
    {
        return myDoNotTrack;
    }

    /**
     * Checks if is checks if is finalized.
     *
     * @return true, if is checks if is finalized
     */
    public final boolean isIsFinalized()
    {
        return myIsFinalized;
    }

    /**
     * Removes the analyzer data for column key.
     *
     * @param columnKey the column key
     * @return the column analyzer data
     */
    public ColumnAnalyzerData removeAnalyzerDataForColumnKey(String columnKey)
    {
        ColumnAnalyzerData removed = null;
        myMapLock.lock();
        try
        {
            removed = myColumnKeyToAnalyzerDataMap.remove(columnKey);
        }
        finally
        {
            myMapLock.unlock();
        }
        return removed;
    }

    /**
     * Sets the analyzer data for column key.
     *
     * @param data the data
     */
    public void setAnalyzerDataForColumnKey(ColumnAnalyzerData data)
    {
        myMapLock.lock();
        try
        {
            myColumnKeyToAnalyzerDataMap.put(data.getColumnName(), data);
        }
        finally
        {
            myMapLock.unlock();
        }
    }

    /**
     * Sets the analyze strings only.
     *
     * @param analyzeStringsOnly the new analyze strings only
     */
    public final void setAnalyzeStringsOnly(boolean analyzeStringsOnly)
    {
        myAnalyzeStringsOnly = analyzeStringsOnly;
    }

    /**
     * Sets the do not track.
     *
     * @param doNotTrack the new do not track
     */
    public final void setDoNotTrack(boolean doNotTrack)
    {
        myDoNotTrack = doNotTrack;
    }

    /**
     * Sets the equal to.
     *
     * @param other the new equal to
     */
    public final void setEqualTo(DataTypeColumnAnalyzerDataSet other)
    {
        myTypeName = other.myTypeName;
        myDoNotTrack = other.myDoNotTrack;
        myIsFinalized = other.myIsFinalized;
        myAnalyzeStringsOnly = other.myAnalyzeStringsOnly;
        Map<String, ColumnAnalyzerData> map = new HashMap<>();
        other.myMapLock.lock();
        try
        {
            if (other.myColumnKeyToAnalyzerDataMap != null)
            {
                for (Map.Entry<String, ColumnAnalyzerData> entry : other.myColumnKeyToAnalyzerDataMap.entrySet())
                {
                    map.put(entry.getKey(), new ColumnAnalyzerData(entry.getValue()));
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
            myColumnKeyToAnalyzerDataMap = map;
        }
        finally
        {
            myMapLock.unlock();
        }
    }

    /**
     * Sets the checks if is finalized.
     *
     * @param isFinalized the new checks if is finalized
     */
    public final void setIsFinalized(boolean isFinalized)
    {
        myIsFinalized = isFinalized;
    }

    /**
     * Sets the last update time.
     *
     * @param lastUpdateTime the new last update time
     */
    public final void setLastUpdateTime(Date lastUpdateTime)
    {
        myLastUpdateTime = new Date(lastUpdateTime == null ? System.currentTimeMillis() : lastUpdateTime.getTime());
    }

    /**
     * Sets the type name.
     *
     * @param typeName the new type name
     */
    public final void setTypeName(String typeName)
    {
        myTypeName = typeName;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(512);
        sb.append("DataTypeColumnAnalyzerDataSet: Type: ").append(myTypeName == null ? "NULL" : myTypeName)
                .append("\n" + "   Last Update Time    : ").append(myLastUpdateTime == null ? "NULL" : myLastUpdateTime)
                .append("\n" + "   Do Not Track        : ").append(myDoNotTrack).append("\n" + "   Is Finalized        : ")
                .append(myIsFinalized).append("\n" + "   Analyze Strings Only: ").append(myAnalyzeStringsOnly)
                .append("\n" + "Columns: ");
        List<String> keyList = new ArrayList<>(myColumnKeyToAnalyzerDataMap.keySet());
        Collections.sort(keyList);
        sb.append(keyList.size()).append('\n');
        for (String key : keyList)
        {
            sb.append(
                    "----------------------------------------------- C O L U M N -----------------------------------------------\n");
            sb.append(myColumnKeyToAnalyzerDataMap.get(key));
            sb.append('\n');
        }
        return sb.toString();
    }

    /**
     * Gets the column key to analyzer data map.
     *
     * @return the column key to analyzer data map
     */
    @SuppressWarnings("unused")
    private Map<String, ColumnAnalyzerData> getColumnKeyToAnalyzerDataMap()
    {
        return myColumnKeyToAnalyzerDataMap;
    }
}

package io.opensphere.csv.config.v2;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.collections.StreamUtilities;
import io.opensphere.core.util.lang.ExpectedCloneableException;
import io.opensphere.core.util.lang.HashCodeHelper;
import io.opensphere.mantle.datasources.IDataSource;
import io.opensphere.mantle.datasources.IDataSourceConfig;

/**
 * The root level config class for CSV data sources.
 *
 * This is used in version 5.0.4 and later.
 */
@XmlRootElement(name = "CSVDataSources")
@XmlAccessorType(XmlAccessType.NONE)
public class CSVDataSources implements IDataSourceConfig, Cloneable
{
    /** The CSV data sources. */
    @XmlElement(name = "CSVDataSource")
    private List<CSVDataSource> myDataSources = New.list();

    /**
     * Gets the data source list as CSVDataSource objects.
     *
     * @return the data source list
     */
    public List<CSVDataSource> getCSVSourceList()
    {
        return myDataSources;
    }

    @Override
    public boolean addSource(IDataSource source)
    {
        if (source instanceof CSVDataSource)
        {
            return myDataSources.add((CSVDataSource)source);
        }
        return false;
    }

    @Override
    public List<IDataSource> getSourceList()
    {
        return new ArrayList<IDataSource>(myDataSources);
    }

    @Override
    public boolean removeSource(IDataSource source)
    {
        if (source instanceof CSVDataSource)
        {
            return myDataSources.remove(source);
        }
        return false;
    }

    @Override
    public void updateSource(IDataSource source)
    {
        removeSource(source);
        addSource(source);
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + HashCodeHelper.getHashCode(myDataSources);
        return result;
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
        CSVDataSources other = (CSVDataSources)obj;
        return Objects.equals(myDataSources, other.myDataSources);
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder(64);
        builder.append("CSVDataSources [dataSources=");
        builder.append(myDataSources);
        builder.append(']');
        return builder.toString();
    }

    @Override
    public CSVDataSources clone()
    {
        try
        {
            CSVDataSources result = (CSVDataSources)super.clone();
            result.myDataSources = StreamUtilities.map(myDataSources, dataSource -> dataSource.clone());
            return result;
        }
        catch (CloneNotSupportedException e)
        {
            throw new ExpectedCloneableException(e);
        }
    }
}

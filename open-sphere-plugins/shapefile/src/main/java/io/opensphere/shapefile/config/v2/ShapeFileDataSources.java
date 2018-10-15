package io.opensphere.shapefile.config.v2;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

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
 * The root level config class for shape file data sources.
 *
 * This is used in version 5.0.5 and later.
 */
@XmlRootElement(name = "ShapeFileDataSources")
@XmlAccessorType(XmlAccessType.NONE)
public class ShapeFileDataSources implements IDataSourceConfig, Cloneable
{
    /** The data sources. */
    @XmlElement(name = "ShapeFileDataSource")
    private List<ShapeFileDataSource> myDataSources = New.list();

    /**
     * Gets the data source list as ShapeFileDataSource objects.
     *
     * @return the data source list
     */
    public List<ShapeFileDataSource> getShapeFileSourceList()
    {
        return myDataSources;
    }

    @Override
    public boolean addSource(IDataSource source)
    {
        if (source instanceof ShapeFileDataSource)
        {
            return myDataSources.add((ShapeFileDataSource)source);
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
        if (source instanceof ShapeFileDataSource)
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
        return HashCodeHelper.getHashCode(1, 31, myDataSources);
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
        ShapeFileDataSources other = (ShapeFileDataSources)obj;
        return Objects.equals(myDataSources, other.myDataSources);
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder(64);
        builder.append("ShapeFileDataSources [dataSources=");
        builder.append(myDataSources);
        builder.append(']');
        return builder.toString();
    }

    @Override
    public ShapeFileDataSources clone()
    {
        try
        {
            ShapeFileDataSources result = (ShapeFileDataSources)super.clone();
            result.myDataSources = StreamUtilities.map(myDataSources, new Function<ShapeFileDataSource, ShapeFileDataSource>()
            {
                @Override
                public ShapeFileDataSource apply(ShapeFileDataSource dataSource)
                {
                    return dataSource.clone();
                }
            });
            return result;
        }
        catch (CloneNotSupportedException e)
        {
            throw new ExpectedCloneableException(e);
        }
    }
}

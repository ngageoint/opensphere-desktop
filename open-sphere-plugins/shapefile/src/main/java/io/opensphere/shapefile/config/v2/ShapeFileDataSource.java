package io.opensphere.shapefile.config.v2;

import java.net.URI;
import java.util.Set;
import java.util.function.Function;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.collections.StreamUtilities;
import io.opensphere.core.util.lang.EqualsHelper;
import io.opensphere.core.util.lang.HashCodeHelper;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.importer.config.ImportDataSource;
import io.opensphere.importer.config.ImportParseParameters;

/**
 * Data source configuration for a shape file.
 */
@XmlRootElement(name = "ShapeFileDataSource")
@XmlAccessorType(XmlAccessType.NONE)
public class ShapeFileDataSource extends ImportDataSource
{
    /** The parse parameters. */
    @XmlElement(name = "parseParameters", required = true)
    private ImportParseParameters myParseParameters;

    /**
     * JAXB Constructor.
     */
    public ShapeFileDataSource()
    {
    }

    /**
     * Constructor.
     *
     * @param sourceUri The data source URI
     */
    public ShapeFileDataSource(URI sourceUri)
    {
        super(sourceUri);
        myParseParameters = new ImportParseParameters();
    }

    /**
     * Gets the parseParameters.
     *
     * @return the parseParameters
     */
    public ImportParseParameters getParseParameters()
    {
        return myParseParameters;
    }

    /**
     * Sets the parseParameters.
     *
     * @param parseParameters the parseParameters
     */
    public void setParseParameters(ImportParseParameters parseParameters)
    {
        myParseParameters = parseParameters;
    }

    @Override
    public int hashCode()
    {
        return HashCodeHelper.getHashCode(super.hashCode(), 31, myParseParameters);
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
        ShapeFileDataSource other = (ShapeFileDataSource)obj;
        return super.equals(obj) && EqualsHelper.equals(myParseParameters, other.myParseParameters);
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder(256);
        builder.append("ShapeFileDataSource [myParseParameters=");
        builder.append(myParseParameters);
        builder.append(", super=");
        builder.append(super.toString());
        builder.append(']');
        return builder.toString();
    }

    @Override
    public ShapeFileDataSource clone()
    {
        ShapeFileDataSource result = (ShapeFileDataSource)super.clone();
        result.myParseParameters = myParseParameters.clone();
        return result;
    }

    /**
     * Generate type key.
     *
     * @return the string
     */
    @Override
    public String generateTypeKey()
    {
        return StringUtilities.concat("SHP::", getName(), "::", toString(getSourceUri()));
    }

    /**
     * Convenience method for getting the list of column names to ignore.
     *
     * @return the column names to ignore
     */
    public Set<String> getColumnFilter()
    {
        return New.set(StreamUtilities.map(myParseParameters.getColumnsToIgnore(), new Function<Integer, String>()
        {
            @Override
            public String apply(Integer index)
            {
                return myParseParameters.getColumnNames().get(index.intValue());
            }
        }));
    }
}

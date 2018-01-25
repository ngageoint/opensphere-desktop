package io.opensphere.csvcommon.config.v2;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import io.opensphere.core.util.lang.ExpectedCloneableException;

/** The column format for a CSV file. */
@XmlAccessorType(XmlAccessType.NONE)
public abstract class CSVColumnFormat implements Cloneable
{
    @Override
    public abstract boolean equals(Object obj);

    @Override
    public abstract int hashCode();

    @Override
    public CSVColumnFormat clone()
    {
        try
        {
            return (CSVColumnFormat)super.clone();
        }
        catch (CloneNotSupportedException e)
        {
            throw new ExpectedCloneableException(e);
        }
    }
}

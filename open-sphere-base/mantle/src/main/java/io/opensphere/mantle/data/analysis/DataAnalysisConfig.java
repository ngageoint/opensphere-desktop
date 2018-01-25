package io.opensphere.mantle.data.analysis;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * A class in which the configuration of items related to the data analyzer are
 * defined.
 */
@XmlRootElement(name = "DataAnalysisConfig")
@XmlAccessorType(XmlAccessType.FIELD)
public class DataAnalysisConfig
{
    /**
     * A flag used to allow the system to automatically clear the cache on
     * startup. Defaults to false.
     */
    @XmlElement(name = "cacheCleared", defaultValue = "false")
    private boolean myCacheCleared = false;

    /**
     * Sets the value of the {@link #myCacheCleared} field.
     *
     * @param cacheCleared the value to store in the {@link #myCacheCleared}
     *            field.
     */
    public void setCacheCleared(boolean cacheCleared)
    {
        myCacheCleared = cacheCleared;
    }

    /**
     * Gets the value of the {@link #myCacheCleared} field.
     *
     * @return the value stored in the {@link #myCacheCleared} field.
     */
    public boolean isCacheCleared()
    {
        return myCacheCleared;
    }
}

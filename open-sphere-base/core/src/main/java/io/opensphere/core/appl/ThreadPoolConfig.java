package io.opensphere.core.appl;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * Configuration for a thread pool.
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(propOrder = { "myNamePattern", "myNormalThreadCount", "myRestrictedThreadCount", "myMinimumThreadCount" })
public class ThreadPoolConfig
{
    /** The thread count under extreme thread conditions. */
    @XmlAttribute(name = "minimumThreadCount")
    private int myMinimumThreadCount;

    /** Pattern to match thread pool names. */
    @XmlAttribute(name = "namePattern")
    private String myNamePattern;

    /** The thread count under normal conditions. */
    @XmlAttribute(name = "normalThreadCount")
    private int myNormalThreadCount;

    /** The thread count under restricted conditions. */
    @XmlAttribute(name = "restrictedThreadCount")
    private int myRestrictedThreadCount;

    /**
     * Get the minimum thread count.
     *
     * @return The minimum thread count.
     */
    public int getMinimumThreadCount()
    {
        return myMinimumThreadCount;
    }

    /**
     * Get the name pattern.
     *
     * @return The name pattern.
     */
    public String getNamePattern()
    {
        return myNamePattern;
    }

    /**
     * Get the normal thread count.
     *
     * @return The normal thread count.
     */
    public int getNormalThreadCount()
    {
        return myNormalThreadCount;
    }

    /**
     * Get the restricted thread count.
     *
     * @return The restricted thread count.
     */
    public int getRestrictedThreadCount()
    {
        return myRestrictedThreadCount;
    }

    /**
     * Set the minimum thread count.
     *
     * @param minimumThreadCount The minimum thread count.
     */
    public void setMinimumThreadCount(int minimumThreadCount)
    {
        myMinimumThreadCount = minimumThreadCount;
    }

    /**
     * Set the name pattern.
     *
     * @param namePattern The name pattern.
     */
    public void setNamePattern(String namePattern)
    {
        myNamePattern = namePattern;
    }

    /**
     * Set the normal thread count.
     *
     * @param normalThreadCount The normal thread count.
     */
    public void setNormalThreadCount(int normalThreadCount)
    {
        myNormalThreadCount = normalThreadCount;
    }

    /**
     * Set the restricted thread count.
     *
     * @param restrictedThreadCount The restricted thread count.
     */
    public void setRestrictedThreadCount(int restrictedThreadCount)
    {
        myRestrictedThreadCount = restrictedThreadCount;
    }
}

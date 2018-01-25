package io.opensphere.core.util.xml;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * JAXB class for a set of strings. This class is not thread-safe.
 */
@XmlRootElement(name = "StringSet")
@XmlAccessorType(XmlAccessType.NONE)
public class StringSet
{
    /**
     * The string set.
     */
    @XmlElement(name = "Value")
    private final Set<String> myStrings = new LinkedHashSet<>();

    /**
     * Constructor.
     */
    public StringSet()
    {
    }

    /**
     * Constructor.
     *
     * @param strings The strings in the set.
     */
    public StringSet(Set<String> strings)
    {
        setStrings(strings);
    }

    /**
     * Get the strings in the set.
     *
     * @return The strings.
     */
    public Set<String> getStrings()
    {
        return Collections.unmodifiableSet(myStrings);
    }

    /**
     * Set the strings in the set.
     *
     * @param strings The strings.
     */
    public final void setStrings(Set<String> strings)
    {
        myStrings.clear();
        myStrings.addAll(strings);
    }

    @Override
    public String toString()
    {
        return myStrings.toString();
    }
}

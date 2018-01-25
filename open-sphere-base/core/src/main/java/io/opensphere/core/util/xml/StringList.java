package io.opensphere.core.util.xml;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * JAXB class for a list of strings. This class is not thread-safe.
 */
@XmlRootElement(name = "StringList")
@XmlAccessorType(XmlAccessType.NONE)
public class StringList
{
    /**
     * The string list.
     */
    @XmlElement(name = "Value")
    private final List<String> myStrings = new ArrayList<>();

    /**
     * Constructor.
     */
    public StringList()
    {
    }

    /**
     * Constructor.
     *
     * @param strings The strings in the list.
     */
    public StringList(List<String> strings)
    {
        setStrings(strings);
    }

    /**
     * Get the strings in the list.
     *
     * @return The strings.
     */
    public List<String> getStrings()
    {
        return Collections.unmodifiableList(myStrings);
    }

    /**
     * Set the strings in the list.
     *
     * @param strings The strings.
     */
    public final void setStrings(List<String> strings)
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

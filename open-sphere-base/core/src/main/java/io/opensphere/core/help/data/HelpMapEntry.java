package io.opensphere.core.help.data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

/**
 * This class holds information pertaining to a mapping entry for a java help
 * mapping file.
 */
@XmlAccessorType(XmlAccessType.NONE)
public class HelpMapEntry
{
    /** The target attribute of the entry. */
    @XmlAttribute(name = "target", required = true)
    private String myTarget;

    /** The URL attribute of the entry. */
    @XmlAttribute(name = "url", required = true)
    private String myUrl;

    /**
     * Default constructor (for JAXB).
     */
    public HelpMapEntry()
    {
    }

    /**
     * Constructor.
     *
     * @param target The target value.
     * @param url The URL value.
     */
    public HelpMapEntry(String target, String url)
    {
        myTarget = target;
        myUrl = url;
    }

    /**
     * Accessor for the target attribute.
     *
     * @return The target attribute.
     */
    public String getTarget()
    {
        return myTarget;
    }

    /**
     * Accessor for the URL attribute.
     *
     * @return The URL attribute.
     */
    public String getUrl()
    {
        return myUrl;
    }

    /**
     * Mutator for the target value.
     *
     * @param target The new target value.
     */
    public void setTarget(String target)
    {
        myTarget = target;
    }

    /**
     * Mutator for URL attribute.
     *
     * @param url The new URL attribute.
     */
    public void setUrl(String url)
    {
        myUrl = url;
    }
}

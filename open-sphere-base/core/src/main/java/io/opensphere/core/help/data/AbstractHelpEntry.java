package io.opensphere.core.help.data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

/**
 * Abstract help entry.
 */
@XmlAccessorType(XmlAccessType.NONE)
public abstract class AbstractHelpEntry
{
    /** The target attribute of the entry. */
    @XmlAttribute(name = "target", required = true)
    private String myTarget;

    /** The title attribute of the entry. */
    @XmlAttribute(name = "text", required = true)
    private String myTitle;

    /**
     * Accessor for the target attribute.
     *
     * @return The target attribute.
     */
    public final String getTarget()
    {
        return myTarget;
    }

    /**
     * Accessor for the title attribute.
     *
     * @return The title attribute.
     */
    public final String getTitle()
    {
        return myTitle;
    }

    /**
     * Mutator for the target value.
     *
     * @param target The new target value.
     */
    public final void setTarget(String target)
    {
        myTarget = target;
    }

    /**
     * Mutator for title attribute.
     *
     * @param title The new title attribute.
     */
    public final void setTitle(String title)
    {
        myTitle = title;
    }
}

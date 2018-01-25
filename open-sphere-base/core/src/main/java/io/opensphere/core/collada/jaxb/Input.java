package io.opensphere.core.collada.jaxb;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

/**
 * A COLLADA "input". This binds a "source" to a particular property of a
 * "mesh".
 */
@XmlAccessorType(XmlAccessType.NONE)
public class Input
{
    /** The offset. */
    @XmlAttribute(name = "offset")
    private int myOffset;

    /** The semantic (POSITION/VERTEX/NORMAL/TEXCOORD). */
    @XmlAttribute(name = "semantic")
    private String mySemantic;

    /** The id of the source. */
    @XmlAttribute(name = "source")
    private String mySource;

    /**
     * Get the offset, which is used to determine which elements in the
     * primitive array to use.
     *
     * @return The offset.
     */
    public int getOffset()
    {
        return myOffset;
    }

    /**
     * Get the semantic (POSITION/VERTEX/NORMAL/TEXCOORD).
     *
     * @return The semantic.
     */
    public String getSemantic()
    {
        return mySemantic;
    }

    /**
     * Get the id of the source.
     *
     * @return The source.
     */
    public String getSource()
    {
        return mySource;
    }
}

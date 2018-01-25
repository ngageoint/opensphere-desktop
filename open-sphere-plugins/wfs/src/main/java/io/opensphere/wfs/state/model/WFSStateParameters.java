package io.opensphere.wfs.state.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Some basic parameters for WFS layers.
 */
@XmlRootElement(name = "params")
@XmlAccessorType(XmlAccessType.FIELD)
public class WFSStateParameters
{
    /** The Type name. */
    @XmlElement(name = "typename")
    private String myTypeName;

    /** The Version. */
    @XmlElement(name = "version")
    private String myVersion;

    /**
     * Gets the type name.
     *
     * @return the type name
     */
    public String getTypeName()
    {
        return myTypeName;
    }

    /**
     * Gets the type name no name space.
     *
     * @return the type name no name space
     */
    public String getTypeNameNoNameSpace()
    {
        String[] typeNameTok = myTypeName.split(":");
        if (typeNameTok.length > 1)
        {
            myTypeName = typeNameTok[typeNameTok.length - 1];
        }
        return myTypeName;
    }

    /**
     * Gets the version.
     *
     * @return the version
     */
    public String getVersion()
    {
        return myVersion;
    }

    /**
     * Sets the type name.
     *
     * @param typeName the new type name
     */
    public void setTypeName(String typeName)
    {
        myTypeName = typeName;
    }

    /**
     * Sets the version.
     *
     * @param version the new version
     */
    public void setVersion(String version)
    {
        myVersion = version;
    }
}

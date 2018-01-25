package io.opensphere.arcgis2.esri;

import java.io.Serializable;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonMethod;
import org.codehaus.jackson.annotate.JsonProperty;

/** The Class EsriField. */
@JsonAutoDetect(JsonMethod.NONE)
public class EsriField implements Serializable
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** My alias. */
    @JsonProperty("alias")
    private String myAlias;

    /** The set of valid values for this field. */
    @JsonProperty("domain")
    private EsriFieldDomain myDomain;

    /** My length. */
    @JsonProperty("length")
    private int myLength;

    /** My name. */
    @JsonProperty("name")
    private String myName;

    /** My type. */
    @JsonProperty("type")
    private EsriFieldType myType;

    /**
     * Gets the alias.
     *
     * @return the alias
     */
    public String getAlias()
    {
        return myAlias;
    }

    /**
     * Gets the set of valid values for this field.
     *
     * @return the set of valid values for this field
     */
    public EsriFieldDomain getDomain()
    {
        return myDomain;
    }

    /**
     * Gets the length.
     *
     * @return the length
     */
    public int getLength()
    {
        return myLength;
    }

    /**
     * Gets the name.
     *
     * @return the name
     */
    public String getName()
    {
        return myName;
    }

    /**
     * Gets the type.
     *
     * @return the type
     */
    public EsriFieldType getType()
    {
        return myType;
    }

    /**
     * Sets the alias.
     *
     * @param alias the new alias
     */
    public void setAlias(String alias)
    {
        myAlias = alias;
    }

    /**
     * Sets the set of valid values for this field.
     *
     * @param domain the new set of valid values for this field
     */
    public void setDomain(EsriFieldDomain domain)
    {
        myDomain = domain;
    }

    /**
     * Sets the length.
     *
     * @param length the new length
     */
    public void setLength(int length)
    {
        myLength = length;
    }

    /**
     * Sets the name.
     *
     * @param name the new name
     */
    public void setName(String name)
    {
        myName = name;
    }

    /**
     * Sets the type.
     *
     * @param type the new type
     */
    public void setType(EsriFieldType type)
    {
        myType = type;
    }

    /** The Enum for ESRI Field Types. */
    public enum EsriFieldType
    {
        /** The ESRI type for generic database blob fields. */
        esriFieldTypeBlob,

        /** The ESRI type for date/time fields. */
        esriFieldTypeDate,

        /** The ESRI type for double precision floating point fields. */
        esriFieldTypeDouble,

        /** The ESRI type for geometry fields. */
        esriFieldTypeGeometry,

        /** The ESRI type for the Global ID String. */
        esriFieldTypeGlobalID,

        /** The ESRI type for the Global Unique ID String. */
        esriFieldTypeGUID,

        /** The ESRI type for integer fields. */
        esriFieldTypeInteger,

        /** The ESRI type for object ID fields. */
        esriFieldTypeOID,

        /** The ESRI Raster type. */
        esriFieldTypeRaster,

        /** The ESRI type for single precision floating point fields. */
        esriFieldTypeSingle,

        /** The ESRI type for small integer fields. */
        esriFieldTypeSmallInteger,

        /** The ESRI type for string fields. */
        esriFieldTypeString,

        /** The ESRI type for fields that are XML-formatted strings. */
        esriFieldTypeXml,
    }
}

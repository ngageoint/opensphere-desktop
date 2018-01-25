package io.opensphere.arcgis2.esri;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonMethod;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * Coded value domain specifies an explicit set of valid values for a field.
 * Each valid value is assigned a unique name. The <code>type</code> property
 * for coded value domains is <code>codedValue</code>.
 */
@JsonAutoDetect(JsonMethod.NONE)
public class EsriFieldCodedValueDomain extends EsriFieldDomain
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** The list of available coded values. */
    @JsonProperty("codedValues")
    private List<EsriCodedValue> myCodedValues;

    /** My name. */
    @JsonProperty("name")
    private String myName;

    /**
     * Gets the list of available values.
     *
     * @return the list of available values
     */
    public List<EsriCodedValue> getCodedValues()
    {
        return myCodedValues == null ? null : Collections.unmodifiableList(myCodedValues);
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
     * Sets the name.
     *
     * @param name the new name
     */
    public void setName(String name)
    {
        myName = name;
    }

    /**
     * Sets the list of available values.
     *
     * @param codedValues the new list of available values
     */
    public void setRange(List<EsriCodedValue> codedValues)
    {
        myCodedValues = codedValues;
    }

    /** The Class EsriCodedValue. */
    @JsonAutoDetect(JsonMethod.NONE)
    public static class EsriCodedValue implements Serializable
    {
        /** Serial version UID. */
        private static final long serialVersionUID = 1L;

        /** My code. */
        @JsonProperty("code")
        private String myCode;

        /** My name. */
        @JsonProperty("name")
        private String myName;

        /**
         * Gets the code.
         *
         * @return the code
         */
        public String getCode()
        {
            return myCode;
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
         * Sets the code.
         *
         * @param code the new code
         */
        public void setCode(String code)
        {
            myCode = code;
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
    }
}

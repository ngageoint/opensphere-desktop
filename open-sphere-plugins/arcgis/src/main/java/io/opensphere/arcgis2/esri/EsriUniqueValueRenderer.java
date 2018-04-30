package io.opensphere.arcgis2.esri;

import java.util.Collections;
import java.util.List;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonMethod;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * A unique value renderer symbolizes groups of features that have matching
 * field values. The <code>type</code> property for unique value renderers is
 * <code>uniqueValue</code>.
 */
@JsonAutoDetect(JsonMethod.NONE)
public class EsriUniqueValueRenderer extends EsriRenderer
{
    /** My default label. */
    @JsonProperty("defaultLabel")
    private String myDefaultLabel;

    /** My default symbol. */
    @JsonProperty("defaultSymbol")
    private EsriSymbol myDefaultSymbol;

    /** First matching field. */
    @JsonProperty("field1")
    private String myField1;

    /** Second matching field. */
    @JsonProperty("field2")
    private String myField2;

    /** Third matching field. */
    @JsonProperty("field3")
    private String myField3;

    /** My field delimiter. */
    @JsonProperty("fieldDelimiter")
    private String myFieldDelimiter;

    /** My list of unique value infos. */
    @JsonProperty("uniqueValueInfos")
    private List<EsriUniqueValueInfo> myUniqueValueInfos;

    /**
     * Gets the default label.
     *
     * @return the default label
     */
    public String getDefaultLabel()
    {
        return myDefaultLabel;
    }

    /**
     * Gets the default symbol.
     *
     * @return the default symbol
     */
    public EsriSymbol getDefaultSymbol()
    {
        return myDefaultSymbol;
    }

    /**
     * Gets the First matching field.
     *
     * @return the First matching field
     */
    public String getField1()
    {
        return myField1;
    }

    /**
     * Gets the Second matching field.
     *
     * @return the Second matching field
     */
    public String getField2()
    {
        return myField2;
    }

    /**
     * Gets the Third matching field.
     *
     * @return the Third matching field
     */
    public String getField3()
    {
        return myField3;
    }

    /**
     * Gets the field delimiter.
     *
     * @return the field delimiter
     */
    public String getFieldDelimiter()
    {
        return myFieldDelimiter;
    }

    /**
     * Gets the list of unique value infos.
     *
     * @return the list of unique value infos
     */
    public List<EsriUniqueValueInfo> getUniqueValueInfos()
    {
        return myUniqueValueInfos == null ? Collections.<EsriUniqueValueInfo>emptyList()
                : Collections.unmodifiableList(myUniqueValueInfos);
    }

    /**
     * Sets the default symbol.
     *
     * @param symbol the new default symbol
     */
    public void setDefaultSymbol(EsriSymbol symbol)
    {
        myDefaultSymbol = symbol;
    }

    /**
     * Sets the First matching field.
     *
     * @param field the First matching field
     */
    public void setField1(String field)
    {
        myField1 = field;
    }

    /**
     * Sets the Second matching field.
     *
     * @param field the Second matching field
     */
    public void setField2(String field)
    {
        myField2 = field;
    }

    /**
     * Sets the Third matching field.
     *
     * @param field the Third matching field
     */
    public void setField3(String field)
    {
        myField3 = field;
    }

    /**
     * Sets the field delimiter.
     *
     * @param fieldDelimiter the new field delimiter
     */
    public void setFieldDelimiter(String fieldDelimiter)
    {
        myFieldDelimiter = fieldDelimiter;
    }

    /**
     * Sets the list of unique value infos.
     *
     * @param uniqueValueInfos the new list of unique value infos
     */
    public void setUniqueValueInfos(List<EsriUniqueValueInfo> uniqueValueInfos)
    {
        myUniqueValueInfos = uniqueValueInfos;
    }

    /** The Sub-Class EsriUniqueValueInfo. */
    @JsonAutoDetect(JsonMethod.NONE)
    public static class EsriUniqueValueInfo extends EsriRendererInfo
    {
        /** Serial version UID. */
        private static final long serialVersionUID = 1L;

        /** My value. */
        @JsonProperty("value")
        private String myValue;

        /**
         * Gets the value.
         *
         * @return the value
         */
        public String getValue()
        {
            return myValue;
        }

        /**
         * Sets the value.
         *
         * @param value the new value
         */
        public void setValue(String value)
        {
            myValue = value;
        }
    }
}

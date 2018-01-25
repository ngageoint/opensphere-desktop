package io.opensphere.arcgis2.esri;

import java.util.Collections;
import java.util.List;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonMethod;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * A class breaks renderer symbolizes each feature based on the value of some
 * numeric field. The <code>type</code> property for class breaks renderers is
 * <code>classBreaks</code>.
 */
@JsonAutoDetect(JsonMethod.NONE)
public class EsriClassBreaksRenderer extends EsriRenderer
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** My field. */
    @JsonProperty("field")
    private String myField;

    /** My minimum value. */
    @JsonProperty("minValue")
    private double myMinValue;

    /** My list of class break infos. */
    @JsonProperty("classBreakInfos")
    private transient List<EsriClassBreakInfo> myClassBreakInfos;

    /**
     * Gets the list of class break infos.
     *
     * @return the list of class break infos
     */
    public List<EsriClassBreakInfo> getClassBreakInfos()
    {
        return myClassBreakInfos == null ? Collections.<EsriClassBreakInfo>emptyList()
                : Collections.unmodifiableList(myClassBreakInfos);
    }

    /**
     * Gets the field.
     *
     * @return the field
     */
    public String getField()
    {
        return myField;
    }

    /**
     * Gets the minimum value.
     *
     * @return the minimum value
     */
    public double getMinValue()
    {
        return myMinValue;
    }

    /**
     * Sets the list of class break infos.
     *
     * @param classBreakInfos the new list of class break infos
     */
    public void setClassBreakInfos(List<EsriClassBreakInfo> classBreakInfos)
    {
        myClassBreakInfos = classBreakInfos;
    }

    /**
     * Sets the field.
     *
     * @param field the field
     */
    public void setField(String field)
    {
        myField = field;
    }

    /**
     * Sets the minimum value.
     *
     * @param value the new minimum value
     */
    public void setMinValue(double value)
    {
        myMinValue = value;
    }

    /** The Sub-Class EsriClassBreakInfo. */
    @JsonAutoDetect(JsonMethod.NONE)
    public static class EsriClassBreakInfo extends EsriRendererInfo
    {
        /** My class's maximum value. */
        @JsonProperty("classMaxValue")
        private int myClassMaxValue;

        /**
         * Gets the class's maximum value.
         *
         * @return the class's value
         */
        public int getClassMaxValue()
        {
            return myClassMaxValue;
        }

        /**
         * Sets the class's maximum value.
         *
         * @param classMaxValue the class's new maximum value
         */
        public void setClassMaxValue(int classMaxValue)
        {
            myClassMaxValue = classMaxValue;
        }
    }
}

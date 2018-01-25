package io.opensphere.arcgis2.esri;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonMethod;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * A renderer that uses one symbol only. The <code>type</code> property for
 * simple renderers is <code>simple</code>.
 */
@JsonAutoDetect(JsonMethod.NONE)
public class EsriSimpleRenderer extends EsriRenderer
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** My description. */
    @JsonProperty("description")
    private String myDescription;

    /** My label. */
    @JsonProperty("label")
    private String myLabel;

    /** My symbol. */
    @JsonProperty("symbol")
    private EsriSymbol mySymbol;

    /**
     * Gets the description.
     *
     * @return the description
     */
    public String getDescription()
    {
        return myDescription;
    }

    /**
     * Gets the label.
     *
     * @return the label
     */
    public String getLabel()
    {
        return myLabel;
    }

    /**
     * Gets the symbol.
     *
     * @return the symbol
     */
    public EsriSymbol getSymbol()
    {
        return mySymbol;
    }

    /**
     * Sets the description.
     *
     * @param description the new description
     */
    public void setDescription(String description)
    {
        myDescription = description;
    }

    /**
     * Sets the label.
     *
     * @param label the new label
     */
    public void setLabel(String label)
    {
        myLabel = label;
    }

    /**
     * Sets the symbol.
     *
     * @param symbol the new symbol
     */
    public void setSymbol(EsriSymbol symbol)
    {
        mySymbol = symbol;
    }
}

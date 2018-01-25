package io.opensphere.filterbuilder.filter.v1;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import io.opensphere.core.datafilter.DataFilterOperators.Logical;

/**
 * A rule for how to combine filters for the given layer.
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "CombinationRule", propOrder = { "myTypeKey", "myOperator" })
public class CombinationRule
{
    /** The data type key, the path to the data source. */
    @XmlAttribute(name = "typeKey", required = true)
    private String myTypeKey;

    /** The operator. */
    @XmlAttribute(name = "operator", required = true)
    private Logical myOperator;

    /**
     * Constructor.
     */
    public CombinationRule()
    {
    }

    /**
     * Gets the operator.
     *
     * @return the operator
     */
    public Logical getOperator()
    {
        return myOperator;
    }

    /**
     * Gets the type key.
     *
     * @return the type key
     */
    public String getTypeKey()
    {
        return myTypeKey;
    }

    /**
     * Sets the operator.
     *
     * @param operator the new operator
     */
    public void setOperator(Logical operator)
    {
        myOperator = operator;
    }

    /**
     * Sets the type key.
     *
     * @param typeKey the new type key
     */
    public void setTypeKey(String typeKey)
    {
        myTypeKey = typeKey;
    }
}

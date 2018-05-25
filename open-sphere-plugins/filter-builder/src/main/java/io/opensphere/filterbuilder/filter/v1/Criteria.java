package io.opensphere.filterbuilder.filter.v1;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang3.StringUtils;

import io.opensphere.core.datafilter.DataFilterCriteria;
import io.opensphere.core.datafilter.DataFilterOperators.Conditional;
import io.opensphere.filterbuilder.filter.FilterComponentEvent;
import io.opensphere.filterbuilder.filter.FilterComponentListener;

/**
 * Criteria: A {@link Filter} criteria.
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "criteria", propOrder = { "myValue", "myOperator", "myField" })
public class Criteria extends FilterItem implements DataFilterCriteria
{
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The field. */
    @XmlAttribute(name = "field", required = true)
    private String myField;

    /** The operator. */
    @XmlAttribute(name = "operator", required = true)
    private Conditional myOperator;

    /** The value. */
    @XmlAttribute(name = "value", required = true)
    private String myValue;

    /** The Filter component listener. */
    private FilterComponentListener myFilterCompListener;

    /**
     * Instantiates a new criteria.
     */
    public Criteria()
    {
        this(null, null, null);
    }

    /**
     * Construct a Criteria from a field name.
     *
     * @param pField the field
     */
    public Criteria(String pField)
    {
        this(pField, null, null);
    }

    /**
     * Constructor.
     *
     * @param field the field
     * @param op the operator
     * @param value the value
     */
    public Criteria(String field, Conditional op, Object value)
    {
        myField = field != null ? field : "";
        myOperator = op != null ? op : Conditional.EQ;
        myValue = value != null ? value.toString() : "";
    }

    @Override
    public Criteria clone()
    {
        Criteria clone = (Criteria)super.clone();
        clone.myFilterCompListener = null;
        return clone;
    }

    @Override
    public Conditional getComparisonOperator()
    {
        return myOperator;
    }

    @Override
    public String getField()
    {
        return myField;
    }

    @Override
    public String getSqlLikeString()
    {
        if (myOperator == Conditional.EMPTY)
        {
            return myField + " IS NULL";
        }
        if (myOperator == Conditional.NOT_EMPTY)
        {
            return myField + " IS NOT NULL";
        }
        if (myOperator == Conditional.NEQ)
        {
            return myField + " <> " + quoteString(myValue);
        }
        if (myOperator == Conditional.CONTAINS)
        {
            return "UPPER(" + myField + ") " + myOperator.symbol() + "'%" + myValue.toUpperCase() + "%'";
        }
        if (myOperator == Conditional.LIKE)
        {
            return myField + ' ' + myOperator.symbol() + ' ' + quote(myValue);
        }
        String operator = myOperator != null ? myOperator.symbol() : "NONE";
        return myField + ' ' + operator + ' ' + quoteString(myValue);
    }

    /**
     * Construct an SQL literal from a String that may be a number. Numerical
     * literals are left unquoted, while all others are quoted.
     *
     * @param s an input String
     * @return an SQL literal
     */
    private static String quoteString(String s)
    {
        if (s == null || s.isEmpty())
        {
            return "''";
        }
        if (isNumeric(s))
        {
            return s;
        }
        return quote(s);
    }

    /**
     * Convert an arbitrary String for into an SQL literal. The value is
     * enclosed in single quotes and any embedded single quotes are escaped as
     * required within SQL.
     *
     * @param s the unquoted value
     * @return the quoted SQL literal
     */
    private static String quote(String s)
    {
        if (s == null)
        {
            return "''";
        }
        return '\'' + s.replaceAll("'", "''") + '\'';
    }

    /**
     * Test whether the provided String contains a number (integer or
     * floating-point) by attempting to parse it as a Double.
     *
     * @param s an input String
     * @return true if and only if <i>s</i> is a number
     */
    private static boolean isNumeric(String s)
    {
        try
        {
            Double.parseDouble(s);
        }
        catch (NumberFormatException eek)
        {
            return false;
        }
        return true;
    }

    @Override
    public String getValue()
    {
        return myValue;
    }

    /**
     * Checks if this criteria is complete. By that we mean that it has entries
     * for "field", "operator" and "value".
     *
     * @return true, if is complete
     */
    public boolean isComplete()
    {
        if (myField == null || myField.length() < 1)
        {
            return false;
        }
        else if (myOperator == null)
        {
            return false;
        }
        else if (myValue == null || myValue.length() < 1)
        {
            return false;
        }

        return true;
    }

    /**
     * Checks to see if this criteria meets the minimum requirements to be a
     * valid criteria. The minimum requirement is that it has a non-null,
     * non-empty, non-whitespace field, value and operator.
     *
     * @return true, if is valid
     */
    public boolean isValid()
    {
        return !(StringUtils.isBlank(myField) || StringUtils.isBlank(myValue) || myOperator == null);
    }

    /**
     * Sets the comparison operator.
     *
     * @param operator the new comparison operator
     */
    public void setComparisonOperator(Conditional operator)
    {
        myOperator = operator;
        fireFilterComponentEvent(new FilterComponentEvent(this, FilterComponentEvent.CRITERIA_OPERATOR_CHANGED));
    }

    /**
     * Sets the field.
     *
     * @param pField the new field
     */
    public void setField(String pField)
    {
        myField = pField;
        fireFilterComponentEvent(new FilterComponentEvent(this, FilterComponentEvent.CRITERIA_FIELD_CHANGED));
    }

    /**
     * Sets the filter component listener.
     *
     * @param pListener the new filter component listener
     */
    public void setFilterComponentListener(FilterComponentListener pListener)
    {
        myFilterCompListener = pListener;
    }

    /**
     * Sets the value.
     *
     * @param value the new value
     */
    public void setValue(String value)
    {
        myValue = value;
        fireFilterComponentEvent(new FilterComponentEvent(this, FilterComponentEvent.CRITERIA_VALUE_CHANGED));
    }

    @Override
    @SuppressWarnings("PMD.ConsecutiveLiteralAppends")
    public String toString()
    {
        StringBuilder sb = new StringBuilder();

        sb.append(myField == null ? getClass().getSimpleName() : myField);
        sb.append(' ');
        sb.append(myOperator == null ? "OP" : myOperator.symbol());
        sb.append(' ');
        sb.append(myValue == null ? "@" + Integer.toHexString(hashCode()) : myValue);
        return sb.toString();
    }

    /**
     * Fire filter component event.
     *
     * @param pEvent the event
     */
    private void fireFilterComponentEvent(FilterComponentEvent pEvent)
    {
        if (myFilterCompListener != null)
        {
            myFilterCompListener.filterComponentChanged(pEvent);
        }
    }
}

package io.opensphere.core.common.json.obj;

import java.io.PrintStream;

public class JSONSaxPair implements JSONComposite
{
    String myKey;

    JSONSaxValue myValue;

    /**
     * CTOR from other {@link JSONSaxPair}
     *
     * @param other
     */
    public JSONSaxPair(JSONSaxPair other)
    {
        this(other.getKey(), other.getValue());
    }

    /**
     * CTOR from key
     *
     * @param key - the key
     */
    public JSONSaxPair(String key)
    {
        if (key == null || key.isEmpty())
            throw new IllegalArgumentException("key may not be null or empty");

        myKey = key;
    }

    /**
     * CTOR from key/value
     *
     * @param key - the key
     * @param value - the {@link JSONSaxValue}
     */
    public JSONSaxPair(String key, JSONSaxValue value)
    {
        if (key == null || key.isEmpty())
            throw new IllegalArgumentException("key may not be null or empty");

        myKey = key;
        myValue = value;
    }

    public void toJSON(PrintStream ps, int indent)
    {
        boolean pretty = indent >= 0;
        ps.append("\"" + myKey + "\"");
        if (pretty)
            ps.append(" : ");
        else
            ps.append(":");
        if (myValue == null)
        {
            ps.append("null");
        }
        else
        {
            switch (myValue.getType())
            {
                case ARRAY:
                case OBJECT:
                    myValue.toJSON(ps, indent);
                    break;
                default:
                    myValue.toJSON(ps, indent + 2);
            }
        }
    }

    /**
     * Gets the key.
     *
     * @return the key
     */
    public String getKey()
    {
        return myKey;
    }

    /**
     * Gets the value
     *
     * @return the value
     */
    public JSONSaxValue getValue()
    {
        return myValue;
    }

    public void setValue(JSONSaxValue val)
    {
        myValue = val;
    }
}

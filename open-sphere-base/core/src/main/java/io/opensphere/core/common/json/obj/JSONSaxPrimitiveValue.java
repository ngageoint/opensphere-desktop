package io.opensphere.core.common.json.obj;

public abstract class JSONSaxPrimitiveValue implements JSONSaxValue
{
    String myText;

    /**
     * CTOR
     *
     * @param text
     */
    public JSONSaxPrimitiveValue(String text)
    {
        myText = text;
    }

    /**
     * Returns true if this value represents a boolean
     *
     * @return true if a boolean, false if not
     */
    public abstract boolean isBoolean();

    /**
     * Returns true if this value represents a number.
     *
     * @return true if a number, false if not
     */
    public abstract boolean isNumber();

    /**
     * Returns true if this value represents null
     *
     * @return true if null, false if not
     */
    public abstract boolean isNull();

    /**
     * Gets the text value
     *
     * @return the text value
     */
    public String getValue()
    {
        return myText;
    }

    @Override
    public String toString()
    {
        return myText;
    }

    public double getDouble()
    {
        return Double.parseDouble(myText);
    }

    public int getInt()
    {
        return Integer.parseInt(myText);
    }

    public long getLong()
    {
        return Long.parseLong(myText);
    }

    public boolean getBoolean()
    {
        return myText == null ? false : myText.equalsIgnoreCase("true");
    }

}

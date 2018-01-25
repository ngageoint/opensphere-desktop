package io.opensphere.core.common.json.obj;

import java.io.PrintStream;

public class JSONSaxBooleanValue extends JSONSaxPrimitiveValue
{
    /**
     * CTOR
     *
     * @param text
     */
    public JSONSaxBooleanValue(String text)
    {
        super(text);
    }

    @Override
    public void toJSON(PrintStream ps, int indent)
    {
        ps.append(Boolean.toString(getBoolean()));
    }

    @Override
    public Type getType()
    {
        return Type.BOOLEAN_VALUE;
    }

    @Override
    public boolean isNull()
    {
        return false;
    }

    @Override
    public boolean isNumber()
    {
        return false;
    }

    @Override
    public boolean isBoolean()
    {
        return true;
    }
}

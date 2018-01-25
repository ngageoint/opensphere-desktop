package io.opensphere.core.common.json.obj;

import java.io.PrintStream;

public class JSONSaxNumberValue extends JSONSaxPrimitiveValue
{
    /**
     * CTOR
     *
     * @param text
     */
    public JSONSaxNumberValue(String text)
    {
        super(text);
    }

    @Override
    public void toJSON(PrintStream ps, int indent)
    {
        ps.append(getValue());
    }

    @Override
    public Type getType()
    {
        return Type.NUMBER_VALUE;
    }

    @Override
    public boolean isNull()
    {
        return false;
    }

    @Override
    public boolean isNumber()
    {
        return true;
    }

    @Override
    public boolean isBoolean()
    {
        return false;
    }
}

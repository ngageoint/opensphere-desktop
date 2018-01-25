package io.opensphere.core.common.json.obj;

import java.io.PrintStream;

public class JSONSaxNullValue extends JSONSaxPrimitiveValue
{
    public static final String NULL = "null";

    public JSONSaxNullValue(String text)
    {
        super(text);
    }

    @Override
    public void toJSON(PrintStream ps, int indent)
    {
        ps.append(NULL);
    }

    @Override
    public Type getType()
    {
        return Type.NULL_VALUE;
    }

    @Override
    public boolean isNull()
    {
        return true;
    }

    @Override
    public boolean isNumber()
    {
        return false;
    }

    @Override
    public boolean isBoolean()
    {
        return false;
    }
}

package io.opensphere.core.common.json.obj;

import java.io.PrintStream;

public interface JSONSaxValue
{
    public enum Type
    {
        OBJECT, ARRAY, TEXT_VALUE, NUMBER_VALUE, BOOLEAN_VALUE, NULL_VALUE
    }

    Type getType();

    void toJSON(PrintStream ps, int indent);
}

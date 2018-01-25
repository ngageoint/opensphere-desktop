package io.opensphere.core.common.json.obj;

import java.io.PrintStream;

public class JSONSaxTextValue extends JSONSaxPrimitiveValue
{
    private boolean determinedNumber = false;

    private boolean isNumber = false;

    private boolean determinedNull = false;

    private boolean isNull = false;

    private boolean determinedBoolean = false;

    private boolean isBoolean = false;

    public JSONSaxTextValue(String text)
    {
        super(text);
    }

    @Override
    public void toJSON(PrintStream ps, int indent)
    {
        ps.append("\"" + myText + "\"");
    }

    @Override
    public Type getType()
    {
        return Type.TEXT_VALUE;
    }

    @Override
    public boolean isBoolean()
    {
        if (!determinedBoolean)
        {
            isBoolean = myText == null ? false : myText.equalsIgnoreCase("false") || myText.equalsIgnoreCase("true");
            determinedBoolean = true;

            determinedNull = true;
            isNull = false;

            determinedNumber = true;
            isNumber = false;
        }
        return isBoolean;
    }

    @Override
    public boolean isNull()
    {
        if (!determinedNull)
        {
            isNull = myText == null || myText.toLowerCase().equals("null");
            determinedNull = true;

            determinedBoolean = true;
            isBoolean = false;

            determinedNumber = true;
            isNumber = false;
        }
        return isNull;
    }

    @Override
    public boolean isNumber()
    {
        if (!determinedNumber)
        {
            isNumber = true;
            try
            {
                Double.parseDouble(myText);
            }
            catch (NumberFormatException e)
            {
                isNumber = false;
            }
            determinedNumber = true;

            determinedBoolean = true;
            isBoolean = false;

            determinedNull = true;
            isNull = false;
        }
        return isNumber;
    }
}

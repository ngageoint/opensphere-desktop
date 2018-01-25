package io.opensphere.core.common.json.handlers;

import java.io.PrintStream;

import io.opensphere.core.common.json.JSONSaxHandler;
import io.opensphere.core.common.json.JSONSaxParseException;
import io.opensphere.core.common.json.obj.JSONSaxBooleanValue;
import io.opensphere.core.common.json.obj.JSONSaxNullValue;
import io.opensphere.core.common.json.obj.JSONSaxNumberValue;
import io.opensphere.core.common.json.obj.JSONSaxPrimitiveValue;

public class JSONSaxPrintHandler implements JSONSaxHandler
{
    boolean myPrettyPrint = false;

    PrintStream myOutputStream = System.out;

    public JSONSaxPrintHandler()
    {

    }

    public JSONSaxPrintHandler(boolean prettyPrint)
    {
        myPrettyPrint = prettyPrint;
    }

    public JSONSaxPrintHandler(PrintStream os, boolean prettyPrint)
    {
        this(prettyPrint);
        myOutputStream = os;
    }

    @Override
    public void arrayEnd()
    {
        myOutputStream.print("]");
    }

    @Override
    public void arrayStart()
    {
        myOutputStream.print("[");
    }

    @Override
    public void documentEnd()
    {
    }

    @Override
    public void documentStart()
    {
    }

    @Override
    public void error(JSONSaxParseException e)
    {
        myOutputStream.println("Error: " + e.getMessage());
    }

    @Override
    public void fatalError(JSONSaxParseException e)
    {
        myOutputStream.println("Fatal Error: " + e.getMessage());
    }

    @Override
    public void ignorableWhiteSpace(String whiteSpaceChars)
    {
        if (myPrettyPrint)
            myOutputStream.print(whiteSpaceChars);
    }

    @Override
    public void key(String keyValue)
    {
        myOutputStream.print("\"" + keyValue + "\"");
    }

    @Override
    public void objectEnd()
    {
        myOutputStream.print("}");
    }

    @Override
    public void objectStart()
    {
        myOutputStream.print("{");
    }

    @Override
    public void value(JSONSaxPrimitiveValue value)
    {
        if (value instanceof JSONSaxNullValue || value instanceof JSONSaxNumberValue || value instanceof JSONSaxBooleanValue)
        {
            myOutputStream.print(value.toString());
        }
        else
        {
            myOutputStream.print("\"" + value.toString() + "\"");
        }
    }

    @Override
    public void warning(JSONSaxParseException e)
    {
        myOutputStream.println("Warning: " + e.getMessage());
    }

    @Override
    public void arrayElementSeparator()
    {
        myOutputStream.print(",");
    }

    @Override
    public void keyValuePairSeparator()
    {
        myOutputStream.print(",");
    }

    @Override
    public void keyValueSeparator()
    {
        myOutputStream.print(":");
    }
}
